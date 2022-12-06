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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.translator.Translator;
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
	
	private String i18n;
	private final Link component;
	private final int presentation;
	private boolean ownDirtyFormWarning;
	private boolean newWindowWithSubmit;

	/**
	 * creates a form link with the given name which acts also as command, i18n
	 * and component name.
	 * @param name
	 */
	public FormLinkImpl(String name) {
		this(name, null, null, Link.LINK);
	}

	/**
	 * creates a form link with a given name and a i18n key. The name is used also
	 * as component name and as command string.
	 * @param name
	 * @param i18n
	 */
	public FormLinkImpl(String name, String i18n){
		this(name, null, i18n, Link.LINK);
	}
	
	/**
	 * creates a form link with a different representation as they are defined
	 * by {@link Link#BUTTON} {@link Link#BUTTON_SMALL} {@link Link#BUTTON_XSMALL} {@link Link#LINK_BACK} etc.
	 * @param name
	 * @param cmd
	 * @param i18n
	 * @param presentation
	 */
	public FormLinkImpl(String name, String cmd, String i18n, int presentation) {
		super(name);
		this.i18n = i18n;
		this.presentation = presentation;
		cmd = cmd == null ? name : cmd;
		i18n = i18n == null ? name : i18n;
		component = new Link(null, name, cmd, i18n, presentation + Link.FLEXIBLEFORMLNK, this);
	}
	
	@Override
	public Link getComponent() {
		return component;
	}

	@Override
	public void setDomReplacementWrapperRequired(boolean required) {
		component.setDomReplacementWrapperRequired(required);
	}
	
	@Override
	public String getTextReasonForDisabling() {
		return component.getTextReasonForDisabling();
	}

	@Override
	public void setTextReasonForDisabling(String textReasonForDisabling) {
		component.setTextReasonForDisabling(textReasonForDisabling);
	}

	@Override
	public boolean isNewWindow() {
		return component.isNewWindow();
	}
	
	@Override
	public boolean isNewWindowAfterDispatchUrl() {
		return component.isNewWindowAfterDispatchUrl();
	}

	@Override
	public boolean isNewWindowWithSubmit() {
		return newWindowWithSubmit;
	}

	@Override
	public void setNewWindow(boolean openInNewWindow, boolean afterDispatchUrl, boolean withSubmit) {
		newWindowWithSubmit = withSubmit;
		component.setNewWindow(openInNewWindow, afterDispatchUrl);
	}

	@Override
	public boolean isPopup() {
		return component.isPopup();
	}

	@Override
	public LinkPopupSettings getPopup() {
		return component.getPopup();
	}

	@Override
	public void setPopup(LinkPopupSettings popup) {
		component.setPopup(popup);
	}

	public String getTarget() {
		return component.getTarget();
	}

	@Override
	public void setTarget(String target) {
		component.setTarget(target);
	}

	@Override
	public boolean isForceOwnDirtyFormWarning() {
		return ownDirtyFormWarning;
	}

	@Override
	public void setForceOwnDirtyFormWarning(boolean warning) {
		ownDirtyFormWarning = warning;
		component.setForceFlexiDirtyFormWarning(ownDirtyFormWarning);
	}

	/*
	 * uses the FormLinkFactory to create the link associated with this formlink
	 * it is deferred to have the translator, and most of all the form id where
	 * the link must dispatch to. 
	 */
	@Override
	protected void rootFormAvailable() {
		// set link text
		if ((presentation - Link.NONTRANSLATED) >= 0) {
			component.setCustomDisplayText(i18n);					
		} else if (StringHelper.containsNonWhitespace(i18n)) {
			component.setCustomDisplayText(getTranslator().translate(i18n));
		}
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
	public void reset() {
		// a link can not be resetted
	}

	@Override
	protected Link getFormItemComponent() {
		return component;
	}

	@Override
	public String getCmd() {
		return component.getCommand();
	}
	
	@Override
	public void setUrl(String url) {
		component.setUrl(url);
	}

	@Override
	public void setTranslator(Translator translator) {
		component.setTranslator(translator);
		super.setTranslator(translator);
	}

	@Override
	public void setElementCssClass(String elementCssClass) {
		component.setElementCssClass(elementCssClass);
		super.setElementCssClass(elementCssClass);
	}

	@Override
	public void setIconLeftCSS(String iconCSS) {
		component.setIconLeftCSS(iconCSS);
	}

	@Override
	public void setIconRightCSS(String iconCSS) {
		component.setIconRightCSS(iconCSS);
	}

	@Override
	public void setTitle(String linkTitle) {
		component.setTitle(linkTitle);
	}

	@Override
	public void setAriaLabel(String label) {
		component.setAriaLabel(label);
	}

	@Override
	public void setAriaRole(String role) {
		component.setAriaRole(role);
	}

	@Override
	public void setCustomEnabledLinkCSS(String customEnabledLinkCSS) {
		component.setCustomEnabledLinkCSS(customEnabledLinkCSS);
	}

	@Override
	public void setCustomDisabledLinkCSS(String customDisabledLinkCSS) {
		component.setCustomDisabledLinkCSS(customDisabledLinkCSS);
	}

	@Override
	public String getI18nKey() {
		return i18n;
	}
	
	@Override
	public void setI18nKey(String i18n, String... args) {
		this.i18n = i18n;
		if ((presentation - Link.NONTRANSLATED) >= 0) {
			// don't translate non-tranlated links
			component.setCustomDisplayText(i18n);					
		} else if (StringHelper.containsNonWhitespace(i18n) && getTranslator() != null) {
			// translate other links
			component.setCustomDisplayText(getTranslator().translate(i18n, args));
		}
	}

	@Override
	protected boolean translateLabel() {
		if (presentation==Link.NONTRANSLATED || (presentation==(Link.NONTRANSLATED + Link.FLEXIBLEFORMLNK))) {
			return false;
		}
		return true;
	}

	@Override
	public void setLinkTitle(String i18nKey) {
		if (StringHelper.containsNonWhitespace(i18nKey)) {
			// translate other links
			component.setTitle(i18nKey);
		}	
	}

	@Override
	public String getLinkTitleText() {
		String linkTitle = component.getCustomDisplayText();
		if (linkTitle == null && getTranslator() != null
				&& StringHelper.containsNonWhitespace(component.getI18n())) {
			linkTitle = getTranslator().translate(component.getI18n());
		}
		return linkTitle;
	}

	@Override
	public void setActive(boolean isActive) {
		component.setActive(isActive);	
	}

	@Override
	public void setPrimary(boolean isPrimary) {
		component.setPrimary(isPrimary);
	}

	@Override
	public void setGhost(boolean ghost) {
		component.setGhost(ghost);
	}
	
	@Override
	public void setFocus(boolean hasFocus){
		component.setFocus(hasFocus);
		// set also on parent as fallback
		super.setFocus(hasFocus);
	}
	
	@Override
	public boolean hasFocus(){
		if (component != null) {
			return component.isFocus();
		}
		// fallback
		return super.hasFocus();			
	}
	
	@Override
	public void setTooltip(String i18nTooltipKey) {
		component.setTooltip(i18nTooltipKey);
	}
}
