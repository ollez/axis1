/* -*- C++ -*- */
/*
 *   Copyright 2003-2004 The Apache Software Foundation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *
 * @author Adrian Dick (adrian.dick@uk.ibm.com)
 *
 */

#if !defined(_GMONTH_HPP____OF_AXIS_INCLUDED_)
#define _GMONTH_HPP____OF_AXIS_INCLUDED_

#include "IAnySimpleType.hpp"
#include "constraints/MinInclusive.hpp"
#include "constraints/MinExclusive.hpp"
#include "constraints/MaxInclusive.hpp"
#include "constraints/MaxExclusive.hpp"
#include <ctime>

AXIS_CPP_NAMESPACE_START

using namespace std;

class GMonth : public IAnySimpleType {
public:

    /**
     * Constructor
     */
    GMonth();
    
  /**
    * Serialize value to it's on-the-wire string form.
    * @param value The value to be serialized.
    * @return Serialized form of value.
   */
    AxisChar* serialize(const void* value) throw (AxisSoapException);
    
  /**
    * Deserialize value from it's on-the-wire string form.
    * @param valueAsChar Serialized form of value.
    * @return Deserialized value.
     */
    void* deserialize(const AxisChar* valueAsChar) throw (AxisSoapException);
    
  /**
    * Serialize GMonth value to it's on-the-wire string form.
   * @param value The GMonth value to be serialized.
   * @return Serialized form of GMonth value.
  */
    AxisChar* serialize(const struct tm* value) throw (AxisSoapException);
   
  /**
    * Deserialized GMonth value from it's on-the-wire string form.
  * @param valueAsChar Serialized form of GMonth value.
   * @return Deserialized GMonth value.
    */
    struct tm* deserializeGMonth(const AxisChar* valueAsChar) throw (AxisSoapException);

protected:

    /**
     * Creates a MinInclusive object.  For the GMonth type this is undefined, 
     * so an unset MinInclusive object is created.
     * @return MinInclusive object
     */
    MinInclusive* getMinInclusive();

    /**
     * Creates a MinExclusive object.  For the GMonth type this is undefined, 
     * so an unset MinExclusive object is created.
     * @return MinExclusive object
     */
    MinExclusive* getMinExclusive();    

    /**
     * Creates a MaxInclusive object.  For the GMonth type this is undefined, 
     * so an unset MaxInclusive object is created.
     * @return MaxInclusive object
     */
    MaxInclusive* getMaxInclusive();

    /**
     * Creates a MaxExclusive object.  For the GMonth type this is undefined, 
     * so an unset MaxExclusive object is created.
     * @return MaxExclusive object
     */
    MaxExclusive* getMaxExclusive();    

    /**
     * Creates a WhiteSpace object to collapse whitespace
     * @return WhiteSpace object set to collapse whitespace
     */
    WhiteSpace* getWhiteSpace();

private:
   struct tm* m_GMonth;
};

AXIS_CPP_NAMESPACE_END

#endif