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
* <p>
*/ 

package org.olat.course.nodes;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ExportUtil;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.condition.Condition;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.ms.MSEditFormController;
import org.olat.course.nodes.projectbroker.ProjectBrokerControllerFactory;
import org.olat.course.nodes.projectbroker.ProjectBrokerCourseEditorController;
import org.olat.course.nodes.projectbroker.ProjectListController;
import org.olat.course.nodes.projectbroker.datamodel.ProjectBroker;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerExportGenerator;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerManagerFactory;
import org.olat.course.nodes.ta.DropboxController;
import org.olat.course.nodes.ta.ReturnboxController;
import org.olat.course.nodes.ta.TaskController;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.properties.PersistingCoursePropertyManager;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;

/**
 *  
 *   @author Christian Guretzki
 */

public class ProjectBrokerCourseNode extends GenericCourseNode implements AssessableCourseNode {
	
	private transient static final String PACKAGE_PROJECTBROKER = Util.getPackageName(ProjectListController.class);

	private transient static final String PACKAGE = Util.getPackageName(ProjectBrokerCourseNode.class);

	private transient static final String TYPE = "projectbroker";
	
	// NLS support:
	
	private transient static final String NLS_GUESTNOACCESS_TITLE = "guestnoaccess.title";
	private transient static final String NLS_GUESTNOACCESS_MESSAGE = "guestnoaccess.message";
	private transient static final String NLS_ERROR_MISSINGSCORECONFIG_SHORT = "error.missingscoreconfig.short";
	private transient static final String NLS_WARN_NODEDELETE = "warn.nodedelete";

	// MUST BE NON TRANSIENT
	private static final int CURRENT_CONFIG_VERSION = 2;

	/** CONF_DROPBOX_ENABLED configuration parameter key. */
	public transient static final String CONF_DROPBOX_ENABLED = "dropbox_enabled";
	/** CONF_DROPBOX_ENABLEMAIL configuration parameter key. */
	public transient static final String CONF_DROPBOX_ENABLEMAIL = "dropbox_enablemail";
	/** CONF_DROPBOX_CONFIRMATION configuration parameter key. */
	public transient static final String CONF_DROPBOX_CONFIRMATION = "dropbox_confirmation";

	/** CONF_SCORING_ENABLED configuration parameter key. */
	public transient static final String CONF_SCORING_ENABLED = "scoring_enabled";

	/** ACCESS_SCORING configuration parameter key. */
	public transient static final String ACCESS_SCORING = "scoring";
	/** ACCESS_DROPBOX configuration parameter key. */
	public transient static final String ACCESS_DROPBOX = "dropbox";
	public transient static final String ACCESS_RETURNBOX = "returnbox";
	public transient static final String ACCESS_PROJECTBROKER = "projectbroker";

	/** CONF_TASK_PREVIEW configuration parameter key used for task-form. */
	public transient static final String CONF_TASK_PREVIEW = "task_preview";

	public transient static final String CONF_RETURNBOX_ENABLED = "returnbox_enabled";

	public transient static final String CONF_ACCOUNTMANAGER_GROUP_KEY = "config_accountmanager_group_id";

	public transient static final String CONF_PROJECTBROKER_KEY = "conf_projectbroker_id";

	public transient static final String CONF_NODE_SHORT_TITLE_KEY = "conf_node_short_title";
	
	// MUST BE NON TRANSIENT
	private Condition  conditionDrop, conditionScoring, conditionReturnbox;
	private Condition  conditionProjectBroker;

	private transient CourseGroupManager groupMgr;

