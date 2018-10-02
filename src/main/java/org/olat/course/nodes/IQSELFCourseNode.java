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
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.iq.CourseIQSecurityCallback;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.nodes.iq.IQRunController;
import org.olat.course.nodes.iq.QTI21AssessmentRunController;
import org.olat.course.nodes.iq.QTIResourceTypeModule;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.QTIResultSet;
import org.olat.ims.qti.export.QTIExportEssayItemFormatConfig;
import org.olat.ims.qti.export.QTIExportFIBItemFormatConfig;
import org.olat.ims.qti.export.QTIExportFormatterCSVType1;
import org.olat.ims.qti.export.QTIExportItemFormatConfig;
import org.olat.ims.qti.export.QTIExportItemFormatDelegate;
import org.olat.ims.qti.export.QTIExportKPRIMItemFormatConfig;
import org.olat.ims.qti.export.QTIExportMCQItemFormatConfig;
import org.olat.ims.qti.export.QTIExportManager;
import org.olat.ims.qti.export.QTIExportSCQItemFormatConfig;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.AssessmentTestSessionDAO;
import org.olat.ims.qti21.manager.archive.QTI21ArchiveFormat;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.modules.iq.IQManager;
import org.olat.modules.iq.IQSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

/**
 * Initial Date: Feb 9, 2004
 * 
 * @author Mike Stock
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class IQSELFCourseNode extends AbstractAccessableCourseNode implements SelfAssessableCourseNode, QTICourseNode {
	private static final long serialVersionUID = -1929987728611139729L;
	private static final OLog log = Tracing.createLoggerFor(IQSELFCourseNode.class);
	private static final String PACKAGE_IQ = Util.getPackageName(IQRunController.class);
	private static final String TYPE = "iqself";

	/**
	 * Constructor to create a course node of type IMS QTI.
	 */
	public IQSELFCourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createEditController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl, org.olat.course.ICourse)
	 */
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		TabbableController childTabCntrllr = new IQEditController(ureq, wControl, stackPanel, course, this, euce);
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
		
		Controller runController;
		ModuleConfiguration config = getModuleConfiguration();
		AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		boolean onyx = IQEditController.CONFIG_VALUE_QTI2.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI));
		if (onyx) {
			Translator trans = Util.createPackageTranslator(IQEditController.class, ureq.getLocale());
			runController = MessageUIFactory.createInfoMessage(ureq, wControl, "", trans.translate("error.onyx"));
		} else {
			RepositoryEntry testEntry = getReferencedRepositoryEntry();
			if(ImsQTI21Resource.TYPE_NAME.equals(testEntry.getOlatResource().getResourceableTypeName())) {
				runController = new QTI21AssessmentRunController(ureq, wControl, userCourseEnv, this);
			} else {
				IQSecurityCallback sec = new CourseIQSecurityCallback(this, am, ureq.getIdentity());
				runController = new IQRunController(userCourseEnv, getModuleConfiguration(), sec, ureq, wControl, this);
			}
		}
		
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, runController, this, "o_iqself_icon");
		return new NodeRunConstructionResult(ctrl);
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
			 * COnfiugre an IQxxx BB with a repo entry, do not publish this BB, mark
			 * IQxxx as deleted, remove repo entry, undelete BB IQxxx and bang you
			 * enter this if.
			 */
			Object repoEntry = IQEditController.getIQReference(getModuleConfiguration(), false);
			if (repoEntry == null) {
				isValid = false;
				IQEditController.removeIQReference(getModuleConfiguration());
				// FIXME:ms: may be show a refined error message, that the former
				// referenced repo entry is meanwhile deleted.
			}
		}
		StatusDescription sd = StatusDescription.NOERROR;
		if (!isValid) {
			String shortKey = "error.self.undefined.short";
			String longKey = "error.self.undefined.long";
			String[] params = new String[] { getShortTitle() };
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, PACKAGE_IQ);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(IQEditController.PANE_TAB_IQCONFIG_SELF);
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
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, PACKAGE_IQ, getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
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
	
	@Override
	public boolean hasAttemptsConfigured() {
		return false;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#informOnDelete(org.olat.core.gui.UserRequest,
	 *      org.olat.course.ICourse)
	 */
	@Override
	public String informOnDelete(Locale locale, ICourse course) {
		// Check if there are qtiresults for this selftest
		String repositorySoftKey = (String) getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		Long repKey = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
		if (QTIResultManager.getInstance().hasResultSets(course.getResourceableId(), this.getIdent(), repKey)) { return new PackageTranslator(
				PACKAGE_IQ, locale).translate("info.nodedelete"); }
		return null;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#cleanupOnDelete(org.olat.course.ICourse)
	 */
	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		// 1) Delete all qtiresults for this node. No properties used on this node
		String repositorySoftKey = (String) getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, false);
		if(re != null) {
			QTIResultManager.getInstance().deleteAllResults(course.getResourceableId(), getIdent(), re.getKey());
		}
		// 2) Delete all assessment test sessions (QTI 2.1)
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		CoreSpringFactory.getImpl(AssessmentTestSessionDAO.class).deleteAllUserTestSessionsByCourse(courseEntry, getIdent());
	}

	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options, ZipOutputStream exportStream, String charset) {
		String repositorySoftKey = (String) getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true);
		
		try {
			if(ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())) {
				RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
				QTI21StatisticSearchParams searchParams = new QTI21StatisticSearchParams(options, re, courseEntry, getIdent());
				QTI21ArchiveFormat qaf = new QTI21ArchiveFormat(locale, searchParams);
				qaf.exportCourseElement(exportStream);
				return true;	
			} else {
				QTIExportManager qem = QTIExportManager.getInstance();
				QTIExportFormatterCSVType1 qef = new QTIExportFormatterCSVType1(locale, "\t", "\"", "\r\n", false);
				qef.setAnonymous(true);
				if (options != null && options.getExportFormat() != null) {
					Map<Class<?>, QTIExportItemFormatConfig> itemConfigs = new HashMap<>();
					Class<?>[] itemTypes = new Class<?>[] {QTIExportSCQItemFormatConfig.class, QTIExportMCQItemFormatConfig.class,
						QTIExportKPRIMItemFormatConfig.class, QTIExportFIBItemFormatConfig.class, QTIExportEssayItemFormatConfig.class};
					for (Class<?> itemClass : itemTypes) {
						itemConfigs.put(itemClass, new QTIExportItemFormatDelegate(options.getExportFormat()));						
					}
					qef.setMapWithExportItemConfigs(itemConfigs);
				}
				return qem.selectAndExportResults(qef, course.getResourceableId(), getShortTitle(), getIdent(), re, exportStream, locale, ".xls");
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
		// self healing
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, false);
		if (re == null) {
			// nothing to export, but correct the module configuration
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
				RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(TestFileResource.TYPE_NAME);
				re = handler.importResource(owner, rie.getInitialAuthor(), rie.getDisplayName(),
					rie.getDescription(), false, organisation, locale, file, null);
			}
			IQEditController.setIQReference(re, getModuleConfiguration());
		} else {
			IQEditController.removeIQReference(getModuleConfiguration());
		}
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
			config.set(IQEditController.CONFIG_KEY_TYPE, AssessmentInstance.QMD_ENTRY_TYPE_SELF);
			config.set(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_DETAILED);
			config.set(IQEditController.CONFIG_KEY_CONFIG_REF, Boolean.TRUE);
		}
	}
	
	/**
	 * 
	 * @see org.olat.course.nodes.SelfAssessableCourseNode#getUserScoreEvaluation(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public ScoreEvaluation getUserScoreEvaluation(final UserCourseEnvironment userCourseEnv) {
		// read score from properties save score, passed and attempts information
		ScoreEvaluation scoreEvaluation = null;
		RepositoryEntry referencedRepositoryEntry = getReferencedRepositoryEntry();
		if (referencedRepositoryEntry != null && QTIResourceTypeModule.isOnyxTest(getReferencedRepositoryEntry().getOlatResource())) {
			AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
			Identity mySelf = userCourseEnv.getIdentityEnvironment().getIdentity();
			Boolean passed = am.getNodePassed(this, mySelf);
			Float score = am.getNodeScore(this, mySelf);
			Long assessmentID = am.getAssessmentID(this, mySelf);
			// <OLATCE-374>
			Boolean fullyAssessed = am.getNodeFullyAssessed(this, mySelf);
			scoreEvaluation = new ScoreEvaluation(score, passed, fullyAssessed, assessmentID);
			// </OLATCE-374>
		} else if(referencedRepositoryEntry != null && ImsQTI21Resource.TYPE_NAME.equals(referencedRepositoryEntry.getOlatResource().getResourceableTypeName())) {
			RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			Identity assessedIdentity = userCourseEnv.getIdentityEnvironment().getIdentity();
			AssessmentTestSession testSession = CoreSpringFactory.getImpl(QTI21Service.class)
					.getLastAssessmentTestSessions(courseEntry, getIdent(), referencedRepositoryEntry, assessedIdentity);
			if(testSession != null) {
				boolean fullyAssessed = (testSession.getFinishTime() != null || testSession.getTerminationTime() != null);
				Float score = testSession.getScore().floatValue();
				return new ScoreEvaluation(score, testSession.getPassed(), fullyAssessed, testSession.getKey());
			}
		} else {
			Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
			long olatResourceId = userCourseEnv.getCourseEnvironment().getCourseResourceableId().longValue();
			QTIResultSet qTIResultSet = CoreSpringFactory.getImpl(IQManager.class).getLastResultSet(identity, olatResourceId, this.getIdent());
			if (qTIResultSet != null) {
				Boolean passed = qTIResultSet.getIsPassed();
				Boolean fullyAssessed = qTIResultSet.getFullyAssessed();
				scoreEvaluation = new ScoreEvaluation(Float.valueOf(qTIResultSet.getScore()), passed, fullyAssessed, new Long(qTIResultSet.getAssessmentID()));
			}
		}
		return scoreEvaluation;
	}
	
	@Override
	public Integer getUserAttempts(UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		return am.getNodeAttempts(this, mySelf);
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

}
