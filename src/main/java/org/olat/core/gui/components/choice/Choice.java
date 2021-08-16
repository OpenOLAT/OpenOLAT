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
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * A <b>Choice </b> is
 * 
 * @author Felix Jost
 */
public class Choice extends AbstractComponent {
	private static final ComponentRenderer RENDERER = new ChoiceRenderer();

	public static final Event EVNT_VALIDATION_OK = new Event("validation ok");
	public static final Event EVNT_FORM_CANCELLED = new Event("form_cancelled");
	public static final Event EVNT_FORM_RESETED = new Event("form_reseted");
	public static final String CANCEL_IDENTIFICATION = "olat_foca";
	public static final String RESET_IDENTIFICATION = "olat_fore";

	private String submitKey, cancelKey, resetKey;
	private boolean displayOnly = false;
	private List<Integer> selectedRows = new ArrayList<>();
	private List<Integer> removedRows = new ArrayList<>();
	private List<Integer> addedRows = new ArrayList<>();
	private ChoiceModel<?> model;
	
	private boolean escapeHtml = true;

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

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		// since we are a >form<, this must be a submit or a cancel
		// check for cancel first
		
		String action = ureq.getParameter("multi_action_identifier");
		if (CANCEL_IDENTIFICATION.equals(action)) {
			fireEvent(ureq, EVNT_FORM_CANCELLED);
		} else if (RESET_IDENTIFICATION.equals(action)) {
			fireEvent(ureq, EVNT_FORM_RESETED);
		} else {
			selectedRows.clear();
			removedRows.clear();
			addedRows.clear();
			// standard behavior: set all values, validate, and fire Event
			int size = model.getRowCount();
			for (int i = 0; i < size; i++) {
				String keyN = "c" + i;
				String exists = ureq.getParameter(keyN);
				Boolean oldV = model.isEnabled(i); // column 0
				// must always
				// return a
				// Boolean
				boolean wasPreviouslySelected = oldV.booleanValue();
				// add to different lists
				Integer key = Integer.valueOf(i);
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
	
	public String getSubmitKey() {
		return submitKey;
	}

	public void setSubmitKey(String string) {
		submitKey = string;
	}
	
	public String getCancelKey() {
		return cancelKey;
	}

	public void setCancelKey(String string) {
		cancelKey = string;
	}

	public String getResetKey() {
		return resetKey;
	}

	public void setResetKey(String resetKey) {
		this.resetKey = resetKey;
	}

	public boolean isEscapeHtml() {
		return escapeHtml;
	}

	public void setEscapeHtml(boolean escapeHtml) {
		this.escapeHtml = escapeHtml;
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
	public List<Integer> getSelectedRows() {
		return selectedRows;
	}
	
	public List<Integer> getAllRows() {
		int size = model.getRowCount();
		List<Integer> allRows = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			allRows.add(Integer.valueOf(i));
		}
		return allRows;
	}

	/**
	 * @return TableDataModel
	 */
	public ChoiceModel<?> getModel() {
		return model;
	}

	/**
	 * the tabledatamodel to represent the choice data. one row belongs to one
	 * checkbox/choice; the columns are merely for graphical reasons. <br>
	 * Important: the first column must return a Boolean object to indicate
	 * whether the according row is currently selected or not
	 * 
	 * @param model
	 */
	public void setModel(ChoiceModel<?> model) {
		this.model = model;
	}

	/**
	 * @return Returns the addedRows (a List of Integers, one Integer stands for
	 *         the position in the model of the element added).
	 */
	public List<Integer> getAddedRows() {
		return addedRows;
	}

	/**
	 * @return Returns the removedRows.
	 */
	public List<Integer> getRemovedRows() {
		return removedRows;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	@Override
	public String getExtendedDebugInfo() {
		return "choice: " + (model == null ? "no model!" : "rows:" + model.getRowCount());
	}
}