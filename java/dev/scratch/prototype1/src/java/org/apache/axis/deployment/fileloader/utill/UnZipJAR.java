package org.apache.axis.deployment.fileloader.utill;

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
 *
 * @author Deepal Jayasinghe
 *         Oct 5, 2004
 *         2:54:57 PM
 *
 */

import org.apache.axis.deployment.DeployCons;
import org.apache.axis.deployment.DeploymentEngine;
import org.apache.axis.deployment.MetaData.ServiceMetaData;
import org.apache.axis.deployment.schemaparser.SchemaParser;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipFile;
import java.util.Enumeration;

public class UnZipJAR implements DeployCons {
    final int BUFFER = 2048;
    /**
     * This method will unzip the given jar or aar.
     * it take two arguments filename and refereance to DeployEngine
     * @param filename
     * @param engine
     */
    public ServiceMetaData unzip(String filename, DeploymentEngine engine) {
        ServiceMetaData service =null;
        // get attribute values
        String strArchive = filename;
        String tempfile = "C:/tem.txt";
        ZipInputStream zin;
        int entrysize = 0;
        try {
            zin = new ZipInputStream(new FileInputStream(strArchive));
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.getName().equals(SERVICEXML)) {
                    SchemaParser schme = new SchemaParser(zin, engine, filename);
                    service = schme.parseServiceXML();
                }
            }
            zin.closeEntry();
            zin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return service;
    }
}








