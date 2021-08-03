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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.InlineElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.util.ValidationStatus;

/**
 * Description:<br>
 * The form basic controller acts as a facade for <tt>Flexi.Form</tt>
 * generation.
 * <ul>
 * <li>subclass it, and generate a constructor calling <tt>super(..)</tt></li>
 * <li>add also <tt>initForm(this.flc, this, ureq)</tt> to your constructor
 * as a last line</li>
 * <li>if you want your form layout, provide the velocity page name in the
 * <tt>super(..,name)</tt> call.</li>
 * <li>add your desired form elements in the <tt>initForm(..)</tt> method
 * implementaion</li>
 * <li>add your complex business validation logic by overriding the method
 * <tt>validateFormLogic(..)</tt></li>
 * <li>add your code to read form values and process them further in the
 * <tt>formOK(..)</tt> method implementation</li>
 * <li>in complex forms with subworkflows the <tt>formInnerEvent(..)</tt>
 * method can be overwritten and used the same way as known from
 * <tt>event(..)</tt>.</li>
 * </ul>
 * 
 * <P>
 * Initial Date: 01.02.2007 <br>
 * 
 * @author patrickb
 */
public abstract class FormBasicController extends BasicController {

	
	public static final int LAYOUT_DEFAULT = 0;
	public static final int LAYOUT_HORIZONTAL = 1;
	public static final int LAYOUT_VERTICAL = 2;
	public static final int LAYOUT_CUSTOM = 3;
	public static final int LAYOUT_BAREBONE = 4;
	public static final int LAYOUT_PANEL = 5;
	public static final int LAYOUT_DEFAULT_6_6 = 6;
	public static final int LAYOUT_DEFAULT_9_3 = 7;
	public static final int LAYOUT_DEFAULT_2_10 = 8;

	protected FormLayoutContainer flc;

	protected Form mainForm;

	protected StackedPanel initialPanel;

	protected final FormUIFactory uifactory = FormUIFactory.getInstance();
	
