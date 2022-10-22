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

import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormBaseComponentIdProvider;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.InlineElement;
import org.olat.core.gui.components.form.flexible.impl.components.SimpleExampleText;
import org.olat.core.gui.components.form.flexible.impl.components.SimpleFormErrorText;
import org.olat.core.gui.components.form.flexible.impl.components.SimpleLabelText;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.AssertException;
import org.olat.core.util.ValidationStatus;

/**
 * <h2>Description:</h2>
 * <P>
 * Initial Date: 22.11.2006 <br>
 * 
 * @author patrickb
 */
public abstract class FormItemImpl implements InlineElement {
	private static final String PREFIX = "PANEL_";
	private boolean componentIsMandatory;
	private String errorKey;
	private String[] errorParams;
	private Component errorComponent;
	private Panel errorPanel;
	private String[] exampleParams;
	private String exampleKey;
	private String[] helpParams;
	private String helpKey;
	private String helpText;
	private String helpUrl;
	private Component exampleC;
	private Panel examplePanel;
	private String[] labelParams;
	private String labelKey;
	private boolean translateLabel;
	private Component labelC;
	private Panel labelPanel;
	protected Translator translator;
	private final String id;
	private final String name;
	private boolean hasLabel = false;
	private boolean hasExample = false;
	protected boolean hasError = false;
	protected boolean hasWarning = false;
	private Form rootForm = null;
	protected int action;
	private Object userObject;
	private boolean hasFocus = false;
	private boolean formItemIsEnabled = true;
	private boolean isInlineEditingElement;
	private boolean isInlineEditingOn;
	private Component inlineEditingComponent;
	private String i18nKey4EmptyText="inline.empty.click.for.edit";
	private String elementCssClass;

	/**
	 * 
	 * @param name
	 */
	public FormItemImpl(String name) {
		this(name, false);//default is not inline
	}
	
	public FormItemImpl(String name, boolean asInlineEditingElement) {
		this(null, name, asInlineEditingElement);
	}

	public FormItemImpl(String id, String name, boolean asInlineEditingElement) {
		this.id = id;
		this.name = name;
		this.isInlineEditingElement = asInlineEditingElement;
		/*
		 * prepare three panels as placeholder for label, example, error
		 */
		String pName = PREFIX.concat(name);
		errorPanel = new Panel(pName.concat(FormItem.ERRORC));
		examplePanel = new Panel(pName.concat(FormItem.EXAMPLEC));
		labelPanel = new Panel(pName.concat(FormItem.LABELC));
	}

	@Override
	public String getFormItemId() {
		return id;
	}

