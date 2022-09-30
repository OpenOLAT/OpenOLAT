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
package org.olat.course.nodes.pf.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VirtualContainer;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.course.CourseModule;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.nodes.pf.ui.DropBoxRow;
import org.olat.course.nodes.pf.ui.PFParticipantController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.user.UserManager;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
*
* Initial date: 09.12.2016<br>
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
@Service
public class PFManager {
	
	private static final Logger log = Tracing.createLoggerFor(PFManager.class);
	private static final VFSItemFilter attachmentExcludeFilter = new VFSSystemItemFilter();

	public static final String FILENAME_PARTICIPANTFOLDER = "participantfolder";
	public static final String FILENAME_RETURNBOX = "returnbox";
	public static final String FILENAME_DROPBOX = "dropbox";

	@Autowired
	private UserManager userManager;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private AssessmentService assessmentService;
	
	/**
	 * Resolve an existing drop folder or return null
	 * 
	 * @param courseEnv The course environment
	 * @param pfNode The course element
	 * @param identity The identity which can drop files
	 * @return
	 */
	public VFSContainer resolveDropFolder(CourseEnvironment courseEnv, PFCourseNode pfNode, Identity identity) {
		Path relPath = Paths.get(FILENAME_PARTICIPANTFOLDER, pfNode.getIdent(), getIdFolderName(identity), FILENAME_DROPBOX); 
		VFSContainer baseContainer = courseEnv.getCourseBaseContainer();
		VFSItem dropboxContainer = baseContainer.resolve(relPath.toString());
		return dropboxContainer instanceof VFSContainer ? (VFSContainer)dropboxContainer : null;
	}
	
	/**
	 * Resolve or create drop folder.
	 *
	 * @param courseEnv
	 * @param pfNode 
	 * @param identity
	 * @return the VFSContainer
	 */
	private VFSContainer resolveOrCreateDropFolder(CourseEnvironment courseEnv, PFCourseNode pfNode, Identity identity) {
		Path relPath = Paths.get(FILENAME_PARTICIPANTFOLDER, pfNode.getIdent(), getIdFolderName(identity), FILENAME_DROPBOX); 
		VFSContainer baseContainer = courseEnv.getCourseBaseContainer();
		return VFSManager.resolveOrCreateContainerFromPath(baseContainer, relPath.toString());
	}

	/**
	 * Resolve or create return folder.
	 *
	 * @param courseEnv
	 * @param pfNode 
	 * @param identity 
	 * @return the VFSContainer
	 */
	private VFSContainer resolveOrCreateReturnFolder(CourseEnvironment courseEnv, PFCourseNode pfNode, Identity identity) {
		Path relPath = Paths.get(FILENAME_PARTICIPANTFOLDER, pfNode.getIdent(), getIdFolderName(identity), FILENAME_RETURNBOX); 
		VFSContainer baseContainer = courseEnv.getCourseBaseContainer();
		return VFSManager.resolveOrCreateContainerFromPath(baseContainer, relPath.toString());
	}

	/**
	 * Count files recursively for each participant.
	 *
	 * @param vfsContainer the root folder
	 * @return the count
	 */
	private int countFiles(VFSContainer vfsContainer) {
		int counter = 0;
		if (vfsContainer.exists()) {
			List<VFSItem> children = vfsContainer.getItems(attachmentExcludeFilter);
			for (VFSItem vfsItem : children) {
				if (vfsItem instanceof VFSContainer){
					counter += countFiles((VFSContainer)vfsItem);
				} else {
					counter++;										
				}
			}
		}
		return counter;
	}
	
	
	/**
	 * Gets the last updated file for each participant.
	 *
	 * @param courseEnv 
	 * @param pfNode 
	 * @param identity 
	 * @return the last updated file as Date
	 */
	private Date getLastUpdated(CourseEnvironment courseEnv, PFCourseNode pfNode, Identity identity, String fileName) {
		Date latest = null;
		List<Long> lastUpdated = new ArrayList<>();
		LocalFolderImpl baseContainer = courseEnv.getCourseBaseContainer();
		Path path = Paths.get(baseContainer.getBasefile().toPath().toString(), FILENAME_PARTICIPANTFOLDER,
				pfNode.getIdent(), getIdFolderName(identity), fileName);
		try {
			Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					lastUpdated.add(attrs.lastModifiedTime().toMillis());
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			log.error("Unknown IOE",e);
		}
		Collections.sort(lastUpdated);
		if (!lastUpdated.isEmpty()) {
			latest = new Date(lastUpdated.get(lastUpdated.size() - 1));
		}
		return latest;
	}