	public FormBasicController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, (String)null);
	}
	
	
	public FormBasicController(UserRequest ureq, WindowControl wControl, Translator fallbackTranslator) {
		this(ureq, wControl, null, null, fallbackTranslator);
	}

	public FormBasicController(UserRequest ureq, WindowControl wControl, String pageName) {
		this(ureq, wControl, null, pageName);
	}
	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param mainFormId Give a fix identifier to the main form for state-less behavior
	 * @param pageName
	 */
	public FormBasicController(UserRequest ureq, WindowControl wControl, String mainFormId, String pageName) {
		super(ureq, wControl);
		constructorInit(mainFormId, pageName);
	}

	/** pay attention when using this with stacked translators, they may get lost somehow!
	* until a translator-fix: use something like:
	* Translator pT = Util.createPackageTranslator(Alabla.class, ureq.getLocale(), getTranslator());
	* this.setTranslator(pT);
	*/
	public FormBasicController(UserRequest ureq, WindowControl wControl, String pageName, Translator fallbackTranslator) {
		this(ureq, wControl, null, pageName, fallbackTranslator);
	}
	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param mainFormId Give a fix identifier to the main form for state-less behavior
	 * @param pageName
	 * @param fallbackTranslator
	 */
	public FormBasicController(UserRequest ureq, WindowControl wControl, String mainFormId, String pageName, Translator fallbackTranslator) {
		super(ureq, wControl, fallbackTranslator);
		constructorInit(mainFormId, pageName);
	}
	

	protected FormBasicController(UserRequest ureq, WindowControl wControl, int layout) {
		this(ureq, wControl, null, layout);
	}

	protected FormBasicController(UserRequest ureq, WindowControl wControl, String mainFormId, int layout){
		super(ureq, wControl);
		if (layout == LAYOUT_HORIZONTAL) {
			// init with horizontal layout
			flc = FormLayoutContainer.createHorizontalFormLayout("ffo_horizontal", getTranslator());		
			mainForm = Form.create(mainFormId, "ffo_main_horizontal", flc, this);

		} else if (layout == LAYOUT_VERTICAL) {
			// init with vertical layout
			flc = FormLayoutContainer.createVerticalFormLayout("ffo_vertical", getTranslator());		
			mainForm = Form.create(mainFormId, "ffo_main_vertical", flc, this);

		} else if (layout == LAYOUT_BAREBONE) {
			// init with bare bone layout
			flc = FormLayoutContainer.createBareBoneFormLayout("ffo_barebone", getTranslator());		
			mainForm = Form.create(mainFormId, "ffo_main_barebone", flc, this);
			
		} else if (layout == LAYOUT_PANEL) {
			// init with panel layout
			flc = FormLayoutContainer.createPanelFormLayout("ffo_panel", getTranslator());		
			mainForm = Form.create(mainFormId, "ffo_main_panel", flc, this);
		} else if (layout == LAYOUT_DEFAULT_6_6) {
			flc = FormLayoutContainer.createDefaultFormLayout_6_6("ffo_default_6_6", getTranslator());
			mainForm = Form.create(mainFormId, "ffo_main_default_6_6", flc, this);
		} else if (layout == LAYOUT_DEFAULT_9_3) {
			// init with panel layout
			flc = FormLayoutContainer.createDefaultFormLayout_9_3("ffo_default_9_3", getTranslator());		
			mainForm = Form.create(mainFormId, "ffo_main_default_9_3", flc, this);
		} else if (layout == LAYOUT_DEFAULT_2_10) {
			// init with panel layout
			flc = FormLayoutContainer.createDefaultFormLayout_2_10("ffo_default_2_10", getTranslator());		
			mainForm = Form.create(mainFormId, "ffo_main_default_2_10", flc, this);
		} else if (layout == LAYOUT_CUSTOM) {
			throw new AssertException("Use another constructor to work with a custom layout!");

		} else {
			// init with default layout
			flc = FormLayoutContainer.createDefaultFormLayout("ffo_default", getTranslator());
			mainForm = Form.create(mainFormId, "ffo_main_default", flc, this);
		}
		initialPanel = putInitialPanel(mainForm.getInitialComponent());
	}

	protected FormBasicController(UserRequest ureq, WindowControl wControl, int layout, String customLayoutPageName, Form externalMainForm){
		super(ureq, wControl);
		if (layout == LAYOUT_HORIZONTAL) {
			// init with horizontal layout
			flc = FormLayoutContainer.createHorizontalFormLayout("ffo_horizontal", getTranslator());		

		} else if (layout == LAYOUT_VERTICAL) {
			// init with vertical layout
			flc = FormLayoutContainer.createVerticalFormLayout("ffo_vertical", getTranslator());		

		} else if (layout == LAYOUT_BAREBONE) {
			// init with vertical layout
			flc = FormLayoutContainer.createBareBoneFormLayout("ffo_barebone", getTranslator());		

		} else if (layout == LAYOUT_PANEL) {
			// init with panel layout
			flc = FormLayoutContainer.createPanelFormLayout("ffo_panel", getTranslator());		

		} else if (layout == LAYOUT_DEFAULT_6_6) {
			// init with default layout
			flc = FormLayoutContainer.createDefaultFormLayout_6_6("ffo_panel", getTranslator());
		
		} else if (layout == LAYOUT_DEFAULT_9_3) {
			// init with default layout
			flc = FormLayoutContainer.createDefaultFormLayout_9_3("ffo_panel", getTranslator());
		
		}  else if (layout == LAYOUT_DEFAULT_2_10) {
			// init with default layout
			flc = FormLayoutContainer.createDefaultFormLayout_2_10("ffo_panel", getTranslator());
		
		} else if (layout == LAYOUT_CUSTOM && customLayoutPageName != null) {
			// init with provided layout
			String vc_pageName = velocity_root + "/" + customLayoutPageName + ".html";
			flc = FormLayoutContainer.createCustomFormLayout("ffo_" + customLayoutPageName+this.hashCode(), getTranslator(), vc_pageName);

		} else {
			// init with default layout
			flc = FormLayoutContainer.createDefaultFormLayout("ffo_default", getTranslator());
		}
		//instead of the constructorInit's Form.create... use a supplied one
		mainForm = externalMainForm;
		flc.setRootForm(externalMainForm);
		mainForm.addSubFormListener(this);
		initialPanel = putInitialPanel(flc.getComponent());
	}
	
	
	/**
	 * should be rarely overwritten, only if you provide infrastructure around flexi forms
	 * @param mainFormId Give a fix identifier to the main form for state-less behavior
	 * @param pageName
	 */
	protected void constructorInit(String mainFormId, String pageName) {
		String ffo_pagename = null;
		if (pageName != null) {
			if(pageName.endsWith(".html")) {
				// init with provided layout
				ffo_pagename = "ffo_" + pageName.replace("/", "_");
				flc = FormLayoutContainer.createCustomFormLayout(ffo_pagename, getTranslator(), pageName);
			} else if(pageName.equals("form_horizontal")) {
					// init with provided layout
					ffo_pagename = "ffo_" + pageName.replace("/", "_");					
					flc = FormLayoutContainer.createHorizontalFormLayout(ffo_pagename, getTranslator());
			} else if(pageName.equals("form_vertical")) {
				// init with provided layout
				ffo_pagename = "ffo_" + pageName.replace("/", "_");
				flc = FormLayoutContainer.createVerticalFormLayout(ffo_pagename, getTranslator());
			} else {
				// init with provided layout
				String vc_pageName = velocity_root + "/" + pageName + ".html";
				ffo_pagename = "ffo_" + pageName;
				flc = FormLayoutContainer.createCustomFormLayout(ffo_pagename, getTranslator(), vc_pageName);
			}
		} else {
			// init with default layout
			ffo_pagename="ffo_default";
			flc = FormLayoutContainer.createDefaultFormLayout(ffo_pagename, getTranslator());
		}
		//
		mainForm = Form.create(mainFormId, "ffo_main_" + pageName, flc, this);
		/*
		 * implementor must call initFormElements(...)
		 */
		initialPanel = putInitialPanel(mainForm.getInitialComponent());
	}

	protected void initForm(UserRequest ureq) {
		initForm(this.flc, this, ureq);
	}

	/**
	 * The creation initialisation and adding form elements to layout happens
	 * here.<br>
	 * The method is not called automatically, but it should be the last line in
	 * your constructor.<br>
	 * 
	 * @param formLayout
	 * @param listener
	 * @param ureq
	 */
	abstract protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq);

	public FormItem getInitialFormItem() {
		return flc;
	}
	
	
	@Override
	protected void removeAsListenerAndDispose(Controller controller) {
		super.removeAsListenerAndDispose(controller);
		
		if(controller instanceof FormBasicController) {
			FormLayoutContainer container = ((FormBasicController) controller).flc;
			if(flc.getRootForm() == container.getRootForm()) {
				flc.getRootForm().removeSubFormListener((FormBasicController)controller);
			}
		}
	}
	
	
	/**
	 * The form first validates each element, then it calls the
	 * <tt>boolean validateFormLogic(ureq)</tt> from the listening controller.
	 * This gives the possibility to implement complex business logic checks.<br>
	 * In the case of valid elements and a valid form logic this method is called.
	 * Typically one will read and save/update values then.
	 */
	abstract protected void formOK(UserRequest ureq);
	
	protected void formNext(UserRequest ureq) {
		formOK(ureq);
	}
	
	protected void formFinish(UserRequest ureq) {
		formOK(ureq);
	}

	/**
	 * called if form validation was not ok.<br>
	 * default implementation does nothing. Each element is assumed to set
	 * errormessages. Needed only if complex business logic is checked even
	 * 
	 * @param ureq 
	 */
	protected void formNOK(UserRequest ureq) {
	// by default nothing to do -> e.g. looping until form is ok
	}

	/**
	 * Called when a form cancel button has been pressed. The form will
	 * automatically be resetted.
	 * 
	 * @param ureq
	 */
	protected void formCancelled(UserRequest ureq) {
		// by default nothing to do
	}
	
	/**
	 * called if an element was activated resetting the form elements to their
	 * initial values. After all resetting took place this method is called.
	 * 
	 * @param ureq
	 */
	protected void formResetted(UserRequest ureq) {
	// by default no cleanup is needed outside of the form after resetting
	}

	/**
	 * called if an element inside of the form triggered an event
	 * 
	 * @param ureq 
	 * @param source
	 * @param event
	 */
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
	// overwrite if you want to listen to inner form elements events
	}

	@Override
	protected void event(UserRequest ureq,Controller source, Event event) {
		super.event(ureq, source, event);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == mainForm.getInitialComponent()) {
			// general form events
			if (event == org.olat.core.gui.components.form.Form.EVNT_VALIDATION_OK) {
				// Set container dirty to remove potentially rendered error messages. Do
				// this before calling formOK() to let formOK override the dirtiness
				// flag
				flc.setDirty(true);
				formOK(ureq);
			} else if (event == org.olat.core.gui.components.form.Form.EVNT_VALIDATION_NEXT) {
				// Set container dirty to remove potentially rendered error messages. Do
				// this before calling formOK() to let formOK override the dirtiness
				// flag
				flc.setDirty(true);
				formNext(ureq);
			} else if (event == org.olat.core.gui.components.form.Form.EVNT_VALIDATION_FINISH) {
				// Set container dirty to remove potentially rendered error messages. Do
				// this before calling formOK() to let formOK override the dirtiness
				// flag
				flc.setDirty(true);
				formFinish(ureq);
			} else if (event == org.olat.core.gui.components.form.Form.EVNT_VALIDATION_NOK) {
				// Set container dirty to rendered error messages. Do this before calling
				// formNOK() to let formNOK override the dirtiness flag
				flc.setDirty(true);
				formNOK(ureq);
			} else if (event == FormEvent.RESET) {
				// Set container dirty to render everything from scratch, remove error
				// messages. Do this before calling
				// formResetted() to let formResetted override the dirtiness flag
				flc.setDirty(true);
				formResetted(ureq);
			} else if (event instanceof FormEvent) {
				FormEvent fe = (FormEvent) event;
				// Special case: cancel events are wrapped as form inner events
				if (fe.getCommand().equals(org.olat.core.gui.components.form.Form.EVNT_FORM_CANCELLED.getCommand())) {
					// Set container dirty to clear error messages. Do this before calling
					// formCancelled() to let formCancelled override the dirtiness flag
					flc.setDirty(true);
					formResetted(ureq);
					formCancelled(ureq);
					return;
				}				
				/*
				 * evaluate normal inner form events
				 */
				FormItem fiSrc = fe.getFormItemSource();
				propagateDirtinessToContainer(fiSrc, fe);
				//
				formInnerEvent(ureq, fiSrc, fe);
				// no need to set container dirty, up to controller code if something is dirty
			}
		}
	}
	
	protected void propagateDirtinessToContainer(FormItem fiSrc, @SuppressWarnings("unused") FormEvent fe) {
		// check for InlineElments remove as the tag library has been replaced
		if(fiSrc instanceof FormLink) {
			FormLink link = (FormLink)fiSrc;
			if(!link.isPopup() && !link.isNewWindow()) {
				flc.setDirty(true);
				// Trigger re-focusing of current focused element										
				JSCommand focusCommand = FormJSHelper.getFormFocusCommand(this.flc.getRootForm().getFormName(), null);						
				getWindowControl().getWindowBackOffice().sendCommandTo(focusCommand);
			}
		} else if(fiSrc instanceof InlineElement) {
			if(fiSrc instanceof RichTextElement) {
				// ignore
			} else if(!((InlineElement) fiSrc).isInlineEditingElement()) {
				//the container need to be redrawn because every form item element
				//is made of severals components. If a form item is set to invisible
				//the layout which glue the different components stay visible
				flc.setDirty(true);
				// Trigger re-focusing of current focused element
				JSCommand focusCommand = FormJSHelper.getFormFocusCommand(this.flc.getRootForm().getFormName(), null);						
				getWindowControl().getWindowBackOffice().sendCommandTo(focusCommand);
			}
		}
	}
	
	public String getAndRemoveFormTitle() {
		String title = (String)flc.contextGet("off_title");
		if(title != null) {
			flc.contextRemove("off_title");
		}
		return title;
	}

	/**
	 * Set an optional form title that is rendered as a fieldset legend. If you
	 * use a custom template this will have no effect
	 * 
	 * @param i18nKey
	 */
	protected void setFormTitle(String i18nKey) {
		if (i18nKey == null) {
			flc.contextRemove("off_title");
		} else {
			flc.contextPut("off_title", getTranslator().translate(i18nKey));
		}
	}
	
	/**
	 * Set an optional form title that is rendered as a fieldset legend. If you
	 * use a custom template this will have no effect
	 * 
	 * @param i18nKey
	 * @param args optional arguments
	 */
	protected void setFormTitle(String i18nKey, String[] args) {
		if (i18nKey == null) {
			flc.contextRemove("off_title");
		} else {
			flc.contextPut("off_title", getTranslator().translate(i18nKey, args));
		}
	}
	
	protected void setFormTranslatedTitle(String translatedTitle) {
		if(translatedTitle == null) {
			flc.contextRemove("off_title");
		} else {
			flc.contextPut("off_title", translatedTitle);
		}
	}
	
	/**
	 * Set an optional icon by giving the necessary css classes
	 * @param iconCss 
	 */
	protected void setFormTitleIconCss(String iconCss) {
		if (iconCss == null) {
			flc.contextRemove("off_icon");
		} else {
			flc.contextPut("off_icon", iconCss);
		}
	}

	/**
	 * Set an optional description. This will appear above the form. If you use a
	 * custom template this will have no effect
	 * 
	 * @param i18nKey
	 */
	protected void setFormDescription(String i18nKey) {
		if (i18nKey == null) {
			flc.contextRemove("off_desc");
		} else {
			flc.contextPut("off_desc", getTranslator().translate(i18nKey));
		}
	}

	/**
	 * Set an optional description. This will appear above the form. If you use a
	 * custom template this will have no effect
	 * 
	 * @param i18nKey
	 * @args args optional arguments
	 */
	protected void setFormDescription(String i18nKey, String[] args) {
		if (i18nKey == null) {
			flc.contextRemove("off_desc");
		} else {
			flc.contextPut("off_desc", getTranslator().translate(i18nKey, args));
		}
	}
	
	protected void setFormTranslatedDescription(String translatedDescription) {
		if (translatedDescription == null) {
			flc.contextRemove("off_desc");
		} else {
			flc.contextPut("off_desc", translatedDescription);
		}
	}

	/**
	 * Set a warning. This will appear before the form, after the description.
	 * If you use a custom template this will have no effect
	 * 
	 * @param i18nKey
	 * @args args optional arguments
	 */
	protected void setFormWarning (String i18nKey, String[] args) {
		if (i18nKey == null) {
			flc.contextRemove("off_warn");
		} else {
			flc.contextPut("off_warn", getTranslator().translate(i18nKey, args));
		}
	}
	
	/**
	 * Set a warning. This will appear before the form, after the description.
	 * If you use a custom template this will have no effect
	 * 
	 * @param i18nKey
	 */
	protected void setFormWarning (String i18nKey) {
		this.flc.contextRemove("off_info");
		if (i18nKey == null) {
			flc.contextRemove("off_warn");
		} else {
			String val = getTranslator().translate(i18nKey);
			flc.contextPut("off_warn", val);
		}
	}
	
	/**
	 * Set a warning. This will appear before the form, after the description.
	 * If you use a custom template this will have no effect
	 * 
	 * @param translatedWarning The warning
	 */
	protected void setFormTranslatedWarning(String translatedWarning) {
		if (translatedWarning == null) {
			flc.contextRemove("off_warn");
		} else {
			flc.contextPut("off_warn", translatedWarning);
		}
	}
	
	/**
	 * Set a message to be displayed in the form, after the description.
	 * The form warning, if there is one, will be removed.
	 * @see org.olat.core.gui.control.controller.BasicController#showInfo(java.lang.String)
	 */
	
	public void setFormInfo (String i18nKey) {
		flc.contextRemove("off_warn");
		if (i18nKey == null) {
			flc.contextRemove("off_info");
		} else {
			flc.contextPut("off_info", getTranslator().translate(i18nKey));
		}
	}
	
	/**
	 * Set a message to be displayed in the form, after the description.
	 * The form warning, if there is one, will be removed.
	 * @see org.olat.core.gui.control.controller.BasicController#showInfo(java.lang.String)
	 */
	
	protected void setFormInfo (String i18nKey, String[] args) {
		flc.contextRemove("off_warn");
		if (i18nKey == null) {
			flc.contextRemove("off_info");
		} else {
			flc.contextPut("off_info", getTranslator().translate(i18nKey, args));
		}
	}
	
	protected void setFormContextHelp(String url) {
		if (url == null) {
			flc.contextRemove("off_chelp_url");
		} else {
			flc.contextPut("off_chelp_url", url);
		}
	}
	
	/**
	 * Set an optional css class to use for this form. May help to achieve custom formatting without
	 * a separate velocity container.
	 * 
	 * @param cssClassName the css class name to wrap around form
	 */
	protected void setFormStyle(String cssClassName){
		if (cssClassName == null){
			flc.contextRemove("off_css_class");
		} else {
			flc.contextPut("off_css_class", cssClassName);
		}
	}

	@Override
	protected void setTranslator(Translator translator) {
		super.setTranslator(translator);
		flc.setTranslator(translator);
	}

	/**
	 * @param ureq
	 * @return
	 */
	protected boolean validateFormLogic(UserRequest ureq) {
		return true;
	}
	
	protected boolean validateFormItem(FormItem item) {
		if(!item.isEnabled() || !item.isVisible()) return true;
		List<ValidationStatus> validationResults = new ArrayList<>(2);
		item.validate(validationResults);
		return validationResults.isEmpty();
	}

	/**
	 * Adding disposing of form items that implement the disposable interface.
	 * Some Form items might generate temporary data that needs to be cleaned
	 * up.
	 * <p>
	 * First, super.dispose() is called.
	 * 
	 * @see org.olat.core.gui.control.DefaultController#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		ThreadLocalUserActivityLoggerInstaller.runWithUserActivityLogger(() -> {
			// Dispose also disposable form items (such as file uploads that needs to
			// cleanup temporary files)
			for (FormItem formItem : FormBasicController.this.flc.getFormItems()) {
				if (formItem instanceof Disposable) {
					Disposable disposableFormItem = (Disposable) formItem;
					disposableFormItem.dispose();				
				}
			}
		}, getUserActivityLogger());
	}
}