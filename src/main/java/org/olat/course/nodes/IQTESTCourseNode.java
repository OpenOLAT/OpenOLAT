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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.logging.KnownIssueException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.ICourse;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeController;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.iq.CourseIQSecurityCallback;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.nodes.iq.IQIdentityListCourseNodeController;
import org.olat.course.nodes.iq.IQPreviewController;
import org.olat.course.nodes.iq.IQRunController;
import org.olat.course.nodes.iq.QTI21AssessmentRunController;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.statistic.StatisticResourceOption;
import org.olat.course.statistic.StatisticResourceResult;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.group.BusinessGroup;
import org.olat.ims.qti.QTI12ResultDetailsController;
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
import org.olat.ims.qti.statistics.QTIType;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.AssessmentTestSessionDAO;
import org.olat.ims.qti21.manager.archive.QTI21ArchiveFormat;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.resultexport.QTI21ResultsExportMediaResource;
import org.olat.ims.qti21.ui.QTI21AssessmentDetailsController;
import org.olat.ims.qti21.ui.statistics.QTI21StatisticResourceResult;
import org.olat.ims.qti21.ui.statistics.QTI21StatisticsSecurityCallback;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.iq.IQSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.OLATResource;

import de.bps.onyx.plugin.OnyxModule;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * Initial Date: Feb 9, 2004
 * @author Mike Stock Comment:
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class IQTESTCourseNode extends AbstractAccessableCourseNode implements PersistentAssessableCourseNode, QTICourseNode {
	private static final long serialVersionUID = 5806292895738005387L;
	private static final OLog log = Tracing.createLoggerFor(IQTESTCourseNode.class);
	private static final String translatorStr = Util.getPackageName(IQEditController.class);
	private static final String TYPE = "iqtest";

	private static final int CURRENT_CONFIG_VERSION = 2;

	public IQTESTCourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
	}
	
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		updateModuleConfigDefaults(false);
		TabbableController childTabCntrllr = new IQEditController(ureq, wControl, stackPanel, course, this, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, euce, childTabCntrllr);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		updateModuleConfigDefaults(false);		
		
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
			ModuleConfiguration config = getModuleConfiguration();
			boolean onyx = IQEditController.CONFIG_VALUE_QTI2.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI));
			if (onyx) {
				Translator transe = Util.createPackageTranslator(IQEditController.class, ureq.getLocale());
				controller = MessageUIFactory.createInfoMessage(ureq, wControl, "", transe.translate("error.onyx"));
			} else {
				RepositoryEntry testEntry = getReferencedRepositoryEntry();
				OLATResource ores = testEntry.getOlatResource();
				if(ImsQTI21Resource.TYPE_NAME.equals(ores.getResourceableTypeName())) {
					//QTI 2.1
					controller = new QTI21AssessmentRunController(ureq, wControl, userCourseEnv, this);
				} else {
					//QTI 1.2
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
			if(!configRef && config.getIntegerSafe(IQEditController.CONFIG_KEY_TIME_LIMIT, -1) > 0) {
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
	
	/**
	 * 
	 * @return
	 */
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

	/**
	 * @see org.olat.course.nodes.CourseNode#createPreviewController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.course.run.userview.NodeEvaluation)
	 */
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
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
	
	@Override
	public AssessmentCourseNodeController getIdentityListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, BusinessGroup group, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback) {
		return new IQIdentityListCourseNodeController(ureq, wControl, stackPanel,
				courseEntry, group, this, coachCourseEnv, toolContainer, assessmentCallback);
	}

	public boolean isQTI12TestRunning(Identity assessedIdentity, CourseEnvironment courseEnv) {
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
	public boolean isStatisticNodeResultAvailable(UserCourseEnvironment userCourseEnv, QTIType... types) {
		return isQTITypeAllowed(types);
	}
	
	private boolean isQTITypeAllowed(QTIType... types) {
		if(types == null) return true;
		if(types.length == 0 || (types.length == 1 && types[0] == null)) return true;
		
		for(QTIType type:types) {
			if(QTIType.test.equals(type) || QTIType.onyx.equals(type) || QTIType.qtiworks.equals(type)) {
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
			String[] params = new String[] { getShortTitle() };
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translatorStr);
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
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getUserScoreEvaluation(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public AssessmentEvaluation getUserScoreEvaluation(UserCourseEnvironment userCourseEnv) {
		// read score from properties save score, passed and attempts information
		AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnv.getIdentityEnvironment().getIdentity();
		AssessmentEntry entry = am.getAssessmentEntry(this, mySelf);
		return getUserScoreEvaluation(entry);
	}

	@Override
	public AssessmentEvaluation getUserScoreEvaluation(AssessmentEntry entry) {
		if(entry != null) {
			return AssessmentEvaluation.toAssessmentEvalutation(entry, this);
		}	
		return AssessmentEvaluation.EMPTY_EVAL;
	}

	@Override
	public AssessmentEntry getUserAssessmentEntry(UserCourseEnvironment userCourseEnv) {
		AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnv.getIdentityEnvironment().getIdentity();
		if(getRepositoryEntrySoftKey() != null) {
			return am.getAssessmentEntry(this, mySelf);
		}
		return null;
	}
	
	@Override
	public boolean isAssessedBusinessGroups() {
		return false;
	}

	@Override
	public Float getCutValueConfiguration() {
		Float cutValue = null;
		
		ModuleConfiguration config = getModuleConfiguration();
		// for onyx and QTI 1.2
		if (IQEditController.CONFIG_VALUE_QTI2.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI))
				|| IQEditController.CONFIG_VALUE_QTI1.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI))) {
			cutValue = (Float) config.get(IQEditController.CONFIG_KEY_CUTVALUE);
		} else {
			RepositoryEntry testEntry = getReferencedRepositoryEntry();
			AssessmentTest assessmentTest = loadAssessmentTest(testEntry);
			if(assessmentTest != null) {
				Double cut = QtiNodesExtractor.extractCutValue(assessmentTest);
				if(cut != null) {
					cutValue = Float.valueOf(cut.floatValue());
				}
			}
		}
		return cutValue;
	}

	@Override
	public Float getMaxScoreConfiguration() {
		Float maxScore = null;

		ModuleConfiguration config = getModuleConfiguration();
		// for onyx and QTI 1.2
		if (IQEditController.CONFIG_VALUE_QTI2.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI))
				|| IQEditController.CONFIG_VALUE_QTI1.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI))) {
			maxScore = (Float) config.get(IQEditController.CONFIG_KEY_MAXSCORE);
		} else {
			RepositoryEntry testEntry = getReferencedRepositoryEntry();
			AssessmentTest assessmentTest = loadAssessmentTest(testEntry);
			if(assessmentTest != null) {
				Double max = QtiNodesExtractor.extractMaxScore(assessmentTest);
				if(max != null) {
					maxScore = Float.valueOf(max.floatValue());
				}
			}
		}
		
		return maxScore;
	}

	@Override
	public Float getMinScoreConfiguration() {
		Float minScore = null;
		ModuleConfiguration config = getModuleConfiguration();
		// for onyx and QTI 1.2
		if (IQEditController.CONFIG_VALUE_QTI2.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI))
				|| IQEditController.CONFIG_VALUE_QTI1.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI))) {
			minScore = (Float) config.get(IQEditController.CONFIG_KEY_MINSCORE);
		} else {
			RepositoryEntry testEntry = getReferencedRepositoryEntry();
			AssessmentTest assessmentTest = loadAssessmentTest(testEntry);
			if(assessmentTest != null) {
				Double min = QtiNodesExtractor.extractMinScore(assessmentTest);
				if(min != null) {
					minScore = Float.valueOf(min.floatValue());
				}
			}
		}
		return minScore;
	}

	@Override
	public boolean hasCommentConfigured() {
		// coach should be able to add comments here, visible to users
		return true;
	}

	@Override
	public boolean hasIndividualAsssessmentDocuments() {
		return true;// like user comment
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
			Identity coachingIdentity, boolean incrementAttempts, Role by) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		try {
			am.saveScoreEvaluation(this, coachingIdentity, mySelf, scoreEvaluation, userCourseEnvironment, incrementAttempts, by);
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

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getUserCoachComment(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public String getUserCoachComment(UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		return am.getNodeCoachComment(this, mySelf);
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getUserUserComment(org.olat.course.run.userview.UserCourseEnvironment)
	 */
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
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		return am.getUserNodeLog(this, mySelf);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#getReferencedRepositoryEntry()
	 */
	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		// ",false" because we do not want to be strict, but just indicate whether
		// the reference still exists or not
		return IQEditController.getIQReference(getModuleConfiguration(), false);
	}
	
	private String getRepositoryEntrySoftKey() {
		return (String)getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
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
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options, ZipOutputStream exportStream, String charset) {
		String repositorySoftKey = (String)getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		Long courseResourceableId = course.getResourceableId();

		// 1) prepare result export
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		try {
			RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true);
			boolean onyx = OnyxModule.isOnyxTest(re.getOlatResource());
			if (onyx) {
				return true;
			} else if(ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())) {
				// 2a) create export resource
				List<Identity> identities = ScoreAccountingHelper.loadUsers(courseEnv, options);
				new QTI21ResultsExportMediaResource(courseEnv, identities, this, locale).exportTestResults(exportStream);
				// excel results
				RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
				QTI21StatisticSearchParams searchParams = new QTI21StatisticSearchParams(options, re, courseEntry, getIdent());
				QTI21ArchiveFormat qaf = new QTI21ArchiveFormat(locale, searchParams);
				qaf.exportCourseElement(exportStream);
				return true;	
			} else {
				// 2b) create export resource
				List<Identity> identities = ScoreAccountingHelper.loadUsers(courseEnv, options);
				new QTI12ResultsExportMediaResource(courseEnv, locale, identities, this).exportTestResults(exportStream);
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
				return qem.selectAndExportResults(qef, courseResourceableId, shortTitle, getIdent(), re, exportStream, locale, ".xls");
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

	@Override
	public Integer getUserAttempts(UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		return am.getNodeAttempts(this, mySelf);
	}

	@Override
	public boolean hasAttemptsConfigured() {
		return true;
	}

	@Override
	public void updateUserAttempts(Integer userAttempts, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity, Role by) {
		if (userAttempts != null) {
			AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
			Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
			am.saveNodeAttempts(this, coachingIdentity, mySelf, userAttempts, by);
		}
	}
	
	public void pullAssessmentTestSession(AssessmentTestSession session, UserCourseEnvironment assessedUserCourseenv, Identity coachingIdentity, Role by) {
		Boolean visibility;
		AssessmentEntryStatus assessmentStatus;
		if(IQEditController.CORRECTION_MANUAL.equals(getModuleConfiguration().getStringValue(IQEditController.CONFIG_CORRECTION_MODE))) {
			assessmentStatus = AssessmentEntryStatus.inReview;
			visibility = Boolean.FALSE;
		} else {
			assessmentStatus = AssessmentEntryStatus.done;
			visibility = Boolean.TRUE;
		}
		ScoreEvaluation sceval = new ScoreEvaluation(session.getScore().floatValue(), session.getPassed(), assessmentStatus, visibility, Boolean.TRUE,
				1.0d, AssessmentRunStatus.done, session.getKey());
		updateUserScoreEvaluation(sceval, assessedUserCourseenv, coachingIdentity, true, by);
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#incrementUserAttempts(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public void incrementUserAttempts(UserCourseEnvironment userCourseEnvironment, Role by) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		am.incrementNodeAttempts(this, mySelf, userCourseEnvironment, by);
	}
	
	@Override
	public boolean hasCompletion() {
		return IQEditController.CONFIG_VALUE_QTI21.equals(getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE_QTI));
	}

	@Override
	public Double getUserCurrentRunCompletion(UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		return am.getNodeCurrentRunCompletion(this, mySelf);
	}
	
	@Override
	public void updateCurrentCompletion(UserCourseEnvironment userCourseEnvironment, Identity identity,
			Double currentCompletion, AssessmentRunStatus runStatus, Role doneBy) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		am.updateCurrentCompletion(this, assessedIdentity, userCourseEnvironment, currentCompletion, runStatus, doneBy);
	}
	
	@Override
	public void updateLastModifications(UserCourseEnvironment userCourseEnvironment, Identity identity, Role by) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		am.updateLastModifications(this, assessedIdentity, userCourseEnvironment, by);
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getDetailsEditController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public Controller getDetailsEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnv) {
		Controller detailsCtrl = null;
		RepositoryEntry ref = getReferencedRepositoryEntry();
		if(ref != null) {
			OLATResource resource = ref.getOlatResource();
			Long courseResourceableId = assessedUserCourseEnv.getCourseEnvironment().getCourseResourceableId();
			Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
			
			if(ImsQTI21Resource.TYPE_NAME.equals(resource.getResourceableTypeName())) {
				RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
				detailsCtrl = new QTI21AssessmentDetailsController(ureq, wControl, (TooledStackedPanel)stackPanel, courseEntry, this, coachCourseEnv, assessedUserCourseEnv);
			} else if(OnyxModule.isOnyxTest(ref.getOlatResource())) {
				Translator trans = Util.createPackageTranslator(IQEditController.class, ureq.getLocale());
				detailsCtrl = MessageUIFactory.createInfoMessage(ureq, wControl, "", trans.translate("error.onyx"));
			} else {
				detailsCtrl = new QTI12ResultDetailsController(ureq, wControl, courseResourceableId, getIdent(), coachCourseEnv, assessedIdentity, ref, AssessmentInstance.QMD_ENTRY_TYPE_ASSESS);
			}	
		}
		return detailsCtrl;
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
					// migrate V1 => V2, new parameter 'enableScoreInfo'
					version = 2;
					config.set(IQEditController.CONFIG_KEY_ENABLESCOREINFO, new Boolean(true));
				}
				config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
			}
		}
	}

}