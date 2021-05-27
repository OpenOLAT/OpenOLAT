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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.components.util.KeyValues.KeyValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.olat.repository.ui.author.copy.wizard.dates.MoveDatesStep;

/**
 * Initial date: 22.02.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CopyCourseStepsStep extends BasicStep {

	private final CopyCourseContext context;
	private final CopyCourseSteps steps;

	public CopyCourseStepsStep(UserRequest ureq, CopyCourseSteps steps, CopyCourseContext context) {
		super(ureq);

		this.steps = steps;
		this.context = context;

		setI18nTitleAndDescr("wizard.copy.course.steps", null);
		setNextStep(NOSTEP);
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return PrevNextFinishConfig.NEXT;
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		stepsRunContext.put(CopyCourseContext.CONTEXT_KEY, context);
		return new CopyLearningPathCourseStepsStepController(ureq, windowControl, form, stepsRunContext);
	}

	private class CopyLearningPathCourseStepsStepController extends StepFormBasicController {

		private SingleSelection dateSettingsEl;
		private SingleSelection metadataSettingsEl;
		private SingleSelection groupSettingsEl;
		private SingleSelection ownerSettingsEl;
		private SingleSelection coachSettingsEl;
		private SingleSelection executionSettingsEl;
		private SingleSelection publicationSettingsEl;
		private SingleSelection disclaimerSettingsEl;

		private SingleSelection blogSettingsEl;
		private SingleSelection wikiSettingsEl;
		private SingleSelection folderSettingsEl;

		private SingleSelection lectureBlockSettingsEl;
		private SingleSelection reminderSettingsEl;
		private SingleSelection assessmentModeSettingsEl;

		public CopyLearningPathCourseStepsStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT_2_10, null);

			initForm(ureq);
		}

		@Override
		protected void doDispose() {
			// Nothing to dispose
		}

		@Override
		protected void formOK(UserRequest ureq) {
			// Date settings
			context.setDateCopyType(context.getCopyType(dateSettingsEl.getSelectedKey()));
			steps.setMoveDates(context.hasStartDate() && dateSettingsEl.getSelectedKey().equals(CopyType.custom.name()));
			steps.setEditDates(context.hasDateDependantNodes() && dateSettingsEl.getSelectedKey().equals(CopyType.custom.name()));

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

			// Execution settings
			context.setExecutionCopyType(context.getCopyType(executionSettingsEl.getSelectedKey()));
			steps.setEditExecution(executionSettingsEl.isKeySelected(CopyType.custom.name()));

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

			setNextStep(MoveDatesStep.create(ureq, steps));
			fireEvent(ureq, StepsEvent.STEPS_CHANGED);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			// Copy options
			KeyValue copy = new KeyValue(CopyType.copy.name(), translate("options.copy"));
			KeyValue ignore = new KeyValue(CopyType.ignore.name(), translate("options.ignore"));
			KeyValue reference = new KeyValue(CopyType.reference.name(), translate("options.reference"));
			KeyValue customize = new KeyValue(CopyType.custom.name(), translate("options.customize"));
			KeyValue createNew = new KeyValue(CopyType.createNew.name(), translate("options.empty.resource"));
			KeyValue configureLater = new KeyValue(CopyType.ignore.name(), translate("options.configure.later"));

			// Date settings
			KeyValues dateSettings = new KeyValues(copy, customize);
			dateSettingsEl = uifactory.addDropdownSingleselect("dates", formLayout, dateSettings.keys(), dateSettings.values());
			dateSettingsEl.setHelpTextKey("dates.help", null);

			// Metadata settings
			KeyValues metadataSettings = new KeyValues(copy, customize);
			metadataSettingsEl = uifactory.addDropdownSingleselect("metadata", formLayout, metadataSettings.keys(), metadataSettings.values());

			// Group settings
			KeyValues groupSettings = new KeyValues(copy, ignore, reference, customize);
			groupSettingsEl = uifactory.addDropdownSingleselect("groups", formLayout, groupSettings.keys(), groupSettings.values());

			// Owner settings
			KeyValues ownerSettings = new KeyValues(copy, ignore, customize);
			ownerSettingsEl = uifactory.addDropdownSingleselect("owners", formLayout, ownerSettings.keys(), ownerSettings.values());

			// Coach settings
			KeyValues coachSettings = new KeyValues(copy, ignore, customize);
			coachSettingsEl = uifactory.addDropdownSingleselect("coaches", formLayout, coachSettings.keys(), coachSettings.values());

			// Execution settings
			KeyValues executionSettings = new KeyValues(copy, customize);
			executionSettingsEl = uifactory.addDropdownSingleselect("execution", formLayout, executionSettings.keys(), executionSettings.values());

			// Publication settings
			KeyValues publicationSettings = new KeyValues(copy, ignore, customize);
			publicationSettingsEl = uifactory.addDropdownSingleselect("publication", formLayout, publicationSettings.keys(), publicationSettings.values());
			publicationSettingsEl.setHelpTextKey("publication.help", null);

			// Disclaimer settings
			KeyValues disclaimerSettings = new KeyValues(copy, ignore, customize);
			disclaimerSettingsEl = uifactory.addDropdownSingleselect("disclaimer", formLayout, disclaimerSettings.keys(), disclaimerSettings.values());

			// Node specific settings
			if (context.hasNodeSpecificSettings()) {
				uifactory.addSpacerElement("spacer", formLayout, false);

				if (context.hasBlog()) {
					KeyValues blogSettings = new KeyValues(createNew, reference, configureLater, customize);
					blogSettingsEl = uifactory.addDropdownSingleselect("blogs", formLayout, blogSettings.keys(), blogSettings.values());
					blogSettingsEl.setHelpTextKey("blogs.help", null);
				}

				if (context.hasFolder()) {
					KeyValues folderSettings = new KeyValues(createNew, reference, configureLater, customize);
					folderSettingsEl = uifactory.addDropdownSingleselect("folders", formLayout, folderSettings.keys(), folderSettings.values());
					folderSettingsEl.setHelpTextKey("folders.help", null);
				}

				if (context.hasWiki()) {
					KeyValues wikiSettings = new KeyValues(createNew, reference, configureLater, customize);
					wikiSettingsEl = uifactory.addDropdownSingleselect("wikis", formLayout, wikiSettings.keys(), wikiSettings.values());
					wikiSettingsEl.setHelpTextKey("wikis.help", null);
				}
			}

			// Additional settings
			if (context.hasAdditionalSettings()) {
				uifactory.addSpacerElement("spacer", formLayout, false);

				// Reminder steps
				if (context.hasReminders()) {
					KeyValues reminderSettings = new KeyValues(copy, ignore, customize);
					reminderSettingsEl = uifactory.addDropdownSingleselect("reminders", formLayout, reminderSettings.keys(), reminderSettings.values());
				}

				// Assessment mode steps
				if (context.hasAssessmentModes()) {
					KeyValues assessmentModeSettings = new KeyValues(copy, ignore, customize);
					assessmentModeSettingsEl = uifactory.addDropdownSingleselect("assessment.modes", formLayout, assessmentModeSettings.keys(), assessmentModeSettings.values());
				}

				// Lecture block steps
				if (context.hasLectureBlocks()) {
					KeyValues lectureBlockSettings = new KeyValues(ignore, customize);
					lectureBlockSettingsEl = uifactory.addDropdownSingleselect("lecture.blocks", formLayout, lectureBlockSettings.keys(), lectureBlockSettings.values());
				}
			}
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {

		}
	}
}
