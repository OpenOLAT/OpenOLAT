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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ValidationStatus;

/**
 * Description:<br>
 * TODO: patrickb Class Description for MultipleSelectionElementImpl
 * <P>
 * Initial Date: 04.01.2007 <br>
 * 
 * @author patrickb
 */
public class MultipleSelectionElementImpl extends FormItemImpl implements MultipleSelectionElement {
	private static final OLog log = Tracing.createLoggerFor(MultipleSelectionElementImpl.class);

	protected String[] keys;
	protected String[] values;
	private String[] cssClasses;
	private String[] iconLeftCSS;
	private Collection<String> selected;
	
	private final Layout layout;
	private final int columns;
	protected MultipleSelectionComponent component;
	private String[] original = null;
	private boolean originalIsDefined = false;
	private boolean escapeHtml = true;
	private boolean domReplacementWrapperRequired = true;

	public MultipleSelectionElementImpl(String name) {
		this(name, Layout.horizontal, 1);
	}
	
	public MultipleSelectionElementImpl(String name, Layout layout) {
		this(name, layout, 1);
	}

	public MultipleSelectionElementImpl(String name, Layout layout, int columns) {
		super(name);
		selected = new HashSet<String>();
		this.layout = layout;
		this.columns = columns;
	}
	
	@Override
	public String getForId() {
		return null;
	}
	
	@Override
	public void setDomReplacementWrapperRequired(boolean required) {
		domReplacementWrapperRequired = required;
		if(component != null) {
			component.setDomReplacementWrapperRequired(required);
		}
	}

	public Layout getLayout() {
		return layout;
	}
	
	public int getColumns() {
		return columns;
	}
	
	public boolean isEscapeHtml() {
		return escapeHtml;
	}
	
	@Override
	public void setEscapeHtml(boolean escapeHtml) {
		this.escapeHtml = escapeHtml;
	}

	public Collection<String> getSelectedKeys() {
		return selected;
	}

	@Override
	public void setKeysAndValues(String[] keys, String[] values) {
		setKeysAndValues(keys, values, null, null);
	}

