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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseEntryRef;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.DueDateService;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.PublishEvents;
import org.olat.course.editor.StatusDescription;
import org.olat.course.editor.importnodes.ImportSettings;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.folder.CourseContainerOptions;
import org.olat.course.learningpath.ui.TabbableLeaningPathNodeConfigController;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.iq.IQConfirmationMailTemplate;
import org.olat.course.nodes.iq.IQDueDateConfig;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.nodes.iq.IQPreviewController;
import org.olat.course.nodes.iq.IQTESTAssessmentConfig;
import org.olat.course.nodes.iq.IQTESTCoachRunController;
import org.olat.course.nodes.iq.IQTESTLearningPathNodeHandler;
import org.olat.course.nodes.iq.QTI21AssessmentRunController;
import org.olat.course.nodes.ms.MSCourseNodeRunController;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.reminder.AssessmentReminderProvider;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.course.statistic.StatisticResourceOption;
import org.olat.course.statistic.StatisticResourceResult;
import org.olat.course.statistic.StatisticType;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.AssessmentTestSessionDAO;
import org.olat.ims.qti21.manager.archive.QTI21ArchiveFormat;
import org.olat.ims.qti21.model.DigitalSignatureOptions;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.ims.qti21.model.xml.QtiMaxScoreEstimator;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.resultexport.QTI21ResultsExport;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.AssessmentTestSessionComparator;
import org.olat.ims.qti21.ui.statistics.QTI21StatisticResourceResult;
import org.olat.ims.qti21.ui.statistics.QTI21StatisticsSecurityCallback;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingAssignmentStatus;
import org.olat.modules.grading.GradingService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.olat.repository.ui.author.copy.wizard.CopyCourseOverviewRow;
import org.olat.resource.OLATResource;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * Initial Date: Feb 9, 2004
 * @author Mike Stock Comment:
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class IQTESTCourseNode extends AbstractAccessableCourseNode implements QTICourseNode {
	private static final long serialVersionUID = 5806292895738005387L;
	private static final Logger log = Tracing.createLoggerFor(IQTESTCourseNode.class);
	@SuppressWarnings("deprecation")
	private static final String TRANSLATOR_PACKAGE = Util.getPackageName(IQEditController.class);
	public static final String TYPE = "iqtest";

	private static final int CURRENT_CONFIG_VERSION = 3;

	private transient RepositoryEntry cachedReferenceRepositoryEntry;

	public IQTESTCourseNode() {
		super(TYPE);
	}
	
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		TabbableController childTabCntrllr = new IQEditController(ureq, wControl, stackPanel, course, this, euce);
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
		// Do not allow guests to start tests
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			if(isGuestAllowedForQTI21(getReferencedRepositoryEntry())) {
				controller = new QTI21AssessmentRunController(ureq, wControl, userCourseEnv, this);
			} else {
				controller = MessageUIFactory.createGuestNoAccessMessage(ureq, wControl, null);
			}
		} else {
			RepositoryEntry testEntry = getReferencedRepositoryEntry();
			OLATResource ores = testEntry.getOlatResource();
			if(ImsQTI21Resource.TYPE_NAME.equals(ores.getResourceableTypeName())) {
				//QTI 2.1
				if (userCourseEnv.isCoach() || userCourseEnv.isAdmin()) {
					controller = new IQTESTCoachRunController(ureq, wControl, userCourseEnv, this);
				} else {
					controller = new QTI21AssessmentRunController(ureq, wControl, userCourseEnv, this);
				}
			} else  {
				Translator transe = Util.createPackageTranslator(IQEditController.class, ureq.getLocale());
				controller = MessageUIFactory.createInfoMessage(ureq, wControl, "", transe.translate("error.qti12"));
			}
		}
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, "o_iqtest_icon");
		return new NodeRunConstructionResult(ctrl);
	}
	
	public boolean isGuestAllowedForQTI21(RepositoryEntry testEntry) {
		OLATResource ores = testEntry.getOlatResource();
		if(ImsQTI21Resource.TYPE_NAME.equals(ores.getResourceableTypeName())) {
			QTI21DeliveryOptions options = CoreSpringFactory.getImpl(QTI21Service.class).getDeliveryOptions(testEntry);
			boolean allowAnonym = options != null && options.isAllowAnonym();
			allowAnonym = getModuleConfiguration().getBooleanSafe(IQEditController.CONFIG_ALLOW_ANONYM, allowAnonym);
			return allowAnonym;
		}
		return false;
	}
	
	/**
	 * @param testEntry The test repository entry
	 * @param courseEntry Thie course entry
	 * @param identity 
	 * @return true if the course node or the test has a time limit set.
	 */
	public boolean hasQTI21TimeLimit(RepositoryEntry testEntry, RepositoryEntry courseEntry, Identity identity) {
		boolean timeLimit = false;
		if(ImsQTI21Resource.TYPE_NAME.equals(testEntry.getOlatResource().getResourceableTypeName())) {
			ModuleConfiguration config = getModuleConfiguration();
			boolean configRef = config.getBooleanSafe(IQEditController.CONFIG_KEY_CONFIG_REF, false);
			if((!configRef && config.getIntegerSafe(IQEditController.CONFIG_KEY_TIME_LIMIT, -1) > 0)
					|| (CoreSpringFactory.getImpl(DueDateService.class).getDueDate(getDueDateConfig(IQEditController.CONFIG_KEY_END_TEST_DATE), courseEntry, identity) != null)) {
				timeLimit = true;
			} else {
				AssessmentTest assessmentTest = loadAssessmentTest(testEntry);
				if(assessmentTest != null && assessmentTest.getTimeLimits() != null && assessmentTest.getTimeLimits().getMaximum() != null) {
					timeLimit = true;
				}
			}
		}
		return timeLimit;
	}
	
	public Double getQTI21EvaluatedMaxScore(RepositoryEntry testEntry) {
		Double estimatedMaxScore = null;
		if(ImsQTI21Resource.TYPE_NAME.equals(testEntry.getOlatResource().getResourceableTypeName())) {
			ResolvedAssessmentTest resolvedAssessmentTest = loadResolvedAssessmentTest(testEntry);
			estimatedMaxScore = QtiMaxScoreEstimator.estimateMaxScore(resolvedAssessmentTest);
		}
		return estimatedMaxScore;
	}
	
	/**
	 * If the course element override the test configuration, the value is from
	 * the course element's configuration. Else, the value is from the assessment
	 * test.
	 * 
	 * @param testEntry The test repository entry
	 * @return the maximum time limit in seconds or -1 if no time limit is configured
	 */
	public int getQTI21TimeLimitMaxInSeconds(RepositoryEntry testEntry) {
		int timeLimit = -1;
		if(ImsQTI21Resource.TYPE_NAME.equals(testEntry.getOlatResource().getResourceableTypeName())) {
			ModuleConfiguration config = getModuleConfiguration();
			boolean configRef = config.getBooleanSafe(IQEditController.CONFIG_KEY_CONFIG_REF, false);
			if(!configRef && config.getIntegerSafe(IQEditController.CONFIG_KEY_TIME_LIMIT, -1) > 0) {
				timeLimit = config.getIntegerSafe(IQEditController.CONFIG_KEY_TIME_LIMIT, -1);
			} else {
				AssessmentTest assessmentTest = loadAssessmentTest(testEntry);
				if(assessmentTest != null && assessmentTest.getTimeLimits() != null && assessmentTest.getTimeLimits().getMaximum() != null) {
					timeLimit = assessmentTest.getTimeLimits().getMaximum().intValue();
				}
			}
		}
		return timeLimit;
	}

	public DigitalSignatureOptions getSignatureOptions(AssessmentTestSession session, Locale locale) {
		RepositoryEntry testEntry = session.getTestEntry();
		RepositoryEntry courseEntry = session.getRepositoryEntry();
		QTI21DeliveryOptions deliveryOptions = CoreSpringFactory.getImpl(QTI21Service.class)
				.getDeliveryOptions(testEntry);
		
		ModuleConfiguration config = getModuleConfiguration();
		boolean digitalSignature = config.getBooleanSafe(IQEditController.CONFIG_DIGITAL_SIGNATURE,
			deliveryOptions.isDigitalSignature());
		boolean sendMail = config.getBooleanSafe(IQEditController.CONFIG_DIGITAL_SIGNATURE_SEND_MAIL,
			deliveryOptions.isDigitalSignatureMail());

		DigitalSignatureOptions options = new DigitalSignatureOptions(digitalSignature, sendMail, courseEntry, testEntry);
		if(digitalSignature) {
			CourseEnvironment courseEnv = CourseFactory.loadCourse(courseEntry).getCourseEnvironment();
			QTI21AssessmentRunController.decorateCourseConfirmation(session, options, courseEnv, this, testEntry, null, locale);
		}
		return options;
	}
	
	public boolean isScoreVisibleAfterCorrection() {
		String defVisibility = CoreSpringFactory.getImpl(QTI21Module.class).isResultsVisibleAfterCorrectionWorkflow()
				? IQEditController.CONFIG_VALUE_SCORE_VISIBLE_AFTER_CORRECTION : IQEditController.CONFIG_VALUE_SCORE_NOT_VISIBLE_AFTER_CORRECTION;
		String visibility = getModuleConfiguration().getStringValue(IQEditController.CONFIG_KEY_SCORE_VISIBILITY_AFTER_CORRECTION, defVisibility);
		return IQEditController.CONFIG_VALUE_SCORE_VISIBLE_AFTER_CORRECTION.equals(visibility);
	}
	
	public AssessmentTest loadAssessmentTest(RepositoryEntry testEntry) {
		if(testEntry == null) return null;
		
		ResolvedAssessmentTest resolvedAssessmentTest = loadResolvedAssessmentTest(testEntry);
		if(resolvedAssessmentTest != null) {
			return resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
		}
		return null;
	}
	
	public ResolvedAssessmentTest loadResolvedAssessmentTest(RepositoryEntry testEntry) {
		if(testEntry == null) return null;
		
		File unzippedDirRoot = FileResourceManager.getInstance().unzipFileResource(testEntry.getOlatResource());
		return CoreSpringFactory.getImpl(QTI21Service.class)
				.loadAndResolveAssessmentTest(unzippedDirRoot, false, false);
	}

	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		Controller controller;
		RepositoryEntry qtiTestEntry = getReferencedRepositoryEntry();
		if (ImsQTI21Resource.TYPE_NAME.equals(qtiTestEntry.getOlatResource().getResourceableTypeName())) {
			controller = new IQPreviewController(ureq, wControl, userCourseEnv, this);
		} else {
			Translator trans = Util.createPackageTranslator(IQEditController.class, ureq.getLocale());
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, "", trans.translate("error.onyx"));
		} 
		return controller;
	}

	@Override
	public StatisticResourceResult createStatisticNodeResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, StatisticResourceOption options, StatisticType type) {
		if(!isStatisticTypeAllowed(type)) return null;
		
		RepositoryEntry qtiTestEntry = getReferencedRepositoryEntry();
		if(ImsQTI21Resource.TYPE_NAME.equals(qtiTestEntry.getOlatResource().getResourceableTypeName())) {
			RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			QTI21StatisticSearchParams searchParams = new QTI21StatisticSearchParams(qtiTestEntry, courseEntry, getIdent());
			boolean admin = userCourseEnv.isAdmin();
			boolean canViewFakeParticipants = false;
			if(options.getParticipantsGroups() != null) {
				searchParams.setLimitToGroups(options.getParticipantsGroups());
			} else {
				Set<IdentityRef> fakeParticipants = CoreSpringFactory.getImpl(AssessmentToolManager.class)
						.getFakeParticipants(courseEntry, userCourseEnv.getIdentityEnvironment().getIdentity(), admin, userCourseEnv.isCoach());
				if (!fakeParticipants.isEmpty()) {
					searchParams.setFakeParticipants(fakeParticipants);
					canViewFakeParticipants = true;
				}
			}
			QTI21StatisticsSecurityCallback secCallback = new QTI21StatisticsSecurityCallback(admin, admin && isGuestAllowedForQTI21(qtiTestEntry), canViewFakeParticipants);
			return new QTI21StatisticResourceResult(qtiTestEntry, courseEntry, this, searchParams, secCallback);
		}
		
		return null;
	}
	
	@Override
	public boolean isStatisticNodeResultAvailable(UserCourseEnvironment userCourseEnv, StatisticType type) {
		return isStatisticTypeAllowed(type);
	}
	
	private boolean isStatisticTypeAllowed(StatisticType type) {
		if(StatisticType.TEST.equals(type)) {
			return true;
		}
		return false;
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
		
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, TRANSLATOR_PACKAGE, getConditionExpressions());
		if(oneClickStatusCache != null && oneClickStatusCache.length > 0) {
			//isConfigValidWithTranslator add first
			sds.remove(oneClickStatusCache[0]);
		}
		sds.addAll(validateInternalConfiguration(cev));
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}

	private List<StatusDescription> validateInternalConfiguration(CourseEditorEnv cev) {
		List<StatusDescription> sdList = new ArrayList<>(2);

		boolean hasTestReference = getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY) != null;
		if (hasTestReference) {
			/*
			 * Configure an IQxxx BB with a repo entry, do not publish
			 * this BB, mark IQxxx as deleted, remove repo entry, undelete BB IQxxx
			 * and bang you enter this if.
			 */
			Object repoEntry = IQEditController.getIQReference(getModuleConfiguration(), false);
			if (repoEntry == null) {
				hasTestReference = false;
				IQEditController.removeIQReference(getModuleConfiguration());
			}
		}
		if (!hasTestReference) {
			addStatusErrorDescription("error.test.undefined.short", "error.test.undefined.long",
					IQEditController.PANE_TAB_IQCONFIG_TEST, sdList);
		}
		
		if (cev != null) {
			IQTESTAssessmentConfig assessmentConfig = new IQTESTAssessmentConfig(new CourseEntryRef(cev), this);
			
			if (isFullyAssessedScoreConfigError(assessmentConfig)) {
				addStatusErrorDescription("error.fully.assessed.score", "error.fully.assessed.score",
						TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
			}
			if (isFullyAssessedPassedConfigError(assessmentConfig)) {
				addStatusErrorDescription("error.fully.assessed.passed", "error.fully.assessed.passed",
						TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
			}
			if (getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_ENABLED) && CoreSpringFactory.getImpl(GradeModule.class).isEnabled()) {
				GradeService gradeService = CoreSpringFactory.getImpl(GradeService.class);
				GradeScale gradeScale = gradeService.getGradeScale(cev.getCourseGroupManager().getCourseEntry(), getIdent());
				if (gradeScale == null) {
					addStatusErrorDescription("error.missing.grade.scale", "error.fully.assessed.passed",
							IQEditController.PANE_TAB_IQCONFIG_TEST, sdList);
				}
			}
		}
		
		return sdList;
	}
	
	private boolean isFullyAssessedScoreConfigError(IQTESTAssessmentConfig assessmentConfig) {
		boolean hasScore = Mode.none != assessmentConfig.getScoreMode();
		boolean isScoreTrigger = CoreSpringFactory.getImpl(IQTESTLearningPathNodeHandler.class)
				.getConfigs(this)
				.isFullyAssessedOnScore(null, null)
				.isEnabled();
		return isScoreTrigger && !hasScore;
	}
	
	private boolean isFullyAssessedPassedConfigError(IQTESTAssessmentConfig assessmentConfig) {
		boolean hasPassed = assessmentConfig.getPassedMode() != Mode.none;
		boolean isPassedTrigger = CoreSpringFactory.getImpl(IQTESTLearningPathNodeHandler.class)
				.getConfigs(this)
				.isFullyAssessedOnPassed(null, null)
				.isEnabled();
		return isPassedTrigger && !hasPassed;
	}

	private void addStatusErrorDescription(String shortDescKey, String longDescKey, String pane,
			List<StatusDescription> status) {
		String[] params = new String[] { getShortTitle() };
		StatusDescription sd = new StatusDescription(StatusDescription.ERROR, shortDescKey, longDescKey, params,
				TRANSLATOR_PACKAGE);
		sd.setDescriptionForUnit(getIdent());
		sd.setActivateableViewIdentifier(pane);
		status.add(sd);
	}
	
	/**
	 * @return A cached instance of the reference repository entry. May be not suitable
	 * 		to insert an assessment entry.
	 */
	public RepositoryEntry getCachedReferencedRepositoryEntry() {
		RepositoryEntry cachedEntry = cachedReferenceRepositoryEntry;
		if(IQEditController.matchIQReference(cachedEntry, getModuleConfiguration())) {
			return cachedEntry;
		}
		// The method updates the cache
		return getReferencedRepositoryEntry();
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		// ",false" because we do not want to be strict, but just indicate whether
		// the reference still exists or not
		RepositoryEntry entry = IQEditController.getIQReference(getModuleConfiguration(), false);
		cachedReferenceRepositoryEntry = entry;
		return entry;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return true;
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		
		CoursePropertyManager pm = course.getCourseEnvironment().getCoursePropertyManager();
		// 1) Delete all properties: score, passed, log, comment, coach_comment,
		// attempts
		pm.deleteNodeProperties(this, null);
		// 2) Delete all assessment test sessions (QTI 2.1)
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		CoreSpringFactory.getImpl(AssessmentTestSessionDAO.class).deleteAllUserTestSessionsByCourse(courseEntry, getIdent());
		
		// Delete GradeScales
		CoreSpringFactory.getImpl(GradeService.class).deleteGradeScale(courseEntry, getIdent());
	}

	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options,
			ZipOutputStream exportStream, String archivePath, String charset) {
		
		String repositorySoftKey = (String)getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		
		// 1) prepare result export
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		try {
			RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, false);
			if(re == null) {
				log.error("Cannot archive course node. Missing repository entry with soft key: {}", repositorySoftKey);
				return false;
			}
			
			if(ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())) {
				// 2a) create export resource
				List<Identity> identities = ScoreAccountingHelper.loadUsers(courseEnv, options);
				boolean withPdfs = options != null && options.getDoer() != null && options.getWindowControl() != null && options.isWithPdfs();
				Translator translator = Util.createPackageTranslator(QTI21ResultsExport.class, locale);
				String exportFolderName = ZipUtil.concat(archivePath, translator.translate("export.folder.name"));
				new QTI21ResultsExport(courseEnv, identities, true, withPdfs, this, exportFolderName, locale,
						options == null ? null : options.getDoer(), options == null ? null : options.getWindowControl())
					.exportTestResults(exportStream);
				// excel results
				RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
				QTI21StatisticSearchParams searchParams = new QTI21StatisticSearchParams(options, re, courseEntry, getIdent());
				QTI21ArchiveFormat qaf = new QTI21ArchiveFormat(locale, searchParams);
				qaf.exportCourseElement(exportStream, archivePath);
				return true;	
			}
			return false;
		} catch (IOException e) {
			log.error("", e);
			return false;
		}
	}

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
	public void importNode(File importDirectory, ICourse course, Identity owner, Organisation organisation, Locale locale, boolean withReferences) {
		RepositoryEntryImportExport rie = new RepositoryEntryImportExport(importDirectory, getIdent());
		if(withReferences && rie.anyExportedPropertiesAvailable()) {
			File file = rie.importGetExportedFile();
			RepositoryHandler handlerQTI21 = RepositoryHandlerFactory.getInstance().getRepositoryHandler(ImsQTI21Resource.TYPE_NAME);
			if(handlerQTI21.acceptImport(file, "repo.zip").isValid()) {
				RepositoryEntry re = handlerQTI21.importResource(owner, rie.getInitialAuthor(), rie.getDisplayName(),
						rie.getDescription(), false, organisation, locale, rie.importGetExportedFile(), null);
				getModuleConfiguration().set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI21);
				IQEditController.setIQReference(re, getModuleConfiguration());
			} else {
				IQEditController.removeIQReference(getModuleConfiguration());
			}
		} else {
			IQEditController.removeIQReference(getModuleConfiguration());
		}
	}
	
	public void sendConfirmationEmail(UserCourseEnvironment assessedUserCourseEnv, AssessmentConfig assessmentConfig, Locale locale) {
		if(!getModuleConfiguration().getBooleanSafe(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_ENABLED, false)) {
			return;
		}
		
		final Translator translator = Util.createPackageTranslator(QTI21AssessmentRunController.class, locale,
				Util.createPackageTranslator(AssessmentTestDisplayController.class, locale,
					Util.createPackageTranslator(MSCourseNodeRunController.class, locale,
						Util.createPackageTranslator(GradeUIFactory.class, locale)))); 
		final RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		final RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		final Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		
		// Copy to
		ContactList bccList = new ContactList("");
		List<String> copyList = getModuleConfiguration().getList(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_COPY, String.class);
		Set<Identity> cc = new HashSet<>();
		if(copyList.contains(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_COPY_TO_OWNER)) {
			cc.addAll(repositoryService.getMembers(courseEntry, RepositoryEntryRelationType.all, GroupRoles.owner.name()));
		}
		if(copyList.contains(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_COPY_TO_ASSIGNED_COACH)) {
			cc.addAll(repositoryService.getAssignedCoaches(assessedIdentity, courseEntry));
		}
		bccList.addAllIdentites(cc);
		
		if(copyList.contains(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_COPY_TO_CUSTOM)) {
			String copyCustom = getModuleConfiguration().getStringValue(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_COPY_CUSTOM);
			if(StringHelper.containsNonWhitespace(copyCustom)) {
				Arrays.stream(copyCustom.replaceAll("\\s", "").split(","))
					.filter(MailHelper::isValidEmailAddress)
					.forEach(bccList::add);
			}
		}

		// Email
		String subject = getModuleConfiguration().getStringValue(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_SUBJECT);
		String body = getModuleConfiguration().getStringValue(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_BODY);
		if(!StringHelper.containsNonWhitespace(body)) {
			body = IQEditController.getDefaultConfirmationEmailText(getModuleConfiguration(), locale);
		}

		MailContext context = new MailContextImpl("[RepositoryEntry:" + courseEntry.getKey() + "]");
		String url = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + courseEntry.getKey();
		
		IQConfirmationMailTemplate mailTemplate = new IQConfirmationMailTemplate(subject, body, url,
				courseEntry, this, assessedUserCourseEnv, assessmentConfig, translator, locale);
		MailManager mailManager = CoreSpringFactory.getImpl(MailManager.class);
		MailerResult result = new MailerResult();
		MailBundle bundle = mailManager.makeMailBundle(context, assessedIdentity, mailTemplate, null, null, result);
		if(bccList.hasAddresses()) {
			bundle.setContactList(bccList);
		}
		mailManager.sendMessageAsync(bundle);
	}

	public void pullAssessmentTestSession(AssessmentTestSession session, UserCourseEnvironment assessedUserCourseEnv,
			Identity coachingIdentity, Role by, Locale locale) {
		Boolean visibility;
		AssessmentEntryStatus assessmentStatus;
		String correctionMode = getModuleConfiguration().getStringValue(IQEditController.CONFIG_CORRECTION_MODE);
		if(IQEditController.CORRECTION_MANUAL.equals(correctionMode)) {
			assessmentStatus = AssessmentEntryStatus.inReview;
			visibility = Boolean.FALSE;
		} else {
			assessmentStatus = AssessmentEntryStatus.done;
			visibility = Boolean.TRUE;
		}
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(assessedUserCourseEnv), this);

		AssessmentTest assessmentTest = loadAssessmentTest(session.getTestEntry());
		Double cutValue = QtiNodesExtractor.extractCutValue(assessmentTest);

		BigDecimal finalScore = session.getFinalScore();
		Float score = finalScore == null ? null : finalScore.floatValue();
		String grade = null;
		String gradeSystemIdent = null;
		String performanceClassIdent = null;
		Boolean passed = session.getPassed();
		if(finalScore != null) {
			if (assessmentConfig.hasGrade() && CoreSpringFactory.getImpl(GradeModule.class).isEnabled()) {
				AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(this, assessedUserCourseEnv);
				if (assessmentConfig.isAutoGrade() || (assessmentEntry != null && StringHelper.containsNonWhitespace(assessmentEntry.getGrade()))) {
					GradeService gradeService = CoreSpringFactory.getImpl(GradeService.class);
					GradeScale gradeScale = gradeService.getGradeScale(
							assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(),
							this.getIdent());
					NavigableSet<GradeScoreRange> gradeScoreRanges = null;gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, locale);
					GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, score);
					grade = gradeScoreRange.getGrade();
					gradeSystemIdent = gradeScoreRange.getGradeSystemIdent();
					performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
					passed = gradeScoreRange.getPassed();
				}
			} else if (cutValue != null) {
				boolean calculated = finalScore.compareTo(BigDecimal.valueOf(cutValue.doubleValue())) >= 0;
				passed = Boolean.valueOf(calculated);
			}
		}
		ScoreEvaluation sceval = new ScoreEvaluation(score, grade, gradeSystemIdent, performanceClassIdent, passed,
				assessmentStatus, visibility, null, 1.0d, AssessmentRunStatus.done, session.getKey());
		courseAssessmentService.updateScoreEvaluation(this, sceval, assessedUserCourseEnv, coachingIdentity, true, by);
		
		if(getModuleConfiguration().getBooleanSafe(IQEditController.CONFIG_KEY_CONFIRMATION_EMAIL_ENABLED, false)) {
			sendConfirmationEmail(assessedUserCourseEnv, assessmentConfig, locale);
		}
		
		if(IQEditController.CORRECTION_GRADING.equals(correctionMode)) {
			AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(this, assessedUserCourseEnv);
			RepositoryEntry testEntry = IQEditController.getIQReference(getModuleConfiguration(), false);
			CoreSpringFactory.getImpl(GradingService.class).assignGrader(testEntry, assessmentEntry, session.getFinishTime(), true);
		}
	}
	
	public void promoteAssessmentTestSession(AssessmentTestSession testSession, UserCourseEnvironment assessedUserCourseEnv,
			boolean updateScoring, Identity coachingIdentity, Role by, Locale locale) {
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		AssessmentEntry currentAssessmentEntry = null;
		
		Float score = null;
		String grade = null;
		String gradeSystemIdent = null;
		String performanceClassIdent = null;
		Boolean passed = null;
		if(updateScoring) {
			AssessmentTest assessmentTest = loadAssessmentTest(testSession.getTestEntry());
			Double cutValue = QtiNodesExtractor.extractCutValue(assessmentTest);
	
			BigDecimal finalScore = testSession.getFinalScore();
			score = finalScore == null ? null : finalScore.floatValue();
			passed = testSession.getPassed();
			if(testSession.getManualScore() != null && finalScore != null) {
				AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(assessedUserCourseEnv), this);
				if (assessmentConfig.hasGrade() && CoreSpringFactory.getImpl(GradeModule.class).isEnabled()) {
					currentAssessmentEntry = courseAssessmentService.getAssessmentEntry(this, assessedUserCourseEnv);
					if (assessmentConfig.isAutoGrade() || (currentAssessmentEntry != null && StringHelper.containsNonWhitespace(currentAssessmentEntry.getGrade()))) {
						GradeService gradeService = CoreSpringFactory.getImpl(GradeService.class);
						GradeScale gradeScale = gradeService.getGradeScale(
								assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(),
								this.getIdent());
						NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, locale);
						GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, score);
						grade = gradeScoreRange.getGrade();
						gradeSystemIdent = gradeScoreRange.getGradeSystemIdent();
						performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
						passed = gradeScoreRange.getPassed();
					}
				} else if (cutValue != null) {
					boolean calculated = finalScore.compareTo(BigDecimal.valueOf(cutValue.doubleValue())) >= 0;
					passed = Boolean.valueOf(calculated);
				}
			}
		}
		
		if (currentAssessmentEntry == null) {
			currentAssessmentEntry = courseAssessmentService.getAssessmentEntry(this, assessedUserCourseEnv);
		}
		boolean increment = currentAssessmentEntry.getAttempts() == null || currentAssessmentEntry.getAttempts().intValue() == 0;
		ScoreEvaluation sceval = new ScoreEvaluation(score, grade, gradeSystemIdent, performanceClassIdent, passed,
				null, null, null, 1.0d, AssessmentRunStatus.done, testSession.getKey());
		courseAssessmentService.updateScoreEvaluation(this, sceval, assessedUserCourseEnv, coachingIdentity, increment, by);
	}
	
	@Override
	public void updateOnPublish(Locale locale, ICourse course, Identity publisher, PublishEvents publishEvents) {
		//Reset the AssessmentEntry and invalidate the test sessions if the referenced test has changed.
		RepositoryEntry testEntry = getReferencedRepositoryEntry();
		if(testEntry == null) {
			return; // not possible but if the test is deleted...
		}
		
		Long testEntryKey = testEntry.getKey();
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		AssessmentService assessmentService = CoreSpringFactory.getImpl(AssessmentService.class);
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, this);
		boolean gradeEnabled = CoreSpringFactory.getImpl(GradeModule.class).isEnabled();
		GradeService gradeService = CoreSpringFactory.getImpl(GradeService.class);
		NavigableSet<GradeScoreRange> gradeScoreRanges = null;
		if (gradeEnabled && assessmentConfig.hasGrade()) {
			GradeScale gradeScale = gradeService.getGradeScale(courseEntry, this.getIdent());
			gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, locale);
		}
		
		List<AssessmentEntry> assessmentEntries = assessmentService.loadAssessmentEntriesBySubIdent(courseEntry, getIdent());
		Map<Long, List<AssessmentTestSession>> identityKeyToSessions = CoreSpringFactory.getImpl(QTI21Service.class)
				.getAssessmentTestSessions(courseEntry, this.getIdent(), testEntry).stream()
				.filter(this::isSessionWithPassed)
				.collect(Collectors.groupingBy(session -> session.getIdentity().getKey()));
		
		for (AssessmentEntry assessmentEntry : assessmentEntries) {
			String grade = null;
			String gradeSystemIdent = null;
			String performanceClassIdent = null;
			Boolean passed = null;
			AssessmentEvaluation currentEval = courseAssessmentService.toAssessmentEvaluation(assessmentEntry, assessmentConfig);
			if (gradeEnabled && assessmentConfig.hasGrade()) {
				if (assessmentConfig.isAutoGrade() || StringHelper.containsNonWhitespace(currentEval.getGrade())) {
					if (currentEval.getScore() != null) {
						GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, currentEval.getScore());
						grade = gradeScoreRange.getGrade();
						gradeSystemIdent = gradeScoreRange.getGradeSystemIdent();
						performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
						passed = gradeScoreRange.getPassed();
					}
				}
			} else {
				List<AssessmentTestSession> sessions = identityKeyToSessions.get(assessmentEntry.getIdentity().getKey());
				if (sessions != null && !sessions.isEmpty()) {
					Collections.sort(sessions, new AssessmentTestSessionComparator());
					passed = sessions.get(0).getPassed();
				}
			}
			
			boolean hasChanges = !Objects.equals(grade, assessmentEntry.getGrade())
					|| !Objects.equals(gradeSystemIdent, assessmentEntry.getGradeSystemIdent())
					|| !Objects.equals(performanceClassIdent, assessmentEntry.getPerformanceClassIdent())
					|| !Objects.equals(passed, assessmentEntry.getPassed());
			
			if (hasChanges
					|| assessmentEntry.getReferenceEntry() == null
					|| !testEntryKey.equals(assessmentEntry.getReferenceEntry().getKey())) {
				IdentityEnvironment ienv = new IdentityEnvironment(assessmentEntry.getIdentity(), Roles.userRoles());
				UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
				ScoreEvaluation scoreEval = new ScoreEvaluation(currentEval.getScore(), grade, gradeSystemIdent,
						performanceClassIdent, passed, currentEval.getAssessmentStatus(), currentEval.getUserVisible(),
						currentEval.getCurrentRunStartDate(), currentEval.getCurrentRunCompletion(),
						currentEval.getCurrentRunStatus(), currentEval.getAssessmentID());
				courseAssessmentService.updateScoreEvaluation(this, scoreEval, uce, publisher, false, Role.coach);
				
				DBFactory.getInstance().commitAndCloseSession();
			}
		}
	}

	private boolean isSessionWithPassed(AssessmentTestSession testSession) {
		return testSession.getIdentity() != null
				&& testSession.getPassed() != null
				&& !testSession.isAuthorMode()
				&& !testSession.isCancelled()
				&& !testSession.isExploded()
				&& (testSession.getFinishTime() != null || testSession.getTerminationTime() != null);
	}

	@Override
	public void archiveForResetUserData(UserCourseEnvironment assessedUserCourseEnv, ZipOutputStream archiveStream,
			String path, Identity doer, Role by) {
		
		RepositoryEntry re = getReferencedRepositoryEntry();
		if(re != null && ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())) {
			try {
				I18nManager i18nManager = CoreSpringFactory.getImpl(I18nManager.class);
				CourseEnvironment courseEnv = assessedUserCourseEnv.getCourseEnvironment();
				RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
				Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
				Locale locale = i18nManager.getLocaleOrDefault(assessedIdentity.getUser().getPreferences().getLanguage());

				List<Identity> assessedIdentitities = new ArrayList<>(1);
				assessedIdentitities.add(assessedIdentity);
				
				//1) create export resource
				new QTI21ResultsExport(courseEnv, assessedIdentitities, true, false, this, path, locale, doer, null)
					.exportTestResults(archiveStream);
				
				ArchiveOptions options = new ArchiveOptions();
				options.setIdentities(assessedIdentitities);
				QTI21StatisticSearchParams searchParams = new QTI21StatisticSearchParams(options, re, courseEntry, getIdent());
				QTI21ArchiveFormat qaf = new QTI21ArchiveFormat(locale, searchParams);
				qaf.exportCourseElement(archiveStream, path);
			} catch (IOException e) {
				log.error("", e);
			}
		}

		super.archiveForResetUserData(assessedUserCourseEnv, archiveStream, path, doer, by);
	}

	@Override
	public void resetUserData(UserCourseEnvironment assessedUserCourseEnv, Identity doer, Role by) {
		DB dbInstance = CoreSpringFactory.getImpl(DB.class);
		QTI21Service qti21Service = CoreSpringFactory.getImpl(QTI21Service.class);
		GradingService gradingService = CoreSpringFactory.getImpl(GradingService.class);
		
		Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<AssessmentTestSession> testSessions = qti21Service
				.getAssessmentTestSessions(courseEntry, getIdent(), assessedIdentity, false);
		
		for(AssessmentTestSession testSession:testSessions) {
			testSession.setCancelled(true);
			qti21Service.updateAssessmentTestSession(testSession);
			dbInstance.commit();
		}
		
		RepositoryEntry testEntry = getReferencedRepositoryEntry();
		if(gradingService.isGradingEnabled(testEntry, null)) {
			CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
			AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(this, assessedUserCourseEnv);
			GradingAssignment assignment = gradingService.getGradingAssignment(testEntry, assessmentEntry);
			if(assignment != null && (assignment.getAssignmentStatus() == GradingAssignmentStatus.assigned
					|| assignment.getAssignmentStatus() == GradingAssignmentStatus.inProcess
					|| assignment.getAssignmentStatus() == GradingAssignmentStatus.done)) {
				gradingService.deactivateAssignment(assignment);
			}
		}
		
		super.resetUserData(assessedUserCourseEnv, doer, by);
	}

	/**
	 * Update the module configuration to have all mandatory configuration flags
	 * set to usefull default values
	 * @param isNewNode true: an initial configuration is set; false: upgrading
	 *          from previous node configuration version, set default to maintain
	 *          previous behaviour
	 */
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType, Identity doer) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType, doer);
		
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			// add default module configuration
			config.set(IQEditController.CONFIG_KEY_ENABLEMENU, Boolean.TRUE);
			config.set(IQEditController.CONFIG_KEY_SEQUENCE, QTI21Constants.QMD_ENTRY_SEQUENCE_ITEM);
			config.set(IQEditController.CONFIG_KEY_TYPE, QTI21Constants.QMD_ENTRY_TYPE_ASSESS);
			config.set(IQEditController.CONFIG_KEY_SUMMARY, QTI21Constants.QMD_ENTRY_SUMMARY_COMPACT);
			config.set(IQEditController.CONFIG_KEY_ENABLESCOREINFO, Boolean.TRUE);
			config.set(IQEditController.CONFIG_KEY_CONFIG_REF, Boolean.TRUE);
			// chat
			config.set(IQEditController.CONFIG_KEY_IM_NOTIFICATIONS_ROLES, "coach");
			config.set(IQEditController.CONFIG_KEY_IM_PARTICIPANT_CAN_START, Boolean.FALSE);
		} else {
			int version = config.getConfigurationVersion();
			if (version < CURRENT_CONFIG_VERSION) {
				// Loaded config is older than current config version => migrate
				if (version == 1) {
					config.set(IQEditController.CONFIG_KEY_ENABLESCOREINFO, Boolean.TRUE);
				} else if (version <= 2) {
					if (config.get(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS) instanceof Boolean) {
						config.setStringValue(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS, String.valueOf(config.getBooleanEntry(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS)));
					}
				}
			}
		}
		
		config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
	}

	@Override
	public boolean hasAttemptsConfigured(RepositoryEntryRef courseEntry) {
		return new IQTESTAssessmentConfig(courseEntry, this).hasAttempts();
	}
	
	@Override
	public CourseNodeReminderProvider getReminderProvider(RepositoryEntryRef courseEntry, boolean rootNode) {
		return new AssessmentReminderProvider(getIdent(), new IQTESTAssessmentConfig(courseEntry, this));
	}
	
	@Override
	public List<Entry<String, DueDateConfig>> getNodeSpecificDatesWithLabel() {
		return IQDueDateConfig.getNodeSpecificDatesWithLabel(getModuleConfiguration());
	}

	@Override
	public DueDateConfig getDueDateConfig(String key) {
		DueDateConfig dueDateConfig = IQDueDateConfig.getDueDateConfig(getModuleConfiguration(), key);
		if (dueDateConfig != null) {
			return dueDateConfig;
		}
		return super.getDueDateConfig(key);
	}

	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course,
			ICourse sourceCrourse, CopyCourseContext context) {
		super.postCopy(envMapper, processType, course, sourceCrourse, context);
		IQDueDateConfig.postCopy(getIdent(), getModuleConfiguration(), context);
		
		if (context != null) {
			CopyType testCopyType = null;
			
			if (context.isCustomConfigsLoaded()) {
				CopyCourseOverviewRow nodeSettings = context.getCourseNodesMap().get(getIdent());
				
				if (nodeSettings != null) {
					testCopyType = nodeSettings.getResourceCopyType();
				}
			} else if (context.getTestCopyType() != null) {
				testCopyType = context.getTestCopyType();
			}
			
			if (testCopyType != null) {
				if (testCopyType.equals(CopyType.reference)) {
					// Reference the old test
					// Current default, do nothing
				} else if (testCopyType.equals(CopyType.copy)) {
					// Copy the test
					RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
					ModuleConfiguration sourceConfig = sourceCrourse.getEditorTreeModel().getCourseEditorNodeById(getIdent()).getCourseNode().getModuleConfiguration();
					RepositoryEntry source = IQEditController.getIQReference(sourceConfig, false);
					
					if (source != null) {
						Translator translator = Util.createPackageTranslator(RepositoryService.class, context.getExecutingLocale()); 
						
						RepositoryEntry copy = repositoryService.copy(source, context.getExecutingIdentity(), source.getDisplayname() + " " + translator.translate("copy.suffix"));
						IQEditController.setIQReference(copy, getModuleConfiguration());
					}
					
				} else if (testCopyType.equals(CopyType.ignore)) {
					// Remove the configured test
					IQEditController.removeIQReference(getModuleConfiguration());					
				}
			}
		}
	}
	
	@Override
	public void postImportCourseNodes(ICourse course, CourseNode sourceCourseNode, ICourse sourceCourse, ImportSettings settings, CourseEnvironmentMapper envMapper) {
		super.postImportCourseNodes(course, sourceCourseNode, sourceCourse, settings, envMapper);
		
		if(settings.getCopyType() == CopyType.copy) {
			VFSContainer sourceCourseFolderCont = sourceCourse.getCourseEnvironment()
					.getCourseFolderContainer(CourseContainerOptions.withoutElements());
			VFSContainer targetCourseFolderCont = course.getCourseEnvironment()
					.getCourseFolderContainer(CourseContainerOptions.withoutElements());

			String disclaimerFilePath = sourceCourseNode.getModuleConfiguration().getStringValue(IQEditController.CONFIG_KEY_DISCLAIMER);
			VFSLeaf sourceLeaf = (VFSLeaf)sourceCourseFolderCont.resolve(disclaimerFilePath);
			
			String targetRelPath = envMapper.getRenamedPathOrSource(disclaimerFilePath);
			VFSItem targetItem = targetCourseFolderCont.resolve(targetRelPath);
			if(targetItem == null && sourceLeaf.exists()) {
				// document is copied by the process before this step
				log.warn("Disclaimer page's file not copied: {}", targetRelPath);
			}
			if(StringHelper.containsNonWhitespace(targetRelPath)) {
				targetRelPath = VFSManager.appendLeadingSlash(targetRelPath);
				getModuleConfiguration().setStringValue(IQEditController.CONFIG_KEY_DISCLAIMER, targetRelPath);
			}
		}
	}
}