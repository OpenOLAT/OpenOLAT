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
package org.olat.core.gui.components.form.flexible.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.context.Context;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormMultipartItem;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.render.velocity.VelocityRenderDecorator;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date: 22.11.2006 <br>
 * 
 * @author patrickb
 */
public class FormLayoutContainer extends FormItemImpl implements FormItemContainer, Disposable {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(FormLayoutContainer.class);
	private static final String LAYOUT_DEFAULT = VELOCITY_ROOT + "/form_default.html";
	private static final String LAYOUT_DEFAULT_6_6 = VELOCITY_ROOT + "/form_default_6_6.html";
	private static final String LAYOUT_DEFAULT_9_3 = VELOCITY_ROOT + "/form_default_9_3.html";
	private static final String LAYOUT_TABLE_CONDENSED = VELOCITY_ROOT + "/form_table_condensed.html";
	private static final String LAYOUT_DEFAULT_2_10 = VELOCITY_ROOT + "/form_default_2_10.html";
	private static final String LAYOUT_HORIZONTAL = VELOCITY_ROOT + "/form_horizontal.html";
	private static final String LAYOUT_VERTICAL = VELOCITY_ROOT + "/form_vertical.html";
	private static final String LAYOUT_BAREBONE = VELOCITY_ROOT + "/form_barebone.html";
	private static final String LAYOUT_BUTTONGROUP = VELOCITY_ROOT + "/form_buttongroup.html";
	private static final String LAYOUT_INPUTGROUP = VELOCITY_ROOT + "/form_inputgroup.html";
	private static final String LAYOUT_PANEL = VELOCITY_ROOT + "/form_panel.html";

	/**
	 * manage the form components of this form container
	 */
	private final VelocityContainer formLayoutContainer;
	/**
	 * formComponents and formComponentNames are managed together, change something here needs a change there.
	 * formComponents contain the FormItem based on their name
	 * formComponentsNames is use in the velocity to render according to the registered name.
	 * The addXXX method adds elements -> 
	 * The register method register an element only -> used for setErrorComponent / setLabelComponent.
	 */
	private final Map<String,FormItem> formComponents;
	private final List<String> formComponentsNames;
	private boolean hasRootForm=false;


	/**
	 * Form layout is provided by caller, access the form item to render inside
	 * the velocity container by
	 * <ul>
	 * <li>name to get the form field</li>
	 * <li>name_LABEL</li>
	 * <li>name_ERROR</li>
	 * <li>name_EXAMPLE</li>
	 * </ul>
	 * You can also access form item information like
	 * <ul>
	 * <li>$f.hasError(name)</li>
	 * <li>$f.hasLabel(name)</li>
	 * <li>$f.hasExample(name)</li>
	 * </ul>
	 * which helps you for layouting the form correct.
	 * 
	 * @param name
	 * @param translator
	 */
	private FormLayoutContainer(String name, Translator formTranslator, String page) {
		this(null, name, formTranslator, page);
		
	}

	private FormLayoutContainer(String id, String name, Translator formTranslator, String page) {
		super(id, name, false);
		formLayoutContainer = new VelocityContainer(id == null ? null : id + "_VC", name, page, formTranslator, null);
		if (page.equals(LAYOUT_DEFAULT) || page.equals(LAYOUT_VERTICAL) || page.equals(LAYOUT_HORIZONTAL)
				|| page.equals(LAYOUT_BUTTONGROUP) || page.equals(LAYOUT_INPUTGROUP) || page.equals(LAYOUT_TABLE_CONDENSED)) {
			// optimize for lower DOM element count - provides its own DOM ID in velocity template
			formLayoutContainer.setDomReplacementWrapperRequired(false);
		}
		translator = formTranslator;
		// add the form decorator for the $f.hasError("ddd") etc.
		formLayoutContainer.contextPut("f", new FormDecorator(this));
		// this container manages the form items, the GUI form item componentes are
		// managed in the associated velocitycontainer
		formComponentsNames = new ArrayList<>(5);
		formLayoutContainer.contextPut("formitemnames", formComponentsNames);
		formComponents = new HashMap<>();
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		// form layouter has no values to store temporary
	}

