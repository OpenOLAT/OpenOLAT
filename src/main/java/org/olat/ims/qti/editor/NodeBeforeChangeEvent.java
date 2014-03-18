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

package org.olat.ims.qti.editor;

import org.olat.core.gui.control.Event;

/**
 * Initial Date:  06.01.2005
 *
 * @author Mike Stock
 */
public class NodeBeforeChangeEvent extends Event {

	private static final long serialVersionUID = -564355640265507966L;
	/**
	 * the affected controllers can access without getter this boolean.
	 */
	boolean hasNewTitle = false;
	/**
	 * the affected controllers can access without getter this boolean.
	 */
	boolean hasNewObjectives=false;
	private String newTitle=null;
	private String newObjectives=null;
	private String newQuestionMat;
	private String matIdent;
	private String newResponseMat;
	private String sectionId;
	private String questionIdent;
	private String responseIdent;
	private String itemIdent;
	
	/**
	 * @param newTitle
	 */
	public NodeBeforeChangeEvent() {
		super("nce");
	}
	/**
	 * fill in the new title if it has changed
	 * @param changedTitle
	 */
	public void setNewTitle(String changedTitle){
		this.newTitle = changedTitle;
		hasNewTitle = true;
	}
	/**
	 * 
	 * @return null or the changed title
	 */
	public String getNewTitle() { return newTitle; }

	/**
	 * fill in the new objectives if they have changed
	 * @return The new title.
	 */
	public void setNewObjectives(String newObjectives) {
		this.newObjectives = newObjectives;
		hasNewObjectives = true;
	}
	/**
	 * 
	 * @return null or the changed objectives
	 */
	public String getNewObjectives(){
		return newObjectives;
	}
	public void setNewQuestionMaterial(String content) {
		this.newQuestionMat = content;
	}
	public String getNewQuestionMaterial(){
		return this.newQuestionMat;
	}
	public void setMatIdent(String id) {
		this.matIdent=id;
	}
	public String getMatIdent(){
		return this.matIdent;
	}
	public void setNewResponseMaterial(String content) {
		this.newResponseMat=content;
	}
	public String getNewResponseMaterial(){
		return this.newResponseMat;
	}
	public void setSectionIdent(String ident) {
		this.sectionId=ident;
	}
	public String getSectionIdent(){
		return this.sectionId;
	}
	public void setQuestionIdent(String id) {
		this.questionIdent = id;
	}
	public String getQuestionIdent(){
		return this.questionIdent;
	}

	public void setResponseIdent(String id) {
		this.responseIdent = id;
	}
	public String getResponseIdent(){
		return this.responseIdent;
	}
	public void setItemIdent(String ident) {
		this.itemIdent=ident;
	}
	public String getItemIdent(){
		return itemIdent;
	}
}
