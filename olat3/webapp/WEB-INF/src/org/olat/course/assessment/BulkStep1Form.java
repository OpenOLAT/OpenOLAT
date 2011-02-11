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
* <p>
*/

package org.olat.course.assessment;

import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;

import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.bulk.BulkAction;
/**
 * 
 * Description:<br>
 * TODO: schneider Class Description for SeparatedValueInputForm
 * 
 * <P>
 * Initial Date:  19.12.2005 <br>
 *
 * @author Alexander Schneider
 */
class BulkStep1Form extends FormBasicController {
	
	private SingleSelection bulkSelElement;
	String[] fKeys;
	String[] fValues;
	
	/**
	 * @param name
	 * @param trans
	 */
	public BulkStep1Form(UserRequest ureq, WindowControl wControl, List bulkActions) {
		super(ureq, wControl);
		
		int sizeBulkActions = bulkActions.size();
		fKeys = new String[sizeBulkActions];
		fValues = new String[sizeBulkActions];
		int j = 0;
		for (Iterator iter = bulkActions.iterator(); iter.hasNext();) {
			fKeys[j] = Integer.toString(j);
			BulkAction ba = (BulkAction) iter.next();
			fValues[j] = ba.getDisplayName();
			j++;
		}
		
		initForm(ureq);
	}
	
	
	/**
	 * @return selected bulkAction
	 */
	public String getSelectedBulkAction() {
		return bulkSelElement.getSelectedKey();
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		bulkSelElement = uifactory.addRadiosVertical("bulkSelElement", "form.step1.bulkactions", formLayout, fKeys, fValues);
		bulkSelElement.select("0",true);
		
		uifactory.addFormSubmitButton("next", "next", formLayout);
	}
	@Override
	protected void doDispose() {
		//
	}
}