	@Override
	public void validate(List<ValidationStatus> validationResults) {
		// form layouter is not validating
	}

	@Override
	public void reset() {
		// form layouter can not be resetted
	}
		
	@Override
	protected void rootFormAvailable() {
		// could initialize all formComponents with rootform
		// simpler -> you can not add before adding rootform
		hasRootForm = true;
	}

	@Override
	public void add(FormItem formComp) {
		add(formComp.getName(), formComp);
	}

	@Override
	public void add(String name, FormItem formComp) {
		if(!hasRootForm){
			throw new AssertionError("first ensure that the layout container knows about its rootform!!");
		}
		// set the formtranslator, and parent
		Translator itemTranslator = formComp.getTranslator();
		if (itemTranslator != null && !itemTranslator.equals(translator)
				&& itemTranslator instanceof PackageTranslator) {
			// let the FormItem provide a more specialized translator
			PackageTranslator itemPt = (PackageTranslator) itemTranslator;
			itemTranslator = PackageTranslator.cascadeTranslators(itemPt, translator);
		} else {
			itemTranslator = translator;
		}
		formComp.setTranslator(itemTranslator);
		formComp.setRootForm(getRootForm());
		//
		String formCompName = name;
		// book keeping of FormComponent order
		formComponentsNames.add(formCompName);
		formComponents.put(formCompName, formComp);
		
		/*
		 * add the gui representation
		 */
		formLayoutContainer.put(formCompName, formComp.getComponent());
		formLayoutContainer.put(formCompName + FormItem.ERRORC, formComp.getErrorC());
		formLayoutContainer.put(formCompName + FormItem.EXAMPLEC, formComp.getExampleC());
		formLayoutContainer.put(formCompName + FormItem.LABELC, formComp.getLabelC());

		// Check for multipart data, add upload limit to form
		if (formComp instanceof FormMultipartItem) {
			getRootForm().setMultipartEnabled(true);
		}
	}
	
	@Override
	public void add(String name, Collection<FormItem> foItems){
		
		//remove if already in
		if(formLayoutContainer.getContext().containsKey(name)){
			//remove existing collection
			formLayoutContainer.contextRemove(name);
			//remove all associated form items
			for (FormItem formItem : foItems) {
				remove(formItem);
			}
		}
		
		//make collection accessible with <name> in the container.
		//collection contains then only the names.
		List<String> foItemsCollectionAsNames = new ArrayList<>();
		formLayoutContainer.contextPut(name, foItemsCollectionAsNames);

		//add all items as form items to the container
		for (FormItem formItem : foItems) {
			String foName = formItem.getName();
			add(foName, formItem);
			foItemsCollectionAsNames.add(foName);
		}
	}
	
	@Override
	public void remove(FormItem toBeRemoved) {
		String formCompName = toBeRemoved.getName();
		remove(formCompName, toBeRemoved);
	}
	
	public void replace(FormItem toBeReplaced, FormItem with){
		String formCompName = toBeReplaced.getName();
		int pos = formComponentsNames.indexOf(formCompName);
		formComponentsNames.add(pos, with.getName());
		formComponentsNames.remove(formCompName);
		/*
		 * remove the gui representation
		 */
		formLayoutContainer.remove(toBeReplaced.getComponent());
		formLayoutContainer.remove(toBeReplaced.getErrorC());
		formLayoutContainer.remove(toBeReplaced.getExampleC());
		formLayoutContainer.remove(toBeReplaced.getLabelC());
		
		
	// set the formtranslator, and parent
		Translator itemTranslator = with.getTranslator();
		if(itemTranslator != null && !itemTranslator.equals(translator)
				&& itemTranslator instanceof PackageTranslator) {
			//let the FormItem provide a more specialized translator
			PackageTranslator itemPt = (PackageTranslator)itemTranslator;
			itemTranslator = PackageTranslator.cascadeTranslators(itemPt, translator);
		}else{
			itemTranslator = translator;
		}
		with.setTranslator(itemTranslator);
		with.setRootForm(getRootForm());
		
		formComponents.put(formCompName, with);
		/*
		 * add the gui representation
		 */
		formLayoutContainer.put(formCompName, with.getComponent());
		formLayoutContainer.put(formCompName + FormItem.ERRORC, with.getErrorC());
		formLayoutContainer.put(formCompName + FormItem.EXAMPLEC, with.getExampleC());
		formLayoutContainer.put(formCompName + FormItem.LABELC, with.getLabelC());

		// Check for multipart data, add upload limit to form
		if (with instanceof FormMultipartItem) {
			getRootForm().setMultipartEnabled(true);
		}
	}
	
