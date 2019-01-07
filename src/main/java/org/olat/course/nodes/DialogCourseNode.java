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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.callbacks.FullAccessCallback;
import org.olat.core.util.vfs.filters.VFSLeafFilter;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.dialog.DialogElement;
import org.olat.course.nodes.dialog.DialogElementsManager;
import org.olat.course.nodes.dialog.ui.DialogConfigForm;
import org.olat.course.nodes.dialog.ui.DialogCourseNodeEditController;
import org.olat.course.nodes.dialog.ui.DialogCourseNodeRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.fo.archiver.ForumArchiveManager;
import org.olat.modules.fo.archiver.formatters.ForumFormatter;
import org.olat.modules.fo.archiver.formatters.ForumRTFFormatter;
import org.olat.modules.fo.archiver.formatters.ForumStreamedRTFFormatter;
import org.olat.repository.RepositoryEntry;

/**
 * Initial Date: 02.11.2005 <br>
 * 
 * @author Guido Schnider
 */
public class DialogCourseNode extends AbstractAccessableCourseNode {

	public static final String TYPE = "dialog";
	private Condition preConditionReader, preConditionPoster, preConditionModerator;

	public DialogCourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
	}

	/**
	 * @see org.olat.course.nodes.GenericCourseNode#createEditController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl, org.olat.course.ICourse,
	 *      org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		updateModuleConfigDefaults(false);
		DialogCourseNodeEditController childTabCntrllr = new DialogCourseNodeEditController(ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, euce, childTabCntrllr);
	}

	/**
	 * @see org.olat.course.nodes.GenericCourseNode#createNodeRunConstructionResult(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.course.run.userview.NodeEvaluation, java.lang.String)
	 */
	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		DialogCourseNodeRunController ctrl = new DialogCourseNodeRunController(ureq, wControl, this, userCourseEnv, ne);
		Controller wrappedCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, ctrl, this, "o_dialog_icon");
		return new NodeRunConstructionResult(wrappedCtrl);
	}

	/**
	 * @see org.olat.course.nodes.GenericCourseNode#isConfigValid(org.olat.course.editor.CourseEditorEnv)
	 */
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		// only here we know which translator to take for translating condition
		// error messages
		String translatorStr = Util.getPackageName(DialogCourseNodeEditController.class);
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
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

		return StatusDescription.NOERROR;
	}

	/**
	 * Update the module configuration to have all mandatory configuration flags
	 * set to usefull default values
	 * 
	 * @param isNewNode true: an initial configuration is set; false: upgrading
	 *          from previous node configuration version, set default to maintain
	 *          previous behaviour
	 */
	public void updateModuleConfigDefaults(boolean isNewNode) {
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			// use defaults for new course building blocks
			//REVIEW:pb version should go to 2 now and the handling for 1er should be to remove 
			config.setConfigurationVersion(1);
			config.set(DialogConfigForm.DIALOG_CONFIG_INTEGRATION, DialogConfigForm.CONFIG_INTEGRATION_VALUE_INLINE);
		}
	}
	
	@Override
	protected void postImportCopyConditions(CourseEnvironmentMapper envMapper) {
		super.postImportCopyConditions(envMapper);
		postImportCondition(preConditionModerator, envMapper);
		postImportCondition(preConditionPoster, envMapper);
		postImportCondition(preConditionReader, envMapper);
	}

	@Override
	public void postExport(CourseEnvironmentMapper envMapper, boolean backwardsCompatible) {
		super.postExport(envMapper, backwardsCompatible);
		postExportCondition(preConditionModerator, envMapper, backwardsCompatible);
		postExportCondition(preConditionPoster, envMapper, backwardsCompatible);
		postExportCondition(preConditionReader, envMapper, backwardsCompatible);
	}

	public String informOnDelete(Locale locale, ICourse course) {
		return null;
	}

	/**
	 * life cycle of node data e.g properties stuff should be deleted if node gets
	 * deleted life cycle: create - delete - migrate
	 */
	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		
		DialogElementsManager depm = CoreSpringFactory.getImpl(DialogElementsManager.class);
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<DialogElement> dialogElements = depm.getDialogElements(entry, getIdent());
		for (DialogElement dialogElement : dialogElements) {
			Long forumKey = dialogElement.getForum().getKey();
			SubscriptionContext subsContext = CourseModule.createSubscriptionContext(course.getCourseEnvironment(), this, forumKey.toString());
			NotificationsManager.getInstance().delete(subsContext);
			depm.deleteDialogElement(dialogElement);
		}
	}

	/**
	 * Archive a single dialog element with files and forum
	 * @param element
	 * @param exportDirectory
	 */
	public void doArchiveElement(DialogElement element, File exportDirectory, Locale locale) {
		DialogElementsManager depm = CoreSpringFactory.getImpl(DialogElementsManager.class);
		VFSContainer dialogContainer = depm.getDialogContainer(element);
		//there is only one file (leave) in the top forum container 
		VFSItem dialogFile = dialogContainer.getItems(new VFSLeafFilter()).get(0);
		VFSContainer exportContainer = new LocalFolderImpl(exportDirectory);
		
		// append export timestamp to avoid overwriting previous export 
		String exportDirName = Formatter.makeStringFilesystemSave(getShortTitle())+"_"+element.getForum().getKey()+"_"+Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()));
		VFSContainer diaNodeElemExportContainer = exportContainer.createChildContainer(exportDirName);
		// don't check quota
		diaNodeElemExportContainer.setLocalSecurityCallback(new FullAccessCallback());
		diaNodeElemExportContainer.copyFrom(dialogFile);

		ForumArchiveManager fam = CoreSpringFactory.getImpl(ForumArchiveManager.class);
		ForumFormatter ff = new ForumRTFFormatter(diaNodeElemExportContainer, false, locale);
		fam.applyFormatter(ff, element.getForum().getKey(), null);
	}
	
	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options, ZipOutputStream exportStream, String charset) {
		boolean dataFound = false;
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<DialogElement> list = CoreSpringFactory.getImpl(DialogElementsManager.class)
				.getDialogElements(entry, getIdent());
		if(list.size() > 0) {
			for (DialogElement element:list) {
				doArchiveElement(element, exportStream, locale);
				dataFound = true;
			}
		}
		return dataFound;
	}
	
	/**
	 * Archive a single dialog element with files and forum
	 * @param element
	 * @param exportDirectory
	 */
	public void doArchiveElement(DialogElement element, ZipOutputStream exportStream, Locale locale) {
		DialogElementsManager depm = CoreSpringFactory.getImpl(DialogElementsManager.class);
		String exportDirName = Formatter.makeStringFilesystemSave(getShortTitle())
				+ "_" + element.getForum().getKey()
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date());
		
		VFSContainer forumContainer =  depm.getDialogContainer(element);
		for(VFSItem item: forumContainer.getItems(new VFSLeafFilter())) {
			ZipUtil.addToZip(item, exportDirName, exportStream);
		}

		ForumArchiveManager fam = CoreSpringFactory.getImpl(ForumArchiveManager.class);
		ForumFormatter ff = new ForumStreamedRTFFormatter(exportStream, exportDirName, false, locale);
		fam.applyFormatter(ff, element.getForum().getKey(), null);
	}

	@Override
	protected void calcAccessAndVisibility(ConditionInterpreter ci, NodeEvaluation nodeEval) {
		// evaluate the preconditions
		boolean reader = (getPreConditionReader().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionReader()));
		nodeEval.putAccessStatus("reader", reader);
		boolean poster = (getPreConditionPoster().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionPoster()));
		nodeEval.putAccessStatus("poster", poster);
		boolean moderator = (getPreConditionModerator().getConditionExpression() == null ? true : ci
				.evaluateCondition(getPreConditionModerator()));
		nodeEval.putAccessStatus("moderator", moderator);

		boolean visible = (getPreConditionVisibility().getConditionExpression() == null ? true : ci
				.evaluateCondition(getPreConditionVisibility()));
		nodeEval.setVisible(visible);
	}

	/**
	 * @return Returns the preConditionModerator.
	 */
	public Condition getPreConditionModerator() {
		if (this.preConditionModerator == null) {
			this.preConditionModerator = new Condition();
			//learner should not be able to delete files by default
			this.preConditionModerator.setEasyModeCoachesAndAdmins(true);
			this.preConditionModerator.setEasyModeAlwaysAllowCoachesAndAdmins(true);
			this.preConditionModerator.setConditionExpression("(  ( isCourseCoach(0) | isCourseAdministrator(0) ) )");
		}
		this.preConditionModerator.setConditionId("moderator");
		return this.preConditionModerator;
	}

	/**
	 * @param preConditionModerator The preConditionModerator to set.
	 */
	public void setPreConditionModerator(Condition preConditionMod) {
		if (preConditionMod == null) {
			preConditionMod = getPreConditionModerator();
		}
		preConditionMod.setConditionId("moderator");
		this.preConditionModerator = preConditionMod;
	}

	/**
	 * @return Returns the preConditionPoster.
	 */
	public Condition getPreConditionPoster() {
		if (preConditionPoster == null) {
			preConditionPoster = new Condition();
		}
		preConditionPoster.setConditionId("poster");
		return preConditionPoster;
	}

	/**
	 * @param preConditionPoster The preConditionPoster to set.
	 */
	public void setPreConditionPoster(Condition preConditionPoster) {
		if (preConditionPoster == null) {
			preConditionPoster = getPreConditionPoster();
		}
		preConditionPoster.setConditionId("poster");
		this.preConditionPoster = preConditionPoster;
	}

	/**
	 * @return Returns the preConditionReader.
	 */
	public Condition getPreConditionReader() {
		if (preConditionReader == null) {
			preConditionReader = new Condition();
		}
		preConditionReader.setConditionId("reader");
		return preConditionReader;
	}

	/**
	 * @param preConditionReader The preConditionReader to set.
	 */
	public void setPreConditionReader(Condition preConditionReader) {
		if (preConditionReader == null) {
			preConditionReader = getPreConditionReader();
		}
		preConditionReader.setConditionId("reader");
		this.preConditionReader = preConditionReader;
	}
}
