/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.commons.controllers.impressum;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.olat.core.commons.controllers.impressum.ImpressumModule.Position;
import org.olat.core.commons.editor.htmleditor.HTMLEditorController;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.EmailAddressValidator;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImpressumAdminController extends FormBasicController {
	
	private static final String[] positionKeys = new String[]{ Position.top.name(), Position.footer.name() };
	
	private SingleSelection positionEl;
	private MultipleSelectionElement enableEl;
	private MultipleSelectionElement contactEnableEl;
	private TextElement contactMailEl;
	private FormLayoutContainer termsCont;
	private FormLayoutContainer impressumCont;
	private FormLayoutContainer dataPrivacyPolicyCont;
	private FormSubmit formSubmit;

	private CloseableModalController cmc;
	private HTMLEditorController editorCtrl;
	
	private final VFSContainer impressumDir;
	private final VFSContainer termsOfUseDir;
	private final VFSContainer dataPrivacyPolicyDir;
	
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private ImpressumModule impressumModule;
	
	public ImpressumAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		impressumDir = new LocalFolderImpl(impressumModule.getImpressumDirectory());
		termsOfUseDir = new LocalFolderImpl(impressumModule.getTermsOfUseDirectory());
		dataPrivacyPolicyDir = new LocalFolderImpl(impressumModule.getPrivacyPolicyDirectory());
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("menu.impressum");
		setFormDescription("config.hint");
		boolean enabled = impressumModule.isEnabled();
		boolean contactEnabled = impressumModule.isContactEnabled();
		
		String[] enableKeys = new String[]{ "on" };
		enableEl = uifactory.addCheckboxesHorizontal("enable", "enable.impressum", formLayout,
				enableKeys, new String[]{ translate("enable") });
		enableEl.addActionListener(FormEvent.ONCHANGE);
		enableEl.select(enableKeys[0], enabled);
		
		String[] positionValues = new String[]{ translate("position.top"), translate("position.footer") };
		positionEl = uifactory.addDropdownSingleselect("position", "position", formLayout, positionKeys, positionValues, null);
		positionEl.addActionListener(FormEvent.ONCHANGE);
		positionEl.setVisible(enabled);
		if(impressumModule.getPosition() != null) {
			switch(impressumModule.getPosition()) {
				case top: positionEl.select(positionKeys[0], true); break;
				case footer: positionEl.select(positionKeys[1], true); break;
				default:{}
			}
		}
		
		impressumCont = FormLayoutContainer.createCustomFormLayout("impressums", getTranslator(), velocity_root + "/buttongroups.html");
		impressumCont.setLabel("impressum.file", null);
		impressumCont.setVisible(enabled);
		formLayout.add(impressumCont);
		
		List<ButtonGroup> impressumButtons = new ArrayList<>();
		impressumCont.contextPut("buttons", impressumButtons);
		
		for(String lang:i18nModule.getEnabledLanguageKeys()) {
			FormLink editLink = uifactory
					.addFormLink("impressum." + lang, "impressum", getTranslated(lang), "impressum.file", impressumCont, Link.BUTTON | Link.NONTRANSLATED);
			editLink.setLabel(null, null);
			String filePath = "index_" + lang + ".html";
			boolean hasImpressum = checkContent(impressumDir.resolve(filePath));
			if(hasImpressum) {
				editLink.setIconLeftCSS("o_icon o_icon_check");	
			}
			
			FormLink deleteLink = uifactory
					.addFormLink("impressum.del." + lang, "delete-impressum", "", "impressum.file", impressumCont, Link.BUTTON | Link.NONTRANSLATED);
			deleteLink.setLabel(null, null);
			deleteLink.setIconLeftCSS("o_icon o_icon_delete_item");
			deleteLink.setVisible(hasImpressum);

			ButtonGroup group = new ButtonGroup(lang, editLink, deleteLink);
			editLink.setUserObject(group);
			deleteLink.setUserObject(group);
			impressumButtons.add(group);
		}
		
		termsCont = FormLayoutContainer.createCustomFormLayout("terms", getTranslator(), velocity_root + "/buttongroups.html");
		termsCont.setLabel("termofuse.file", null);
		termsCont.setVisible(enabled);
		formLayout.add(termsCont);
		
		List<ButtonGroup> termsOfUseButtons = new ArrayList<>();
		termsCont.contextPut("buttons", termsOfUseButtons);
		
		for(String lang:i18nModule.getEnabledLanguageKeys()) {
			FormLink editLink = uifactory.addFormLink("termofuser." + lang, "termsofuse", getTranslated(lang), "termofuse.file", termsCont, Link.BUTTON | Link.NONTRANSLATED);
			editLink.setLabel(null, null);
			String filePath = "index_" + lang + ".html";
			boolean hasTermsOfUse = checkContent(termsOfUseDir.resolve(filePath));
			if(hasTermsOfUse) {
				editLink.setIconLeftCSS("o_icon o_icon_check");
			}
			
			FormLink deleteLink = uifactory
					.addFormLink("impressum.del." + lang, "delete-termsofuse", "", "termofuse.file", termsCont, Link.BUTTON | Link.NONTRANSLATED);
			deleteLink.setLabel(null, null);
			deleteLink.setIconLeftCSS("o_icon o_icon_delete_item");
			deleteLink.setVisible(hasTermsOfUse);

			ButtonGroup group = new ButtonGroup(lang, editLink, deleteLink);
			editLink.setUserObject(group);
			deleteLink.setUserObject(group);
			termsOfUseButtons.add(group);
		}
		
		dataPrivacyPolicyCont = FormLayoutContainer.createCustomFormLayout("dataprivacies", getTranslator(), velocity_root + "/buttongroups.html");
		dataPrivacyPolicyCont.setLabel("dataprivacy.file", null);
		dataPrivacyPolicyCont.setVisible(enabled);
		formLayout.add(dataPrivacyPolicyCont);
		
		List<ButtonGroup> dataPrivacyPolicyButtons = new ArrayList<>();
		dataPrivacyPolicyCont.contextPut("buttons", dataPrivacyPolicyButtons);
		
		for(String lang:i18nModule.getEnabledLanguageKeys()) {
			FormLink editLink = uifactory.addFormLink("dataprivacy." + lang, "dataprivacy", getTranslated(lang), "dataprivacy.file", dataPrivacyPolicyCont, Link.BUTTON | Link.NONTRANSLATED);
			editLink.setLabel(null, null);
			String filePath = "index_" + lang + ".html";
			boolean hasDataPrivacyPolicy = checkContent(dataPrivacyPolicyDir.resolve(filePath));
			if(hasDataPrivacyPolicy) {
				editLink.setIconLeftCSS("o_icon o_icon_check");
			}
			
			FormLink deleteLink = uifactory
					.addFormLink("impressum.del." + lang, "delete-dataprivacy", "", "dataprivacy.file", dataPrivacyPolicyCont, Link.BUTTON | Link.NONTRANSLATED);
			deleteLink.setLabel(null, null);
			deleteLink.setIconLeftCSS("o_icon o_icon_delete_item");
			deleteLink.setVisible(hasDataPrivacyPolicy);

			ButtonGroup group = new ButtonGroup(lang, editLink, deleteLink);
			editLink.setUserObject(group);
			deleteLink.setUserObject(group);
			dataPrivacyPolicyButtons.add(group);
		}
		
		uifactory.addSpacerElement("spacer", formLayout, true);
		uifactory.addSpacerElement("spacer_line", formLayout, false);
		
		contactEnableEl = uifactory.addCheckboxesHorizontal("contactenable", "enable.contact", formLayout,
				enableKeys, new String[]{ translate("enable") });
		contactEnableEl.addActionListener(FormEvent.ONCHANGE);
		contactEnableEl.select(enableKeys[0], contactEnabled);
		contactEnableEl.setVisible(enabled);
		
		contactMailEl = uifactory.addTextElement("contact.mail", 255, impressumModule.getContactMail(), formLayout);
		contactMailEl.setVisible(contactEnabled && enabled);
		contactMailEl.setMandatory(contactEnabled && enabled);
		
		formSubmit = uifactory.addFormSubmitButton("submit", formLayout);
		formSubmit.setVisible(contactEnabled && enabled);
	}
	
	private String getTranslated(String lang) {
		String langName;
		Locale locale = i18nManager.getLocaleOrNull(lang);
		if(locale != null) {
			langName = locale.getDisplayName(getLocale());
		} else {
			langName = lang;
		}
		return langName;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (contactEnableEl.getSelectedKeys().contains("on")) {
			impressumModule.setContactEnabled(true);
			impressumModule.setContactMail(contactMailEl.getValue());
		} else {
			impressumModule.setContactEnabled(false);
		}		
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk = validateTextInput(contactMailEl, contactMailEl.getMaxLength(), true);
		
		return allOk;
	}
	
	private boolean validateTextInput(TextElement textElement, int lenght, boolean isMail) {		
		textElement.clearError();
		if(StringHelper.containsNonWhitespace(textElement.getValue())) {
			if (isMail) {
				if (!EmailAddressValidator.isValidEmailAddress(textElement.getValue())) {
					textElement.setErrorKey("input.wrong.mail", null);
					return false;
				}
			} if(lenght != -1 && textElement.getValue().length() > lenght) {
				textElement.setErrorKey("input.toolong", new String[]{ String.valueOf(lenght) });
				return false;
			}
		} else if (textElement.isMandatory()) {
			textElement.setErrorKey("form.legende.mandatory", null);
			return false;
		}

		return true;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			boolean enabled = enableEl.isAtLeastSelected(1);
			boolean contactEnabled = impressumModule.isContactEnabled();
			impressumModule.setEnabled(enabled);
			
			positionEl.setVisible(enabled);
			termsCont.setVisible(enabled);
			impressumCont.setVisible(enabled);
			dataPrivacyPolicyCont.setVisible(enabled);
			contactEnableEl.setVisible(enabled);
			contactEnableEl.select("on", contactEnabled);
			contactMailEl.setVisible(contactEnabled && enabled);
			contactMailEl.setMandatory(contactEnabled && enabled);
			formSubmit.setVisible(enabled && contactEnabled);
			
			getWindowControl().getWindowBackOffice().getWindow().setDirty(true);
			getWindowControl().getWindowBackOffice().getChiefController().wishReload(ureq, true);
		} else if(positionEl == source) {
			if(positionEl.isOneSelected()) {
				String key = positionEl.getSelectedKey();
				impressumModule.setPosition(key);
				getWindowControl().getWindowBackOffice().getWindow().setDirty(true);
				getWindowControl().getWindowBackOffice().getChiefController().wishReload(ureq, true);
			}
		} else if (contactEnableEl == source) {
			boolean contactEnabled = contactEnableEl.isAtLeastSelected(1);
			impressumModule.setContactEnabled(contactEnabled);
			
			contactMailEl.setVisible(contactEnabled);
			contactMailEl.setMandatory(contactEnabled);
			formSubmit.setVisible(contactEnabled);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			ButtonGroup group = (ButtonGroup)source.getUserObject();
			String lang = group.getLang();
			if("impressum".equals(cmd)) {
				doEdit(ureq, link, impressumDir, lang);
			} else if("termsofuse".equals(cmd)) {
				doEdit(ureq, link, termsOfUseDir, lang);
			} else if("delete-impressum".equals(cmd)) {
				doDelete(impressumDir, lang);
				group.getEditButton().setIconLeftCSS(null);
				group.getDeleteButton().setVisible(false);
			} else if("delete-termsofuse".equals(cmd)) {
				doDelete(termsOfUseDir, lang);
				group.getEditButton().setIconLeftCSS(null);
				group.getDeleteButton().setVisible(false);
			} else if ("dataprivacy".equals(cmd)) {
				doEdit(ureq, link, dataPrivacyPolicyDir, lang);
			} else if ("delete-dataprivacy".equals(cmd)) {
				doDelete(dataPrivacyPolicyDir, lang);
				group.getEditButton().setIconLeftCSS(null);
				group.getDeleteButton().setVisible(false);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editorCtrl == source) {
			FormLink link = (FormLink)editorCtrl.getUserObject();
			cmc.deactivate();
			cleanUp();
			
			String cmd = link.getCmd();
			ButtonGroup group = (ButtonGroup)link.getUserObject();
			String lang = group.getLang();
			String filePath = "index_" + lang + ".html";
			
			boolean exists = false;
			if("impressum".equals(cmd)) {
				exists = checkContent(impressumDir.resolve(filePath));
			} else if("termsofuse".equals(cmd)) {
				exists = checkContent(termsOfUseDir.resolve(filePath));
			} else if ("dataprivacy".equals(cmd)) {
				exists = checkContent(dataPrivacyPolicyDir.resolve(filePath));
			}
			
			if(exists) {
				group.getEditButton().setIconLeftCSS("o_icon o_icon_check");
				group.getDeleteButton().setVisible(true);
			} else {
				group.getEditButton().setIconLeftCSS(null);
				group.getDeleteButton().setVisible(false);
			}
			//needed to redraw the delete buttons
			flc.getComponent().setDirty(true);
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private boolean checkContent(VFSItem file) {
		boolean check = false;
		if(file instanceof VFSLeaf && file.exists() ) {
			if(file instanceof LocalFileImpl) {
				File f = ((LocalFileImpl)file).getBasefile();
				try {
					String content = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
					content = FilterFactory.getHtmlTagAndDescapingFilter().filter(content);
					if(content.length() > 0) {
						content = content.trim();
					}
					if(content.length() > 0) {
						check = true;
					}
				} catch (IOException e) {
					logError("", e);
				}
			} else {
				check = true;
			}
		}
		return check;
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editorCtrl);
		removeAsListenerAndDispose(cmc);
		editorCtrl = null;
		cmc = null;
	}
	
	private void doDelete(VFSContainer rootDir, String lang) {
		String filePath = "index_" + lang + ".html";
		
		VFSItem file = rootDir.resolve(filePath);
		if(file != null) {
			file.delete();
		}
	}
	
	private void doEdit(UserRequest ureq, FormLink link, VFSContainer rootDir, String lang) {
		String filePath = "index_" + lang + ".html";
		if(rootDir.resolve(filePath) == null) {
			rootDir.createChildLeaf(filePath);
		}
		editorCtrl =  WysiwygFactory.createWysiwygController(ureq, getWindowControl(), rootDir, filePath, true, false);
		editorCtrl.setUserObject(link);
		listenTo(editorCtrl);
		cmc = new CloseableModalController(getWindowControl(), "close", editorCtrl.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
	
	public static final class ButtonGroup {
		
		private final String lang;
		private final FormLink editButton;
		private final FormLink deleteButton;
		
		public ButtonGroup(String lang, FormLink editButton, FormLink deleteButton) {
			this.lang = lang;
			this.editButton = editButton;
			this.deleteButton = deleteButton;
		}
		
		public String getLang() {
			return lang;
		}
		
		public FormLink getEditButton() {
			return editButton;
		}
		
		public String getEditButtonName() {
			return editButton.getComponent().getComponentName();
		}
		
		public boolean isDelete() {
			return deleteButton.isVisible();
		}
		
		public FormLink getDeleteButton() {
			return deleteButton;
		}
		
		public String getDeleteButtonName() {
			return deleteButton.getComponent().getComponentName();
		}
	}
}