	/**
	 * remove the component with the give name from this container	  
	 * @param binderName
	 */
	@Override
	public void remove(String formCompName) {
		FormItem toBeRemoved = getFormComponent(formCompName);
		if(toBeRemoved != null) {
			remove(formCompName, toBeRemoved);
		}
	}
	
	private void remove(String formCompName, FormItem toBeRemoved) {
		// book keeping of FormComponent order
		formComponentsNames.remove(formCompName);
		formComponents.remove(formCompName);
		/*
		 * remove the gui representation
		 */
		formLayoutContainer.remove(toBeRemoved.getComponent());
		formLayoutContainer.remove(toBeRemoved.getErrorC());
		formLayoutContainer.remove(toBeRemoved.getExampleC());
		formLayoutContainer.remove(toBeRemoved.getLabelC());
	}
	
	public void removeAll() {
		formComponentsNames.clear();
		formComponents.clear();
		formLayoutContainer.clear();
	}
	
	public void moveBefore(FormItem itemToBeMoved, FormItem itemRef) {
		int index = formComponentsNames.indexOf(itemToBeMoved.getName());
		int indexRef = formComponentsNames.indexOf(itemRef.getName());
		if(index > 0 && indexRef >= 0) {
			String toMove = formComponentsNames.remove(index);
			formComponentsNames.add(indexRef, toMove);
		}
	}
	
	@Override
	public VelocityContainer getFormItemComponent() {
		return formLayoutContainer;
	}
	
	@Override
	public Iterable<FormItem> getFormItems() {
		return new ArrayList<>(formComponents.values());
	}

	@Override
	public Map<String, FormItem> getFormComponents() {
		return Map.copyOf(formComponents);
	}

	@Override
	public boolean hasFormComponent(FormItem item) {
		return formComponents.containsValue(item);
	}

	@Override
	public FormItem getFormComponent(String name){
		return formComponents.get(name);
	}
	
	public Context getContext() {
		return formLayoutContainer.getContext();
	}
	
	public void contextPut(String key, Object value) {
		formLayoutContainer.contextPut(key, value);
	}
	
	public void contextRemove(String key) {
		formLayoutContainer.contextRemove(key);
	}
	
	public Object contextGet(String key) {
		return formLayoutContainer.contextGet(key);
	}

	public void put(String name, Component component) {
		formLayoutContainer.put(name, component);
	}
	
	public void remove(Component component){
		formLayoutContainer.remove(component);
	}

	public Component getComponent(String name) {
		return formLayoutContainer.getComponent(name);
	}

	@Override
	public boolean isDomReplacementWrapperRequired() {
		return formLayoutContainer.isDomReplacementWrapperRequired();
	}
	
	public String getId(String prefix) {
		return VelocityRenderDecorator.getId(prefix, formLayoutContainer);
	}
	
	@Override
	public String getForId() {
		return null;
	}
	
	@Override
	public void setElementCssClass(String elementCssClass) {
		formLayoutContainer.setElementCssClass(elementCssClass);
		super.setElementCssClass(elementCssClass);
	}

	/**
	 * Set the translated title
	 * @param title
	 */
	public void setFormTitle(String title) {
		formLayoutContainer.contextPut("off_title", title);
	}
	
