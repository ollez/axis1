/*
 *   Copyright 2003-2004 The Apache Software Foundation.
// (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved
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

/**
 * This class has all the information about the Webservice. It pass through 
 * all the classes in genaration of wrapper giving them the info they are 
 * required. This is the Mediator that make sure all the classes interact with 
 * this for information but with this. It will keep the system loosley coupled
 * So easier to handle.  
 *  
 * @author Srinath Perera (hemapani@opensource.lk)
 * @author Dimuthu Leelarathne (muthulee@opensource.lk)
 */

package org.apache.axis.wsdl.wsdl2ws.info;

import java.util.HashSet;

import org.apache.axis.wsdl.wsdl2ws.WrapperFault;

public class WebServiceContext
{
    private WrapperInfo wrapInfo;
    private ServiceInfo serInfo;
    private TypeMap typemap;

    private HashSet generatedFileList = new HashSet();

    public WebServiceContext(
        WrapperInfo wrapInfo,
        ServiceInfo serInfo,
        TypeMap typemap)
        throws WrapperFault
    {
        this.wrapInfo = wrapInfo;
        this.serInfo = serInfo;
        this.typemap = typemap;

        if (wrapInfo == null || this.serInfo == null || this.typemap == null)
        {
            throw new WrapperFault("Insufficient Info");
        }
    }

    /**
     * @return ServiceInfo
     */
    public ServiceInfo getSerInfo()
    {
        return serInfo;
    }

    /**
     * Adds a name of the generated file 
     * @param filename
     */
    public void addGeneratedFile(String filename)
    {
        generatedFileList.add(filename);
    }

    public HashSet getGeneratedFileList()
    {
        return generatedFileList;
    }
    /**
     * @return TypeMap
     */
    public TypeMap getTypemap()
    {
        return typemap;
    }

    /**
     * @return WrapperInfo
     */
    public WrapperInfo getWrapInfo()
    {
        return wrapInfo;
    }

}
