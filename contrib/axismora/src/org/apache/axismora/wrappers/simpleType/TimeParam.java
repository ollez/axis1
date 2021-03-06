/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *    Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.axismora.wrappers.simpleType;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.axis.AxisFault;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.message.SOAPFault;
import org.apache.axismora.MessageContext;
import org.apache.axismora.encoding.InOutParameter;
import org.apache.axismora.encoding.InParameter;

/**
 * Created on Sep 25, 2003
 * @author vtpavan(vtpavan@opensource.lk)
 * Class that represents the xsd:time XML Schema type
 */
public class TimeParam implements InOutParameter {

    private Calendar calendar_value;
    private String param;

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSS'Z'");

    // We should always format dates in the GMT timezone
    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public TimeParam() {
    }

    public TimeParam(MessageContext msgcontext) {
        try {
            this.desierialize(msgcontext);
        } catch (AxisFault e) {
            msgcontext.setSoapFault(new SOAPFault(e));
        }
    }
    /**
     * Initialize with a Calender, year month and date are ignored
    */
    public TimeParam(Calendar value) {
        this.calendar_value = value;
        calendar_value.set(0, 0, 0); // ignore year, month, date
    }

    /**
     * Converts a string formatted as HH:mm:ss[.SSS][+/-offset]
     */
    public TimeParam(String source) {
        this.setSource(source);
    }
    public void setSource(String value) throws NumberFormatException {
        calendar_value = makeValue(value);
    }
    /* (non-Javadoc)
     * @see org.apache.axismora.encoding.OutParameter#serialize(org.apache.axis.encoding.SerializationContext)
     */
    public void serialize(SerializationContext context) throws IOException {
//        String type_name = "Time";
//        StringBuffer buf = new StringBuffer();
//        buf.append("<Time xsi:type=\"ns1:").append(type_name + "\" xmlns:ns1 =\"");
//        buf.append(org.apache.axis.Constants.URI_DEFAULT_SCHEMA_XSD + "/#Time\">");
//        buf.append(param);
//        buf.append("</Time>\n");
        try {
            context.writeString(param);
        } catch (IOException e) {
            e.printStackTrace(); //ioexception
        }
    }

    /* (non-Javadoc)
     * @see org.apache.axismora.encoding.InParameter#desierialize(org.apache.axismora.MessageContext)
     */
    public InParameter desierialize(MessageContext msgdata) throws AxisFault {
        String value = msgdata.nextText();
        this.setSource(value);
        this.param = this.toString();
        return this;
    }

    public Calendar getAsCalendar() {
        return calendar_value;
    }

    public void setTime(Calendar date) {
        this.calendar_value = date;
        calendar_value.set(0, 0, 0); // ignore year, month, date
    }

    public void setTime(Date date) {
        calendar_value.setTime(date);
        calendar_value.set(0, 0, 0); // ignore year, month, date
    }

    /**
     * Utility function that parses xsd:time strings and returns a Date object
     */
    private Calendar makeValue(String source) throws NumberFormatException {
        Calendar calendar = Calendar.getInstance();
        Date date;

        // validate fixed portion of format
        if (source != null) {
            if (source.charAt(2) != ':' || source.charAt(5) != ':')
                throw new NumberFormatException("badTimeformat");
            if (source.length() < 8) {
                throw new NumberFormatException("badTimeformat");
            }
        }

        // convert what we have validated so far
        try {
            synchronized (dateFormat) {
                date =
                    dateFormat.parse(
                        source == null ? null : (source.substring(0, 8) + ".000Z"));
            }
        } catch (Exception e) {
            throw new NumberFormatException(e.toString());
        }

        int pos = 8; // The "." in hh:mm:ss.sss

        // parse optional milliseconds
        if (source != null) {
            if (pos < source.length() && source.charAt(pos) == '.') {
                int milliseconds = 0;
                int start = ++pos;
                while (pos < source.length() && Character.isDigit(source.charAt(pos)))
                    pos++;

                String decimal = source.substring(start, pos);
                if (decimal.length() == 3) {
                    milliseconds = Integer.parseInt(decimal);
                } else if (decimal.length() < 3) {
                    milliseconds = Integer.parseInt((decimal + "000").substring(0, 3));
                } else {
                    milliseconds = Integer.parseInt(decimal.substring(0, 3));
                    if (decimal.charAt(3) >= '5')
                        ++milliseconds;
                }

                // add milliseconds to the current date
                date.setTime(date.getTime() + milliseconds);
            }

            // parse optional timezone
            if (pos + 5 < source.length()
                && (source.charAt(pos) == '+' || (source.charAt(pos) == '-'))) {
                if (!Character.isDigit(source.charAt(pos + 1))
                    || !Character.isDigit(source.charAt(pos + 2))
                    || source.charAt(pos + 3) != ':'
                    || !Character.isDigit(source.charAt(pos + 4))
                    || !Character.isDigit(source.charAt(pos + 5)))
                    throw new NumberFormatException("badTimezoneFormat");

                int hours =
                    (source.charAt(pos + 1) - '0') * 10 + source.charAt(pos + 2) - '0';
                int mins =
                    (source.charAt(pos + 4) - '0') * 10 + source.charAt(pos + 5) - '0';
                int milliseconds = (hours * 60 + mins) * 60 * 1000;

                // subtract milliseconds from current date to obtain GMT
                if (source.charAt(pos) == '+')
                    milliseconds = -milliseconds;
                date.setTime(date.getTime() + milliseconds);
                pos += 6;
            }

            if (pos < source.length() && source.charAt(pos) == 'Z') {
                pos++;
                calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
            }

            if (pos < source.length())
                throw new NumberFormatException("badCharsFormat");
        }

        calendar.setTime(date);
        calendar.set(0, 0, 0); // ignore year, month, date

        return calendar;
    }

    public String toString() {
        synchronized (dateFormat) {
            return dateFormat.format(calendar_value.getTime());
        }

    }

    public TimeParam getParam() {
        return this;
    }

}
