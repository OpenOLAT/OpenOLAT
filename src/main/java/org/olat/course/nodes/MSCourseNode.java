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

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeController;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.ms.MSCourseNodeEditController;
import org.olat.course.nodes.ms.MSCourseNodeRunController;
import org.olat.course.nodes.ms.MSEvaluationFormExecutionController;
import org.olat.course.nodes.ms.MSIdentityListCourseNodeController;
import org.olat.course.nodes.ms.MSService;
import org.olat.course.nodes.ms.MinMax;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.properties.PersistingCoursePropertyManager;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.handler.EvaluationFormResource;
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
public class MSCourseNode extends AbstractAccessableCourseNode implements PersistentAssessableCourseNode {
	
	private static final long serialVersionUID = -7741172700015384397L;
	
	@SuppressWarnings("deprecation")
	private static final String PACKAGE_MS = Util.getPackageName(MSCourseNodeRunController.class);

	public static final int CURRENT_VERSION = 2;
	private static final String TYPE = "ms";
	/** configuration: score can be set */
	public static final String CONFIG_KEY_HAS_SCORE_FIELD = "hasScoreField";
	/** configuration: score min value */
	public static final String CONFIG_KEY_SCORE_MIN = "scoreMin";
	public static final Float CONFIG_DEFAULT_SCORE_MIN = Float.valueOf(0);
	/** configuration: score max value */
	public static final String CONFIG_KEY_SCORE_MAX = "scoreMax";
	public static final Float CONFIG_DEFAULT_SCORE_MAX = Float.valueOf(0);
	/** configuration: passed can be set */
	public static final String CONFIG_KEY_HAS_PASSED_FIELD = "hasPassedField";
	/** configuration: passed set to when score higher than cut value */
	public static final String CONFIG_KEY_PASSED_CUT_VALUE = "passedCutValue";
	/** configuration: comment can be set */
	public static final String CONFIG_KEY_HAS_COMMENT_FIELD = "hasCommentField";
	/** configuration: individual assessment document can be set (use getBooleanSafe default false) */
	public static final String CONFIG_KEY_HAS_INDIVIDUAL_ASSESSMENT_DOCS = "hasIndividualAsssessmentDocs";
	/** configuration: infotext for user */
	public static final String CONFIG_KEY_INFOTEXT_USER = "infoTextUser";
	/** configuration: infotext for coach */
	public static final String CONFIG_KEY_INFOTEXT_COACH = "nfoTextCoach";
	/** configuration: infotext for coach */
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

