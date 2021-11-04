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
package org.olat.course.nodes;

import static org.olat.modules.forms.EvaluationFormSurveyIdentifier.of;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.editor.importnodes.ImportSettings;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.noderight.NodeRight;
import org.olat.course.noderight.NodeRightGrant.NodeRightRole;
import org.olat.course.noderight.NodeRightService;
import org.olat.course.noderight.NodeRightType;
import org.olat.course.noderight.NodeRightTypeBuilder;
import org.olat.course.nodes.survey.SurveyRunSecurityCallback;
import org.olat.course.nodes.survey.ui.SurveyEditController;
import org.olat.course.nodes.survey.ui.SurveyRunController;
import org.olat.course.nodes.survey.ui.SurveyStatisticResourceResult;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.statistic.StatisticResourceOption;
import org.olat.course.statistic.StatisticResourceResult;
import org.olat.course.statistic.StatisticType;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.ui.EvaluationFormExcelExport;
import org.olat.modules.forms.ui.LegendNameGenerator;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.ReportHelperUserColumns;
import org.olat.modules.forms.ui.SessionInformationLegendNameGenerator;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;

/**
 * 
 * Initial date: 23.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SurveyCourseNode extends AbstractAccessableCourseNode {
	
	private static final Logger log = Tracing.createLoggerFor(SurveyCourseNode.class);

	private static final long serialVersionUID = 905046067514602922L;

	public static final String TYPE = "survey";
	public static final String SURVEY_ICON = "o_survey_icon";

	private static final int CURRENT_VERSION = 3;
	public static final String CONFIG_KEY_REPOSITORY_SOFTKEY = "repository.softkey";
	
	private static final String LEGACY_KEY_EXECUTION_BY_OWNER = "execution.by.owner";
	private static final String LEGACY_KEY_EXECUTION_BY_COACH = "execution.by.coach";
	private static final String LEGACY_KEY_EXECUTION_BY_PARTICIPANT = "execution.by.participant";
	private static final String LEGACY_KEY_EXECUTION_BY_GUEST = "execution.by.guest";
	private static final String LEGACY_KEY_REPORT_FOR_OWNER = "report.for.owner";
	private static final String LEGACY_KEY_REPORT_FOR_COACH = "report.for.coach";
	private static final String LEGACY_KEY_REPORT_FOR_PARTICIPANT = "report.for.participant";
	private static final String LEGACY_KEY_REPORT_FOR_GUEST = "report.for.guest";
	
	public static final NodeRightType EXECUTION = NodeRightTypeBuilder.ofIdentifier("execution")
			.setLabel(SurveyEditController.class, "edit.execution")
			.addRole(NodeRightRole.owner, false)
			.addRole(NodeRightRole.coach, false)
			.addRole(NodeRightRole.participant, true)
			.addRole(NodeRightRole.guest, false)
			.build();
	public static final NodeRightType REPORT = NodeRightTypeBuilder.ofIdentifier("report")
			.setLabel(SurveyEditController.class, "edit.report")
			.addRole(NodeRightRole.owner, false)
			.addRole(NodeRightRole.coach, true)
			.addRole(NodeRightRole.participant, false)
			.addRole(NodeRightRole.guest, false)
			.build();
	public static final List<NodeRightType> NODE_RIGHT_TYPES = List.of(EXECUTION, REPORT);

	public SurveyCourseNode() {
		super(TYPE);
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return getEvaluationForm(getModuleConfiguration());
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) {
			return oneClickStatusCache[0];
		}

		StatusDescription sd = StatusDescription.NOERROR;
		String repoKey = getModuleConfiguration().getStringValue(CONFIG_KEY_REPOSITORY_SOFTKEY);
		boolean repoKeyMissing = !StringHelper.containsNonWhitespace(repoKey);
		if (repoKeyMissing) {
			String shortKey = "error.repo.no.key.short";
			String longKey = "error.repo.no.key.long";
			String[] params = new String[] { this.getShortTitle() };
			String translPackage = Util.getPackageName(SurveyEditController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(SurveyEditController.PANE_TAB_CONFIG);
		}
		return sd;
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment euce) {
		TabbableController childTabCtrl	= new SurveyEditController(ureq, wControl, this, course);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCtrl);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		SurveyRunSecurityCallback secCallback = new SurveyRunSecurityCallback(getModuleConfiguration(), userCourseEnv);
		Controller runCtrl = new SurveyRunController(ureq, wControl, userCourseEnv, this, secCallback);
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, runCtrl, userCourseEnv, this, SURVEY_ICON);
		return new NodeRunConstructionResult(ctrl);
	}
	
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		return createNodeRunConstructionResult(ureq, wControl, userCourseEnv, nodeSecCallback, null).getRunController();
	}
	
	@Override
	public StatisticResourceResult createStatisticNodeResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, StatisticResourceOption options, StatisticType type) {
		if (isStatisticAllowed(type)) {
			RepositoryEntry ores = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			SurveyRunSecurityCallback secCallback = new SurveyRunSecurityCallback(getModuleConfiguration(), userCourseEnv);
			Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
			return new SurveyStatisticResourceResult(ores, this, identity, secCallback);
		}
		return null;
	}
	
	@Override
	public boolean isStatisticNodeResultAvailable(UserCourseEnvironment userCourseEnv, StatisticType type) {
		return isStatisticAllowed(type);
	}
	
	private boolean isStatisticAllowed(StatisticType type) {
		if(StatisticType.SURVEY.equals(type)) {
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		String translatorStr = Util.getPackageName(ConditionEditController.class);
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
	}
	
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType);
		
		ModuleConfiguration config = getModuleConfiguration();
		int version = config.getConfigurationVersion();
		
		if (version < 2 && config.has(LEGACY_KEY_EXECUTION_BY_OWNER)) {
			NodeRightService nodeRightService = CoreSpringFactory.getImpl(NodeRightService.class);
			// Execution
			NodeRight executionRight = nodeRightService.getRight(config, EXECUTION);
			Collection<NodeRightRole> executionRoles = new ArrayList<>(4);
			if (config.getBooleanSafe(LEGACY_KEY_EXECUTION_BY_OWNER)) {
				executionRoles.add(NodeRightRole.owner);
			}
			if (config.getBooleanSafe(LEGACY_KEY_EXECUTION_BY_COACH)) {
				executionRoles.add(NodeRightRole.coach);
			}
			if (config.getBooleanSafe(LEGACY_KEY_EXECUTION_BY_PARTICIPANT)) {
				executionRoles.add(NodeRightRole.participant);
			}
			if (config.getBooleanSafe(LEGACY_KEY_EXECUTION_BY_GUEST)) {
				executionRoles.add(NodeRightRole.guest);
			}
			nodeRightService.setRoleGrants(executionRight, executionRoles);
			nodeRightService.setRight(config, executionRight);
			// Report
			NodeRight reportRight = nodeRightService.getRight(config, REPORT);
			Collection<NodeRightRole> reportRoles = new ArrayList<>(4);
			if (config.getBooleanSafe(LEGACY_KEY_REPORT_FOR_OWNER)) {
				reportRoles.add(NodeRightRole.owner);
			}
			if (config.getBooleanSafe(LEGACY_KEY_REPORT_FOR_COACH)) {
				reportRoles.add(NodeRightRole.coach);
			}
			if (config.getBooleanSafe(LEGACY_KEY_REPORT_FOR_PARTICIPANT)) {
				reportRoles.add(NodeRightRole.participant);
			}
			if (config.getBooleanSafe(LEGACY_KEY_REPORT_FOR_GUEST)) {
				reportRoles.add(NodeRightRole.guest);
			}
			nodeRightService.setRoleGrants(reportRight, reportRoles);
			nodeRightService.setRight(config, reportRight);
			// Remove legacy
			config.remove(LEGACY_KEY_EXECUTION_BY_OWNER);
			config.remove(LEGACY_KEY_EXECUTION_BY_COACH);
			config.remove(LEGACY_KEY_EXECUTION_BY_PARTICIPANT);
			config.remove(LEGACY_KEY_EXECUTION_BY_GUEST);
			config.remove(LEGACY_KEY_REPORT_FOR_OWNER);
			config.remove(LEGACY_KEY_REPORT_FOR_COACH);
			config.remove(LEGACY_KEY_REPORT_FOR_PARTICIPANT);
			config.remove(LEGACY_KEY_REPORT_FOR_GUEST);
		}
		if (version < 3) {
			NodeRightService nodeRightService = CoreSpringFactory.getImpl(NodeRightService.class);
			nodeRightService.initDefaults(config, NODE_RIGHT_TYPES);
		}
		
		config.setConfigurationVersion(CURRENT_VERSION);
	}

	@Override
	public void exportNode(File exportDirectory, ICourse course) {
		RepositoryEntry re = getEvaluationForm(getModuleConfiguration());
		if (re == null) return;
		
		File fExportDirectory = new File(exportDirectory, getIdent());
		fExportDirectory.mkdirs();
		RepositoryEntryImportExport reie = new RepositoryEntryImportExport(re, fExportDirectory);
		reie.exportDoExport();
	}
	
	@Override
	public void importNode(File importDirectory, ICourse course, Identity owner, Organisation organisation, Locale locale, boolean withReferences) {
		RepositoryEntryImportExport rie = new RepositoryEntryImportExport(importDirectory, getIdent());
		if(withReferences && rie.anyExportedPropertiesAvailable()) {
			RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(EvaluationFormResource.TYPE_NAME);
			RepositoryEntry re = handler.importResource(owner, rie.getInitialAuthor(), rie.getDisplayName(),
				rie.getDescription(), false, organisation, locale, rie.importGetExportedFile(), null);
			setEvaluationFormReference(re, getModuleConfiguration());
			postImportCopy(course, getIdent());
		} else {
			removeEvaluationFormReference(getModuleConfiguration());
		}
	}
	
	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCourse, CopyCourseContext context) {
		super.postCopy(envMapper, processType, course, sourceCourse, context);
		postImportCopy(course, getIdent());
	}
	
	@Override
	public CourseNode createInstanceForCopy(boolean isNewTitle, ICourse course, Identity author) {
		CourseNode copyInstance = super.createInstanceForCopy(isNewTitle, course, author);
		postImportCopy(course, copyInstance.getIdent());
		return copyInstance;
	}

	@Override
	public void postImportCourseNodes(ICourse course, CourseNode sourceCourseNode, ICourse sourceCourse,
			ImportSettings settings, CourseEnvironmentMapper envMapper) {
		super.postImportCourseNodes(course, sourceCourseNode, sourceCourse, settings, envMapper);
		postImportCopy(course, getIdent());
	}

	private void postImportCopy(ICourse course, String nodeIdent) {
		RepositoryEntry formEntry = getEvaluationForm(getModuleConfiguration());
		if (formEntry == null) return;
		
		RepositoryEntry ores = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
		EvaluationFormManager evaluationFormManager = CoreSpringFactory.getImpl(EvaluationFormManager.class);
		EvaluationFormSurveyIdentifier surveyIdent = of(ores, nodeIdent);
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(surveyIdent);
		if (survey == null) {
			survey = evaluationFormManager.createSurvey(surveyIdent, formEntry);
		} else {
			boolean isFormUpdateable = evaluationFormManager.isFormUpdateable(survey);
			if (isFormUpdateable) {
				survey = evaluationFormManager.updateSurveyForm(survey, formEntry);
			}
		}
	}

	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options,
			ZipOutputStream exportStream, String archivePath, String charset) {
		EvaluationFormManager evaluationFormManager = CoreSpringFactory.getImpl(EvaluationFormManager.class);
		
		try {
			RepositoryEntry ores = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
			EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(of(ores, getIdent()));
			SessionFilter filter = SessionFilterFactory.createSelectDone(survey);
			Form form = evaluationFormManager.loadForm(survey.getFormEntry());
			
			LegendNameGenerator legendNameGenerator = new SessionInformationLegendNameGenerator(filter);
			ReportHelper reportHelper = ReportHelper.builder(locale).withLegendNameGenrator(legendNameGenerator).build();
			ReportHelperUserColumns userColumns = new ReportHelperUserColumns(reportHelper);
			
			EvaluationFormExcelExport evaluationFormExport = new EvaluationFormExcelExport(form, filter,
					reportHelper.getComparator(), userColumns, getShortName());
			evaluationFormExport.export(exportStream, archivePath);
		} catch (IOException e) {
			log.error("", e);
			return false;
		}
		return true;
	}
	
	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		
		EvaluationFormManager evaluationFormManager = CoreSpringFactory.getImpl(EvaluationFormManager.class);
		RepositoryEntry ores = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(of(ores, getIdent()));
		evaluationFormManager.deleteSurvey(survey);
	}

	public static RepositoryEntry getEvaluationForm(ModuleConfiguration config) {
		if (config == null) return null;
		
		String repoSoftkey = config.getStringValue(CONFIG_KEY_REPOSITORY_SOFTKEY);
		if (!StringHelper.containsNonWhitespace(repoSoftkey)) return null;

		return RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repoSoftkey, false);
	}
	
	public static void setEvaluationFormReference(RepositoryEntry re, ModuleConfiguration moduleConfig) {
		moduleConfig.set(CONFIG_KEY_REPOSITORY_SOFTKEY, re.getSoftkey());
	}
	
	public static void removeEvaluationFormReference(ModuleConfiguration moduleConfig) {
		moduleConfig.remove(CONFIG_KEY_REPOSITORY_SOFTKEY);
	}
	
	@Override
	public List<NodeRightType> getNodeRightTypes() {
		return NODE_RIGHT_TYPES;
	}
}

