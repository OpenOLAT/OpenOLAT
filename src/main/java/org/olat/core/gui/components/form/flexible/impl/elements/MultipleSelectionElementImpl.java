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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.GUIInterna;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormLayouter;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
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
	protected static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(MultipleSelectionElementImpl.class);
 
	private final static String HORIZONTAL_DEFAULT_CHCKBX = VELOCITY_ROOT + "/sel_elems_horizontal.html";
	private final static String VERTICAL_CHCKBX = VELOCITY_ROOT + "/sel_elems_vertical.html";
	//FIXME:pb Jan 2008 Flexi Form refactoring -> move selbox rendering out
	private final static String SELECTBOX = VELOCITY_ROOT + "/sel_elems_selbox.html";

	protected String[] keys;
	protected String[] values;
	private String[] cssClasses;
	private Set<String> selected;
	
	protected FormLayouter formLayoutContainer;
	private String[] original = null;
	private boolean originalIsDefined = false;
	private boolean escapeHtml = true;

	public MultipleSelectionElementImpl(String name) {
		this(name, createHorizontalLayout(name));
	}

	public MultipleSelectionElementImpl(String name, FormLayouter formLayout) {
		super(name);
		selected = new HashSet<String>();
		formLayoutContainer = formLayout;
	}
	
	@Override
	public void setEscapeHtml(boolean escapeHtml) {
		Component sssc = formLayoutContainer.getComponent(getName() + "_SELBOX");
		if(sssc instanceof SelectboxComponent) {
			((SelectboxComponent)sssc).setEscapeHtml(escapeHtml);
		}
		
		if(keys != null) {
			for (String key:keys) {
				Component checkCmp = formLayoutContainer.getComponent(getName()+"_"+key);
				if(checkCmp instanceof CheckboxElementComponent) {
					((CheckboxElementComponent)checkCmp).setEscapeHtml(escapeHtml);
				}
			}
		}
		
		this.escapeHtml = escapeHtml;
	}

	public Set<String> getSelectedKeys() {
		return selected;
	}

	public void setKeysAndValues(String[] keys, String[] values, String[] cssClasses){
		this.keys = keys;
		this.values = values;
		this.cssClasses = cssClasses;
		//
		// remove all elements
		// add new elements
		// 
		// set isEnabled for all created components
		Component sssc = formLayoutContainer.getComponent(getName()+"_SELBOX");
		formLayoutContainer.remove(sssc);
		
		for (int i = 0; i < keys.length; i++) {
			Component elm = formLayoutContainer.getComponent(getName()+"_"+keys[i]);
			if(elm!=null){
				//can be null if setKeysAndValues is called as "model" update
				formLayoutContainer.remove(elm);
			}
		}
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
		return keys[which];
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
		if(!originalIsDefined){
			originalIsDefined = true;
			if(selected != null && selected.size() > 0){
				original = new String[selected.size()];
				original = selected.toArray(original);
			}else{
				original = null;
			}
		}
		// set container dirty to render new selection
		this.formLayoutContainer.setDirty(true);
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
		this.formLayoutContainer.setDirty(true);
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
		//set isEnabled for all created components
		Component sssc = formLayoutContainer.getComponent(getName()+"_SELBOX");
		sssc.setEnabled(isEnabled);
		for (int i = 0; i < keys.length; i++) {
			Component elm = formLayoutContainer.getComponent(getName()+"_"+keys[i]);
			elm.setEnabled(isEnabled);
		}
		// set container dirty to render new values
		formLayoutContainer.setDirty(true);
	}
	
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement#setEnabled(java.lang.String, boolean)
	 */
	public void setEnabled(String key, boolean isEnabled) {
		Component checkbox = formLayoutContainer.getComponent(getName() + "_" + key);
		if (checkbox != null) {
			checkbox.setEnabled(isEnabled);
		}
	}
	
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement#setEnabled(java.util.Set, boolean)
	 */
	public void setEnabled(Set<String> keys, boolean isEnabled) {
		for (String key : keys) {
			setEnabled(key, isEnabled);
		}
	}
	
	@Override
	public void setVisible(boolean isVisible) {
		super.setVisible(isVisible);
		//set isEnabled for all created components
		Component sssc = formLayoutContainer.getComponent(getName()+"_SELBOX");
		sssc.setVisible(isVisible);
		for (int i = 0; i < keys.length; i++) {
			Component elm = formLayoutContainer.getComponent(getName()+"_"+keys[i]);
			elm.setVisible(isVisible);
		}
		// set container dirty to render new values
		formLayoutContainer.setDirty(true);
	}
	
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement#setVisible(java.lang.String, boolean)
	 */
	public void setVisible(String key, boolean isVisible) {
		Component checkbox = formLayoutContainer.getComponent(getName() + "_" + key);
		if (checkbox != null) {
			checkbox.setVisible(isVisible);
		}
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement#setVisible(java.util.Set, boolean)
	 */
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
		String[] items = new String[keys.length];
		for (int i = 0; i < keys.length; i++) {
			CheckboxElementComponent ssec = new CheckboxElementComponent(getName()+"_"+keys[i], translator, this, i, (cssClasses == null ? null : cssClasses[i]));
			formLayoutContainer.put(getName()+"_"+keys[i], ssec);
			items[i] = getName()+"_"+keys[i];
			ssec.setEnabled(isEnabled());
			ssec.setEscapeHtml(escapeHtml);
			
			if (GUIInterna.isLoadPerformanceMode()) {
				if (getRootForm()!=null) {
					getRootForm().getReplayableDispatchID(ssec);
				}
			}
		}
		// create and add selectbox element
		String ssscId = getFormItemId() == null ? null : getFormItemId() + "_SELBOX";
		SelectboxComponent sssc = new SelectboxComponent(ssscId, getName() + "_SELBOX", translator, this, keys, values, cssClasses);
		sssc.setEscapeHtml(escapeHtml);

		formLayoutContainer.put(getName() + "_SELBOX", sssc);
		formLayoutContainer.contextPut("selectbox", getName() + "_SELBOX");
		// add items and size (used for column calculation)
		formLayoutContainer.contextPut("items", items);
		formLayoutContainer.contextPut("size", items.length);

		// set container dirty to render new config
		formLayoutContainer.setDirty(true);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormBaseComponentIdProvider#getFormDispatchId()
	 */
	@Override
	public String getFormDispatchId() {
		if(GUIInterna.isLoadPerformanceMode()){
			return DISPPREFIX+getRootForm().getReplayableDispatchID(formLayoutContainer.getComponent());
		}else{
			return DISPPREFIX+formLayoutContainer.getComponent().getDispatchID();
		}
	}
	

	/**
	 * as selectbox
	 * @param name
	 * @return
	 */
	public static FormLayouter createSelectboxLayouter(String name) {
		return FormLayoutContainer.createCustomFormLayout(name+"SELECTBOX", null, SELECTBOX);
	}
	

	/**
	 * radio buttons horizontal
	 * @param name
	 * @return
	 */
	public static FormLayouter createHorizontalLayout(String name) {
		return FormLayoutContainer.createCustomFormLayout(name+"HORIZONTAL_DEFAULT_CHCKBX", null, HORIZONTAL_DEFAULT_CHCKBX);
	}

	/**
	 * radio buttons vertical
	 * @param name
	 * @return
	 */
	public static FormLayouter createVerticalLayout(String name, int columns) {
		FormLayoutContainer layout =  FormLayoutContainer.createCustomFormLayout(name+"VERTICAL_CHCKBX", null, VERTICAL_CHCKBX);
		if (columns < 1 || columns > 2) 
			throw new AssertException("Currently only 1 or 2 columns are implemented");
		layout.contextPut("columns", columns);
		return layout;
	}

	@Override
	protected Component getFormItemComponent() {
		/**
		 * FIXME:pb dirty hack or not? to allow singleselection subcomponents being
		 * added to surrounding formlayouters -> e.g. language chooser selectbox
		 * we have to return the formLayoutContainer.Component if it was not a 
		 * "custom" formlayouter. -> detection via ..endsWith() bad not beautyful
		 * but functional so far.
		 */
		String tmp = formLayoutContainer.getComponent().getComponentName();
		boolean isDefault = tmp.endsWith("VERTICAL_CHCKBX") || tmp.endsWith("HORIZONTAL_DEFAULT_CHCKBX") || tmp.endsWith("SELECTBOX");
		if(isDefault){
			return formLayoutContainer.getComponent();
		}else{
			//return a dummy, not to break rendering process with a null component.
			return createSelectboxLayouter("dummy").getComponent();
		}
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
		this.formLayoutContainer.setDirty(true);
	}

	/**
	 * Uncheck all selection elements.
	 */
	public void uncheckAll() {
		selected = new HashSet<String>(3); 
		// set container dirty to render new selection
		this.formLayoutContainer.setDirty(true);
	}

}