	/**
	 * Set an icon to thte title
	 * @param iconCss
	 */
	public void setFormTitleIconCss(String iconCss) {
		if (iconCss == null) {
			formLayoutContainer.contextRemove("off_icon");
		} else {
			formLayoutContainer.contextPut("off_icon", iconCss);
		}
	}
	
	/**
	 * Set the translated description
	 * @param description
	 */
	public void setFormDescription(String description) {
		formLayoutContainer.contextPut("off_desc", description);
	}
	
	public void setFormInfo(String info) {
		formLayoutContainer.contextPut("off_info", info);
	}
	
	public void setFormWarning(String warning) {
		formLayoutContainer.contextPut("off_warn", warning);
	}
	
	public void setFormInfoHelp(String url) {
		if (url == null) {
			formLayoutContainer.contextRemove("off_info_help_url");
		} else {
			formLayoutContainer.contextPut("off_info_help_url", url);
		}
	}
	
	/**
	 * Set an optional context help link for this form. If you use a custom
	 * template this will have no effect
	 * 
	 * @param url The page in OpenOlat-docs 
	 */
	public void setFormContextHelp(String url) {
		if (url == null) {
			formLayoutContainer.contextRemove("off_chelp_url");
		} else {
			formLayoutContainer.contextPut("off_chelp_url", url);
		}
	}
	

	/**
	 * 
	 * @see org.olat.core.gui.components.form.flexible.FormLayouter#setDirty(boolean)
	 */
	public void setDirty(boolean dirty){
		formLayoutContainer.setDirty(dirty);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormItemImpl#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean isEnabled) {
		//enable / disable this
		super.setEnabled(isEnabled);
		//iterate over all components and disable / enable them
		for (FormItem element : getFormItems()) {
			element.setEnabled(isEnabled);
		}
	}

	/**
	 * Create a default layout container with the standard label - element alignment left.
	 * 
	 * @param name
	 * @param formTranslator
	 * @return
	 */
	public static FormLayoutContainer createDefaultFormLayout(String name, Translator formTranslator){
		return new FormLayoutContainer(name, formTranslator, LAYOUT_DEFAULT);
	}
	
	/**
	 * This a variant of the default form layout but with a ration 6 to 6 for label and field.
	 * @param name
	 * @param formTranslator
	 * @return
	 */
	public static FormLayoutContainer createDefaultFormLayout_6_6(String name, Translator formTranslator){
		return new FormLayoutContainer(name, formTranslator, LAYOUT_DEFAULT_6_6);
	}
	
	
	/**
	 * This a variant of the default form layout but with a ration 9 to 3 for label and field.
	 * @param name
	 * @param formTranslator
	 * @return
	 */
	public static FormLayoutContainer createDefaultFormLayout_9_3(String name, Translator formTranslator){
		return new FormLayoutContainer(name, formTranslator, LAYOUT_DEFAULT_9_3);
	}
	
	/**
	 * This a variant of the default form layout but with a ration 2 to 10 for label and field.
	 * @param name
	 * @param formTranslator
	 * @return
	 */
	public static FormLayoutContainer createDefaultFormLayout_2_10(String name, Translator formTranslator){
		return new FormLayoutContainer(name, formTranslator, LAYOUT_DEFAULT_2_10);
	}
	
	/**
	 * Create a layout container that renders the form elements and its labels vertically. 
	 * 
	 * @param name
	 * @param formTranslator
	 * @return
	 */
	public static FormLayoutContainer createHorizontalFormLayout(String name, Translator formTranslator){
		return new FormLayoutContainer(name, formTranslator, LAYOUT_HORIZONTAL);
	}

	/**
	 * Create a layout container that renders the form elements and its labels
	 * vertically. This means that the label of an element is forced to be on a
	 * separate line without any left indent.
	 * 
	 * @param name
	 * @param formTranslator
	 * @return
	 */
	public static FormLayoutContainer createVerticalFormLayout(String name, Translator formTranslator){
		return new FormLayoutContainer(name, formTranslator, LAYOUT_VERTICAL);
	}
	
