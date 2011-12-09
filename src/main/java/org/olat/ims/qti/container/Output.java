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

package org.olat.ims.qti.container;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.olat.ims.qti.container.qtielements.GenericQTIElement;
import org.olat.ims.qti.container.qtielements.Hint;
import org.olat.ims.qti.container.qtielements.Solution;
/**
 * 
 */
public class Output implements Serializable {

	private GenericQTIElement el_response;
	private Hint hint;
	private Solution solution;
	private List itemresponses = null;

	/**
	 * @return Element
	 */
	public GenericQTIElement getEl_response() {
		return el_response;
	}

	/**
	 * Sets the el_response.
	 * @param el_response The el_response to set
	 */
	public void setEl_response(GenericQTIElement el_response) {
		this.el_response = el_response;
	}

	/**
	 * Method addItem_El_response.
	 * @param el_resolved
	 */
	public void addItem_El_response(Element el_answerchosen, Element el_new_response) {
		if (itemresponses == null) itemresponses = new ArrayList();		
		itemresponses.add(new Element[]{el_answerchosen, el_new_response});
	}
	
	public Element getItemFeedback(int which) {
		return ((Element[])itemresponses.get(which))[1];
	}
	
	public Element getItemAnswerChosen(int which) {
		return ((Element[])itemresponses.get(which))[0];
	}
	
	public int getFeedbackCount() {
		return (itemresponses == null? 0 : itemresponses.size());
	}
	
	public boolean hasItem_Responses() {
		return itemresponses != null;
	}

	public Hint getHint() {
		return hint;
	}
	public void setHint(Hint hint) {
		this.hint = hint;
	}
	public Solution getSolution() {
		return solution;
	}
	public void setSolution(Solution solution) {
		this.solution = solution;
	}
}
