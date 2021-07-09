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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.callbacks.FullAccessCallback;
import org.olat.core.util.vfs.filters.VFSLeafFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.noderight.NodeRight;
import org.olat.course.noderight.NodeRightGrant.NodeRightRole;
import org.olat.course.noderight.NodeRightService;
import org.olat.course.noderight.NodeRightType;
import org.olat.course.noderight.NodeRightTypeBuilder;
import org.olat.course.nodes.dialog.DialogElement;
import org.olat.course.nodes.dialog.DialogElementsManager;
import org.olat.course.nodes.dialog.DialogSecurityCallback;
import org.olat.course.nodes.dialog.security.SecurityCallbackFactory;
import org.olat.course.nodes.dialog.ui.DialogCourseNodeEditController;
import org.olat.course.nodes.dialog.ui.DialogCourseNodeRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.archiver.ForumArchive;
import org.olat.repository.RepositoryEntry;

/**
 * Initial Date: 02.11.2005 <br>
 * 
 * @author Guido Schnider
 */
public class DialogCourseNode extends AbstractAccessableCourseNode {

	private static final Logger log = Tracing.createLoggerFor(DialogCourseNode.class);
	public static final String TYPE = "dialog";
	
	@SuppressWarnings("deprecation")
	private static final String TRANSLATOR_PACKAGE = Util.getPackageName(DialogCourseNodeEditController.class);
	
	private static final int CURRENT_VERSION = 3;
	
	private static final String LEGACY_KEY_UPLOAD_BY_COACH = "upload.by.coach";
	private static final String LEGACY_KEY_UPLOAD_BY_PARTICIPANT = "upload.by.participant";
	private static final String LEGACY_KEY_MODERATE_BY_COACH = "moderate.by.coach";
	private static final String LEGACY_KEY_POST_BY_COACH = "post.by.coach";
	private static final String LEGACY_KEY_POST_BY_PARTICIPANT = "post.by.participant";
	
	public static final NodeRightType MODARATE = NodeRightTypeBuilder.ofIdentifier("modarate")
			.setLabel(DialogCourseNodeEditController.class, "edit.moderator")
			.addRole(NodeRightRole.coach, true)
			.build();
	public static final NodeRightType UPLOAD = NodeRightTypeBuilder.ofIdentifier("upload")
			.setLabel(DialogCourseNodeEditController.class, "edit.upload")
			.addRole(NodeRightRole.coach, true)
			.addRole(NodeRightRole.participant, true)
			.build();
	public static final NodeRightType POST = NodeRightTypeBuilder.ofIdentifier("post")
			.setLabel(DialogCourseNodeEditController.class, "edit.poster")
			.addRole(NodeRightRole.coach, true)
			.addRole(NodeRightRole.participant, true)
			.build();
	public static final List<NodeRightType> NODE_RIGHT_TYPES = List.of(MODARATE, UPLOAD, POST);
	
	private Condition preConditionReader, preConditionPoster, preConditionModerator;

	public DialogCourseNode() {
		this(null);
	}