	/**
	 * Upload file to drop box.
	 *
	 * @param uploadFile
	 * @param fileName
	 * @param limitFileCount
	 * @param courseEnv
	 * @param pfNode 
	 * @param identity
	 * @return true, if successful
	 */
	public boolean uploadFileToDropBox(File uploadFile, String fileName, int limitFileCount, 
			CourseEnvironment courseEnv, PFCourseNode pfNode, Identity identity) {
		if (uploadFile.exists() && uploadFile.isFile() && uploadFile.length() > 0){
			VFSContainer dropbox = resolveOrCreateDropFolder(courseEnv, pfNode, identity);
			int fileCount = countFiles(dropbox);
			if (fileCount <= limitFileCount) {
				VFSLeaf uploadedFile = dropbox.createChildLeaf(fileName);
				VFSManager.copyContent(uploadFile, uploadedFile, identity);
				return true;
			}
			
		}
		return false;
	}
	
	/**
	 * Upload file to return box.
	 *
	 * @param uploadFile
	 * @param fileName 
	 * @param courseEnv
	 * @param pfNode
	 * @param identity
	 */
	private void uploadFileToReturnBox(File uploadFile, String fileName, CourseEnvironment courseEnv,
			PFCourseNode pfNode, Identity identity) {
		if (uploadFile.exists() && uploadFile.isFile() && uploadFile.length() > 0) {
			VFSContainer dropbox = resolveOrCreateReturnFolder(courseEnv, pfNode, identity);
			VFSLeaf uploadedFile = dropbox.createChildLeaf(fileName);
			VFSManager.copyContent(uploadFile, uploadedFile, identity);
		}
	}
	
	/**
	 * Upload file to all return boxes of a given list of identities.
	 *
	 * @param uploadFile
	 * @param fileName 
	 * @param courseEnv
	 * @param pfNode 
	 * @param identities 
	 */
	public void uploadFileToAllReturnBoxes (File uploadFile, String fileName, CourseEnvironment courseEnv,
			PFCourseNode pfNode, List<Identity> identities) {
		for(Identity identity : identities){
			uploadFileToReturnBox(uploadFile, fileName, courseEnv, pfNode, identity);			
		}
	}
	
