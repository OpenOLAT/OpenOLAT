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

package org.olat.ims.resources;

import java.io.InputStream;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author mike
 *
 */
public class IMSEntityResolver implements EntityResolver {

	/**
	 * Default constructor.
	 */
	public IMSEntityResolver() {
		super();
	}

	/**
	 * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
	 */
	public InputSource resolveEntity(String publicId, String systemId) {
		systemId = systemId.substring(systemId.lastIndexOf('/') + 1);
		// allways test extensions with lowercase
		String systemId_ = systemId.toLowerCase();
		if (systemId_.endsWith(".dtd")) {
			InputStream in = getClass().getResourceAsStream("/org/olat/ims/resources/dtd/" + systemId);
			return new InputSource(in);
		} else if (systemId_.endsWith(".xsd")) {
			InputStream in = getClass().getResourceAsStream("/org/olat/ims/resources/xsd/" + systemId);
			return new InputSource(in);
		} else if (systemId_.endsWith(".xsl")) {
			InputStream in = getClass().getResourceAsStream("/org/olat/ims/resources/xsl/" + systemId);
			return new InputSource(in);
		}
		throw new RuntimeException("dtd '"+systemId+"', pubId "+publicId+" could not be resolved by OLAT");
		//return null;
	}
}
