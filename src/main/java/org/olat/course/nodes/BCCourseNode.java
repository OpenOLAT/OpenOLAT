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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSRevisionsAndThumbnailsFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.bc.BCCourseNodeEditController;
import org.olat.course.nodes.bc.BCCourseNodeRunController;
import org.olat.course.nodes.bc.BCPeekviewController;
import org.olat.course.nodes.bc.BCPreviewController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

/*
 * Description:<br>
 * @author Felix Jost
 */
public class BCCourseNode extends AbstractAccessableCourseNode {
	private static final long serialVersionUID = 6887400715976544402L;
	private static final String PACKAGE_BC = Util.getPackageName(BCCourseNodeRunController.class);
	public static final String TYPE = "bc";
	
	private static final int CURRENT_VERSION = 3;
	public static final String CONFIG_AUTO_FOLDER = "config.autofolder";
	public static final String CONFIG_SUBPATH = "config.subpath";
	public static final String CONFIG_KEY_UPLOAD_BY_COACH = "upload.by.coach";
	public static final String CONFIG_KEY_UPLOAD_BY_PARTICIPANT = "upload.by.participant";

	/**
	 * Condition.getCondition() == null means no precondition, always accessible
	 */
	private Condition preConditionUploaders, preConditionDownloaders;

	public BCCourseNode() {
		this(null);
	}
	
