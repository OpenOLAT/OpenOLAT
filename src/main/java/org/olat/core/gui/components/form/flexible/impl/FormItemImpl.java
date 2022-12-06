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

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormBaseComponentIdProvider;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.InlineElement;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.AssertException;

/**
 * <h2>Description:</h2>
 * <P>
 * Initial Date: 22.11.2006 <br>
 * 
 * @author patrickb
 */
public abstract class FormItemImpl implements InlineElement {

	private boolean componentIsMandatory;
	
	private String errorKey;
	private String[] errorParams;
	protected boolean hasError = false;
	private String warningKey;
	private String[] warningParams;
	protected boolean hasWarning = false;
	
	private String helpKey;
	private String[] helpParams;
	private String helpText;
	private String helpUrl;

	private String[] exampleParams;
	private String exampleKey;
	private boolean hasExample = false;
	
	private String[] labelParams;
	private String labelKey;
	private boolean translateLabel;
	private boolean hasLabel;
	
	protected Translator translator;
	private final String id;
	private final String name;
	private Form rootForm = null;
	protected int action;
	private Object userObject;
	private boolean hasFocus = false;
	private boolean formItemIsEnabled = true;
	private boolean isInlineEditingElement;
	private boolean isInlineEditingOn;
	private boolean inlineValidationOn;
	private Component inlineEditingComponent;
	private String i18nKey4EmptyText="inline.empty.click.for.edit";
	private String elementCssClass;

	/**
	 * 
	 * @param name
	 */
	protected FormItemImpl(String name) {
		this(name, false);//default is not inline
	}
	
	protected FormItemImpl(String name, boolean asInlineEditingElement) {
		this(null, name, asInlineEditingElement);
	}

	protected FormItemImpl(String id, String name, boolean asInlineEditingElement) {
		this.id = id;
		this.name = name;
		this.isInlineEditingElement = asInlineEditingElement;
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
	public boolean isInlineValidationOn() {
		return inlineValidationOn;
	}

	@Override
	public void setInlineValidationOn(boolean on) {
		this.inlineValidationOn = on;
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

		if(helpKey != null) {
			helpText = translate(helpKey, helpParams);
		}
	}

	@Override
	public Translator getTranslator() {
		if(translator == null && getFormItemComponent() != null) {
			return getFormItemComponent().getTranslator();
		}
		return translator;
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
		hasLabel = label != null;
		translateLabel = translate;
		labelKey = label;
		labelParams = params;
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

	@Override
	public String getExampleText() {
		return translate(exampleKey, exampleParams);
	}

	@Override
	public String getHelpText() {
		// always translated
		return helpText;		
	}
	
	@Override
	public String getHelpUrl() {
		return helpUrl;
	}

	@Override
	public void setExampleKey(String exampleKey, String[] params) {
		if(exampleKey == null) {
			hasExample = false;
		} else {
			hasExample = true;
			this.exampleKey = exampleKey;
			this.exampleParams = params;
		}
	}

	@Override
	public void setHelpTextKey(String helpKey, String[] params) {
		this.helpKey = helpKey;
		this.helpParams = params;
		if (getTranslator() != null) {
			this.helpText = translate(helpKey, helpParams);
		}
	}

	@Override
	public void setHelpText(String helpText) {
		this.helpKey = null;
		this.helpParams = null;
		this.helpText = helpText;
	}

	@Override
	public void setHelpUrl(String helpUrl) {
		this.helpUrl = helpUrl;
	}

	@Override
	public void setHelpUrlForManualPage(String manualAliasName) {
		HelpModule helpModule = CoreSpringFactory.getImpl(HelpModule.class);
		
		if (helpModule.isManualEnabled()) {
			Locale locale = getTranslator().getLocale();
			this.helpUrl = helpModule.getManualProvider().getURL(locale, manualAliasName);
		}
	}

	@Override
	public String getErrorText() {
		return translate(errorKey, errorParams);
	}

	@Override
	public void setErrorKey(String errorKey, String... params) {
		this.hasError = true;
		this.errorKey = errorKey;
		// legacy check to prevent NPE when passing null to params instead of nothing
		if (params != null && params.length == 1 && params[0] == null) {
			errorParams = null;
		} else {
			errorParams = params;
		}
		setComponentDirty();
	}
	
	@Override
	public String getWarningText() {
		return translate(warningKey, warningParams);
	}

	@Override
	public void setWarningKey(String warningKey, String... params) {
		this.hasWarning = true;
		this.warningKey = warningKey;
		
		// legacy check to prevent NPE when passing null to params instead of nothing
		if (params != null && params.length == 1 && params[0] == null) {
			warningParams = null;
		} else {
			warningParams = params;
		}
		setComponentDirty();
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
	public void setEnabled(boolean isEnabled) {
		formItemIsEnabled = isEnabled;
		if(getComponent() != null) {
			getComponent().setEnabled(isEnabled);
		}
	}

	@Override
	public boolean isEnabled(){
		return formItemIsEnabled;
	}

	@Override
	public void setVisible(boolean isVisible) {
		if(getComponent() != null) {
			getComponent().setVisible(isVisible);
		}
	}

	@Override
	public boolean isVisible() {
		if(getComponent() == null) return false;
		return getComponent().isVisible();
	}

	@Override
	public String getFormLayout() {
		if(getComponent() != null) {
			return getComponent().getLayout();
		}
		return null;
	}

	@Override
	public void setFormLayout(String layout) {
		if(getComponent() != null) {
			getComponent().setLayout(layout);
		}
	}

	@Override
	public boolean hasError() {
		return hasError;
	}

	@Override
	public boolean hasLabel() {
		return labelKey != null && hasLabel;
	}

	@Override
	public boolean hasExample() {
		return hasExample;
	}
	
	@Override
	public boolean hasWarning() {
		return hasWarning;
	}

	@Override
	public void showLabel(boolean show) {
		this.hasLabel = show;
	}
	
	@Override
	public void showError(boolean show) {
		if(hasError != show) {
			this.hasError = show;
			setComponentDirty();
		}
	}
	
	public void showWarning(boolean show) {
		if(hasWarning != show) {
			this.hasWarning = show;
			setComponentDirty();
		}
	}

	@Override
	public void clearError() {
		showError(false);
		hasError = false;
	}
	
	@Override
	public void clearWarning() {
		showWarning(false);
		hasWarning = false;
	}

	protected void setComponentDirty() {
		Component cmp = getComponent();
		if(cmp != null) {
			cmp.setDirty(true);
		}
	}

	@Override
	public void showExample(boolean show) {
		this.hasExample = show;
	}

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
			case FormEvent.ONBLUR:
				getRootForm().fireFormEvent(ureq, new FormEvent("ONBLUR", this, FormEvent.ONBLUR));
				break;
			case FormEvent.ONVALIDATION:	
				getRootForm().validateInline(ureq, this);
				break;
			default:
				//nothing to do, default is handled
		}
	}

	@Override
	public Object getUserObject() {
		return userObject;
	}

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
	public boolean validate() {
		return true;
	}

	@Override
	public boolean isValidationDeferred() {
		return false;
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
