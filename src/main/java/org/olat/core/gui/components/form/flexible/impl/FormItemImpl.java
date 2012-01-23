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

import org.olat.core.gui.GUIInterna;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormBaseComponentIdProvider;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormLayouter;
import org.olat.core.gui.components.form.flexible.elements.InlineElement;
import org.olat.core.gui.components.form.flexible.impl.components.SimpleExampleText;
import org.olat.core.gui.components.form.flexible.impl.components.SimpleFormErrorText;
import org.olat.core.gui.components.form.flexible.impl.components.SimpleLabelText;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Controller;
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
public abstract class FormItemImpl implements FormItem, InlineElement {
	private static final String PREFIX = "PANEL_";
	private boolean componentIsMandatory;
	private String errorKey;
	private String[] errorParams;
	private Component errorComponent;
	private Panel errorPanel;
	private String[] exampleParams;
	private String exampleKey;
	private Component exampleC;
	private Panel examplePanel;
	private String[] labelParams;
	private String labelKey;
	private Component labelC;
	private Panel labelPanel;
	protected Translator translator;
	private final String id;
	private String name;
	private boolean hasLabel = false;
	private boolean hasExample = false;
	protected boolean hasError = false;
	private Form rootForm = null;
	protected int action;
	private Object userObject;
	private boolean hasFocus = false;
	private boolean formItemIsEnabled = true;
	private boolean isInlineEditingElement;
	private boolean isInlineEditingOn;
	private Component inlineEditingComponent;
	private String i18nKey4EmptyText="inline.empty.click.for.edit";

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
		errorPanel = new Panel(PREFIX + name + FormItem.ERRORC);
		examplePanel = new Panel(PREFIX + name + FormItem.EXAMPLEC);
		labelPanel = new Panel(PREFIX + name + FormItem.LABELC);
	}
	
	public String getFormItemId() {
		return id;
	}
	
	public String getName() {
		return name;
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
	
	protected boolean isInlineEditingElement(){
		return isInlineEditingElement;
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#getAsComponent()
	 */
	public Component getComponent(){
		//
		return isInlineEditingElement ? getInlineEditingComponent() : getFormItemComponent();
	}
	
	protected abstract Component getFormItemComponent();

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#getRootForm()
	 */
	public Form getRootForm() {
		return rootForm;
	}
	
	public void setRootForm(Form rootForm){
		this.rootForm = rootForm;
		rootFormAvailable();
	}
	
	protected abstract void rootFormAvailable();
	
	protected boolean translateLabel() {
		return true;
	}

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
		labelC = new SimpleLabelText(labelKey, labelTrsl);
		errorComponent = new SimpleFormErrorText(errorKey, translate(errorKey, errorParams));
		exampleC = new SimpleExampleText(exampleKey, translate(exampleKey, exampleParams));
		labelPanel.setContent(labelC);
		errorPanel.setContent(errorComponent);
		examplePanel.setContent(exampleC);
	}

	public Translator getTranslator() {
		return translator;
	}

	public Component getLabelC() {
		return labelPanel;
	}

	public String getLabelText() {
		return translate(labelKey, labelParams);
	}

	public void setLabel(String label, String[] params) {
		if (label == null) {
			hasLabel = false;
		}
		hasLabel = true;
		labelKey = label;
		labelParams = params;
		// set label may be called before the translator is available
		if (getTranslator() != null) {
			labelC = new SimpleLabelText(label, translate(label, params));
			labelPanel.setContent(labelC);
		}
	}


	/**
	 * 
	 * @param labelComponent
	 * @param container
	 * @return this
	 */
	public FormItem setLabelComponent(FormItem labelComponent, FormItemContainer container) {
		if(labelComponent == null){
			throw new AssertException("do not clear error by setting null, instead use showLabel(false).");
		}
		
		this.hasLabel = true;
		//initialize root form of form item
		FormLayoutContainer flc = (FormLayoutContainer)container;//TODO:pb: fix this hierarchy mismatch
		flc.register(labelComponent);//errorFormItem must be part of the composite chain, that it gets dispatched
		
		labelC = labelComponent.getComponent();
		labelPanel.setContent(labelC);
		
		return this;
	}
	
	
	public void setFocus(boolean hasFocus){
		this.hasFocus  = hasFocus;
	}
	
	public boolean isMandatory() {
		return componentIsMandatory;
	}
	
	public boolean hasFocus(){
		return hasFocus;
	}

	public void setMandatory(boolean isMandatory) {
		componentIsMandatory = isMandatory;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#getExample()
	 */
	public Component getExampleC() {
		return examplePanel;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#getExampleText()
	 */
	public String getExampleText() {
		return translate(exampleKey, exampleParams);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#setExampleKey(java.lang.String,
	 *      java.lang.String[])
	 */
	public void setExampleKey(String exampleKey, String[] params) {
		hasExample = true;
		this.exampleKey = exampleKey;
		this.exampleParams = params;
		if (getTranslator() != null) {
			exampleC = new SimpleExampleText(exampleKey, translate(exampleKey, params));
			examplePanel.setContent(exampleC);
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#setErrorKey(java.lang.String,
	 *      java.lang.String[])
	 */
	public void setErrorKey(String errorKey, String[] params) {
		this.hasError = true;
		this.errorKey = errorKey;
		this.errorParams = params;
		if (getTranslator() != null) {
			errorComponent = new SimpleFormErrorText(errorKey, translate(errorKey, errorParams));		
			errorPanel.setContent(errorComponent);
		}
		this.showError(hasError);
		this.getRootForm().getInitialComponent().setDirty(true);
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

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#setErrorController(org.olat.core.gui.control.Controller)
	 */
	public void setErrorComponent(FormItem errorFormItem, FormLayouter container) {
		if(errorFormItem == null){
			throw new AssertException("do not clear error by setting null, instead use showError(false).");
		}
		//initialize root form of form item
		FormLayoutContainer flc = (FormLayoutContainer)container;//TODO:pb: fix this hierarchy mismatch
		flc.register(errorFormItem);//errorFormItem must be part of the composite chain, that it gets dispatched
		
		this.hasError = true;
		this.errorComponent = errorFormItem.getComponent();
		errorPanel.setContent(this.errorComponent);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#getErrorController()
	 */
	public Component getErrorC() {
		return errorPanel;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#getErrorText()
	 */
	public String getErrorText() {
		return translate(errorKey, errorParams);
	}

	public void setEnabled(boolean isEnabled) {
		getErrorC().setEnabled(isEnabled);
		if(errorComponent != null) errorComponent.setEnabled(isEnabled);
		getExampleC().setEnabled(isEnabled);
		if(exampleC!=null) exampleC.setEnabled(isEnabled);
		getLabelC().setEnabled(isEnabled);
		if(labelC!=null) labelC.setEnabled(isEnabled);
		this.formItemIsEnabled = isEnabled;
		if(getComponent()==null) return;
		getComponent().setEnabled(isEnabled);
	}
	
	public boolean isEnabled(){
		return formItemIsEnabled;
	}

	public void setVisible(boolean isVisible) {
		//FIXME:pb: getComponent can be null in the case of FormLink for example
		if(getComponent()==null) return;
		getComponent().setVisible(isVisible);
		showError(isVisible && hasError);
		showExample(isVisible && hasExample);
		showLabel(isVisible && hasLabel);
	}
	
	public boolean isVisible() {
		if(getComponent() == null) return false;
		return getComponent().isVisible();
	}
	
	public boolean hasError(){
		return hasError;
	}
	
	public boolean hasLabel(){
		return hasLabel;
	}
	
	public boolean hasExample(){
		return hasExample;
	}
	
	public void showLabel(boolean show){
		if(show) {
			labelPanel.setContent(labelC);
		}else{
			labelPanel.setContent(null);
		}
		labelPanel.setVisible(show);
		labelPanel.setEnabled(show);
	}
	public void showError(boolean show){
		if (show) {
			errorPanel.setContent(errorComponent);
		}else{
			errorPanel.setContent(null);
		}
		errorPanel.setVisible(show);
		errorPanel.setEnabled(show);
	}
	
	public void clearError(){
		showError(false);
		hasError = false;
	}
	
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
	public void addActionListener(Controller listener, int action) {
		/*
		 * for simplicity only one action and listener per item (at the moment)
		 */
		this.action = action;
		//for (int i = 0; i < FormEvent.ON_DOTDOTDOT.length; i++) {
			//if(action - FormEvent.ON_DOTDOTDOT[i] == 0){
				//String key = String.valueOf(FormEvent.ON_DOTDOTDOT[i]);
				//if(actionListeners.containsKey(key)){
					//List listeners = (List)actionListeners.get(key);
					//if(!listeners.contains(listener)){
						//listeners.add(listener);
					//}
				//}else{
					//String key = String.valueOf(this.action);
					//List listeners = new ArrayList(1);
					//actionListeners.put(key, listeners);
				//}
			//}
			//
			//action = action - FormEvent.ON_DOTDOTDOT[i];
		//}
	}
	
	/*public List getActionListenersFor(int event){
		return (List)actionListeners.get(String.valueOf(event));
	}*/

	public int getAction() {
		return action;
	}
	
	/**
	 * gets called if the implementing component is part of a form which gets
	 * partly submitted -> extract data for you and store it temporarly for
	 * redisplay without a validation
	 * 
	 * @param ureq
	 */
	public abstract void evalFormRequest(UserRequest ureq);

	/**
	 * gets called if the implementing component was clicked.
	 * 
	 * @param ureq
	 * @param formId
	 */
	public void doDispatchFormRequest(UserRequest ureq){
		//first let implementor do its job
		dispatchFormRequest(ureq);
		if(getRootForm().hasAlreadyFired()){
			//dispatchFormRequest did fire already
			//in this case we do not try to fire the general events
			return;
		}
		//before/ after pattern
		int action = getRootForm().getAction();
		switch (action) {
			case FormEvent.ONCLICK:
				getRootForm().fireFormEvent(ureq, new FormEvent("ONCLICK", this, FormEvent.ONCLICK));
				break;
			case FormEvent.ONDBLCLICK:
				getRootForm().fireFormEvent(ureq, new FormEvent("ONDBLCLICK", this, FormEvent.ONDBLCLICK));
				break;
			case FormEvent.ONCHANGE:
				getRootForm().fireFormEvent(ureq, new FormEvent("ONCHANGE", this, FormEvent.ONCHANGE));
				break;
			default:
				//nothing to do, default is handled
		}
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.api.FormItem#getUserObject()
	 */
	public Object getUserObject() {
		return userObject;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.api.FormItem#setUserObject(java.lang.Object)
	 */
	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}
	
	
	/**
	 * 
	 */
	public String getFormDispatchId() {	

		Component comp = getComponent();

		if(comp instanceof FormBaseComponentIdProvider){
			return ((FormBaseComponentIdProvider)comp).getFormDispatchId();
		}else{
			//do the same as the FormBaseComponentIdProvider would do
			if(GUIInterna.isLoadPerformanceMode()) {
				return DISPPREFIX+getRootForm().getReplayableDispatchID(comp);
			} else {
				return DISPPREFIX+comp.getDispatchID();
			}
		}
	}

	
	/**
	 * override to implement your behaviour
	 * @param ureq
	 */
	protected void dispatchFormRequest(UserRequest ureq){
		//default implementation does nothing
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.FormComponent#validate(java.util.List)
	 */
	public abstract void validate(List validationResults);

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormItem#reset()
	 */
	public abstract void reset();
	
	@Override
	public String toString(){
		return "FoItem:"+getName()+
		"[ena:"+isEnabled()+
		", vis:"+isVisible()+
		", err:"+hasError()+
		", exa:"+hasExample()+
		", lab:"+hasLabel();
	}
	
}
