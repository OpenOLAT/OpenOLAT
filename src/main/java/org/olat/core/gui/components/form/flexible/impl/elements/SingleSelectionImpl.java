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
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.SingleSelectionComponent.RadioElementComponent;
import org.olat.core.logging.AssertException;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.ValidationStatusImpl;

/**
 * Initial Date: 27.12.2006 <br>
 * 
 * @author patrickb
 */
public class SingleSelectionImpl extends FormItemImpl implements SingleSelection {

	private String[] values;
	private String[] keys;
	private String original = null;
	private boolean originalSelect = false;
	private int selectedIndex = -1;
	private boolean allowNoSelection = false;

	private final Layout layout;
	private final SingleSelectionComponent component;
	
	public SingleSelectionImpl(String name) {
		this(null, name, Layout.horizontal);
	}

	/**
	 * set your layout
	 * @param id A fix identifier for state-less behavior, must be unique or null
	 * @param name
	 * @param presentation
	 */
	public SingleSelectionImpl(String id, String name, Layout layout) {
		super(id, name, false);
		this.layout = layout;
		component = new SingleSelectionComponent(id, this);
	}

	@Override
	public void setDomReplacementWrapperRequired(boolean required) {
		component.setDomReplacementWrapperRequired(required);
	}
	
	@Override
	public String getForId() {
		return null;//every radio box has its own label
	}

	@Override
	public int getSelected() {
		return selectedIndex;
	}
	
	public Layout getLayout() {
		return layout;
	}

	@Override
	public String getSelectedKey() {
		if (!isOneSelected()) throw new AssertException("no key selected");
		return keys[selectedIndex];
	}

	@Override
	public String getSelectedValue() {
		if(selectedIndex >= 0 && selectedIndex < values.length) {
			return values[selectedIndex];
		}
		return null;
	}

	@Override
	public boolean isOneSelected() {
		return selectedIndex != -1;
	}

	@Override
	public void setKeysAndValues(String[] keys, String[] values, String[] cssClasses) {
		if (keys.length != values.length) {
			throw new AssertException("Key and value length do not match");
		}
		this.keys = keys;
		this.values = values;
		// reset values
		this.selectedIndex = -1;
		this.original = null;
		this.originalSelect = false;
		// initialize everything
		initSelectionElements();
	}
	
	@Override
	public String[] getKeys() {
		return keys;
	}
	
	@Override
	public String getKey(int which) {
		return keys[which];
	}

	@Override
	public int getSize() {
		return keys.length;
	}

	@Override
	public String getValue(int which) {
		return values[which];
	}

	/**
	 * Set a fix width to the enclosing div/label of the radio elements. Spaced
	 * had a space after the end div/label.
	 * 
	 * @param widthInPercent The width (example: 9)
	 * @param spaced If true had a trailing space
	 */
	@Override
	public void setWidthInPercent(int width, boolean trailingSpace) {
		component.setWidthInPercent(width, trailingSpace);
	}

	@Override
	public boolean isAllowNoSelection() {
		return allowNoSelection;
	}

	@Override
	public void setAllowNoSelection(boolean allowNoSelection) {
		this.allowNoSelection = allowNoSelection;
	}

	@Override
	public boolean isEscapeHtml() {
		return component.isEscapeHtml();
	}

	@Override
	public void setEscapeHtml(boolean escape) {
		component.setEscapeHtml(escape);
	}

	@Override
	public boolean isSelected(int which) {
		return which == selectedIndex;
	}

	@Override
	public void select(String key, boolean select) {
		boolean found = false;
		if (select) {
			for (int i = 0; i < keys.length; i++) {
				if (key.equals(keys[i])) {
					selectedIndex = i;
					found = true;
					break;
				}
			}
		} else {
			if (selectedIndex >= 0 && key.equals(keys[selectedIndex])) {
				selectedIndex = -1;
			}
			found = true;
		}
		
		//remember original selection
		if(original == null){
			original = key;
			originalSelect = select;
		}
		if (!found) {
			throw new AssertException("could not set <" + key + "> to " + select + " because key was not found!");
		}
	}

	/**
	 * we are single selection, hence return always false here
	 * @see org.olat.core.gui.components.form.flexible.elements.SelectionElement#isMultiselect()
	 */
	@Override
	public boolean isMultiselect() {
		return false;
	}

	@Override
	protected void rootFormAvailable() {
		// create components and add them to the velocity container
		initSelectionElements();
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		if(!isEnabled()){
			return;
		}
		// which one was selected?
		// selection change?
		// mark corresponding comps as dirty
		
		String[] reqVals = getRootForm().getRequestParameterValues(getName());
		// -> single selection reqVals.lenght == 0 | 1
		if (reqVals != null && reqVals.length == 1) {
			for (int i = 0; i < keys.length; i++) {
				if(reqVals[0].equals(keys[i])){
					select(keys[i], true);
				}
			}
		}
	}
	
	@Override
	public void validate(List<ValidationStatus> validationResults) {
		if (!allowNoSelection && isVisible() && !isOneSelected()) {
			validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
			return;
		}
		clearError();
	}

	@Override
	public void reset() {
		//reset to originial value
		select(original, originalSelect);
		clearError();
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormItemImpl#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean isEnabled) {
		super.setEnabled(isEnabled);
		component.setEnabled(isEnabled);
	}
	
	@Override
	public void setVisible(boolean isVisible) {
		super.setVisible(isVisible);
		component.setVisible(isVisible);
	}

	@Override
	public String getFormDispatchId() {
		/**
		 * FIXME:pb dirty hack or not to allow singleselection subcomponents being
		 * added to surrounding formlayouters -> e.g. language chooser selectbox
		 * ..................................................................
		 * you would expect here ...+getComponent()+... for generating the form 
		 * dispatch id, but it must always be the formLayoutContainer -> see getComponent() to
		 * understand why this is not always the case.
		 */
		return DISPPREFIX + component.getDispatchID();
	}

	private void initSelectionElements() {
		boolean createValues = (values == null) || (values.length == 0);
		if (createValues) {
			values = new String[keys.length];
			for (int i = 0; i < keys.length; i++) {
				values[i] = translator.translate(keys[i]);
			}
		}
		// keys,values initialized
		// create and add radio elements
		RadioElementComponent[] radios = new RadioElementComponent[keys.length];
		for (int i = 0; i < keys.length; i++) {
			radios[i] = new RadioElementComponent(this, i);
		}
		component.setRadioComponents(radios);
	}

	@Override
	protected SingleSelectionComponent getFormItemComponent() {
		return component;
	}
}