	/**
	 * Constructor for a course building block of type manual score
	 */
	public MSCourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
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
	public void updateModuleConfigDefaults(boolean isNewNode) {
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			initDefaultConfig(config);
			config.setStringValue(CONFIG_KEY_SCORE, CONFIG_VALUE_SCORE_NONE);
		}
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
		config.setConfigurationVersion(CURRENT_VERSION);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		updateModuleConfigDefaults(false);
		MSCourseNodeEditController childTabCntrllr = new MSCourseNodeEditController(ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, euce, childTabCntrllr);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		Controller controller;
		// Do not allow guests to have manual scoring
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			Translator trans = Util.createPackageTranslator(MSCourseNode.class, ureq.getLocale());
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			controller = new MSCourseNodeRunController(ureq, wControl, userCourseEnv, this, true, true);
		}
		
		Controller wrappedCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, "o_ms_icon");
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

	@SuppressWarnings("deprecation")
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		String translatorStr = Util.getPackageName(ConditionEditController.class);
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
	}
	
	@Override
	public StatusDescription isConfigValid() {
		return  StatusDescription.NOERROR;
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
	public AssessmentEvaluation getUserScoreEvaluation(UserCourseEnvironment userCourseEnv) {
		if(hasPassedConfigured() || hasScoreConfigured() || hasCommentConfigured()) {
			return getUserScoreEvaluation(getUserAssessmentEntry(userCourseEnv));
		}
		return AssessmentEvaluation.EMPTY_EVAL;
	}

	@Override
	public AssessmentEvaluation getUserScoreEvaluation(AssessmentEntry entry) {
		return AssessmentEvaluation.toAssessmentEvalutation(entry, this);
	}

	@Override
	public AssessmentEntry getUserAssessmentEntry(UserCourseEnvironment userCourseEnv) {
		AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnv.getIdentityEnvironment().getIdentity();
		return am.getAssessmentEntry(this, mySelf);
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
		RepositoryEntry ores = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
		msService.deleteSessions(ores, getIdent());
	}

	@Override
	public boolean hasCommentConfigured() {
		ModuleConfiguration config = getModuleConfiguration();
		Boolean comment = (Boolean) config.get(CONFIG_KEY_HAS_COMMENT_FIELD);
		if (comment == null) return false;
		return comment.booleanValue();
	}

	@Override
	public boolean hasIndividualAsssessmentDocuments() {
		return getModuleConfiguration().getBooleanSafe(CONFIG_KEY_HAS_INDIVIDUAL_ASSESSMENT_DOCS, false);
	}

	@Override
	public boolean hasPassedConfigured() {
		ModuleConfiguration config = getModuleConfiguration();
		Boolean passed = (Boolean) config.get(CONFIG_KEY_HAS_PASSED_FIELD);
		if (passed == null) return false;
		return passed.booleanValue();
	}

	@Override
	public boolean hasScoreConfigured() {
		updateModuleConfigDefaults(false);
		ModuleConfiguration config = getModuleConfiguration();
		String scoreKey = config.getStringValue(CONFIG_KEY_SCORE);
		return !CONFIG_VALUE_SCORE_NONE.equals(scoreKey);
	}
	
	@Override
	public boolean hasStatusConfigured() {
		return false;
	}
	
	@Override
	public boolean isAssessedBusinessGroups() {
		return false;
	}

	@Override
	public Float getMaxScoreConfiguration() {
		if (!hasScoreConfigured()) { throw new OLATRuntimeException(MSCourseNode.class, "getMaxScore not defined", null); }
		return getMinMax().getMax();
	}

	@Override
	public Float getMinScoreConfiguration() {
		if (!hasScoreConfigured()) { throw new OLATRuntimeException(MSCourseNode.class, "getMinScore not defined", null); }
		return getMinMax().getMin();
	}
	
	private MinMax getMinMax() {
		ModuleConfiguration config = getModuleConfiguration();
		String scoreConfig = config.getStringValue(CONFIG_KEY_SCORE);
		String scaleConfig = config.getStringValue(CONFIG_KEY_EVAL_FORM_SCALE);
		
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

	@Override
	public Float getCutValueConfiguration() {
		if (!hasPassedConfigured()) { throw new OLATRuntimeException(MSCourseNode.class, "getCutValue not defined when hasPassed set to false", null); }
		ModuleConfiguration config = getModuleConfiguration();
		Float cut = (Float) config.get(CONFIG_KEY_PASSED_CUT_VALUE);
		return cut;
	}

	@Override
	public String getUserCoachComment(UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		String coachCommentValue = am.getNodeCoachComment(this, userCourseEnvironment.getIdentityEnvironment().getIdentity());
		return coachCommentValue;
	}

	@Override
	public String getUserUserComment(UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		return am.getNodeComment(this, userCourseEnvironment.getIdentityEnvironment().getIdentity());
	}
	
	@Override
	public List<File> getIndividualAssessmentDocuments(UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		return am.getIndividualAssessmentDocuments(this, userCourseEnvironment.getIdentityEnvironment().getIdentity());
	}

	@Override
	public String getUserLog(UserCourseEnvironment userCourseEnvironment) {
		UserNodeAuditManager am = userCourseEnvironment.getCourseEnvironment().getAuditManager();
		String logValue = am.getUserNodeLog(this, userCourseEnvironment.getIdentityEnvironment().getIdentity());
		return logValue;
	}

	@Override
	public boolean isEditableConfigured() {
		// manual scoring fields can be edited manually
		return true;
	}

	@Override
	public void updateUserCoachComment(String coachComment, UserCourseEnvironment userCourseEnvironment) {
		if (coachComment != null) {
			AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
			Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
			am.saveNodeCoachComment(this, mySelf, coachComment);
		}
	}

	@Override
	public void updateUserScoreEvaluation(ScoreEvaluation scoreEvaluation, UserCourseEnvironment userCourseEnvironment,
			Identity coachingIdentity, boolean incrementAttempts, Role by) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		am.saveScoreEvaluation(this, coachingIdentity, mySelf, new ScoreEvaluation(scoreEvaluation), userCourseEnvironment, incrementAttempts, by);		
	}

	@Override
	public void updateUserUserComment(String userComment, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity) {
		if (userComment != null) {
			AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
			Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
			am.saveNodeComment(this, coachingIdentity, mySelf, userComment);
		}
	}
	
	@Override
	public void addIndividualAssessmentDocument(File document, String filename, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity) {
		if(document != null) {
			AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
			Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
			am.addIndividualAssessmentDocument(this, coachingIdentity, assessedIdentity, document, filename);
		}
	}

	@Override
	public void removeIndividualAssessmentDocument(File document, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity) {
		if(document != null) {
			AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
			Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
			am.removeIndividualAssessmentDocument(this, coachingIdentity, assessedIdentity, document);
		}
	}

	@Override
	public Integer getUserAttempts(UserCourseEnvironment userCourseEnvironment) {
		throw new OLATRuntimeException(MSCourseNode.class, "No attempts available in MS nodes", null);

	}

	@Override
	public boolean hasAttemptsConfigured() {
		return false;
	}

	@Override
	public void updateUserAttempts(Integer userAttempts, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity, Role by) {
		throw new OLATRuntimeException(MSCourseNode.class, "Attempts variable can't be updated in MS nodes", null);
	}

	@Override
	public void incrementUserAttempts(UserCourseEnvironment userCourseEnvironment, Role by) {
		throw new OLATRuntimeException(MSCourseNode.class, "Attempts variable can't be updated in MS nodes", null);
	}
	
	@Override
	public boolean hasCompletion() {
		return false;
	}

	@Override
	public Double getUserCurrentRunCompletion(UserCourseEnvironment userCourseEnvironment) {
		throw new OLATRuntimeException(MSCourseNode.class, "No completion available in MS nodes", null);
	}
	
	@Override
	public void updateCurrentCompletion(UserCourseEnvironment userCourseEnvironment, Identity identity,
			Double currentCompletion, AssessmentRunStatus status, Role doneBy) {
		throw new OLATRuntimeException(MSCourseNode.class, "Completion variable can't be updated in MS nodes", null);
	}

	@Override
	public void updateLastModifications(UserCourseEnvironment userCourseEnvironment, Identity identity, Role by) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		am.updateLastModifications(this, assessedIdentity, userCourseEnvironment, by);
	}

	@Override
	public Controller getDetailsEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnv) {
		return new MSEvaluationFormExecutionController(ureq, wControl, assessedUserCourseEnv, this);
	}
	
	@Override
	public AssessmentCourseNodeController getIdentityListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, BusinessGroup group, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback) {
		return new MSIdentityListCourseNodeController(ureq, wControl, stackPanel,
				courseEntry, group, this, coachCourseEnv, toolContainer, assessmentCallback);
	}

	@Override
	public String getDetailsListView(UserCourseEnvironment userCourseEnvironment) {
		return null;
	}

	@Override
	public String getDetailsListViewHeaderKey() {
		throw new OLATRuntimeException(MSCourseNode.class, "Details not available in MS nodes", null);
	}

	@Override
	public boolean hasDetails() {
		return getModuleConfiguration().getBooleanSafe(CONFIG_KEY_EVAL_FORM_ENABLED);
	}
	
	public void updateScoreEvaluation(Identity identity, UserCourseEnvironment assessedUserCourseEnv,
			Identity assessedIdentity, Role by, EvaluationFormSession session) {
		AssessmentManager am = assessedUserCourseEnv.getCourseEnvironment().getAssessmentManager();
		MSService msService = CoreSpringFactory.getImpl(MSService.class);
		ModuleConfiguration config = getModuleConfiguration();
		
		// Get score
		String scoreConfig = config.getStringValue(CONFIG_KEY_SCORE);
		String scaleConfig = config.getStringValue(CONFIG_KEY_EVAL_FORM_SCALE);
		float scale = Float.parseFloat(scaleConfig);
		Float score = null;
		if (CONFIG_VALUE_SCORE_EVAL_FORM_AVG.equals(scoreConfig)) {
			score = msService.calculateScoreByAvg(session);
			score = msService.scaleScore(score, scale);
		} else if (CONFIG_VALUE_SCORE_EVAL_FORM_SUM.equals(scoreConfig)) {
			score = msService.calculateScoreBySum(session);
			score = msService.scaleScore(score, scale);
		} else if (CONFIG_VALUE_SCORE_MANUAL.equals(scoreConfig)) {
			ScoreEvaluation currentEval = getUserScoreEvaluation(am.getAssessmentEntry(this, assessedIdentity));
			score = currentEval.getScore();
		}
		
		// Score has to be in configured range.
		MinMax minMax = getMinMax();
		if(score != null && minMax.getMax().floatValue() < score.floatValue()) {
			score = minMax.getMax();
		}
		if(score != null && minMax.getMin().floatValue() > score.floatValue()) {
			score = minMax.getMin();
		}
		
		// Get passed
		Float cutConfig = (Float) config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		Boolean passed = null;
		if (cutConfig != null && score != null) {
			boolean aboveCutValue = score.floatValue() >= cutConfig.floatValue();
			passed = Boolean.valueOf(aboveCutValue);
		} else {
			ScoreEvaluation currentEval = getUserScoreEvaluation(am.getAssessmentEntry(this, assessedIdentity));
			passed = currentEval.getPassed();
		}
		
		// save
		ScoreEvaluation scoreEvaluation = new ScoreEvaluation(score, passed);
		am.saveScoreEvaluation(this, identity, assessedIdentity, scoreEvaluation, assessedUserCourseEnv, false, by);
	}
	
}