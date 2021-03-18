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

package org.olat.core.commons.modules.bc;

import static java.util.Arrays.asList;

import java.util.List;

import org.olat.core.commons.controllers.linkchooser.FileLinkChooserController;
import org.olat.core.commons.controllers.linkchooser.LinkChooserController;
import org.olat.core.commons.controllers.linkchooser.URLChoosenEvent;
import org.olat.core.commons.modules.bc.commands.FolderCommand;
import org.olat.core.commons.modules.bc.commands.FolderCommandStatus;
import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.commons.services.vfs.VFSVersionModule;
import org.olat.core.commons.services.vfs.ui.version.RevisionListController;
import org.olat.core.commons.services.vfs.ui.version.VersionCommentController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.ButtonClickedEvent;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.VFSManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * <P>
 * Initial Date:  18 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class FileCopyController extends LinkChooserController {
	private final FolderComponent folderComponent;
	
	private DialogBoxController lockedFileDialog;
	private DialogBoxController overwriteDialog;
	private RevisionListController revisionListCtr;
	private CloseableModalController revisionListDialogBox;
	private VersionCommentController commentVersionCtr;
	private CloseableModalController commentVersionDialogBox;
	private VersionCommentController unlockCtr;
	private CloseableModalController unlockDialogBox;
	
	private VFSLeaf newFile;
	private VFSLeaf sourceLeaf;
	private VFSLeaf existingVFSItem;
	private String renamedFilename;
	
	@Autowired
	private VFSLockManager vfsLockManager;
	@Autowired
	private VFSVersionModule versionsModule;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public FileCopyController(UserRequest ureq, WindowControl wControl, VFSContainer rootDir,
			FolderComponent folderComponent) {
		super(ureq, wControl, rootDir, null, null, null, false, "", null, null, true);
		this.folderComponent = folderComponent;
	}
	
	@Override
	//this is a hack to overwrite the package used by the BasicController
	protected VelocityContainer createVelocityContainer(String page) {
		Translator fallbackTranslator = Util.createPackageTranslator(FileCopyController.class, getLocale());
		setTranslator(Util.createPackageTranslator(LinkChooserController.class, getLocale(), fallbackTranslator));
		velocity_root = Util.getPackageVelocityRoot(LinkChooserController.class);
		return super.createVelocityContainer(page);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {	
		if(source instanceof FileLinkChooserController) {
			if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT){
				fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
			} else if (event instanceof URLChoosenEvent) {
				URLChoosenEvent choosenEvent = (URLChoosenEvent)event;
				String url = choosenEvent.getURL();
				if(url.indexOf("://") < 0) {
					VFSContainer cContainer = folderComponent.getExternContainerForCopy();
					VFSItem item = cContainer.resolve(url);
					if(item instanceof VFSLeaf) {
						sourceLeaf = (VFSLeaf)item;
						String filename = sourceLeaf.getName();
						VFSContainer tContainer = folderComponent.getCurrentContainer();
						newFile = tContainer.createChildLeaf(filename);
						if(newFile == null) {
							existingVFSItem = (VFSLeaf)tContainer.resolve(filename);
							fileAlreadyExists(ureq);
						} else {
							finishUpload(ureq);
						}
					} else {
						fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
					}
				} else {
					fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
				}
			}
		} else if (source == overwriteDialog) {
			if (event instanceof ButtonClickedEvent) {
				ButtonClickedEvent buttonClickedEvent = (ButtonClickedEvent) event;
				if (buttonClickedEvent.getPosition() == 0) { //ok
					if (existingVFSItem.canVersion() == VFSConstants.YES) {
						//new version
						int maxNumOfRevisions = versionsModule.getMaxNumberOfVersions();
						if(maxNumOfRevisions == 0) {
							//someone play with the configuration
							// Overwrite...
							String fileName = existingVFSItem.getName();
							existingVFSItem.delete();
							newFile = folderComponent.getCurrentContainer().createChildLeaf(fileName);
							// ... and notify listeners.
							finishUpload(ureq);
						} else {
							
							removeAsListenerAndDispose(commentVersionCtr);
							
							boolean locked = vfsLockManager.isLocked(existingVFSItem, null, null);
							commentVersionCtr = new VersionCommentController(ureq,getWindowControl(), locked, true);
							listenTo(commentVersionCtr);
							
							removeAsListenerAndDispose(commentVersionDialogBox);
							commentVersionDialogBox = new CloseableModalController(getWindowControl(), translate("save"), commentVersionCtr.getInitialComponent());
							listenTo(commentVersionDialogBox);
							
							commentVersionDialogBox.activate();
						}
					} else {
						//if the file is locked, ask for unlocking it
						if(vfsLockManager.isLocked(existingVFSItem, null, null)) {
							
							removeAsListenerAndDispose(unlockCtr);
							unlockCtr = new VersionCommentController(ureq,getWindowControl(), true, false);
							listenTo(unlockCtr);
							
							removeAsListenerAndDispose(unlockDialogBox);
							unlockDialogBox = new CloseableModalController(getWindowControl(), translate("ok"), unlockCtr.getInitialComponent());
							listenTo(unlockDialogBox);
							
							unlockDialogBox.activate();
							
						} else {
							// Overwrite...
							String fileName = existingVFSItem.getName();
							existingVFSItem.delete();
							newFile = folderComponent.getCurrentContainer().createChildLeaf(fileName);
							// ... and notify listeners.
							finishUpload(ureq);
						}
					}
				} else if (buttonClickedEvent.getPosition() == 1) { //not ok
					//make newFile with the proposition of filename
					newFile = folderComponent.getCurrentContainer().createChildLeaf(renamedFilename);
				// ... and notify listeners.
					finishUpload(ureq);
				} else if (buttonClickedEvent.getPosition() == 2) { // cancel
					//cancel -> do nothing

				} else {
					throw new RuntimeException("Unknown button number " + buttonClickedEvent.getPosition());
				}
			}
		} else if (source == lockedFileDialog) {
			if (event instanceof ButtonClickedEvent) {
				ButtonClickedEvent buttonClickedEvent = (ButtonClickedEvent) event;
				switch(buttonClickedEvent.getPosition()) {
					case 0: {
						// ... and notify listeners.
						newFile = existingVFSItem;
						finishUpload(ureq);
						break;
					}
					case 1: {//cancel
						fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
						break;
					}
					default:
						throw new RuntimeException("Unknown button number " + buttonClickedEvent.getPosition());
				}
			}
		} else if (source == commentVersionCtr) {
			String comment = commentVersionCtr.getComment();
			boolean locked = vfsLockManager.isLocked(existingVFSItem, VFSLockApplicationType.vfs, null);
			if(locked && !commentVersionCtr.keepLocked()) {
				vfsLockManager.unlock(existingVFSItem, VFSLockApplicationType.vfs);
			}
			
			commentVersionDialogBox.deactivate();
			if(revisionListDialogBox != null) {
				revisionListDialogBox.deactivate();
			}
			
			//ok, new version of the file
			boolean ok = vfsRepositoryService.addVersion(existingVFSItem, ureq.getIdentity(), false, comment, sourceLeaf.getInputStream());
			if(ok) {
				newFile = existingVFSItem;
			}
			finishSuccessfullUpload(existingVFSItem.getName(), ureq);
		} else if (source == unlockCtr) {
			// Overwrite...
			if(!unlockCtr.keepLocked()) {
				vfsLockManager.unlock(existingVFSItem, VFSLockApplicationType.vfs);
			}
			
			unlockDialogBox.deactivate();
			
			newFile = existingVFSItem;
			// ... and notify listeners.
			finishSuccessfullUpload(existingVFSItem.getName(), ureq);
			
		} else if (source == revisionListCtr) {
			if(FolderCommandStatus.STATUS_CANCELED == revisionListCtr.getStatus()) {
	
				revisionListDialogBox.deactivate();
	
				//don't want to delete revisions
				fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
			} else {
				if (existingVFSItem.canVersion() == VFSConstants.YES) {

					revisionListDialogBox.deactivate();
	
					int maxNumOfRevisions = versionsModule.getMaxNumberOfVersions();
					VFSMetadata metadata = vfsRepositoryService.getMetadataFor(existingVFSItem);
					List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
					if(maxNumOfRevisions < 0 || maxNumOfRevisions > revisions.size()) {
						
						removeAsListenerAndDispose(commentVersionCtr);
						boolean locked = vfsLockManager.isLocked(existingVFSItem, VFSLockApplicationType.vfs, null);
						commentVersionCtr = new VersionCommentController(ureq,getWindowControl(), locked, true);
						listenTo(commentVersionCtr);
						
						removeAsListenerAndDispose(commentVersionDialogBox);
						commentVersionDialogBox = new CloseableModalController(getWindowControl(), translate("save"), commentVersionCtr.getInitialComponent());
						listenTo(commentVersionDialogBox);
						
						commentVersionDialogBox.activate();
						
					} else {
						
						removeAsListenerAndDispose(revisionListCtr);
						revisionListCtr = new RevisionListController(ureq,getWindowControl(), existingVFSItem, false);
						listenTo(revisionListCtr);
						
						removeAsListenerAndDispose(revisionListDialogBox);
						revisionListDialogBox = new CloseableModalController(getWindowControl(), translate("delete"), revisionListCtr.getInitialComponent());
						listenTo(revisionListDialogBox);
						
						revisionListDialogBox.activate();
					}
				}
			}
		}
	}
	
	private void finishUpload(UserRequest ureq) {
		VFSManager.copyContent(sourceLeaf, newFile, true, ureq.getIdentity());
		finishSuccessfullUpload(newFile.getName(), ureq);
	}
	
	private void finishSuccessfullUpload(String fileName, UserRequest ureq) {
		ThreadLocalUserActivityLogger.log(FolderLoggingAction.FILE_COPIED, getClass(), CoreLoggingResourceable.wrapUploadFile(fileName));
		// Notify listeners about upload
		fireEvent(ureq, new FolderEvent(FolderEvent.NEW_FILE_EVENT, newFile.getName()));
		fireEvent(ureq, FolderCommand.FOLDERCOMMAND_FINISHED);
	}
	
	private void fileAlreadyExists(UserRequest ureq) {
		renamedFilename =  proposedRenamedFilename(existingVFSItem);
		boolean locked = vfsLockManager.isLockedForMe(existingVFSItem, getIdentity(), VFSLockApplicationType.vfs, null);
		if (locked) {
			//the file is locked and cannot be overwritten
			removeAsListenerAndDispose(lockedFileDialog);
			lockedFileDialog = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), translate("ul.lockedFile.title"),
					translate("ul.lockedFile.text", new String[] {existingVFSItem.getName(), renamedFilename} ),
					asList(translate("ul.overwrite.threeoptions.rename", renamedFilename), translate("ul.overwrite.threeoptions.cancel")));
			listenTo(lockedFileDialog);
			lockedFileDialog.activate();
		} else if (existingVFSItem.canVersion() == VFSConstants.YES) {

			int maxNumOfRevisions = versionsModule.getMaxNumberOfVersions();
			VFSMetadata metadata = vfsRepositoryService.getMetadataFor(existingVFSItem);
			List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
			if(maxNumOfRevisions == 0) {
				//it's possible if someone change the configuration
				// let calling method decide what to do.
				removeAsListenerAndDispose(overwriteDialog);
				overwriteDialog = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), translate("ul.overwrite.threeoptions.title"),
						translate("ul.overwrite.threeoptions.text", new String[] {existingVFSItem.getName(), renamedFilename} ),
						asList(translate("ul.overwrite.threeoptions.overwrite"), translate("ul.overwrite.threeoptions.rename", renamedFilename),
								translate("ul.overwrite.threeoptions.cancel")));
				listenTo(overwriteDialog);
				
				overwriteDialog.activate();
				
			} else if(revisions.isEmpty() || maxNumOfRevisions < 0 || maxNumOfRevisions > revisions.size()) {
				// let calling method decide what to do.
				removeAsListenerAndDispose(overwriteDialog);
				overwriteDialog = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), translate("ul.overwrite.threeoptions.title"),
						translate("ul.versionoroverwrite", new String[] {existingVFSItem.getName(), renamedFilename} ),
						asList(translate("ul.overwrite.threeoptions.newVersion"), translate("ul.overwrite.threeoptions.rename", renamedFilename),
						translate("ul.overwrite.threeoptions.cancel")));
				listenTo(overwriteDialog);
				
				overwriteDialog.activate();
				
			} else {
			
				String title = translate("ul.tooManyRevisions.title", new String[]{Integer.toString(maxNumOfRevisions), Integer.toString(revisions.size())});
				String description = translate("ul.tooManyRevisions.description", new String[]{Integer.toString(maxNumOfRevisions), Integer.toString(revisions.size())});
				
				removeAsListenerAndDispose(revisionListCtr);
				revisionListCtr = new RevisionListController(ureq, getWindowControl(), existingVFSItem, null, description, false);
				listenTo(revisionListCtr);
				
				removeAsListenerAndDispose(revisionListDialogBox);
				revisionListDialogBox = new CloseableModalController(getWindowControl(), translate("delete"), revisionListCtr.getInitialComponent(), true, title);
				listenTo(revisionListDialogBox);
				
				revisionListDialogBox.activate();
			}
		} else {
			// let calling method decide what to do.
			// for this, we put a list with "existing name" and "new name"
			overwriteDialog = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), translate("ul.overwrite.threeoptions.title"),
					translate("ul.overwrite.threeoptions.text", new String[] {existingVFSItem.getName(), renamedFilename} ),
					asList(translate("ul.overwrite.threeoptions.overwrite"), translate("ul.overwrite.threeoptions.rename", renamedFilename),
					translate("ul.overwrite.threeoptions.cancel")));
			listenTo(overwriteDialog);
			overwriteDialog.activate();
		}
	}
	
	private String proposedRenamedFilename(VFSLeaf file) {
		String currentName = file.getName();
		for(int i=1; i<999; i++) {
			String proposition = FileUtils.appendNumberAtTheEndOfFilename(currentName, i);
			VFSItem item = folderComponent.getCurrentContainer().resolve(proposition);
			if(item == null) {
				return proposition;
			}
		}
		return null;
		
	}
}
