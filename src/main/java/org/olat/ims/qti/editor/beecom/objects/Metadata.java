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

package org.olat.ims.qti.editor.beecom.objects;

import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Element;

/**
 * Initial Date:  08.09.2003
 *
 * @author Mike Stock
 */
public class Metadata implements QTIObject {

	private HashMap<String,String> metadata = new HashMap<>();
	
	
	/* (non-Javadoc)
	 * @see org.olat.ims.qti.editor.beecom.objects.QTIObject#addToElement(org.dom4j.Element)
	 */
	public void addToElement(Element root) {
		if (metadata.size() == 0) return;
		Element qtimetadata = root.addElement("qtimetadata");
		for (Iterator<String> iter = metadata.keySet().iterator(); iter.hasNext();) {
			String key = iter.next();
			String value = metadata.get(key);
			Element metadatafield = qtimetadata.addElement("qtimetadatafield");
			metadatafield.addElement("fieldlabel").setText(key);
			metadatafield.addElement("fieldentry").setText(value);
		}
	}

	public String getField(String key) { return metadata.get(key); }
	
	public void setField(String key, String value) { metadata.put(key, value); }

}