	public DialogCourseNode(INode parent) {
		super(TYPE, parent);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		DialogCourseNodeEditController childTabCntrllr = new DialogCourseNodeEditController(ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCntrllr);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return hasCustomPreConditions()
				? ConditionAccessEditConfig.custom()
				: ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		DialogSecurityCallback secCallback = SecurityCallbackFactory.create(this, userCourseEnv, nodeSecCallback.getNodeEvaluation());
		DialogCourseNodeRunController ctrl = new DialogCourseNodeRunController(ureq, wControl, this, userCourseEnv, secCallback);
		Controller wrappedCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, ctrl, userCourseEnv, this, "o_dialog_icon");
		return new NodeRunConstructionResult(wrappedCtrl);
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, TRANSLATOR_PACKAGE, getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}

	@Override
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
	 * @param isNewNode true: an initial configuration is set; false: upgrading
	 *          from previous node configuration version, set default to maintain
	 *          previous behaviour
	 */
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent) {
		ModuleConfiguration config = getModuleConfiguration();
		int version = config.getConfigurationVersion();
		
		if (version < 2) {
			removeDefaultPreconditions();
		}
		if (version < 3 && config.has(LEGACY_KEY_MODERATE_BY_COACH)) {
			NodeRightService nodeRightService = CoreSpringFactory.getImpl(NodeRightService.class);
			// Moderate
			NodeRight executionRight = nodeRightService.getRight(config, MODARATE);
			Collection<NodeRightRole> moderateRoles = new ArrayList<>(1);
			if (config.getBooleanSafe(LEGACY_KEY_MODERATE_BY_COACH)) {
				moderateRoles.add(NodeRightRole.coach);
			}
			nodeRightService.setRoleGrants(executionRight, moderateRoles);
			nodeRightService.setRight(config, executionRight);
			// Upload
			NodeRight uploadRight = nodeRightService.getRight(config, UPLOAD);
			Collection<NodeRightRole> uploadRoles = new ArrayList<>(2);
			if (config.getBooleanSafe(LEGACY_KEY_UPLOAD_BY_COACH)) {
				uploadRoles.add(NodeRightRole.coach);
			}
			if (config.getBooleanSafe(LEGACY_KEY_UPLOAD_BY_PARTICIPANT)) {
				uploadRoles.add(NodeRightRole.participant);
			}
			nodeRightService.setRoleGrants(uploadRight, uploadRoles);
			nodeRightService.setRight(config, uploadRight);
			// Post
			NodeRight postRight = nodeRightService.getRight(config, POST);
			Collection<NodeRightRole> postRoles = new ArrayList<>(2);
			if (config.getBooleanSafe(LEGACY_KEY_POST_BY_COACH)) {
				postRoles.add(NodeRightRole.coach);
			}
			if (config.getBooleanSafe(LEGACY_KEY_POST_BY_PARTICIPANT)) {
				postRoles.add(NodeRightRole.participant);
			}
			nodeRightService.setRoleGrants(postRight, postRoles);
			nodeRightService.setRight(config, postRight);
			// Remove legacy
			config.remove(LEGACY_KEY_MODERATE_BY_COACH);
			config.remove(LEGACY_KEY_UPLOAD_BY_COACH);
			config.remove(LEGACY_KEY_UPLOAD_BY_PARTICIPANT);
			config.remove(LEGACY_KEY_POST_BY_COACH);
			config.remove(LEGACY_KEY_POST_BY_PARTICIPANT);
		}
		config.setConfigurationVersion(CURRENT_VERSION);
	}
	
	private void removeDefaultPreconditions() {
		if (hasCustomPreConditions()) {
			boolean defaultPreconditions =
					!preConditionModerator.isExpertMode()
				&& preConditionModerator.isEasyModeCoachesAndAdmins()
				&& preConditionModerator.isEasyModeAlwaysAllowCoachesAndAdmins()
				&& !preConditionModerator.isAssessmentMode()
				&& !preConditionModerator.isAssessmentModeViewResults()
				&& !preConditionPoster.isExpertMode()
				&& !preConditionPoster.isEasyModeCoachesAndAdmins()
				&& !preConditionPoster.isEasyModeAlwaysAllowCoachesAndAdmins()
				&& !preConditionPoster.isAssessmentMode()
				&& !preConditionPoster.isAssessmentModeViewResults()
				&& !preConditionReader.isExpertMode()
				&& !preConditionReader.isEasyModeCoachesAndAdmins()
				&& !preConditionReader.isEasyModeAlwaysAllowCoachesAndAdmins()
				&& !preConditionReader.isAssessmentMode()
				&& !preConditionReader.isAssessmentModeViewResults();
			if (defaultPreconditions) {
				removeCustomPreconditions();
			}
		}
	}
	
	public void removeCustomPreconditions() {
		preConditionModerator = null;
		preConditionPoster = null;
		preConditionReader = null;
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

	@Override
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
			CoreSpringFactory.getImpl(NotificationsManager.class).delete(subsContext);
			depm.deleteDialogElement(dialogElement);
		}
	}

	/**
	 * Archive a single dialog element with files and forum
	 * @param element
	 * @param exportDirectory
	 * @param savedBy 
	 */
	public void doArchiveElement(DialogElement element, File exportDirectory, Locale locale, Identity savedBy) {
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
		diaNodeElemExportContainer.copyFrom(dialogFile, savedBy);
		
		try {
			ForumArchive archiver = new ForumArchive(element.getForum(), null, locale, null);
			archiver.export("Forum.docx", diaNodeElemExportContainer);
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options,
			ZipOutputStream exportStream, String archivePath, String charset) {
		boolean dataFound = false;
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<DialogElement> list = CoreSpringFactory.getImpl(DialogElementsManager.class)
				.getDialogElements(entry, getIdent());
		if(!list.isEmpty()) {
			for (DialogElement element:list) {
				doArchiveElement(element, exportStream, archivePath, locale);
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
	public void doArchiveElement(DialogElement element, ZipOutputStream exportStream, String archivePath, Locale locale) {
		DialogElementsManager depm = CoreSpringFactory.getImpl(DialogElementsManager.class);
		String exportDirName = Formatter.makeStringFilesystemSave(getShortTitle())
				+ "_" + element.getForum().getKey()
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date());
		exportDirName = ZipUtil.concat(archivePath, exportDirName);
		
		VFSContainer forumContainer =  depm.getDialogContainer(element);
		for(VFSItem item: forumContainer.getItems(new VFSLeafFilter())) {
			ZipUtil.addToZip(item, exportDirName, exportStream, new VFSSystemItemFilter(), false);
		}
		
		try {
			Forum forum = element.getForum();
			ForumArchive archiver = new ForumArchive(forum, null, locale, null);
			archiver.export("Dialogs.docx", exportDirName, exportStream);
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void calcAccessAndVisibility(ConditionInterpreter ci, NodeEvaluation nodeEval) {
		if (hasCustomPreConditions()) {
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
		} else {
			super.calcAccessAndVisibility(ci, nodeEval);
		}
	}
	
	public boolean hasCustomPreConditions() {
		return preConditionModerator != null || preConditionPoster != null || preConditionReader != null;
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
