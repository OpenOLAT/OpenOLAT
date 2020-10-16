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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.learningpath.ui.TabbableLeaningPathNodeConfigController;
import org.olat.course.nodes.iq.CourseIQSecurityCallback;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.nodes.iq.IQPreviewController;
import org.olat.course.nodes.iq.IQRunController;
import org.olat.course.nodes.iq.IQTESTAssessmentConfig;
import org.olat.course.nodes.iq.IQTESTLearningPathNodeHandler;
import org.olat.course.nodes.iq.QTI21AssessmentRunController;
import org.olat.course.nodes.iq.QTIResourceTypeModule;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.statistic.StatisticResourceOption;
import org.olat.course.statistic.StatisticResourceResult;
import org.olat.course.statistic.StatisticType;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti.QTIModule;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.export.QTIExportEssayItemFormatConfig;
import org.olat.ims.qti.export.QTIExportFIBItemFormatConfig;
import org.olat.ims.qti.export.QTIExportFormatter;
import org.olat.ims.qti.export.QTIExportFormatterCSVType1;
import org.olat.ims.qti.export.QTIExportItemFormatConfig;
import org.olat.ims.qti.export.QTIExportItemFormatDelegate;
import org.olat.ims.qti.export.QTIExportKPRIMItemFormatConfig;
import org.olat.ims.qti.export.QTIExportMCQItemFormatConfig;
import org.olat.ims.qti.export.QTIExportManager;
import org.olat.ims.qti.export.QTIExportSCQItemFormatConfig;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.FilePersister;
import org.olat.ims.qti.resultexport.QTI12ResultsExportMediaResource;
import org.olat.ims.qti.statistics.QTIStatisticResourceResult;
import org.olat.ims.qti.statistics.QTIStatisticSearchParams;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.AssessmentTestSessionDAO;
import org.olat.ims.qti21.manager.archive.QTI21ArchiveFormat;
import org.olat.ims.qti21.model.DigitalSignatureOptions;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.resultexport.QTI21ResultsExportMediaResource;
import org.olat.ims.qti21.ui.statistics.QTI21StatisticResourceResult;
import org.olat.ims.qti21.ui.statistics.QTI21StatisticsSecurityCallback;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.grading.GradingService;
import org.olat.modules.iq.IQSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
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
		this(null);
	}

	public IQTESTCourseNode(INode parent) {
		super(TYPE, parent);
	}
	
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		TabbableController childTabCntrllr = new IQEditController(ureq, wControl, stackPanel, course, this, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, course, chosenNode, euce, childTabCntrllr);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		Controller controller;
		// Do not allow guests to start tests
		Roles roles = ureq.getUserSession().getRoles();
		Translator trans = Util.createPackageTranslator(IQTESTCourseNode.class, ureq.getLocale());
		if (roles.isGuestOnly()) {
			if(isGuestAllowedForQTI21(getReferencedRepositoryEntry())) {
				controller = new QTI21AssessmentRunController(ureq, wControl, userCourseEnv, this);
			} else {
				String title = trans.translate("guestnoaccess.title");
				String message = trans.translate("guestnoaccess.message");
				controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
			}
		} else {
			RepositoryEntry testEntry = getReferencedRepositoryEntry();
			OLATResource ores = testEntry.getOlatResource();
			if(ImsQTI21Resource.TYPE_NAME.equals(ores.getResourceableTypeName())) {
				//QTI 2.1
				controller = new QTI21AssessmentRunController(ureq, wControl, userCourseEnv, this);
			} else if(QTIResourceTypeModule.isOnyxTest(ores)) {
				Translator transe = Util.createPackageTranslator(IQEditController.class, ureq.getLocale());
				controller = MessageUIFactory.createInfoMessage(ureq, wControl, "", transe.translate("error.onyx"));
			} else {
				//QTI 1.2
				QTIModule qtiModule = CoreSpringFactory.getImpl(QTIModule.class);
				if (qtiModule.isRunEnabled()) {
					TestFileResource fr = new TestFileResource();
					fr.overrideResourceableId(ores.getResourceableId());
					if(!CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(fr, null)) {
						AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
						IQSecurityCallback sec = new CourseIQSecurityCallback(this, am, ureq.getIdentity());
						controller = new IQRunController(userCourseEnv, getModuleConfiguration(), sec, ureq, wControl, this, testEntry);
					} else {
						String title = trans.translate("editor.lock.title");
						String message = trans.translate("editor.lock.message");
						controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
					}
				} else {
					Translator transe = Util.createPackageTranslator(IQEditController.class, ureq.getLocale());
					controller = MessageUIFactory.createInfoMessage(ureq, wControl, "", transe.translate("error.qti12"));
				}
			}
		}
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, "o_iqtest_icon");
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
	 * @return true if the course node or the test has a time limit set.
	 */
	public boolean hasQTI21TimeLimit(RepositoryEntry testEntry) {
		boolean timeLimit = false;
		if(ImsQTI21Resource.TYPE_NAME.equals(testEntry.getOlatResource().getResourceableTypeName())) {
			ModuleConfiguration config = getModuleConfiguration();
			boolean configRef = config.getBooleanSafe(IQEditController.CONFIG_KEY_CONFIG_REF, false);
			if((!configRef && config.getIntegerSafe(IQEditController.CONFIG_KEY_TIME_LIMIT, -1) > 0)
					|| config.getDateValue(IQEditController.CONFIG_KEY_END_TEST_DATE) != null) {
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
		
		File unzippedDirRoot = FileResourceManager.getInstance().unzipFileResource(testEntry.getOlatResource());
		ResolvedAssessmentTest resolvedAssessmentTest = CoreSpringFactory.getImpl(QTI21Service.class)
				.loadAndResolveAssessmentTest(unzippedDirRoot, false, false);
		if(resolvedAssessmentTest != null) {
			return resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
		}
		return null;
	}

	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		Controller controller;
		ModuleConfiguration config = getModuleConfiguration();
		boolean onyx = IQEditController.CONFIG_VALUE_QTI2.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI));
		if (onyx) {
			Translator trans = Util.createPackageTranslator(IQEditController.class, ureq.getLocale());
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, "", trans.translate("error.onyx"));
		} else {
			controller = new IQPreviewController(ureq, wControl, userCourseEnv, this);
		}
		return controller;
	}

	public boolean isQTI12TestRunning(Identity assessedIdentity, CourseEnvironment courseEnv) {
		String resourcePath = courseEnv.getCourseResourceableId() + File.separator + getIdent();
		FilePersister qtiPersister = new FilePersister(assessedIdentity, resourcePath);
		return qtiPersister.exists();
	}

	@Override
	public StatisticResourceResult createStatisticNodeResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, StatisticResourceOption options, StatisticType type) {
		if(!isStatisticTypeAllowed(type)) return null;
		
		Long courseId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
		OLATResourceable courseOres = OresHelper.createOLATResourceableInstance("CourseModule", courseId);
		
		RepositoryEntry qtiTestEntry = getReferencedRepositoryEntry();
		if(ImsQTI21Resource.TYPE_NAME.equals(qtiTestEntry.getOlatResource().getResourceableTypeName())) {
			RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			QTI21StatisticSearchParams searchParams = new QTI21StatisticSearchParams(qtiTestEntry, courseEntry, getIdent());
			boolean admin = userCourseEnv.isAdmin();
			if(options.getParticipantsGroups() != null) {
				searchParams.setLimitToGroups(options.getParticipantsGroups());
			}
			QTI21StatisticsSecurityCallback secCallback = new QTI21StatisticsSecurityCallback(admin, admin && isGuestAllowedForQTI21(qtiTestEntry));
			return new QTI21StatisticResourceResult(qtiTestEntry, courseEntry, this, searchParams, secCallback);
		}
		
		QTIStatisticSearchParams searchParams = new QTIStatisticSearchParams(courseOres.getResourceableId(), getIdent());
		searchParams.setLimitToGroups(options.getParticipantsGroups());
		return new QTIStatisticResourceResult(courseOres, this, qtiTestEntry, searchParams);
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
		
		List<StatusDescription> statusDescs = validateInternalConfiguration();
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
		sds.addAll(validateInternalConfiguration());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}

	private List<StatusDescription> validateInternalConfiguration() {
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
		
		if (isFullyAssessedScoreConfigError()) {
			addStatusErrorDescription("error.fully.assessed.score", "error.fully.assessed.score",
					TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
		}
		if (isFullyAssessedPassedConfigError()) {
			addStatusErrorDescription("error.fully.assessed.passed", "error.fully.assessed.passed",
					TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
		}
		
		return sdList;
	}
	
	private boolean isFullyAssessedScoreConfigError() {
		boolean hasScore = Mode.none != new IQTESTAssessmentConfig(this).getScoreMode();
		boolean isScoreTrigger = CoreSpringFactory.getImpl(IQTESTLearningPathNodeHandler.class)
				.getConfigs(this)
				.isFullyAssessedOnScore(null, null)
				.isEnabled();
		return isScoreTrigger && !hasScore;
	}
	
	private boolean isFullyAssessedPassedConfigError() {
		boolean hasPassed = new IQTESTAssessmentConfig(this).getPassedMode() != Mode.none;
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

	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		
		CoursePropertyManager pm = course.getCourseEnvironment().getCoursePropertyManager();
		// 1) Delete all properties: score, passed, log, comment, coach_comment,
		// attempts
		pm.deleteNodeProperties(this, null);
		// 2) Delete all qtiresults for this node (QTI 1.2 + qtiworks)
		String repositorySoftKey = (String) getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, false);
		if(re != null) {
			QTIResultManager.getInstance().deleteAllResults(course.getResourceableId(), getIdent(), re.getKey());
		}
		// 3) Delete all assessment test sessions (QTI 2.1)
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		CoreSpringFactory.getImpl(AssessmentTestSessionDAO.class).deleteAllUserTestSessionsByCourse(courseEntry, getIdent());
	}

	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options,
			ZipOutputStream exportStream, String archivePath, String charset) {
		
		String repositorySoftKey = (String)getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		Long courseResourceableId = course.getResourceableId();

		// 1) prepare result export
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		try {
			RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, false);
			if(re == null) {
				log.error("Cannot archive course node. Missing repository entry with soft key: {}", repositorySoftKey);
				return false;
			}
			
			boolean onyx = QTIResourceTypeModule.isOnyxTest(re.getOlatResource());
			if (onyx) {
				return true;
			} else if(ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())) {
				// 2a) create export resource
				List<Identity> identities = ScoreAccountingHelper.loadUsers(courseEnv, options);
				new QTI21ResultsExportMediaResource(courseEnv, identities, this, archivePath, locale).exportTestResults(exportStream);
				// excel results
				RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
				QTI21StatisticSearchParams searchParams = new QTI21StatisticSearchParams(options, re, courseEntry, getIdent());
				QTI21ArchiveFormat qaf = new QTI21ArchiveFormat(locale, searchParams);
				qaf.exportCourseElement(exportStream, archivePath);
				return true;	
			} else {
				// 2b) create export resource
				List<Identity> identities = ScoreAccountingHelper.loadUsers(courseEnv, options);
				new QTI12ResultsExportMediaResource(courseEnv, identities, this, archivePath, locale).exportTestResults(exportStream);
				// excel results
				String shortTitle = getShortTitle();
				QTIExportManager qem = QTIExportManager.getInstance();
				QTIExportFormatter qef = new QTIExportFormatterCSVType1(locale, "\t", "\"", "\r\n", false);
				if (options != null && options.getExportFormat() != null) {
					Map<Class<?>, QTIExportItemFormatConfig> itemConfigs = new HashMap<>();
					Class<?>[] itemTypes = new Class<?>[] {QTIExportSCQItemFormatConfig.class, QTIExportMCQItemFormatConfig.class,
						QTIExportKPRIMItemFormatConfig.class, QTIExportFIBItemFormatConfig.class, QTIExportEssayItemFormatConfig.class};
					for (Class<?> itemClass : itemTypes) {
						itemConfigs.put(itemClass, new QTIExportItemFormatDelegate(options.getExportFormat()));						
					}
					qef.setMapWithExportItemConfigs(itemConfigs);
				}
				return qem.selectAndExportResults(qef, courseResourceableId, shortTitle, getIdent(), re, exportStream, archivePath, locale, ".xls");
			}
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

			RepositoryEntry re;
			if(handlerQTI21.acceptImport(file, "repo.zip").isValid()) {
				re = handlerQTI21.importResource(owner, rie.getInitialAuthor(), rie.getDisplayName(),
						rie.getDescription(), false, organisation, locale, rie.importGetExportedFile(), null);

				getModuleConfiguration().set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI21);
			} else {
				RepositoryHandler handlerQTI = RepositoryHandlerFactory.getInstance().getRepositoryHandler(TestFileResource.TYPE_NAME);
				re = handlerQTI.importResource(owner, rie.getInitialAuthor(), rie.getDisplayName(),
						rie.getDescription(), false, organisation, locale, rie.importGetExportedFile(), null);
			}
			IQEditController.setIQReference(re, getModuleConfiguration());
		} else {
			IQEditController.removeIQReference(getModuleConfiguration());
		}
	}

	public void pullAssessmentTestSession(AssessmentTestSession session, UserCourseEnvironment assessedUserCourseEnv, Identity coachingIdentity, Role by) {
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
		ScoreEvaluation sceval = new ScoreEvaluation(session.getScore().floatValue(), session.getPassed(), assessmentStatus, visibility,
				null, 1.0d, AssessmentRunStatus.done, session.getKey());
		courseAssessmentService.updateScoreEvaluation(this, sceval, assessedUserCourseEnv, coachingIdentity, true, by);
		
		if(IQEditController.CORRECTION_GRADING.equals(correctionMode)) {
			AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(this, assessedUserCourseEnv);
			RepositoryEntry testEntry = IQEditController.getIQReference(getModuleConfiguration(), false);
			CoreSpringFactory.getImpl(GradingService.class).assignGrader(testEntry, assessmentEntry, session.getFinishTime(), true);
		}
	}
	
	public void promoteAssessmentTestSession(AssessmentTestSession testSession, UserCourseEnvironment assessedUserCourseEnv,
			boolean updateScoring, Identity coachingIdentity, Role by) {
		
		Float score = null;
		Boolean passed = null;
		if(updateScoring) {
			AssessmentTest assessmentTest = loadAssessmentTest(testSession.getTestEntry());
			Double cutValue = QtiNodesExtractor.extractCutValue(assessmentTest);
	
			BigDecimal finalScore = testSession.getFinalScore();
			score = finalScore == null ? null : finalScore.floatValue();
			passed = testSession.getPassed();
			if(testSession.getManualScore() != null && finalScore != null && cutValue != null) {
				boolean calculated = finalScore.compareTo(BigDecimal.valueOf(cutValue.doubleValue())) >= 0;
				passed = Boolean.valueOf(calculated);
			}
		}

		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		ScoreEvaluation sceval = new ScoreEvaluation(score, passed, null, null, null, 1.0d, AssessmentRunStatus.done, testSession.getKey());
		courseAssessmentService.updateScoreEvaluation(this, sceval, assessedUserCourseEnv, coachingIdentity, true, by);
	}

	/**
	 * Update the module configuration to have all mandatory configuration flags
	 * set to usefull default values
	 * @param isNewNode true: an initial configuration is set; false: upgrading
	 *          from previous node configuration version, set default to maintain
	 *          previous behaviour
	 */
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent) {
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			// add default module configuration
			config.set(IQEditController.CONFIG_KEY_ENABLEMENU, Boolean.TRUE);
			config.set(IQEditController.CONFIG_KEY_SEQUENCE, AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM);
			config.set(IQEditController.CONFIG_KEY_TYPE, AssessmentInstance.QMD_ENTRY_TYPE_ASSESS);
			config.set(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_COMPACT);
			config.set(IQEditController.CONFIG_KEY_ENABLESCOREINFO, Boolean.TRUE);
			config.set(IQEditController.CONFIG_KEY_CONFIG_REF, Boolean.TRUE);
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
	public boolean hasAttemptsConfigured() {
		return new IQTESTAssessmentConfig(this).hasAttempts();
	}

}