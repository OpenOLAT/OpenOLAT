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
package org.olat.course.disclaimer.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.disclaimer.CourseDisclaimerManager;
import org.olat.course.run.RunMainController;
import org.olat.course.wizard.CourseDisclaimerContext;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/* 
 * Date: 11 Mar 2020<br>
 * @author Alexander Boeckle
 */
public class CourseDisclaimerController extends FormBasicController {
	private static final String[] onKeys = new String[]{ "on" };
	private final String[] onValues;

	private MultipleSelectionElement disclaimer1CheckBoxEl;
	private MultipleSelectionElement disclaimer2CheckBoxEl;

	private TextElement disclaimer1TitleEl;
	private TextElement disclaimer1Label1El;
	private TextElement disclaimer1Label2El;
	private TextElement disclaimer2TitleEl;
	private TextElement disclaimer2Label1El;
	private TextElement disclaimer2Label2El;

	private RichTextElement disclaimer1TermsEl;
	private RichTextElement disclaimer2TermsEl;

	private StaticTextElement disclaimer1HeaderEl;
	private StaticTextElement disclaimer2HeaderEl;

	private SpacerElement enableAndContentSpacer;
	private SpacerElement spacer;

	private boolean disclaimer1Enabled = false;
	private boolean disclaimer2Enabled = false;
	private final boolean readOnly;

	private RepositoryEntry repositoryEntry;

	private CloseableModalController cmc;
	private CourseDisclaimerUpdateConfirmController revokeConfirmController;
	private CourseDisclaimerUpdateConfirmController removeConfirmController;
	
	private boolean usedInWizard = false;
	private boolean saveConfig = true;

	@Autowired
	private CourseDisclaimerManager disclaimerManager;

