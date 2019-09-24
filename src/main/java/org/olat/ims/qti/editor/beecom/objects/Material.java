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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;

/**
 * @author rkulow
 *
 */
public class Material implements QTIObject, MatElement {
	
	private String id;
	private String lable = null;
	private List<QTIObject> elements = new ArrayList<>();
	
	public Material() {
		id = "" + CodeHelper.getRAMUniqueID();
	}
	
	/**
	 * @see org.olat.ims.qti.editor.beecom.objects.QTIObject#addToElement(org.dom4j.Element)
	 */
	public void addToElement(Element root) {
		if (elements.size() == 0) return;
		Element material = root.addElement("material");
		if(this.lable != null)
			material.addAttribute("label", this.lable);

		for(QTIObject tmp : elements) {
			tmp.addToElement(material);	
		}
	}
	
	public String renderAsHtml(String mediaBaseURL) {
		StringBuilder sb = new StringBuilder();
		for (Iterator<QTIObject> iter = elements.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (obj instanceof MatElement)
				sb.append(((MatElement)obj).renderAsHtml(mediaBaseURL));
		}
		if (!mediaBaseURL.equals("")) {
			Filter urlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(mediaBaseURL);
			return urlFilter.filter(sb.toString());
		}
		return sb.toString();
	}

	public String renderAsHtmlForEditor() {
		String htmlWithToken = renderAsHtml("");
		return htmlWithToken;
	}
	
	/**
	 * @see org.olat.ims.qti.editor.beecom.objects.MatElement#renderAsText()
	 */
	public String renderAsText() {
		StringBuilder sb = new StringBuilder();
		for (Iterator<QTIObject> iter = elements.iterator(); iter.hasNext();) {
			Object obj = iter.next();
			if (obj instanceof MatElement)
				sb.append(((MatElement)obj).renderAsText());
		}
		return sb.toString();
	}
	
	public void add(QTIObject obj) { elements.add(obj); }
	
	public void removeById(String sId) {
		Object obj = findById(sId);
		if (obj != null) elements.remove(obj);
 }

	public MatElement findById(String sId) {
		for (Iterator<QTIObject> iter = elements.iterator(); iter.hasNext();) {
		Object obj = iter.next();
			if (!(obj instanceof MatElement)) continue;
			if (((MatElement)obj).getId().equals(sId))
				return (MatElement)obj;
		}
		return null;
	}
	
	/**
	 * Returns the elements.
	 * @return List
	 */
	public List<QTIObject> getElements() {
		return elements;
	}

	/**
	 * Returns the lable.
	 * @return String
	 */
	public String getLable() {
		return lable;
	}

	/**
	 * Sets the elements.
	 * @param elements The elements to set
	 */
	public void setElements(List<QTIObject> elements) {
		this.elements = elements;
	}

	/**
	 * Sets the lable.
	 * @param lable The lable to set
	 */
	public void setLable(String lable) {
		this.lable = lable;
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
