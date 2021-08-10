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
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.olat.repository.ui.author.copy.wizard.general.CourseOverviewStep;

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
			return CourseOverviewStep.create(ureq, steps);
		}
	}
	
	private CopyCourseSteps steps;

	public CopyCourseStepsStep(UserRequest ureq, CopyCourseSteps steps) {
		super(ureq);

		this.steps = steps;
		
		setI18nTitleAndDescr("wizard.copy.course.steps", null);
		setNextStep(steps.showNodesOverview() ? CourseOverviewStep.create(ureq, steps) : NOSTEP);
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

		private SingleSelection lectureBlockSettingsEl;
		private SingleSelection reminderSettingsEl;
		private SingleSelection assessmentModeSettingsEl;
		
		private List<SingleSelection> allOptions;
		private FormLink customizeAllLink;

		public CopyCourseStepsStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT_2_10, null);
			
			context = (CopyCourseContext) runContext.get(CopyCourseContext.CONTEXT_KEY);

			initForm(ureq);
			loadDefaultConfig();
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
			// Create new list for all options
			allOptions = new ArrayList<>();
			
			// Copy options
			SelectionValue copy = new SelectionValue(CopyType.copy.name(), translate("options.copy"), "o_light_green", true);
			SelectionValue ignore = new SelectionValue(CopyType.ignore.name(), translate("options.ignore"), "o_red", true);
			SelectionValue reference = new SelectionValue(CopyType.reference.name(), translate("options.reference"), "o_light_blue", true);
			SelectionValue customize = new SelectionValue(CopyType.custom.name(), translate("options.customize"), "o_yellow", true);
			SelectionValue createNew = new SelectionValue(CopyType.createNew.name(), translate("options.empty.resource"), "o_purple", true);
			SelectionValue configureLater = new SelectionValue(CopyType.ignore.name(), translate("options.configure.later"), "o_orange", true);
			SelectionValue copyContent = new SelectionValue(CopyType.copy.name(), translate("options.copy.content"), "o_light_green", true);
			SelectionValue ignoreContent = new SelectionValue(CopyType.ignore.name(), translate("options.ignore.content"), "o_red", true);

			// Group settings
			SelectionValues groupSettings = new SelectionValues(copy, ignore, reference, customize);
			groupSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("groups", formLayout, groupSettings);
			groupSettingsEl.addActionListener(FormEvent.ONCHANGE);
			allOptions.add(groupSettingsEl);

			// Owner settings
			disableOptions(reference);
			SelectionValues ownerSettings = new SelectionValues(copy, ignore, reference, customize);
			ownerSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("owners", formLayout, ownerSettings);
			ownerSettingsEl.addActionListener(FormEvent.ONCHANGE);
			allOptions.add(ownerSettingsEl);

			// Coach settings
			SelectionValues coachSettings = new SelectionValues(copy, ignore, reference, customize);
			coachSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("coaches", formLayout, coachSettings);
			coachSettingsEl.addActionListener(FormEvent.ONCHANGE);
			allOptions.add(coachSettingsEl);

			// Publication settings
			SelectionValues publicationSettings = new SelectionValues(copy, ignore, reference, customize);
			publicationSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("publication", formLayout, publicationSettings);
			publicationSettingsEl.setHelpTextKey("publication.help", null);
			publicationSettingsEl.addActionListener(FormEvent.ONCHANGE);
			allOptions.add(publicationSettingsEl);

			// Disclaimer settings
			SelectionValues disclaimerSettings = new SelectionValues(copy, ignore, reference, customize);
			disclaimerSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("disclaimer", formLayout, disclaimerSettings);
			disclaimerSettingsEl.addActionListener(FormEvent.ONCHANGE);
			allOptions.add(disclaimerSettingsEl);
			
			// Reset enabled states
			enableOptions(reference);

			// Node specific settings
			if (context.hasNodeSpecificSettings()) {
				uifactory.addSpacerElement("spacer", formLayout, false);

				if (context.hasFolder()) {
					SelectionValues folderSettings = new SelectionValues(copyContent, ignoreContent);
					folderSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("folders", formLayout, folderSettings);
					folderSettingsEl.setHelpTextKey("folders.help", null);
					folderSettingsEl.addActionListener(FormEvent.ONCHANGE);
					allOptions.add(folderSettingsEl);
					
					if (context.hasBlog() || context.hasWiki()) {
						uifactory.addSpacerElement("spacer", formLayout, false);
					}
				}
				
				if (context.hasBlog()) {
					SelectionValues blogSettings = new SelectionValues(createNew, reference, configureLater);
					blogSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("blogs", formLayout, blogSettings);
					blogSettingsEl.setHelpTextKey("blogs.help", null);
					blogSettingsEl.addActionListener(FormEvent.ONCHANGE);
					allOptions.add(blogSettingsEl);
				}


				if (context.hasWiki()) {
					SelectionValues wikiSettings = new SelectionValues(createNew, reference, configureLater);
					wikiSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("wikis", formLayout, wikiSettings);
					wikiSettingsEl.setHelpTextKey("wikis.help", null);
					wikiSettingsEl.addActionListener(FormEvent.ONCHANGE);
					allOptions.add(wikiSettingsEl);
				}
			}

			// Additional settings
			if (context.hasAdditionalSettings()) {
				uifactory.addSpacerElement("spacer", formLayout, false);

				// Reminder steps
				if (context.hasReminders()) {
					SelectionValues reminderSettings = new SelectionValues(copy, ignore, customize);
					reminderSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("reminders", formLayout, reminderSettings);
					reminderSettingsEl.addActionListener(FormEvent.ONCHANGE);
					allOptions.add(reminderSettingsEl);
				}

				// Assessment mode steps
				if (context.hasAssessmentModes()) {
					SelectionValues assessmentModeSettings = new SelectionValues(copy, ignore, customize);
					assessmentModeSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("assessment.modes", formLayout, assessmentModeSettings);
					assessmentModeSettingsEl.addActionListener(FormEvent.ONCHANGE);
					allOptions.add(assessmentModeSettingsEl);
				}

				// Lecture block steps
				if (context.hasLectureBlocks()) {
					disableOptions(copy);
					SelectionValues lectureBlockSettings = new SelectionValues(copy, ignore, customize);
					lectureBlockSettingsEl = uifactory.addButtonGroupSingleSelectHorizontal("lecture.blocks", formLayout, lectureBlockSettings);
					lectureBlockSettingsEl.addActionListener(FormEvent.ONCHANGE);
					allOptions.add(lectureBlockSettingsEl);
				}
			}
			
			uifactory.addSpacerElement("spacer", formLayout, false);
			
			customizeAllLink = uifactory.addFormLink("customize.all", formLayout, Link.BUTTON_XSMALL);
		}
		
		private void enableOptions(SelectionValue... options) {
			if (options == null || options.length == 0) {
				return;
			}
			
			for (SelectionValue option : options) {
				option.setEnabled(true);
			}
		}
		
		private void disableOptions(SelectionValue... options) {
			if (options == null || options.length == 0) {
				return;
			}
			
			for (SelectionValue option : options) {
				option.setEnabled(false);
			}
		}
		
		private void loadDefaultConfig() {
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
				reminderSettingsEl.select(context.getReminderCopyType().name(), true);
			}
			if (assessmentModeSettingsEl != null) {
				assessmentModeSettingsEl.select(context.getAssessmentModeCopyType().name(), true);
			}
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == customizeAllLink) {
				for (SingleSelection option : allOptions) {
					if (option.containsKey(CopyType.custom.name())) {
						option.select(CopyType.custom.name(), true);
					}
				}
			} else if (source instanceof SingleSelection) {
				saveStepConfig(ureq);
			}
		}
		
		private void saveStepConfig(UserRequest ureq) {
			// Group settings
			context.setGroupCopyType(context.getCopyType(groupSettingsEl.getSelectedKey()));
			steps.setEditGroups(groupSettingsEl.isKeySelected(CopyType.custom.name()));

			// Owner settings
			context.setOwnersCopyType(context.getCopyType(ownerSettingsEl.getSelectedKey()));
			steps.setEditOwners(ownerSettingsEl.isKeySelected(CopyType.custom.name()));

			// Coach settings
			context.setCoachesCopyType(context.getCopyType(coachSettingsEl.getSelectedKey()));
			steps.setEditCoaches(coachSettingsEl.isKeySelected(CopyType.custom.name()));

			// Publication settings
			context.setCatalogCopyType(context.getCopyType(publicationSettingsEl.getSelectedKey()));
			steps.setEditCatalog(publicationSettingsEl.isKeySelected(CopyType.custom.name()));

			// Disclaimer settings
			context.setDisclaimerCopyType(context.getCopyType(disclaimerSettingsEl.getSelectedKey()));
			steps.setEditDisclaimer(disclaimerSettingsEl.isKeySelected(CopyType.custom.name()));

			// Blog settings
			if (context.hasBlog()) {
				context.setBlogCopyType(context.getCopyType(blogSettingsEl.getSelectedKey()));
			}

			// Wiki settings
			if (context.hasWiki()) {
				context.setWikiCopyType(context.getCopyType(wikiSettingsEl.getSelectedKey()));
			}

			// Folder settings
			if (context.hasFolder()) {
				context.setFolderCopyType(context.getCopyType(folderSettingsEl.getSelectedKey()));
			}

			// Lecture block settings
			if (context.hasLectureBlocks()) {
				context.setLectureBlockCopyType(context.getCopyType(lectureBlockSettingsEl.getSelectedKey()));
				steps.setEditLectureBlocks(lectureBlockSettingsEl.isKeySelected(CopyType.custom.name()));
			}

			// Reminder settings
			if (context.hasReminders()) {
				context.setReminderCopyType(context.getCopyType(reminderSettingsEl.getSelectedKey()));
				steps.setEditReminders(reminderSettingsEl.isKeySelected(CopyType.custom.name()));
			}

			// Assessment mode settings
			if (context.hasAssessmentModes()) {
				context.setAssessmentModeCopyType(context.getCopyType(assessmentModeSettingsEl.getSelectedKey()));
				steps.setEditAssessmentModes(assessmentModeSettingsEl.isKeySelected(CopyType.custom.name()));
			}

			setNextStep(CourseOverviewStep.create(ureq, steps));
			fireEvent(ureq, StepsEvent.STEPS_CHANGED);
		}
	}
}
