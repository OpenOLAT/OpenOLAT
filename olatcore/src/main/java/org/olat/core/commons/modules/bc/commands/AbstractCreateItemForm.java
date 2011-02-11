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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.core.commons.modules.bc.commands;

import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.Reset;
import org.olat.core.gui.components.form.flexible.elements.Submit;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormReset;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;

/**
 * Provides the Form elements (a mandatory textElement with a submit and a reset button), the layout, and contains the itemName. <p>
 * The validateFormLogic and formOK impl are the responsability of the subclasses.
 * 
 * @author Lavinia Dumitrescu
 *
 */
public abstract class AbstractCreateItemForm extends FormBasicController {
	
	protected TextElement textElement;
	protected Submit createFile;
	protected Reset reset;
	private String itemName;
	
	protected static String TEXT_ELEM_I18N_KEY = "TEXT_ELEM_I18N_KEY";
	protected static String SUBMIT_ELEM_I18N_KEY = "SUBMIT_ELEM_I18N_KEY";
	protected static String RESET_ELEM_I18N_KEY = "RESET_ELEM_I18N_KEY";
	
	private Map<String,String> i18nkeyMap;
	private static final int MAX_NAME_LENGTH = -1;
		
	public AbstractCreateItemForm(UserRequest ureq, WindowControl wControl, Translator translator, Map<String,String> i18nkeyMap) {		
		super(ureq, wControl);
					
		this.i18nkeyMap = i18nkeyMap;
		setTranslator(translator);
				
		initForm(ureq);		
	}					
	
	/**
	 * Use this constructor ONLY if the initForm is overriden in the subclass.
	 * @param ureq
	 * @param wControl
	 * @param translator
	 */
	public AbstractCreateItemForm(UserRequest ureq, WindowControl wControl, Translator translator) {
		this(ureq, wControl, translator, null);
	}
	

	@Override
	protected void doDispose() {
					//
	}

	@Override
	protected abstract boolean validateFormLogic(UserRequest ureq); 
	
	@Override
	protected abstract void formOK(UserRequest ureq); 
	
	@Override
	protected void formResetted(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);      
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {						
		textElement = uifactory.addTextElement("fileName", i18nkeyMap.get(TEXT_ELEM_I18N_KEY), MAX_NAME_LENGTH, "", formLayout);
		// set appropriate display length
		textElement.setDisplaySize(20);
		textElement.setMandatory(true);
		
		FormLayoutContainer formButtons = FormLayoutContainer.createHorizontalFormLayout("formButton", getTranslator());
		formLayout.add(formButtons);
		createFile = new FormSubmit("submit",i18nkeyMap.get(SUBMIT_ELEM_I18N_KEY));
		formButtons.add(createFile);
		reset = new FormReset("reset",i18nkeyMap.get(RESET_ELEM_I18N_KEY));
		formButtons.add(reset);			
	}	

	protected String getItemName() {
		return itemName;
	}

	protected void setItemName(String itemName) {
		this.itemName = itemName;
	}

}
