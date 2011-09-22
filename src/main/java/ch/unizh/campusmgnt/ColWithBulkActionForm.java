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

package ch.unizh.campusmgnt;

import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.bulk.BulkAction;

import ch.unizh.campusmgnt.controller.CampusManagementController;
/**
 * 
 * Description:<br>
 * TODO: schneider Class Description for ColWithBulkActionForm
 * 
 * <P>
 * Initial Date:  19.12.2005 <br>
 *
 * @author Alexander Schneider
 */
public class ColWithBulkActionForm extends FormBasicController {

	private SingleSelection colSelElement;
	private SingleSelection bulkSelElement;
	
	private String[] cKeys, cValues;
	private String[] fKeys, fValues;
	/**
	 * @param name
	 * @param trans
	 */
	public ColWithBulkActionForm(UserRequest ureq, WindowControl wControl, List columns, List bulkActions) {
		super(ureq, wControl);
		
		setBasePackage(CampusManagementController.class);
		
		int sizeCols = columns.size();
		cKeys = new String[sizeCols];
		cValues = new String[sizeCols];
		int i = 0;
		for (Iterator iter = columns.iterator(); iter.hasNext();) {
			cKeys[i] = Integer.toString(i);
			cValues[i] = (String)iter.next();
			i++;
		}
		
		
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
		
		initForm (ureq);
	}
	/**
	 * @see org.olat.core.gui.components.Form#validate(org.olat.core.gui.UserRequest)
	 */
	public boolean validate() {
		return true;
	}
	
	/**
	 * @return selected bulkAction
	 */
	public String getSelectedBulkAction() {
		return bulkSelElement.getSelectedKey();
	}
	
	/**
	 * @return selected column
	 */
	public String getSelectedColumn() {
		return colSelElement.getSelectedKey();
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		colSelElement = uifactory.addDropdownSingleselect("colSelElement", "form.step3.columns", formLayout, cKeys, cValues, null);
		colSelElement.select(cKeys[0],true);
		
		bulkSelElement = uifactory.addDropdownSingleselect("bulkSelElement", "form.step3.bulkactions", formLayout, fKeys, fValues, null);
		bulkSelElement.select(fKeys[0],true);
		
		uifactory.addFormSubmitButton("next", formLayout);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
}