/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.core.util.filter.impl;

import org.olat.core.util.filter.Filter;

public class XMLValidCharacterFilter implements Filter {

	@Override
	public String filter(String in) {
		if(in == null) return null;
		
		StringBuilder out = new StringBuilder(); // Used to hold the output.
		int codePoint; // Used to reference the current character.

		int i = 0;
		while (i < in.length()) {
			codePoint = in.codePointAt(i); // This is the unicode code of the
										   // character.

			if ((codePoint == 0x9) // Consider testing larger ranges first to improve speed.
					|| (codePoint == 0xA) || (codePoint == 0xD)
					|| ((codePoint >= 0x20) && (codePoint <= 0xD7FF))
					|| ((codePoint >= 0xE000) && (codePoint <= 0xFFFD))
					|| ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF))) {
				out.append(Character.toChars(codePoint));
			}
			i += Character.charCount(codePoint); // Increment with the number of
												 // code units(java chars)
												 // needed to represent a
												 // Unicode char.
		}
		return out.toString();
	}
}