	public BCCourseNode(INode parent) {
		super(TYPE, parent);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		BCCourseNodeEditController childTabCntrllr = new BCCourseNodeEditController(ureq, wControl, stackPanel, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, course, chosenNode, euce, childTabCntrllr);
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
		BCCourseNodeRunController bcCtrl = new BCCourseNodeRunController(ureq, wControl, userCourseEnv, this, nodeSecCallback.getNodeEvaluation());
		if (StringHelper.containsNonWhitespace(nodecmd)) {
			bcCtrl.activatePath(ureq, nodecmd);
		}
		Controller titledCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, bcCtrl, this, "o_bc_icon");
		return new NodeRunConstructionResult(titledCtrl);
	}

	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			CourseNodeSecurityCallback nodeSecCallback) {
		if (nodeSecCallback.isAccessible()) {
			// Create a folder peekview controller that shows the latest two entries
			VFSContainer rootFolder = null;
			if(getModuleConfiguration().getBooleanSafe(CONFIG_AUTO_FOLDER)) {
				rootFolder = getNodeFolderContainer(this, userCourseEnv.getCourseEnvironment());
			} else {
				String subPath = getModuleConfiguration().getStringValue(CONFIG_SUBPATH, "");
				VFSItem item = userCourseEnv.getCourseEnvironment().getCourseFolderContainer().resolve(subPath);
				if(item instanceof VFSContainer) {
					rootFolder = (VFSContainer)item;
				}
			}
			
			if(rootFolder == null) {
				return super.createPeekViewRunController(ureq, wControl, userCourseEnv, nodeSecCallback);
			}
			rootFolder.setDefaultItemFilter(new VFSSystemItemFilter());
			return new BCPeekviewController(ureq, wControl, rootFolder, getIdent(), 4);
		} else {
			// use standard peekview
			return super.createPeekViewRunController(ureq, wControl, userCourseEnv, nodeSecCallback);
		}
	}

	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		return new BCPreviewController(ureq, wControl, this, userCourseEnv, nodeSecCallback.getNodeEvaluation());
	}

	/**
	 * @param courseEnv
	 * @param node
	 * @return the relative folder base path for this folder node
	 */
	public static String getFoldernodePathRelToFolderBase(CourseEnvironment courseEnv, CourseNode node) {
		return getFoldernodesPathRelToFolderBase(courseEnv) + "/" + node.getIdent();
	}

	/**
	 * @param courseEnv
	 * @return the relative folder base path for folder nodes
	 */
	public static String getFoldernodesPathRelToFolderBase(CourseEnvironment courseEnv) {
		return courseEnv.getCourseBaseContainer().getRelPath() + "/foldernodes";
	}

	public boolean isSharedFolder(){
		return getModuleConfiguration().getStringValue(CONFIG_SUBPATH, "")
				.startsWith("/_sharedfolder");
	}

	/**
	 * Get a named container of a node with the node title as its name.
	 * @param node
	 * @param courseEnv
	 * @return
	 */
	public static VFSContainer getNodeFolderContainer(BCCourseNode node, CourseEnvironment courseEnv) {
		String path = getFoldernodePathRelToFolderBase(courseEnv, node);
		VFSContainer rootFolder = VFSManager.olatRootContainer(path, null);
		return new NamedContainerImpl(node.getShortTitle(), rootFolder);
	}

	@Override
	public void exportNode(File exportDirectory, ICourse course) {
		// this is the node folder, a folder with the node's ID, so we can just copy
		// the contents over to the export folder
		VFSContainer nodeContainer = VFSManager
				.olatRootContainer(getFoldernodePathRelToFolderBase(course.getCourseEnvironment(), this), null);
		File fNodeExportDir = new File(exportDirectory, getIdent());
		fNodeExportDir.mkdirs();
		File outputFile = new File(fNodeExportDir, "oonode.zip");
		ZipUtil.zip(nodeContainer, outputFile, new VFSRevisionsAndThumbnailsFilter(), true);
	}

	@Override
	public void importNode(File importDirectory, ICourse course, Identity owner, Organisation organisation, Locale locale, boolean withReferences) {
		// the export has copies the files under the node's ID
		File fFolderNodeData = new File(importDirectory, getIdent());
		File fFolderNodeZip = new File(fFolderNodeData, "oonode.zip");
		if(fFolderNodeZip.exists()) {
			VFSContainer nodeContainer = VFSManager
					.olatRootContainer(getFoldernodePathRelToFolderBase(course.getCourseEnvironment(), this), null);
			ZipUtil.unzipNonStrict(fFolderNodeZip, nodeContainer, owner, false);
		} else {
			// the whole folder can be moved back to the root directory of foldernodes
			// of this course
			File fFolderNodeDir = new File(FolderConfig.getCanonicalRoot() + getFoldernodePathRelToFolderBase(course.getCourseEnvironment(), this));
			fFolderNodeDir.mkdirs();
			FileUtils.copyDirContentsToDir(fFolderNodeData, fFolderNodeDir, true, "import course node");
		}
	}

	@Override
	public CourseNode createInstanceForCopy(boolean isNewTitle, ICourse course, Identity author) {
		CourseNode node = super.createInstanceForCopy(isNewTitle, course, author);
		if(node.getModuleConfiguration().getBooleanSafe(CONFIG_AUTO_FOLDER)){
			File fFolderNodeDir = new File(FolderConfig.getCanonicalRoot() + getFoldernodePathRelToFolderBase(course.getCourseEnvironment(), node));
			fFolderNodeDir.mkdirs();
		}
		return node;
	}

	@Override
	public void calcAccessAndVisibility(ConditionInterpreter ci, NodeEvaluation nodeEval) {
		if (hasCustomPreConditions()) {
			boolean uploadability = (getPreConditionUploaders().getConditionExpression() == null ? true : ci
					.evaluateCondition(getPreConditionUploaders()));
			nodeEval.putAccessStatus("upload", uploadability);
			boolean downloadability = (getPreConditionDownloaders().getConditionExpression() == null ? true : ci
					.evaluateCondition(getPreConditionDownloaders()));
			nodeEval.putAccessStatus("download", downloadability);

			boolean visible = (getPreConditionVisibility().getConditionExpression() == null ? true : ci
					.evaluateCondition(getPreConditionVisibility()));
			nodeEval.setVisible(visible);
		} else {
			super.calcAccessAndVisibility(ci, nodeEval);
		}
	}
	
	public boolean canDownload(NodeEvaluation ne) {
		if (hasCustomPreConditions()) {
			return ne != null? ne.isCapabilityAccessible("download"): false;
		}
		return true;
	}
	
	public boolean canUpload(UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		if (hasCustomPreConditions()) {
			return ne != null? ne.isCapabilityAccessible("upload"): false;
		} else if (
				(getModuleConfiguration().getBooleanSafe(CONFIG_KEY_UPLOAD_BY_COACH) && userCourseEnv.isCoach())
				|| (getModuleConfiguration().getBooleanSafe(CONFIG_KEY_UPLOAD_BY_PARTICIPANT) && userCourseEnv.isParticipant())
				) {
			return true;
		}
		return false;
	}
	
	/**
	 * The conditions to control the upload or download of files are deprecated. In
	 * new course nodes this options are controlled by module configurations.
	 * Existing course nodes may have preconditions. In that case they are still
	 * used for compatibility reasons.
	 *
	 * @return
	 */
	public boolean hasCustomPreConditions() {
		return preConditionDownloaders != null || preConditionUploaders != null;
	}

	public Condition getPreConditionDownloaders() {
		if (preConditionDownloaders == null) {
			preConditionDownloaders = new Condition();
		}
		preConditionDownloaders.setConditionId("downloaders");
		return preConditionDownloaders;
	}

	public void setPreConditionDownloaders(Condition preConditionDownloaders) {
		if (preConditionDownloaders == null) {
			preConditionDownloaders = getPreConditionDownloaders();
		}
		this.preConditionDownloaders = preConditionDownloaders;
		preConditionDownloaders.setConditionId("downloaders");
	}

	public Condition getPreConditionUploaders() {
		if (preConditionUploaders == null) {
			preConditionUploaders = new Condition();
		}
		preConditionUploaders.setConditionId("uploaders");
		return preConditionUploaders;
	}

	public void setPreConditionUploaders(Condition preConditionUploaders) {
		if (preConditionUploaders == null) {
			preConditionUploaders = getPreConditionUploaders();
		}
		preConditionUploaders.setConditionId("uploaders");
		this.preConditionUploaders = preConditionUploaders;
	}

	@Override
	public StatusDescription isConfigValid() {
		CourseNode parent = this.getParent() instanceof CourseNode? (CourseNode)this.getParent(): null;
		updateModuleConfigDefaults(false, parent);

		StatusDescription sd = StatusDescription.NOERROR;
		if(!getModuleConfiguration().getBooleanSafe(CONFIG_AUTO_FOLDER)){
			String subpath = getModuleConfiguration().getStringValue(CONFIG_SUBPATH,"");
			if(!StringHelper.containsNonWhitespace(subpath)){
				String shortKey = "error.missingfolder.short";
				String longKey = "error.missingfolder.long";
				String[] params = new String[] { this.getShortTitle() };
				String translPackage = Util.getPackageName(BCCourseNodeEditController.class);
				sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
				sd.setDescriptionForUnit(getIdent());
				// set which pane is affected by error
				sd.setActivateableViewIdentifier(BCCourseNodeEditController.PANE_TAB_FOLDER);
			}
		}

		if(oneClickStatusCache!=null) {
			return oneClickStatusCache[0];
		}
		return sd;
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		//only here we know which translator to take for translating condition error messages
		oneClickStatusCache = null;
		String translatorStr = Util.getPackageName(BCCourseNodeEditController.class);
		List<StatusDescription> statusDescs =isConfigValidWithTranslator(cev, translatorStr,getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(statusDescs);
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
	public String informOnDelete(Locale locale, ICourse course) {
		return new PackageTranslator(PACKAGE_BC, locale).translate("warn.folderdelete");
	}

	/**
	 * Delete the folder if node is deleted.
	 * 
	 * @see org.olat.course.nodes.CourseNode#cleanupOnDelete(org.olat.course.ICourse)
	 */
	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		
		// mark the subscription to this node as deleted
		SubscriptionContext folderSubContext = CourseModule.createTechnicalSubscriptionContext(course.getCourseEnvironment(), this);
		CoreSpringFactory.getImpl(NotificationsManager.class).delete(folderSubContext);
		// delete filesystem
		File fFolderRoot = new File(FolderConfig.getCanonicalRoot() + getFoldernodePathRelToFolderBase(course.getCourseEnvironment(), this));
		if (fFolderRoot.exists()) FileUtils.deleteDirsAndFiles(fFolderRoot, true, true);
	}
	
	@Override
	protected void postImportCopyConditions(CourseEnvironmentMapper envMapper) {
		super.postImportCopyConditions(envMapper);
		postImportCondition(preConditionUploaders, envMapper);
		postImportCondition(preConditionDownloaders, envMapper);
	}

	@Override
	public void postExport(CourseEnvironmentMapper envMapper, boolean backwardsCompatible) {
		super.postExport(envMapper, backwardsCompatible);
		postExportCondition(preConditionUploaders, envMapper, backwardsCompatible);
		postExportCondition(preConditionDownloaders, envMapper, backwardsCompatible);
	}

	@Override
	public List<ConditionExpression> getConditionExpressions() {
		if (hasCustomPreConditions()) {
			List<ConditionExpression> retVal;
			List<ConditionExpression> parentsConditions = super.getConditionExpressions();
			if (parentsConditions.size() > 0) {
				retVal = new ArrayList<>(parentsConditions);
			} else {
				retVal = new ArrayList<>();
			}
			
			String coS = getPreConditionDownloaders().getConditionExpression();
			if (coS != null && !coS.equals("")) {
				ConditionExpression ce = new ConditionExpression(getPreConditionDownloaders().getConditionId());
				ce.setExpressionString(getPreConditionDownloaders().getConditionExpression());
				retVal.add(ce);
			}
			
			coS = getPreConditionUploaders().getConditionExpression();
			if (coS != null && !coS.equals("")) {
				ConditionExpression ce = new ConditionExpression(getPreConditionUploaders().getConditionId());
				ce.setExpressionString(getPreConditionUploaders().getConditionExpression());
				retVal.add(ce);
			}
			
			return retVal;
		}
		
		return super.getConditionExpressions();
	}

	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent) {
		ModuleConfiguration config = getModuleConfiguration();
		int version = config.getConfigurationVersion();
		
		if (version < 2) {
			config.setBooleanEntry(CONFIG_AUTO_FOLDER, true);
			config.setStringValue(CONFIG_SUBPATH, "");
		}
		if (version < 3) {
			config.setBooleanEntry(CONFIG_KEY_UPLOAD_BY_COACH, true);
			config.setBooleanEntry(CONFIG_KEY_UPLOAD_BY_PARTICIPANT, false);
			removeDefaultPreconditions();
		}
		config.setConfigurationVersion(CURRENT_VERSION);
	}

	/**
	 * We don't want to have preConditions for upload and download. So we keep these
	 * preConditions only, if they have some special configs. Otherwise we delete
	 * them and use the regular configs.
	 */
	private void removeDefaultPreconditions() {
		if (hasCustomPreConditions()) {
			boolean defaultPreconditions =
					!preConditionUploaders.isExpertMode()
				&& preConditionUploaders.isEasyModeCoachesAndAdmins()
				&& !preConditionUploaders.isEasyModeAlwaysAllowCoachesAndAdmins()
				&& !preConditionUploaders.isAssessmentMode()
				&& !preConditionUploaders.isAssessmentModeViewResults()
				&& !preConditionDownloaders.isExpertMode()
				&& !preConditionDownloaders.isEasyModeCoachesAndAdmins()
				&& !preConditionDownloaders.isEasyModeAlwaysAllowCoachesAndAdmins()
				&& !preConditionDownloaders.isAssessmentMode()
				&& !preConditionDownloaders.isAssessmentModeViewResults();
			if (defaultPreconditions) {
				removeCustomPreconditions();
			}
		}
	}
	
	public void removeCustomPreconditions() {
		preConditionDownloaders = null;
		preConditionUploaders = null;
	}
}