	/**
	 * Create a layout container which only loop and render a list of components. There isn't
	 * any warning, error, label rendering.
	 * @param name
	 * @param formTranslator
	 * @return
	 */
	public static FormLayoutContainer createBareBoneFormLayout(String name, Translator formTranslator){
		return new FormLayoutContainer(name, formTranslator, LAYOUT_BAREBONE);
	}
	
	/**
	 * Create a layout container based on the panel of bootstrap
	 * @param name
	 * @param formTranslator
	 * @return
	 */
	public static FormLayoutContainer createPanelFormLayout(String name, Translator formTranslator){
		return new FormLayoutContainer(name, formTranslator, LAYOUT_PANEL);
	}

	/**
	 * Create a layout container that should be only used to render buttons using
	 * a o_button_group css wrapper. Buttons are ususally rendered on one line
	 * without indent
	 * 
	 * @param name
	 * @param formTranslator
	 * @return
	 */
	public static FormLayoutContainer createButtonLayout(String name, Translator formTranslator) {
		return new FormLayoutContainer(name, formTranslator, LAYOUT_BUTTONGROUP);
	}
	
	/**
	 * 
	 * @param name
	 * @param formTranslator
	 * @return
	 */
	public static FormLayoutContainer createTableCondensedLayout(String name, Translator formTranslator) {
		return new FormLayoutContainer(name, formTranslator, LAYOUT_TABLE_CONDENSED);
	}

	/**
	 * Create a layout container that should be only used to render input groups. Input groups are 
	 * form items that are decorated with some left or right sided add-on. The add-on can be either
	 * a text (e.g. "@" to indicate an email address field) or an html i-tag with some OpenOLAT image
	 * classes. Alternatively, a second component can be added to the layout container with the name
	 * "leftAddOn" or "rightAddOn" to use the component as add-on (e.g. to implement a chooser link)
	 * 
	 * @param name The name of the form layout container
	 * @param formTranslator the form translator
	 * @param leftTextAddOn the left side add-on text or NULL if not used
	 * @param rightTextAddOn the right side add-on text or NULL if not used
	 * @return the form layout container
	 */
	public static FormLayoutContainer createInputGroupLayout(String name, Translator formTranslator, String leftTextAddOn, String rightTextAddOn) {
		FormLayoutContainer tmp = new FormLayoutContainer(name, formTranslator, LAYOUT_INPUTGROUP);
		if (StringHelper.containsNonWhitespace(leftTextAddOn)) {
			tmp.contextPut("leftAddOn", leftTextAddOn);
		}
		if (StringHelper.containsNonWhitespace(rightTextAddOn)) {
			tmp.contextPut("rightAddOn", rightTextAddOn);
		}
		return tmp;
	}

	/**
	 * Create a layout based on a velocity template.
	 * 
	 * @param name The name of the component
	 * @param formTranslator The translator
	 * @param page The velocity template
	 * @return
	 */
	public static FormLayoutContainer createCustomFormLayout(String name, Translator formTranslator, String page) {
		return createCustomFormLayout(null, name, formTranslator, page);
	}

	public static FormLayoutContainer createCustomFormLayout(String id, String name, Translator formTranslator, String page) {	
		return new FormLayoutContainer(id, name, formTranslator, page);
	}

	@Override
	public void setTranslator(Translator translator) {
		super.setTranslator(translator);
		// set also translator on velocity container delegate
		formLayoutContainer.setTranslator(translator);
	}

	/**
	 * Dispose all child elements from this container
	 * 
	 * @see org.olat.core.gui.control.Disposable#dispose()
	 */
	@Override
	public void dispose() {
		// Dispose also disposable form items (such as file uploads that needs to
		// cleanup temporary files)
		for (FormItem formItem : getFormItems()) {
			if (formItem instanceof Disposable) {
				Disposable disposableFormItem = (Disposable) formItem;
				disposableFormItem.dispose();
			}
		}
	}
}
