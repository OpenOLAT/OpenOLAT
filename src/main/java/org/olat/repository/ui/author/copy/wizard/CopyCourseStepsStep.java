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
package org.olat.repository.ui.author.copy.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.olat.repository.ui.author.copy.wizard.general.OwnersStep;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 22.02.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CopyCourseStepsStep extends BasicStep {

	public static Step create(UserRequest ureq, CopyCourseSteps steps) {
		if (steps.isAdvancedMode()) {
			return new CopyCourseStepsStep(ureq, steps);
		} else {
			return OwnersStep.create(ureq, steps);
		}
	}
	
	private CopyCourseSteps steps;

	public CopyCourseStepsStep(UserRequest ureq, CopyCourseSteps steps) {
		super(ureq);

		this.steps = steps;
		
		setI18nTitleAndDescr("wizard.copy.course.steps", null);
		setNextStep(OwnersStep.create(ureq, steps));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, !nextStep().equals(NOSTEP), nextStep().equals(NOSTEP));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new CopyCourseStepsStepController(ureq, windowControl, form, stepsRunContext);
	}

	private class CopyCourseStepsStepController extends StepFormBasicController {

		private CopyCourseContext context;
		
		private SingleSelection groupSettingsEl;
		private SingleSelection ownerSettingsEl;
		private SingleSelection coachSettingsEl;
		private SingleSelection publicationSettingsEl;
		private SingleSelection disclaimerSettingsEl;

		private SingleSelection blogSettingsEl;
		private SingleSelection wikiSettingsEl;
		private SingleSelection folderSettingsEl;
		private SingleSelection taskSettingsEl;

		private SingleSelection lectureBlockSettingsEl;
		private SingleSelection reminderSettingsEl;
		private SingleSelection assessmentModeSettingsEl;
		
		private List<SingleSelection> allOptions;
		private FormLink customizeAllLink;
		private FormLink resetToDefaultLink;
		
		@Autowired
		private CopyCourseWizardModule wizardModule;

		public CopyCourseStepsStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
			
			context = (CopyCourseContext) runContext.get(CopyCourseContext.CONTEXT_KEY);

			initForm(ureq);
			loadConfigFromContext(ureq);
		}

		@Override
		protected void doDispose() {
			// Nothing to dispose
		}

		@Override
		protected void formOK(UserRequest ureq) {
			saveStepConfig(ureq);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			// Help text
			setFormDescription("copy.steps.info");
			
			// Buttons
			FormLayoutContainer buttonWrapperLayout = FormLayoutContainer.createDefaultFormLayout_2_10("buttonWrapperLayout", getTranslator());
			buttonWrapperLayout.setRootForm(mainForm);
			formLayout.add(buttonWrapperLayout);
			
			String page = Util.getPackageVelocityRoot(getClass()) + "/buttons_pull_right.html";
			FormLayoutContainer buttonLayout = FormLayoutContainer.createCustomFormLayout("buttonLayout", getTranslator(), page);
			buttonWrapperLayout.add(buttonLayout);
			
			customizeAllLink = uifactory.addFormLink("customize.all", buttonLayout, Link.BUTTON);
			customizeAllLink.setElementCssClass("pull-right");
			resetToDefaultLink = uifactory.addFormLink("default.all", buttonLayout, Link.BUTTON);
			resetToDefaultLink.setElementCssClass("pull-right");
			
			// Create new list for all options
			allOptions = new ArrayList<>();
			
			// Copy options
			SelectionValue copy = new SelectionValue(CopyType.copy.name(), translate("options.copy"), "o_primary", true);
			SelectionValue ignore = new SelectionValue(CopyType.ignore.name(), translate("options.ignore"), "o_primary", true);
			SelectionValue reference = new SelectionValue(CopyType.reference.name(), translate("options.reference"), "o_primary", true);
			SelectionValue customize = new SelectionValue(CopyType.custom.name(), translate("options.customize"), "o_primary", true);
			SelectionValue createNew = new SelectionValue(CopyType.createNew.name(), translate("options.empty.resource"), "o_primary", true);
			SelectionValue configureLater = new SelectionValue(CopyType.ignore.name(), translate("options.configure.later"), "o_primary", true);

			// Members management
			FormLayoutContainer memebersManagementLayout = FormLayoutContainer.createDefaultFormLayout_2_10("memebersManagementLayout", getTranslator());
			memebersManagementLayout.setRootForm(mainForm);
			memebersManagementLayout.setFormTitle(translate("members.management"));
			formLayout.add(memebersManagementLayout);
			
			// Owner settings
			if (context.hasOwners()) {
				SelectionValues ownerSettings = new SelectionValues(customize, copy, ignore);
				ownerSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("owners", memebersManagementLayout, ownerSettings);
				ownerSettingsEl.addActionListener(FormEvent.ONCHANGE);
				allOptions.add(ownerSettingsEl);
			}
			
			// Coach settings
			if (context.hasCoaches()) {
				SelectionValues coachSettings = new SelectionValues(customize, copy, ignore);
				coachSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("coaches", memebersManagementLayout, coachSettings);
				coachSettingsEl.addActionListener(FormEvent.ONCHANGE);
				allOptions.add(coachSettingsEl);
			}
			
			// Group settings
			if (context.hasGroups()) {
				SelectionValues groupSettings = new SelectionValues(customize, copy, ignore, reference);
				groupSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("groups", memebersManagementLayout, groupSettings);
				groupSettingsEl.addActionListener(FormEvent.ONCHANGE);
				allOptions.add(groupSettingsEl);
			}

			// Node specific settings
			if (context.hasNodeSpecificSettings()) {
				FormLayoutContainer nodeSettingsLayout = FormLayoutContainer.createDefaultFormLayout_2_10("nodeSettingsLayout", getTranslator());
				nodeSettingsLayout.setRootForm(mainForm);
				nodeSettingsLayout.setFormTitle(translate("course.node.settings"));
				formLayout.add(nodeSettingsLayout);

				if (context.hasTask()) {
					SelectionValue copyAssignmentAndSolution = new SelectionValue(CopyType.copy.name(), translate("options.copy.assignment.solution"), "o_primary", true);
					SelectionValue ignoreAssignmentAndSolution = new SelectionValue(CopyType.ignore.name(), translate("options.ignore.assignment.solution"), "o_primary", true);
					SelectionValues taskSettings = new SelectionValues(copyAssignmentAndSolution, ignoreAssignmentAndSolution);
					taskSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("tasks", nodeSettingsLayout, taskSettings);
					taskSettingsEl.addActionListener(FormEvent.ONCHANGE);
					allOptions.add(taskSettingsEl);
					
				}
				
				if (context.hasFolder()) {
					SelectionValue copyContent = new SelectionValue(CopyType.copy.name(), translate("options.copy.content"), "o_primary", true);
					SelectionValue ignoreContent = new SelectionValue(CopyType.ignore.name(), translate("options.ignore.content"), "o_primary", true);
					SelectionValues folderSettings = new SelectionValues(copyContent, ignoreContent);
					folderSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("folders", nodeSettingsLayout, folderSettings);
					folderSettingsEl.setHelpTextKey("folders.help", null);
					folderSettingsEl.addActionListener(FormEvent.ONCHANGE);
					allOptions.add(folderSettingsEl);
					
				}
				
				if (context.hasBlog()) {
					SelectionValues blogSettings = new SelectionValues(reference, configureLater, createNew);
					blogSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("blogs", nodeSettingsLayout, blogSettings);
					blogSettingsEl.setHelpTextKey("blogs.help", null);
					blogSettingsEl.addActionListener(FormEvent.ONCHANGE);
					allOptions.add(blogSettingsEl);
				}


				if (context.hasWiki()) {
					SelectionValues wikiSettings = new SelectionValues(reference, configureLater, createNew);
					wikiSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("wikis", nodeSettingsLayout, wikiSettings);
					wikiSettingsEl.setHelpTextKey("wikis.help", null);
					wikiSettingsEl.addActionListener(FormEvent.ONCHANGE);
					allOptions.add(wikiSettingsEl);
				}
			}
			
			// Additional settings
			FormLayoutContainer additionalSettingsLayout = FormLayoutContainer.createDefaultFormLayout_2_10("additionalSettingsLayout", getTranslator());
			additionalSettingsLayout.setRootForm(mainForm);
			additionalSettingsLayout.setFormTitle(translate("additional.settings"));
			formLayout.add(additionalSettingsLayout);
			
			// Publication settings
			if (context.hasCatalogEntry()) {
				SelectionValues publicationSettings = new SelectionValues(customize, copy, ignore);
				publicationSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("publication", additionalSettingsLayout, publicationSettings);
				publicationSettingsEl.setHelpTextKey("publication.help", null);
				publicationSettingsEl.addActionListener(FormEvent.ONCHANGE);
				allOptions.add(publicationSettingsEl);
			}

			// Disclaimer settings
			if (context.hasDisclaimer()) {
				SelectionValues disclaimerSettings = new SelectionValues(customize, copy, ignore);
				disclaimerSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("disclaimer", additionalSettingsLayout, disclaimerSettings);
				disclaimerSettingsEl.addActionListener(FormEvent.ONCHANGE);
				allOptions.add(disclaimerSettingsEl);
			}

			// Reminder steps
			if (context.hasReminders()) {
				SelectionValues reminderSettings = null;
				
				if (context.hasDateDependantReminders()) {
					reminderSettings = new SelectionValues(customize, copy, ignore);
				} else {
					reminderSettings = new SelectionValues(copy, ignore);
				}
				
				reminderSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("reminders", additionalSettingsLayout, reminderSettings);
				reminderSettingsEl.addActionListener(FormEvent.ONCHANGE);
				allOptions.add(reminderSettingsEl);
			}
			
			// Lecture block steps
			if (context.hasLectureBlocks()) {
				SelectionValues lectureBlockSettings = new SelectionValues(customize, copy, ignore);
				lectureBlockSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("lecture.blocks", additionalSettingsLayout, lectureBlockSettings);
				lectureBlockSettingsEl.addActionListener(FormEvent.ONCHANGE);
				allOptions.add(lectureBlockSettingsEl);
			}
						
			// Assessment mode steps
			if (context.hasAssessmentModes()) {
				SelectionValues assessmentModeSettings = new SelectionValues(customize, copy, ignore);
				assessmentModeSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("assessment.modes", additionalSettingsLayout, assessmentModeSettings);
				assessmentModeSettingsEl.addActionListener(FormEvent.ONCHANGE);
				allOptions.add(assessmentModeSettingsEl);
			}
			
			additionalSettingsLayout.setVisible(false);
			for (FormItem item : additionalSettingsLayout.getFormItems()) {
				additionalSettingsLayout.setVisible(true);
				break;
			}
		}
		
		private void loadDefaultConfig(UserRequest ureq) {
			if (groupSettingsEl != null) {
				groupSettingsEl.select(wizardModule.getGroupsCopyType().name(), true);
			}
			if (ownerSettingsEl != null) {
				ownerSettingsEl.select(wizardModule.getOwnersCopyType().name(), true);
			}
			if (coachSettingsEl != null) {
				coachSettingsEl.select(wizardModule.getCoachesCopyType().name(), true);
			}
			if (publicationSettingsEl != null) {
				publicationSettingsEl.select(wizardModule.getCatalogCopyType().name(), true);
			}
			if (disclaimerSettingsEl != null) {
				disclaimerSettingsEl.select(wizardModule.getDisclaimerCopyType().name(), true);
			}
			
			if (taskSettingsEl != null) {
				taskSettingsEl.select(wizardModule.getTaskCopyType().name(), true);
			}
			if (blogSettingsEl != null) {
				blogSettingsEl.select(wizardModule.getBlogCopyType().name(), true);
			}
			if (wikiSettingsEl != null) {
				wikiSettingsEl.select(wizardModule.getWikiCopyType().name(), true);
			}
			if (folderSettingsEl != null) {
				folderSettingsEl.select(wizardModule.getFolderCopyType().name(), true);
			}

			if (lectureBlockSettingsEl != null) {
				lectureBlockSettingsEl.select(wizardModule.getLectureBlockCopyType().name(), true);
			}
			if (reminderSettingsEl != null) {
				String copyMode = wizardModule.getReminderCopyType().name();
				
				if (copyMode.equals(CopyType.custom.name()) && !context.hasDateDependantReminders()) {
					copyMode = CopyType.copy.name();
				}
				
				reminderSettingsEl.select(copyMode, true);
			}
			if (assessmentModeSettingsEl != null) {
				assessmentModeSettingsEl.select(wizardModule.getAssessmentCopyType().name(), true);
			}
			
			saveStepConfig(ureq);
		}
		
		private void loadConfigFromContext(UserRequest ureq) {
			if (groupSettingsEl != null) {
				groupSettingsEl.select(context.getGroupCopyType().name(), true);
			}
			if (ownerSettingsEl != null) {
				ownerSettingsEl.select(context.getOwnersCopyType().name(), true);
			}
			if (coachSettingsEl != null) {
				coachSettingsEl.select(context.getCoachesCopyType().name(), true);
			}
			if (publicationSettingsEl != null) {
				publicationSettingsEl.select(context.getCatalogCopyType().name(), true);
			}
			if (disclaimerSettingsEl != null) {
				disclaimerSettingsEl.select(context.getDisclaimerCopyType().name(), true);
			}
			
			if (taskSettingsEl != null) {
				taskSettingsEl.select(context.getTaskCopyType().name(), true);
			}
			if (blogSettingsEl != null) {
				blogSettingsEl.select(context.getBlogCopyType().name(), true);
			}
			if (wikiSettingsEl != null) {
				wikiSettingsEl.select(context.getWikiCopyType().name(), true);
			}
			if (folderSettingsEl != null) {
				folderSettingsEl.select(context.getFolderCopyType().name(), true);
			}

			if (lectureBlockSettingsEl != null) {
				lectureBlockSettingsEl.select(context.getLectureBlockCopyType().name(), true);
			}
			if (reminderSettingsEl != null) {
				String copyMode = context.getReminderCopyType().name();
				
				if (copyMode.equals(CopyType.custom.name()) && !context.hasDateDependantReminders()) {
					copyMode = CopyType.copy.name();
				}
				
				reminderSettingsEl.select(copyMode, true);
			}
			if (assessmentModeSettingsEl != null) {
				assessmentModeSettingsEl.select(context.getAssessmentModeCopyType().name(), true);
			}
			
			saveStepConfig(ureq);
		}
		
		private void customizeAllOptions(UserRequest ureq) {
			for (SingleSelection option : allOptions) {
				if (option.containsKey(CopyType.custom.name())) {
					option.select(CopyType.custom.name(), true);
				}
			}
			
			saveStepConfig(ureq);
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == customizeAllLink) {
				customizeAllOptions(ureq);
			} else if (source == resetToDefaultLink) {
				loadDefaultConfig(ureq);
			} else if (source instanceof SingleSelection) {
				saveStepConfig(ureq);
			}
		}
		
		private void saveStepConfig(UserRequest ureq) {
			// Group settings
			if (groupSettingsEl != null) {
				context.setGroupCopyType(context.getCopyType(groupSettingsEl.getSelectedKey()));
				steps.setEditGroups(groupSettingsEl.isKeySelected(CopyType.custom.name()));
			}

			// Owner settings
			if (ownerSettingsEl != null) {
				context.setOwnersCopyType(context.getCopyType(ownerSettingsEl.getSelectedKey()));
				steps.setEditOwners(ownerSettingsEl.isKeySelected(CopyType.custom.name()));
			}

			// Coach settings
			if (coachSettingsEl != null) {
				context.setCoachesCopyType(context.getCopyType(coachSettingsEl.getSelectedKey()));
				steps.setEditCoaches(coachSettingsEl.isKeySelected(CopyType.custom.name()));
			}

			// Publication settings
			if (publicationSettingsEl != null) {
				context.setCatalogCopyType(context.getCopyType(publicationSettingsEl.getSelectedKey()));
				steps.setEditCatalog(publicationSettingsEl.isKeySelected(CopyType.custom.name()));
			}

			// Disclaimer settings
			if (disclaimerSettingsEl != null) {
				context.setDisclaimerCopyType(context.getCopyType(disclaimerSettingsEl.getSelectedKey()));
				steps.setEditDisclaimer(disclaimerSettingsEl.isKeySelected(CopyType.custom.name()));
			}

			// Task settings
			if (taskSettingsEl != null) {
				context.setTaskCopyType(context.getCopyType(taskSettingsEl.getSelectedKey()));
			}
			
			// Blog settings
			if (blogSettingsEl != null) {
				context.setBlogCopyType(context.getCopyType(blogSettingsEl.getSelectedKey()));
			}

			// Wiki settings
			if (wikiSettingsEl != null) {
				context.setWikiCopyType(context.getCopyType(wikiSettingsEl.getSelectedKey()));
			}

			// Folder settings
			if (folderSettingsEl != null) {
				context.setFolderCopyType(context.getCopyType(folderSettingsEl.getSelectedKey()));
			}

			// Lecture block settings
			if (lectureBlockSettingsEl != null) {
				context.setLectureBlockCopyType(context.getCopyType(lectureBlockSettingsEl.getSelectedKey()));
				steps.setEditLectureBlocks(lectureBlockSettingsEl.isKeySelected(CopyType.custom.name()));
			}

			// Reminder settings
			if (reminderSettingsEl != null) {
				context.setReminderCopyType(context.getCopyType(reminderSettingsEl.getSelectedKey()));
				steps.setEditReminders(reminderSettingsEl.isKeySelected(CopyType.custom.name()));
			}

			// Assessment mode settings
			if (assessmentModeSettingsEl != null) {
				context.setAssessmentModeCopyType(context.getCopyType(assessmentModeSettingsEl.getSelectedKey()));
				steps.setEditAssessmentModes(assessmentModeSettingsEl.isKeySelected(CopyType.custom.name()));
			}

			setNextStep(OwnersStep.create(ureq, steps));
			fireEvent(ureq, StepsEvent.STEPS_CHANGED);
		}
	}
}
