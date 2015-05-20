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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.qti21.QTI21AssessmentDetailsController;
import org.olat.course.nodes.qti21.QTI21AssessmentRunController;
import org.olat.course.nodes.qti21.QTI21EditController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * 
 * Initial date: 19.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentCourseNode extends AbstractAccessableCourseNode implements AssessableCourseNode {

	private static final long serialVersionUID = -3619170190576867622L;
	private final static String PACKAGE_QTI21 = Util.getPackageName(QTI21EditController.class);
	
	public static final String CONFIG_KEY_REPOSITORY_SOFTKEY = "repoSoftkey";
	public static final String CONFIG_KEY_ENABLESCOREINFO = "enableScoreInfo";
	public static final String CONFIG_KEY_ATTEMPTS = "attempts";
	public static final String CONFIG_KEY_BLOCK_AFTER_SUCCESS = "blockAfterSuccess";
	public final static String CONFIG_FULLWINDOW = "fullwindow";
	
	
	public static final String TYPE = "qti21assessment";
	private static final int CURRENT_CONFIG_VERSION = 1;

	public QTI21AssessmentCourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
	}
	
	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return true;
	}
	
	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		String repoSoftkey = getModuleConfiguration().getStringValue(CONFIG_KEY_REPOSITORY_SOFTKEY);
		if (repoSoftkey == null) {
			return null;
		}
		return CoreSpringFactory.getImpl(RepositoryManager.class)
				.lookupRepositoryEntryBySoftkey(repoSoftkey, false);
	}
	
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode) {
		ModuleConfiguration config = getModuleConfiguration();
		if(isNewNode) {
			//setup default configuration:
			//layout
			config.set(QTI21AssessmentCourseNode.CONFIG_FULLWINDOW, Boolean.TRUE);
			
			//configure grading
			config.set(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, Boolean.FALSE);
			config.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, new Float(0));
			config.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, new Float(0));
			config.set(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, Boolean.TRUE);
			
			config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
		} else {
			config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
		}
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
		oneClickStatusCache = null;//delete the cache
		
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, PACKAGE_QTI21, getConditionExpressions());
		if(oneClickStatusCache != null && oneClickStatusCache.length > 0) {
			//isConfigValidWithTranslator add first
			sds.remove(oneClickStatusCache[0]);
		}
		sds.addAll(validateInternalConfiguration(cev));
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}
	
	private List<StatusDescription> validateInternalConfiguration(CourseEditorEnv cev) {
		List<StatusDescription> sdList = new ArrayList<>(5);

		ModuleConfiguration config = getModuleConfiguration();
		
		String repoEntrySoftKey = config.getStringValue(CONFIG_KEY_REPOSITORY_SOFTKEY);
		if(!StringHelper.containsNonWhitespace(repoEntrySoftKey)) {
			addStatusErrorDescription("error.missing.score.config", QTI21EditController.PANE_TAB_CONFIG_RE, sdList);
		}
		
		return sdList;
	}
	
	private void addStatusErrorDescription(String key, String pane, List<StatusDescription> status) {
		String[] params = new String[] { getShortTitle() };
		StatusDescription sd = new StatusDescription(StatusDescription.ERROR, key, key, params, PACKAGE_QTI21);
		sd.setDescriptionForUnit(getIdent());
		sd.setActivateableViewIdentifier(pane);
		status.add(sd);
	}
	
	@Override
	public void exportNode(File fExportDirectory, ICourse course) {
		super.exportNode(fExportDirectory, course);
	}
	
	@Override
	public void importNode(File importDirectory, ICourse course, Identity owner, Locale locale, boolean withReferences) {
		super.importNode(importDirectory, course, owner, locale, withReferences);
	}

	@Override
	public CourseNode createInstanceForCopy(boolean isNewTitle, ICourse course) {
		return super.createInstanceForCopy(isNewTitle, course);
	}
	
	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options, ZipOutputStream exportStream, String charset) {
		return super.archiveNodeData(locale, course, options, exportStream, charset);
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
	}
	
	@Override
	public boolean hasStatusConfigured() {
		return false;
	}

	@Override
	public Float getMaxScoreConfiguration() {
		if (!hasScoreConfigured()) {
			throw new OLATRuntimeException(QTI21AssessmentCourseNode.class, "getMaxScore not defined when hasScore set to false", null);
		}
		return getModuleConfiguration().getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MAX);
	}

	@Override
	public Float getMinScoreConfiguration() {
		return getModuleConfiguration().getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MIN);
	}

	@Override
	public Float getCutValueConfiguration() {
		return getModuleConfiguration().getFloatEntry(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
	}

	@Override
	public boolean hasScoreConfigured() {
		return true;
	}

	@Override
	public boolean hasPassedConfigured() {
		return true;
	}

	@Override
	public boolean hasCommentConfigured() {
		return true;
	}

	@Override
	public boolean hasAttemptsConfigured() {
		return true;
	}

	@Override
	public boolean hasDetails() {
		return true;
	}

	@Override
	public boolean isEditableConfigured() {
		return true;
	}
	
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment euce) {
		QTI21EditController editCtrl = new QTI21EditController(ureq, wControl, stackPanel, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, euce, editCtrl);
	}
	
	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		updateModuleConfigDefaults(false);
		Controller controller;
		// Do not allow guests to start tests
		Roles roles = ureq.getUserSession().getRoles();
		Translator trans = Util.createPackageTranslator(QTI21AssessmentCourseNode.class, ureq.getLocale());
		if (roles.isGuestOnly()) {
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			controller = new QTI21AssessmentRunController(ureq, wControl, userCourseEnv, this);
		}
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, "o_iqtest_icon");
		return new NodeRunConstructionResult(ctrl);
	}
	
	@Override
	public String getDetailsListViewHeaderKey() {
		return "table.header.details.qti21test";
	}

	@Override
	public String getDetailsListView(UserCourseEnvironment userCourseEnvironment) {
		return "";
	}
	
	@Override
	public Controller getDetailsEditController(UserRequest ureq, WindowControl wControl,
			BreadcrumbPanel stackPanel, UserCourseEnvironment userCourseEnvironment) {
		return new QTI21AssessmentDetailsController(ureq, wControl, userCourseEnvironment, this);
	}

	@Override
	public ScoreEvaluation getUserScoreEvaluation(UserCourseEnvironment userCourseEnv) {
		// read score from properties save score, passed and attempts information
		AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnv.getIdentityEnvironment().getIdentity();
		Boolean passed = am.getNodePassed(this, mySelf);
		Float score = am.getNodeScore(this, mySelf);		
		Long assessmentID = am.getAssessmentID(this, mySelf);	
		Boolean fullyAssessed = am.getNodeFullyAssessed(this, mySelf);	
		return new ScoreEvaluation(score, passed, fullyAssessed, assessmentID);
	}

	@Override
	public String getUserUserComment(UserCourseEnvironment userCourseEnv) {
		AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		return am.getNodeComment(this, userCourseEnv.getIdentityEnvironment().getIdentity());
	}

	@Override
	public String getUserCoachComment(UserCourseEnvironment userCourseEnv) {
		AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		return am.getNodeCoachComment(this, userCourseEnv.getIdentityEnvironment().getIdentity());
	}

	@Override
	public String getUserLog(UserCourseEnvironment userCourseEnv) {
		UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
		return am.getUserNodeLog(this, userCourseEnv.getIdentityEnvironment().getIdentity());
	}

	@Override
	public Integer getUserAttempts(UserCourseEnvironment userCourseEnv) {
		AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		Identity assessedIdentity = userCourseEnv.getIdentityEnvironment().getIdentity();
		return am.getNodeAttempts(this, assessedIdentity);
	}

	@Override
	public void updateUserScoreEvaluation(ScoreEvaluation scoreEvaluation, UserCourseEnvironment userCourseEnv,
			Identity coachingIdentity, boolean incrementAttempts) {
		AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		Identity assessedIdentity = userCourseEnv.getIdentityEnvironment().getIdentity();
		am.saveScoreEvaluation(this, coachingIdentity, assessedIdentity, new ScoreEvaluation(scoreEvaluation.getScore(), scoreEvaluation.getPassed()), userCourseEnv, incrementAttempts);
	}

	@Override
	public void updateUserUserComment(String userComment, UserCourseEnvironment userCourseEnv, Identity coachingIdentity) {
		AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		Identity assessedIdentity = userCourseEnv.getIdentityEnvironment().getIdentity();
		if (userComment != null) {
			am.saveNodeComment(this, coachingIdentity, assessedIdentity, userComment);
		}
	}

	@Override
	public void incrementUserAttempts(UserCourseEnvironment userCourseEnv) {
		AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		Identity assessedIdentity = userCourseEnv.getIdentityEnvironment().getIdentity();
		am.incrementNodeAttempts(this, assessedIdentity, userCourseEnv);
	}

	@Override
	public void updateUserAttempts(Integer userAttempts, UserCourseEnvironment userCourseEnv, Identity coachingIdentity) {
		if (userAttempts != null) {
			AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
			Identity assessedIdentity = userCourseEnv.getIdentityEnvironment().getIdentity();
			am.saveNodeAttempts(this, coachingIdentity, assessedIdentity, userAttempts);
		}
	}

	@Override
	public void updateUserCoachComment(String coachComment, UserCourseEnvironment userCourseEnv) {
		AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		Identity assessedIdentity = userCourseEnv.getIdentityEnvironment().getIdentity();
		if (coachComment != null) {
			am.saveNodeCoachComment(this, assessedIdentity, coachComment);
		}
	}
}