	/**
	 * Can be used in regular places
	 * 
	 * @param ureq
	 * @param wControl
	 * @param entry
	 */
	public CourseDisclaimerController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean readOnly) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(RunMainController.class, getLocale(), getTranslator()));

		this.readOnly = readOnly;
		this.repositoryEntry = entry;
		this.onValues = new String[]{ translate("enabled") };

		initForm(ureq);
		loadData();
		showElements();
	}
	
	/**
	 * Only for wizards
	 * 
	 * @param ureq
	 * @param wControl
	 * @param externalForm
	 * @param entry
	 */
	public CourseDisclaimerController(UserRequest ureq, WindowControl wControl, Form externalForm, RepositoryEntry entry) {
		super(ureq, wControl, LAYOUT_DEFAULT, null, externalForm);

		setTranslator(Util.createPackageTranslator(RunMainController.class, getLocale(), getTranslator()));

		this.repositoryEntry = entry;
		readOnly = false;
		this.onValues = new String[]{ translate("enabled") };
		this.usedInWizard = true;

		initForm(ureq);
		loadData();
		showElements();
	}
	
	

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (!usedInWizard) {
			setFormTitle("course.disclaimer.headline");
		}
		setFormContextHelp("Course Settings#_course_disclaimer");
		
		// Enable and disable the disclaimers
		disclaimer1CheckBoxEl = uifactory.addCheckboxesHorizontal("course.disclaimer.1", formLayout, onKeys, onValues);
		disclaimer1CheckBoxEl.select(onKeys[0], disclaimer1Enabled);
		disclaimer1CheckBoxEl.addActionListener(FormEvent.ONCHANGE);
		disclaimer1CheckBoxEl.setEnabled(!readOnly);
		disclaimer2CheckBoxEl = uifactory.addCheckboxesHorizontal("course.disclaimer.2", formLayout, onKeys, onValues);
		disclaimer2CheckBoxEl.select(onKeys[0], disclaimer2Enabled);
		disclaimer2CheckBoxEl.addActionListener(FormEvent.ONCHANGE);
		disclaimer2CheckBoxEl.setEnabled(!readOnly);

		enableAndContentSpacer = uifactory.addSpacerElement("course.disclaimer.spacer.1", formLayout, false);

		// Disclaimer 1
		disclaimer1HeaderEl = uifactory.addStaticTextElement("course.disclaimer.1.section", null, translate("course.disclaimer.1"), formLayout);
		disclaimer1HeaderEl.showLabel(false);
		disclaimer1HeaderEl.setElementCssClass("o_static_textelement_bold");
		disclaimer1HeaderEl.setEnabled(!readOnly);

		disclaimer1TitleEl = uifactory.addTextElement("course.disclaimer.1.title", "course.disclaimer.title", 255, translate("course.disclaimer.1.title.value"), formLayout);
		disclaimer1TitleEl.setMandatory(true);
		disclaimer1TitleEl.setEnabled(!readOnly);

		disclaimer1TermsEl = uifactory.addRichTextElementForStringDataMinimalistic("course.disclaimer.1.terms", "course.disclaimer.terms", translate("course.disclaimer.1.terms.value"), 10, 0, formLayout, getWindowControl());
		disclaimer1TermsEl.setMandatory(true);
		disclaimer1TermsEl.setEnabled(!readOnly);

		disclaimer1Label1El = uifactory.addTextElement("course.disclaimer.1.label.1", "course.disclaimer.label.1", 255, translate("course.disclaimer.label.1.value"), formLayout);
		disclaimer1Label1El.setMandatory(true);
		disclaimer1Label1El.setEnabled(!readOnly);

		disclaimer1Label2El = uifactory.addTextElement("course.disclaimer.1.label.2", "course.disclaimer.label.2", 255, "", formLayout);
		disclaimer1Label2El.setEnabled(!readOnly);
		
		// Spacer
		spacer = uifactory.addSpacerElement("course.disclaimer.spacer.2", formLayout, false);

		// Disclaimer 2
		disclaimer2HeaderEl = uifactory.addStaticTextElement("course.disclaimer.2.section", null, translate("course.disclaimer.2"), formLayout);
		disclaimer2HeaderEl.showLabel(false);
		disclaimer2HeaderEl.setElementCssClass("o_static_textelement_bold");
		disclaimer2HeaderEl.setEnabled(!readOnly);

		disclaimer2TitleEl = uifactory.addTextElement("course.disclaimer.2.title", "course.disclaimer.title", 255, translate("course.disclaimer.2.title.value"), formLayout);
		disclaimer2TitleEl.setMandatory(true);
		disclaimer2TitleEl.setEnabled(!readOnly);

		disclaimer2TermsEl = uifactory.addRichTextElementForStringDataMinimalistic("course.disclaimer.2.terms", "course.disclaimer.terms", translate("course.disclaimer.2.terms.value"), 10, 0, formLayout, getWindowControl());
		disclaimer2TermsEl.setMandatory(true);
		disclaimer2TermsEl.setEnabled(!readOnly);

		disclaimer2Label1El = uifactory.addTextElement("course.disclaimer.2.label.1", "course.disclaimer.label.1", 255, translate("course.disclaimer.label.1.value"), formLayout);
		disclaimer2Label1El.setMandatory(true);
		disclaimer2Label1El.setEnabled(!readOnly);

		disclaimer2Label2El = uifactory.addTextElement("course.disclaimer.2.label.2", "course.disclaimer.label.2", 255, "", formLayout);
		disclaimer2Label2El.setEnabled(!readOnly);
		
		// Spacer and Submit
		if (!usedInWizard && !readOnly) {
			uifactory.addFormSubmitButton("submit", formLayout);
			uifactory.addSpacerElement("course.disclaimer.spacer.bottom", formLayout, true);
		}	
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (saveConfig) {
			if (disclaimerManager.hasAnyEntry(repositoryEntry)) {
				if (!disclaimer1Enabled && !disclaimer2Enabled) {
					// Ask whether existing entries should be deleted
					askForRemoval(ureq);
				}
			}
			saveConfig(ureq);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		if (disclaimer1Enabled) {
			allOk &= validateTextInput(disclaimer1TitleEl, 255);
			allOk &= validateTextInput(disclaimer1TermsEl, 30000);
			allOk &= validateTextInput(disclaimer1Label1El, -1);
		}

		if (disclaimer2Enabled) {
			allOk &= validateTextInput(disclaimer2TitleEl, 255);
			allOk &= validateTextInput(disclaimer2TermsEl, 30000);
			allOk &= validateTextInput(disclaimer2Label1El, -1);
		}

		return allOk;
	}

	private boolean validateTextInput(TextElement textElement, int lenght) {	
		if (textElement == null) {
			return false;
		}
		
		textElement.clearError();
		if(StringHelper.containsNonWhitespace(textElement.getValue())) {
			if(lenght != -1 && textElement.getValue().length() > lenght) {
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
	protected void doDispose() {
		// Nothing to dispose here
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == disclaimer1CheckBoxEl) {
			disclaimer1Enabled = disclaimer1CheckBoxEl.getSelectedKeys().contains(onKeys[0]);
			showElements();
		} else if (source == disclaimer2CheckBoxEl) {
			disclaimer2Enabled = disclaimer2CheckBoxEl.getSelectedKeys().contains(onKeys[0]);
			showElements();
		}

		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (revokeConfirmController == source) {
			if (event.equals(Event.DONE_EVENT)) {
				revokeConsents();
				cmc.deactivate();
				getWindowControl().setInfo(translate("disclaimer.update.revoke"));
			} else if (event.equals(Event.CANCELLED_EVENT)) {
				cmc.deactivate();
				getWindowControl().setInfo(translate("disclaimer.update.no.revoke"));
			}
			cleanUp();
		} else if (removeConfirmController == source) {
			if (event.equals(Event.DONE_EVENT)) {
				removeConsents();
				cmc.deactivate();
				getWindowControl().setInfo(translate("disclaimer.update.remove"));
			} else if (event.equals(Event.CANCELLED_EVENT)) {
				cmc.deactivate();
				getWindowControl().setInfo(translate("disclaimer.update.no.remove"));
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void askForRemoval(UserRequest ureq) {
		removeConfirmController = new CourseDisclaimerUpdateConfirmController(ureq, getWindowControl(), repositoryEntry, true, true);
		listenTo(removeConfirmController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), removeConfirmController.getInitialComponent(), true, translate("disclaimer.update.confirm.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void askForRevoke(UserRequest ureq) {
		revokeConfirmController = new CourseDisclaimerUpdateConfirmController(ureq, getWindowControl(), repositoryEntry, false, true);
		listenTo(revokeConfirmController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), revokeConfirmController.getInitialComponent(), true, translate("disclaimer.update.confirm.title"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void revokeConsents() {
		disclaimerManager.revokeAllConsents(repositoryEntry);
		getWindowControl().setInfo(translate("consents.update.revoke"));
	}

	private void removeConsents() {
		disclaimerManager.removeAllConsents(repositoryEntry);
		getWindowControl().setInfo(translate("consents.update.remove"));
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(revokeConfirmController);
		removeAsListenerAndDispose(removeConfirmController);

		cmc = null;
		revokeConfirmController = null;
		removeConfirmController = null;
	}

	private void showElements() {
		disclaimer1TitleEl.setVisible(disclaimer1Enabled);
		disclaimer1TermsEl.setVisible(disclaimer1Enabled);
		disclaimer1Label1El.setVisible(disclaimer1Enabled);
		disclaimer1Label2El.setVisible(disclaimer1Enabled);	
		disclaimer1HeaderEl.setVisible(disclaimer1Enabled);
		disclaimer1HeaderEl.showLabel(false);

		disclaimer2TitleEl.setVisible(disclaimer2Enabled);
		disclaimer2TermsEl.setVisible(disclaimer2Enabled);
		disclaimer2Label1El.setVisible(disclaimer2Enabled);
		disclaimer2Label2El.setVisible(disclaimer2Enabled);
		disclaimer2HeaderEl.setVisible(disclaimer2Enabled);
		disclaimer2HeaderEl.showLabel(false);

		if (disclaimer1Enabled && disclaimer2Enabled) {
			spacer.setVisible(true);
		} else {
			spacer.setVisible(false);
		}
		
		enableAndContentSpacer.setVisible(!usedInWizard || disclaimer1Enabled || disclaimer2Enabled);
	}

	private void loadData() {
		ICourse course = CourseFactory.loadCourse(repositoryEntry);
		CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();

		disclaimer1Enabled = courseConfig.isDisclaimerEnabled(1);
		disclaimer2Enabled = courseConfig.isDisclaimerEnabled(2);

		disclaimer1CheckBoxEl.select(onKeys[0], disclaimer1Enabled);
		disclaimer2CheckBoxEl.select(onKeys[0], disclaimer2Enabled);

		if (StringHelper.containsNonWhitespace(courseConfig.getDisclaimerTitel(1))) {
			disclaimer1TitleEl.setValue(courseConfig.getDisclaimerTitel(1));
		}
		if (StringHelper.containsNonWhitespace(courseConfig.getDisclaimerTerms(1))) {
			disclaimer1TermsEl.setValue(courseConfig.getDisclaimerTerms(1));
		}
		if (StringHelper.containsNonWhitespace(courseConfig.getDisclaimerLabel(1, 1))) {
			disclaimer1Label1El.setValue(courseConfig.getDisclaimerLabel(1, 1));
		}
		if (StringHelper.containsNonWhitespace(courseConfig.getDisclaimerLabel(1, 2))) {
			disclaimer1Label2El.setValue(courseConfig.getDisclaimerLabel(1, 2));
		}

		if (StringHelper.containsNonWhitespace(courseConfig.getDisclaimerTitel(2))) {
			disclaimer2TitleEl.setValue(courseConfig.getDisclaimerTitel(2));
		}
		if (StringHelper.containsNonWhitespace(courseConfig.getDisclaimerTerms(2))) {
			disclaimer2TermsEl.setValue(courseConfig.getDisclaimerTerms(2));
		}
		if (StringHelper.containsNonWhitespace(courseConfig.getDisclaimerLabel(2, 1))) {
			disclaimer2Label1El.setValue(courseConfig.getDisclaimerLabel(2, 1));
		}
		if (StringHelper.containsNonWhitespace(courseConfig.getDisclaimerLabel(2, 2))) {
			disclaimer2Label2El.setValue(courseConfig.getDisclaimerLabel(2, 2));
		}
	}
	
	public void loadDataFromContext(CourseDisclaimerContext context) {
		saveConfig = false;
		
		if (context == null) {
			loadData();
		} else {
			disclaimer1Enabled = context.isTermsOfUseEnabled();
			disclaimer2Enabled = context.isDataProtectionEnabled();

			disclaimer1CheckBoxEl.select(onKeys[0], disclaimer1Enabled);
			disclaimer2CheckBoxEl.select(onKeys[0], disclaimer2Enabled);

			disclaimer1TitleEl.setValue(context.getTermsOfUseTitle());
			disclaimer1TermsEl.setValue(context.getTermsOfUseContent());
			disclaimer1Label1El.setValue(context.getTermsOfUseLabel1());
			disclaimer1Label2El.setValue(context.getTermsOfUseLabel2());

			disclaimer2TitleEl.setValue(context.getDataProtectionTitle());
			disclaimer2TermsEl.setValue(context.getDataProtectionContent());
			disclaimer2Label1El.setValue(context.getDataProtectionLabel1());
			disclaimer2Label2El.setValue(context.getDataProtectionLabel2());
		}
	}

	private void saveConfig(UserRequest ureq) {
		OLATResourceable courseOres = repositoryEntry.getOlatResource();
		if (CourseFactory.isCourseEditSessionOpen(courseOres.getResourceableId())) {
			showWarning("error.editoralreadylocked", new String[] { "???" });
			return;
		}

		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();

		if (	// Check if any disclaimer is enabled
				(disclaimer1Enabled || disclaimer2Enabled) &&
				(	// Check if any value is different 
						courseConfig.isDisclaimerEnabled(1) != disclaimer1Enabled || 
						courseConfig.isDisclaimerEnabled(2) != disclaimer2Enabled || 
						!courseConfig.getDisclaimerTitel(1).equals(disclaimer1TitleEl.getValue()) ||
						!courseConfig.getDisclaimerTitel(2).equals(disclaimer2TitleEl.getValue()) ||
						!courseConfig.getDisclaimerTerms(1).equals(disclaimer1TermsEl.getValue()) ||
						!courseConfig.getDisclaimerTerms(2).equals(disclaimer2TermsEl.getValue()) ||
						!courseConfig.getDisclaimerLabel(1, 1).equals(disclaimer1Label1El.getValue()) || 
						!courseConfig.getDisclaimerLabel(1, 2).equals(disclaimer1Label2El.getValue()) || 
						!courseConfig.getDisclaimerLabel(2, 1).equals(disclaimer2Label1El.getValue()) || 
						!courseConfig.getDisclaimerLabel(2, 2).equals(disclaimer2Label2El.getValue())) 
				&& !(
						// Check if any disclaimer is still disabled
						!courseConfig.isDisclaimerEnabled() && (
								// Check if the values are emtpy
								!StringHelper.containsNonWhitespace(courseConfig.getDisclaimerTitel(1)) && 
								!StringHelper.containsNonWhitespace(courseConfig.getDisclaimerTerms(1))) || (
										!StringHelper.containsNonWhitespace(courseConfig.getDisclaimerTitel(2)) && 
										!StringHelper.containsNonWhitespace(courseConfig.getDisclaimerTerms(2)))) 
				&&
				// Check if there any consents yet
				disclaimerManager.hasAnyConsent(repositoryEntry)) {
				askForRevoke(ureq);
		}

		courseConfig.setDisclaimerEnabled(1, disclaimer1Enabled);
		if (disclaimer1Enabled) {
			courseConfig.setDisclaimerTitle(1, disclaimer1TitleEl.getValue());
			courseConfig.setDisclaimerTerms(1, disclaimer1TermsEl.getValue());
			courseConfig.setDisclaimerLabel(1, 1, disclaimer1Label1El.getValue());
			courseConfig.setDisclaimerLabel(1, 2, disclaimer1Label2El.getValue());
		}

		courseConfig.setDisclaimerEnabled(2, disclaimer2Enabled);
		if (disclaimer2Enabled) {
			courseConfig.setDisclaimerTitle(2, disclaimer2TitleEl.getValue());
			courseConfig.setDisclaimerTerms(2, disclaimer2TermsEl.getValue());
			courseConfig.setDisclaimerLabel(2, 1, disclaimer2Label1El.getValue());
			courseConfig.setDisclaimerLabel(2, 2, disclaimer2Label2El.getValue());
		}

		CourseFactory.setCourseConfig(courseOres.getResourceableId(), courseConfig);
		CourseFactory.saveCourse(courseOres.getResourceableId());
		CourseFactory.closeCourseEditSession(courseOres.getResourceableId(), true);
	}
	
	public CourseDisclaimerContext getSummary() {
		CourseDisclaimerContext context = new CourseDisclaimerContext();
		
		context.setEntry(repositoryEntry);
		
		context.setTermsOfUseEnabled(disclaimer1Enabled);
		context.setTermsOfUseTitle(disclaimer1TitleEl.getValue());
		context.setTermsOfUseContent(disclaimer1TermsEl.getValue());
		context.setTermsOfUseLabel1(disclaimer1Label1El.getValue());
		context.setTermsOfUseLabel2(disclaimer1Label2El.getValue());
		
		context.setDataProtectionEnabled(disclaimer2Enabled);
		context.setDataProtectionTitle(disclaimer2TitleEl.getValue());
		context.setDataProtectionContent(disclaimer2TermsEl.getValue());
		context.setDataProtectionLabel1(disclaimer2Label1El.getValue());
		context.setDataProtectionLabel2(disclaimer2Label2El.getValue());
		
		return context;
	}
}
