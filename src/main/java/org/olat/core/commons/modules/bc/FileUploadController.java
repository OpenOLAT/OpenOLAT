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
* <p>
*/ 
package org.olat.core.commons.modules.bc;

import static java.util.Arrays.asList;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.regex.Pattern;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.commands.FolderCommandStatus;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.MetaInfoFactory;
import org.olat.core.commons.modules.bc.meta.MetaInfoFormController;
import org.olat.core.commons.modules.bc.meta.MetaInfoHelper;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.commons.modules.bc.version.RevisionListController;
import org.olat.core.commons.modules.bc.version.VersionCommentController;
import org.olat.core.commons.modules.bc.vfs.OlatRootFileImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.ButtonClickedEvent;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.CoreLoggingResourceable;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.ImageHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.version.Versionable;
import org.olat.core.util.vfs.version.Versions;
import org.olat.core.util.vfs.version.VersionsManager;

import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
import com.oreilly.servlet.multipart.FileRenamePolicy;

/**
 * <h3>Description</h3>
 * <p>
 * This controller offers a file upload in a dedicated form. It can be
 * configured with an upload limit, a limitation to mime types as allowed upload
 * types and if the path to the target directory should be displayed in the
 * form.
 * 
 * <h3>Events fired by this controller</h3>
 * <ul>
 * <li>FolderEvent (whenever something like upload occures)</li>
 * <li>Event.CANCELLED_EVENT</li>
 * <li>Event.FAILED_EVENT</li>
 * <li>Event.DONE_EVENT (fired after the folder upload event)</li>
 * </ul>
 * <p>
 * 
 * Initial Date: August 15, 2005
 * 
 * @author Alexander Schneider
 * @author Florian Gnägi
 */
public class FileUploadController extends FormBasicController {
	private int status = FolderCommandStatus.STATUS_SUCCESS;

	private VFSContainer currentContainer;
	private VFSContainer uploadVFSContainer;
	private String uploadRelPath = null;
	private RevisionListController revisionListCtr;
	private CloseableModalController revisionListDialogBox, commentVersionDialogBox, unlockDialogBox;
	private VersionCommentController commentVersionCtr;
	private VersionCommentController unlockCtr;
	private DialogBoxController overwriteDialog;
	private DialogBoxController lockedFileDialog;
	private VFSLeaf newFile = null;
	private VFSItem existingVFSItem = null;
	private int uploadLimitKB;
	private int remainingQuotKB;
	private Set<String> mimeTypes;
	private FilesInfoMBean fileInfoMBean;
	//
	// Form elements
	private FileElement fileEl;
	private MultipleSelectionElement resizeEl;
	private StaticTextElement pathEl;
	private boolean showTargetPath = false;

	private boolean fileOverwritten = false;
	private boolean resizeImg;
	
	// Metadata subform
	private MetaInfoFormController metaDataCtr;
	private boolean showMetadata = false;
	// 
	// Cancel button
	private boolean showCancel = true; // default is to show cancel button
	
	private static Pattern imageExtPattern = Pattern.compile("\\b.(jpg|jpeg|png)\\b");
	
	/**
	 * @param wControl
	 * @param curContainer Path to the upload directory. Used to check for
	 *          existing files with same name and for displaying the optional
	 *          targetPath
	 * @param ureq
	 * @param upLimitKB the max upload file size in kBytes (e.g. 10*1024*1024 for
	 *          10MB)
	 * @param remainingQuotKB the available space left for file upload kBytes
	 *          (e.g. 10*1024*1024 for 10MB). Quota.UNLIMITED for no limitation, 0
	 *          for no more space left
	 * @param mimeTypes Set of supported mime types (image/*, image/jpg) or NULL
	 *          if no restriction should be applied.
	 * @param showTargetPath true: show the relative path where the file will be
	 *          uploaded to; false: show no path
	 */
	public FileUploadController(WindowControl wControl, VFSContainer curContainer, UserRequest ureq, int upLimitKB, int remainingQuotKB, Set<String> mimeTypesRestriction, boolean showTargetPath) {
		this(wControl, curContainer, ureq, upLimitKB, remainingQuotKB, mimeTypesRestriction, showTargetPath, false, true, true);
	}
	
