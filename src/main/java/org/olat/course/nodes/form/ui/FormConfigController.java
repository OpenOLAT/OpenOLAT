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
package org.olat.course.nodes.form.ui;

import java.io.File;
import java.util.Date;

import org.olat.NewControllerFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsPreviewController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.TranslatorHelper;
import org.olat.core.util.StringHelper;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.nodes.SurveyCourseNode;
import org.olat.course.nodes.form.FormManager;
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
 * Initial date: 21.04.2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormConfigController extends FormBasicController {
	
	private static final String[] ON_KEYS = new String[]{ "on" };

	private StaticTextElement evaluationFormNotChoosen;
	private FormLink evaluationFormLink;
	private FormLink chooseLink;
	private FormLink replaceLink;
	private FormLink editLink;
	private DateChooser participationDeadlineEl;
	private MultipleSelectionElement confirmationEl;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController searchCtrl;
	private LayoutMain3ColsPreviewController previewCtr;
	
	private final ModuleConfiguration config;
	private final EvaluationFormSurveyIdentifier surveyIdent;
	private EvaluationFormSurvey survey;
	
	@Autowired
	private FormManager formManager;

	public FormConfigController(UserRequest ureq, WindowControl wControl, FormCourseNode formCourseNode,
			RepositoryEntry courseEntry) {
		super(ureq, wControl);
		this.config = formCourseNode.getModuleConfiguration();
		this.surveyIdent = formManager.getSurveyIdentifier(formCourseNode, courseEntry);
		this.survey = formManager.loadSurvey(surveyIdent);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("edit.title");
		
		evaluationFormNotChoosen = uifactory.addStaticTextElement("edit.evaluation.form.not.choosen", "edit.evaluation.form",
				translate("edit.evaluation.form.not.choosen"), formLayout);
		evaluationFormLink = uifactory.addFormLink("edit.evaluation.form", "", translate("edit.evaluation.form"), formLayout,
				Link.NONTRANSLATED);
		evaluationFormLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		chooseLink = uifactory.addFormLink("edit.choose", buttonsCont, "btn btn-default o_xsmall");
		replaceLink = uifactory.addFormLink("edit.replace", buttonsCont, "btn btn-default o_xsmall");
		editLink = uifactory.addFormLink("edit.edit", buttonsCont, "btn btn-default o_xsmall");
		
		Date participationDeadline = config.getDateValue(FormCourseNode.CONFIG_KEY_PARTICIPATION_DEADLINE);
		participationDeadlineEl = uifactory.addDateChooser("edit.participation.deadline", participationDeadline, formLayout);
		participationDeadlineEl.setDateChooserTimeEnabled(true);
		participationDeadlineEl.addActionListener(FormEvent.ONCHANGE);
		
		confirmationEl = uifactory.addCheckboxesVertical("edit.confirmation.enabled", formLayout, ON_KEYS,
				TranslatorHelper.translateAll(getTranslator(), ON_KEYS), 1);
		confirmationEl.setHelpTextKey("edit.confirmation.help", null);
		confirmationEl.addActionListener(FormEvent.ONCHANGE);
		boolean confirmationEnabled = config.getBooleanSafe(FormCourseNode.CONFIG_KEY_CONFIRMATION_ENABLED);
		confirmationEl.select(confirmationEl.getKey(0), confirmationEnabled);
		
		updateUI();
	}
	
	private void updateUI() {
		boolean replacePossible = formManager.isFormUpdateable(survey);
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
			doChooseEvaluationForm(ureq);
		} else if (source == editLink) {
			doEditevaluationForm(ureq);
		} else if (source == evaluationFormLink) {
			doPreviewEvaluationForm(ureq);
		} else if (source == participationDeadlineEl) {
			setParticipationDeadline(ureq);
		} else if (source == confirmationEl) {
			setConfirmation(ureq);
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

	private void doChooseEvaluationForm(UserRequest ureq) {
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
				survey = formManager.createSurvey(surveyIdent, formEntry);
			} else {
				boolean isFormUpdateable = formManager.isFormUpdateable(survey);
				if (isFormUpdateable) {
					survey = formManager.updateSurveyForm(survey, formEntry);
				} else {
					showError("error.repo.entry.not.replaceable");
				}
			}
			updateUI();
			
			SurveyCourseNode.setEvaluationFormReference(formEntry, config);
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
		File formFile = formManager.getFormFile(survey);
		DataStorage storage = formManager.loadStorage(survey);
		Controller controller = new EvaluationFormExecutionController(ureq, getWindowControl(), formFile, storage,
				FormCourseNode.EMPTY_STATE);
		
		previewCtr = new LayoutMain3ColsPreviewController(ureq, getWindowControl(), null,
				controller.getInitialComponent(), null);
		previewCtr.addDisposableChildController(controller);
		previewCtr.activate();
		listenTo(previewCtr);
	}
	
	private void setParticipationDeadline(UserRequest ureq) {
		config.setDateValue(FormCourseNode.CONFIG_KEY_PARTICIPATION_DEADLINE, participationDeadlineEl.getDate());
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}
	
	private void setConfirmation(UserRequest ureq) {
		config.setBooleanEntry(FormCourseNode.CONFIG_KEY_CONFIRMATION_ENABLED, confirmationEl.isAtLeastSelected(1));
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
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