	/**
	 * Calculate callback dependent on ModuleConfiguration.
	 *
	 * @param pfNode 
	 * @param dropbox
	 * @return the VFSSecurityCallback
	 */
	private VFSSecurityCallback calculateCallback(CourseEnvironment courseEnv, String quotaPath, PFCourseNode pfNode, VFSContainer dropbox, boolean webdav) {
		VFSSecurityCallback callback;
		SubscriptionContext folderSubContext = CourseModule.createSubscriptionContext(courseEnv, pfNode);
		int count = countFiles(dropbox);
		boolean limitCount = pfNode.hasLimitCountConfigured() && pfNode.isGreaterOrEqualToLimit(count);
		boolean timeFrame = pfNode.hasDropboxTimeFrameConfigured() && !pfNode.isInDropboxTimeFrame();
		boolean alterFile = pfNode.hasAlterFileConfigured();
		if (timeFrame || limitCount && !alterFile){
			callback = new ReadOnlyCallback(folderSubContext, quotaPath);
		} else if (webdav) {
			callback= new CountingCallback(folderSubContext, dropbox, pfNode.getLimitCount(), alterFile);
		} else if (limitCount && alterFile) {
			callback = new ReadDeleteCallback(folderSubContext);
		} else if (!limitCount && !alterFile) {
			callback = new ReadWriteCallback(folderSubContext, quotaPath);
		} else {
			callback = new ReadWriteDeleteCallback(folderSubContext, quotaPath);
		}
		return callback;
	}

	
	/**
	 * Provide coach or participant view for webdav.
	 *
	 * @param pfNode 
	 * @param userCourseEnv
	 * @param identity 
	 * @return the VFSContainer
	 */
	public VFSContainer provideCoachOrParticipantContainer (PFCourseNode pfNode, UserCourseEnvironment userCourseEnv,
			Identity identity, boolean courseReadOnly) {	
		VFSContainer vfsContainer = null;
		if (userCourseEnv.isCoach() || userCourseEnv.isAdmin()) {
			vfsContainer = provideCoachContainer(pfNode, userCourseEnv.getCourseEnvironment(), identity, userCourseEnv.isAdmin());
		} else if (userCourseEnv.isParticipant()) {
			vfsContainer = provideParticipantContainer(pfNode, userCourseEnv.getCourseEnvironment(), identity, courseReadOnly);
		}
		return vfsContainer;
	}
	
	/**
	 * Provide participant view in webdav.
	 *
	 * @param pfNode
	 * @param courseEnv 
	 * @param identity
	 * @return the VFS container
	 */
	private VFSContainer provideParticipantContainer (PFCourseNode pfNode, CourseEnvironment courseEnv,
			Identity identity, boolean courseReadOnly) {
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(PFParticipantController.class, locale);
		SubscriptionContext subsContext = CourseModule.createSubscriptionContext(courseEnv, pfNode);
		String path = courseEnv.getCourseBaseContainer().getRelPath() + "/" + FILENAME_PARTICIPANTFOLDER;
		String quotaPath = path + "/" + pfNode.getIdent();
		VFSContainer courseElementBaseContainer = VFSManager.olatRootContainer(path, null);
		VirtualContainer namedCourseFolder = new VirtualContainer(identity.getName());
		Path relPath = Paths.get(pfNode.getIdent(), getIdFolderName(identity));
		VFSContainer userBaseContainer = VFSManager.resolveOrCreateContainerFromPath(courseElementBaseContainer, relPath.toString());		
		if (pfNode.hasParticipantBoxConfigured()){
			VFSContainer dropContainer = new NamedContainerImpl(translator.translate("drop.box"),
					VFSManager.resolveOrCreateContainerFromPath(userBaseContainer, FILENAME_DROPBOX));
			if (courseReadOnly) {
				dropContainer.setLocalSecurityCallback(new ReadOnlyCallback(subsContext, quotaPath));
			} else {
				VFSContainer dropbox = resolveOrCreateDropFolder(courseEnv, pfNode, identity);
				VFSSecurityCallback callback = calculateCallback(courseEnv, quotaPath, pfNode, dropbox, true);
				dropContainer.setLocalSecurityCallback(callback);
			}
			namedCourseFolder.addItem(dropContainer);
		}		
		if (pfNode.hasCoachBoxConfigured()){
			VFSContainer returnContainer = new NamedContainerImpl(translator.translate("return.box"),
					VFSManager.resolveOrCreateContainerFromPath(userBaseContainer, FILENAME_RETURNBOX));
			returnContainer.setLocalSecurityCallback(new ReadOnlyCallback(subsContext, quotaPath));
			namedCourseFolder.addItem(returnContainer);
		}
		return namedCourseFolder;
	}
	