	/**
	 * @param wControl
	 * @param curContainer
	 * @param ureq
	 * @param upLimitKB
	 * @param remainingQuotKB
	 * @param mimeTypesRestriction
	 * @param showTargetPath
	 * @param showMetadata Display the meta data sub form
	 */
	public FileUploadController(WindowControl wControl, VFSContainer curContainer, UserRequest ureq, int upLimitKB, int remainingQuotKB,
			Set<String> mimeTypesRestriction, boolean showTargetPath, boolean showMetadata) {
		this(wControl, curContainer, ureq, upLimitKB, remainingQuotKB, mimeTypesRestriction, showTargetPath, showMetadata, true, true);
	}
	
	public FileUploadController(WindowControl wControl, VFSContainer curContainer, UserRequest ureq, int upLimitKB, int remainingQuotKB,
			Set<String> mimeTypesRestriction, boolean showTargetPath, boolean showMetadata, boolean resizeImg, boolean showCancel) {
		super(ureq, wControl, "file_upload");
		setVariables(curContainer, upLimitKB, remainingQuotKB, mimeTypesRestriction, showTargetPath, showMetadata, resizeImg, showCancel);
		
		initForm(ureq);
	}
	
	private void setVariables(VFSContainer curContainer, int upLimitKB, int remainingQuotKB,Set<String> mimeTypesRestriction, boolean showTargetPath, boolean showMetadata, boolean resizeImg, boolean showCancel) {
		this.currentContainer = curContainer;
		this.fileInfoMBean = (FilesInfoMBean) CoreSpringFactory.getBean(FilesInfoMBean.class.getCanonicalName());
		this.mimeTypes = mimeTypesRestriction;
		this.showTargetPath = showTargetPath;
		// set remaining quota and max upload size
		this.uploadLimitKB = upLimitKB;
		this.remainingQuotKB = remainingQuotKB;
		// use base container as upload dir
		this.uploadRelPath = null;
		this.uploadVFSContainer = this.currentContainer;
		this.resizeImg = resizeImg;
		this.showMetadata = showMetadata;
		this.showCancel = showCancel;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		// Trigger fieldset and title
		setFormTitle("ul.header");
		
		this.flc.contextPut("showMetadata", showMetadata);
		// Add file element
		FormItemContainer fileUpload;
		// the layout of the file upload depends on the metadata. if they're
		// shown, align the file upload element
		if (showMetadata) {
			fileUpload = FormLayoutContainer.createDefaultFormLayout("file_upload", getTranslator());
		} else {
			fileUpload = FormLayoutContainer.createVerticalFormLayout("file_upload", getTranslator());
		}
		formLayout.add(fileUpload);
		flc.contextPut("resizeImg", resizeImg);
		//

		if(resizeImg) {
			FormLayoutContainer resizeCont;
			if (showMetadata) {
				resizeCont = FormLayoutContainer.createDefaultFormLayout("resize_image_wrapper", getTranslator());
			} else {
				resizeCont = FormLayoutContainer.createVerticalFormLayout("resize_image_wrapper", getTranslator());
			}
			formLayout.add(resizeCont);

			String[] keys = new String[]{"resize"};
			String[] values = new String[]{translate("resize_image")};
			resizeEl = uifactory.addCheckboxesHorizontal("resize_image", resizeCont, keys, values, null);
			resizeEl.setLabel(null, null);
			resizeEl.select("resize", true);
		}
		
		fileEl = uifactory.addFileElement("fileEl", "ul.file", fileUpload);
		setMaxUploadSizeKB(this.uploadLimitKB);
		fileEl.setMandatory(true, "NoFileChoosen");
		if (mimeTypes != null && mimeTypes.size() > 0) {
			fileEl.limitToMimeType(mimeTypes, "WrongMimeType", new String[]{mimeTypes.toString()});					
		}
		
		// Check remaining quota
		if (remainingQuotKB == 0) {
			fileEl.setEnabled(false);
			getWindowControl().setError(translate("QuotaExceeded"));
		}
		//
		// Add path element
		if (showTargetPath) {			
			String path = "/ " + uploadVFSContainer.getName();
			VFSContainer container = uploadVFSContainer.getParentContainer();
			while (container != null) {
				path = "/ " + container.getName() + " " + path;
				container = container.getParentContainer();
			}
			pathEl = uifactory.addStaticTextElement("ul.target", path,fileUpload);
		}
		
		if (showMetadata) {
			metaDataCtr = new MetaInfoFormController(ureq, getWindowControl(),
					mainForm);
			formLayout.add("metadata", metaDataCtr.getFormItem());
			listenTo(metaDataCtr);
		}
		//
		// Add cancel and submit in button group layout
		FormItemContainer buttons;
		if (showMetadata) {
			buttons = FormLayoutContainer.createDefaultFormLayout("buttons", getTranslator());
		} else {
			buttons = FormLayoutContainer.createVerticalFormLayout("buttons", getTranslator());			
		}
		formLayout.add(buttons);
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		buttons.add(buttonGroupLayout);
		uifactory.addFormSubmitButton("ul.upload", buttonGroupLayout);
		if (showCancel) {
			uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());			
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if ( fileEl.isUploadSuccess()) {
			// check for available space
			if (remainingQuotKB != -1) {
				if (fileEl.getUploadFile().length() / 1024 > remainingQuotKB) {
					fileEl.setErrorKey("QuotaExceeded", null);
					fileEl.getUploadFile().delete();
					return;
				}				
			}
			String fileName = fileEl.getUploadFileName();
			
			File uploadedFile = fileEl.getUploadFile();
			if(resizeImg && fileName != null && imageExtPattern.matcher(fileName.toLowerCase()).find()
					&& resizeEl.isSelected(0)) {
				String extension = FileUtils.getFileSuffix(fileName);
				File imageScaled = new File(uploadedFile.getParentFile(), "scaled_" + uploadedFile.getName() + "." + extension);
				if(ImageHelper.scaleImage(uploadedFile, extension, imageScaled, 1280)) {
					//problem happen, special GIF's (see bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6358674)
					//don't try to scale if not all ok 
					uploadedFile = imageScaled;
				}
			}
			
			// check if such a filename does already exist
			existingVFSItem = uploadVFSContainer.resolve(fileName);
			if (existingVFSItem == null) {
				// save file and finish
				newFile = uploadVFSContainer.createChildLeaf(fileName);
				InputStream in = null;
				OutputStream out = null;
				boolean success = true;
				
				try {
					
					in = new FileInputStream(uploadedFile);
					out = newFile.getOutputStream(false);
					FileUtils.bcopy(in, out, "uploadTmpFileToDestFile");
					uploadedFile.delete();
					
				} catch (IOException e) {
					FileUtils.closeSafely(in);
					FileUtils.closeSafely(out);
					success = false;
				}
				
				if (success) {
					String filePath = (uploadRelPath == null ? "" : uploadRelPath + "/") + newFile.getName();
					finishSuccessfullUpload(filePath, ureq);
					fileInfoMBean.logUpload(newFile.getSize());
					fireEvent(ureq, Event.DONE_EVENT);										
				} else {
					showError("failed");
					status = FolderCommandStatus.STATUS_FAILED;
					fireEvent(ureq, Event.FAILED_EVENT);					
				}
			} else {
				// file already exists... upload anyway with new filename and
				// in the folder manager status.
				// rename file and ask user what to do
				FileRenamePolicy frp = new DefaultFileRenamePolicy();
				if ( ! (existingVFSItem instanceof LocalImpl)) {
					throw new AssertException("Can only LocalImpl VFS items, don't know what to do with file of type::" + existingVFSItem.getClass().getCanonicalName());
				}
				File existingFile = ((LocalImpl)existingVFSItem).getBasefile();
				File tmpOrigFilename = new File(existingFile.getAbsolutePath());
				String renamedFilename = frp.rename(tmpOrigFilename).getName();
				newFile = (VFSLeaf) uploadVFSContainer.resolve(renamedFilename);
				// Copy content to tmp file
				
				InputStream in = null;
				BufferedOutputStream out = null;
				boolean success = false;
				try {
					in = new FileInputStream(uploadedFile);
					out = new BufferedOutputStream(newFile.getOutputStream(false));
					if (in != null) {
						success = FileUtils.copy(in, out);					
					}
					uploadedFile.delete();
				} catch (FileNotFoundException e) {
					success = false;
				} finally {
					FileUtils.closeSafely(in);
					FileUtils.closeSafely(out);
				}
				
				if (success) {
					if (existingVFSItem instanceof MetaTagged && MetaInfoHelper.isLocked(existingVFSItem, ureq)) {
						//the file is locked and cannot be overwritten
						removeAsListenerAndDispose(lockedFileDialog);
						lockedFileDialog = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), translate("ul.lockedFile.title"), translate("ul.lockedFile.text", new String[] {existingVFSItem.getName(), renamedFilename} ), asList(translate("ul.overwrite.threeoptions.rename", renamedFilename), translate("ul.overwrite.threeoptions.cancel")));
						listenTo(lockedFileDialog);
						
						lockedFileDialog.activate();
					}
					else if (existingVFSItem instanceof Versionable && ((Versionable)existingVFSItem).getVersions().isVersioned()) {
						Versionable versionable = (Versionable)existingVFSItem;
						Versions versions = versionable.getVersions();
						String relPath = null;
						if(existingVFSItem instanceof OlatRootFileImpl) {
							relPath = ((OlatRootFileImpl)existingVFSItem).getRelPath();
						}
						int maxNumOfRevisions = FolderConfig.versionsAllowed(relPath);
						if(maxNumOfRevisions == 0) {
							//it's possible if someone change the configuration
							// let calling method decide what to do.
							removeAsListenerAndDispose(overwriteDialog);
							overwriteDialog = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), translate("ul.overwrite.threeoptions.title"), translate("ul.overwrite.threeoptions.text", new String[] {existingVFSItem.getName(), renamedFilename} ), asList(translate("ul.overwrite.threeoptions.overwrite"), translate("ul.overwrite.threeoptions.rename", renamedFilename), translate("ul.overwrite.threeoptions.cancel")));
							listenTo(overwriteDialog);
							
							overwriteDialog.activate();
							
						} else if(versions.getRevisions().isEmpty() || maxNumOfRevisions < 0 || maxNumOfRevisions > versions.getRevisions().size()) {
							// let calling method decide what to do.
							removeAsListenerAndDispose(overwriteDialog);
							overwriteDialog = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), translate("ul.overwrite.threeoptions.title"), translate("ul.versionoroverwrite", new String[] {existingVFSItem.getName(), renamedFilename} ), asList(translate("ul.overwrite.threeoptions.newVersion"), translate("ul.overwrite.threeoptions.rename", renamedFilename), translate("ul.overwrite.threeoptions.cancel")));
							listenTo(overwriteDialog);
							
