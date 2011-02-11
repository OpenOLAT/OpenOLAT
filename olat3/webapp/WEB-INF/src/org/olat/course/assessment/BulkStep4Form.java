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
class BulkStep4Form extends FormBasicController {

	private SingleSelection keySelElement;
	private SingleSelection colSelElement;
	
	String[] cKeys;
	String[] cValues;
	
	List olatKeys;
	
	/**
	 * @param name
	 * @param trans
	 */
	public BulkStep4Form(UserRequest ureq, WindowControl wControl, List olatKeys, List columns) {
		super(ureq, wControl);
		
		this.olatKeys = olatKeys;
		
		int sizeCols = columns.size();
		cKeys = new String[sizeCols];
		cValues = new String[sizeCols];
		
		int j = 0;
		for (Iterator iter = columns.iterator(); iter.hasNext();) {
			cKeys[j] = Integer.toString(j);
			cValues[j] = (String)iter.next();
			j++;
		}
		
		initForm (ureq);
	}
	
	
	/**
	 * @return selected olatKey
	 */
	public String getSelectedOlatKey() {
		return keySelElement.getSelectedKey();
	}
	
	/**
	 * @return selected value of olatKey
	 */
	public String getSelectedValueOfOlatKey(int key) {
		return keySelElement.getValue(key);
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
		
		colSelElement = uifactory.addDropdownSingleselect("colSelElement", "form.step4.columns", formLayout, cKeys, cValues, null);
		colSelElement.select("0",true);
		
	
		int sizeOlKs = olatKeys.size();
		String[] oKeys = new String[sizeOlKs];
		String[] oValues = new String[sizeOlKs];
		int i = 0;
		for (Iterator iter = olatKeys.iterator(); iter.hasNext();) {
			oKeys[i] = Integer.toString(i);
			oValues[i] = (String)iter.next();
			i++;
		}
		keySelElement = uifactory.addDropdownSingleselect("keySelElement", "form.step4.olatkeys", formLayout, oKeys, oValues, null);
		keySelElement.select("0",true);
		
		uifactory.addFormSubmitButton("next", "next", formLayout);
	}
	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}
	
}