	/**
	 * Provide coach view in webdav.
	 *
	 * @param pfNode 
	 * @param courseEnv
	 * @param identity
	 * @return the VFSContainer
	 */
	private VFSContainer provideCoachContainer (PFCourseNode pfNode, CourseEnvironment courseEnv, Identity identity, boolean admin) {
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(PFParticipantController.class, locale);
		SubscriptionContext nodefolderSubContext = CourseModule.createSubscriptionContext(courseEnv, pfNode);
		List<Identity> participants =  getParticipants(identity, courseEnv, admin);
		String courseContainerRelPath = courseEnv.getCourseBaseContainer().getRelPath();
		String path = courseContainerRelPath + "/" + FILENAME_PARTICIPANTFOLDER;
		String quotaPath = path + "/" + pfNode.getIdent();
		VFSContainer courseElementBaseContainer = VFSManager.olatRootContainer(path, null);
		VirtualContainer namedCourseFolder = new VirtualContainer(translator.translate("participant.folder"));
		for (Identity participant : participants) {
			Path relPath = Paths.get(pfNode.getIdent(), getIdFolderName(participant));
			VFSContainer userBaseContainer = VFSManager.resolveOrCreateContainerFromPath(courseElementBaseContainer, relPath.toString());
			String participantfoldername = userManager.getUserDisplayName(participant);
			VirtualContainer participantFolder = new VirtualContainer(participantfoldername);
			namedCourseFolder.addItem(participantFolder);
			
			if (pfNode.hasParticipantBoxConfigured()){
				VFSContainer dropContainer = new NamedContainerImpl(translator.translate("drop.box"),
						VFSManager.resolveOrCreateContainerFromPath(userBaseContainer, FILENAME_DROPBOX));
				//if coach is also participant, can user his/her webdav folder with participant rights
				if (identity.equals(participant)){
					VFSContainer dropbox = resolveOrCreateDropFolder(courseEnv, pfNode, identity);
					VFSSecurityCallback callback = calculateCallback(courseEnv, quotaPath, pfNode, dropbox, true);
					dropContainer.setLocalSecurityCallback(callback);
				} else {
					dropContainer.setLocalSecurityCallback(new ReadOnlyCallback(nodefolderSubContext, quotaPath));
				}
				participantFolder.addItem(dropContainer);
			}
			
			if (pfNode.hasCoachBoxConfigured()){
				VFSContainer returnContainer = new NamedContainerImpl(translator.translate("return.box"),
						VFSManager.resolveOrCreateContainerFromPath(userBaseContainer, FILENAME_RETURNBOX));
				returnContainer.setLocalSecurityCallback(new ReadWriteDeleteCallback(nodefolderSubContext, quotaPath));
				participantFolder.addItem(returnContainer);
			}
		}
		
		return namedCourseFolder;
	}
	
