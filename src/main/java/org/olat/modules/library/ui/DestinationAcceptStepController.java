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
package org.olat.modules.library.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.tree.MenuTreeItem;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSContainerFilter;
import org.olat.modules.library.LibraryManager;
import org.olat.modules.library.LibraryModule;
import org.olat.modules.library.ui.comparator.FilenameComparator;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Select the destination or the destinations of the reviewed document.<br>
 * Events fired:
 * <ul>
 * 	<li>ACTIVATE_NEXT</li>
 * </ul>
 * <P>
 * Initial Date:  2 oct. 2009 <br>
 *
 * @author twuersch, srosse
 */
public class DestinationAcceptStepController extends StepFormBasicController {
	
	public static final String STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_KEY = "destination";
	public static final String STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_MODEL_KEY = "destination_model";

	private MenuTreeItem treeMultipleSelectionElement;
	private final LibraryTreeModel treeModel;
	private VFSLeaf targetFile;

	@Autowired
	private MailManager mailManager;
	@Autowired
	private LibraryModule libraryModule;
	@Autowired
	private LibraryManager libraryManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private RepositoryService repositoryService;
	
	public DestinationAcceptStepController(UserRequest ureq, WindowControl wControl, StepsRunContext stepsRunContext, Form rootForm) {
		super(ureq, wControl, rootForm, stepsRunContext, LAYOUT_VERTICAL, null);
		
		if(containsRunContextKey(STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_MODEL_KEY)) {
			treeModel = (LibraryTreeModel)getFromRunContext(STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_MODEL_KEY);
		} else {
			VFSContainer folder = libraryManager.getSharedFolder();
			if (folder == null) throw new OLATRuntimeException("no library-folder setup. you cannot publish items as long as no ressource-folder is configured!" , null);
			treeModel = new LibraryTreeModel(folder, new VFSContainerFilter(), new FilenameComparator(getLocale()), getLocale(), false);
			treeModel.getRootNode().setTitle(translate("main.menu.title"));
			treeModel.setIconCssClass("b_filetype_folder");
		}		
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormUIFactory formUIFactory = FormUIFactory.getInstance();
		treeMultipleSelectionElement = formUIFactory.addTreeMultiselect("acceptstep.destination.treename", null, formLayout, treeModel, this);
		treeMultipleSelectionElement.setMultiSelect(true);

		TreeNode rootNode = treeModel.getRootNode();
		for(int i=rootNode.getChildCount(); i-->0; ) {
			treeMultipleSelectionElement.open((TreeNode)rootNode.getChildAt(i));
		}

		if (containsRunContextKey(STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_KEY)) {
			@SuppressWarnings("unchecked")
			Set<String> selection = (Set<String>) getFromRunContext(STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_KEY);
			for (String key : selection) {
				treeMultipleSelectionElement.select(key, true);
			}
		}
	}

