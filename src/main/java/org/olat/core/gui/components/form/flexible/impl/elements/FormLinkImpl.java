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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.link.FormLinkFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Event;
import org.olat.core.util.StringHelper;

/**
 * Description:<br>
 * Links / Buttons within the flexi form have to be form items. This implementation
 * of the {@link FormLink} interface adapts the {@link Link} component from the
 * GUI Framework to the flexi form substructure.<p>
 * implementation note:<br>
 * although a form link is created by <code>new ...</code> the corresponding
 * {@link Link} creation is deferred until the root form is available. E.g. this
 * is typically the case when the form link is added to a layout or form.
 * 
 * 
 * <P>
 * Initial Date:  10.12.2006 <br>
 * @author patrickb
 */
public class FormLinkImpl extends FormItemImpl implements FormLink {

	private Link component;
	private int presentation = Link.LINK;
	private String i18n = null;
	private String cmd = null;
	private boolean hasCustomEnabledCss = false;
	private boolean hasCustomDisabledCss = false;
	private String customEnabledLinkCSS = null;
	private String customDisabledLinkCSS = null;

	/**
	 * creates a form link with the given name which acts also as command, i18n
	 * and component name.
	 * @param name
	 */
	public FormLinkImpl(String name) {
		super(name);
	}

	/**
	 * creates a form link with a given name and a i18n key. The name is used also
	 * as component name and as command string.
	 * @param name
	 * @param i18n
	 */
	public FormLinkImpl(String name, String i18n){
		this(name);
		this.i18n = i18n;
	}
	
	/**
	 * creates a form link with a different representation as they are defined
	 * by {@link Link#BUTTON} {@link Link#BUTTON_SMALL} {@link Link#BUTTON_XSMALL} {@link Link#LINK_BACK} etc.
	 * @param name
	 * @param cmd
	 * @param i18n
	 * @param presentation
	 */
	public FormLinkImpl(String name, String cmd, String i18n, int presentation){
		this(name, i18n);
		hasCustomEnabledCss = true;
		this.cmd = cmd;
		this.presentation = presentation;
	}
	
	/*
	 * uses the FormLinkFactory to create the link associated with this formlink
	 * it is deferred to have the translator, and most of all the form id where
	 * the link must dispatch to. 
	 */
	@Override
	protected void rootFormAvailable() {
		// create component if we have the root form
		String name = getName();
		cmd = cmd == null ? name : cmd;
		i18n = i18n == null ? name : i18n;
		if(hasCustomEnabledCss || hasCustomDisabledCss){
			this.component = FormLinkFactory.createCustomFormLink(name, cmd, i18n, presentation, this.getRootForm());
			if(customEnabledLinkCSS != null){
				this.component.setCustomEnabledLinkCSS(customEnabledLinkCSS);
			}
			if(customDisabledLinkCSS != null){
				this.component.setCustomDisabledLinkCSS(customDisabledLinkCSS);
			}
			if ((presentation - Link.FLEXIBLEFORMLNK - Link.NONTRANSLATED) >= 0) {
				// don't translate non-tranlated links
				this.component.setCustomDisplayText(i18n);					
			}
		} else {
			this.component = FormLinkFactory.createFormLink(name, this.getRootForm());
			// set link text
			if ((presentation - Link.FLEXIBLEFORMLNK - Link.NONTRANSLATED) >= 0) {
				// don't translate non-tranlated links
				this.component.setCustomDisplayText(i18n);					
			} else {
				// translate other links
				if (StringHelper.containsNonWhitespace(i18n)) {
					this.component.setCustomDisplayText(getTranslator().translate(i18n));
				}
			}
		}
		//if enabled or not must be set now in case it was set during construction time
		this.component.setEnabled(isEnabled());
	}
	
	@Override
	public void dispatchFormRequest(UserRequest ureq) {
		getRootForm().fireFormEvent(ureq, new FormEvent(Event.DONE_EVENT, this, FormEvent.ONCLICK));
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		// a link has no data to remember
	}

	@Override
	public void validate(List validationResults) {
		// typically a link does not validate its data
	}
	
	@Override
	public void reset() {
		// a link can not be resetted
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	public String getCmd() {
		return cmd;
	}

	@Override
	public void setElementCssClass(String elementCssClass) {
		component.setElementCssClass(elementCssClass);
		super.setElementCssClass(elementCssClass);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FormLink#setCustomEnabledLinkCSS(java.lang.String)
	 */
	public void setCustomEnabledLinkCSS(String customEnabledLinkCSS) {
		hasCustomEnabledCss=true;
		this.customEnabledLinkCSS = customEnabledLinkCSS;
		if(customEnabledLinkCSS != null && this.component != null){
			this.component.setCustomEnabledLinkCSS(customEnabledLinkCSS);
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FormLink#setCustomDisabledLinkCSS(java.lang.String)
	 */
	public void setCustomDisabledLinkCSS(String customDisabledLinkCSS) {
		hasCustomDisabledCss = true;
		this.customDisabledLinkCSS  = customDisabledLinkCSS;
		if(customDisabledLinkCSS != null && this.component != null){
			this.component.setCustomDisabledLinkCSS(customDisabledLinkCSS);
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FormLink#setI18nKey(java.lang.String)
	 */
	public void setI18nKey(String i18n) {
		this.i18n = i18n;
		if (this.component != null) {
			if ((presentation - Link.FLEXIBLEFORMLNK - Link.NONTRANSLATED) >= 0) {
				// don't translate non-tranlated links
				this.component.setCustomDisplayText(i18n);					
			} else {
				// translate other links
				if (StringHelper.containsNonWhitespace(i18n)) {
					this.component.setCustomDisplayText(getTranslator().translate(i18n));
				}
			}
		}
	}

	@Override
	protected boolean translateLabel() {
		if (presentation==Link.NONTRANSLATED ||
				(presentation==(Link.NONTRANSLATED + Link.FLEXIBLEFORMLNK))) {
			return false;
		} else {
			return true;
		}
	}
}
