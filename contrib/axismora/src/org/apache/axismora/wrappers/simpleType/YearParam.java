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
import java.text.NumberFormat;

import org.apache.axis.AxisFault;
import org.apache.axis.encoding.SerializationContext;
import org.apache.axis.message.SOAPFault;
import org.apache.axismora.MessageContext;
import org.apache.axismora.encoding.InOutParameter;
import org.apache.axismora.encoding.InParameter;

/**
 * Created on Sep 25, 2003
 * @author vtpavan(vtpavan@opensource.lk)
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#gYear">XML Schema 3.2.11</a>
 */
public class YearParam implements InOutParameter {
    private int year;
    private String timezone = null;
    private String param;

    public YearParam() {
    }

    public YearParam(MessageContext msgcontext) {
        try {
            this.desierialize(msgcontext);
        } catch (AxisFault e) {
            msgcontext.setSoapFault(new SOAPFault(e));
        }
    }
    /**
     * Constructs a Year with the given values
     * No timezone is specified
     */
    public YearParam(int year) throws NumberFormatException {
        setValue(year);
    }

    /**
     * Constructs a Year with the given values, including a timezone string
     * The timezone is validated but not used.
     */
    public YearParam(int year, String timezone) throws NumberFormatException {
        setValue(year, timezone);
    }

    /**
     * Construct a Year from a String in the format [-]CCYY[timezone]
     */
    public YearParam(String source) {
        this.setSource(source);
    }
    public void setSource(String source) throws NumberFormatException {
        int negative = 0;

        if (source.charAt(0) == '-') {
            negative = 1;
        }
        if (source.length() < (4 + negative)) {
            throw new NumberFormatException("badYearFormat");
        }

        // calculate how many more than 4 digits (if any) in the year
        int pos = 4 + negative; // skip minus sign if present
        while (pos < source.length() && Character.isDigit(source.charAt(pos))) {
            ++pos;
        }
        setValue(Integer.parseInt(source.substring(0, pos)), source.substring(pos));
    }

    /* (non-Javadoc)
    	 * @see org.apache.axismora.encoding.OutParameter#serialize(org.apache.axis.encoding.SerializationContext)
    	 */
    public void serialize(SerializationContext context) throws IOException {
        String type_name = "Year";
        StringBuffer buf = new StringBuffer();
        buf.append("<Year xsi:type=\"ns1:").append(type_name + "\" xmlns:ns1 =\"");
        buf.append(org.apache.axis.Constants.URI_2001_SCHEMA_XSD + "/#gYear\">");
        buf.append(param);
        buf.append("</Year>\n");
        try {
            context.writeString(buf.toString());
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

    public void setYear(int year) {
        // validate year, more than 4 digits are allowed!
        if (year == 0) {
            throw new NumberFormatException("badYearFormat");
        }

        this.year = year;
    }

    public void setTimezone(String timezone) {
        // validate timezone
        if (timezone != null && timezone.length() > 0) {
            // Format [+/-]HH:MM
            if (timezone.charAt(0) == '+' || (timezone.charAt(0) == '-')) {
                if (timezone.length() != 6
                    || !Character.isDigit(timezone.charAt(1))
                    || !Character.isDigit(timezone.charAt(2))
                    || timezone.charAt(3) != ':'
                    || !Character.isDigit(timezone.charAt(4))
                    || !Character.isDigit(timezone.charAt(5)))
                    throw new NumberFormatException("badTimezoneFormat");

            } else if (!timezone.equals("Z")) {
                throw new NumberFormatException("badTimezoneFormat");
            }
            // if we got this far, its good
            this.timezone = timezone;
        }
    }

    public void setValue(int year, String timezone) throws NumberFormatException {
        setYear(year);
        setTimezone(timezone);
    }

    public void setValue(int year) throws NumberFormatException {
        setYear(year);
    }

    public String toString() {
        // use NumberFormat to ensure leading zeros
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);

        // year
        nf.setMinimumIntegerDigits(4);
        String s = nf.format(year);

        // timezone
        if (timezone != null) {
            s = s + timezone;
        }
        return s;
    }
    public YearParam getParam() {
        return this;
    }
    
	public void init(){
	
	}

}
