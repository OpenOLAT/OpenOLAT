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
package org.olat.course.nodes.survey;

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
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.SurveyCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SurveyConfigController extends FormBasicController {

	private StaticTextElement questionnaireNotChoosen;
	private FormLink questionnaireLink;
	private FormLink chooseLink;
	private FormLink replaceLink;
	private FormLink editLink;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController searchCtrl;
	private LayoutMain3ColsPreviewController previewCtr;
	
	private final OLATResourceable ores;
	private final String subIdent;
	private final ModuleConfiguration moduleConfiguration;
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public SurveyConfigController(UserRequest ureq, WindowControl wControl, ICourse course,
			SurveyCourseNode surveyCourseNode) {
		super(ureq, wControl);
		this.ores = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
		this.subIdent = surveyCourseNode.getIdent();
		this.moduleConfiguration = surveyCourseNode.getModuleConfiguration();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("edit.title");
		setFormContextHelp("Assessment#_survey");
		
		questionnaireNotChoosen = uifactory.addStaticTextElement("edit.questionnaire.not.choosen", "edit.questionnaire",
				translate("edit.questionnaire.not.choosen"), formLayout);
		questionnaireLink = uifactory.addFormLink("edit.questionnaire", "", translate("edit.questionnaire"), formLayout,
				Link.NONTRANSLATED);
		questionnaireLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		chooseLink = uifactory.addFormLink("edit.choose", buttonsCont, "btn btn-default o_xsmall");
		replaceLink = uifactory.addFormLink("edit.replace", buttonsCont, "btn btn-default o_xsmall");
		editLink = uifactory.addFormLink("edit.edit", buttonsCont, "btn btn-default o_xsmall");
		
		updateUI();
	}

	private void updateUI() {
		RepositoryEntry re = SurveyCourseNode.getSurvey(moduleConfiguration);
		updateUI(re);
	}

	private void updateUI(RepositoryEntry re) {
		boolean hasSessions = evaluationFormManager.hasSessions(ores, subIdent);
		String repoKey = moduleConfiguration.getStringValue(SurveyCourseNode.CONFIG_KEY_REPOSITORY_SOFTKEY);
		boolean hasRepoConfig = StringHelper.containsNonWhitespace(repoKey);
		
		if (hasRepoConfig && re == null) {
			hasRepoConfig = false;
			showError("error.repo.entry.missing");
		}
		
		if (re != null) {
			String displayname = StringHelper.escapeHtml(re.getDisplayname());
			questionnaireLink.setI18nKey(displayname);
			flc.setDirty(true);
		}
		questionnaireNotChoosen.setVisible(!hasRepoConfig);
		chooseLink.setVisible(!hasRepoConfig);
		questionnaireLink.setVisible(hasRepoConfig);
		replaceLink.setVisible(hasRepoConfig && !hasSessions);
		editLink.setVisible(hasRepoConfig);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == chooseLink || source == replaceLink) {
			doChooseQuestionnaire(ureq);
		} else if (source == editLink) {
			doEditQuestionnaire(ureq);
		} else if (source == questionnaireLink) {
			doPreviewQuestionnaire(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (searchCtrl == source) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				doReplaceQuestionnaire(ureq);
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
				EvaluationFormResource.TYPE_NAME, translate("edit.choose.questionnaire"));
		this.listenTo(searchCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				searchCtrl.getInitialComponent(), true, translate("edit.choose.questionnaire"));
		cmc.activate();
	}

	private void doReplaceQuestionnaire(UserRequest ureq) {
		RepositoryEntry re = searchCtrl.getSelectedEntry();
		if (re != null) {
			moduleConfiguration.set(SurveyCourseNode.CONFIG_KEY_REPOSITORY_SOFTKEY, re.getSoftkey());
			updateUI(re);
			// fire event so the updated config is saved by the
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
	}

	private void doEditQuestionnaire(UserRequest ureq) {
		RepositoryEntry re = SurveyCourseNode.getSurvey(moduleConfiguration);
		if (re == null) {
			showError("error.repo.entry.missing");
		} else {
			String bPath = "[RepositoryEntry:" + re.getKey() + "][Editor:0]";
			NewControllerFactory.getInstance().launch(bPath, ureq, getWindowControl());
		}
	}

	private void doPreviewQuestionnaire(UserRequest ureq) {
		RepositoryEntry re = SurveyCourseNode.getSurvey(moduleConfiguration);
		Controller controller = new EvaluationFormExecutionController(ureq, getWindowControl(), null, null, re, false,
				false);
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