	@Override
	public void setKeysAndValues(String[] keys, String[] values, String[] cssClasses, String[] iconLeftCSS) {
		this.keys = keys;
		this.values = values;
		this.cssClasses = cssClasses;
		this.iconLeftCSS = iconLeftCSS;
		initSelectionElements();
	}
	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement#isAtLeastSelected(int,
	 *      java.lang.String)
	 */
	public boolean isAtLeastSelected(int howmany) {
		boolean ok = selected.size() >= howmany;
		return ok;
	}

	public String getKey(int which) {
		if(which >= 0 && which < keys.length) {
			return keys[which];
		}
		return null;
	}

	public int getSize() {
		return keys.length;
	}

	public String getValue(int which) {
		return values[which];
	}

	/**
	 * selection element supports multiple select
	 * 
	 * @see org.olat.core.gui.components.form.flexible.elements.SelectionElement#isMultiselect()
	 */
	public boolean isMultiselect() {
		return true;
	}

	public boolean isSelected(int which) {
		String key = getKey(which);
		return selected.contains(key);
	}

	public void select(String key, boolean select) {
		if (select) {
			selected.add(key);
		} else {
			selected.remove(key);
		}
		if(!originalIsDefined) {
			originalIsDefined = true;
			if(selected != null && selected.size() > 0){
				original = new String[selected.size()];
				original = selected.toArray(original);
			}else{
				original = null;
			}
		}
		// set container dirty to render new selection
		component.setDirty(true);
	}

	/**
	 * array of values which are selected
	 * 
	 * @param values
	 */
	public void setSelectedValues(String[] values) {
		selected = new HashSet<String>(3);
		//remember original values
		if(!originalIsDefined){
			originalIsDefined = true;
			original = (values != null ? values.clone() : null);
		}
		
		if (values == null) return; // no selection made (no checkbox activated) ->
		// selection is empty
		// H: values != null
		for (int i = 0; i < values.length; i++) {
			String key = values[i];
			// prevent introducing fake keys
			int ksi = keys.length;
			boolean foundKey = false;
			int j = 0;
			while (!foundKey && j < ksi) {
				String eKey = keys[j];
				if (eKey.equals(key)) foundKey = true;
				j++;
			}
			if (!foundKey){
				log.warn("submitted key '" + key + "' was not found in the keys of formelement named "
					+ this.getName() + " , keys=" + Arrays.asList(keys));
			}else{
				selected.add(key);	
			}
		}
		// set container dirty to render new values
		component.setDirty(true);
	}

	@Override
	protected void rootFormAvailable() {
		// create components and add them to the velocity container
		initSelectionElements();
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		if(!isEnabled()){
			//if element is visible as disabled it would be resetted
			return;
		}
		// which one was selected?
		// selection change?
		// mark corresponding comps as dirty
		String[] reqVals = getRootForm().getRequestParameterValues(getName());
		if (reqVals == null) {
			// selection box?
			reqVals = getRootForm().getRequestParameterValues(getName() + "_SELBOX");
		}
		//
		selected = new HashSet<String>();
		if (reqVals != null) {
			for (int i = 0; i < reqVals.length; i++) {
				selected.add(reqVals[i]);
			}
		}
	}

	@Override
	public void validate(List<ValidationStatus> validationResults) {
		// no constraint to be checked	
	}

	@Override
	public void reset() {
		setSelectedValues(original);
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
	
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement#setEnabled(java.lang.String, boolean)
	 */
	@Override
	public void setEnabled(String key, boolean isEnabled) {
		for(CheckboxElement check : component.getCheckComponents()) {
			if(check.getKey().equals(key)) {
				check.setEnabled(isEnabled);
			}
		}
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement#setEnabled(java.util.Set, boolean)
	 */
	@Override
	public void setEnabled(Set<String> keys, boolean isEnabled) {
		for (String key : keys) {
			setEnabled(key, isEnabled);
		}
	}
	
	@Override
	public void setVisible(boolean isVisible) {
		super.setVisible(isVisible);
		// set container dirty to render new values
		component.setVisible(isVisible);
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement#setVisible(java.lang.String, boolean)
	 */
	@Override
	public void setVisible(String key, boolean isVisible) {
		for(CheckboxElement check : component.getCheckComponents()) {
			if(check.getKey().equals(key)) {
				check.setVisible(isVisible);
			}
		}
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement#setVisible(java.util.Set, boolean)
	 */
	@Override
	public void setVisible(Set<String> keys, boolean isEnabled) {
		for (String key : keys) {
			setEnabled(key, isEnabled);
		}
	}
	
	/**
	 * Returns the keys of the checkboxes in this {@link MultipleSelectionElement}.
	 * @return Keys of the checkboxes
	 */
	public Set<String> getKeys() {
		return new HashSet<String>(Arrays.asList(keys));
	}
	
	protected void initSelectionElements() {
		boolean createValues = (values == null) || (values.length == 0);
		if (createValues) {
			values = new String[keys.length];
			for (int i = 0; i < keys.length; i++) {
				values[i] = translator.translate(keys[i]);
			}
		}

		// keys,values initialized
		// create and add radio elements
		CheckboxElement[] ssecs = new CheckboxElement[keys.length];
		for (int i = 0; i < keys.length; i++) {
			String checkName = getName() + "_" + keys[i];
			ssecs[i] = new CheckboxElement(checkName, this, i,
					(cssClasses == null ? null : cssClasses[i]),
					(iconLeftCSS == null ? null : iconLeftCSS[i]));
			ssecs[i].setEnabled(isEnabled());
		}
		
		// create and add selectbox element
		if(component == null) {
			String ssscId = getFormItemId() == null ? null : getFormItemId() + "_SELBOX";
			component = new MultipleSelectionComponent(ssscId, this);
			component.setDomReplacementWrapperRequired(domReplacementWrapperRequired);
			component.setCheckComponents(ssecs);
		} else {
			component.setCheckComponents(ssecs);
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormBaseComponentIdProvider#getFormDispatchId()
	 */
	@Override
	public String getFormDispatchId() {
		return DISPPREFIX.concat(getComponent().getDispatchID());
	}

	@Override
	protected MultipleSelectionComponent getFormItemComponent() {
		return component;
	}

	/**
	 * Select all selection elements.
	 */
	public void selectAll() {
		selected = new HashSet<String>(3);
		for (int i = 0; i < keys.length; i++) {
			selected.add(keys[i]);
		}
		// set container dirty to render new selection
		component.setDirty(true);
	}

	/**
	 * Uncheck all selection elements.
	 */
	public void uncheckAll() {
		selected = new HashSet<String>(3); 
		// set container dirty to render new selection
		component.setDirty(true);
	}
}