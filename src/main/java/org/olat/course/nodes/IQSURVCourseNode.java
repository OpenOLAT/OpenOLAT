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
import org.olat.core.gui.translator.PackageTranslator;
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
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.iq.CourseIQSecurityCallback;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.nodes.iq.IQRunController;
import org.olat.course.nodes.iq.QTIResourceTypeModule;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.statistic.StatisticResourceOption;
import org.olat.course.statistic.StatisticResourceResult;
import org.olat.course.statistic.StatisticType;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.ims.qti.QTIModule;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.export.QTIExportFormatter;
import org.olat.ims.qti.export.QTIExportFormatterCSVType3;
import org.olat.ims.qti.export.QTIExportManager;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.statistics.QTIStatisticResourceResult;
import org.olat.ims.qti.statistics.QTIStatisticSearchParams;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.ims.qti21.ui.statistics.QTI21StatisticResourceResult;
import org.olat.ims.qti21.ui.statistics.QTI21StatisticsSecurityCallback;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.iq.IQSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

/**
 * Initial Date: Feb 9, 2004
 * @author Mike Stock Comment:
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class IQSURVCourseNode extends AbstractAccessableCourseNode implements QTICourseNode {

	private static final long serialVersionUID = 1672009454920536416L;
	private static final Logger log = Tracing.createLoggerFor(IQSURVCourseNode.class);

	private static final String TYPE = "iqsurv";
	/** category that is used to persist the node properties */
	public static final String PROPERTY_CATEGORY = "iqsu";
	private static final String PACKAGE_IQ = Util.getPackageName(IQRunController.class);
	
	public IQSURVCourseNode() {
		this(null);
	}

	public IQSURVCourseNode(INode parent) {
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
		// Do not allow guests to start questionnaires
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			Translator trans = Util.createPackageTranslator(IQSURVCourseNode.class, ureq.getLocale());
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			RepositoryEntry repositoryEntry = getReferencedRepositoryEntry();
			OLATResourceable ores = repositoryEntry.getOlatResource();
			if (QTIResourceTypeModule.isOnyxTest(ores)) {
				Translator trans = Util.createPackageTranslator(IQEditController.class, ureq.getLocale());
				controller = MessageUIFactory.createInfoMessage(ureq, wControl, "", trans.translate("error.onyx"));
			} else {
				QTIModule qtiModule = CoreSpringFactory.getImpl(QTIModule.class);
				if (qtiModule.isRunSurveyEnabled()) {
					Long resId = ores.getResourceableId();
					SurveyFileResource fr = new SurveyFileResource();
					fr.overrideResourceableId(resId);
					if(!CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(fr, null)) {
						AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
						IQSecurityCallback sec = new CourseIQSecurityCallback(this, am, ureq.getIdentity());
						controller = new IQRunController(userCourseEnv, getModuleConfiguration(), sec, ureq, wControl, this);
					} else {
						Translator trans = Util.createPackageTranslator(IQSURVCourseNode.class, ureq.getLocale());
						String title = trans.translate("editor.lock.title");
						String message = trans.translate("editor.lock.message");
						controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
					}
				} else {
					Translator transe = Util.createPackageTranslator(IQEditController.class, ureq.getLocale());
					controller = MessageUIFactory.createInfoMessage(ureq, wControl, "", transe.translate("error.qti12.survey"));
				}
			}
		}
		
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, this, "o_iqsurv_icon");
		return new NodeRunConstructionResult(ctrl);
	}

	@Override
	public StatisticResourceResult createStatisticNodeResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, StatisticResourceOption options, StatisticType type) {
		if(!isStatisticTypeAllowed(type)) return null;
		
		Long courseId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
		OLATResourceable courseOres = OresHelper.createOLATResourceableInstance("CourseModule", courseId);

		RepositoryEntry qtiSurveyEntry = getReferencedRepositoryEntry();
		if(ImsQTI21Resource.TYPE_NAME.equals(qtiSurveyEntry.getOlatResource().getResourceableTypeName())) {
			RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			QTI21StatisticSearchParams searchParams = new QTI21StatisticSearchParams(qtiSurveyEntry, courseEntry, getIdent());
			searchParams.setLimitToGroups(options.getParticipantsGroups());
			QTI21DeliveryOptions deliveryOptions = CoreSpringFactory.getImpl(QTI21Service.class)
					.getDeliveryOptions(qtiSurveyEntry);
			boolean admin = userCourseEnv.isAdmin();
			QTI21StatisticsSecurityCallback secCallback = new QTI21StatisticsSecurityCallback(admin, admin && deliveryOptions.isAllowAnonym());
			return new QTI21StatisticResourceResult(qtiSurveyEntry, courseEntry, this, searchParams, secCallback);
		}
		
		QTIStatisticSearchParams searchParams = new QTIStatisticSearchParams(courseOres.getResourceableId(), getIdent());
		searchParams.setLimitToGroups(options.getParticipantsGroups());
		return new QTIStatisticResourceResult(courseOres, this, qtiSurveyEntry, searchParams);
	}
	
	@Override
	public boolean isStatisticNodeResultAvailable(UserCourseEnvironment userCourseEnv, StatisticType type) {
		return isStatisticTypeAllowed(type);
	}
	
	private boolean isStatisticTypeAllowed(StatisticType type) {
		if(StatisticType.SURVEY.equals(type)) {
			return true;
		}
		return false;
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
			}
		}
		StatusDescription sd = StatusDescription.NOERROR;
		if (!isValid) {
			String shortKey = "error.surv.undefined.short";
			String longKey = "error.surv.undefined.long";
			String[] params = new String[] { getShortTitle() };
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, PACKAGE_IQ);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(IQEditController.PANE_TAB_IQCONFIG_SURV);
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
		RepositoryEntry re = IQEditController.getIQReference(getModuleConfiguration(), false);
		return re;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return true;
	}

	@Override
	public String informOnDelete(Locale locale, ICourse course) {
		// Check if there are qtiresults for this questionnaire
		String repositorySoftKey = (String) getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		Long repKey = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, true).getKey();
		if (QTIResultManager.getInstance().hasResultSets(course.getResourceableId(), this.getIdent(), repKey)) { return new PackageTranslator(
				PACKAGE_IQ, locale).translate("info.nodedelete"); }
		return null;
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		
		CoursePropertyManager pm = course.getCourseEnvironment().getCoursePropertyManager();
		// 1) Delete all properties: attempts
		pm.deleteNodeProperties(this, null);
		// 2) Delete all qtiresults for this node
		String repositorySoftKey = (String) getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, false);
		if(re != null) {
			QTIResultManager.getInstance().deleteAllResults(course.getResourceableId(), getIdent(), re.getKey());
		}
	}

	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options,
			ZipOutputStream exportStream, String archivePath, String charset) {
		QTIExportManager qem = QTIExportManager.getInstance();
		String repositorySoftKey = (String) getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repositorySoftKey, false);
		if(re == null) {
			log.error("Cannot archive course node. Missing repository entry with soft key: ", repositorySoftKey);
			return false;
		}

		QTIExportFormatter qef = new QTIExportFormatterCSVType3(locale, null,"\t", "\"", "\r\n", false);
		try {
			return qem.selectAndExportResults(qef, course.getResourceableId(), getShortTitle(), getIdent(), re, exportStream, archivePath, locale, ".xls");
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
			RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(SurveyFileResource.TYPE_NAME);
			RepositoryEntry re = handler.importResource(owner, rie.getInitialAuthor(), rie.getDisplayName(),
				rie.getDescription(), false, organisation, locale, rie.importGetExportedFile(), null);
			IQEditController.setIQReference(re, getModuleConfiguration());
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
			config.set(IQEditController.CONFIG_KEY_ENABLEMENU, new Boolean(true));
			config.set(IQEditController.CONFIG_KEY_SEQUENCE, AssessmentInstance.QMD_ENTRY_SEQUENCE_ITEM);
			config.set(IQEditController.CONFIG_KEY_TYPE, AssessmentInstance.QMD_ENTRY_TYPE_SURVEY);
			config.set(IQEditController.CONFIG_KEY_SUMMARY, AssessmentInstance.QMD_ENTRY_SUMMARY_NONE); // not used in survey
		}
	}

	@Override
	public boolean hasAttemptsConfigured() {
		return true;
	}

}
