package org.apache.axis.om;

import javax.xml.namespace.QName;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * One must implement relevant constructors for the class implementing this interface
 * all the things like namespace, parent, value, etc., that should come in this are defined in base classes
 */
public interface OMAttribute{

   public String getLocalName();

    public void setLocalName(String localName);

    public String getValue();

    public void setValue(String value);

    public void setOMNamespace(OMNamespace omNamespace);

    public OMNamespace getNamespace();

    public QName getQName();


}