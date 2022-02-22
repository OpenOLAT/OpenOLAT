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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ValidationStatus;

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
	private String i18n;
	private String cmd;
	private String url;
	private boolean hasCustomEnabledCss = false;
	private boolean hasCustomDisabledCss = false;
	private boolean domReplacementWrapperRequired = false;
	private boolean isPrimary = false;
	private boolean ownDirtyFormWarning = false;
	private boolean newWindow;
	private boolean newWindowAfterDispatchUrl;
	private boolean newWindowWithSubmit;
	private LinkPopupSettings popup;
	private String iconLeftCSS;
	private String iconRightCSS;
	private String customEnabledLinkCSS;
	private String customDisabledLinkCSS;
	private String title;	
	private String ariaLabel;
	private String ariaRole;
	private String textReasonForDisabling;
	private String target;

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
	
	@Override
	public Link getComponent() {
		return component;
	}

	@Override
	public void setDomReplacementWrapperRequired(boolean required) {
		this.domReplacementWrapperRequired = required;
		if(component != null) {
			component.setDomReplacementWrapperRequired(required);
		}
	}
	
	@Override
	public String getTextReasonForDisabling() {
		return textReasonForDisabling;
	}

	@Override
	public void setTextReasonForDisabling(String textReasonForDisabling) {
		this.textReasonForDisabling = textReasonForDisabling;
		if(component != null) {
			component.setTextReasonForDisabling(textReasonForDisabling);
		}
	}

	@Override
	public boolean isNewWindow() {
		return newWindow;
	}
	
	@Override
	public boolean isNewWindowAfterDispatchUrl() {
		return newWindowAfterDispatchUrl;
	}

	@Override
	public boolean isNewWindowWithSubmit() {
		return newWindowWithSubmit;
	}

	@Override
	public void setNewWindow(boolean openInNewWindow, boolean afterDispatchUrl, boolean withSubmit) {
		newWindow = openInNewWindow;
		newWindowWithSubmit = withSubmit;
		newWindowAfterDispatchUrl = afterDispatchUrl;
		if(component != null) {
			component.setNewWindow(openInNewWindow, afterDispatchUrl);
		}
	}

	@Override
	public boolean isPopup() {
		return popup != null;
	}

	@Override
	public LinkPopupSettings getPopup() {
		return popup;
	}

	@Override
	public void setPopup(LinkPopupSettings popup) {
		this.popup = popup;
		if(component != null) {
			component.setPopup(popup);
		}
	}

	public String getTarget() {
		return target;
	}

	@Override
	public void setTarget(String target) {
		this.target = target;
		if(component != null) {
			component.setTarget(target);
		}
	}

	@Override
	public boolean isForceOwnDirtyFormWarning() {
		return ownDirtyFormWarning;
	}

	@Override
	public void setForceOwnDirtyFormWarning(boolean warning) {
		ownDirtyFormWarning = warning;
		if(component != null) {
			component.setForceFlexiDirtyFormWarning(ownDirtyFormWarning);
		}
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
			component = new Link(null, name, cmd, i18n, presentation + Link.FLEXIBLEFORMLNK, this);
			if(customEnabledLinkCSS != null){
				component.setCustomEnabledLinkCSS(customEnabledLinkCSS);
			}
			if(customDisabledLinkCSS != null){
				component.setCustomDisabledLinkCSS(customDisabledLinkCSS);
			}
			if ((presentation - Link.FLEXIBLEFORMLNK - Link.NONTRANSLATED) >= 0) {
				// don't translate non-tranlated links
				component.setCustomDisplayText(i18n);					
			}
		} else {
			component = new Link(null, name, name, name,  Link.LINK + Link.FLEXIBLEFORMLNK, this);
			// set link text
			if ((presentation - Link.FLEXIBLEFORMLNK - Link.NONTRANSLATED) >= 0) {
				// don't translate non-tranlated links
				component.setCustomDisplayText(i18n);					
			} else {
				// translate other links
				if (StringHelper.containsNonWhitespace(i18n)) {
					component.setCustomDisplayText(getTranslator().translate(i18n));
				}
			}
		}
		//if enabled or not must be set now in case it was set during construction time
		component.setEnabled(isEnabled());
		component.setTranslator(getTranslator());
		component.setIconLeftCSS(iconLeftCSS);
		component.setIconRightCSS(iconRightCSS);
		component.setElementCssClass(getElementCssClass());
		component.setTitle(title);
		component.setPrimary(isPrimary);
		component.setAriaLabel(ariaLabel);
		component.setForceFlexiDirtyFormWarning(ownDirtyFormWarning);
		component.setPopup(popup);
		component.setNewWindow(newWindow, newWindowAfterDispatchUrl);
		component.setUrl(url);
		if(textReasonForDisabling != null) {
			component.setTextReasonForDisabling(textReasonForDisabling);
		}
		component.setDomReplacementWrapperRequired(domReplacementWrapperRequired);
		component.setFocus(super.hasFocus());
		if(StringHelper.containsNonWhitespace(getElementCssClass())) {
			component.setElementCssClass(getElementCssClass());
		}
		if (ariaRole != null) {
			component.setAriaRole(ariaRole);			
		} else if (presentation >= Link.BUTTON_XSMALL) {
			// set button role if button style is applied. Extract the link style
			int linkStyle = presentation;
			if ((linkStyle - Link.FLEXIBLEFORMLNK) >= 0) {
				linkStyle = linkStyle - Link.FLEXIBLEFORMLNK;
			}
			if ((linkStyle - Link.NONTRANSLATED) >= 0) {
				linkStyle = linkStyle - Link.NONTRANSLATED;
			}
			if (linkStyle >= Link.BUTTON_XSMALL && linkStyle <= Link.BUTTON_LARGE) {
				component.setAriaRole(Link.ARIA_ROLE_BUTTON);
			}
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
	public void validate(List<ValidationStatus> validationResults) {
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
	public void setUrl(String url) {
		this.url = url;
		if(component != null) {
			component.setUrl(url);
		}
	}

	@Override
	public void setTranslator(Translator translator) {
		if(component != null) {
			component.setTranslator(translator);
		}
		super.setTranslator(translator);
	}

	@Override
	public void setElementCssClass(String elementCssClass) {
		if(component != null) {
			component.setElementCssClass(elementCssClass);
		}
		super.setElementCssClass(elementCssClass);
	}

	@Override
	public void setIconLeftCSS(String iconCSS) {
		this.iconLeftCSS = iconCSS;
		if(component != null){
			component.setIconLeftCSS(iconCSS);
		}
	}

	@Override
	public void setIconRightCSS(String iconCSS) {
		this.iconRightCSS = iconCSS;
		if(component != null){
			component.setIconRightCSS(iconCSS);
		}
	}

	@Override
	public void setTitle(String linkTitle) {
		this.title = linkTitle;
		if(component != null){
			component.setTitle(linkTitle);
		}
	}

	@Override
	public void setAriaLabel(String label) {
		this.ariaLabel = label;
		if(component != null) {
			component.setAriaLabel(label);
		}
	}

	@Override
	public void setAriaRole(String role) {
		this.ariaRole = role;
		if(component != null) {
			component.setAriaRole(role);
		}
	}

	@Override
	public void setCustomEnabledLinkCSS(String customEnabledLinkCSS) {
		hasCustomEnabledCss=true;
		this.customEnabledLinkCSS = customEnabledLinkCSS;
		if(customEnabledLinkCSS != null && component != null){
			component.setCustomEnabledLinkCSS(customEnabledLinkCSS);
		}
	}

	@Override
	public void setCustomDisabledLinkCSS(String customDisabledLinkCSS) {
		hasCustomDisabledCss = true;
		this.customDisabledLinkCSS  = customDisabledLinkCSS;
		if(customDisabledLinkCSS != null && component != null){
			component.setCustomDisabledLinkCSS(customDisabledLinkCSS);
		}
	}

	@Override
	public String getI18nKey() {
		return i18n;
	}

	@Override
	public void setI18nKey(String i18n) {
		this.i18n = i18n;
		if (component != null) {
			if ((presentation - Link.NONTRANSLATED) >= 0) {
				// don't translate non-tranlated links
				component.setCustomDisplayText(i18n);					
			} else if (StringHelper.containsNonWhitespace(i18n)) {
				// translate other links
				component.setCustomDisplayText(getTranslator().translate(i18n));
			}
		}
	}
	
	@Override
	public void setI18nKey(String i18n, String[] args) {
		this.i18n = i18n;
		if (component != null) {
			if ((presentation - Link.NONTRANSLATED) >= 0) {
				// don't translate non-tranlated links
				component.setCustomDisplayText(i18n);					
			} else if (StringHelper.containsNonWhitespace(i18n)) {
				// translate other links
				component.setCustomDisplayText(getTranslator().translate(i18n, args));
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

	@Override
	public void setLinkTitle(String i18nKey) {
		if (component != null) {
			if (StringHelper.containsNonWhitespace(i18nKey)) {
				// translate other links
				component.setTitle(i18nKey);
			}
		}		
	}

	@Override
	public String getLinkTitleText() {
		String linkTitle = null;
		if (component != null) {
			linkTitle = component.getCustomDisplayText();
			if (linkTitle == null && getTranslator() != null) {
				if (StringHelper.containsNonWhitespace(component.getI18n())) {
					linkTitle = getTranslator().translate(component.getI18n());
				}
			}
		}
		return linkTitle;
	}

	@Override
	public void setActive(boolean isActive) {
		if (component != null) {
			component.setActive(isActive);
		}		
	}

	@Override
	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
		if (component != null) {
			component.setPrimary(isPrimary);
		}		
	}
	
	@Override
	public void setFocus(boolean hasFocus){
		if (component != null) {
			component.setFocus(hasFocus);
		}
		// set also on parent as fallback
		super.setFocus(hasFocus);
	}
	
	@Override
	public boolean hasFocus(){
		if (component != null) {
			return component.isFocus();
		} else {
			// fallback
			return super.hasFocus();			
		}
	}
	
	@Override
	public void setTooltip(String i18nTooltipKey) {
		component.setTooltip(i18nTooltipKey);
	}
}
