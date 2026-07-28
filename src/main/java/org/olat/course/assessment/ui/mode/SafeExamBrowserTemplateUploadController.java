/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.mode;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.PList;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.SafeExamBrowserTemplate;
import org.olat.course.assessment.SafeExamBrowserTemplateType;
import org.olat.course.assessment.model.SafeExamBrowserTemplateImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 juin 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class SafeExamBrowserTemplateUploadController extends FormBasicController {

	private static final String ACTIVE_STATUS_KEY = "active";
	private static final String INACTIVE_STATUS_KEY = "inactive";
	
	private TextElement nameEl;
	private FileElement uploadEl;
	private TextElement filenameEl;
	private FormToggle allowToExitEl;
	private FormLink activeLink;
	private FormLink inactiveLink;
	private FormLink replaceButton;
	private DropdownItem statusDropdown;
	private TextElement passwordToQuitEl;
	private SingleSelection downloadConfigEl;
	private RichTextElement safeExamBrowserHintEl;
	private TextElement safeExamBrowserAuthorHintEl;
	
	private String temporaryConfigPList;
	private SafeExamBrowserTemplate sebTemplate;
	
	private CloseableModalController cmc;
	private SafeExamBrowserConfigUploadController uploadCtrl;
	private SafeExamBrowserRawConfigurationController rawConfigurationCtrl;
	
	@Autowired
	private AssessmentModeManager assessmentModeManager;
	
	public SafeExamBrowserTemplateUploadController(UserRequest ureq, WindowControl wControl, SafeExamBrowserTemplate sebTemplate) {
		super(ureq, wControl, "seb_template");
		this.sebTemplate = sebTemplate;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer templateCont = uifactory.addDefaultFormLayout("templatecont", null, formLayout);
		initTemplateForm(templateCont, formLayout);
		
		FormLayoutContainer specificCont = uifactory.addDefaultFormLayout("specificcont", null, formLayout);
		specificCont.setFormTitle(translate("seb.template.mode.title"));
		specificCont.setFormInfo(translate("seb.template.mode.hint"));
		initSpecificForm(specificCont, ureq);

		FormLayoutContainer buttonsWrapperCont = uifactory.addDefaultFormLayout("buttonscont", null, formLayout);
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, buttonsWrapperCont);
		String i18nKey = sebTemplate != null ? "save" : "upload";
		uifactory.addFormSubmitButton(i18nKey, buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		
		FormLayoutContainer rawConfigurationCont = uifactory.addDefaultFormLayout("rawconfigurationcont", null, formLayout);
		rawConfigurationCont.setFormTitle(translate("seb.raw.configuration.section.title"));
		rawConfigurationCont.setFormInfo(translate("seb.raw.configuration.section.hint"));
		initRawConfiguration(rawConfigurationCont, ureq);
	}
	
	private void initTemplateForm(FormLayoutContainer templateLayout, FormItemContainer formLayout) {
		String name = sebTemplate != null ? sebTemplate.getName() : "";
		nameEl = uifactory.addTextElement("seb.template.name", "seb.template.name", 255, name, templateLayout);
		nameEl.setMandatory(true);
		
		statusDropdown = uifactory.addDropdownMenu("seb.template.status", "seb.template.status", templateLayout, getTranslator());
		statusDropdown.setLabel("seb.template.status", null, true);
		statusDropdown.setTranslatedLabel(null);
		statusDropdown.setOrientation(DropdownOrientation.right);
		statusDropdown.setElementCssClass("o_seb_template_status");
		statusDropdown.setEmbbeded(true);
		statusDropdown.setLabeled(true, true);
		boolean active = sebTemplate == null || sebTemplate.isActive();
		updateStatus(active ? ACTIVE_STATUS_KEY : INACTIVE_STATUS_KEY);
		
		activeLink = uifactory.addFormLink("seb.template.active", formLayout, Link.LINK);
		activeLink.setIconLeftCSS("o_icon o_icon-fw o_icon_seb_template_status_active");
		activeLink.setElementCssClass("o_labeled o_seb_template_status_active");
		statusDropdown.addElement(activeLink);
		
		inactiveLink = uifactory.addFormLink("seb.template.inactive", formLayout, Link.LINK);
		inactiveLink.setIconLeftCSS("o_icon o_icon-fw o_icon_seb_template_status_inactive");
		inactiveLink.setElementCssClass("o_labeled o_seb_template_status_inactive");
		statusDropdown.addElement(inactiveLink);
		
		if(sebTemplate == null) {
			uploadEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "seb.template.file", "seb.template.file", templateLayout);
			uploadEl.setMultiFileUpload(false);
			if(sebTemplate != null && StringHelper.containsNonWhitespace(sebTemplate.getSafeExamBrowserConfigFilename())) {
				uploadEl.setInitialFile(new File(sebTemplate.getSafeExamBrowserConfigFilename()));
			}
		} else {
			String filename = sebTemplate.getSafeExamBrowserConfigFilename();
			if(!StringHelper.containsNonWhitespace(filename)) {
				filename = ".seb";
			}
			
			FormLayoutContainer replaceCont = uifactory.addInputGroupFormLayout("replaceCont", "seb.template.file", templateLayout);
			filenameEl = uifactory.addTextElement("seb.template.filename", null, 255, filename, replaceCont);
			filenameEl.setDomReplacementWrapperRequired(false);
			filenameEl.setEnabled(false);
			replaceButton = uifactory.addFormLink("rightAddOn", "replace", "replace", replaceCont, Link.BUTTON);
			replaceButton.setIconLeftCSS("o_icon o_icon-fw o_icon_search o_icon-lg");
			replaceButton.setElementCssClass("input-group-addon");
		}
		
		String authorHint = sebTemplate != null ? sebTemplate.getSafeExamBrowserAuthorHint() : "";
		safeExamBrowserAuthorHintEl = uifactory.addTextAreaElement("safeexamauthorhint", "mode.safeexambrowser.hint.author",
				4000, 3, 60, true, false, false, authorHint, templateLayout);
	}

	private void initSpecificForm(FormLayoutContainer formLayout, UserRequest ureq) {	
		SelectionValues trueFalseValues = new SelectionValues();
		trueFalseValues.add(SelectionValues.entry("true", translate("yes")));
		trueFalseValues.add(SelectionValues.entry("false", translate("no")));

		boolean download = sebTemplate != null && sebTemplate.getSafeExamBrowserConfigDownload() != null
				&& sebTemplate.getSafeExamBrowserConfigDownload().booleanValue();
		downloadConfigEl = uifactory.addRadiosHorizontal("mode.safeexambrowser.download.config", formLayout,
				trueFalseValues.keys(), trueFalseValues.values());
		downloadConfigEl.select(download ? "true" : "false", true);

		String hint = sebTemplate != null ? sebTemplate.getSafeExamBrowserHint() : "";
		safeExamBrowserHintEl = uifactory.addRichTextElementForStringData("safeexamhint", "mode.safeexambrowser.hint",
				hint, 4, -1, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		safeExamBrowserHintEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		
		allowToExitEl = uifactory.addToggleButton("mode.safeexambrowser.allow.toexit", "mode.safeexambrowser.allow.toexit", translate("on"), translate("off"), formLayout);
		allowToExitEl.toggle(sebTemplate == null
				|| (sebTemplate.getSafeExamBrowserConfigAllowExit() != null && sebTemplate.getSafeExamBrowserConfigAllowExit().booleanValue()));
		allowToExitEl.setEnabled(sebTemplate == null || !sebTemplate.isDefault());
		
		String password = sebTemplate != null ? sebTemplate.getSafeExamBrowserConfigExitPassword() : null;
		passwordToQuitEl = uifactory.addTextElement("password.quit", "mode.safeexambrowser.password.exit", 255, password, formLayout);
	}
	
	private void initRawConfiguration(FormLayoutContainer formLayout, UserRequest ureq) {
		rawConfigurationCtrl = new SafeExamBrowserRawConfigurationController(ureq, getWindowControl(), mainForm);
		listenTo(rawConfigurationCtrl);
		
		String plist = sebTemplate != null ? sebTemplate.getSafeExamBrowserConfigPList() : null;
		if(StringHelper.containsNonWhitespace(plist)) {
			rawConfigurationCtrl.loadConfiguration(plist);
		}
		
		FormItem rawTableEl = rawConfigurationCtrl.getInitialFormItem();
		rawTableEl.setFormLayout("nolayout");
		formLayout.add(rawTableEl);
	}
	
	private void updateStatus(String status) {
		statusDropdown.setIconCSS("o_icon o_icon_seb_template_status_" + status);
		statusDropdown.setInnerText(translate("seb.template." + status));
		statusDropdown.setToggleCSS("o_labeled o_seb_template_status_" + status);
		statusDropdown.setUserObject(status);
	}
	
	private void updateUI() {
		boolean allowToExit = allowToExitEl.isOn();
		passwordToQuitEl.setVisible(allowToExit);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == uploadCtrl) {
			if (event == Event.DONE_EVENT) {
				temporaryConfigPList = uploadCtrl.loadPList();
				filenameEl.setValue(uploadCtrl.getFilename());
				rawConfigurationCtrl.loadConfiguration(temporaryConfigPList);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(uploadCtrl);
		removeAsListenerAndDispose(cmc);
		uploadCtrl = null;
		cmc = null;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		nameEl.clearError();
		if (!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		} else if (nameEl.getValue().length() > 255) {
			nameEl.setErrorKey("form.error.toolong", "255");
			allOk &= false;
		}
		
		if(uploadEl != null) {
			uploadEl.clearError();
			if(sebTemplate == null && uploadEl.getUploadFile() == null) {
				uploadEl.setErrorKey("form.mandatory.hover");
				allOk &= false;
			} else if(uploadEl.getUploadFile() != null && !validateFile(uploadEl.getUploadFile())) {
				uploadEl.setErrorKey("error.safe.exam.config.format");
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	private boolean validateFile(File file) {
		boolean allOk = false;
		
		try {
			String xml = FileUtils.load(file, "UTF-8");
			PList plist = PList.valueOf(xml);
			return plist.getRootDict() != null;
		} catch (Exception e) {
			getLogger().warn("Cannot read a configuration file");
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == replaceButton) {
			doReplaceConfigFile(ureq);
		} else if(source == activeLink) {
			updateStatus(ACTIVE_STATUS_KEY);
		} else if(source == inactiveLink) {
			updateStatus(INACTIVE_STATUS_KEY);
		} else if(source == allowToExitEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (sebTemplate == null) {
			sebTemplate = assessmentModeManager.createSafeExamBrowserTemplate(nameEl.getValue(), SafeExamBrowserTemplateType.SEB_FILE);
		} else {
			sebTemplate.setName(nameEl.getValue());
		}
		sebTemplate.setActive(ACTIVE_STATUS_KEY.equals(statusDropdown.getUserObject()));
		
		File uploadedFile = uploadEl == null ? null : uploadEl.getUploadFile();
		if(uploadedFile != null) {
			String plistConfiguration = FileUtils.load(uploadedFile, "UTF-8");
			((SafeExamBrowserTemplateImpl)sebTemplate).setSafeExamBrowserConfigPList(plistConfiguration);
			sebTemplate.setSafeExamBrowserConfigFilename(uploadedFile.getName());
		} else if(StringHelper.containsNonWhitespace(temporaryConfigPList)) {
			((SafeExamBrowserTemplateImpl)sebTemplate).setSafeExamBrowserConfigPList(temporaryConfigPList);
			sebTemplate.setSafeExamBrowserConfigFilename(filenameEl.getValue());
		}
	
		sebTemplate.setSafeExamBrowserHint(safeExamBrowserHintEl.getValue());
		sebTemplate.setSafeExamBrowserAuthorHint(safeExamBrowserAuthorHintEl.getValue());
		sebTemplate.setSafeExamBrowserConfigDownload(downloadConfigEl.isOneSelected()
				&& "true".equals(downloadConfigEl.getSelectedKey()));
		
		boolean allowToExit = allowToExitEl.isOn();
		sebTemplate.setSafeExamBrowserConfigAllowExit(allowToExit);
		if(allowToExit) {
			sebTemplate.setSafeExamBrowserConfigExitPassword(passwordToQuitEl.getValue());
		} else {
			sebTemplate.setSafeExamBrowserConfigExitPassword(null);
		}
		
		sebTemplate = assessmentModeManager.updateSafeExamBrowserTemplate(sebTemplate);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doReplaceConfigFile(UserRequest ureq) {
		removeAsListenerAndDispose(uploadCtrl);
		uploadCtrl = new SafeExamBrowserConfigUploadController(ureq, getWindowControl());
		listenTo(uploadCtrl);

		String title = translate("upload.seb.template");
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				uploadCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
}