							overwriteDialog.activate();
							
						} else {
						
							String title = translate("ul.tooManyRevisions.title", new String[]{Integer.toString(maxNumOfRevisions), Integer.toString(versions.getRevisions().size())});
							String description = translate("ul.tooManyRevisions.description", new String[]{Integer.toString(maxNumOfRevisions), Integer.toString(versions.getRevisions().size())});
							
							removeAsListenerAndDispose(revisionListCtr);
							revisionListCtr = new RevisionListController(ureq, getWindowControl(), versionable, title, description);
							listenTo(revisionListCtr);
							
							removeAsListenerAndDispose(revisionListDialogBox);
							revisionListDialogBox = new CloseableModalController(getWindowControl(), translate("delete"), revisionListCtr.getInitialComponent());
							listenTo(revisionListDialogBox);
							
							revisionListDialogBox.activate();
						}
					} else {
						// let calling method decide what to do.
						// for this, we put a list with "existing name" and "new name"
						overwriteDialog = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), translate("ul.overwrite.threeoptions.title"), translate("ul.overwrite.threeoptions.text", new String[] {existingVFSItem.getName(), renamedFilename} ), asList(translate("ul.overwrite.threeoptions.overwrite"), translate("ul.overwrite.threeoptions.rename", renamedFilename), translate("ul.overwrite.threeoptions.cancel")));
						listenTo(overwriteDialog);
						overwriteDialog.activate();
					}
				} else {
					showError("failed");
					status = FolderCommandStatus.STATUS_FAILED;
					fireEvent(ureq, Event.FAILED_EVENT);					
				}
			}
		} else {
			if (mainForm.getLastRequestError() == Form.REQUEST_ERROR_GENERAL ) {
				showError("failed");				
			} else if (mainForm.getLastRequestError() == Form.REQUEST_ERROR_FILE_EMPTY ) {
				showError("failed");				
			}else if (mainForm.getLastRequestError() == Form.REQUEST_ERROR_UPLOAD_LIMIT_EXCEEDED) {
				showError("QuotaExceeded");				
			}
			status = FolderCommandStatus.STATUS_FAILED;
			fireEvent(ureq, Event.FAILED_EVENT);					
		}
	}
	
	private boolean askForLock(VFSItem item, UserRequest ureq) {
		if(item instanceof MetaTagged) {
			MetaInfo info = ((MetaTagged)item).getMetaInfo();
			if(info.isLocked() && !MetaInfoHelper.isLocked(item, ureq)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formCancelled(org.olat.core.gui.UserRequest)
	 */
	@Override	
	protected void formCancelled(UserRequest ureq) {
		status = FolderCommandStatus.STATUS_CANCELED;
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == overwriteDialog) {
			
			if (event instanceof ButtonClickedEvent) {
				ButtonClickedEvent buttonClickedEvent = (ButtonClickedEvent) event;
				if (buttonClickedEvent.getPosition() == 0) { //ok
					if (existingVFSItem instanceof Versionable && ((Versionable)existingVFSItem).getVersions().isVersioned()) {
						//new version
						String relPath = null;
						if(existingVFSItem instanceof OlatRootFileImpl) {
							relPath = ((OlatRootFileImpl)existingVFSItem).getRelPath();
						}
						int maxNumOfRevisions = FolderConfig.versionsAllowed(relPath);
						if(maxNumOfRevisions == 0) {
							//someone play with the configuration
							// Overwrite...
							String fileName = existingVFSItem.getName();
							existingVFSItem.delete();
							newFile.rename(fileName);
							
							// ... and notify listeners.
							finishUpload(ureq);
						} else {
							
							removeAsListenerAndDispose(commentVersionCtr);
							commentVersionCtr = new VersionCommentController(ureq,getWindowControl(), askForLock(existingVFSItem, ureq), true);
							listenTo(commentVersionCtr);
							
							removeAsListenerAndDispose(commentVersionDialogBox);
							commentVersionDialogBox = new CloseableModalController(getWindowControl(), translate("save"), commentVersionCtr.getInitialComponent());
							listenTo(commentVersionDialogBox);
							
							commentVersionDialogBox.activate();
						}
					} else {
						//if the file is locked, ask for unlocking it
						if(existingVFSItem instanceof MetaTagged && ((MetaTagged)existingVFSItem).getMetaInfo().isLocked()) {
							
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
							newFile.rename(fileName);
							
							// ... and notify listeners.
							finishUpload(ureq);
						}
					}
				} else if (buttonClickedEvent.getPosition() == 1) { //not ok
					// Upload renamed. Since we've already uploaded the file with a changed name, don't do anything much here...
					this.fileOverwritten = true;

					// ... and notify listeners.
					finishUpload(ureq);
				} else if (buttonClickedEvent.getPosition() == 2) { // cancel
					// Cancel. Remove the new file since it has already been uploaded. Note that we don't have to explicitly close the
					// dialog box since it closes itself whenever something gets clicked.
					newFile.delete();
					VersionsManager.getInstance().delete(newFile, true);//force delete the auto-versioning of this temp. file
				} else {
					throw new RuntimeException("Unknown button number " + buttonClickedEvent.getPosition());
				}
			}
		} else if (source == lockedFileDialog) {

			if (event instanceof ButtonClickedEvent) {
				ButtonClickedEvent buttonClickedEvent = (ButtonClickedEvent) event;
				switch(buttonClickedEvent.getPosition()) {
					case 0: {
						//upload the file with a new name
						this.fileOverwritten = true;
						// ... and notify listeners.
						finishUpload(ureq);
						break;
					}
					case 1: {//cancel
						newFile.delete();
						VersionsManager.getInstance().delete(newFile, true);//force delete the auto-versioning of this temp. file
						fireEvent(ureq, Event.CANCELLED_EVENT);
						break;
					}
					default:
						throw new RuntimeException("Unknown button number " + buttonClickedEvent.getPosition());
				}
			}
		} else if (source == commentVersionCtr) {
			String comment = commentVersionCtr.getComment();
			if(existingVFSItem instanceof MetaTagged) {
				MetaInfo info = ((MetaTagged)existingVFSItem).getMetaInfo();
				if(info.isLocked() && !commentVersionCtr.keepLocked()) {
					info.setLocked(false);
					info.write();
				}
			}
			
			commentVersionDialogBox.deactivate();
			if(revisionListDialogBox != null) {
				revisionListDialogBox.deactivate();
			}
			
			//ok, new version of the file
			Versionable existingVersionableItem = (Versionable)existingVFSItem;
			boolean ok = existingVersionableItem.getVersions().addVersion(ureq.getIdentity(), comment, newFile.getInputStream());
			if(ok) {
				newFile.delete();
				VersionsManager.getInstance().delete(newFile, true);
				//what can i do if existingVFSItem is a container
				if(existingVFSItem instanceof VFSLeaf) {
					newFile = (VFSLeaf)existingVFSItem;
				}
			}
			finishUpload(ureq);
		} else if (source == unlockCtr) {
			// Overwrite...
			String fileName = existingVFSItem.getName();
			if(!unlockCtr.keepLocked()) {
				MetaInfo info = ((MetaTagged)existingVFSItem).getMetaInfo();
				info.setLocked(false);
				info.setLockedBy(null);
				info.write();
			}
			
			unlockDialogBox.deactivate();
			
			existingVFSItem.delete();
			newFile.rename(fileName);

			// ... and notify listeners.
			finishUpload(ureq);
			
		} else if (source == revisionListCtr) {
			if(FolderCommandStatus.STATUS_CANCELED == revisionListCtr.getStatus()) {

				revisionListDialogBox.deactivate();

				//don't want to delete revisions, clean the temporary file
				newFile.delete();
				VersionsManager.getInstance().delete(newFile, true);
				fireEvent(ureq, Event.CANCELLED_EVENT);
			} else {
				if (existingVFSItem instanceof Versionable && ((Versionable)existingVFSItem).getVersions().isVersioned()) {

					revisionListDialogBox.deactivate();
	
					Versionable versionable = (Versionable)existingVFSItem;
					Versions versions = versionable.getVersions();
					int maxNumOfRevisions = FolderConfig.versionsAllowed(null);
					if(maxNumOfRevisions < 0 || maxNumOfRevisions > versions.getRevisions().size()) {
						
						removeAsListenerAndDispose(commentVersionCtr);
						commentVersionCtr = new VersionCommentController(ureq,getWindowControl(), askForLock(existingVFSItem, ureq), true);
						listenTo(commentVersionCtr);
						
						removeAsListenerAndDispose(commentVersionDialogBox);
						commentVersionDialogBox = new CloseableModalController(getWindowControl(), translate("save"), commentVersionCtr.getInitialComponent());
						listenTo(commentVersionDialogBox);
						
						commentVersionDialogBox.activate();
						
					} else {
						
						removeAsListenerAndDispose(revisionListCtr);
						revisionListCtr = new RevisionListController(ureq,getWindowControl(),versionable);
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
		// in both cases the upload must be finished and notified with a FolderEvent
		String filePath = (uploadRelPath == null ? "" : uploadRelPath + "/") + newFile.getName();
		finishSuccessfullUpload(filePath, ureq);
		fileInfoMBean.logUpload(newFile.getSize());
		fireEvent(ureq, Event.DONE_EVENT);
	}

	/**
	 * Internal helper to finish the upload and add metadata
	 */
	private void finishSuccessfullUpload(String fileName, UserRequest ureq) {
		VFSItem item = currentContainer.resolve(fileName);
		if (item instanceof OlatRootFileImpl) {
			OlatRootFileImpl relPathItem = (OlatRootFileImpl) item;
			// create meta data
			MetaInfo meta = MetaInfoFactory.createMetaInfoFor(relPathItem);
			if (metaDataCtr != null) {
				meta = metaDataCtr.getMetaInfo(meta);
			}
			meta.setAuthor(ureq.getIdentity().getName());
			meta.clearThumbnails();//if overwrite an older file
			meta.write();
		}
		ThreadLocalUserActivityLogger.log(FolderLoggingAction.FILE_UPLOADED, getClass(), CoreLoggingResourceable.wrapUploadFile(fileName));

		// Notify listeners about upload
		fireEvent(ureq, new FolderEvent(FolderEvent.UPLOAD_EVENT, fileName));
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// 
	}

	/**
	 * @return The upload status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @return true: an existing file has benn overwritten; false: no file with
	 *         same name existed or new file has been renamed
	 */
	public boolean isExistingFileOverwritten() {
		return fileOverwritten;
	}
	/**
	 * Set the max upload limit.
	 * @param uploadLimitKB
	 */
	public void setMaxUploadSizeKB(int uploadLimitKB) {
		this.uploadLimitKB = uploadLimitKB;
		String supportAddr = WebappHelper.getMailConfig("mailSupport");
		fileEl.setMaxUploadSizeKB(uploadLimitKB, "ULLimitExceeded", new String[] { Formatter.roundToString((uploadLimitKB+0f) / 1000, 1), supportAddr });
	}

	/**
	 * Reset the upload controller
	 */
	public void reset() {
		newFile = null;
		existingVFSItem = null;
		status = FolderCommandStatus.STATUS_SUCCESS;
		fileEl.reset();
	}

	/**
	 * Call this to remove the fieldset and title from the form rendering. This
	 * can not be reverted. Default is to show the upload title and fieldset,
	 * after calling this function no more title will be shown.
	 */
	public void hideTitleAndFieldset() {
		this.setFormTitle(null);
	}

	/**
	 * Set the relative path within the rootDir where uploaded files should be put
	 * into. If NULL, the root Dir is used
	 * 
	 * @param uploadRelPath
	 */
	public void setUploadRelPath(String uploadRelPath) {
		this.uploadRelPath = uploadRelPath;
		// resolve upload dir from rel upload path
		if (uploadRelPath == null) {
			// reset to current base container
			this.uploadVFSContainer = this.currentContainer;
		} else {
			// try to resolve given rel path from current container
			VFSItem uploadDir = currentContainer.resolve(uploadRelPath);
			if (uploadDir != null) {
				// make sure this is really a container and not a file!
				if (uploadDir instanceof VFSContainer) {
					this.uploadVFSContainer = (VFSContainer) uploadDir;
				} else {
					// fallback to current base 
					this.uploadVFSContainer = this.currentContainer;
				}
			} else {
				// does not yet exist - create subdir
				if (VFSConstants.YES.equals(this.currentContainer.canWrite())) {
					this.uploadVFSContainer = this.currentContainer.createChildContainer(uploadRelPath);
				}
			}			
		}
		
		// update the destination path in the GUI
		if (showTargetPath) {			
			String path = "/ " + currentContainer.getName() + (uploadRelPath == null ? "" : " / " + uploadRelPath);
			VFSContainer container = currentContainer.getParentContainer();
			while (container != null) {
				path = "/ " + container.getName() + " " + path;
				container = container.getParentContainer();
			}
			pathEl.setValue(path);
		}
	}

	public String getNewFileName() {
		return (this.newFile != null) ? this.newFile.getName() : null; 
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		
		String fileName = fileEl.getUploadFileName();
		
		if (fileName == null) {
			fileEl.setErrorKey("NoFileChosen", null);
			return false;
		}
		
		boolean isFilenameValid = FileUtils.validateFilename(fileName);		
		if(!isFilenameValid) {
			fileEl.setErrorKey("cfile.name.notvalid", null);
			return false;
		}
		
		if (remainingQuotKB != -1) {
			if (fileEl.getUploadFile().length() / 1024 > remainingQuotKB) {
				fileEl.clearError();
				String supportAddr = WebappHelper.getMailConfig("mailSupport");
				getWindowControl().setError(translate("ULLimitExceeded", new String[] { Formatter.roundToString((uploadLimitKB+0f) / 1000, 1), supportAddr }));
				return false;
			}				
		}
		
		return true;
	}
}
