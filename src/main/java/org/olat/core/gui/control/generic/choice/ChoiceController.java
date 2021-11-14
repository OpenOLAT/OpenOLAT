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
* <p>
*/
package org.olat.core.gui.control.generic.choice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.Reset;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormReset;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * Description:<br>
 * Multiple/single choice controller with at least a <code>Submit</code> <code>FormItem</code>. <p>
 * One could add a <code>Reset</code> <code>FormItem</code>, if neccessary. (see: addReset() method)
 * 
 * <P>
 * Initial Date: 06.08.2007 <br>
 * 
 * @author Lavinia Dumitrescu
 */
public class ChoiceController extends FormBasicController {

	private String[] keysIn;
	private String[] translatedKeys;
	private boolean singleSelection = true;
	private boolean layoutVertical = true;
	private SelectionElement entrySelector;

	private String selectionName = "choiceSelection";
	private String submitI18nKey = "apply";
	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param keys
	 * @param translatedKeys
	 * @param selectedKeys
	 * @param singleSelection
	 * @param layoutVertical
	 * @param submitI18nKey
	 */
	public ChoiceController(UserRequest ureq, WindowControl wControl, String[] keys, String[] translatedKeys, String[] selectedKeys,
			boolean singleSelection, boolean layoutVertical, String submitI18nKey) {
		super(ureq, wControl);
		this.keysIn = keys;
		this.translatedKeys = translatedKeys;
		this.singleSelection = singleSelection;
		this.layoutVertical = layoutVertical;
		if (submitI18nKey != null) {
			this.submitI18nKey = submitI18nKey;
		}
		/*
		 * init form element(s)
		 */
		initForm(this.flc, this, ureq);
		/*
		 * after initialising the element, select the entries
		 */
		for (int i = 0; i < selectedKeys.length; i++) {
			entrySelector.select(selectedKeys[i], true);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		if(singleSelection && layoutVertical){
			entrySelector = uifactory.addRadiosVertical(selectionName, null, formLayout, keysIn, translatedKeys);
		}else if(singleSelection && !layoutVertical){
			entrySelector = uifactory.addRadiosHorizontal(selectionName, null, formLayout, keysIn, translatedKeys);
		}else if(!singleSelection && layoutVertical){
			entrySelector = uifactory.addCheckboxesVertical(selectionName, null, formLayout, keysIn, translatedKeys, 1);
		}else if(!singleSelection && !layoutVertical){
			entrySelector = uifactory.addCheckboxesHorizontal(selectionName, null, formLayout, keysIn, translatedKeys);
		}
		
		// add Submit
		Submit subm = new FormSubmit("subm", submitI18nKey);
		formLayout.add(subm);
	}

	/**
	 * Adds a <code>Reset</code> <code>FormItem</code> to the current <code>FormLayoutContainer</code>.	 
	 * @param i18nKey
	 */
	public void addReset(String i18nKey) {
		Reset reset = new FormReset("reset", i18nKey);
		this.flc.add(reset);
	}

	/**
	 * Gets the list of the selected entry's keys.<p>
	 * Do call this at event reception!
	 * @return a not null selected keys List.
	 */
	public List<String> getSelectedEntries() {
		List<String> selected = new ArrayList<>();
		if (entrySelector instanceof MultipleSelectionElement) {
			//sort the selected keys according with the keysIn order 
			Collection<String> selectedKeys = ((MultipleSelectionElement) entrySelector).getSelectedKeys(); 			
			int numKeys = keysIn.length;
			for(int i=0; i<numKeys; i++) {
				if(selectedKeys.contains(keysIn[i])) {
					selected.add(keysIn[i]);
				}
			}
		} else if (entrySelector instanceof SingleSelection) {			
			selected.add(((SingleSelection) entrySelector).getSelectedKey());
			return selected;
		}
		return selected;
	}
}
