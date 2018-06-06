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

import static org.olat.modules.forms.handler.EvaluationFormResource.FORM_XML_FILE;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.survey.SurveyEditController;
import org.olat.course.nodes.survey.SurveyRunController;
import org.olat.course.nodes.survey.SurveyRunSecurityCallback;
import org.olat.course.nodes.survey.SurveyStatisticResourceResult;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.statistic.StatisticResourceOption;
import org.olat.course.statistic.StatisticResourceResult;
import org.olat.course.statistic.StatisticType;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionStatus;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.FormXStream;
import org.olat.modules.forms.ui.EvaluationFormExcelExport;
import org.olat.modules.forms.ui.LegendNameGenerator;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.SessionInformationLegendNameGenerator;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

/**
 * 
 * Initial date: 23.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SurveyCourseNode extends AbstractAccessableCourseNode {
	
	private static final OLog log = Tracing.createLoggerFor(SurveyCourseNode.class);

	private static final long serialVersionUID = 905046067514602922L;

	public static final String SURVEY_ICON = "o_survey_icon";

	private static final String TYPE = "survey";
	
	public static final int CURRENT_VERSION = 1;
	public static final String CONFIG_KEY_REPOSITORY_SOFTKEY = "repository.softkey";
	public static final String CONFIG_KEY_EXECUTION_BY_OWNER = "execution.by.owner";
	public static final String CONFIG_KEY_EXECUTION_BY_COACH = "execution.by.coach";
	public static final String CONFIG_KEY_EXECUTION_BY_PARTICIPANT = "execution.by.participant";
	public static final String CONFIG_KEY_EXECUTION_BY_GUEST = "execution.by.guest";
	public static final String CONFIG_KEY_REPORT_FOR_OWNER = "report.for.owner";
	public static final String CONFIG_KEY_REPORT_FOR_COACH = "report.for.coach";
	public static final String CONFIG_KEY_REPORT_FOR_PARTICIPANT = "report.for.participant";
	public static final String CONFIG_KEY_REPORT_FOR_GUEST = "report.for.guest";

	public SurveyCourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
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
		updateModuleConfigDefaults(false);
		TabbableController childTabCntrllr	= new SurveyEditController(ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, euce, childTabCntrllr);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		SurveyRunSecurityCallback secCallback = new SurveyRunSecurityCallback(getModuleConfiguration(), userCourseEnv);
		Controller runCtrl = new SurveyRunController(ureq, wControl, userCourseEnv, this, secCallback);
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, runCtrl, this, SURVEY_ICON);
		return new NodeRunConstructionResult(ctrl);
	}
	
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		return createNodeRunConstructionResult(ureq, wControl, userCourseEnv, ne, null).getRunController();
	}
	
	@Override
	public StatisticResourceResult createStatisticNodeResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, StatisticResourceOption options, StatisticType type) {
		if (isStatisticAllowed(type)) {
			RepositoryEntry ores = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			SurveyRunSecurityCallback secCallback = new SurveyRunSecurityCallback(getModuleConfiguration(), userCourseEnv);
			Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
			return new SurveyStatisticResourceResult(ores, getIdent(), identity, secCallback);
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
	public void updateModuleConfigDefaults(boolean isNewNode) {
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			config.setBooleanEntry(CONFIG_KEY_EXECUTION_BY_OWNER, true);
			config.setBooleanEntry(CONFIG_KEY_EXECUTION_BY_COACH, true);
			config.setBooleanEntry(CONFIG_KEY_EXECUTION_BY_PARTICIPANT, true);
			config.setBooleanEntry(CONFIG_KEY_EXECUTION_BY_GUEST, true);
			config.setBooleanEntry(CONFIG_KEY_REPORT_FOR_OWNER, true);
			config.setBooleanEntry(CONFIG_KEY_REPORT_FOR_COACH, true);
			config.setBooleanEntry(CONFIG_KEY_REPORT_FOR_PARTICIPANT, true);
			config.setBooleanEntry(CONFIG_KEY_REPORT_FOR_GUEST, true);
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
			postImportCopy(course);
		} else {
			removeEvaluationFormReference(getModuleConfiguration());
		}
	}
	
	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCrourse) {
		super.postCopy(envMapper, processType, course, sourceCrourse);
		postImportCopy(course);
	}

	private void postImportCopy(ICourse course) {
		RepositoryEntry ores = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
		EvaluationFormManager evaluationFormManager = CoreSpringFactory.getImpl(EvaluationFormManager.class);
		RepositoryEntry formEntry = getEvaluationForm(getModuleConfiguration());
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(ores, getIdent());
		if (survey == null) {
			survey = evaluationFormManager.createSurvey(ores, getIdent(), formEntry);
		} else {
			boolean isFormUpdateable = evaluationFormManager.isFormUpdateable(survey);
			if (isFormUpdateable) {
				survey = evaluationFormManager.updateSurveyForm(survey, formEntry);
			}
		}
	}

	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options, ZipOutputStream exportStream,
			String charset) {
		RepositoryEntry ores = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
		EvaluationFormManager evaluationFormManager = CoreSpringFactory.getImpl(EvaluationFormManager.class);
		EvaluationFormSurvey survey = evaluationFormManager.loadSurvey(ores, getIdent());
		List<EvaluationFormSession> sessions = evaluationFormManager.loadSessionsBySurvey(survey,
				EvaluationFormSessionStatus.done);

		File repositoryDir = new File(
				FileResourceManager.getInstance().getFileResourceRoot(survey.getFormEntry().getOlatResource()),
				FileResourceManager.ZIPDIR);
		File formFile = new File(repositoryDir, FORM_XML_FILE);
		Form form = (Form) XStreamHelper.readObject(FormXStream.getXStream(), formFile);

		LegendNameGenerator legendNameGenerator = new SessionInformationLegendNameGenerator(sessions);
		ReportHelper reportHelper = ReportHelper.builder(locale).withLegendNameGenrator(legendNameGenerator).build();

		EvaluationFormExcelExport evaluationFormExport = new EvaluationFormExcelExport(form, sessions, reportHelper,
				getShortName());
		try {
			evaluationFormExport.export(exportStream);
		} catch (IOException e) {
			log.error("", e);
			return false;
		}
		return true;
	}
	
	public static RepositoryEntry getEvaluationForm(ModuleConfiguration config) {
		if (config == null) return null;
		
		String repoSoftkey = config.getStringValue(CONFIG_KEY_REPOSITORY_SOFTKEY);
		if (!StringHelper.containsNonWhitespace(repoSoftkey)) return null;
		
		//TODO uh soft deletes?
		return RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repoSoftkey, false);
	}
	
	public static void setEvaluationFormReference(RepositoryEntry re, ModuleConfiguration moduleConfig) {
		moduleConfig.set(CONFIG_KEY_REPOSITORY_SOFTKEY, re.getSoftkey());
	}
	
	public static void removeEvaluationFormReference(ModuleConfiguration moduleConfig) {
		moduleConfig.remove(CONFIG_KEY_REPOSITORY_SOFTKEY);
	}
}

