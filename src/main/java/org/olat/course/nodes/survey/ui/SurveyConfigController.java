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
package org.olat.course.nodes.survey.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.course.editor.CourseNodeReferenceProvider;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.SurveyCourseNode;
import org.olat.course.nodes.survey.SurveyManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.repository.RepositoryCoachCandidates;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryReferenceController;
import org.olat.repository.ui.RepositoryEntryReferenceProvider.ReferenceContentProvider;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent;

/**
 * 
 * Initial date: 24.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SurveyConfigController extends FormBasicController implements ReferenceContentProvider {

	private final IconPanelLabelTextContent iconPanelContent;
	private final RepositoryEntryReferenceController referenceCtrl;

	private LayoutMain3ColsPreviewController previewCtr;
	
	private final RepositoryEntry courseEntry;
	private final ModuleConfiguration moduleConfiguration;
	private final EvaluationFormSurveyIdentifier surveyIdent;
	private EvaluationFormSurvey survey;
	private RepositoryEntry formEntry;
	
	@Autowired
	private SurveyManager surveyManager;
	@Autowired
	private RepositoryService repositoryService;

	public SurveyConfigController(UserRequest ureq, WindowControl wControl, SurveyCourseNode surveyCourseNode,
			RepositoryEntry courseEntry) {
		super(ureq, wControl, "survey_config");
		this.courseEntry = courseEntry;
		this.moduleConfiguration = surveyCourseNode.getModuleConfiguration();
		this.surveyIdent = surveyManager.getSurveyIdentifier(surveyCourseNode, courseEntry);
		survey = surveyManager.loadSurvey(surveyIdent);
		this.formEntry = survey != null ? survey.getFormEntry() : null;

		iconPanelContent = new IconPanelLabelTextContent("content");

		// Empty state config with Select/Create/Import buttons inside the box
		EmptyStateConfig emptyStateConfig = EmptyStateConfig.builder()
				.withMessageTranslated(translate("no.survey.resource.selected"))
				.withDescTranslated(translate("no.survey.resource.selected.text"))
				.withIconCss("o_icon o_FileResource-FORM_icon")
				.withPrimaryButton("o_icon_search o_sel_survey_choose_repofile", "select", null)
				.withSecondaryButton("o_icon_add", "create", null, "create")
				.withSecondaryButton("o_icon_import", "import", null, "import")
				.build();
		List<Organisation> defaultOrganisations = repositoryService.getOrganisations(courseEntry);
		SurveyCourseNodeReferenceProvider referenceProvider = new SurveyCourseNodeReferenceProvider(repositoryService,
				List.of(EvaluationFormResource.TYPE_NAME), defaultOrganisations, emptyStateConfig,
				translate("edit.choose.evaluation.form"), this);
		referenceCtrl = new RepositoryEntryReferenceController(ureq, wControl, formEntry, referenceProvider);
		listenTo(referenceCtrl);

		initForm(ureq);
		updateUI();

		if (formEntry != null) {
			updateReferenceContentUI(formEntry);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("edit.title");
		setFormContextHelp("manual_user/learningresources/Course_Element_Survey/");
		formLayout.setElementCssClass("o_sel_survey_config_form");
		
		// Render the reference controller (empty state or card) in the form
		flc.put("reference", referenceCtrl.getInitialComponent());
	}
	
	private void updateUI() {
		if (formEntry != null) {
			updateReferenceContentUI(formEntry);
		}
	}

	private void updateReferenceContentUI(RepositoryEntry formEntry) {
		long numOfResponses = 0;
		if (survey != null) {
			numOfResponses = surveyManager.getSessionsCount(SessionFilterFactory.create(surveyIdent));
		}

		List<IconPanelLabelTextContent.LabelText> labelTexts = new ArrayList<>(1);
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("num.responses"), String.valueOf(numOfResponses)));
		iconPanelContent.setLabelTexts(labelTexts);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == referenceCtrl) {
			if (event == RepositoryEntryReferenceController.SELECTION_EVENT) {
				doChangeSurvey(ureq, referenceCtrl.getRepositoryEntry());
			} else if (event == RepositoryEntryReferenceController.PREVIEW_EVENT) {
				doPreviewEvaluationForm(ureq);
			}
		} else if (source == previewCtr) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(previewCtr);
		previewCtr = null;
	}

	private void doChangeSurvey(UserRequest ureq, RepositoryEntry newFormEntry) {
		if (newFormEntry == null) {
			return;
		}
		if (this.survey == null) {
			this.survey = surveyManager.createSurvey(surveyIdent, newFormEntry);
		} else {
			this.survey = surveyManager.updateSurveyForm(this.survey, newFormEntry);
		}
		this.formEntry = newFormEntry;
		SurveyCourseNode.setEvaluationFormReference(newFormEntry, moduleConfiguration);
		updateReferenceContentUI(newFormEntry);
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	private void doPreviewEvaluationForm(UserRequest ureq) {
		survey = surveyManager.loadSurvey(surveyIdent);
		File formFile = surveyManager.getFormFile(survey);
		DataStorage storage = surveyManager.loadStorage(survey);
		Controller controller = new EvaluationFormExecutionController(ureq, getWindowControl(), formFile, storage,
				new RepositoryCoachCandidates(courseEntry), null);

		previewCtr = new LayoutMain3ColsPreviewController(ureq, getWindowControl(), null,
				controller.getInitialComponent(), null);
		previewCtr.addDisposableChildController(controller);
		previewCtr.activate();
		listenTo(previewCtr);
	}

	@Override
	public Component getContent(RepositoryEntry repositoryEntry) {
		return iconPanelContent;
	}

	@Override
	public void refresh(Component cmp, RepositoryEntry repositoryEntry) {
		// Refresh is handled on change event.
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		return super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	/**
	 * Reference provider for the survey course node. A survey can only be created when no form is selected yet,
	 * and it can only be replaced when the survey is still updateable.
	 */
	public class SurveyCourseNodeReferenceProvider extends CourseNodeReferenceProvider {

		public SurveyCourseNodeReferenceProvider(RepositoryService repositoryService, List<String> resourceTypes,
				List<Organisation> defaultOrganisations, EmptyStateConfig emptyStateConfig, String selectionTitle,
				ReferenceContentProvider referenceContentProvider) {
			super(repositoryService, resourceTypes, defaultOrganisations, emptyStateConfig, selectionTitle,
					referenceContentProvider);
		}

		@Override
		public boolean canCreate() {
			return formEntry == null;
		}

		@Override
		public Confirm confirmCanReplace(UserRequest ureq) {
			// When no form is selected yet, select/create/import are always allowed.
			if (formEntry == null) {
				return new Confirm(true, null);
			}
			// A form is selected, this is a Replace action: only allowed if the survey is updateable.
			if (surveyManager.isFormUpdateable(survey)) {
				return new Confirm(true, null);
			}
			return new Confirm(false, translate("error.repo.entry.not.replaceable"));
		}
	}

}
