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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.logging.KnownIssueException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.nodes.iq.IQRunController;
import org.olat.course.nodes.iq.IQUIFactory;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.statistic.StatisticResourceOption;
import org.olat.course.statistic.StatisticResourceResult;
import org.olat.ims.qti.QTI12ResultDetailsController;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.QTIResultSet;
import org.olat.ims.qti.export.QTIExportFormatter;
import org.olat.ims.qti.export.QTIExportFormatterCSVType1;
import org.olat.ims.qti.export.QTIExportManager;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.FilePersister;
import org.olat.ims.qti.statistics.QTIStatisticResourceResult;
import org.olat.ims.qti.statistics.QTIStatisticSearchParams;
import org.olat.ims.qti.statistics.QTIType;
import org.olat.ims.qti.statistics.ui.QTI12PullTestsToolController;
import org.olat.ims.qti.statistics.ui.QTI12StatisticsToolController;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

import de.bps.ims.qti.QTIResultDetailsController;
import de.bps.onyx.plugin.OnyxExportManager;
import de.bps.onyx.plugin.OnyxModule;

/**
 * Initial Date: Feb 9, 2004
 * @author Mike Stock Comment:
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class IQTESTCourseNode extends AbstractAccessableCourseNode implements AssessableCourseNode, QTICourseNode {
	private static final long serialVersionUID = 5806292895738005387L;
	private static final OLog log = Tracing.createLoggerFor(IQTESTCourseNode.class);
	private static final String TYPE = "iqtest";

	private static final int CURRENT_CONFIG_VERSION = 2;

	public IQTESTCourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
	}
	
	/**
	 * @see org.olat.course.nodes.CourseNode#createEditController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl, org.olat.course.ICourse)
	 */
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		updateModuleConfigDefaults(false);
		TabbableController childTabCntrllr = IQUIFactory.createIQTestEditController(ureq, wControl, stackPanel, course, this, course.getCourseEnvironment().getCourseGroupManager(), euce); 
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, euce, childTabCntrllr);
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
		Controller controller = IQUIFactory.createIQTestRunController(ureq, wControl, userCourseEnv, this);
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, "o_iqtest_icon");
		return new NodeRunConstructionResult(ctrl);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createPreviewController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.course.run.userview.NodeEvaluation)
	 */
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		return IQUIFactory.createIQTestPreviewController(ureq, wControl, userCourseEnv, this);
	}

	@Override
	public List<Controller> createAssessmentTools(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			CourseEnvironment courseEnv, AssessmentToolOptions options) {
		List<Controller> tools = new ArrayList<>();
		tools.add(new QTI12StatisticsToolController(ureq, wControl, stackPanel, courseEnv, options, this));
		if(options.getGroup() == null && options.getIdentities() != null && options.getIdentities().size() > 0) {
			for(Identity assessedIdentity:options.getIdentities()) {
				if(isTestRunning(assessedIdentity, courseEnv)) {
					tools.add(new QTI12PullTestsToolController(ureq, wControl, courseEnv, options, this));
					break;
				}
			}
		}
		return tools;
	}
	
	public boolean isTestRunning(Identity assessedIdentity, CourseEnvironment courseEnv) {
		String resourcePath = courseEnv.getCourseResourceableId() + File.separator + getIdent();
		FilePersister qtiPersister = new FilePersister(assessedIdentity, resourcePath);
		return qtiPersister.exists();
	}

	@Override
	public StatisticResourceResult createStatisticNodeResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, StatisticResourceOption options, QTIType... types) {
		if(!isQTITypeAllowed(types)) return null;
		
		Long courseId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
		OLATResourceable courseOres = OresHelper.createOLATResourceableInstance("CourseModule", courseId);
		QTIStatisticSearchParams searchParams = new QTIStatisticSearchParams(courseOres.getResourceableId(), getIdent());
		searchParams.setLimitToGroups(options.getParticipantsGroups());

		QTIStatisticResourceResult result = new QTIStatisticResourceResult(courseOres, this, searchParams);
		return result;
	}
	
	@Override
	public boolean isStatisticNodeResultAvailable(UserCourseEnvironment userCourseEnv, QTIType... types) {
		return isQTITypeAllowed(types);
	}
	
	private boolean isQTITypeAllowed(QTIType... types) {
		if(types == null) return true;
		if(types.length == 0 || (types.length == 1 && types[0] == null)) return true;
		
		for(QTIType type:types) {
			if(QTIType.test.equals(type) || QTIType.onyx.equals(type)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid()
	 */
	@Override
	public StatusDescription isConfigValid() {
		/*
		 * first check the one click cache
		 */
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

		boolean isValid = getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY) != null;
		if (isValid) {
			/*
			 * COnfiugre an IQxxx BB with a repo entry, do not publish
			 * this BB, mark IQxxx as deleted, remove repo entry, undelete BB IQxxx
			 * and bang you enter this if.
			 */
			Object repoEntry = IQEditController.getIQReference(getModuleConfiguration(), false);
			if (repoEntry == null) {
				isValid = false;
				IQEditController.removeIQReference(getModuleConfiguration());
			}
		}
		StatusDescription sd = StatusDescription.NOERROR;
		if (!isValid) {
			String shortKey = "error.test.undefined.short";
			String longKey = "error.test.undefined.long";
			String[] params = new String[] { this.getShortTitle() };
			String translPackage = Util.getPackageName(IQEditController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(IQEditController.PANE_TAB_IQCONFIG_TEST);
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
		String translatorStr = Util.getPackageName(IQEditController.class);
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getUserScoreEvaluation(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public ScoreEvaluation getUserScoreEvaluation(UserCourseEnvironment userCourseEnvironment) {
		// read score from properties save score, passed and attempts information
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		Boolean passed = am.getNodePassed(this, mySelf);
		Float score = am.getNodeScore(this, mySelf);		
		Long assessmentID = am.getAssessmentID(this, mySelf);	
		Boolean fullyAssessed = am.getNodeFullyAssessed(this, mySelf);	
		ScoreEvaluation se = new ScoreEvaluation(score, passed, fullyAssessed, assessmentID);
		return se;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getCutValueConfiguration()
	 */
	@Override
	public Float getCutValueConfiguration() {
		ModuleConfiguration config = this.getModuleConfiguration();
		return (Float) config.get(IQEditController.CONFIG_KEY_CUTVALUE);
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getMaxScoreConfiguration()
	 */
	@Override
	public Float getMaxScoreConfiguration() {
		ModuleConfiguration config = this.getModuleConfiguration();
		return (Float) config.get(IQEditController.CONFIG_KEY_MAXSCORE);
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getMinScoreConfiguration()
	 */
	@Override
	public Float getMinScoreConfiguration() {
		ModuleConfiguration config = this.getModuleConfiguration();
		return (Float) config.get(IQEditController.CONFIG_KEY_MINSCORE);
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#hasCommentConfigured()
	 */
	@Override
	public boolean hasCommentConfigured() {
		// coach should be able to add comments here, visible to users
		return true;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#hasPassedConfigured()
	 */
	@Override
	public boolean hasPassedConfigured() {
		return true;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#hasScoreConfigured()
	 */
	@Override
	public boolean hasScoreConfigured() {
		return true;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#hasStatusConfigured()
	 */
	@Override
	public boolean hasStatusConfigured() {
		return false;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#isEditableConfigured()
	 */
	@Override
	public boolean isEditableConfigured() {
		// test scoring fields can be edited manually
		return true;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#updateUserCoachComment(java.lang.String,
	 *      org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public void updateUserCoachComment(String coachComment, UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		if (coachComment != null) {
			am.saveNodeCoachComment(this, userCourseEnvironment.getIdentityEnvironment().getIdentity(), coachComment);
		}
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#updateUserScoreEvaluation(org.olat.course.run.scoring.ScoreEvaluation,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.core.id.Identity)
	 */
	@Override
	public void updateUserScoreEvaluation(ScoreEvaluation scoreEvaluation, UserCourseEnvironment userCourseEnvironment,
			Identity coachingIdentity, boolean incrementAttempts) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		try {
		 am.saveScoreEvaluation(this, coachingIdentity, mySelf, scoreEvaluation, userCourseEnvironment, incrementAttempts);
		} catch(DBRuntimeException ex) {
			throw new KnownIssueException("DBRuntimeException - Row was updated or deleted...", 3570, ex);
		}
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#updateUserUserComment(java.lang.String,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.core.id.Identity)
	 */
	@Override
	public void updateUserUserComment(String userComment, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity) {
		if (userComment != null) {
			AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
			Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
			am.saveNodeComment(this, coachingIdentity, mySelf, userComment);
		}
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getUserCoachComment(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public String getUserCoachComment(UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		String coachCommentValue = am.getNodeCoachComment(this, mySelf);
		return coachCommentValue;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getUserUserComment(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public String getUserUserComment(UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		String userCommentValue = am.getNodeComment(this, mySelf);
		return userCommentValue;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getUserLog(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public String getUserLog(UserCourseEnvironment userCourseEnvironment) {
		UserNodeAuditManager am = userCourseEnvironment.getCourseEnvironment().getAuditManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		String logValue = am.getUserNodeLog(this, mySelf);
		return logValue;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#getReferencedRepositoryEntry()
	 */
	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		// ",false" because we do not want to be strict, but just indicate whether
		// the reference still exists or not
		RepositoryEntry re = IQEditController.getIQReference(getModuleConfiguration(), false);
		return re;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#needsReferenceToARepositoryEntry()
	 */
	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return true;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#informOnDelete(org.olat.core.gui.UserRequest,
	 *      org.olat.course.ICourse)
	 */
	@Override
	public String informOnDelete(Locale locale, ICourse course) {
		// Check if there are qtiresults for this test
		String repositorySoftKey = (String) getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		Long repKey = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
		if (QTIResultManager.getInstance().hasResultSets(course.getResourceableId(), this.getIdent(), repKey)) {
			Translator trans = Util.createPackageTranslator(IQRunController.class, locale);
			return trans.translate("info.nodedelete");
		}
		return null;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#cleanupOnDelete(org.olat.course.ICourse)
	 */
	@Override
	public void cleanupOnDelete(ICourse course) {
		CoursePropertyManager pm = course.getCourseEnvironment().getCoursePropertyManager();
		// 1) Delete all properties: score, passed, log, comment, coach_comment,
		// attempts
		pm.deleteNodeProperties(this, null);
		// 2) Delete all qtiresults for this node
		String repositorySoftKey = (String) getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, false);
		if(re != null) {
			QTIResultManager.getInstance().deleteAllResults(course.getResourceableId(), getIdent(), re.getKey());
		}
	}

	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options, ZipOutputStream exportStream, String charset) {
		String repositorySoftKey = (String)getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		Long courseResourceableId = course.getResourceableId();

		try {
			RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true);
			boolean onyx = OnyxModule.isOnyxTest(re.getOlatResource());
			if (onyx) {
				QTIResultManager qrm = QTIResultManager.getInstance();
				List<QTIResultSet> results = qrm.getResultSets(courseResourceableId, getIdent(), re.getKey(), null);
				if (results.size() > 0) {
					OnyxExportManager.getInstance().exportResults(results, exportStream, this);
				}
				return true;
			} else {
				String shortTitle = getShortTitle();
				QTIExportManager qem = QTIExportManager.getInstance();
				QTIExportFormatter qef = new QTIExportFormatterCSVType1(locale, "\t", "\"", "\r\n", false);
				return qem.selectAndExportResults(qef, courseResourceableId, shortTitle, getIdent(), re, exportStream, ".xls");
			}
		} catch (IOException e) {
			log.error("", e);
			return false;
		}
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#exportNode(java.io.File,
	 *      org.olat.course.ICourse)
	 */
	@Override
	public void exportNode(File exportDirectory, ICourse course) {
		String repositorySoftKey = (String) getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		if (repositorySoftKey == null) return; // nothing to export
		//self healing
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, false);
		if(re==null) {
			//nothing to export, but correct the module configuration
			IQEditController.removeIQReference(getModuleConfiguration());
			return;
		}
		File fExportDirectory = new File(exportDirectory, getIdent());
		fExportDirectory.mkdirs();
		RepositoryEntryImportExport reie = new RepositoryEntryImportExport(re, fExportDirectory);
		reie.exportDoExport();
	}

	@Override
	public void importNode(File importDirectory, ICourse course, Identity owner, Locale locale, boolean withReferences) {
		RepositoryEntryImportExport rie = new RepositoryEntryImportExport(importDirectory, getIdent());
		if(withReferences && rie.anyExportedPropertiesAvailable()) {
			RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(TestFileResource.TYPE_NAME);
			RepositoryEntry re = handler.importResource(owner, rie.getInitialAuthor(), rie.getDisplayName(),
				rie.getDescription(), false, locale, rie.importGetExportedFile(), null);
			IQEditController.setIQReference(re, getModuleConfiguration());
		} else {
			IQEditController.removeIQReference(getModuleConfiguration());
		}
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getUserAttempts(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public Integer getUserAttempts(UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		Integer userAttemptsValue = am.getNodeAttempts(this, mySelf);
		return userAttemptsValue;

	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#hasAttemptsConfigured()
	 */
	@Override
	public boolean hasAttemptsConfigured() {
		return true;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#updateUserAttempts(java.lang.Integer,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.core.id.Identity)
	 */
	@Override
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
	@Override
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
	@Override
	public Controller getDetailsEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, UserCourseEnvironment userCourseEnvironment) {
		RepositoryEntry ref = getReferencedRepositoryEntry();
		Long courseResourceableId = userCourseEnvironment.getCourseEnvironment().getCourseResourceableId();
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		boolean onyx = OnyxModule.isOnyxTest(ref.getOlatResource());
		if(onyx) {
			return new QTIResultDetailsController(courseResourceableId, getIdent(), assessedIdentity, ref, AssessmentInstance.QMD_ENTRY_TYPE_ASSESS, ureq, wControl);
		} else {
			return new QTI12ResultDetailsController(ureq, wControl, courseResourceableId, getIdent(), assessedIdentity, ref, AssessmentInstance.QMD_ENTRY_TYPE_ASSESS);
		}
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getDetailsListView(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public String getDetailsListView(UserCourseEnvironment userCourseEnvironment) {
		return null;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getDetailsListViewHeaderKey()
	 */
	@Override
	public String getDetailsListViewHeaderKey() {
		return null;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#hasDetails()
	 */
	@Override
	public boolean hasDetails() {
		return true;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createInstanceForCopy()
	 */
	@Override
	public CourseNode createInstanceForCopy(boolean isNewTitle, ICourse course, Identity author) {
		CourseNode copyInstance = super.createInstanceForCopy(isNewTitle, course, author);
		IQEditController.removeIQReference(copyInstance.getModuleConfiguration());
		return copyInstance;
	}

	/**
	 * Update the module configuration to have all mandatory configuration flags
	 * set to usefull default values
	 * 
	 * @param isNewNode true: an initial configuration is set; false: upgrading
	 *          from previous node configuration version, set default to maintain
	 *          previous behaviour
	 */
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode) {
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			// add default module configuration
			config.set(IQEditController.CONFIG_KEY_ENABLEMENU, new Boolean(true));
			config.set(IQEditController.CONFIG_KEY_SEQUENCE, AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM);
			config.set(IQEditController.CONFIG_KEY_TYPE, AssessmentInstance.QMD_ENTRY_TYPE_ASSESS);
			config.set(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT);
			config.set(IQEditController.CONFIG_KEY_ENABLESCOREINFO, new Boolean(true));
		} else {
			int version = config.getConfigurationVersion();
			if (version < CURRENT_CONFIG_VERSION) {
				// Loaded config is older than current config version => migrate
				if (version == 1) {
					// migrate V1 => V2, new parameter 'enableScoreInfo'
					version = 2;
					config.set(IQEditController.CONFIG_KEY_ENABLESCOREINFO, new Boolean(true));
				}
				config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
			}
		}
	}

}