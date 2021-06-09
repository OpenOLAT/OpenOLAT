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
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.components.util.KeyValues.KeyValue;
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
import org.olat.repository.ui.author.copy.wizard.dates.GeneralDatesStep;

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
			return GeneralDatesStep.create(ureq, null, steps);
		}
	}
	
	private CopyCourseSteps steps;

	public CopyCourseStepsStep(UserRequest ureq, CopyCourseSteps steps) {
		super(ureq);

		this.steps = steps;
		
		setI18nTitleAndDescr("wizard.copy.course.steps", null);
		setNextStep(steps.isEditDates() ? GeneralDatesStep.create(ureq, null, steps) : NOSTEP);
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
		
		private SingleSelection metadataSettingsEl;
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
			KeyValue copy = new KeyValue(CopyType.copy.name(), translate("options.copy"));
			KeyValue ignore = new KeyValue(CopyType.ignore.name(), translate("options.ignore"));
			KeyValue reference = new KeyValue(CopyType.reference.name(), translate("options.reference"));
			KeyValue customize = new KeyValue(CopyType.custom.name(), translate("options.customize"));
			KeyValue createNew = new KeyValue(CopyType.createNew.name(), translate("options.empty.resource"));
			KeyValue configureLater = new KeyValue(CopyType.ignore.name(), translate("options.configure.later"));

			// Metadata settings
			KeyValues metadataSettings = new KeyValues(copy, customize);
			metadataSettingsEl = uifactory.addDropdownSingleselect("metadata", formLayout, metadataSettings.keys(), metadataSettings.values());
			metadataSettingsEl.addActionListener(FormEvent.ONCHANGE);
			allOptions.add(metadataSettingsEl);

			// Group settings
			KeyValues groupSettings = new KeyValues(copy, ignore, reference, customize);
			groupSettingsEl = uifactory.addDropdownSingleselect("groups", formLayout, groupSettings.keys(), groupSettings.values());
			groupSettingsEl.addActionListener(FormEvent.ONCHANGE);
			allOptions.add(groupSettingsEl);

			// Owner settings
			KeyValues ownerSettings = new KeyValues(copy, ignore, customize);
			ownerSettingsEl = uifactory.addDropdownSingleselect("owners", formLayout, ownerSettings.keys(), ownerSettings.values());
			ownerSettingsEl.addActionListener(FormEvent.ONCHANGE);
			allOptions.add(ownerSettingsEl);

			// Coach settings
			KeyValues coachSettings = new KeyValues(copy, ignore, customize);
			coachSettingsEl = uifactory.addDropdownSingleselect("coaches", formLayout, coachSettings.keys(), coachSettings.values());
			coachSettingsEl.addActionListener(FormEvent.ONCHANGE);
			allOptions.add(coachSettingsEl);

			// Publication settings
			KeyValues publicationSettings = new KeyValues(copy, ignore, customize);
			publicationSettingsEl = uifactory.addDropdownSingleselect("publication", formLayout, publicationSettings.keys(), publicationSettings.values());
			publicationSettingsEl.setHelpTextKey("publication.help", null);
			publicationSettingsEl.addActionListener(FormEvent.ONCHANGE);
			allOptions.add(publicationSettingsEl);

			// Disclaimer settings
			KeyValues disclaimerSettings = new KeyValues(copy, ignore, customize);
			disclaimerSettingsEl = uifactory.addDropdownSingleselect("disclaimer", formLayout, disclaimerSettings.keys(), disclaimerSettings.values());
			disclaimerSettingsEl.addActionListener(FormEvent.ONCHANGE);
			allOptions.add(disclaimerSettingsEl);

			// Node specific settings
			if (context.hasNodeSpecificSettings()) {
				uifactory.addSpacerElement("spacer", formLayout, false);

				if (context.hasBlog()) {
					KeyValues blogSettings = new KeyValues(createNew, reference, configureLater, customize);
					blogSettingsEl = uifactory.addDropdownSingleselect("blogs", formLayout, blogSettings.keys(), blogSettings.values());
					blogSettingsEl.setHelpTextKey("blogs.help", null);
					blogSettingsEl.addActionListener(FormEvent.ONCHANGE);
					allOptions.add(blogSettingsEl);
				}

				if (context.hasFolder()) {
					KeyValues folderSettings = new KeyValues(createNew, reference, configureLater, customize);
					folderSettingsEl = uifactory.addDropdownSingleselect("folders", formLayout, folderSettings.keys(), folderSettings.values());
					folderSettingsEl.setHelpTextKey("folders.help", null);
					folderSettingsEl.addActionListener(FormEvent.ONCHANGE);
					allOptions.add(folderSettingsEl);
				}

				if (context.hasWiki()) {
					KeyValues wikiSettings = new KeyValues(createNew, reference, configureLater, customize);
					wikiSettingsEl = uifactory.addDropdownSingleselect("wikis", formLayout, wikiSettings.keys(), wikiSettings.values());
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
					KeyValues reminderSettings = new KeyValues(copy, ignore, customize);
					reminderSettingsEl = uifactory.addDropdownSingleselect("reminders", formLayout, reminderSettings.keys(), reminderSettings.values());
					reminderSettingsEl.addActionListener(FormEvent.ONCHANGE);
					allOptions.add(reminderSettingsEl);
				}

				// Assessment mode steps
				if (context.hasAssessmentModes()) {
					KeyValues assessmentModeSettings = new KeyValues(copy, ignore, customize);
					assessmentModeSettingsEl = uifactory.addDropdownSingleselect("assessment.modes", formLayout, assessmentModeSettings.keys(), assessmentModeSettings.values());
					assessmentModeSettingsEl.addActionListener(FormEvent.ONCHANGE);
					allOptions.add(assessmentModeSettingsEl);
				}

				// Lecture block steps
				if (context.hasLectureBlocks()) {
					KeyValues lectureBlockSettings = new KeyValues(ignore, customize);
					lectureBlockSettingsEl = uifactory.addDropdownSingleselect("lecture.blocks", formLayout, lectureBlockSettings.keys(), lectureBlockSettings.values());
					lectureBlockSettingsEl.addActionListener(FormEvent.ONCHANGE);
					allOptions.add(lectureBlockSettingsEl);
				}
			}
			
			uifactory.addSpacerElement("spacer", formLayout, false);
			
			customizeAllLink = uifactory.addFormLink("customize.all", formLayout, Link.BUTTON_XSMALL);
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if (source == customizeAllLink) {
				for (SingleSelection option : allOptions) {
					if (option.containsKey(CopyType.custom.name())) {
						option.select(CopyType.custom.name(), true);
					}
				}
			}
		}
		
		private void saveStepConfig(UserRequest ureq) {
			// Metadata settings
			context.setMetadataCopyType(context.getCopyType(metadataSettingsEl.getSelectedKey()));
			steps.setEditMetadata(metadataSettingsEl.isKeySelected(CopyType.custom.name()));

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
				steps.setEditBlogSettings(blogSettingsEl.isKeySelected(CopyType.custom.name()));
			}

			// Wiki settings
			if (context.hasWiki()) {
				context.setWikiCopyType(context.getCopyType(wikiSettingsEl.getSelectedKey()));
				steps.setEditWikiSettings(wikiSettingsEl.isKeySelected(CopyType.custom.name()));
			}

			// Folder settings
			if (context.hasFolder()) {
				context.setFolderCopyType(context.getCopyType(folderSettingsEl.getSelectedKey()));
				steps.setEditFolderSettings(folderSettingsEl.isKeySelected(CopyType.custom.name()));
			}

			// Lecture block settings
			if (context.hasLectureBlocks()) {
				context.setLectureBlockCopyType(context.getCopyType(lectureBlockSettingsEl.getSelectedKey()));
				steps.setEditLecutreBlocks(lectureBlockSettingsEl.isKeySelected(CopyType.custom.name()));
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

			setNextStep(GeneralDatesStep.create(ureq, null, steps));
			fireEvent(ureq, StepsEvent.STEPS_CHANGED);
		}
	}
}
