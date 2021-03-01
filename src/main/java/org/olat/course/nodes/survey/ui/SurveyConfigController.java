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

import org.olat.NewControllerFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.SurveyCourseNode;
import org.olat.course.nodes.survey.SurveyManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SurveyConfigController extends FormBasicController {

	private StaticTextElement evaluationFormNotChoosen;
	private FormLink evaluationFormLink;
	private FormLink chooseLink;
	private FormLink replaceLink;
	private FormLink editLink;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController searchCtrl;
	private LayoutMain3ColsPreviewController previewCtr;
	
	private final ModuleConfiguration moduleConfiguration;
	private final EvaluationFormSurveyIdentifier surveyIdent;
	private EvaluationFormSurvey survey;
	
	@Autowired
	private SurveyManager surveyManager;

	public SurveyConfigController(UserRequest ureq, WindowControl wControl, SurveyCourseNode surveyCourseNode,
			RepositoryEntry courseEntry) {
		super(ureq, wControl);
		this.moduleConfiguration = surveyCourseNode.getModuleConfiguration();
		this.surveyIdent = surveyManager.getSurveyIdentifier(surveyCourseNode, courseEntry);
		this.survey = surveyManager.loadSurvey(surveyIdent);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("edit.title");
		setFormContextHelp("Assessment#_survey");
		
		evaluationFormNotChoosen = uifactory.addStaticTextElement("edit.evaluation.form.not.choosen", "edit.evaluation.form",
				translate("edit.evaluation.form.not.choosen"), formLayout);
		evaluationFormLink = uifactory.addFormLink("edit.evaluation.form", "", translate("edit.evaluation.form"), formLayout,
				Link.NONTRANSLATED);
		evaluationFormLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		chooseLink = uifactory.addFormLink("edit.choose", buttonsCont, "btn btn-default o_xsmall");
		chooseLink.setElementCssClass("o_sel_survey_choose_repofile");
		replaceLink = uifactory.addFormLink("edit.replace", buttonsCont, "btn btn-default o_xsmall");
		editLink = uifactory.addFormLink("edit.edit", buttonsCont, "btn btn-default o_xsmall");
		
		updateUI();
	}
	
	private void updateUI() {
		boolean replacePossible = surveyManager.isFormUpdateable(survey);
		boolean hasRepoConfig = survey != null;
		RepositoryEntry formEntry = survey != null? survey.getFormEntry(): null;
		
		if (hasRepoConfig && formEntry == null) {
			hasRepoConfig = false;
			showError("error.repo.entry.missing");
		}
		
		if (formEntry != null) {
			String displayname = StringHelper.escapeHtml(formEntry.getDisplayname());
			evaluationFormLink.setI18nKey(displayname);
			flc.setDirty(true);
		}
		evaluationFormNotChoosen.setVisible(!hasRepoConfig);
		chooseLink.setVisible(!hasRepoConfig);
		evaluationFormLink.setVisible(hasRepoConfig);
		replaceLink.setVisible(hasRepoConfig && replacePossible);
		editLink.setVisible(hasRepoConfig);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == chooseLink || source == replaceLink) {
			doChooseQuestionnaire(ureq);
		} else if (source == editLink) {
			doEditevaluationForm(ureq);
		} else if (source == evaluationFormLink) {
			doPreviewEvaluationForm(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (searchCtrl == source) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				doReplaceEvaluationForm(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == previewCtr) {
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(previewCtr);
		removeAsListenerAndDispose(searchCtrl);
		removeAsListenerAndDispose(cmc);
		previewCtr = null;
		searchCtrl = null;
		cmc = null;
	}

	private void doChooseQuestionnaire(UserRequest ureq) {
		searchCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq,
				EvaluationFormResource.TYPE_NAME, translate("edit.choose.evaluation.form"));
		this.listenTo(searchCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				searchCtrl.getInitialComponent(), true, translate("edit.choose.evaluation.form"));
		cmc.activate();
	}

	private void doReplaceEvaluationForm(UserRequest ureq) {
		RepositoryEntry formEntry = searchCtrl.getSelectedEntry();
		if (formEntry != null) {
			if (survey == null) {
				survey = surveyManager.createSurvey(surveyIdent, formEntry);
			} else {
				boolean isFormUpdateable = surveyManager.isFormUpdateable(survey);
				if (isFormUpdateable) {
					survey = surveyManager.updateSurveyForm(survey, formEntry);
				} else {
					showError("error.repo.entry.not.replaceable");
				}
			}
			updateUI();
			
			SurveyCourseNode.setEvaluationFormReference(formEntry, moduleConfiguration);
			// fire event so the updated config is saved
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
	}

	private void doEditevaluationForm(UserRequest ureq) {
		RepositoryEntry re = survey.getFormEntry();
		if (re == null) {
			showError("error.repo.entry.missing");
		} else {
			String bPath = "[RepositoryEntry:" + re.getKey() + "][Editor:0]";
			NewControllerFactory.getInstance().launch(bPath, ureq, getWindowControl());
		}
	}

	private void doPreviewEvaluationForm(UserRequest ureq) {
		File formFile = surveyManager.getFormFile(survey);
		DataStorage storage = surveyManager.loadStorage(survey);
		Controller controller = new EvaluationFormExecutionController(ureq, getWindowControl(), formFile, storage);

		previewCtr = new LayoutMain3ColsPreviewController(ureq, getWindowControl(), null,
				controller.getInitialComponent(), null);
		previewCtr.addDisposableChildController(controller);
		previewCtr.activate();
		listenTo(previewCtr);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
