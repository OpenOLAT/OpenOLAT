/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipOutputStream;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.pf.PFModule;
import org.olat.course.nodes.pf.manager.FileSystemExport;
import org.olat.course.nodes.pf.manager.PFManager;
import org.olat.course.nodes.pf.ui.PFCoachController;
import org.olat.course.nodes.pf.ui.PFDefaultsEditController;
import org.olat.course.nodes.pf.ui.PFEditController;
import org.olat.course.nodes.pf.ui.PFParticipantController;
import org.olat.course.nodes.pf.ui.PFPeekviewController;
import org.olat.course.nodes.pf.ui.PFPreviewController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;

public class PFCourseNode extends AbstractAccessableCourseNode
		implements CourseNodeWithFiles, CourseNodeWithDefaults {
	
	public static final String TYPE = "pf";
	
	public static final String CONFIG_KEY_PARTICIPANTBOX = "participantbox";
	public static final String CONFIG_KEY_COACHBOX = "coachbox";
	public static final String CONFIG_KEY_ALTERFILE = "alterfile";
	public static final String CONFIG_KEY_LIMITCOUNT = "limitcount";
	public static final String CONFIG_KEY_FILECOUNT = "filecount";
	public static final String CONFIG_KEY_TIMEFRAME = "timeframe";
	public static final String CONFIG_KEY_DATESTART = "datestart";
	public static final String CONFIG_KEY_DATEEND = "dateend";
	public static final String CONFIG_KEY_TEMPLATE = "template";
	public static final String FOLDER_RETURN_BOX = "return.box";
	public static final String FOLDER_DROP_BOX = "drop.box";

	public static final long serialVersionUID = 1L;

	public PFCourseNode() {
		super(TYPE);
	}
	
	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCourse, CopyCourseContext context) {
		super.postCopy(envMapper, processType, course, sourceCourse, context);
		
		if (context != null) {
			ModuleConfiguration config = getModuleConfiguration();
			
			Date uploadStart = config.getDateValue(CONFIG_KEY_DATESTART);
			Date uploadEnd = config.getDateValue(CONFIG_KEY_DATEEND);
			
			if (uploadStart != null) {
				uploadStart.setTime(uploadStart.getTime() + context.getDateDifference(getIdent()));
				config.setDateValue(CONFIG_KEY_DATESTART, uploadStart);
			}
			
			if (uploadEnd != null) {
				uploadEnd.setTime(uploadEnd.getTime() + context.getDateDifference(getIdent()));
				config.setDateValue(CONFIG_KEY_DATEEND, uploadEnd);
			}
		}
	}

	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType, Identity doer) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType, doer);

		// PFModule has the default config values
		PFModule pfModule = CoreSpringFactory.getImpl(PFModule.class);

		if (isNewNode && pfModule != null) {
			// default is to enable both boxes without restrictions
			updateModuleConfig(pfModule.hasParticipantBox(), pfModule.hasCoachBox(), pfModule.canAlterFile(), pfModule.canLimitCount(), pfModule.getFileCount(), false, null, null);
		}
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}
	
	public void updateModuleConfig(boolean participantbox, boolean coachbox, boolean alterfile,
			boolean limitcount,	int filecount, boolean timeframe, Date start, Date end) {
		ModuleConfiguration config = getModuleConfiguration();
		
		config.setBooleanEntry(CONFIG_KEY_PARTICIPANTBOX, participantbox);
		config.setBooleanEntry(CONFIG_KEY_COACHBOX, coachbox);
		config.setBooleanEntry(CONFIG_KEY_ALTERFILE, alterfile);
		config.setBooleanEntry(CONFIG_KEY_LIMITCOUNT, limitcount);
		if (limitcount){
			config.set(CONFIG_KEY_FILECOUNT, filecount);
		}
		
		config.setBooleanEntry(CONFIG_KEY_TIMEFRAME, timeframe);
		if (timeframe) {
			config.set(CONFIG_KEY_DATESTART, start);
			config.set(CONFIG_KEY_DATEEND, end);	
		} else {
			config.remove(CONFIG_KEY_DATESTART);
			config.remove(CONFIG_KEY_DATEEND);	
		}
	}
	
	public boolean hasParticipantBoxConfigured() {
		return getModuleConfiguration().getBooleanSafe(CONFIG_KEY_PARTICIPANTBOX);
	}
	
	public boolean hasCoachBoxConfigured() {
		return getModuleConfiguration().getBooleanSafe(CONFIG_KEY_COACHBOX);
	}
	
	public boolean hasAlterFileConfigured() {
		boolean hasStundentBox = getModuleConfiguration().getBooleanSafe(CONFIG_KEY_PARTICIPANTBOX);
		if (hasStundentBox) {
			return getModuleConfiguration().getBooleanSafe(CONFIG_KEY_ALTERFILE);
		}
		return false;
	}
	
	public boolean hasLimitCountConfigured() {
		boolean hasStundentBox = getModuleConfiguration().getBooleanSafe(CONFIG_KEY_PARTICIPANTBOX);
		if (hasStundentBox) {
			return getModuleConfiguration().getBooleanSafe(CONFIG_KEY_LIMITCOUNT);
		}
		return false;
	}
	
	public boolean isGreaterOrEqualToLimit (int count) {
		ModuleConfiguration config = getModuleConfiguration();
		int limit = config.getBooleanEntry(CONFIG_KEY_FILECOUNT) != null ? 
				(int) config.get(CONFIG_KEY_FILECOUNT) : 0;				
		return count >= limit;			
	}
	
	public boolean hasDropboxTimeFrameConfigured() {
		boolean hasStundentBox = getModuleConfiguration().getBooleanSafe(CONFIG_KEY_PARTICIPANTBOX);
		if (hasStundentBox) {
			return getModuleConfiguration().getBooleanSafe(CONFIG_KEY_TIMEFRAME);
		}
		return false;
	}
	
	public boolean isInDropboxTimeFrame () {
		ModuleConfiguration config = getModuleConfiguration();
		Date start = config.getBooleanEntry(CONFIG_KEY_DATESTART) != null ? 
				config.getDateValue(CONFIG_KEY_DATESTART) : new Date();
		Date end = config.getBooleanEntry(CONFIG_KEY_DATEEND) != null ? 
				config.getDateValue(CONFIG_KEY_DATEEND) : new Date();
		Date current = new Date();
		
		return start.before(current) && end.after(current);		
	}
	
	
	public int getLimitCount() {
		ModuleConfiguration config = getModuleConfiguration();
		return config.getBooleanEntry(CONFIG_KEY_FILECOUNT) != null ? 
				(int) config.get(CONFIG_KEY_FILECOUNT) : 0;
	}
	
	public Date getDateStart() {
		ModuleConfiguration config = getModuleConfiguration();
		return config.getBooleanEntry(CONFIG_KEY_DATESTART) != null ? 
				config.getDateValue(CONFIG_KEY_DATESTART) : null;
	}
	
	public Date getDateEnd() {
		ModuleConfiguration config = getModuleConfiguration();
		return config.getBooleanEntry(CONFIG_KEY_DATEEND) != null ? 
				config.getDateValue(CONFIG_KEY_DATEEND) : null;
	}
	

	@Override
	public StatusDescription isConfigValid() {
		StatusDescription sd = StatusDescription.NOERROR;
		boolean isValid = hasCoachBoxConfigured() || hasParticipantBoxConfigured();
		if (!isValid) {
			String shortKey = "error.noreference.short";
			String longKey = "error.noreference.long";
			String[] params = new String[] { this.getShortTitle() };
			@SuppressWarnings("deprecation")
			String translPackage = Util.getPackageName(PFEditController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(CONFIG_KEY_PARTICIPANTBOX);
		}
		return sd;
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment euce) {
		PFEditController ordnerCtr = new PFEditController(ureq, wControl, this);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, ordnerCtr); 
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd, VisibilityFilter visibilityFilter) {
		Controller runCtrl;
		if (ureq.getUserSession().getRoles().isGuestOnly()) {
			runCtrl = MessageUIFactory.createGuestNoAccessMessage(ureq, wControl, null);
		} else if (userCourseEnv.isCoach() || userCourseEnv.isAdmin()) {
			runCtrl = new PFCoachController(ureq, wControl, this, userCourseEnv);
		} else if (userCourseEnv.getCourseEnvironment().getCourseGroupManager().isIdentityCourseParticipant(ureq.getIdentity())) {
			runCtrl = new PFParticipantController(ureq, wControl, this, userCourseEnv,
					userCourseEnv.getIdentityEnvironment().getIdentity(), null, false, false, false);
		} else {
			Translator trans = Util.createPackageTranslator(PFEditController.class, ureq.getLocale());
			String title = trans.translate("no.membership.title");
			String message = trans.translate("no.membership.message");
			runCtrl = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		}
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, runCtrl, userCourseEnv, this, "o_pf_icon");
		return new NodeRunConstructionResult(ctrl);
	}
	
	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, boolean small) {
		CourseEnvironment courseEnv = userCourseEnv.getCourseEnvironment();
		Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
		
		List<VFSContainer> rootFolder = new ArrayList<>();
		LocalFolderImpl baseContainer = courseEnv.getCourseBaseContainer();
		PFManager pfManager = CoreSpringFactory.getImpl(PFManager.class);
		if (userCourseEnv.isCoach() || userCourseEnv.isAdmin()) {
			List<Identity> participants = pfManager.getParticipants(identity, courseEnv, userCourseEnv.isAdmin());
			for(Identity participant:participants) {
				Path folderRelPath = Paths.get(baseContainer.getBasefile().toPath().toString(), 
						PFManager.FILENAME_PARTICIPANTFOLDER, getIdent(),
						pfManager.getIdFolderName(participant));
				rootFolder.add(new LocalFolderImpl(folderRelPath.toFile()));
			}
		} else if (userCourseEnv.isParticipant()) {
			Path folderRelPath = Paths.get(baseContainer.getBasefile().toPath().toString(), 
					PFManager.FILENAME_PARTICIPANTFOLDER, getIdent(), 
					pfManager.getIdFolderName(identity));
			rootFolder.add(new LocalFolderImpl(folderRelPath.toFile()));
		}
		
		if (rootFolder.isEmpty()) {
			return super.createPeekViewRunController(ureq, wControl, userCourseEnv, nodeSecCallback, small);
		}
		return new PFPeekviewController(ureq, wControl, rootFolder, getIdent(), 4);
	}
	
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		return new PFPreviewController(ureq, wControl, this, userCourseEnv);

	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		// mark the subscription to this node as deleted
		SubscriptionContext folderSubContext = CourseModule.createTechnicalSubscriptionContext(course.getCourseEnvironment(), this);
		CoreSpringFactory.getImpl(NotificationsManager.class).delete(folderSubContext);
		// delete filesystem
		
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		File root = Paths.get(courseEnv.getCourseBaseContainer().getRelPath(), 
				PFManager.FILENAME_PARTICIPANTFOLDER, getIdent()).toFile();
		if (root.exists()){
			FileUtils.deleteDirsAndFiles(root, true, true);		
		} 
	}
	
	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options,
			ZipOutputStream exportStream, String archivePath, String charset) {
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		Path sourceFolder = Paths.get(courseEnv.getCourseBaseContainer().getBasefile().getAbsolutePath(),
				PFManager.FILENAME_PARTICIPANTFOLDER, getIdent()); 
		Translator translator = Util.createPackageTranslator(PFParticipantController.class, locale);
		return FileSystemExport.fsToZip(exportStream, archivePath, sourceFolder, this, null, translator);
	}
	
	@Override
	public void archiveForResetUserData(UserCourseEnvironment assessedUserCourseEnv, ZipOutputStream archiveStream,
			String path, Identity doer, Role by) {
		CourseEnvironment courseEnv = assessedUserCourseEnv.getCourseEnvironment();
		Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		PFManager pfManager = CoreSpringFactory.getImpl(PFManager.class);
		
		VFSContainer dropContainer = pfManager.resolveDropFolder(courseEnv, this, assessedIdentity);
		if(dropContainer != null) {
			ZipUtil.zip(dropContainer, archiveStream, path + "/" + PFManager.FILENAME_DROPBOX, new VFSSystemItemFilter(), false);
		}
		VFSContainer returnContainer = pfManager.resolveReturnFolder(courseEnv, this, assessedIdentity);
		if(returnContainer != null) {
			ZipUtil.zip(returnContainer, archiveStream, path + "/" + PFManager.FILENAME_RETURNBOX, new VFSSystemItemFilter(), false);
		}
		super.archiveForResetUserData(assessedUserCourseEnv, archiveStream, path, doer, by);
	}

	@Override
	public void resetUserData(UserCourseEnvironment assessedUserCourseEnv, Identity identity, Role by) {
		super.resetUserData(assessedUserCourseEnv, identity, by);

		CourseEnvironment courseEnv = assessedUserCourseEnv.getCourseEnvironment();
		Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		PFManager pfManager = CoreSpringFactory.getImpl(PFManager.class);
		
		VFSContainer dropContainer = pfManager.resolveDropFolder(courseEnv, this, assessedIdentity);
		VFSManager.deleteContainersAndLeaves(dropContainer, true, false, true);
		VFSContainer returnContainer = pfManager.resolveReturnFolder(courseEnv, this, assessedIdentity);
		VFSManager.deleteContainersAndLeaves(returnContainer, true, false, true);
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		return new StatusDescription[]{StatusDescription.NOERROR};
	}
	
	@Override
	public List<Entry<String, DueDateConfig>> getNodeSpecificDatesWithLabel() {
		return List.of(
				Map.entry("participant.folder.upload.start", DueDateConfig.absolute(getModuleConfiguration().getDateValue(CONFIG_KEY_DATESTART))),
				Map.entry("participant.folder.upload.end", DueDateConfig.absolute(getModuleConfiguration().getDateValue(CONFIG_KEY_DATEEND)))
			);
	}

	/**
	 * @param courseEnv
	 * @param node
	 * @return the relative folder base path for this folder node
	 */
	public static String getPFNodePathRelToFolderBase(CourseEnvironment courseEnv, CourseNode node) {
		return getPFNodesPathRelToFolderBase(courseEnv) + "/" + node.getIdent();
	}

	/**
	 * @param courseEnv
	 * @return the relative folder base path for folder nodes
	 */
	public static String getPFNodesPathRelToFolderBase(CourseEnvironment courseEnv) {
		return courseEnv.getCourseBaseContainer().getRelPath() + "/participantfolder";
	}

	/**
	 *
	 * @param node
	 * @param courseEnv
	 * @return
	 */
	public static VFSContainer getPFFolderContainer(PFCourseNode node, CourseEnvironment courseEnv) {
		String path = getPFNodePathRelToFolderBase(courseEnv, node);
		VFSContainer rootFolder = VFSManager.olatRootContainer(path, null);
		return new NamedContainerImpl(node.getShortTitle(), rootFolder);
	}

	@Override
	public Quota getQuota(Identity identity, Roles roles, RepositoryEntry entry, QuotaManager quotaManager) {
		Quota courseElementQuota = null;
		if (quotaManager != null) {
			VFSContainer participantFolder = VFSManager.getOrCreateContainer(CourseFactory.loadCourse(entry).getCourseBaseContainer(), "participantfolder");
			if (participantFolder != null) {
				courseElementQuota = quotaManager.getCustomQuotaOrDefaultDependingOnRole(identity, roles, participantFolder.getRelPath() + "/" + this.getIdent());
			}
		}
		return courseElementQuota;
	}

	@Override
	public Long getUsageKb(CourseEnvironment courseEnvironment) {
		return VFSManager.getUsageKB(getNodeContainer(courseEnvironment));
	}

	@Override
	public String getRelPath(CourseEnvironment courseEnvironment) {
		return getNodeContainer(courseEnvironment).getRelPath();
	}

	@Override
	public Integer getNumOfFiles(CourseEnvironment courseEnvironment) {
		return null;
	}

	private VFSContainer getNodeContainer(CourseEnvironment courseEnvironment) {
		return getPFFolderContainer(this, courseEnvironment);
	}

	@Override
	public boolean isStorageExtern() {
		return false;
	}

	@Override
	public boolean isStorageInCourseFolder() {
		return false;
	}

	@Override
	public Controller createDefaultsController(UserRequest ureq, WindowControl wControl) {
		Controller controller;
		controller = new PFDefaultsEditController(ureq, wControl);
		return controller;
	}

	@Override
	public String getCourseNodeConfigManualUrl() {
		return  "manual_user/learningresources/Course_Element_Participant_Folder/#folder-settings";
	}
}
