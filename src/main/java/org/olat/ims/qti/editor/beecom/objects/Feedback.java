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

/**
 * @author rkulow
 *
 */
public class Feedback implements QTIObject {
	
	private String ident = null;
	private String title = null;
	private String view = null;
	private List materials = new ArrayList();
	
	/**
	 * Returns the ident.
	 * @return String
	 */
	public String getIdent() {
		return ident;
	}

	/**
	 * Returns the material.
	 * @return List
	 */
	public List getMaterials() {
		return materials;
	}

	/**
	 * Returns the title.
	 * @return String
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Returns the view.
	 * @return String
	 */
	public String getView() {
		return view;
	}

	/**
	 * Sets the ident.
	 * @param ident The ident to set
	 */
	public void setIdent(String ident) {
		this.ident = ident;
	}

	/**
	 * Sets the material.
	 * @param material The material to set
	 */
	public void setMaterials(List materials) {
		this.materials = materials;
	}

	/**
	 * Sets the title.
	 * @param title The title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Sets the view.
	 * @param view The view to set
	 */
	public void setView(String view) {
		this.view = view;
	}
	
	/**
	 * @see org.olat.ims.qti.editor.beecom.objects.QTIObject#addToElement(org.dom4j.Element)
	 */
	public void addToElement(Element root) {

		// don't presist feedbacks with no materials		
		if (materials.size() == 0) return;
		
		String rootName = root.getName();
		Element feedback = root.addElement(rootName+"feedback");
		
		if(this.ident!=null) {
			feedback.addAttribute("ident", this.ident);
		}
		if(this.title!=null) {
			feedback.addAttribute("title",this.title);
		}
		if(this.view!=null) {
			feedback.addAttribute("view",this.view);
		}

		for(Iterator i = this.materials.iterator(); i.hasNext();) {
			QTIObject obj = (QTIObject)i.next();
			if(obj!=null) {
				obj.addToElement(feedback);	
			}			
		}
	}

}