	/**
	 * Provide admin view for webdav, contains all participants of the course.
	 *
	 * @param pfNode the pf node
	 * @param courseEnv the course env
	 * @return the VFS container
	 */
	public VFSContainer provideAdminContainer (PFCourseNode pfNode, CourseEnvironment courseEnv) {
		Translator translator = Util.createPackageTranslator(PFParticipantController.class, I18nModule.getDefaultLocale());
		SubscriptionContext nodefolderSubContext = CourseModule.createSubscriptionContext(courseEnv, pfNode);
		RepositoryEntry re = courseEnv.getCourseGroupManager().getCourseEntry();
		List<Identity> participants =  repositoryEntryRelationDao.getMembers(re, 
				RepositoryEntryRelationType.all, GroupRoles.participant.name());
		participants = new ArrayList<>(new HashSet<>(participants));
		
		String path = courseEnv.getCourseBaseContainer().getRelPath() + "/" + FILENAME_PARTICIPANTFOLDER;
		String quotaPath = path + "/" + pfNode.getIdent();
		VFSContainer courseElementBaseContainer = VFSManager.olatRootContainer(path, null);
		VirtualContainer namedCourseFolder = new VirtualContainer(translator.translate("participant.folder"));
		for (Identity participant : participants) {
			Path relPath = Paths.get(pfNode.getIdent(), getIdFolderName(participant));
			VFSContainer userBaseContainer = VFSManager.resolveOrCreateContainerFromPath(courseElementBaseContainer, relPath.toString());
			String participantfoldername = userManager.getUserDisplayName(participant);
			VirtualContainer participantFolder = new VirtualContainer(participantfoldername);
			participantFolder.setParentContainer(namedCourseFolder);
			namedCourseFolder.addItem(participantFolder);
			
			if (pfNode.hasParticipantBoxConfigured()) {
				VFSContainer dropContainer = new NamedContainerImpl(translator.translate("drop.box"),
						VFSManager.resolveOrCreateContainerFromPath(userBaseContainer, FILENAME_DROPBOX));
				dropContainer.setLocalSecurityCallback(new ReadOnlyCallback(nodefolderSubContext, quotaPath));
				participantFolder.addItem(dropContainer);
			}
			
			if (pfNode.hasCoachBoxConfigured()){
				VFSContainer returnContainer = new NamedContainerImpl(translator.translate("return.box"),
						VFSManager.resolveOrCreateContainerFromPath(userBaseContainer, FILENAME_RETURNBOX));
				returnContainer.setLocalSecurityCallback(new ReadWriteDeleteCallback(nodefolderSubContext, quotaPath));
				participantFolder.addItem(returnContainer);
			}
		}		
		return namedCourseFolder;
	}
	

	
	/**
	 * Provide participant folder in GUI.
	 *
	 * @param pfNode 
	 * @param pfView 
	 * @param courseEnv
	 * @param identity
	 * @param isCoach 
	 * @return the VFS container
	 */
	public VFSContainer provideParticipantFolder (PFCourseNode pfNode, PFView pfView, Translator translator,
			CourseEnvironment courseEnv, Identity identity, boolean isCoach, boolean readOnly) {
		SubscriptionContext nodefolderSubContext = CourseModule.createSubscriptionContext(courseEnv, pfNode);
		
		String path = courseEnv.getCourseBaseContainer().getRelPath() + "/" + FILENAME_PARTICIPANTFOLDER;
		String quotaPath = path + "/" + pfNode.getIdent();
		VFSContainer courseElementBaseContainer = VFSManager.olatRootContainer(path, null);

		Path relPath = Paths.get(pfNode.getIdent(), getIdFolderName(identity));
		
		VFSContainer userBaseContainer = VFSManager.resolveOrCreateContainerFromPath(courseElementBaseContainer, relPath.toString());
		
		String baseContainerName = userManager.getUserDisplayName(identity);
		
		VirtualContainer namedCourseFolder = new VirtualContainer(baseContainerName);
		namedCourseFolder.setLocalSecurityCallback(new ReadOnlyCallback(nodefolderSubContext, quotaPath));

		VFSContainer dropContainer = new NamedContainerImpl(PFView.onlyDrop.equals(pfView) || PFView.onlyReturn.equals(pfView) ? 
				baseContainerName : translator.translate("drop.box"), 
				VFSManager.resolveOrCreateContainerFromPath(userBaseContainer, FILENAME_DROPBOX));

		if (pfNode.hasParticipantBoxConfigured()){
			namedCourseFolder.addItem(dropContainer);
		}
		
		VFSContainer returnContainer = new NamedContainerImpl(PFView.onlyDrop.equals(pfView) || PFView.onlyReturn.equals(pfView) ? 
				baseContainerName : translator.translate("return.box"),
				VFSManager.resolveOrCreateContainerFromPath(userBaseContainer, FILENAME_RETURNBOX));

		if (pfNode.hasCoachBoxConfigured()){
			namedCourseFolder.addItem(returnContainer);
		}

		// Generate folders from template
        syncModuleConfigWithVFSContainer(pfNode, namedCourseFolder);

		if (readOnly) {
			dropContainer.setLocalSecurityCallback(new ReadOnlyCallback(nodefolderSubContext, null));
			returnContainer.setLocalSecurityCallback(new ReadOnlyCallback(nodefolderSubContext, null));
		} else {
			if (isCoach) {
				dropContainer.setLocalSecurityCallback(new ReadOnlyCallback(nodefolderSubContext, quotaPath));
				returnContainer.setLocalSecurityCallback(new ReadWriteDeleteCallback(nodefolderSubContext, quotaPath));
			} else {
				VFSContainer dropbox = resolveOrCreateDropFolder(courseEnv, pfNode, identity);
				VFSSecurityCallback callback = calculateCallback(courseEnv, quotaPath, pfNode, dropbox, false);
				dropContainer.setLocalSecurityCallback(callback);
				returnContainer.setLocalSecurityCallback(new ReadOnlyCallback(nodefolderSubContext, quotaPath));
			}
		}
		
		VFSContainer folderRunContainer;
		switch (pfView) {
		case dropAndReturn: 
			folderRunContainer = namedCourseFolder;			
			break;
		case onlyDrop: 
			folderRunContainer = dropContainer;			
			break;
		case onlyReturn: 
			folderRunContainer = returnContainer;			
			break;
		default: 
			folderRunContainer = namedCourseFolder;
			break;
		}
		
		return folderRunContainer;
	}

