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

#if !defined(_NONNEGATIVEINTEGER_HPP____OF_AXIS_INCLUDED_)
#define _NONNEGATIVEINTEGER_HPP____OF_AXIS_INCLUDED_

#include "Integer.hpp"

AXIS_CPP_NAMESPACE_START

using namespace std;

class NonNegativeInteger : public Integer {
public:

    /**
     * Constructor
     */
    NonNegativeInteger();
    
    /**
     * Destructor
     */
    ~NonNegativeInteger();

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
     * Serialize NonNegativeInteger value to it's on-the-wire string form.
     * @param value The NonNegativeInteger value to be serialized.
     * @return Serialized form of NonNegativeInteger value.
     */
    AxisChar* serialize(const unsigned LONGLONG* value) throw (AxisSoapException);
  
  /**
   * Deserialized NonNegativeInteger value from it's on-the-wire string form.
   * @param valueAsChar Serialized form of NonNegativeInteger value.
   * @return Deserialized NonNegativeInteger value.
   */
    unsigned LONGLONG* deserializeNonNegativeInteger(const AxisChar* valueAsChar) throw (AxisSoapException);

protected:

    /**
     * Creates a MinInclusive object.  For the NonNegativeInteger type this is
     * defined as 0.
     * @return MinInclusive object
     */
    virtual MinInclusive* getMinInclusive();

    /**
     * Creates a MinExclusive object.  For the NonNegativeInteger type this is
     * defined as -1.
     * @return MinExclusive object
     */
    virtual MinExclusive* getMinExclusive();

    /**
     * Creates a MaxInclusive object.  For the NonNegativeInteger type this is
     * undefined, so an unset MaxExclusive object is returned.
     * @return MaxInclusive object
     */
    virtual MaxInclusive* getMaxInclusive();

    /**
     * Creates a MaxExclusive object.  For the NonNegativeInteger type this is
     * undefined, so an unset MaxExclusive object is returned.
     * @return MaxExclusive object
     */
    virtual MaxExclusive* getMaxExclusive();

private:
   unsigned LONGLONG* m_NonNegativeInteger;

};

AXIS_CPP_NAMESPACE_END

#endif