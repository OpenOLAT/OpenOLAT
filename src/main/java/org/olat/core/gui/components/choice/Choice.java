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

package org.olat.core.gui.components.choice;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * A <b>Choice </b> is
 * 
 * @author Felix Jost
 */
public class Choice extends Component {
	private static final ComponentRenderer RENDERER = new ChoiceRenderer();

	/**
	 * Comment for <code>EVENT_VALIDATION_OK</code>
	 */
	public static final Event EVNT_VALIDATION_OK = new Event("validation ok");
	/**
	 * Comment for <code>EVENT_FORM_CANCELLED</code>
	 */
	public static final Event EVNT_FORM_CANCELLED = new Event("form_cancelled");
	/**
	 * Comment for <code>CANCEL_IDENTIFICATION</code>
	 */
	public static final String CANCEL_IDENTIFICATION = "olat_foca";

	private String submitKey;
	private String cancelKey;
	private boolean displayOnly = false;
	private List selectedRows = new ArrayList();
	private List removedRows = new ArrayList();
	private List addedRows = new ArrayList();
	private TableDataModel tableDataModel;

	/**
	 * @param name of the component
	 */
	public Choice(String name) {
		super(name);
	}

	/**
	 * @param name of the component
	 */
	public Choice(String name, Translator translator) {
		// Use translation keys from table for toggle on/off switch
		super(name, Util.createPackageTranslator(Table.class, translator.getLocale(), translator));
	}

	/**
	 * @see org.olat.core.gui.components.Component#dispatchRequest(org.olat.core.gui.UserRequest)
	 */
	protected void doDispatchRequest(UserRequest ureq) {
		// since we are a >form<, this must be a submit or a cancel
		// check for cancel first
		if (ureq.getParameter(CANCEL_IDENTIFICATION) != null) {
			fireEvent(ureq, EVNT_FORM_CANCELLED);
		} else {
			selectedRows.clear();
			removedRows.clear();
			addedRows.clear();
			// standard behavior: set all values, validate, and fire Event
			int size = tableDataModel.getRowCount();
			for (int i = 0; i < size; i++) {
				String keyN = "c" + i;
				String exists = ureq.getParameter(keyN);
				Boolean oldV = (Boolean) tableDataModel.getValueAt(i, 0); // column 0
				// must always
				// return a
				// Boolean
				boolean wasPreviouslySelected = oldV.booleanValue();
				// add to different lists
				Integer key = new Integer(i);
				if (exists != null) { // the row was selected
					selectedRows.add(key);
					if (!wasPreviouslySelected) { // not selected in model, but now ->
						// "added"
						addedRows.add(key);
					}
				} else {
					// the row was not selected
					if (wasPreviouslySelected) { // was selected in model, but not now
						// anymore -> "removed"
						removedRows.add(key);
					}
				}

			}
			setDirty(true);
			fireEvent(ureq, EVNT_VALIDATION_OK);
		}
	}

	/**
	 * @return String
	 */
	public String getCancelKey() {
		return cancelKey;
	}

	/**
	 * @return String
	 */
	public String getSubmitKey() {
		return submitKey;
	}

	/**
	 * @param string
	 */
	public void setCancelKey(String string) {
		cancelKey = string;
	}

	/**
	 * @param string
	 */
	public void setSubmitKey(String string) {
		submitKey = string;
	}

	/**
	 * @see org.olat.core.gui.components.Component#getExtendedDebugInfo()
	 */
	public String getExtendedDebugInfo() {
		return "choice: "
				+ (tableDataModel == null ? "no model!" : "rows:" + tableDataModel.getRowCount() + ", cols:" + tableDataModel.getColumnCount());
	}

	/**
	 * @return boolean
	 */
	public boolean isDisplayOnly() {
		return displayOnly;
	}

	/**
	 * @param b
	 */
	public void setDisplayOnly(boolean b) {
		displayOnly = b;
	}

	/**
	 * @return the List of the selected rows indexes (List of Integers).
	 */
	public List getSelectedRows() {
		return selectedRows;
	}

	/**
	 * @return TableDataModel
	 */
	public TableDataModel getTableDataModel() {
		return tableDataModel;
	}

	/**
	 * the tabledatamodel to represent the choice data. one row belongs to one
	 * checkbox/choice; the columns are merely for graphical reasons. <br>
	 * Important: the first column must return a Boolean object to indicate
	 * whether the according row is currently selected or not
	 * 
	 * @param model
	 */
	public void setTableDataModel(TableDataModel model) {
		tableDataModel = model;
	}

	/**
	 * @return Returns the addedRows (a List of Integers, one Integer stands for
	 *         the position in the model of the element added).
	 */
	public List getAddedRows() {
		return addedRows;
	}

	/**
	 * @return Returns the removedRows.
	 */
	public List getRemovedRows() {
		return removedRows;
	}

	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	/**
	 * @see org.olat.core.gui.components.Component#validate(org.olat.core.gui.UserRequest, org.olat.core.gui.render.ValidationResult)
	 */
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		// include needed css and js libs
		JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
		jsa.addRequiredJsFile(Choice.class, "js/choice.js");
	}

}