	private void syncModuleConfigWithVFSContainer(PFCourseNode pfNode, VirtualContainer namedCourseFolder) {
    	List<String> folders;
    	ModuleConfiguration moduleConfiguration = pfNode.getModuleConfiguration();

    	if (moduleConfiguration.get(PFCourseNode.CONFIG_KEY_TEMPLATE) != null) {
    		folders = new ArrayList<>(Arrays.asList(moduleConfiguration.get(PFCourseNode.CONFIG_KEY_TEMPLATE).toString().split(",")));
			// sanitize
			folders.removeIf(f -> f.startsWith("/"));
			folders.removeIf(f -> !FileUtils.validateFilename(f.replaceAll(".+?/", "")));
			Collections.sort(folders);

			for(String folder : folders){
				List<String> path = new ArrayList<>(Arrays.asList(folder.split("/")));

				if (path.get(0).equals(PFCourseNode.FOLDER_DROP_BOX) && namedCourseFolder.getItems().stream().anyMatch(b -> b.getMetaInfo().getFilename().equals(FILENAME_DROPBOX))) {
					folder = folder.replace(PFCourseNode.FOLDER_DROP_BOX + "/", "");
					VFSManager.resolveOrCreateContainerFromPath(
							(VFSContainer) Objects.requireNonNull(namedCourseFolder
									.getItems()
									.stream()
									.filter(b -> b.getMetaInfo().getFilename().equals(FILENAME_DROPBOX))
									.findFirst()
									.orElse(null), "DropBox does not exist."), folder);
				} else if (path.get(0).equals(PFCourseNode.FOLDER_RETURN_BOX) && namedCourseFolder.getItems().stream().anyMatch(b -> b.getMetaInfo().getFilename().equals(FILENAME_RETURNBOX))) {
					folder = folder.replace(PFCourseNode.FOLDER_RETURN_BOX + "/", "");
					VFSManager.resolveOrCreateContainerFromPath(
							(VFSContainer) Objects.requireNonNull(namedCourseFolder
									.getItems()
									.stream()
									.filter(b -> b.getMetaInfo().getFilename().equals(FILENAME_RETURNBOX))
									.findFirst()
									.orElse(null), "ReturnBox does not exist."), folder);
				}
			}
    	}
	}
	
	public List<DropBoxRow> getParticipants(ParticipantSearchParams params, PFCourseNode pfNode,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale, CourseEnvironment courseEnv) {
		List<Identity> allIdentities;
		if ((params.getBusinessGroupRefs() != null && !params.getBusinessGroupRefs().isEmpty())
				|| (params.getCurriculumElements() != null && !params.getCurriculumElements().isEmpty())) {
			allIdentities = new ArrayList<>(32);
			if (params.getBusinessGroupRefs() != null && !params.getBusinessGroupRefs().isEmpty()) {
				List<Identity> identityList = businessGroupRelationDao.getMembers(params.getBusinessGroupRefs(),
						GroupRoles.participant.name());
				allIdentities.addAll(identityList);
			}
			if (params.getCurriculumElements() != null && !params.getCurriculumElements().isEmpty()) {
				List<Identity> identityElementList = curriculumElementDao.getMembers(params.getCurriculumElements(),
						GroupRoles.participant.name());
				allIdentities.addAll(identityElementList);
			}
		} else {
			allIdentities = getParticipants(params.getIdentity(), courseEnv, params.isAdmin());
		}
		
		if (params.getAssessmentObligations() != null) {
			List<Long> identityObligationFiltered = assessmentService.getIdentityKeys(
					courseEnv.getCourseGroupManager().getCourseEntry(), pfNode.getIdent(),
					params.getAssessmentObligations());
			allIdentities.removeIf(identity -> !identityObligationFiltered.contains(identity.getKey()));
		}
		
		return getParticipants(pfNode, userPropertyHandlers, locale, allIdentities, courseEnv);
	}
	
