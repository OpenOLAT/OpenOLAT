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
package org.olat.core.gui.control.generic.wizard;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.StaticTextElementImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Util;

/**
 * Initial Date:  18.01.2008 <br>
 * @author patrickb
 */
public abstract class BasicStep implements Step {
	
	private Locale locale;
	private Identity identity;
	private Translator translator;
	private Step nextStep;
	private String i18nStepTitle;
	private String i18nStepDescription;
	private String[] i18nArguments;
	private StepCollection stepCollection;

	public BasicStep(UserRequest ureq){
		this.locale = ureq.getLocale();
		this.identity = ureq.getIdentity();
		this.translator = Util.createPackageTranslator(this.getClass(), locale);
		nextStep = Step.NOSTEP;
	}
	
	public void setTranslator(Translator translator) {
		this.translator = translator;
	}
	
	/**
	 * generates a StaticTextElement with i18n key defined, or returns null if 
	 * i18n key undefined.
	 * @see org.olat.core.gui.control.generic.wizard.Step#getStepShortDescription()
	 */
	@Override
	public FormItem getStepShortDescription(){
		if(i18nStepDescription == null){
			return null;
		}
		return new StaticTextElementImpl(i18nStepDescription, getTranslator().translate(i18nStepDescription));
	}

	/**
	 * generates FormLink with defined i18nKey, otherwise override and provide
	 * your own FormItem here.
	 * @see org.olat.core.gui.control.generic.wizard.Step#getStepTitle()
	 */
	@Override
	public FormItem getStepTitle(){
		if(i18nStepTitle == null){
			throw new AssertException("no i18n key set for step title, or getStepTitle() not overridden.");
		}
		FormLink fl;
		if(i18nArguments != null && i18nArguments.length > 0) {
			String title = this.getTranslator().translate(i18nStepTitle, i18nArguments);
			fl = new FormLinkImpl(CodeHelper.getUniqueID(), null, title, Link.FLEXIBLEFORMLNK + Link.NONTRANSLATED);
		} else {
			fl = new FormLinkImpl(CodeHelper.getUniqueID(), i18nStepTitle);
		}
		
		fl.setTranslator(getTranslator());
		return fl;
	}
	
	@Override
	public Step nextStep(){
		return nextStep;
	}
	
	
	protected Identity getIdentity(){
		return identity;
	}
	
	protected Translator getTranslator(){
		return translator;
	}

	protected Locale getLocale(){
		return locale;
	}	
	protected void setNextStep(Step nextStep){
		this.nextStep = nextStep;
	}
	
	protected void setI18nTitleAndDescr(String i18nKeyTitle, String i18nKeyDescription){
		this.i18nStepTitle = i18nKeyTitle;
		this.i18nStepDescription = i18nKeyDescription;
	}
	
	protected void setI18nTitleAndDescr(String i18nKeyTitle, String i18nKeyDescription, String[] i18nArguments){
		setI18nTitleAndDescr(i18nKeyTitle, i18nKeyDescription);
		this.i18nArguments = i18nArguments;
	}

	@Override
	public StepCollection getStepCollection() {
		return stepCollection;
	}

	public void setStepCollection(StepCollection stepCollection) {
		this.stepCollection = stepCollection;
	}
}