	@Override
	public String getForId() {
		return getFormDispatchId();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getElementCssClass() {
		return elementCssClass;
	}

	@Override
	public void setElementCssClass(String elementCssClass) {
		this.elementCssClass = elementCssClass;
	}

	@Override
	public boolean isInlineEditingOn() {
		if(!isInlineEditingElement) throw new AssertException("isInlineEditingOn called although it is not a inlineEditingElement");
		return isInlineEditingOn;
	}
	
	@Override
	public FormItem setEmptyDisplayText(String i18nKey4Text) {
		this.i18nKey4EmptyText = i18nKey4Text;
		return this;
	}

	@Override
	public String getEmptyDisplayText(){
		if(getTranslator()==null) throw new AssertException("getEmptyDisplayText called to early, no translator available");
		return translate(i18nKey4EmptyText, null);
	}
	
	protected void isInlineEditingOn(boolean isOn){
		if(!isInlineEditingElement) throw new AssertException("isInlineEditingOn(..) called although it is not a inlineEditingElement");
		isInlineEditingOn = isOn;
	}
	
	protected Component getInlineEditingComponent(){
		if(!isInlineEditingElement) throw new AssertException("getInlineEditingComponent called although it is not a inlineEditingElement");
		return inlineEditingComponent;
	}
	
	protected void setInlineEditingComponent(Component inlineEditingComponent){
		if(!isInlineEditingElement) throw new AssertException("getInlineEditingComponent called although it is not a inlineEditingElement");
		this.inlineEditingComponent = inlineEditingComponent;
	}
	
	@Override
	public boolean isInlineEditingElement(){
		return isInlineEditingElement;
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#getAsComponent()
	 */
	@Override
	public Component getComponent(){
		//
		return isInlineEditingElement ? getInlineEditingComponent() : getFormItemComponent();
	}
	
	protected abstract Component getFormItemComponent();

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#getRootForm()
	 */
	@Override
	public Form getRootForm() {
		return rootForm;
	}

	@Override
	public void setRootForm(Form rootForm){
		this.rootForm = rootForm;
		rootFormAvailable();
	}
	
	protected abstract void rootFormAvailable();
	
	protected boolean translateLabel() {
		return translateLabel;
	}

	@Override
	public void setTranslator(Translator translator) {
		this.translator = translator;		
		//(re)translate label, error, example
		//typically setTranslator is called form parent container if the FormItem
		//is added.
		String labelTrsl = translateLabel() ? translate(labelKey, labelParams) : labelKey;
		if(Settings.isDebuging()){
			//in develmode, check that either translation for labelkey is available
			//or that the other method to add the element is used.
			//other in the sense of using the LabelI18nKey set to null, this 
			//avoids false messages in the logfile concering missng translations.
			if(labelTrsl == null && hasLabel()){
				throw new AssertException("Your label "+labelKey+" for formitem "+getName()+" is not available, please use the addXXX method with labelI18nKey and set it to null.");
			}
		}
		if(labelKey != null) {
			labelC = new SimpleLabelText(this, labelKey, labelTrsl);
			labelC.setTranslator(translator);
			labelPanel.setContent(labelC);
		}
		if(errorKey != null) {
			errorComponent = new SimpleFormErrorText(errorKey, translate(errorKey, errorParams), hasWarning());
			errorPanel.setContent(errorComponent);
		}
		if(exampleKey != null) {
			exampleC = new SimpleExampleText(exampleKey, translate(exampleKey, exampleParams));
			examplePanel.setContent(exampleC);
		}
		if(helpKey != null) {
			helpText = translate(helpKey, helpParams);
		}
	}

	@Override
	public Translator getTranslator() {
		return translator;
	}

	@Override
	public Component getLabelC() {
		return labelPanel;
	}

	@Override
	public String getLabelText() {
		return translateLabel() ? translate(labelKey, labelParams) : labelKey;
	}

	@Override
	public void setLabel(String label, String[] params) {
		setLabel(label, params,  true); 
	}

	@Override
	public void setLabel(String label, String[] params, boolean translate) {
		hasLabel = (label != null);
		translateLabel = translate;
		labelKey = label;
		labelParams = params;
		// set label may be called before the translator is available
		if (getTranslator() != null && labelKey != null) {
			labelC = new SimpleLabelText(this, label, getLabelText());
			labelC.setTranslator(getTranslator());
			labelPanel.setContent(labelC);
		} else if(label == null) {
			labelC = null;
			labelPanel.setContent(labelC);
		}
	}

	@Override
	public void setFocus(boolean hasFocus){
		this.hasFocus  = hasFocus;
	}

	@Override
	public boolean isMandatory() {
		return componentIsMandatory;
	}

	@Override
	public boolean hasFocus(){
		return hasFocus;
	}

	@Override
	public void setMandatory(boolean isMandatory) {
		componentIsMandatory = isMandatory;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#getExample()
	 */
	@Override
	public Component getExampleC() {
		return examplePanel;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#getExampleText()
	 */
	@Override
	public String getExampleText() {
		return translate(exampleKey, exampleParams);
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#getHelpText()
	 */
	@Override
	public String getHelpText() {
		// always translated
		return helpText;		
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#getHelpUrl()
	 */
	@Override
	public String getHelpUrl() {
		return helpUrl;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#setExampleKey(java.lang.String,
	 *      java.lang.String[])
	 */
	@Override
	public void setExampleKey(String exampleKey, String[] params) {
		if(exampleKey == null) {
			// reset
			exampleC = null;
			examplePanel.setContent(exampleC);
			hasExample = false;
		} else {
			hasExample = true;
			this.exampleKey = exampleKey;
			this.exampleParams = params;
			if (getTranslator() != null) {
				exampleC = new SimpleExampleText(exampleKey, translate(exampleKey, params));
				examplePanel.setContent(exampleC);
			} 
		}
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#setHelpTextKey(java.lang.String,
	 *      java.lang.String[])
	 */
	@Override
	public void setHelpTextKey(String helpKey, String[] params) {
		this.helpKey = helpKey;
		this.helpParams = params;
		if (getTranslator() != null) {
			this.helpText = translate(helpKey, helpParams);
		}
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#setHelpText(java.lang.String)
	 */
	@Override
	public void setHelpText(String helpText) {
		this.helpKey = null;
		this.helpParams = null;
		this.helpText = helpText;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#setHelpUrl(java.lang.String)
	 */
	@Override
	public void setHelpUrl(String helpUrl) {
		this.helpUrl = helpUrl;
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#setHelpUrlForManualPage(java.lang.String)
	 */
	@Override
	public void setHelpUrlForManualPage(String manualAliasName) {
		HelpModule helpModule = CoreSpringFactory.getImpl(HelpModule.class);
		
		if (helpModule.isManualEnabled()) {
			Locale locale = getTranslator().getLocale();
			this.helpUrl = helpModule.getManualProvider().getURL(locale, manualAliasName);
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#setErrorKey(java.lang.String,
	 *      java.lang.String[])
	 */
	@Override
	public void setErrorKey(String errorKey, String[] params) {
		setErrorKey(errorKey, false, params);
	}
	
	@Override
	public void setErrorKey(String errorKey, boolean isWarning, String... params) {
		this.hasError = !isWarning;
		this.hasWarning = isWarning;
		this.errorKey = errorKey;
		
		// legacy check to prevent NPE when passing null to params instead of nothing
		if (params != null && params.length == 1 && params[0] == null) {
			this.errorParams = null;
		} else {
			this.errorParams = params;
		}
		if (getTranslator() != null) {
			errorComponent = new SimpleFormErrorText(errorKey, translate(errorKey, errorParams), isWarning);		
			errorPanel.setContent(errorComponent);
		}
		showError(true);
		getRootForm().getInitialComponent().setDirty(true);
	}

	/**
	 * convenience
	 * 
	 * @param key
	 * @param params
	 * @return
	 */
	private String translate(String key, String[] params) {
		String retVal = null;
		if (key != null && params != null) {
			retVal = translator.translate(key, params);
		} else if (key != null) {
			retVal = translator.translate(key);
		}
		return retVal;
	}

	@Override
	public void setErrorComponent(FormItem errorFormItem, FormItemContainer container) {
		if(errorFormItem == null){
			throw new AssertException("do not clear error by setting null, instead use showError(false).");
		}
		//initialize root form of form item
		FormLayoutContainer flc = (FormLayoutContainer)container;
		flc.register(errorFormItem);//errorFormItem must be part of the composite chain, that it gets dispatched
		
		hasError = true;
		errorComponent = errorFormItem.getComponent();
		errorPanel.setContent(errorComponent);
	}

	@Override
	public Component getErrorC() {
		return errorPanel;
	}
	
	@Override
	public void setEnabled(boolean isEnabled) {
		getErrorC().setEnabled(isEnabled);
		if(errorComponent != null) {
			errorComponent.setEnabled(isEnabled);
		}
		getExampleC().setEnabled(isEnabled);
		if(exampleC!=null) {
			exampleC.setEnabled(isEnabled);
		}
		getLabelC().setEnabled(isEnabled);
		if(labelC!=null) {
			labelC.setEnabled(isEnabled);
		}
		
		formItemIsEnabled = isEnabled;
		if(getComponent()==null) return;
		getComponent().setEnabled(isEnabled);
	}

	@Override
	public boolean isEnabled(){
		return formItemIsEnabled;
	}

	@Override
	public void setVisible(boolean isVisible) {
		if(getComponent()==null) return;
		getComponent().setVisible(isVisible);
		showError(isVisible && hasError);
		showExample(isVisible && hasExample);
		showLabel(isVisible && hasLabel);
	}

	@Override
	public boolean isVisible() {
		if(getComponent() == null) return false;
		return getComponent().isVisible();
	}

	@Override
	public boolean hasError(){
		return hasError;
	}

	@Override
	public boolean hasLabel(){
		return hasLabel;
	}

	@Override
	public boolean hasExample(){
		return hasExample;
	}
	
	@Override
	public boolean hasWarning() {
		return hasWarning;
	}

	@Override
	public void showLabel(boolean show){
		if(show) {
			labelPanel.setContent(labelC);
		}else{
			labelPanel.setContent(null);
		}
		labelPanel.setVisible(show);
		labelPanel.setEnabled(show);
	}
	
	@Override
	public void showError(boolean show){
		if (show) {
			errorPanel.setContent(errorComponent);
		}else{
			errorPanel.setContent(null);
		}
		errorPanel.setVisible(show);
		errorPanel.setEnabled(show);
	}

	@Override
	public void clearError(){
		showError(false);
		hasError = false;
		hasWarning = false;
	}
	

	@Override
	public void showExample(boolean show){
		if(show) {
			examplePanel.setContent(exampleC);
		}else{
			examplePanel.setContent(null);
		}
		examplePanel.setVisible(show);
		examplePanel.setEnabled(show);
	}
		
	/**
	 * @see org.olat.core.gui.components.form.flexible.FormItem#addActionListenerFor(org.olat.core.gui.control.Controller, int)
	 */
	@Override
	public void addActionListener(int action) {
		this.action = action;
	}

	@Override
	public int getAction() {
		return action;
	}

	/**
	 * gets called if the implementing component was clicked.
	 * 
	 * @param ureq
	 * @param formId
	 */
	@Override
	public void doDispatchFormRequest(UserRequest ureq){
		//first let implementor do its job
		dispatchFormRequest(ureq);
		if(getRootForm().hasAlreadyFired()){
			//dispatchFormRequest did fire already
			//in this case we do not try to fire the general events
			return;
		}
		//before/ after pattern
		switch (getRootForm().getAction()) {
			case FormEvent.ONCLICK:
				getRootForm().fireFormEvent(ureq, new FormEvent("ONCLICK", this, FormEvent.ONCLICK));
				break;
			case FormEvent.ONDBLCLICK:
				getRootForm().fireFormEvent(ureq, new FormEvent("ONDBLCLICK", this, FormEvent.ONDBLCLICK));
				break;
			case FormEvent.ONCHANGE:
				getRootForm().fireFormEvent(ureq, new FormEvent("ONCHANGE", this, FormEvent.ONCHANGE));
				break;
			case FormEvent.ONKEYUP:
				getRootForm().fireFormEvent(ureq, new FormEvent("ONCHANGE", this, FormEvent.ONKEYUP));
				break;
			default:
				//nothing to do, default is handled
		}
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.api.FormItem#getUserObject()
	 */
	@Override
	public Object getUserObject() {
		return userObject;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.api.FormItem#setUserObject(java.lang.Object)
	 */
	@Override
	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	@Override
	public String getFormDispatchId() {	
		Component comp = getComponent();
		if(comp instanceof FormBaseComponentIdProvider){
			return ((FormBaseComponentIdProvider)comp).getFormDispatchId();
		} else {
			//do the same as the FormBaseComponentIdProvider would do
			return DISPPREFIX.concat(comp.getDispatchID());
		}
	}

	
	/**
	 * override to implement your behaviour
	 * @param ureq
	 */
	protected void dispatchFormRequest(UserRequest ureq){
		//default implementation does nothing
	}
	
	@Override
	public void validate(List<ValidationStatus> validationResults) {
		//
	}
	
	@Override
	public String toString(){
		return "FoItem:"+getName()+
		"[ena:"+isEnabled()+
		", vis:"+isVisible()+
		", err:"+hasError()+
		", warn:"+hasWarning()+
		", exa:"+hasExample()+
		", lab:"+hasLabel();
	}
	
}
