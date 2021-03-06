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

package org.apache.geronimo.ews.ws4j2ee.context;

import org.apache.geronimo.ews.ws4j2ee.toWs.GenerationFault;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>Input file can be a file or a input stream this class hide the both behind the
 * same interface</p>
 */
public interface InputOutputFile {
    public String fileName() throws GenerationFault;

    public InputStream getInputStream() throws GenerationFault;

    public OutputStream getOutStream() throws GenerationFault;
}
