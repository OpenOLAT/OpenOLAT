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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

import ch.unizh.campusmgnt.controller.CampusManagementController;

/**
 * 
 * Description:<br>
 * Display a textarea for separated values and two radios for chosing tab or comma as delimiter
 * 
 * <P>
 * Initial Date:  19.12.2005 <br>
 *
 * @author Alexander Schneider
 */
public class SeparatedValueInputForm extends FormBasicController {
	private TextElement idata;
	private SingleSelection delimiter;
	private List rows;
	private int numOfValPerLine;
	private int numOfLines;
	
	private String[] delKeys, delValues;
	/**
	 * @param name
	 * @param trans
	 */
	public SeparatedValueInputForm(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		setBasePackage(CampusManagementController.class);
		
		delKeys = new String[] {"tab","comma"};
		delValues = new String[] {translate("form.step1.delimiter.tab"),translate("form.step1.delimiter.comma")};
		
		initForm (ureq);
	}
	
	@Override
	protected boolean validateFormLogic (UserRequest ureq) {
		String errorKey = processInput();
		if(errorKey != null){
			idata.setErrorKey(errorKey, null);
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * add input values to a list
	 * @return String error, if return null, input values are added to list successfully 
	 */
	private String processInput(){
		String error = null;
		String[] lines = idata.getValue().split("\r?\n");
		this.numOfLines = lines.length;
		
		this.rows = new ArrayList(this.numOfLines);
		List inputRows = new ArrayList(this.numOfLines);
		
		String d;
		if (delimiter.getSelectedKey().startsWith("t")) d = "\t"; else d = ",";
		
		int maxNumOfCols = 0;
		for (int i = 0; i < numOfLines; i++) {
			String line = lines[i];
			List lineFields;
			if(!line.equals("")){
				Object[] values = line.split(d,-1);
				if(values.length > maxNumOfCols) maxNumOfCols = values.length;
				lineFields = new ArrayList(Arrays.asList(values));
			}else{
				lineFields = new ArrayList(maxNumOfCols);
				lineFields.add(" ");
			}
			inputRows.add(lineFields);
		}
		this.numOfValPerLine = maxNumOfCols;
		
		for (Iterator iter = inputRows.iterator(); iter.hasNext();) {
			List lineFields = (ArrayList) iter.next();
			int numOfLineFields = lineFields.size();
			if (numOfLineFields != maxNumOfCols){
				for(int i=0; i < maxNumOfCols - numOfLineFields; i++){
					lineFields.add(" ");
				}
			}
			// add an additional column to reduce number of preconditions
			// e.g. user adds lines with only one value; user adds lines with no empty value in a line 
			lineFields.add(" ");
		}
		
		for (Iterator iter = inputRows.iterator(); iter.hasNext();) {
			List lineFields = (List) iter.next();
			rows.add(lineFields.toArray());
		}	
		return error;
	}
	
	/**
	 * 
	 * @return a list containing every input line as an object array. The fields of an object array are the
	 * separared values 
	 */
	public List getInputRows(){
		return rows;
	}
	
	/**
	 * 
	 * @return int number of separated values per line
	 */
	public int getNumOfValPerLine(){
		return numOfValPerLine;
	}
	
	/**
	 * 
	 * @return int number of all lines with separated values
	 */
	public int getNumOfLines(){
		return numOfLines;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		idata = uifactory.addTextAreaElement("addsepval", "form.step1.sepvalin", -1, 5, 80, true, "", formLayout);
		idata.setNotEmptyCheck("form.legende.mandatory");
		
		delimiter = uifactory.addRadiosVertical("delimiter", "form.step1.delimiter", formLayout, delKeys, delValues);
		delimiter.select("tab", true);
	
		uifactory.addFormSubmitButton("next", formLayout);
	}
	
	@Override
	protected void doDispose() {
		//
	}
}