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


#if !defined(__ATTRIBUTEDURI_OF_AXIS_INCLUDED__)
#define __ATTRIBUTEDURI_OF_AXIS_INCLUDED__

#include "Constants.hpp"
#include <axis/IHeaderBlock.hpp>
#include <axis/IMessageData.hpp>

AXIS_CPP_NAMESPACE_USE

class AttributedUri
{
private:
    AxisChar * m_pachLocalName;
    AxisChar * m_pachUri;
    bool m_bMustUnderstand;
    
public:
	AttributedUri(); 
    AttributedUri(const AxisChar * pachLocalName,const AxisChar * pachUri);
	~AttributedUri(); 
    void setUri(const AxisChar * pUri);
    AxisChar * getUri();
    void setLocalName(const AxisChar * pachLocalName);
    AxisChar * getLocalName();
    void setMustUnderstand(bool bMustUnderstand);
    bool isMustUnderstand();
    IHeaderBlock * toSoapHeaderBlock(IMessageData *pIMsg);   
};
#endif