	/**
	 * Default constructor.
	 */
	public ProjectBrokerCourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createEditController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl, org.olat.course.ICourse)
	 */
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, ICourse course, UserCourseEnvironment euce) {
		updateModuleConfigDefaults(false);
		ProjectBrokerCourseEditorController childTabCntrllr = ProjectBrokerControllerFactory.createCourseEditController(ureq, wControl, course, euce, this );
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		groupMgr = course.getCourseEnvironment().getCourseGroupManager();
		NodeEditController editController = new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, groupMgr, euce, childTabCntrllr);
		editController.addControllerListener(childTabCntrllr);
		return editController;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createNodeRunConstructionResult(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.course.run.userview.NodeEvaluation)
	 */
	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		updateModuleConfigDefaults(false);
		Controller controller;
		// Do not allow guests to access tasks
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			Translator trans = new PackageTranslator(PACKAGE, ureq.getLocale());
			String title = trans.translate(NLS_GUESTNOACCESS_TITLE);
			String message = trans.translate(NLS_GUESTNOACCESS_MESSAGE);
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			// Add message id to business path if nodemcd is available
			if (nodecmd != null) {
				try {
					Long projectId = Long.valueOf(nodecmd);
					BusinessControlFactory bcf =  BusinessControlFactory.getInstance();
					BusinessControl businessControl = bcf.createFromString("[Project:"+projectId+"]");
					wControl = bcf.createBusinessWindowControl(businessControl, wControl);
				} catch (NumberFormatException e) {
					// ups, nodecmd is not a message, what the heck is it then?
					Tracing.createLoggerFor(this.getClass()).warn("Could not create message ID from given nodemcd::" + nodecmd, e);
				}
			}
			controller = ProjectBrokerControllerFactory.createRunController(ureq, wControl,userCourseEnv, ne);
		}
		Controller wrapperCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, "o_projectbroker_icon");
		return new NodeRunConstructionResult(wrapperCtrl);
	}

	/**
	 * @see org.olat.course.nodes.GenericCourseNode#createPreviewController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.course.run.userview.NodeEvaluation)
	 */
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		return ProjectBrokerControllerFactory.createPreviewController(ureq, wControl,userCourseEnv, ne);
	}

	/**
	 * @see org.olat.course.nodes.GenericCourseNode#createPeekViewRunController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.course.run.userview.NodeEvaluation)
	 */
	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			NodeEvaluation ne) {
		if (ne.isAtLeastOneAccessible()) {
			Controller peekViewController = ProjectBrokerControllerFactory.createPeekViewRunController(ureq, wControl,userCourseEnv, ne);
			return peekViewController;			
		} else {
			// use standard peekview
			return super.createPeekViewRunController(ureq, wControl, userCourseEnv, ne);
		}
	}	
	
	/**
	 * @see org.olat.course.nodes.CourseNode#getReferencedRepositoryEntry()
	 */
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#needsReferenceToARepositoryEntry()
	 */
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid()
	 */
	public StatusDescription isConfigValid() {
		/*
		 * first check the one click cache
		 */
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

		boolean isValid = true;
		Boolean hasScoring = (Boolean) getModuleConfiguration().get(CONF_SCORING_ENABLED);
		if (hasScoring.booleanValue()) {
			if (!MSEditFormController.isConfigValid(getModuleConfiguration())) isValid = false;
		}
		StatusDescription sd = StatusDescription.NOERROR;
		if (!isValid) {
			// FIXME: refine statusdescriptions by moving the statusdescription
			String shortKey = NLS_ERROR_MISSINGSCORECONFIG_SHORT;
			String longKey = NLS_ERROR_MISSINGSCORECONFIG_SHORT;
			String[] params = new String[] { this.getShortTitle() };
			String translPackage = Util.getPackageName(MSEditFormController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
// TODO:cg 28.01.2010 no assessment-tool in V1.0			
//			sd.setActivateableViewIdentifier(ProjectBrokerCourseEditorController.PANE_TAB_CONF_SCORING);
		}
		return sd;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		// only here we know which translator to take for translating condition
		// error messages
		String translatorStr = Util.getPackageName(ProjectBrokerCourseEditorController.class);
		// check if group-manager is already initialized
		if (groupMgr == null) {
			groupMgr = cev.getCourseGroupManager();
		}
		List sds = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}

	@Override
	protected void calcAccessAndVisibility(ConditionInterpreter ci, NodeEvaluation nodeEval) {
		if (ci == null) throw new OLATRuntimeException("no condition interpreter <" + getIdent() + " " + getShortName() + ">",
				new IllegalArgumentException());
		if (nodeEval == null) throw new OLATRuntimeException("node Evaluationt is null!! for <" + getIdent() + " " + getShortName() + ">",
				new IllegalArgumentException());
		// evaluate the preconditions
		boolean projectBrokerAccess = (getConditionProjectBroker().getConditionExpression() == null ? true : ci.evaluateCondition(conditionProjectBroker));
		nodeEval.putAccessStatus(ACCESS_PROJECTBROKER, projectBrokerAccess);
		// add a dummy access-status to open course node in general otherwise the hole project-broker could be closed
// TODO:ch 28.01.2010 : ProjectBroker does not support assessment-tool in V1.0
//		boolean scoring = (getConditionScoring().getConditionExpression() == null ? true : ci.evaluateCondition(conditionScoring));
//		nodeEval.putAccessStatus(ACCESS_SCORING, scoring);

		boolean visible = (getPreConditionVisibility().getConditionExpression() == null ? true : ci
				.evaluateCondition(getPreConditionVisibility()));
		nodeEval.setVisible(visible);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#informOnDelete(org.olat.core.gui.UserRequest,
	 *      org.olat.course.ICourse)
	 */
	@Override
	public String informOnDelete(Locale locale, ICourse course) {
		Translator trans = new PackageTranslator(PACKAGE_PROJECTBROKER, locale);
		CoursePropertyManager cpm = PersistingCoursePropertyManager.getInstance(course);
		List list = cpm.listCourseNodeProperties(this, null, null, null);
		if (list.size() != 0) return trans.translate(NLS_WARN_NODEDELETE); // properties exist
		File fDropboxFolder = new File(FolderConfig.getCanonicalRoot() + DropboxController.getDropboxPathRelToFolderRoot(course.getCourseEnvironment(), this));
		if (fDropboxFolder.exists() && fDropboxFolder.list().length > 0) return trans.translate(NLS_WARN_NODEDELETE); // Dropbox folder contains files
		File fReturnboxFolder = new File(FolderConfig.getCanonicalRoot() + ReturnboxController.getReturnboxPathRelToFolderRoot(course.getCourseEnvironment(), this));
		if (fReturnboxFolder.exists() && fReturnboxFolder.list().length > 0) return trans.translate(NLS_WARN_NODEDELETE); // Returnbox folder contains files

		return null; // no data yet.
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#cleanupOnDelete(
	 *      org.olat.course.ICourse)
	 */
	@Override
	public void cleanupOnDelete(ICourse course) {
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		Long projectBrokerId = ProjectBrokerManagerFactory.getProjectBrokerManager().getProjectBrokerId(cpm, this);
		File fDropBox = new File(FolderConfig.getCanonicalRoot() + DropboxController.getDropboxPathRelToFolderRoot(course.getCourseEnvironment(), this));
		if (fDropBox.exists()) FileUtils.deleteDirsAndFiles(fDropBox, true, true);
		File fReturnBox = new File(FolderConfig.getCanonicalRoot() + ReturnboxController.getReturnboxPathRelToFolderRoot(course.getCourseEnvironment(), this));
		if (fReturnBox.exists()) FileUtils.deleteDirsAndFiles(fReturnBox, true, true);
		File attachmentDir = new File(FolderConfig.getCanonicalRoot() + ProjectBrokerManagerFactory.getProjectBrokerManager().getAttachmentBasePathRelToFolderRoot(course.getCourseEnvironment(), this));
		if (attachmentDir.exists()) FileUtils.deleteDirsAndFiles(attachmentDir, true, true);
		// Delete project-broker, projects and project-groups
		if (projectBrokerId != null) {
			ProjectBrokerManagerFactory.getProjectBrokerManager().deleteProjectBroker(projectBrokerId, course.getCourseEnvironment(), this);
		}
		// Delete all properties...
		cpm.deleteNodeProperties(this, null);
	}

	/**
	 * @return dropbox condition
	 */
	public Condition getConditionDrop() {
		if (conditionDrop == null) {
			conditionDrop = new Condition();
		}
		conditionDrop.setConditionId("drop");
		return conditionDrop;
	}

	/**
	 * @return scoring condition
	 */
	public Condition getConditionScoring() {
		if (conditionScoring == null) {
			conditionScoring = new Condition();
		}
		conditionScoring.setConditionId("scoring");
		return conditionScoring;
	}

	/**
	 * 
	 * @return Returnbox condition
	 */
	public Condition getConditionReturnbox() {
		if (conditionReturnbox == null) {
			conditionReturnbox = new Condition();
		}
		conditionReturnbox.setConditionId("returnbox");
		return conditionReturnbox;
	}

	/**
	 * @param conditionDrop
	 */
	public void setConditionDrop(Condition conditionDrop) {
		if (conditionDrop == null) {
			conditionDrop = getConditionDrop();
		}
		conditionDrop.setConditionId("drop");
		this.conditionDrop = conditionDrop;
	}

	/**
	 * @param conditionScoring
	 */
	public void setConditionScoring(Condition conditionScoring) {
		if (conditionScoring == null) {
			conditionScoring = getConditionScoring();
		}
		conditionScoring.setConditionId("scoring");
		this.conditionScoring = conditionScoring;
	}

	/**
	 * 
	 * @param condition
	 */
	public void setConditionReturnbox(Condition condition) {
		if (condition == null) {
			condition = getConditionReturnbox();
		}
		condition.setConditionId("returnbox");
		this.conditionReturnbox = condition;
	}

	public Condition getConditionProjectBroker() {
		if (conditionProjectBroker == null) {
			conditionProjectBroker = new Condition();
		}
		conditionProjectBroker.setConditionId("projectbroker");
		return conditionProjectBroker;
	}

	public void setConditionProjectBroker(Condition condition) {
		if (condition == null) {
			condition = getConditionProjectBroker();
		}
		condition.setConditionId("projectbroker");
		this.conditionProjectBroker = condition;
	}
	
	// //////////// assessable interface implementation

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getUserScoreEvaluation(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	public ScoreEvaluation getUserScoreEvaluation(UserCourseEnvironment userCourseEnvironment) {
		// read score from properties
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		Boolean passed = null;
		Float score = null;
		// only db lookup if configured, else return null
		if (hasPassedConfigured()) passed = am.getNodePassed(this, mySelf);
		if (hasScoreConfigured()) score = am.getNodeScore(this, mySelf);

		ScoreEvaluation se = new ScoreEvaluation(score, passed);
		return se;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#hasCommentConfigured()
	 */
	public boolean hasCommentConfigured() {
		return false;// TODO:ch 28.01.2010 : ProjectBroker does not support assessment-tool in V1.0
//		ModuleConfiguration config = getModuleConfiguration();
//		Boolean comment = (Boolean) config.get(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD);
//		if (comment == null) return false;
//		return comment.booleanValue();
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#hasPassedConfigured()
	 */
	public boolean hasPassedConfigured() {
		return false;// TODO:ch 28.01.2010 : ProjectBroker does not support assessment-tool in V1.0
//		ModuleConfiguration config = getModuleConfiguration();
//		Boolean passed = (Boolean) config.get(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD);
//		if (passed == null) return false;
//		return passed.booleanValue();
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#hasScoreConfigured()
	 */
	public boolean hasScoreConfigured() {
		return false;// TODO:ch 28.01.2010 : ProjectBroker does not support assessment-tool in V1.0
//		ModuleConfiguration config = getModuleConfiguration();
//		Boolean score = (Boolean) config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
//		if (score == null) return false;
//		return score.booleanValue();
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#hasStatusConfigured()
	 */
	public boolean hasStatusConfigured() {
		return false; // Project broker Course node has no status-field
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getMaxScoreConfiguration()
	 */
	public Float getMaxScoreConfiguration() {
		if (!hasScoreConfigured()) { throw new OLATRuntimeException(ProjectBrokerCourseNode.class, "getMaxScore not defined when hasScore set to false", null); }
		ModuleConfiguration config = getModuleConfiguration();
		Float max = (Float) config.get(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		return max;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getMinScoreConfiguration()
	 */
	public Float getMinScoreConfiguration() {
		if (!hasScoreConfigured()) { throw new OLATRuntimeException(ProjectBrokerCourseNode.class, "getMinScore not defined when hasScore set to false", null); }
		ModuleConfiguration config = getModuleConfiguration();
		Float min = (Float) config.get(MSCourseNode.CONFIG_KEY_SCORE_MIN);
		return min;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getCutValueConfiguration()
	 */
	public Float getCutValueConfiguration() {
		if (!hasPassedConfigured()) { throw new OLATRuntimeException(ProjectBrokerCourseNode.class, "getCutValue not defined when hasPassed set to false", null); }
		ModuleConfiguration config = getModuleConfiguration();
		Float cut = (Float) config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		return cut;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getUserCoachComment(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	public String getUserCoachComment(UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		String coachCommentValue = am.getNodeCoachComment(this, userCourseEnvironment.getIdentityEnvironment().getIdentity());
		return coachCommentValue;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getUserUserComment(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	public String getUserUserComment(UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		String userCommentValue = am.getNodeComment(this, userCourseEnvironment.getIdentityEnvironment().getIdentity());
		return userCommentValue;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getUserLog(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	public String getUserLog(UserCourseEnvironment userCourseEnvironment) {
		UserNodeAuditManager am = userCourseEnvironment.getCourseEnvironment().getAuditManager();
		String logValue = am.getUserNodeLog(this, userCourseEnvironment.getIdentityEnvironment().getIdentity());
		return logValue;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#isEditableConfigured()
	 */
	public boolean isEditableConfigured() {
		// always true when assessable
		return false;// TODO:ch 28.01.2010 : ProjectBroker does not support assessment-tool in V1.0
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#updateUserCoachComment(java.lang.String,
	 *      org.olat.course.run.userview.UserCourseEnvironment)
	 */
	public void updateUserCoachComment(String coachComment, UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		if (coachComment != null) {
			am.saveNodeCoachComment(this, mySelf, coachComment);
		}
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#updateUserScoreEvaluation(org.olat.course.run.scoring.ScoreEvaluation,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.core.id.Identity)
	 */
	public void updateUserScoreEvaluation(ScoreEvaluation scoreEvaluation, UserCourseEnvironment userCourseEnvironment,
			Identity coachingIdentity, boolean incrementAttempts) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		am.saveScoreEvaluation(this, coachingIdentity, mySelf, new ScoreEvaluation(scoreEvaluation.getScore(), scoreEvaluation.getPassed()), userCourseEnvironment, incrementAttempts);		
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#updateUserUserComment(java.lang.String,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.core.id.Identity)
	 */
	public void updateUserUserComment(String userComment, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		if (userComment != null) {
			am.saveNodeComment(this, coachingIdentity, mySelf, userComment);
		}
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getUserAttempts(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	public Integer getUserAttempts(UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		Integer userAttemptsValue = am.getNodeAttempts(this, mySelf);
		return userAttemptsValue;

	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#hasAttemptsConfigured()
	 */
	public boolean hasAttemptsConfigured() {
		return false;// TODO:ch 28.01.2010 : ProjectBroker does not support assessment-tool in V1.0
//		return true;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#updateUserAttempts(java.lang.Integer,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.core.id.Identity)
	 */
	public void updateUserAttempts(Integer userAttempts, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity) {
		if (userAttempts != null) {
			AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
			Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
			am.saveNodeAttempts(this, coachingIdentity, mySelf, userAttempts);
		}
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#incrementUserAttempts(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	public void incrementUserAttempts(UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		am.incrementNodeAttempts(this, mySelf, userCourseEnvironment);
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getDetailsEditController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment)
	 */
	public Controller getDetailsEditController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnvironment) {
		// prepare file component
		throw new AssertException("ProjectBroker does not support AssessmentTool");
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getDetailsListView(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	public String getDetailsListView(UserCourseEnvironment userCourseEnvironment) {
		Identity identity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		CoursePropertyManager propMgr = userCourseEnvironment.getCourseEnvironment().getCoursePropertyManager();
		List samples = propMgr.findCourseNodeProperties(this, identity, null, TaskController.PROP_ASSIGNED);
		if (samples.size() == 0) return null; // no sample assigned yet
		return ((Property) samples.get(0)).getStringValue();
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getDetailsListViewHeaderKey()
	 */
	public String getDetailsListViewHeaderKey() {
		return "table.header.details.ta";
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#hasDetails()
	 */
	public boolean hasDetails() {
		Boolean hasDropbox = (Boolean) getModuleConfiguration().get(CONF_DROPBOX_ENABLED);
		if (hasDropbox == null) hasDropbox = Boolean.FALSE;
		return  hasDropbox.booleanValue();
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#exportNode(java.io.File,
	 *      org.olat.course.ICourse)
	 */
	@Override
	public void exportNode(File fExportDirectory, ICourse course) {
		// nothing to export 
		//		File fNodeExportDir = new File(fExportDirectory, this.getIdent());
		//		fNodeExportDir.mkdirs();
	}

	/**
	 * @see org.olat.course.nodes.GenericCourseNode#importNode(java.io.File,
	 *      org.olat.course.ICourse, org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public Controller importNode(File importDirectory, ICourse course, boolean unattendedImport, UserRequest ureq, WindowControl wControl) {
		ProjectBroker projectBroker = ProjectBrokerManagerFactory.getProjectBrokerManager().createAndSaveProjectBroker();
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		ProjectBrokerManagerFactory.getProjectBrokerManager().saveProjectBrokerId(projectBroker.getKey(), cpm, this);
		
		return null;
	}

	/**
	 * archives the dropbox of this task course node to the user's personal folder
	 * under private/archive/[coursename]/dropboxes/[nodeIdent].zip
	 * 
	 * @param locale
	 * @param course
	 * @param fArchiveDirectory
	 * @param charset
	 */
	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, File fArchiveDirectory, String charset) {
		boolean dataFound = false;
		String dropboxPath = FolderConfig.getCanonicalRoot() + DropboxController.getDropboxPathRelToFolderRoot(course.getCourseEnvironment(),this);
		File dropboxDir = new File(dropboxPath);
		String returnboxPath = FolderConfig.getCanonicalRoot() + ReturnboxController.getReturnboxPathRelToFolderRoot(course.getCourseEnvironment(),this);
		File returnboxDir = new File(returnboxPath);

		if (dropboxDir.exists() || returnboxDir.exists() ){
			// Create Temp Dir for zipping
			String tmpDirPath = FolderConfig.getCanonicalTmpDir() + course.getCourseEnvironment().getCourseBaseContainer().getRelPath();
			File tmpDir = new File( tmpDirPath );
			if (!tmpDir.exists()) {
			  tmpDir.mkdirs();
			}

			// prepare writing course results overview table
// TODO:ch 28.01.2010 : ProjectBroker does not support assessment-tool in V1.0			
// 			
//			List users = ScoreAccountingHelper.loadUsers(course.getCourseEnvironment());
//			List nodes = ScoreAccountingHelper.loadAssessableNodes(course.getCourseEnvironment());
//			String s = ScoreAccountingHelper.createCourseResultsOverviewTable(users, nodes, course, locale);
//	
//			String courseTitle = course.getCourseTitle();
//			String fileName = ExportUtil.createFileNameWithTimeStamp(courseTitle + "-score", "xls");
//	
//			// write course results overview table to filesystem
//			ExportUtil.writeContentToFile(fileName, s, tmpDir, charset);

			String projectBrokerTableExport = ProjectBrokerExportGenerator.createCourseResultsOverviewTable(this, course, locale);
			String tableExportFileName = ExportUtil.createFileNameWithTimeStamp(this.getShortTitle() + "-projectbroker_overview", "xls");
			ExportUtil.writeContentToFile(tableExportFileName, projectBrokerTableExport, tmpDir, charset);

			// prepare zipping the node directory and the course results overview table
			Set fileList = new HashSet();
			// move xls file to tmp dir
// TODO:ch 28.01.2010 : ProjectBroker does not support assessment-tool in V1.0
//			fileList.add(fileName);
			fileList.add(tableExportFileName);

			// copy dropboxes to tmp dir
			if (dropboxDir.exists()) {
				//OLAT-6426 archive only dropboxes of users that handed in at least one file -> prevent empty folders in archive
				boolean validDropboxesfound = false; 
				File[] themaFolderArray = dropboxDir.listFiles();
				for(File themaFolder: themaFolderArray){
					File[] userFolderArray = themaFolder.listFiles();
					if(userFolderArray==null) continue;
					for(File userFolder : userFolderArray){
						if(userFolder.isDirectory() && !FileUtils.isDirectoryEmpty(userFolder)){
							validDropboxesfound= true;
							File source = new File(dropboxDir+"/"+themaFolder.getName()+"/"+userFolder.getName());
							File target = new File(tmpDirPath+"/dropboxes/"+themaFolder.getName()+"/"+userFolder.getName()) ;
							FileUtils.copyDirContentsToDir(source, target,false, "archive projectbroker dropboxes ");
						}
					}
				}
				
				if(validDropboxesfound){
					// dropboxes exists, so there is something to archive
					fileList.add("dropboxes");
					dataFound |= true;
				}
			}
			
			// copy returnboxes to tmp dir
			if (returnboxDir.exists()) {
				boolean validReturnboxesfound = false; 
				File[] themaFolderArray = returnboxDir.listFiles();
				for(File themaFolder: themaFolderArray){
						if(themaFolder.isDirectory() && !FileUtils.isDirectoryEmpty(themaFolder)){
							validReturnboxesfound = true;
							File source = new File(returnboxDir+"/"+themaFolder.getName());
							File target = new File(tmpDirPath + "/returnboxes/"+themaFolder.getName());
							FileUtils.copyDirContentsToDir(source , target , false, "archive projectbroker returnboxes");
					}
				}
				if(validReturnboxesfound){
					fileList.add("returnboxes");
					//returnboxes exists, so there is something to archive
					dataFound |= true;
				}
			}
			
			if(dataFound) {
			  String zipName = ExportUtil.createFileNameWithTimeStamp(this.getIdent(), "zip");

			  java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss_SSS");
			  String exportDirName = "projectbroker_" + this.getShortName() + "_" + formatter.format(new Date(System.currentTimeMillis()));
			  File fDropBoxArchiveDir = new File(fArchiveDirectory, exportDirName);
			  if (!fDropBoxArchiveDir.exists()) {
				  fDropBoxArchiveDir.mkdir();
			  }
			  File archiveDir = new File(fDropBoxArchiveDir, zipName);	
			
			  // zip
			  dataFound &= ZipUtil.zip(fileList, tmpDir, archiveDir, true);
			  // Delete all temp files
			  FileUtils.deleteDirsAndFiles( tmpDir, true, true);
			}
		}	
  	return dataFound;
	}


	/**
	 * @see org.olat.course.nodes.GenericCourseNode#getConditionExpressions()
	 */
	@Override
	public List getConditionExpressions() {
		ArrayList retVal;
		List parentsConditions = super.getConditionExpressions();
		if (parentsConditions.size() > 0) {
			retVal = new ArrayList(parentsConditions);
		} else {
			retVal = new ArrayList();
		}
		//
		String conditionProjectBroker = getConditionProjectBroker().getConditionExpression();
		if (conditionProjectBroker != null && !conditionProjectBroker.equals("")) {
			// an active condition is defined
			ConditionExpression ce = new ConditionExpression(getConditionProjectBroker().getConditionId());
			ce.setExpressionString(getConditionProjectBroker().getConditionExpression());
			retVal.add(ce);
		}
		//
		return retVal;
	}


	/**
	 * Init config parameter with default values for a new course node.
	 */
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode) {
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			// use defaults for new course building blocks
			// dropbox defaults
			config.set(CONF_DROPBOX_ENABLED, Boolean.TRUE);
			config.set(CONF_DROPBOX_ENABLEMAIL, Boolean.FALSE);
			config.set(CONF_DROPBOX_CONFIRMATION, "");
			// scoring defaults
			config.set(CONF_SCORING_ENABLED, Boolean.FALSE);
			// returnbox defaults
			config.set(CONF_RETURNBOX_ENABLED, Boolean.TRUE);
			// New config parameter version 2
			config.setBooleanEntry(CONF_TASK_PREVIEW, false);
			MSCourseNode.initDefaultConfig(config);
	    config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
		} else {
			int version = config.getConfigurationVersion();
			if (version < CURRENT_CONFIG_VERSION) {
				// Loaded config is older than current config version => migrate
				if (version == 1) {
					// migrate V1 => V2 (remove all condition
					this.setConditionDrop(null);
					this.setConditionReturnbox(null);
					version = 2;
				}
				config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
			}
		}
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createInstanceForCopy()
	 */
	public CourseNode createInstanceForCopy(boolean isNewTitle) {
		CourseNode copyInstance = super.createInstanceForCopy(isNewTitle);
		return copyInstance;
	}

		
}