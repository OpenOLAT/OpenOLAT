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

import org.olat.core.gui.GUIInterna;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormLayouter;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.ValidationStatusImpl;

/**
 * Description:<br>
 * TODO: patrickb Class Description for SingleSelectionContainerImpl
 * <P>
 * Initial Date: 27.12.2006 <br>
 * 
 * @author patrickb
 */
public class SingleSelectionImpl extends FormItemImpl implements SingleSelection {

	//
	private static final String PACKAGE = Util.getPackageName(SingleSelectionImpl.class);
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(PACKAGE);

	private final static String HORIZONTAL_DEFAULT_RADIO = VELOCITY_ROOT + "/sel_elems_horizontal.html";
	private final static String VERTICAL_RADIO = VELOCITY_ROOT + "/sel_elems_vertical.html";
	private final static String SELECTBOX = VELOCITY_ROOT + "/sel_elems_selbox.html";

	protected String[] values;
	protected String[] keys;
	protected String[] cssClasses;
	private String original = null;
	private boolean originalSelect = false;
	private int selectedIndex = -1;

	private FormLayouter formLayoutContainer;
	
	/**
	 * @param name
	 */
	public SingleSelectionImpl(String name) {
		this(null, name, createHorizontalLayout(null, name));
	}


	/**
	 * set your layout
	 * @param id A fix identifier for state-less behavior, must be unique or null
	 * @param name
	 * @param presentation
	 */
	public SingleSelectionImpl(String id, String name, FormLayouter formLayout) {
		super(id, name, false);
		formLayoutContainer = formLayout;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.SingleSelectionContainer#getSelected()
	 */
	public int getSelected() {
		return selectedIndex;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.SingleSelectionContainer#getSelectedKey()
	 */
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


	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.SingleSelectionContainer#isOneSelected()
	 */
	public boolean isOneSelected() {
		return selectedIndex != -1;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.SingleSelection#setKeysAndValues(String[], String[], String[])
	 */
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
		// initialize everything
		initSelectionElements();
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.SelectionContainer#getKey(int)
	 */
	public String getKey(int which) {
		return keys[which];
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.SelectionContainer#getSize()
	 */
	public int getSize() {
		return keys.length;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.SelectionContainer#getValue(int)
	 */
	public String getValue(int which) {
		return values[which];
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.SelectionContainer#isSelected(int)
	 */
	public boolean isSelected(int which) {
		return which == selectedIndex;
	}

	/**
	 * 
	 * @see org.olat.core.gui.components.form.flexible.elements.SelectionElement#select(java.lang.String, boolean)
	 */
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
		if (!found) { throw new AssertException("could not set <" + key + "> to " + select + " because key was not found!"); }
	}

	/**
	 * we are single selection, hence return always false here
	 * @see org.olat.core.gui.components.form.flexible.elements.SelectionElement#isMultiselect()
	 */	
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
		if(reqVals == null){
			//selection box?
			reqVals = getRootForm().getRequestParameterValues(getName()+"_SELBOX");
		}
		// -> single selection reqVals.lenght == 0 | 1
		if (reqVals != null && reqVals.length == 1) {
			for (int i = 0; i < keys.length; i++) {
				if(reqVals[0].equals(keys[i])){
					select(keys[i], true);
					//System.out.println("SingleSelect <"+getName()+"> "+keys[i]+":"+values[i]);
				}//endif
			}//endfor
		}//endif
	}//endmethod
	

	@Override
	public void validate(List<ValidationStatus> validationResults) {
		if ( ! isOneSelected()) {
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
		//set isEnabled for all created components
		Component sssc = formLayoutContainer.getComponent(getName()+"_SELBOX");
		sssc.setEnabled(isEnabled);
		for (int i = 0; i < keys.length; i++) {
			Component elm = formLayoutContainer.getComponent(getName()+"_"+keys[i]);
			elm.setEnabled(isEnabled);
		}
	}
	@Override
	public void setVisible(boolean isVisible) {
		super.setVisible(isVisible);
		//set isEnabled for all created components
		Component sssc = formLayoutContainer.getComponent(getName()+"_SELBOX");
		if(sssc != null){
			sssc.setVisible(isVisible);
		}
		for (int i = 0; i < keys.length; i++) {
			Component elm = formLayoutContainer.getComponent(getName()+"_"+keys[i]);
			if(elm != null){
				elm.setVisible(isVisible);
			}
		}
	};

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormBaseComponentIdProvider#getFormDispatchId()
	 */
	public String getFormDispatchId() {
		/**
		 * FIXME:pb dirty hack or not to allow singleselection subcomponents being
		 * added to surrounding formlayouters -> e.g. language chooser selectbox
		 * ..................................................................
		 * you would expect here ...+getComponent()+... for generating the form 
		 * dispatch id, but it must always be the formLayoutContainer -> see getComponent() to
		 * understand why this is not always the case.
		 */
		if(GUIInterna.isLoadPerformanceMode()){
			return DISPPREFIX+getRootForm().getReplayableDispatchID(formLayoutContainer.getComponent());
		}else{
			return DISPPREFIX+formLayoutContainer.getComponent().getDispatchID();
		}
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
		String[] items = new String[keys.length];
		for (int i = 0; i < keys.length; i++) {
			RadioElementComponent ssec = new RadioElementComponent(getName()+"_"+keys[i], translator, this, i);
			formLayoutContainer.put(getName()+"_"+keys[i], ssec);
			items[i]=getName()+"_"+keys[i];
			//formComponentsNames.add(keys[i]);
			//formComponents.put(keys[i], ssec);
			if (GUIInterna.isLoadPerformanceMode()) {
				getRootForm().getReplayableDispatchID(ssec);
			}
		}
		//create and add selectbox element
		String ssscId = getFormItemId() == null ? null : getFormItemId() + "_SELBOX";
		SelectboxComponent sssc = new SelectboxComponent(ssscId , getName() + "_SELBOX", translator, this, keys, values, cssClasses);
		formLayoutContainer.put(getName()+"_SELBOX", sssc);
		formLayoutContainer.contextPut("selectbox", getName()+"_SELBOX");
		//formComponentsNames.add(getName()+"_SELBOX");
		//formComponents.put(getName()+"_SELBOX", sssc);
		//
		formLayoutContainer.contextPut("items", items);
	}

	/**
	 * as selectbox
	 * @param id A fix identification for state-less behavior, must be unique
	 * @param name
	 * @return
	 */
	public static FormLayoutContainer createSelectboxLayouter(String id, String name) {
		String contId = (id == null ? null : id + "_SELECTBOX_CONT");
		return FormLayoutContainer.createCustomFormLayout(contId, name+"SELECTBOX", null, SELECTBOX);
	}

	/**
	 * radio buttons horizontal
	 * @param id A fix identification for state-less behavior, must be unique
	 * @param name
	 * @return
	 */
	public static FormLayoutContainer createHorizontalLayout(String id, String name) {
		String contId = (id == null ? null : id + "_HORIZONTAL_DEFAULT_RADIO_CONT");
		return FormLayoutContainer.createCustomFormLayout(contId, name+"HORIZONTAL_DEFAULT_RADIO", null, HORIZONTAL_DEFAULT_RADIO);
	}

	/**
	 * radio buttons vertical
	 * @param id A fix identification for state-less behavior, must be unique
	 * @param name
	 * @return
	 */
	public static FormLayoutContainer createVerticalLayout(String id, String name) {
		String contId = (id == null ? null : id + "_VERTICAL_RADIO_CONT");
		return FormLayoutContainer.createCustomFormLayout(contId, name+"VERTICAL_RADIO", null, VERTICAL_RADIO);
	}

	protected Component getFormItemComponent() {
		/**
		 * FIXME:pb dirty hack or not? to allow singleselection subcomponents being
		 * added to surrounding formlayouters -> e.g. language chooser selectbox
		 * we have to return the formLayoutContainer.Component if it was not a 
		 * "custom" formlayouter. -> detection via ..endsWith() bad not beautyful
		 * but functional so far.
		 */
		String tmp = formLayoutContainer.getComponent().getComponentName();
		boolean isDefault = tmp.endsWith("VERTICAL_RADIO") || tmp.endsWith("HORIZONTAL_DEFAULT_RADIO") || tmp.endsWith("SELECTBOX");
		if(isDefault){
			return formLayoutContainer.getComponent();
		}else{
			//return a dummy, not to break rendering process with a null component.
			return createSelectboxLayouter(null, "dummy").getComponent();
		}
	}


	
}
