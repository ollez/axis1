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
package org.apache.axis.tracetool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A parameter from a method signature. The parameter is the datatype plus its 
 * name but not its value. 
 */
class Parameter {
	private ArrayList type = new ArrayList();
	private String name = null;
	private boolean failed = false;

	/**
	 *  Defaults to a parameter in a parameter list so it HAS a parameter name in it
	 */
	Parameter(List parts) {
		this(parts, false);
	}

	/**
	 * A parameter which is a return type does not have a parameter name.
	 * Parameters in a parameter list do have parameter names embedded in them 
	 */
	Parameter(List parts, boolean isRetType) {
		if (parts == null || parts.size() == 0)
			return;

		if (!isRetType && parts.size() == 1) {
			if ("...".equals(parts.get(0))) {
				type.add("...");
				name = "";
			} else if (!"void".equals(parts.get(0)))
				failed = true; // Seems like bad C++ code here
			return;
		}

		if (isRetType) {
			Iterator it = parts.iterator();
			while (it.hasNext())
				type.add(it.next());

			// Some methods return have void on their signature and others
			// have nothing. So to make them both the same, if a method 
			// doesn't return anything make type null.
			if (1 == type.size() && "void".equals(type.get(0)))
				type = new ArrayList();

		} else {
			// Cope with array subscripts [] after the name
			int arrIdx = -1;
			for (int i = 0; i < parts.size(); i++) {
				String tok = (String) parts.get(i);
				if ("[".equals(tok)) {
					arrIdx = i;
					break;
				}
			}

			// Find the name			
			int nameIdx = parts.size() - 1;
			if (-1 != arrIdx)
				nameIdx = arrIdx - 1;

			// Construct the type
			for (int i = 0; i < nameIdx; i++)
				type.add(parts.get(i));

			name = (String) parts.get(nameIdx);
			if (-1 != arrIdx)
				for (int i = arrIdx; i < parts.size(); i++)
					type.add(parts.get(i));
		}
	}

	boolean failed() {
		return failed;
	}

	String getType() {
		String s = null;
		Iterator it = type.iterator();
		while (it.hasNext()) {
			String next = (String) it.next();
			if (null == s)
				s = next;
			else if ("*".equals(next) || "&".equals(next))
				s += next;
			else
				s += " " + next;
		}
		return s;
	}

	String getTypeWithoutConst() {
		String s = null;
		Iterator it = type.iterator();
		while (it.hasNext()) {
			String next = (String) it.next();
			if ("const".equals(next))
				continue;
			else if (null == s)
				s = next;
			else if ("*".equals(next) || "&".equals(next))
				s += next;
			else
				s += " " + next;
		}
		return s;
	}

	String getName() {
		return name;
	}

	boolean isVoid() {
		return 0 == type.size();
	}

      boolean isDotDotDot() {
            return 1 == type.size() && "...".equals(type.get(0));
      }

	public String toString() {
		if (0 == type.size())
			return "void";
		if (null == name)
			return getType();
		return getType() + " " + name;
	}
}