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
 */
 
 /*
  * @file IConstrainingFacet.hpp
  */
  
#if !defined(_CONSTRAININGFACET_HPP____OF_AXIS_INCLUDED_)
#define _CONSTRAININGFACET_HPP____OF_AXIS_INCLUDED_

#include "../../AxisSoapException.h"

AXIS_CPP_NAMESPACE_START

/**
 *   @class IConstrainingFacet
 *   @brief Interface for all XSD constraining facets
 *
 *   @author Adrian Dick (adrian.dick@uk.ibm.com)
 *
 */
class IConstrainingFacet {

public:
    virtual ~IConstrainingFacet () {};

    /**
     * To allow checking if a value has been set on this constraining facet.
     */    
    virtual bool isSet() = 0;
    
protected:
    bool m_isSet;

};

AXIS_CPP_NAMESPACE_END

#endif