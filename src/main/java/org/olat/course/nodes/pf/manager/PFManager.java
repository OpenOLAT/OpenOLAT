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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VirtualContainer;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.course.CourseModule;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.nodes.pf.ui.DropBoxRow;
import org.olat.course.nodes.pf.ui.PFRunController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
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
	
	private static final OLog log = Tracing.createLoggerFor(PFManager.class);
	
	public static final String FILENAME_PARTICIPANTFOLDER = "participantfolder";
	public static final String FILENAME_RETURNBOX = "returnbox";
	public static final String FILENAME_DROPBOX = "dropbox";

	@Autowired
	private	BusinessGroupService groupService;
	@Autowired
	private UserManager userManager;
	
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
		OlatRootFolderImpl baseContainer = courseEnv.getCourseBaseContainer();
		VFSContainer dropboxContainer = VFSManager.resolveOrCreateContainerFromPath(baseContainer, relPath.toString());
		return dropboxContainer;
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
		OlatRootFolderImpl baseContainer = courseEnv.getCourseBaseContainer();
		VFSContainer returnboxContainer = VFSManager.resolveOrCreateContainerFromPath(baseContainer, relPath.toString());
		return returnboxContainer;
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
			List<VFSItem> children = vfsContainer.getItems();
			for (VFSItem vfsItem : children) {
				if (vfsItem instanceof VFSContainer){
					counter += countFiles((VFSContainer)vfsItem);
				} else {
					String filename = vfsItem.getName();
					String suffix = FileUtils.getFileSuffix(filename);
					if (!filename.startsWith(".") && !"zip".equals(suffix)){
						counter++;					
					}					
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
		OlatRootFolderImpl baseContainer = courseEnv.getCourseBaseContainer();
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
		if (lastUpdated.size() > 0) {
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
				VFSManager.copyContent(uploadFile, uploadedFile);
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
			VFSManager.copyContent(uploadFile, uploadedFile);
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
	 * Export media resource as folder download.
	 *
	 * @param ureq
	 * @param identities
	 * @param pfNode 
	 * @param courseEnv 
	 */
	public MediaResource exportMediaResource (UserRequest ureq, List<Identity> identities, PFCourseNode pfNode, CourseEnvironment courseEnv) {
		MediaResource resource = new FileSystemExport (identities, pfNode, courseEnv);
		ureq.getDispatchResult().setResultingMediaResource(resource);
		return resource;
	}
	
	/**
	 * Calculate callback dependent on CourseModule Settings.
	 *
	 * @param pfNode 
	 * @param dropbox
	 * @return the VFSSecurityCallback
	 */
	private VFSSecurityCallback calculateCallback (CourseEnvironment courseEnv, PFCourseNode pfNode, VFSContainer dropbox) {
		VFSSecurityCallback callback;
		SubscriptionContext folderSubContext = CourseModule.createSubscriptionContext(courseEnv, pfNode);
		int count = countFiles(dropbox);
		boolean limitCount = pfNode.hasLimitCountConfigured() && pfNode.isGreaterOrEqualToLimit(count);
		boolean timeFrame = pfNode.hasDropboxTimeFrameConfigured() && !pfNode.isInDropboxTimeFrame();
		boolean alterFile = pfNode.hasAlterFileConfigured();
		if (timeFrame || limitCount && !alterFile){
			callback = new ReadOnlyCallback(folderSubContext);
		} else if (limitCount && alterFile) {
			callback = new ReadDeleteCallback(folderSubContext);
		} else if (!limitCount && !alterFile) {
			callback = new ReadWriteCallback(folderSubContext);
		} else {
			callback = new ReadWriteDeleteCallback(folderSubContext);
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
	public VFSContainer provideCoachOrParticipantContainer (PFCourseNode pfNode, UserCourseEnvironment userCourseEnv, Identity identity) {	
		VFSContainer vfsContainer = null;
		if (userCourseEnv.isCoach() || userCourseEnv.isAdmin()) {
			vfsContainer = provideCoachContainer(pfNode, userCourseEnv.getCourseEnvironment(), identity);
		} else if (userCourseEnv.isParticipant()) {
			vfsContainer = provideParticipantContainer(pfNode, userCourseEnv, identity);
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
	private VFSContainer provideParticipantContainer (PFCourseNode pfNode, UserCourseEnvironment userCourseEnv, Identity identity) {
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(PFRunController.class, locale);
		CourseEnvironment courseEnv = userCourseEnv.getCourseEnvironment();
		boolean readOnly = userCourseEnv.isCourseReadOnly();
		SubscriptionContext subsContext = CourseModule.createSubscriptionContext(courseEnv, pfNode);
		String path = courseEnv.getCourseBaseContainer().getRelPath() + "/" + FILENAME_PARTICIPANTFOLDER;
		VFSContainer courseElementBaseContainer = new OlatRootFolderImpl(path, null);
		VirtualContainer namedCourseFolder = new VirtualContainer(identity.getName());
		Path relPath = Paths.get(pfNode.getIdent(), getIdFolderName(identity));
		VFSContainer userBaseContainer = VFSManager.resolveOrCreateContainerFromPath(courseElementBaseContainer, relPath.toString());		
		if (pfNode.hasParticipantBoxConfigured()){
			VFSContainer dropContainer = new NamedContainerImpl(translator.translate("drop.box"),
					VFSManager.resolveOrCreateContainerFromPath(userBaseContainer, FILENAME_DROPBOX));
			if (readOnly) {
				dropContainer.setLocalSecurityCallback(new ReadOnlyCallback(subsContext));
			} else {
				VFSContainer dropbox = resolveOrCreateDropFolder(courseEnv, pfNode, identity);
				VFSSecurityCallback callback = calculateCallback(courseEnv, pfNode, dropbox);
				dropContainer.setLocalSecurityCallback(callback);
			}
			namedCourseFolder.addItem(dropContainer);
		}		
		if (pfNode.hasCoachBoxConfigured()){
			VFSContainer returnContainer = new NamedContainerImpl(translator.translate("return.box"),
					VFSManager.resolveOrCreateContainerFromPath(userBaseContainer, FILENAME_RETURNBOX));
			returnContainer.setLocalSecurityCallback(new ReadOnlyCallback(subsContext));
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
	private VFSContainer provideCoachContainer (PFCourseNode pfNode, CourseEnvironment courseEnv, Identity identity) {
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage());
		Translator translator = Util.createPackageTranslator(PFRunController.class, locale);
		SubscriptionContext nodefolderSubContext = CourseModule.createSubscriptionContext(courseEnv, pfNode);
		List<Identity> participants =  getParticipants(identity, courseEnv); 
		String path = courseEnv.getCourseBaseContainer().getRelPath() + "/" + FILENAME_PARTICIPANTFOLDER;
		VFSContainer courseElementBaseContainer = new OlatRootFolderImpl(path, null);
		VirtualContainer namedCourseFolder = new VirtualContainer(identity.getName());
		for (Identity participant : participants) {
			Path relPath = Paths.get(pfNode.getIdent(), getIdFolderName(participant));
			VFSContainer userBaseContainer = VFSManager.resolveOrCreateContainerFromPath(courseElementBaseContainer, relPath.toString());
			VirtualContainer participantFolder = new VirtualContainer(participant.getName());
			namedCourseFolder.addItem(participantFolder);
			
			if (pfNode.hasParticipantBoxConfigured()){
				VFSContainer dropContainer = new NamedContainerImpl(translator.translate("drop.box"),
						VFSManager.resolveOrCreateContainerFromPath(userBaseContainer, FILENAME_DROPBOX));
				if (identity.equals(participant)){
					VFSContainer dropbox = resolveOrCreateDropFolder(courseEnv, pfNode, identity);
					VFSSecurityCallback callback = calculateCallback(courseEnv, pfNode, dropbox);
					dropContainer.setLocalSecurityCallback(callback);
				} else {
					dropContainer.setLocalSecurityCallback(new ReadOnlyCallback(nodefolderSubContext));
				}
				participantFolder.addItem(dropContainer);
			}
			
			if (pfNode.hasCoachBoxConfigured()){
				VFSContainer returnContainer = new NamedContainerImpl(translator.translate("return.box"),
						VFSManager.resolveOrCreateContainerFromPath(userBaseContainer, FILENAME_RETURNBOX));
				returnContainer.setLocalSecurityCallback(new ReadWriteDeleteCallback(nodefolderSubContext));
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
	public VFSContainer provideParticipantFolder (PFCourseNode pfNode, PFView pfView,
			CourseEnvironment courseEnv, Identity identity, boolean isCoach, boolean readOnly) {
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(identity.getUser().getPreferences().getLanguage());
		SubscriptionContext nodefolderSubContext = CourseModule.createSubscriptionContext(courseEnv, pfNode);
		Translator translator = Util.createPackageTranslator(PFRunController.class, locale);
		
		String path = courseEnv.getCourseBaseContainer().getRelPath() + "/" + FILENAME_PARTICIPANTFOLDER;
		VFSContainer courseElementBaseContainer = new OlatRootFolderImpl(path, null);

		Path relPath = Paths.get(pfNode.getIdent(), getIdFolderName(identity));
		
		VFSContainer userBaseContainer = VFSManager.resolveOrCreateContainerFromPath(courseElementBaseContainer, relPath.toString());
		
		String baseContainerName = userManager.getUserDisplayName(identity);
		
		VirtualContainer namedCourseFolder = new VirtualContainer(baseContainerName);
		namedCourseFolder.setLocalSecurityCallback(new ReadOnlyCallback(nodefolderSubContext));

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
		

		if (readOnly) {
			dropContainer.setLocalSecurityCallback(new ReadOnlyCallback(nodefolderSubContext));
			returnContainer.setLocalSecurityCallback(new ReadOnlyCallback(nodefolderSubContext));
		} else {
			if (isCoach) {
				dropContainer.setLocalSecurityCallback(new ReadOnlyCallback(nodefolderSubContext));
				returnContainer.setLocalSecurityCallback(new ReadWriteDeleteCallback(nodefolderSubContext));
			} else {
				VFSContainer dropbox = resolveOrCreateDropFolder(courseEnv, pfNode, identity);
				VFSSecurityCallback callback = calculateCallback(courseEnv, pfNode, dropbox);
				dropContainer.setLocalSecurityCallback(callback);
				returnContainer.setLocalSecurityCallback(new ReadOnlyCallback(nodefolderSubContext));
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
	
	/**
	 * Gets the participants for different group or course coaches.
	 *
	 * @param id the identity
	 * @param pfNode 
	 * @param locale 
	 * @param courseEnv
	 * @return the participants
	 */
	public List<Identity> getParticipants (Identity id, CourseEnvironment courseEnv) {
		CourseGroupManager groupManager = courseEnv.getCourseGroupManager();
		Set<Identity> identitiesSet = new HashSet<>();
		// iterate course members
		if (groupManager.isIdentityCourseCoach(id) || groupManager.isIdentityCourseAdministrator(id)) {
			List<Identity> identities = groupManager.getParticipants();
			for (Identity identity : identities) {
				identitiesSet.add(identity);
			}
		}
		//iterate group members
		List<BusinessGroup> bgroups = groupManager.getOwnedBusinessGroups(id);
		if (bgroups != null) {
			for (BusinessGroup bgroup : bgroups) {
				List<Identity> identities = groupService.getMembers(bgroup, GroupRoles.participant.toString());
				for (Identity identity : identities) {
					identitiesSet.add(identity);
				}
			}
		}
		List<Identity> participants = identitiesSet.stream().collect(Collectors.toList());
		return participants;
	}
	
	/**
	 * Gets the participants for different group or course coaches as TableModel. 
	 *
	 * @param id the identity
	 * @param pfNode 
	 * @param userPropertyHandlers 
	 * @param locale 
	 * @param courseEnv
	 * @return the participants
	 */
	public List<DropBoxRow> getParticipants (Identity id, PFCourseNode pfNode, List<UserPropertyHandler> userPropertyHandlers, 
			Locale locale, CourseEnvironment courseEnv) {
		Map<Identity, DropBoxRow> participantsMap = new HashMap<>();
		CourseGroupManager groupManager = courseEnv.getCourseGroupManager();
		// iterate course members
		if (groupManager.isIdentityCourseCoach(id) || groupManager.isIdentityCourseAdministrator(id)) {
			List<Identity> identities = groupManager.getParticipants();
			for (Identity identity : identities) {
				VFSContainer dropbox = resolveOrCreateDropFolder(courseEnv, pfNode, identity);
				int filecount = countFiles(dropbox);
				VFSContainer returnbox = resolveOrCreateReturnFolder(courseEnv, pfNode, identity);
				int filecountR = countFiles(returnbox);
				Date lastModified = getLastUpdated(courseEnv, pfNode, identity, FILENAME_DROPBOX);
				Date lastModifiedR = getLastUpdated(courseEnv, pfNode, identity, FILENAME_RETURNBOX);
				UserPropertiesRow urow = new UserPropertiesRow(identity, userPropertyHandlers, locale);
				participantsMap.put(identity, new DropBoxRow(urow, "status",
						filecount, filecountR, pfNode.getLimitCount(), lastModified, lastModifiedR));		

			}
		}
		//iterate group members
		List<BusinessGroup> bgroups = groupManager.getOwnedBusinessGroups(id);
		if (bgroups != null) {
			for (BusinessGroup bgroup : bgroups) {
				List<Identity> identities = groupService.getMembers(bgroup, GroupRoles.participant.toString());
				for (Identity identity : identities) {
					VFSContainer dropbox = resolveOrCreateDropFolder(courseEnv, pfNode, identity);
					int filecount = countFiles(dropbox);
					VFSContainer returnbox = resolveOrCreateReturnFolder(courseEnv, pfNode, identity);
					int filecountR = countFiles(returnbox);
					Date lastModified = getLastUpdated(courseEnv, pfNode, identity, FILENAME_DROPBOX);
					Date lastModifiedR = getLastUpdated(courseEnv, pfNode, identity, FILENAME_RETURNBOX);
					UserPropertiesRow urow = new UserPropertiesRow(identity, userPropertyHandlers, locale);
					participantsMap.put(identity, new DropBoxRow(urow, "status",
							filecount, filecountR, pfNode.getLimitCount(), lastModified, lastModifiedR));
				
				}
			}
		}
		List<DropBoxRow> participants = new ArrayList<>(participantsMap.values());
		return participants;		
	}
	
	/**
	 * Provide pfView dependent on ModuleConfiguration.
	 *
	 * @param pfNode 
	 * @return the PF view
	 */
	public PFView providePFView (PFCourseNode pfNode) {
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