	@Override
	protected void formNext(UserRequest ureq) {
		Collection<String> selection = treeMultipleSelectionElement.getSelectedKeys();
		if (selection.isEmpty()) {
			showError("acceptstep.destination.noselectionerror");
		} else {
			addToRunContext(STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_KEY, selection);
			addToRunContext(STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_MODEL_KEY, treeModel);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// formOk is only relevant when approval is not needed in this wizard
		if (!libraryModule.isApprovalEnabled()) {
			try {
				// get the source file name.
				String relativeSourceFileName = (String) getFromRunContext(MetadataAcceptStepController.STEPS_RUN_CONTEXT_FILENAME_KEY);

				// get the relative destination file name and the selection.
				Set<String> selection = treeMultipleSelectionElement.getSelectedKeys();

				// for all selected destination folders
				processToDestinationFolders(selection, relativeSourceFileName);

				// Send notification email
				sendNotificationEmail(targetFile);

				// notify user
				showInfo("library.uploadnotification.approval.success", relativeSourceFileName);
				fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			} catch (Exception e) {
				logError("Exception while reading source folder.", e);
				showError("acceptstep.notification.ioexception");
				fireEvent(ureq, Event.FAILED_EVENT);
			}

			fireEvent(ureq, StepsEvent.INFORM_FINISHED);
		}
	}

	private void processToDestinationFolders(Set<String> selection, String relativeSourceFileName) {
		VFSContainer rootContainer = VFSManager.olatRootContainer("", null);

		// Get the uploadFileEl from the run context and the corresponding file
		FileElement fileUploadEl = (FileElement) getFromRunContext(MetadataAcceptStepController.STEP_RUN_CONTEXT_FILE_UPLOAD_EL_KEY);
		File fileToUpload = fileUploadEl.getUploadFile();

		// Iterate through the selected destination folders
		for (String key : selection) {
			TreeNode selectedNode = treeModel.getNodeById(key);

			String relativeDestinationDirectoryName = (String) selectedNode.getUserObject();
			VFSContainer destinationDirectory = (VFSContainer) rootContainer.resolve(relativeDestinationDirectoryName);

			// Try to create the target file in the destination directory
			targetFile = destinationDirectory.createChildLeaf(relativeSourceFileName);

			if (targetFile == null) {
				// File already exists... upload anyway with a new filename
				String newName = VFSManager.rename(destinationDirectory, fileUploadEl.getUploadFileName());
				targetFile = destinationDirectory.createChildLeaf(newName);
			}

			// Copy the file to the destination directory
			try (InputStream in = new FileInputStream(fileToUpload);
				 OutputStream out = targetFile.getOutputStream(false)) {
				// Copy file content from source to destination
				FileUtils.cpio(in, out, "uploadTmpFileToDestFile");
			} catch (IOException e) {
				// Log an error if the upload fails
				logError("Uploading file failed", e);
			}

			// Update metadata for the copied file
			updateFileMetadata(targetFile, relativeDestinationDirectoryName, relativeSourceFileName);
		}

		// Clean up the temporary uploaded file
		FileUtils.deleteFile(fileToUpload);
	}

	/**
	 * In MetaInfoForm the meta data got populated, now pass that data to the targetFile and save it
	 * @param targetFile
	 * @param relativeDestinationDirectoryName
	 * @param relativeSourceFileName
	 */
	private void updateFileMetadata(VFSLeaf targetFile, String relativeDestinationDirectoryName,
									String relativeSourceFileName) {
		VFSMetadata metaInfo = targetFile.getMetaInfo();
		metaInfo.copyValues((VFSMetadata) getFromRunContext(MetadataAcceptStepController.STEPS_RUN_CONTEXT_METADATA_KEY), true);
		metaInfo = vfsRepositoryService.updateMetadata(metaInfo);
		vfsRepositoryService.itemSaved(targetFile, getIdentity());

		if (metaInfo == null) {
			logError("Error writing metadata for " + relativeDestinationDirectoryName + "/" + relativeSourceFileName, null);
			showError("acceptstep.notification.metafileerror");
		}
	}

	private void sendNotificationEmail(VFSLeaf sourceFile) {
		String body = translate("library.upload.success.body", getIdentity().getName(), sourceFile.getName());
		String subject = translate("library.upload.success.subject");

		Object recipients = getRecipients();

		if (recipients instanceof String recipient) {
			sendEmailToNotifyAfterUpload(recipient, subject, body);
		} else if (recipients instanceof List<?>) {
			@SuppressWarnings("unchecked")
			List<ContactList> contactLists = (List<ContactList>) recipients;
			sendEmailToSharedFolderOwners(contactLists, getIdentity(), subject, body);
		}
	}

	private void sendEmailToNotifyAfterUpload(String recipient, String subject, String body) {
		// Prepare and send email to a single recipient
		MailBundle bundle = new MailBundle();
		bundle.setTo(recipient);
		bundle.setFrom(WebappHelper.getMailConfig("mailReplyTo"));
		bundle.setContent(subject, body);
		mailManager.sendMessage(bundle);
	}

	private void sendEmailToSharedFolderOwners(List<ContactList> contactLists, Identity senderIdentity, String subject, String body) {
		// Prepare and send email to a list of shared folder owners
		MailBundle bundle = new MailBundle();
		bundle.setFromId(senderIdentity);
		bundle.setContactLists(contactLists);
		bundle.setContent(subject, body);
		mailManager.sendMessage(bundle);
	}

	private Object getRecipients() {
		String emailContacts = libraryModule.getEmailContactsToNotifyAfterUpload();

		if (StringHelper.containsNonWhitespace(emailContacts)) {
			// Return the configured email contact string
			return emailContacts;
		} else {
			// Retrieve repository entry and its owners
			RepositoryEntry repositoryEntry = libraryManager.getCatalogRepoEntry();
			List<Identity> sharedFolderOwners = repositoryService.getMembers(
					repositoryEntry, RepositoryEntryRelationType.all, GroupRoles.owner.name());

			// Convert owners to a list of contact lists
			return buildContactLists(sharedFolderOwners);
		}
	}

	private List<ContactList> buildContactLists(List<Identity> sharedFolderOwners) {
		List<ContactList> recipients = new ArrayList<>();
		for (Identity identity : sharedFolderOwners) {
			ContactList contactList = new ContactList(identity.getName());
			contactList.add(identity);
			recipients.add(contactList);
		}
		return recipients;
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		super.event(ureq, source, event);
		addToRunContext(STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_KEY, treeMultipleSelectionElement.getSelectedKeys());
		addToRunContext(STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_MODEL_KEY, treeModel);
	}
}