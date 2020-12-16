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
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.ValidationStatusImpl;

/**
 * Initial Date: 27.12.2006 <br>
 * 
 * @author patrickb
 */
public class SelectboxSelectionImpl extends FormItemImpl implements SingleSelection {

	private String[] values;
	private String[] keys;
	private String[] cssClasses;
	private String original = null;
	private boolean originalSelect = false;
	private int selectedIndex = -1;
	private boolean allowNoSelection = false;

	private final SelectboxComponent component;
	private final Translator noSelectionTranslator;

	/**
	 * set your layout
	 * @param id A fix identifier for state-less behavior, must be unique or null
	 * @param name
	 * @param locale
	 */
	public SelectboxSelectionImpl(String id, String name, Locale locale) {
		this(id, name, Util.createPackageTranslator(SelectboxSelectionImpl.class, locale));
	}
	
	SelectboxSelectionImpl(String id, String name, Translator noSelectonTranslator) {
		super(id, name, false);
		
		this.noSelectionTranslator = noSelectonTranslator;
		
		String ssscId = getFormItemId() == null ? null : getFormItemId() + "_SELBOX";
		component = new SelectboxComponent(ssscId , getName() + "_SELBOX", translator, this);
	}

	String[] getValues() {
		return values;
	}

	@Override
	public String[] getKeys() {
		return keys;
	}

	@Override
	public boolean containsKey(String value) {
		if(keys != null && keys.length > 0) {
			for(String key:keys) {
				if((key == null && value == null) || (key != null && key.equals(value))) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void setDomReplacementWrapperRequired(boolean required) {
		component.setDomReplacementWrapperRequired(required);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.SingleSelectionContainer#getSelected()
	 */
	@Override
	public int getSelected() {
		return selectedIndex - getNoValueOffset();
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.SingleSelectionContainer#getSelectedKey()
	 */
	@Override
	public String getSelectedKey() {
		if (!isOneSelected()) throw new AssertException("no key selected");
		return keys[selectedIndex];
	}

	@Override
	public String getSelectedValue() {
		if(selectedIndex >= getNoValueOffset() && selectedIndex < values.length + getNoValueOffset()) {
			return values[selectedIndex];
		}
		return null;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.SingleSelectionContainer#isOneSelected()
	 */
	@Override
	public boolean isOneSelected() {
		return getSelected() != -1;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.SingleSelection#setKeysAndValues(String[], String[], String[])
	 */
	@Override
	public void setKeysAndValues(String[] keys, String[] values, String[] cssClasses) {
		if (keys.length != values.length) {
			throw new AssertException("Key and value length do not match");
		}
		this.keys = keys;
		this.values = values;
		this.cssClasses = cssClasses;
		// reset values
		this.selectedIndex = -1;
		this.original = null;
		this.originalSelect = false;
		if (isAllowNoSelection()) {
			addNoSelectionEntry();
		}
		component.setOptionsAndValues(this.keys, this.values, this.cssClasses);
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.SelectionContainer#getCurriculumKey(int)
	 */
	@Override
	public String getKey(int which) {
		return keys[which + getNoValueOffset()];
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.SelectionContainer#getSize()
	 */
	@Override
	public int getSize() {
		return keys.length - getNoValueOffset();
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.SelectionContainer#getValue(int)
	 */
	@Override
	public String getValue(int which) {
		return values[which + getNoValueOffset()];
	}

	@Override
	public boolean isEscapeHtml() {
		return component.isEscapeHtml();
	}

	@Override
	public void setEscapeHtml(boolean escapeHtml) {
		component.setEscapeHtml(escapeHtml);
	}

	@Override
	public boolean isAllowNoSelection() {
		return allowNoSelection;
	}

	@Override
	public void setAllowNoSelection(boolean allowNoSelection) {
		boolean changedToTrue = !this.allowNoSelection && allowNoSelection;
		boolean changedToFalse = this.allowNoSelection && !allowNoSelection;
		if (changedToFalse) {
			removeNoSelectionEntry();
		}
		this.allowNoSelection = allowNoSelection;
		if (changedToTrue) {
			addNoSelectionEntry();
		}
		component.setOptionsAndValues(this.keys, this.values, this.cssClasses);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.SelectionContainer#isSelected(int)
	 */
	@Override
	public boolean isSelected(int which) {
		return which == getSelected();
	}

	/**
	 * 
	 * @see org.olat.core.gui.components.form.flexible.elements.SelectionElement#select(java.lang.String, boolean)
	 */
	@Override
	public void select(String key, boolean select) {
		boolean found = false;
		for (int i = 0; i < keys.length; i++) {
			if (key.equals(keys[i])) {
				selectedIndex = i;
				found = true;
				break;
			}
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
	 * These options is not supported by this element.
	 * 
	 */
	@Override
	public void setWidthInPercent(int width, boolean trailingSpace) {
		//
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
		//
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		if(!isEnabled()){
			return;
		}
		// which one was selected?
		// selection change?
		// mark corresponding comps as dirty
		
		String[] reqVals = getRootForm().getRequestParameterValues(getName()+"_SELBOX");
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
		//reset to original value
		if (original != null) {
			select(original, originalSelect);
		}
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

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormBaseComponentIdProvider#getFormDispatchId()
	 */
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
	
	@Override
	protected SelectboxComponent getFormItemComponent() {
		return component;
	}

	private int getNoValueOffset() {
		return isAllowNoSelection()? 1: 0;
	}
	
	private void addNoSelectionEntry() {
		if (keys != null && values != null) {
			String[] movedKeys = new String[keys.length + 1];
			String[] movedValues = new String[values.length + 1];
			movedKeys[0] = SingleSelection.NO_SELECTION_KEY;
			movedValues[0] = noSelectionTranslator.translate("selection.no.value");
			for (int i=keys.length; i-->0;) {
				movedKeys[i + 1] = keys[i];
				movedValues[i + 1] = values[i];
			}
			keys = movedKeys;
			values = movedValues;
			selectedIndex = selectedIndex + getNoValueOffset();
		}
		if (cssClasses != null && cssClasses.length > 0) {
			String[] movedCssClasses = new String[cssClasses.length + 1];
			for (int i=cssClasses.length; i-->0;) {
				movedCssClasses[i + 1] = cssClasses[i];
			}
			cssClasses = movedCssClasses;
		}
	}
	
	private void removeNoSelectionEntry() {
		if (keys != null && values != null) {
			String[] movedKeys = new String[keys.length - 1];
			String[] movedValues = new String[values.length - 1];
			for (int i=keys.length; i-->1;) {
				movedKeys[i - 1] = keys[i];
				movedValues[i -1] = values[i];
			}
			keys = movedKeys;
			values = movedValues;
			selectedIndex = selectedIndex - getNoValueOffset();
		}
		if (cssClasses != null && cssClasses.length > 0) {
			String[] movedCssClasses = new String[cssClasses.length - 1];
			for (int i=cssClasses.length; i-->1;) {
				movedCssClasses[i - 1] = cssClasses[i];
			}
			cssClasses = movedCssClasses;
		};
	}
	
}