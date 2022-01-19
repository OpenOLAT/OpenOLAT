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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.nodes.iq.QTI21AssessmentRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.course.statistic.StatisticResourceOption;
import org.olat.course.statistic.StatisticResourceResult;
import org.olat.course.statistic.StatisticType;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;

/**
 * Initial Date: Feb 9, 2004
 * @author Mike Stock Comment:
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class IQSURVCourseNode extends AbstractAccessableCourseNode implements QTICourseNode {

	private static final long serialVersionUID = 1672009454920536416L;

	private static final String TYPE = "iqsurv";
	/** category that is used to persist the node properties */
	public static final String PROPERTY_CATEGORY = "iqsu";
	private static final String PACKAGE_IQ = Util.getPackageName(QTI21AssessmentRunController.class);
	
	public IQSURVCourseNode() {
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
		
		Translator transe = Util.createPackageTranslator(IQEditController.class, ureq.getLocale());
		Controller controller = MessageUIFactory.createInfoMessage(ureq, wControl, "", transe.translate("error.qti12.survey"));
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, "o_iqsurv_icon");
		return new NodeRunConstructionResult(ctrl);
	}

	@Override
	public StatisticResourceResult createStatisticNodeResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, StatisticResourceOption options, StatisticType type) {
		return null;
	}
	
	@Override
	public boolean isStatisticNodeResultAvailable(UserCourseEnvironment userCourseEnv, StatisticType type) {
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
		return IQEditController.getIQReference(getModuleConfiguration(), false);
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return true;
	}

	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options,
			ZipOutputStream exportStream, String archivePath, String charset) {
		return false;
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

	/**
	 * Update the module configuration to have all mandatory configuration flags
	 * set to usefull default values
	 * @param isNewNode true: an initial configuration is set; false: upgrading
	 *          from previous node configuration version, set default to maintain
	 *          previous behaviour
	 */
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType);
		
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			// add default module configuration
			config.set(IQEditController.CONFIG_KEY_ENABLEMENU, Boolean.valueOf(true));
			config.set(IQEditController.CONFIG_KEY_SEQUENCE, QTI21Constants.QMD_ENTRY_SEQUENCE_ITEM);
			config.set(IQEditController.CONFIG_KEY_TYPE, QTI21Constants.QMD_ENTRY_TYPE_SURVEY);
			config.set(IQEditController.CONFIG_KEY_SUMMARY, QTI21Constants.QMD_ENTRY_SUMMARY_NONE); // not used in survey
		}
	}

	@Override
	public boolean hasAttemptsConfigured() {
		return true;
	}

}
