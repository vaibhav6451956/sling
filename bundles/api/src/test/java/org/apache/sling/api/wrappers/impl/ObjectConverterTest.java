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
package org.apache.sling.api.wrappers.impl;

import static org.apache.sling.api.wrappers.impl.DateUtils.calendarToString;
import static org.apache.sling.api.wrappers.impl.DateUtils.toCalendar;
import static org.apache.sling.api.wrappers.impl.DateUtils.toDate;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Ignore;
import org.junit.Test;

public class ObjectConverterTest {
    
    private static final String STRING_1 = "item1";
    private static final String STRING_2 = "item2";
    private static final boolean BOOLEAN_1 = true;
    private static final boolean BOOLEAN_2 = false;
    private static final byte BYTE_1 = (byte)0x01;
    private static final byte BYTE_2 = (byte)0x02;
    private static final short SHORT_1 = (short)12;
    private static final short SHORT_2 = (short)34;
    private static final int INT_1 = 55;
    private static final int INT_2 = -123;
    private static final long LONG_1 = 1234L;
    private static final long LONG_2 = -4567L;
    private static final float FLOAT_1 = 1.23f;
    private static final float FLOAT_2 = -4.56f;
    private static final double DOUBLE_1 = 12.34d;
    private static final double DOUBLE_2 = -45.67d;
    private static final BigDecimal BIGDECIMAL_1 = new BigDecimal("12345.67");
    private static final BigDecimal BIGDECIMAL_2 = new BigDecimal("-23456.78");
    private static final Calendar CALENDAR_1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
    private static final Calendar CALENDAR_2 = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.US);
    {
        CALENDAR_1.set(2016, 10, 15, 8, 20, 30);
        CALENDAR_1.setLenient(true);
        CALENDAR_2.set(2015, 6, 31, 19, 10, 20);
        CALENDAR_2.setLenient(true);
    }
    private static final Date DATE_1 = toDate(CALENDAR_1);
    private static final Date DATE_2 = toDate(CALENDAR_2);

    @Test
            public void testDateToString() {
                Convert.from(STRING_1, STRING_2).to(STRING_1, STRING_2).test();
                Convert.fromPrimitive(BOOLEAN_1, BOOLEAN_2).to(Boolean.toString(BOOLEAN_1), Boolean.toString(BOOLEAN_2)).test();
                Convert.from(BOOLEAN_1, BOOLEAN_2).to(Boolean.toString(BOOLEAN_1), Boolean.toString(BOOLEAN_2)).test();
                Convert.fromPrimitive(BYTE_1, BYTE_2).to(Byte.toString(BYTE_1), Byte.toString(BYTE_2)).test();
                Convert.from(BYTE_1, BYTE_2).to(Byte.toString(BYTE_1), Byte.toString(BYTE_2)).test();
                Convert.fromPrimitive(SHORT_1, SHORT_2).to(Short.toString(SHORT_1), Short.toString(SHORT_2)).test();
                Convert.from(SHORT_1, SHORT_2).to(Short.toString(SHORT_1), Short.toString(SHORT_2)).test();
                Convert.fromPrimitive(INT_1, INT_2).to(Integer.toString(INT_1), Integer.toString(INT_2)).test();
                Convert.from(INT_1, INT_2).to(Integer.toString(INT_1), Integer.toString(INT_2)).test();
                Convert.fromPrimitive(LONG_1, LONG_2).to(Long.toString(LONG_1), Long.toString(LONG_2)).test();
                Convert.from(LONG_1, LONG_2).to(Long.toString(LONG_1), Long.toString(LONG_2)).test();
                Convert.fromPrimitive(FLOAT_1, FLOAT_2).to(Float.toString(FLOAT_1), Float.toString(FLOAT_2)).test();
                Convert.from(FLOAT_1, FLOAT_2).to(Float.toString(FLOAT_1), Float.toString(FLOAT_2)).test();
                Convert.fromPrimitive(DOUBLE_1, DOUBLE_2).to(Double.toString(DOUBLE_1), Double.toString(DOUBLE_2)).test();
                Convert.from(DOUBLE_1, DOUBLE_2).to(Double.toString(DOUBLE_1), Double.toString(DOUBLE_2)).test();
                Convert.from(BIGDECIMAL_1, BIGDECIMAL_2).to(BIGDECIMAL_1.toString(), BIGDECIMAL_2.toString()).test();
                Convert.from(CALENDAR_1, CALENDAR_2).to(calendarToString(CALENDAR_1), calendarToString(CALENDAR_2)).test();
                Convert.from(DATE_1, DATE_2).to(calendarToString(toCalendar(DATE_1)), calendarToString(toCalendar(DATE_2))).test();
            }
    
    @Test
    public void testToBoolean() {
        Convert.fromPrimitive(BOOLEAN_1, BOOLEAN_2).to(BOOLEAN_1, BOOLEAN_2).test();
        Convert.from(BOOLEAN_1, BOOLEAN_2).to(BOOLEAN_1, BOOLEAN_2).test();
        Convert.from(Boolean.toString(BOOLEAN_1), Boolean.toString(BOOLEAN_2)).to(BOOLEAN_1, BOOLEAN_2).test();
        
        // test other types that should not be converted
        // TODO: test is not successful yet
        /*
        TestUtils.<Integer,Boolean>conv(INT_1, INT_2).toNull(Boolean.class).test();
        TestUtils.<Date,Boolean>conv(DATE_1, DATE_2).toNull(Boolean.class).test();
        */
    }
    
    @Test
    @Ignore // TODO: support primitive
    public void testToBooleanPrimitive() {
        Convert.fromPrimitive(BOOLEAN_1, BOOLEAN_2).toPrimitive(BOOLEAN_1, BOOLEAN_2).nullValue(false).test();
        Convert.from(BOOLEAN_1, BOOLEAN_2).toPrimitive(BOOLEAN_1, BOOLEAN_2).nullValue(false).test();
        Convert.from(Boolean.toString(BOOLEAN_1), Boolean.toString(BOOLEAN_2)).toPrimitive(BOOLEAN_1, BOOLEAN_2).nullValue(false).test();
        
        // test other types that should not be converted
        Convert.from(INT_1, INT_2).toPrimitive(false,  false).nullValue(false).test();
        Convert.from(DATE_1, DATE_2).toPrimitive(false,  false).nullValue(false).test();
    }
    
    @Test
    public void testToByte() {
        Convert.fromPrimitive(BYTE_1, BYTE_2).to(BYTE_1, BYTE_2).test();
        Convert.from(BYTE_1, BYTE_2).to(BYTE_1, BYTE_2).test();
        Convert.from(Byte.toString(BYTE_1), Byte.toString(BYTE_2)).to(BYTE_1, BYTE_2).test();
        
        // test conversion from other number types
        Convert.from(INT_1, INT_2).to((byte)INT_1, (byte)INT_2).test();
        Convert.fromPrimitive(INT_1, INT_2).to((byte)INT_1, (byte)INT_2).test();

        // test other types that should not be converted
        // TODO: test is not successful yet
        /*
        TestUtils.<Date,Byte>conv(DATE_1, DATE_2).toNull(Byte.class).test();
        */
    }
    
    @Test
    @Ignore // TODO: support primitive
    public void testToBytePrimitive() {
        Convert.fromPrimitive(BYTE_1, BYTE_2).toPrimitive(BYTE_1, BYTE_2).nullValue(0).test();
        Convert.from(BYTE_1, BYTE_2).toPrimitive(BYTE_1, BYTE_2).nullValue(0).test();
        Convert.from(Byte.toString(BYTE_1), Byte.toString(BYTE_2)).toPrimitive(BYTE_1, BYTE_2).nullValue(0).test();

        // test conversion from other number types
        Convert.from(INT_1, INT_2).toPrimitive((byte)INT_1, (byte)INT_2).test();
        Convert.fromPrimitive(INT_1, INT_2).toPrimitive((byte)INT_1, (byte)INT_2).test();

        // test other types that should not be converted
        Convert.from(INT_1, INT_2).toPrimitive((byte)0, (byte)0).nullValue(false).test();
    }
    
    @Test
    public void testToShort() {
        Convert.fromPrimitive(SHORT_1, SHORT_2).to(SHORT_1, SHORT_2).test();
        Convert.from(SHORT_1, SHORT_2).to(SHORT_1, SHORT_2).test();
        Convert.from(Short.toString(SHORT_1), Short.toString(SHORT_2)).to(SHORT_1, SHORT_2).test();
        
        // test conversion from other number types
        Convert.from(INT_1, INT_2).to((short)INT_1, (short)INT_2).test();
        Convert.fromPrimitive(INT_1, INT_2).to((short)INT_1, (short)INT_2).test();

        // test other types that should not be converted
        // TODO: test is not successful yet
        /*
        TestUtils.<Date,Short>conv(DATE_1, DATE_2).toNull(Short.class).test();
        */
    }
    
    @Test
    @Ignore // TODO: support primitive
    public void testToShortPrimitive() {
        Convert.fromPrimitive(SHORT_1, SHORT_2).toPrimitive(SHORT_1, SHORT_2).nullValue(0).test();
        Convert.from(SHORT_1, SHORT_2).toPrimitive(SHORT_1, SHORT_2).nullValue(0).test();
        Convert.from(Short.toString(SHORT_1), Short.toString(SHORT_2)).toPrimitive(SHORT_1, SHORT_2).nullValue(0).test();

        // test conversion from other number types
        Convert.from(INT_1, INT_2).toPrimitive((short)INT_1, (short)INT_2).test();
        Convert.fromPrimitive(INT_1, INT_2).toPrimitive((short)INT_1, (short)INT_2).test();

        // test other types that should not be converted
        Convert.from(INT_1, INT_2).toPrimitive((short)0, (short)0).nullValue(false).test();
    }
    
    @Test
    public void testToInteger() {
        Convert.fromPrimitive(INT_1, INT_2).to(INT_1, INT_2).test();
        Convert.from(INT_1, INT_2).to(INT_1, INT_2).test();
        Convert.from(Integer.toString(INT_1), Integer.toString(INT_2)).to(INT_1, INT_2).test();
        
        // test conversion from other number types
        Convert.from(SHORT_1, SHORT_2).to((int)SHORT_1, (int)SHORT_2).test();
        Convert.fromPrimitive(SHORT_1, SHORT_2).to((int)SHORT_1, (int)SHORT_2).test();

        // test other types that should not be converted
        // TODO: test is not successful yet
        /*
        TestUtils.<Date,Integer>conv(DATE_1, DATE_2).toNull(Integer.class).test();
        */
    }
    
    @Test
    @Ignore // TODO: support primitive
    public void testToIntegerPrimitive() {
        Convert.fromPrimitive(INT_1, INT_2).toPrimitive(INT_1, INT_2).nullValue(0).test();
        Convert.from(INT_1, INT_2).toPrimitive(INT_1, INT_2).nullValue(0).test();
        Convert.from(Integer.toString(INT_1), Integer.toString(INT_2)).toPrimitive(INT_1, INT_2).nullValue(0).test();

        // test conversion from other number types
        Convert.from(SHORT_1, SHORT_2).toPrimitive((int)SHORT_1, (int)SHORT_2).test();
        Convert.fromPrimitive(SHORT_1, SHORT_2).toPrimitive((int)SHORT_1, (int)SHORT_2).test();

        // test other types that should not be converted
        Convert.from(INT_1, INT_2).toPrimitive((int)0, (int)0).nullValue(false).test();
    }
    
    @Test
    public void testToLong() {
        Convert.fromPrimitive(LONG_1, LONG_2).to(LONG_1, LONG_2).test();
        Convert.from(LONG_1, LONG_2).to(LONG_1, LONG_2).test();
        Convert.from(Long.toString(LONG_1), Long.toString(LONG_2)).to(LONG_1, LONG_2).test();
        
        // test conversion from other number types
        Convert.from(SHORT_1, SHORT_2).to((long)SHORT_1, (long)SHORT_2).test();
        Convert.fromPrimitive(SHORT_1, SHORT_2).to((long)SHORT_1, (long)SHORT_2).test();

        // test other types that should not be converted
        // TODO: test is not successful yet
        /*
        TestUtils.<Date,Long>conv(DATE_1, DATE_2).toNull(Long.class).test();
        */
    }
    
    @Test
    @Ignore // TODO: support primitive
    public void testToLongPrimitive() {
        Convert.fromPrimitive(LONG_1, LONG_2).toPrimitive(LONG_1, LONG_2).nullValue(0).test();
        Convert.from(LONG_1, LONG_2).toPrimitive(LONG_1, LONG_2).nullValue(0).test();
        Convert.from(Long.toString(LONG_1), Long.toString(LONG_2)).toPrimitive(LONG_1, LONG_2).nullValue(0).test();

        // test conversion from other number types
        Convert.from(SHORT_1, SHORT_2).toPrimitive((long)SHORT_1, (long)SHORT_2).test();
        Convert.fromPrimitive(SHORT_1, SHORT_2).toPrimitive((long)SHORT_1, (long)SHORT_2).test();

        // test other types that should not be converted
        Convert.from(LONG_1, LONG_2).toPrimitive((long)0, (long)0).nullValue(false).test();
    }
    
    @Test
    public void testToFloat() {
        Convert.fromPrimitive(FLOAT_1, FLOAT_2).to(FLOAT_1, FLOAT_2).test();
        Convert.from(FLOAT_1, FLOAT_2).to(FLOAT_1, FLOAT_2).test();
        Convert.from(Float.toString(FLOAT_1), Float.toString(FLOAT_2)).to(FLOAT_1, FLOAT_2).test();
        
        // test conversion from other number types
        Convert.from(SHORT_1, SHORT_2).to((float)SHORT_1, (float)SHORT_2).test();
        Convert.fromPrimitive(SHORT_1, SHORT_2).to((float)SHORT_1, (float)SHORT_2).test();

        // test other types that should not be converted
        // TODO: test is not successful yet
        /*
        TestUtils.<Date,Float>conv(DATE_1, DATE_2).toNull(Float.class).test();
        */
    }
    
    @Test
    @Ignore // TODO: support primitive
    public void testToFloatPrimitive() {
        Convert.fromPrimitive(FLOAT_1, FLOAT_2).toPrimitive(FLOAT_1, FLOAT_2).nullValue(0).test();
        Convert.from(FLOAT_1, FLOAT_2).toPrimitive(FLOAT_1, FLOAT_2).nullValue(0).test();
        Convert.from(Float.toString(FLOAT_1), Float.toString(FLOAT_2)).toPrimitive(FLOAT_1, FLOAT_2).nullValue(0).test();

        // test conversion from other number types
        Convert.from(SHORT_1, SHORT_2).toPrimitive((float)SHORT_1, (float)SHORT_2).test();
        Convert.fromPrimitive(SHORT_1, SHORT_2).toPrimitive((float)SHORT_1, (float)SHORT_2).test();

        // test other types that should not be converted
        Convert.from(FLOAT_1, FLOAT_2).toPrimitive((float)0, (float)0).nullValue(false).test();
    }
 
    @Test
    public void testToDouble() {
        Convert.fromPrimitive(DOUBLE_1, DOUBLE_2).to(DOUBLE_1, DOUBLE_2).test();
        Convert.from(DOUBLE_1, DOUBLE_2).to(DOUBLE_1, DOUBLE_2).test();
        Convert.from(Double.toString(DOUBLE_1), Double.toString(DOUBLE_2)).to(DOUBLE_1, DOUBLE_2).test();
        
        // test conversion from other number types
        Convert.from(SHORT_1, SHORT_2).to((double)SHORT_1, (double)SHORT_2).test();
        Convert.fromPrimitive(SHORT_1, SHORT_2).to((double)SHORT_1, (double)SHORT_2).test();

        // test other types that should not be converted
        // TODO: test is not successful yet
        /*
        TestUtils.<Date,Double>conv(DATE_1, DATE_2).toNull(Double.class).test();
        */
    }
    
    @Test
    @Ignore // TODO: support primitive
    public void testToDoublePrimitive() {
        Convert.fromPrimitive(DOUBLE_1, DOUBLE_2).toPrimitive(DOUBLE_1, DOUBLE_2).nullValue(0).test();
        Convert.from(DOUBLE_1, DOUBLE_2).toPrimitive(DOUBLE_1, DOUBLE_2).nullValue(0).test();
        Convert.from(Double.toString(DOUBLE_1), Double.toString(DOUBLE_2)).toPrimitive(DOUBLE_1, DOUBLE_2).nullValue(0).test();

        // test conversion from other number types
        Convert.from(SHORT_1, SHORT_2).toPrimitive((double)SHORT_1, (double)SHORT_2).test();
        Convert.fromPrimitive(SHORT_1, SHORT_2).toPrimitive((double)SHORT_1, (double)SHORT_2).test();

        // test other types that should not be converted
        Convert.from(DOUBLE_1, DOUBLE_2).toPrimitive((double)0, (double)0).nullValue(false).test();
    }
    
    @Test
    public void testToBigDecimal() {
        Convert.from(BIGDECIMAL_1, BIGDECIMAL_2).to(BIGDECIMAL_1, BIGDECIMAL_2).test();
        Convert.from(BIGDECIMAL_1.toString(), BIGDECIMAL_2.toString()).to(BIGDECIMAL_1, BIGDECIMAL_2).test();
        
        // test conversion from other number types
        Convert.from(LONG_1, LONG_2).to(BigDecimal.valueOf(LONG_1), BigDecimal.valueOf(LONG_2)).test();
        Convert.fromPrimitive(LONG_1, LONG_2).to(BigDecimal.valueOf(LONG_1), BigDecimal.valueOf(LONG_2)).test();
        Convert.from(DOUBLE_1, DOUBLE_2).to(BigDecimal.valueOf(DOUBLE_1), BigDecimal.valueOf(DOUBLE_2)).test();
        Convert.fromPrimitive(DOUBLE_1, DOUBLE_2).to(BigDecimal.valueOf(DOUBLE_1), BigDecimal.valueOf(DOUBLE_2)).test();

        // test other types that should not be converted
        // TODO: test is not successful yet
        /*
        TestUtils.<Date,BigDecimal>conv(DATE_1, DATE_2).toNull(BigDecimal.class).test();
        */
    }
    
    @Test
    public void testToCalendar() {
        Convert.from(CALENDAR_1, CALENDAR_2).to(CALENDAR_1, CALENDAR_2).test();
        Convert.from(DateUtils.calendarToString(CALENDAR_1), DateUtils.calendarToString(CALENDAR_2)).to(CALENDAR_1, CALENDAR_2).test();
        
        // test conversion from other date types
        Convert.from(DATE_1, DATE_2).to(toCalendar(DATE_1), toCalendar(DATE_2)).test();

        // test other types that should not be converted
        Convert.<String,Calendar>from(STRING_1, STRING_2).toNull(Calendar.class).test();
        Convert.<Boolean,Calendar>from(BOOLEAN_1, BOOLEAN_2).toNull(Calendar.class).test();
    }
    
    @Test
    public void testToDate() {
        Convert.from(DATE_1, DATE_2).to(DATE_1, DATE_2).test();
        Convert.from(DateUtils.dateToString(DATE_1), DateUtils.dateToString(DATE_2)).to(DATE_1, DATE_2).test();
        
        // test conversion from other date types
        Convert.from(CALENDAR_1, CALENDAR_2).to(toDate(CALENDAR_1), toDate(CALENDAR_2)).test();

        // test other types that should not be converted
        Convert.<String,Date>from(STRING_1, STRING_2).toNull(Date.class).test();
        Convert.<Boolean,Date>from(BOOLEAN_1, BOOLEAN_2).toNull(Date.class).test();
    }
    
}
