/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.wsdl.roundtrip;

/**
 * The InvalidTradeExchange class is used to test the ability of
 * Java2WSDL to correctly generate faults in the WSDL.
 *
 * @version   1.00  18 Feb 2002
 * @author    Brent Ulbricht
 */
public class InvalidTradeExchange extends Exception {

    public String tradeExchange;

    public InvalidTradeExchange(String tradeExchange) {
        this.tradeExchange = tradeExchange;
    } // Constructor

    public String getTradeExchange() {
        return this.tradeExchange;  
    } // getTradeExchange

} // InvalidTradeExchange