	public List<Identity> getParticipants(Identity id, CourseEnvironment courseEnv, boolean admin) {
		Set<Identity> identitySet = new HashSet<>();
		RepositoryEntry re = courseEnv.getCourseGroupManager().getCourseEntry();
		if(admin) {
			List<Identity> participants = repositoryEntryRelationDao.getMembers(re, RepositoryEntryRelationType.all, GroupRoles.participant.name());
			identitySet.addAll(participants);
		} else {
			List<Identity> participants = repositoryEntryRelationDao.getRelatedMembers(re, id, GroupRoles.coach, GroupRoles.participant);
			identitySet.addAll(participants);
		}
		
		// deduplicate list (participants from groups and direct course membership)
		return identitySet.stream().collect(Collectors.toList());
	}
	
	private List<DropBoxRow> getParticipants(PFCourseNode pfNode, List<UserPropertyHandler> userPropertyHandlers, 
			Locale locale, List<Identity> identityList, CourseEnvironment courseEnv) {

		Set<Identity> duplicates = new HashSet<>();
		List<DropBoxRow> participants = new ArrayList<>(identityList.size());
		for (Identity identity : identityList) {
			if(duplicates.contains(identity)) {
				continue;
			}
			duplicates.add(identity);

			VFSContainer dropbox = resolveOrCreateDropFolder(courseEnv, pfNode, identity);
			int filecount = countFiles(dropbox);
			VFSContainer returnbox = resolveOrCreateReturnFolder(courseEnv, pfNode, identity);
			int filecountR = countFiles(returnbox);
			Date lastModified = getLastUpdated(courseEnv, pfNode, identity, FILENAME_DROPBOX);
			Date lastModifiedR = getLastUpdated(courseEnv, pfNode, identity, FILENAME_RETURNBOX);
			UserPropertiesRow urow = new UserPropertiesRow(identity, userPropertyHandlers, locale);
			participants.add(new DropBoxRow(urow, "status",
					filecount, filecountR, pfNode.getLimitCount(), lastModified, lastModifiedR));
		}
		return participants;		
	}
	
	/**
	 * Provide pfView dependent on ModuleConfiguration.
	 *
	 * @param pfNode 
	 * @return the PF view
	 */
	public PFView providePFView(PFCourseNode pfNode) {
		boolean hasParticipantBox = pfNode.hasParticipantBoxConfigured();
		boolean hasCoachBox = pfNode.hasCoachBoxConfigured();
		PFView pfView = PFView.dropAndReturn;
		if (hasParticipantBox && hasCoachBox) {
			pfView = PFView.dropAndReturn;
		} else if (!hasParticipantBox && hasCoachBox) {
			pfView = PFView.onlyReturn;
		} else if (hasParticipantBox && !hasCoachBox) {
			pfView = PFView.onlyDrop;
		} else if (!hasParticipantBox && !hasCoachBox) {
			pfView = PFView.nothingToDisplay;
		}
		return pfView;
	}
	
	/**
	 * Gets the id folder name.
	 *
	 * @param identity the identity
	 * @return the id folder name
	 */
	public String getIdFolderName(Identity identity) {
		return identity.getKey().toString();
	}

}
