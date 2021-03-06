/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.discovery.oak;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.discovery.TopologyEvent;
import org.apache.sling.discovery.base.its.setup.OSGiMock;
import org.apache.sling.discovery.base.its.setup.VirtualInstance;
import org.apache.sling.discovery.base.its.setup.mock.DummyResourceResolverFactory;
import org.apache.sling.discovery.base.its.setup.mock.MockFactory;
import org.apache.sling.discovery.commons.providers.base.DummyListener;
import org.apache.sling.discovery.commons.providers.spi.base.DescriptorHelper;
import org.apache.sling.discovery.commons.providers.spi.base.DiscoveryLiteConfig;
import org.apache.sling.discovery.commons.providers.spi.base.DiscoveryLiteDescriptor;
import org.apache.sling.discovery.commons.providers.spi.base.DiscoveryLiteDescriptorBuilder;
import org.apache.sling.discovery.commons.providers.spi.base.DummySlingSettingsService;
import org.apache.sling.discovery.commons.providers.spi.base.IdMapService;
import org.apache.sling.discovery.oak.its.setup.OakTestConfig;
import org.apache.sling.discovery.oak.its.setup.OakVirtualInstanceBuilder;
import org.apache.sling.discovery.oak.its.setup.SimulatedLease;
import org.apache.sling.discovery.oak.its.setup.SimulatedLeaseCollection;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OakDiscoveryServiceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public final class SimpleCommonsConfig implements DiscoveryLiteConfig {

        private long bgIntervalMillis;
        private long bgTimeoutMillis;

        SimpleCommonsConfig(long bgIntervalMillis, long bgTimeoutMillis) {
            this.bgIntervalMillis = bgIntervalMillis;
            this.bgTimeoutMillis = bgTimeoutMillis;
        }

        @Override
        public String getSyncTokenPath() {
            return "/var/synctokens";
        }

        @Override
        public String getIdMapPath() {
            return "/var/idmap";
        }

        @Override
        public long getClusterSyncServiceTimeoutMillis() {
            return bgTimeoutMillis;
        }

        @Override
        public long getClusterSyncServiceIntervalMillis() {
            return bgIntervalMillis;
        }

    }

    @Test
    public void testBindBeforeActivate() throws Exception {
        OakVirtualInstanceBuilder builder =
                (OakVirtualInstanceBuilder) new OakVirtualInstanceBuilder()
                .setDebugName("test")
                .newRepository("/foo/bar", true);
        String slingId = UUID.randomUUID().toString();;
        DiscoveryLiteDescriptorBuilder discoBuilder = new DiscoveryLiteDescriptorBuilder();
        discoBuilder.id("id").me(1).activeIds(1);
        // make sure the discovery-lite descriptor is marked as not final
        // such that the view is not already set before we want it to be
        discoBuilder.setFinal(false);
        DescriptorHelper.setDiscoveryLiteDescriptor(builder.getResourceResolverFactory(),
                discoBuilder);
        IdMapService idMapService = IdMapService.testConstructor(new SimpleCommonsConfig(1000, -1), new DummySlingSettingsService(slingId), builder.getResourceResolverFactory());
        assertTrue(idMapService.waitForInit(2000));
        OakDiscoveryService discoveryService = (OakDiscoveryService) builder.getDiscoverService();
        assertNotNull(discoveryService);
        DummyListener listener = new DummyListener();
        for(int i=0; i<100; i++) {
            discoveryService.bindTopologyEventListener(listener);
            discoveryService.unbindTopologyEventListener(listener);
        }
        discoveryService.bindTopologyEventListener(listener);
        assertEquals(0, listener.countEvents());
        discoveryService.activate(null);
        assertEquals(0, listener.countEvents());
        // some more confusion...
        discoveryService.unbindTopologyEventListener(listener);
        discoveryService.bindTopologyEventListener(listener);
        // only set the final flag now - this makes sure that handlePotentialTopologyChange
        // will actually detect a valid new, different view and send out an event -
        // exactly as we want to
        discoBuilder.setFinal(true);
        DescriptorHelper.setDiscoveryLiteDescriptor(builder.getResourceResolverFactory(),
                discoBuilder);
        // SLING-6924 : need to simulate a OakViewChecker.activate to trigger resetLeaderElectionId
        // otherwise no TOPOLOGY_INIT will be generated as without a leaderElectionId we now
        // consider a view as NO_ESTABLISHED_VIEW
        OSGiMock.activate(builder.getViewChecker());
        discoveryService.checkForTopologyChange();
        assertEquals(0, discoveryService.getViewStateManager().waitForAsyncEvents(2000));
        assertEquals(1, listener.countEvents());
        discoveryService.unbindTopologyEventListener(listener);
        assertEquals(1, listener.countEvents());
        discoveryService.bindTopologyEventListener(listener);
        assertEquals(0, discoveryService.getViewStateManager().waitForAsyncEvents(2000));
        assertEquals(2, listener.countEvents()); // should now have gotten an INIT too
    }

    @Test
    public void testDescriptorSeqNumChange() throws Exception {
        logger.info("testDescriptorSeqNumChange: start");
        OakVirtualInstanceBuilder builder1 =
                (OakVirtualInstanceBuilder) new OakVirtualInstanceBuilder()
                .setDebugName("instance1")
                .newRepository("/foo/barry/foo/", true)
                .setConnectorPingInterval(999)
                .setConnectorPingTimeout(999);
        VirtualInstance instance1 = builder1.build();
        OakVirtualInstanceBuilder builder2 =
                (OakVirtualInstanceBuilder) new OakVirtualInstanceBuilder()
                .setDebugName("instance2")
                .useRepositoryOf(instance1)
                .setConnectorPingInterval(999)
                .setConnectorPingTimeout(999);
        VirtualInstance instance2 = builder2.build();
        logger.info("testDescriptorSeqNumChange: created both instances, binding listener...");

        DummyListener listener = new DummyListener();
        OakDiscoveryService discoveryService = (OakDiscoveryService) instance1.getDiscoveryService();
        discoveryService.bindTopologyEventListener(listener);

        logger.info("testDescriptorSeqNumChange: waiting 2sec, listener should not get anything yet");
        assertEquals(0, discoveryService.getViewStateManager().waitForAsyncEvents(2000));
        assertEquals(0, listener.countEvents());

        logger.info("testDescriptorSeqNumChange: issuing 2 heartbeats with each instance should let the topology get established");
        instance1.heartbeatsAndCheckView();
        instance2.heartbeatsAndCheckView();
        instance1.heartbeatsAndCheckView();
        instance2.heartbeatsAndCheckView();

        logger.info("testDescriptorSeqNumChange: listener should get an event within 2sec from now at latest");
        assertEquals(0, discoveryService.getViewStateManager().waitForAsyncEvents(2000));
        assertEquals(1, listener.countEvents());

        ResourceResolverFactory factory = instance1.getResourceResolverFactory();
        ResourceResolver resolver = factory.getServiceResourceResolver(null);

        instance1.heartbeatsAndCheckView();
        assertEquals(0, discoveryService.getViewStateManager().waitForAsyncEvents(2000));
        assertEquals(1, listener.countEvents());

        // increment the seqNum by 2 - simulating a coming and going instance
        // while we were sleeping
        SimulatedLeaseCollection c = builder1.getSimulatedLeaseCollection();
        c.incSeqNum(2);
        logger.info("testDescriptorSeqNumChange: incremented seqnum by 2 - issuing another heartbeat should trigger a topology change");
        instance1.heartbeatsAndCheckView();

        // due to the nature of the syncService/minEventDelay we now explicitly first sleep 2sec before waiting for async events for another 2sec
        logger.info("testDescriptorSeqNumChange: sleeping 2sec for topology change to happen");
        Thread.sleep(2000);
        logger.info("testDescriptorSeqNumChange: ensuring no async events are still in the pipe - for another 2sec");
        assertEquals(0, discoveryService.getViewStateManager().waitForAsyncEvents(2000));
        logger.info("testDescriptorSeqNumChange: now listener should have received 3 events, it got: "+listener.countEvents());
        assertEquals(3, listener.countEvents());
    }

    @Test
    public void testNotYetInitializedLeaderElectionid() throws Exception {
        logger.info("testNotYetInitializedLeaderElectionid: start");
        OakVirtualInstanceBuilder builder1 =
                (OakVirtualInstanceBuilder) new OakVirtualInstanceBuilder()
                .setDebugName("instance")
                .newRepository("/foo/barrx/foo/", true)
                .setConnectorPingInterval(999)
                .setConnectorPingTimeout(999);
        VirtualInstance instance1 = builder1.build();
        logger.info("testNotYetInitializedLeaderElectionid: created 1 instance, binding listener...");

        DummyListener listener = new DummyListener();
        OakDiscoveryService discoveryService = (OakDiscoveryService) instance1.getDiscoveryService();
        discoveryService.bindTopologyEventListener(listener);

        logger.info("testNotYetInitializedLeaderElectionid: waiting 2sec, listener should not get anything yet");
        assertEquals(0, discoveryService.getViewStateManager().waitForAsyncEvents(2000));
        assertEquals(0, listener.countEvents());

        logger.info("testNotYetInitializedLeaderElectionid: issuing 2 heartbeats with each instance should let the topology get established");
        instance1.heartbeatsAndCheckView();
        instance1.heartbeatsAndCheckView();

        logger.info("testNotYetInitializedLeaderElectionid: listener should get an event within 2sec from now at latest");
        assertEquals(0, discoveryService.getViewStateManager().waitForAsyncEvents(2000));
        assertEquals(1, listener.countEvents());

        SimulatedLeaseCollection c = builder1.getSimulatedLeaseCollection();
        String secondSlingId = UUID.randomUUID().toString();
        final SimulatedLease newIncomingInstance = new SimulatedLease(instance1.getResourceResolverFactory(), c, secondSlingId);
        c.hooked(newIncomingInstance);
        c.incSeqNum(1);
        newIncomingInstance.updateLeaseAndDescriptor(new OakTestConfig());
        
        logger.info("testNotYetInitializedLeaderElectionid: issuing another 2 heartbeats");
        instance1.heartbeatsAndCheckView();
        instance1.heartbeatsAndCheckView();
        
        // there are different properties that an instance must set in the repository such that it finally becomes visible.
        // these include:
        // 1) idmap : it must map the oak id to sling id
        // 2) node named after its own slingId under /var/discovery/oak/clusterInstances/<slingId>
        // 3) store the leaderElectionId under /var/discovery/oak/clusterInstances/<slingId>
        // in all 3 cases the code must work fine if that node/property doesn't exist
        // and that's exactly what we're testing here.


        // initially not even the idmap is updated, so we're stuck with TOPOLOGY_CHANGING

        // due to the nature of the syncService/minEventDelay we now explicitly first sleep 2sec before waiting for async events for another 2sec
        logger.info("testNotYetInitializedLeaderElectionid: sleeping 2sec for topology change to happen");
        Thread.sleep(2000);
        logger.info("testNotYetInitializedLeaderElectionid: ensuring no async events are still in the pipe - for another 2sec");
        assertEquals(0, discoveryService.getViewStateManager().waitForAsyncEvents(2000));
        logger.info("testNotYetInitializedLeaderElectionid: now listener should have received 2 events, INIT and CHANGING, it got: "+listener.countEvents());
        assertEquals(2, listener.countEvents());
        List<TopologyEvent> events = listener.getEvents();
        assertEquals(TopologyEvent.Type.TOPOLOGY_INIT, events.get(0).getType());
        assertEquals(TopologyEvent.Type.TOPOLOGY_CHANGING, events.get(1).getType());
        
        // let's update the idmap first then
        DummyResourceResolverFactory factory1 = (DummyResourceResolverFactory) instance1.getResourceResolverFactory();
        ResourceResolverFactory factory2 = MockFactory.mockResourceResolverFactory(factory1.getSlingRepository());

        ResourceResolver resourceResolver = getResourceResolver(instance1.getResourceResolverFactory());
        DiscoveryLiteDescriptor descriptor =
                DiscoveryLiteDescriptor.getDescriptorFrom(resourceResolver);
        resourceResolver.close();
        
        DiscoveryLiteDescriptorBuilder dlb = prefill(descriptor);
        dlb.me(2);
        DescriptorHelper.setDiscoveryLiteDescriptor(factory2, dlb);

        IdMapService secondIdMapService = IdMapService.testConstructor((DiscoveryLiteConfig) builder1.getConnectorConfig(), new DummySlingSettingsService(secondSlingId), factory2);
        
        instance1.heartbeatsAndCheckView();
        instance1.heartbeatsAndCheckView();
        Thread.sleep(2000);
        assertEquals(2, listener.countEvents());
        
        
        // now let's add the /var/discovery/oak/clusterInstances/<slingId> node
        resourceResolver = getResourceResolver(factory2);
        Resource clusterInstancesRes = resourceResolver.getResource(builder1.getConnectorConfig().getClusterInstancesPath());
        assertNull(clusterInstancesRes.getChild(secondSlingId));
        resourceResolver.create(clusterInstancesRes, secondSlingId, null);
        resourceResolver.commit();
        assertNotNull(clusterInstancesRes.getChild(secondSlingId));
        resourceResolver.close();

        instance1.heartbeatsAndCheckView();
        instance1.heartbeatsAndCheckView();
        Thread.sleep(2000);
        assertEquals(2, listener.countEvents());

        // now let's add the leaderElectionId
        resourceResolver = getResourceResolver(factory2);
        Resource instanceResource = resourceResolver.getResource(builder1.getConnectorConfig().getClusterInstancesPath() + "/" + secondSlingId);
        assertNotNull(instanceResource);
        instanceResource.adaptTo(ModifiableValueMap.class).put("leaderElectionId", "0");
        resourceResolver.commit();
        resourceResolver.close();
        
        instance1.heartbeatsAndCheckView();
        instance1.heartbeatsAndCheckView();
        Thread.sleep(2000);
        assertEquals(3, listener.countEvents());
        assertEquals(TopologyEvent.Type.TOPOLOGY_CHANGED, events.get(2).getType());
    }

    private DiscoveryLiteDescriptorBuilder prefill(DiscoveryLiteDescriptor d) throws Exception {
        DiscoveryLiteDescriptorBuilder b = new DiscoveryLiteDescriptorBuilder();
        b.setFinal(true);
        long seqnum = d.getSeqNum();
        b.seq((int) seqnum);
        b.activeIds(box(d.getActiveIds()));
        b.deactivatingIds(box(d.getDeactivatingIds()));
        b.me(d.getMyId());
        b.id(d.getViewId());
        return b;
    }

    private Integer[] box(final int[] ids) {
        //TODO: use Guava
        List<Integer> list = new ArrayList<Integer>(ids.length);
        for (Integer i : ids) {
            list.add(i);
        }
        return list.toArray(new Integer[list.size()]);
    }
    
    private ResourceResolver getResourceResolver(ResourceResolverFactory resourceResolverFactory) throws LoginException {
        return resourceResolverFactory.getServiceResourceResolver(null);
    }
}
