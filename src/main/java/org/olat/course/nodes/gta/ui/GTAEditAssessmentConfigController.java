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
package org.olat.course.nodes.gta.ui;

import static org.olat.modules.forms.handler.EvaluationFormResource.FORM_XML_FILE;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.editor.CourseNodeReferenceProvider;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.FormCourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.ms.MSEditFormController;
import org.olat.course.nodes.ms.MSService;
import org.olat.course.nodes.ms.MinMax;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryReferenceController;
import org.olat.repository.ui.RepositoryEntryReferenceProvider.ReferenceContentProvider;
import org.olat.repository.ui.RepositoryEntryReferenceProvider.SettingsContentProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 avr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAEditAssessmentConfigController extends BasicController implements ReferenceContentProvider, SettingsContentProvider {

	private static final List<String> RESOURCE_TYPES = List.of(EvaluationFormResource.TYPE_NAME);
	
	private final VelocityContainer mainVC;
	private final BreadcrumbPanel stackPanel;
	private final IconPanelLabelTextContent iconPanelContent;
	private final IconPanelLabelTextContent iconPanelSettings;

	private final NodeAccessType nodeAccessType;
	private ModuleConfiguration moduleConfiguration;

	private MSEditFormController manualAssessmentCtrl;
	private EvaluationFormExecutionController previewCtr;
	private RepositoryEntryReferenceController referenceCtrl;
	private GTAEditEvaluationConfigController evaluationOnOffCtrl;
	
	@Autowired
	private MSService msService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	public GTAEditAssessmentConfigController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			GTACourseNode gtaNode, ICourse course) {
		super(ureq, wControl);
		moduleConfiguration = gtaNode.getModuleConfiguration();
		nodeAccessType = NodeAccessType.of(course);
		this.stackPanel = stackPanel;
		
		mainVC = createVelocityContainer("edit_assessment_config");
		
		evaluationOnOffCtrl = new GTAEditEvaluationConfigController(ureq, getWindowControl(), moduleConfiguration);
		listenTo(evaluationOnOffCtrl);
		mainVC.put("evaluationOnOff", evaluationOnOffCtrl.getInitialComponent());
		
		iconPanelContent = new IconPanelLabelTextContent("content");
		iconPanelSettings = new IconPanelLabelTextContent("content");
		
		EmptyStateConfig emptyStateConfig = EmptyStateConfig.builder()
				.withMessageTranslated(translate("no.form.resource.selected"))
				.withIconCss("o_icon o_FileResource-FORM_icon")
				.build();
		String selectionTitle = translate("select.form");
		CourseNodeReferenceProvider referenceProvider = new GTACourseNodeReferenceProvider(repositoryService,
				RESOURCE_TYPES, emptyStateConfig, selectionTitle, this, this);
		RepositoryEntry formEntry = MSCourseNode.getEvaluationForm(moduleConfiguration);
		referenceCtrl = new RepositoryEntryReferenceController(ureq, wControl, formEntry, referenceProvider);
		listenTo(referenceCtrl);
		mainVC.put("reference", referenceCtrl.getInitialComponent());
		
		manualAssessmentCtrl = new MSEditFormController(ureq, getWindowControl(), course, gtaNode, nodeAccessType,
				translate("grading.configuration.title"), "manual_user/learningresources/Course_Element_Task/");
		MinMax minMax = calculateMinMax();
		manualAssessmentCtrl.setEvaluationOn(ureq, evaluationOnOffCtrl.isEvaluationEnabled(), minMax, null);
		listenTo(manualAssessmentCtrl);
		mainVC.put("scoring", manualAssessmentCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
		updateUI(ureq);
	}
	
	public class GTACourseNodeReferenceProvider extends CourseNodeReferenceProvider {
		
		private final SettingsContentProvider settingsProvider;
		
		public GTACourseNodeReferenceProvider(RepositoryService repositoryService, List<String> resourceTypes,
				EmptyStateConfig emptyStateConfig, String selectionTitle, ReferenceContentProvider referenceContentProvider,
				SettingsContentProvider settingsProvider) {
			super(repositoryService, resourceTypes,  emptyStateConfig, selectionTitle, referenceContentProvider);
			this.settingsProvider = settingsProvider;
		}

		@Override
		public boolean hasSettings() {
			return true;
		}

		@Override
		public SettingsContentProvider getSettingsContentProvider() {
			return settingsProvider;
		}
	}
	
	@Override
	public Controller getEditSettingsController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		EvaluationFormSettingsController settingsCtrl = new EvaluationFormSettingsController(ureq, wControl, moduleConfiguration);
		listenTo(settingsCtrl);
		return settingsCtrl;
	}

	@Override
	public Component getSettingsContent(RepositoryEntry repositoryEntry) {
		updateSettingsPanel();
		return iconPanelSettings;
	}
	
	private void updateSettingsPanel() {
		String scoreKey = moduleConfiguration.getStringValue(MSCourseNode.CONFIG_KEY_SCORE_EVAL_FORM);
		String i18nScoreKey;
		if(MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM.equals(scoreKey)) {
			i18nScoreKey = "score.evaluation.points.sum";
		} else if(MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG.equals(scoreKey)) {
			i18nScoreKey = "score.evaluation.points.avg";
		} else {
			i18nScoreKey = "score.evaluation.points.undefined";
		}

		List<IconPanelLabelTextContent.LabelText> labelTexts = new ArrayList<>(4);
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("score.evaluation.points"), translate(i18nScoreKey)));
		iconPanelSettings.setLabelTexts(labelTexts);
	}

	@Override
	public void refreshSettings(Component cmp, RepositoryEntry repositoryEntry) {
		//
	}

	@Override
	public Component getContent(RepositoryEntry repositoryEntry) {
		MinMax minMaxAvg = msService.calculateMinMaxAvg(repositoryEntry, 1.0f);
		MinMax minMaxSum = msService.calculateMinMaxSum(repositoryEntry, 1.0f);
		
		List<IconPanelLabelTextContent.LabelText> labelTexts = new ArrayList<>(4);
		if (minMaxSum != null) {
			labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("score.evaluation.sum"), AssessmentHelper.getRoundedScore(minMaxSum.getMax())));
		}
		if (minMaxAvg != null) {
			labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("score.evaluation.avg"), AssessmentHelper.getRoundedScore(minMaxAvg.getMax())));
		}
		iconPanelContent.setLabelTexts(labelTexts);
		return iconPanelContent;
	}

	@Override
	public void refresh(Component cmp, RepositoryEntry repositoryEntry) {
		// Refresh is handled on change event.
	}
	
	public void updateModuleConfiguration(ModuleConfiguration config) {
		moduleConfiguration = config;
		manualAssessmentCtrl.updateModuleConfiguration(moduleConfiguration);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(evaluationOnOffCtrl == source) {
			updateUI(ureq);
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		} else if (referenceCtrl == source) {
			if (event == RepositoryEntryReferenceController.SELECTION_EVENT) {
				doSaveEvaluation(ureq, referenceCtrl.getRepositoryEntry());
				updateUI(ureq);
			} else if (event == RepositoryEntryReferenceController.PREVIEW_EVENT) {
				doPreview(ureq, referenceCtrl.getRepositoryEntry());
			}
		} else if(manualAssessmentCtrl == source) {
			if(event == Event.DONE_EVENT) {
				manualAssessmentCtrl.updateModuleConfiguration(moduleConfiguration);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if(source instanceof EvaluationFormSettingsController) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
				updateSettingsPanel();
				updateUI(ureq);
			}
			removeAsListenerAndDispose(source);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private void updateUI(UserRequest ureq) {
		boolean evaluationEnabled = evaluationOnOffCtrl.isEvaluationEnabled();
		referenceCtrl.getInitialComponent().setVisible(evaluationEnabled);

		MinMax minMax = calculateMinMax();
		String evalScoringMethod = moduleConfiguration.getStringValue(MSCourseNode.CONFIG_KEY_SCORE_EVAL_FORM);
		manualAssessmentCtrl.setEvaluationOn(ureq, evaluationEnabled, minMax, evalScoringMethod);
	}
	
	private MinMax calculateMinMax() {
		RepositoryEntry formEntry = MSCourseNode.getEvaluationForm(moduleConfiguration);
		if (formEntry == null) {
			return null;
		}
		
		String scoreKey = moduleConfiguration.getStringValue(MSCourseNode.CONFIG_KEY_SCORE_EVAL_FORM);
		
		MinMax formMinMax = null;
		if(StringHelper.containsNonWhitespace(scoreKey)) {
			Float scalingFactor = manualAssessmentCtrl.getEvaluationScale();
			float scale = scalingFactor == null ? 1.0f : scalingFactor.floatValue();
			switch (scoreKey) {
				case MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM:
					formMinMax = msService.calculateMinMaxSum(formEntry, scale);
					break;
				case MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG:
					formMinMax = msService.calculateMinMaxAvg(formEntry, scale);
					break;
				default:
					break;
			}
		}
		return formMinMax;
	}
	
	private void doPreview(UserRequest ureq, RepositoryEntry formEntry) {
		
		File repositoryDir = new File(
				FileResourceManager.getInstance().getFileResourceRoot(formEntry.getOlatResource()),
				FileResourceManager.ZIPDIR);
		File formFile = new File(repositoryDir, FORM_XML_FILE);
		DataStorage storage = evaluationFormManager.loadStorage(formEntry);
		previewCtr = new EvaluationFormExecutionController(ureq, getWindowControl(), formFile, storage,
				FormCourseNode.EMPTY_STATE);
		listenTo(previewCtr);

		stackPanel.pushController(translate("preview"), previewCtr);
	}
	
	private void doSaveEvaluation(UserRequest ureq, RepositoryEntry formEntry) {
		boolean evalFormEnabled = evaluationOnOffCtrl.isEvaluationEnabled();
		moduleConfiguration.setBooleanEntry(MSCourseNode.CONFIG_KEY_EVAL_FORM_ENABLED, evalFormEnabled);
		String currentScoringMethod = moduleConfiguration.getStringValue(MSCourseNode.CONFIG_KEY_SCORE);
		String currentEvalScoringMethod = moduleConfiguration.getStringValue(MSCourseNode.CONFIG_KEY_SCORE_EVAL_FORM);
		if (evalFormEnabled) {
			MSCourseNode.setEvaluationFormReference(formEntry, moduleConfiguration);
			if(!StringHelper.containsNonWhitespace(currentEvalScoringMethod)) {
				moduleConfiguration.setStringValue(MSCourseNode.CONFIG_KEY_SCORE_EVAL_FORM, MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM);
			}
			
			MinMax minMax = calculateMinMax();
			if(minMax != null) {
				moduleConfiguration.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, minMax.getMin());
				moduleConfiguration.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, minMax.getMax());
			}
		} else {
			MSCourseNode.removeEvaluationFormReference(moduleConfiguration);
			moduleConfiguration.remove(MSCourseNode.CONFIG_KEY_SCORE_EVAL_FORM);
			
			if(MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_AVG.equals(currentScoringMethod)
					|| MSCourseNode.CONFIG_VALUE_SCORE_EVAL_FORM_SUM.equals(currentScoringMethod)) {
				moduleConfiguration.setStringValue(MSCourseNode.CONFIG_KEY_SCORE, MSCourseNode.CONFIG_VALUE_SCORE_MANUAL);
			}
		}
		updateSettingsPanel();
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}
}
