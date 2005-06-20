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
 */
package org.apache.axis.attachments;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * @author <a href="mailto:thilina@opensource.lk">Thilina Gunarathne </a>
 * This class provides input streams delimited by the mime boundaries.
 */
public class MimeBodyPartInputStream extends InputStream {
	PushbackInputStream inStream;
	
	byte[] boundary;
	
	public MimeBodyPartInputStream(PushbackInputStream inStream, byte[] boundary) {
		super();
		this.inStream = inStream;
		this.boundary = boundary;
	}
	
	public int read() throws IOException {
		
		// read the next value from stream
		int value = inStream.read();
		
		//read value is not the first byte of the boundary
		if ((byte) value != boundary[0]) {
			return value;
		}
		
		// read value is the first byte of the boundary. Start matching the
		// next characters to find a boundary
		int boundaryIndex = 0; 
		while ((boundaryIndex < boundary.length)
				&& ((byte) value == boundary[boundaryIndex])) {
			value = inStream.read();
			boundaryIndex++;
		}
		
		if (boundaryIndex == boundary.length) { // boundary found
			return -1;
		}
		
		// Boundary not found. Restoring bytes skipped.
		// write first skipped byte, push back the rest
		
		if (value != -1) { // Stream might have ended
			inStream.unread(value); 
		}
		inStream.unread(boundary, 1, boundaryIndex - 1);
		return boundary[0];
	}
}