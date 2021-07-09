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
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.nodes.iq.QTI21AssessmentRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.AssessmentTestSessionDAO;
import org.olat.ims.qti21.manager.archive.QTI21ArchiveFormat;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.OLATResource;

/**
 * Initial Date: Feb 9, 2004
 * 
 * @author Mike Stock
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class IQSELFCourseNode extends AbstractAccessableCourseNode implements SelfAssessableCourseNode, QTICourseNode {
	private static final long serialVersionUID = -1929987728611139729L;
	private static final Logger log = Tracing.createLoggerFor(IQSELFCourseNode.class);
	private static final String PACKAGE_IQ = Util.getPackageName(QTI21AssessmentRunController.class);
	public static final String TYPE = "iqself";
	
	private static final int CURRENT_CONFIG_VERSION = 3;

	public IQSELFCourseNode() {
		this(null);
	}

	public IQSELFCourseNode(INode parent) {
		super(TYPE, parent);
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
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		
		Controller runController;
		RepositoryEntry testEntry = getReferencedRepositoryEntry();
		OLATResource ores = testEntry.getOlatResource();
		if(ImsQTI21Resource.TYPE_NAME.equals(ores.getResourceableTypeName())) {
			runController = new QTI21AssessmentRunController(ureq, wControl, userCourseEnv, this);
		} else {
			Translator trans = Util.createPackageTranslator(IQEditController.class, ureq.getLocale());
			runController = MessageUIFactory.createInfoMessage(ureq, wControl, "", trans.translate("error.qti12"));
		}
		
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, runController, userCourseEnv, this, "o_iqself_icon");
		return new NodeRunConstructionResult(ctrl);
	}

	@Override
	public StatusDescription isConfigValid() {
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

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		// only here we know which translator to take for translating condition
		// error messages
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, PACKAGE_IQ, getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		// ",false" because we do not want to be strict, but just indicate whether
		// the reference still exists or not
		return IQEditController.getIQReference(getModuleConfiguration(), false);
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return true;
	}
	
	@Override
	public boolean hasAttemptsConfigured() {
		return false;
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		// 1) Delete all assessment test sessions (QTI 2.1)
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		CoreSpringFactory.getImpl(AssessmentTestSessionDAO.class).deleteAllUserTestSessionsByCourse(courseEntry, getIdent());
	}

	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options,
			ZipOutputStream exportStream, String archivePath, String charset) {
		String repositorySoftKey = (String) getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, false);
		if(re == null) {
			log.error("Cannot archive course node. Missing repository entry with soft key: {}", repositorySoftKey);
			return false;
		}
		
		try {
			if(ImsQTI21Resource.TYPE_NAME.equals(re.getOlatResource().getResourceableTypeName())) {
				RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
				QTI21StatisticSearchParams searchParams = new QTI21StatisticSearchParams(options, re, courseEntry, getIdent());
				QTI21ArchiveFormat qaf = new QTI21ArchiveFormat(locale, searchParams);
				qaf.exportCourseElement(exportStream, archivePath);
				return true;	
			}
			return false;
		} catch (Exception e) {
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
			if(handlerQTI21.acceptImport(file, "repo.zip").isValid()) {
				RepositoryEntry re = handlerQTI21.importResource(owner, rie.getInitialAuthor(), rie.getDisplayName(),
						rie.getDescription(), false, organisation, locale, rie.importGetExportedFile(), null);
				getModuleConfiguration().set(IQEditController.CONFIG_KEY_TYPE_QTI, IQEditController.CONFIG_VALUE_QTI21);
				IQEditController.setIQReference(re, getModuleConfiguration());
			}
		} else {
			IQEditController.removeIQReference(getModuleConfiguration());
		}
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
			config.set(IQEditController.CONFIG_KEY_SEQUENCE, QTI21Constants.QMD_ENTRY_SEQUENCE_ITEM);
			config.set(IQEditController.CONFIG_KEY_TYPE, QTI21Constants.QMD_ENTRY_TYPE_SELF);
			config.set(IQEditController.CONFIG_KEY_SUMMARY, QTI21Constants.QMD_ENTRY_SUMMARY_DETAILED);
			config.set(IQEditController.CONFIG_KEY_CONFIG_REF, Boolean.TRUE);
		} else {
			int version = config.getConfigurationVersion();
			if (version < CURRENT_CONFIG_VERSION) {
				if (version <= 3) {
					if (config.get(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS) instanceof Boolean) {
						config.setStringValue(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS, String.valueOf(config.getBooleanEntry(IQEditController.CONFIG_KEY_DATE_DEPENDENT_RESULTS)));
					}
				}
				
			}
		}
		
		config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
	}
	
	@Override
	public ScoreEvaluation getUserScoreEvaluation(final UserCourseEnvironment userCourseEnv) {
		// read score from properties save score, passed and attempts information
		ScoreEvaluation scoreEvaluation = null;
		RepositoryEntry referencedRepositoryEntry = getReferencedRepositoryEntry();
		if(referencedRepositoryEntry != null && ImsQTI21Resource.TYPE_NAME.equals(referencedRepositoryEntry.getOlatResource().getResourceableTypeName())) {
			RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			Identity assessedIdentity = userCourseEnv.getIdentityEnvironment().getIdentity();
			AssessmentTestSession testSession = CoreSpringFactory.getImpl(QTI21Service.class)
					.getLastAssessmentTestSessions(courseEntry, getIdent(), referencedRepositoryEntry, assessedIdentity);
			if(testSession != null) {
				Float score = testSession.getScore() == null ? null : testSession.getScore().floatValue();
				return new ScoreEvaluation(score, testSession.getPassed(), testSession.getKey());
			}
		}
		return scoreEvaluation;
	}
	
	@Override
	public Integer getUserAttempts(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		return am.getNodeAttempts(this, mySelf);
	}

	@Override
	public void incrementUserAttempts(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment, Role by) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		am.incrementNodeAttempts(this, mySelf, userCourseEnvironment, by);
	}

}
