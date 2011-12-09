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

import org.dom4j.Element;
import org.olat.core.util.CodeHelper;

/**
 * @author rkulow
 *
 */
public class Matimage implements QTIObject, MatElement {
	
	private String id = null;
	private String URI = null;
	
	public Matimage(String uri) {
		id = "" + CodeHelper.getRAMUniqueID();
		setURI(uri);
	}
	
	/**
	 * @see org.olat.ims.qti.editor.beecom.objects.QTIObject#addToElement(org.dom4j.Element)
	 */
	public void addToElement(Element root) {
		if (URI != null) {
			Element matimage = root.addElement("matimage");
			matimage.addAttribute("uri", URI);
		}
	}

	/**
	 * @see org.olat.ims.qti.editor.beecom.objects.MatElement#renderAsHtml(java.lang.String)
	 */
	public String renderAsHtml(String mediaBaseURL) {
		if (URI == null) return "[ IMAGE: no image selected ]";
		boolean relURI = (URI.indexOf("://") == -1);
		StringBuilder sb = new StringBuilder("<img src=\"");
		if (relURI) // relative URI, add mediaBaseURI
			sb.append(mediaBaseURL + "/");
		sb.append(URI);
		sb.append("\" border=\"0\" alt=\"");
		sb.append(URI);
		sb.append("\">");
		return sb.toString();
	}

	/**
	 * @see org.olat.ims.qti.editor.beecom.objects.MatElement#renderAsText()
	 */
	public String renderAsText() {
		if (URI == null) return "[ IMAGE: no image selected ]";
		return "[ IMAGE: "+URI+" ]";
	}

	/**
	 * @return
	 */
	public String getURI() {
		return URI;
	}

	/**
	 * @param string
	 */
	public void setURI(String string) {
		URI = string;
	}

	/**
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param string
	 */
	public void setId(String string) {
		id = string;
	}

}
