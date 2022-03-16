/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes;

import static org.olat.modules.forms.EvaluationFormSessionStatus.done;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NavigableSet;
import java.util.Objects;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.PublishEvents;
import org.olat.course.editor.StatusDescription;
import org.olat.course.learningpath.ui.TabbableLeaningPathNodeConfigController;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.ms.MSAssessmentConfig;
import org.olat.course.nodes.ms.MSCoachRunController;
import org.olat.course.nodes.ms.MSCourseNodeEditController;
import org.olat.course.nodes.ms.MSCourseNodeRunController;
import org.olat.course.nodes.ms.MSLearningPathNodeHandler;
import org.olat.course.nodes.ms.MSService;
import org.olat.course.nodes.ms.MinMax;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.properties.PersistingCoursePropertyManager;
import org.olat.course.reminder.AssessmentReminderProvider;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.OLATResource;

/**
 * Initial Date: Jun 16, 2004
 * 
 * @author gnaegi
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class MSCourseNode extends AbstractAccessableCourseNode {
	
	private static final long serialVersionUID = -7741172700015384397L;
	
	@SuppressWarnings("deprecation")
	private static final String PACKAGE_MS = Util.getPackageName(MSCourseNodeRunController.class);

	public static final int CURRENT_VERSION = 3;
	public static final String TYPE = "ms";
	/** configuration: score can be set */
	public static final String CONFIG_KEY_HAS_SCORE_FIELD = "hasScoreField";
	/** configuration: score min value */
	public static final String CONFIG_KEY_SCORE_MIN = "scoreMin";
	public static final Float CONFIG_DEFAULT_SCORE_MIN = Float.valueOf(0);
	/** configuration: score max value */
	public static final String CONFIG_KEY_SCORE_MAX = "scoreMax";
	public static final Float CONFIG_DEFAULT_SCORE_MAX = Float.valueOf(0);
	/** configuration: grade */
	public static final String CONFIG_KEY_GRADE_ENABLED = "grade.enabled";
	public static final String CONFIG_KEY_GRADE_AUTO = "grade.auto";
	public static final String CONFIG_KEY_GRADE_SYSTEM = "grade.system";
	/** configuration: passed can be set */
	public static final String CONFIG_KEY_HAS_PASSED_FIELD = "hasPassedField";
	/** configuration: passed set to when score higher than cut value */
	public static final String CONFIG_KEY_PASSED_CUT_VALUE = "passedCutValue";
	public static final String CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT = "ignoreInCourseAssessment";
	/** configuration: comment can be set */
	public static final String CONFIG_KEY_HAS_COMMENT_FIELD = "hasCommentField";
	/** configuration: individual assessment document can be set (use getBooleanSafe default false) */
	public static final String CONFIG_KEY_HAS_INDIVIDUAL_ASSESSMENT_DOCS = "hasIndividualAsssessmentDocs";
	/** configuration: infotext for user */
	public static final String CONFIG_KEY_INFOTEXT_USER = "infoTextUser";
	/** configuration: infotext for coach */
	public static final String CONFIG_KEY_INFOTEXT_COACH = "nfoTextCoach";
	
	public static final String CONFIG_KEY_OPTIONAL = "cnOptional";
	public static final String CONFIG_KEY_SCORE = "score";
	public static final String CONFIG_VALUE_SCORE_NONE = "score.none";
	public static final String CONFIG_VALUE_SCORE_MANUAL = "score.manual";
	public static final String CONFIG_VALUE_SCORE_EVAL_FORM_SUM = "score.evaluation.form.sum";
	public static final String CONFIG_VALUE_SCORE_EVAL_FORM_AVG = "score.evaluation.form.avg";
	public static final String CONFIG_KEY_EVAL_FORM_ENABLED = "evaluation.form.enabled";
	public static final String CONFIG_KEY_EVAL_FORM_SOFTKEY = "evaluation.form.softkey";
	public static final String CONFIG_KEY_EVAL_FORM_SCALE = "evaluation.form.scale";
	public static final String CONFIG_DEFAULT_EVAL_FORM_SCALE = "1.0";
	
	public static final String CONFIG_KEY_INITIAL_STATUS = "initial.status";

	public MSCourseNode() {
		super(TYPE);
	}
	
	/**
	 * Adds to the given module configuration the default configuration for the
	 * manual scoring
	 * 
	 * @param moduleConfiguration
	 */
	public static void initDefaultConfig(ModuleConfiguration moduleConfiguration) {
		moduleConfiguration.set(CONFIG_KEY_HAS_SCORE_FIELD, Boolean.FALSE);
		moduleConfiguration.set(CONFIG_KEY_SCORE_MIN, CONFIG_DEFAULT_SCORE_MIN);
		moduleConfiguration.set(CONFIG_KEY_SCORE_MAX, CONFIG_DEFAULT_SCORE_MAX);
		moduleConfiguration.set(CONFIG_KEY_HAS_PASSED_FIELD, Boolean.TRUE);
		// no preset for passed cut value -> manual setting of passed
		moduleConfiguration.set(CONFIG_KEY_HAS_COMMENT_FIELD, Boolean.TRUE);
		moduleConfiguration.set(CONFIG_KEY_INFOTEXT_USER, "");
		moduleConfiguration.set(CONFIG_KEY_INFOTEXT_COACH, "");
	}
	
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType);
		
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			initDefaultConfig(config);
			config.setStringValue(CONFIG_KEY_SCORE, CONFIG_VALUE_SCORE_NONE);
			config.setStringValue(CONFIG_KEY_INITIAL_STATUS, AssessmentEntryStatus.inReview.name());
		}
		updateModuleDefaults(config);
		config.setConfigurationVersion(CURRENT_VERSION);
	}

	public void updateModuleDefaults(ModuleConfiguration config) {
		if (config.getConfigurationVersion() < 2) {
			// migrate legacy configs
			boolean hasScoreFiled = config.getBooleanSafe(CONFIG_KEY_HAS_SCORE_FIELD, false);
			if (hasScoreFiled) {
				config.setStringValue(CONFIG_KEY_SCORE, CONFIG_VALUE_SCORE_MANUAL);
			} else {
				config.setStringValue(CONFIG_KEY_SCORE, CONFIG_VALUE_SCORE_NONE);
			}
			// init configs from v1
			config.setBooleanEntry(CONFIG_KEY_HAS_INDIVIDUAL_ASSESSMENT_DOCS, false);
			// new configs
			config.setBooleanEntry(CONFIG_KEY_EVAL_FORM_ENABLED, false);
			config.setStringValue(CONFIG_KEY_EVAL_FORM_SCALE, CONFIG_DEFAULT_EVAL_FORM_SCALE);
		}
		if (config.getConfigurationVersion() < 3) {
			if (!config.has(CONFIG_KEY_INITIAL_STATUS)) {
				config.setStringValue(CONFIG_KEY_INITIAL_STATUS, AssessmentEntryStatus.notStarted.name());
			}
		}
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		MSCourseNodeEditController childTabCntrllr = new MSCourseNodeEditController(ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCntrllr);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd, VisibilityFilter visibilityFilter) {
		Controller controller;
		// Do not allow guests to have manual scoring
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			Translator trans = Util.createPackageTranslator(MSCourseNode.class, ureq.getLocale());
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else if (userCourseEnv.isParticipant()) {
			controller = new MSCourseNodeRunController(ureq, wControl, userCourseEnv, this, true, true);
		} else if (userCourseEnv.isCoach() || userCourseEnv.isAdmin()) {
			controller = new MSCoachRunController(ureq, wControl, userCourseEnv, this);
		} else {
			Translator trans = Util.createPackageTranslator(MSCourseNode.class, ureq.getLocale());
			String title = trans.translate("error.no.role.title");
			String message = trans.translate("error.no.role.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		}
		
		Controller wrappedCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, "o_ms_icon");
		return new NodeRunConstructionResult(wrappedCtrl);
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return getEvaluationForm(getModuleConfiguration());
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return getReferencedRepositoryEntry() != null? true: false;
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null && oneClickStatusCache.length > 0) {
			return oneClickStatusCache[0];
		}
		
		List<StatusDescription> statusDescs = validateInternalConfiguration(null);
		if(statusDescs.isEmpty()) {
			statusDescs.add(StatusDescription.NOERROR);
		}
		oneClickStatusCache = StatusDescriptionHelper.sort(statusDescs);
		return oneClickStatusCache[0];
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, PACKAGE_MS, getConditionExpressions());
		if(oneClickStatusCache != null && oneClickStatusCache.length > 0) {
			//isConfigValidWithTranslator add first
			sds.remove(oneClickStatusCache[0]);
		}
		sds.addAll(validateInternalConfiguration(cev));
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}
	
	private List<StatusDescription> validateInternalConfiguration(CourseEditorEnv cev) {
		List<StatusDescription> sdList = new ArrayList<>(1);
		
		if (isFullyAssessedScoreConfigError()) {
			addStatusErrorDescription("error.fully.assessed.score", "error.fully.assessed.score",
					TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
		}
		if (isFullyAssessedPassedConfigError()) {
			addStatusErrorDescription("error.fully.assessed.passed", "error.fully.assessed.passed",
					TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
		}
		
		if (cev != null) {
			if (getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_ENABLED) && CoreSpringFactory.getImpl(GradeModule.class).isEnabled()) {
				GradeService gradeService = CoreSpringFactory.getImpl(GradeService.class);
				GradeScale gradeScale = gradeService.getGradeScale(cev.getCourseGroupManager().getCourseEntry(), getIdent());
				if (gradeScale == null) {
					addStatusErrorDescription("error.missing.grade.scale", "error.fully.assessed.passed",
							MSCourseNodeEditController.PANE_TAB_CONFIGURATION, sdList);
				}
			}
		}
		
		return sdList;
	}
	
	private boolean isFullyAssessedScoreConfigError() {
		boolean hasScore = Mode.none != new MSAssessmentConfig(getModuleConfiguration()).getScoreMode();
		boolean isScoreTrigger = CoreSpringFactory.getImpl(MSLearningPathNodeHandler.class)
				.getConfigs(this)
				.isFullyAssessedOnScore(null, null)
				.isEnabled();
		return isScoreTrigger && !hasScore;
	}
	
	private boolean isFullyAssessedPassedConfigError() {
		boolean hasPassed = new MSAssessmentConfig(getModuleConfiguration()).getPassedMode() != Mode.none;
		boolean isPassedTrigger = CoreSpringFactory.getImpl(MSLearningPathNodeHandler.class)
				.getConfigs(this)
				.isFullyAssessedOnPassed(null, null)
				.isEnabled();
		return isPassedTrigger && !hasPassed;
	}
	
	private void addStatusErrorDescription(String shortDescKey, String longDescKey, String pane,
			List<StatusDescription> status) {
		String[] params = new String[] { getShortTitle() };
		StatusDescription sd = new StatusDescription(StatusDescription.ERROR, shortDescKey, longDescKey, params,
				PACKAGE_MS);
		sd.setDescriptionForUnit(getIdent());
		sd.setActivateableViewIdentifier(pane);
		status.add(sd);
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
		} else {
			removeEvaluationFormReference(getModuleConfiguration());
		}
	}

	public static RepositoryEntry getEvaluationForm(ModuleConfiguration config) {
		if (config == null) return null;
		
		String repoSoftkey = config.getStringValue(CONFIG_KEY_EVAL_FORM_SOFTKEY);
		if (!StringHelper.containsNonWhitespace(repoSoftkey)) return null;
		
		return RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repoSoftkey, false);
	}
	
	public static void setEvaluationFormReference(RepositoryEntry re, ModuleConfiguration moduleConfig) {
		moduleConfig.set(CONFIG_KEY_EVAL_FORM_SOFTKEY, re.getSoftkey());
	}
	
	public static void removeEvaluationFormReference(ModuleConfiguration moduleConfig) {
		moduleConfig.remove(CONFIG_KEY_EVAL_FORM_SOFTKEY);
	}

	@Override
	public String informOnDelete(Locale locale, ICourse course) {
		CoursePropertyManager cpm = PersistingCoursePropertyManager.getInstance(course);
		List<Property> list = cpm.listCourseNodeProperties(this, null, null, null);
		if (list.size() == 0) return null; // no properties created yet
		Translator trans = new PackageTranslator(PACKAGE_MS, locale);
		return trans.translate("warn.nodedelete");
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		
		CoursePropertyManager pm = course.getCourseEnvironment().getCoursePropertyManager();
		// Delete all properties: score, passed, log, comment, coach_comment
		pm.deleteNodeProperties(this, null);
		
		OLATResource resource = course.getCourseEnvironment().getCourseGroupManager().getCourseResource();
		CoreSpringFactory.getImpl(TaskExecutorManager.class).delete(resource, getIdent());
		
		// Delete the surveys
		MSService msService = CoreSpringFactory.getImpl(MSService.class);
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		msService.deleteSessions(entry, getIdent());
		
		// Delete GradeScales
		CoreSpringFactory.getImpl(GradeService.class).deleteGradeScale(entry, getIdent());
	}
	
	private MinMax getMinMax() {
		return getMinMax(getModuleConfiguration());
	}
	
	public static MinMax getMinMax(ModuleConfiguration config) {
		String scoreConfig = config.getStringValue(CONFIG_KEY_SCORE);
		String scaleConfig = config.getStringValue(CONFIG_KEY_EVAL_FORM_SCALE, CONFIG_DEFAULT_EVAL_FORM_SCALE);
		
		if (CONFIG_VALUE_SCORE_MANUAL.equals(scoreConfig)) {
			Float min = (Float) config.get(CONFIG_KEY_SCORE_MIN);
			Float max = (Float) config.get(CONFIG_KEY_SCORE_MAX);
			return MinMax.of(min, max);
		} else if (CONFIG_VALUE_SCORE_EVAL_FORM_SUM.equals(scoreConfig)) {
			MSService msService = CoreSpringFactory.getImpl(MSService.class);
			return  msService.calculateMinMaxSum(getEvaluationForm(config), Float.parseFloat(scaleConfig));
		} else if (CONFIG_VALUE_SCORE_EVAL_FORM_AVG.equals(scoreConfig)) {
			MSService msService = CoreSpringFactory.getImpl(MSService.class);
			return  msService.calculateMinMaxAvg(getEvaluationForm(config), Float.parseFloat(scaleConfig));
		}
		return MinMax.of(0.0f,  0.0f);
	}
	
	public void updateScoreEvaluation(Identity identity, UserCourseEnvironment assessedUserCourseEnv,
			Role by, EvaluationFormSession session, Locale locale) {
		MSService msService = CoreSpringFactory.getImpl(MSService.class);
		
		ScoreEvaluation currentEval = assessedUserCourseEnv.getScoreAccounting().evalCourseNode(this);
		Float score = getScore(msService, assessedUserCourseEnv, session);
		ScoreEvaluation updateEval = getUpdateScoreEvaluation(assessedUserCourseEnv, locale, score);
		
		// save
		ScoreEvaluation scoreEvaluation = new ScoreEvaluation(updateEval.getScore(), updateEval.getGrade(),
				updateEval.getPerformanceClassIdent(), updateEval.getPassed(), currentEval.getAssessmentStatus(),
				currentEval.getUserVisible(), currentEval.getCurrentRunStartDate(),
				currentEval.getCurrentRunCompletion(), currentEval.getCurrentRunStatus(),
				currentEval.getAssessmentID());
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		courseAssessmentService.saveScoreEvaluation(this, identity, scoreEvaluation, assessedUserCourseEnv, false, by);
	}

	@Override
	public void updateOnPublish(Locale locale, ICourse course, Identity publisher, PublishEvents publishEvents) {
		CoursePropertyManager pm = course.getCourseEnvironment().getCoursePropertyManager();
		List<Identity> assessedUsers = pm.getAllIdentitiesWithCourseAssessmentData(null);

		int count = 0;
		for(Identity assessedIdentity: assessedUsers) {
			updateScorePassedOnPublish(course, assessedIdentity, publisher, locale);
			if(++count % 10 == 0) {
				DBFactory.getInstance().commitAndCloseSession();
			}
		}
		DBFactory.getInstance().commitAndCloseSession();
		super.updateOnPublish(locale, course, publisher, publishEvents);
	}
	
	private void updateScorePassedOnPublish(ICourse course, Identity assessedIdentity, Identity coachIdentity, Locale locale) {
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		MSService msService = CoreSpringFactory.getImpl(MSService.class);
		RepositoryEntry ores = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		IdentityEnvironment identityEnv = new IdentityEnvironment(assessedIdentity, null);
		UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment());
		
		ScoreEvaluation currentEval = courseAssessmentService.getAssessmentEvaluation(this, userCourseEnv);
		EvaluationFormSession session = msService.getSession(ores, getIdent(), assessedIdentity, done);
		
		Float updatedScore = getScore(msService, userCourseEnv, session);
		ScoreEvaluation updateScoreEvaluation = getUpdateScoreEvaluation(userCourseEnv, locale, updatedScore);
		String updateGrade = updateScoreEvaluation.getGrade();
		String updatePerformanceClassIdent = updateScoreEvaluation.getPerformanceClassIdent();
		Boolean updatedPassed = updateScoreEvaluation.getPassed();
		
		boolean needUpdate = !Objects.equals(updatedScore, currentEval.getScore())
				|| !Objects.equals(updateGrade, currentEval.getGrade())
				|| !Objects.equals(updatePerformanceClassIdent, currentEval.getPerformanceClassIdent())
				|| !Objects.equals(updatedPassed, currentEval.getPassed()) ;
		if(needUpdate) {
			ScoreEvaluation scoreEval = new ScoreEvaluation(updatedScore, updateGrade, updatePerformanceClassIdent,
					updatedPassed, currentEval.getAssessmentStatus(), currentEval.getUserVisible(),
					currentEval.getCurrentRunStartDate(), currentEval.getCurrentRunCompletion(),
					currentEval.getCurrentRunStatus(), currentEval.getAssessmentID());
			courseAssessmentService.saveScoreEvaluation(this, coachIdentity, scoreEval, userCourseEnv, false, Role.coach);
		}
	}

	private Float getScore(MSService msService, UserCourseEnvironment assessedUserCourseEnv, EvaluationFormSession session) {
		Float score = null;
		ModuleConfiguration config = getModuleConfiguration();
		String scoreConfig = config.getStringValue(CONFIG_KEY_SCORE);
		String scaleConfig = config.getStringValue(CONFIG_KEY_EVAL_FORM_SCALE, CONFIG_DEFAULT_EVAL_FORM_SCALE);
		float scale = Float.parseFloat(scaleConfig);
		if (CONFIG_VALUE_SCORE_EVAL_FORM_AVG.equals(scoreConfig)) {
			score = msService.calculateScoreByAvg(session);
			score = msService.scaleScore(score, scale);
		} else if (CONFIG_VALUE_SCORE_EVAL_FORM_SUM.equals(scoreConfig)) {
			score = msService.calculateScoreBySum(session);
			score = msService.scaleScore(score, scale);
		}
		if (score == null) {
			CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
			ScoreEvaluation currentEval = courseAssessmentService.getAssessmentEvaluation(this, assessedUserCourseEnv);
			score = currentEval.getScore();
		}
		
		// Score has to be in configured range.
		MinMax minMax = getMinMax();
		if (score != null) {
			if(minMax.getMax().floatValue() < score.floatValue()) {
				score = minMax.getMax();
			}
			if(minMax.getMin().floatValue() > score.floatValue()) {
				score = minMax.getMin();
			}
		}
		return score;
	}

	private ScoreEvaluation getUpdateScoreEvaluation(UserCourseEnvironment assessedUserCourseEnv, Locale locale, Float score) {
		GradeScoreRange gradeScoreRange = null;
		String grade = null;
		String performanceClassIdent = null;
		Boolean passed = null;
		ModuleConfiguration config = getModuleConfiguration();
		
		if (config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD)) {
			if (config.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_ENABLED)) {
				if (CoreSpringFactory.getImpl(GradeModule.class).isEnabled() && score != null) {
					boolean applyGrade = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_AUTO);
					if (!applyGrade) {
						ScoreEvaluation currentEval = assessedUserCourseEnv.getScoreAccounting().evalCourseNode(this);
						applyGrade = StringHelper.containsNonWhitespace(currentEval.getGrade());
					}
					if (applyGrade) {
						GradeService gradeService = CoreSpringFactory.getImpl(GradeService.class);
						GradeScale gradeScale = gradeService.getGradeScale(assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), getIdent());
						NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, locale);
						gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, score);
						grade = gradeScoreRange.getGrade();
						performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
						passed = Boolean.valueOf(gradeScoreRange.isPassed());
					}
				}
			} else if (config.has(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE)) {
				Float cutConfig = (Float) config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
				if (cutConfig != null && score != null) {
					boolean aboveCutValue = score.floatValue() >= cutConfig.floatValue();
					passed = Boolean.valueOf(aboveCutValue);
				}
			} else {
				ScoreEvaluation currentEval = assessedUserCourseEnv.getScoreAccounting().evalCourseNode(this);
				grade = currentEval.getGrade();
				performanceClassIdent = currentEval.getPerformanceClassIdent();
				passed = currentEval.getPassed();
			}
			
		}
		return new ScoreEvaluation(score, grade, performanceClassIdent, passed, null, null, null, null, null, null);
	}

	@Override
	public CourseNodeReminderProvider getReminderProvider(boolean rootNode) {
		return new AssessmentReminderProvider(getIdent(), new MSAssessmentConfig(getModuleConfiguration()));
	}
	
}