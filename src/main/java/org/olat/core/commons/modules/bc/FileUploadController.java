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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.olat.core.commons.modules.bc.commands.FolderCommandStatus;
import org.olat.core.commons.modules.bc.meta.MetaInfoFormController;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.commons.services.vfs.VFSVersionModule;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.commons.services.vfs.ui.version.RevisionListController;
import org.olat.core.commons.services.vfs.ui.version.VersionCommentController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementEvent;
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
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.VFSManager;
import org.springframework.beans.factory.annotation.Autowired;

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

	private static final String[] resizeKeys = new String[]{"resize"};
	private int status = FolderCommandStatus.STATUS_SUCCESS;

	private VFSContainer currentContainer;
	private VFSContainer uploadVFSContainer;
	private String uploadRelPath = null;
	private RevisionListController revisionListCtr;
	private CloseableModalController unlockDialogBox;
	private CloseableModalController revisionListDialogBox;
	private CloseableModalController commentVersionDialogBox;
	private VersionCommentController commentVersionCtr;
	private VersionCommentController unlockCtr;
	private DialogBoxController overwriteDialog;
	private DialogBoxController lockedFileDialog;
	private VFSLeaf newFile;
	private VFSItem existingVFSItem;
	private long uploadLimitKB;
	private long remainingQuotKB;
	private Set<String> mimeTypes;
	private boolean uriValidation;
	//
	// Form elements
	private FileElement fileEl;
	private MultipleSelectionElement resizeEl;
	private StaticTextElement pathEl;
	private boolean showTargetPath = false;
	private boolean showTitle = true;

	private boolean fileOverwritten = false;
	private boolean resizeImg;
	
	// Metadata subform
	private MetaInfoFormController metaDataCtr;
	private boolean showMetadata = false;
	// 
	// Cancel button
	private boolean showCancel = true; // default is to show cancel button
	
	private static Pattern imageExtPattern = Pattern.compile("\\b.(jpg|jpeg|png)\\b");
	private static final Pattern validSubPathPattern = Pattern.compile("[\\p{Alnum}-_\\./]*");		

	@Autowired
	private ImageService imageHelper;
	@Autowired
	private VFSLockManager vfsLockManager;
	@Autowired
	private VFSVersionModule versionsModule;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;

	private String subfolderPath;
	private TextElement targetSubPath ;
	
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
	public FileUploadController(WindowControl wControl, VFSContainer curContainer, UserRequest ureq, long upLimitKB, long remainingQuotKB,
			Set<String> mimeTypesRestriction, boolean showTargetPath) {
		this(wControl, curContainer, ureq, upLimitKB, remainingQuotKB, mimeTypesRestriction, false, showTargetPath, false, true, true, true);
	}
	
	public FileUploadController(WindowControl wControl, VFSContainer curContainer, UserRequest ureq, long upLimitKB, long remainingQuotKB,
			Set<String> mimeTypesRestriction, boolean uriValidation, boolean showTargetPath, boolean showMetadata, boolean resizeImg, boolean showCancel, boolean showTitle) {
		this(wControl,curContainer,  ureq,  upLimitKB,  remainingQuotKB,
				mimeTypesRestriction, uriValidation, showTargetPath,  showMetadata,  resizeImg,  showCancel,  showTitle,null);
	}
	
	public FileUploadController(WindowControl wControl, VFSContainer curContainer, UserRequest ureq, long upLimitKB, long remainingQuotKB,
			Set<String> mimeTypesRestriction, boolean uriValidation, boolean showTargetPath, boolean showMetadata, boolean resizeImg, boolean showCancel, boolean showTitle, String subfolderPath) {
		super(ureq, wControl, "file_upload");
		setVariables(curContainer, upLimitKB, remainingQuotKB, mimeTypesRestriction, uriValidation, showTargetPath, showMetadata, resizeImg, showCancel, showTitle, subfolderPath);
		initForm(ureq);
	}
	
	private void setVariables(VFSContainer curContainer, long upLimitKB, long remainingQuotKB, Set<String> mimeTypesRestriction, boolean uriValidation, boolean showTargetPath,
			boolean showMetadata, boolean resizeImg, boolean showCancel, boolean showTitle, String subfolderPath) {
		this.currentContainer = curContainer;
		this.mimeTypes = mimeTypesRestriction;
		this.showTitle = showTitle;
		this.showTargetPath = showTargetPath;
		// set remaining quota and max upload size
		this.uploadLimitKB = upLimitKB;
		this.uriValidation = uriValidation;
		this.remainingQuotKB = remainingQuotKB;
		// use base container as upload dir
		this.uploadRelPath = null;
		this.uploadVFSContainer = this.currentContainer;
		this.resizeImg = resizeImg;
		this.showMetadata = showMetadata;
		this.showCancel = showCancel;
		this.subfolderPath = subfolderPath;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Trigger fieldset and title
		if(showTitle) {
			setFormTitle("ul.header");
		}
		
		flc.contextPut("showMetadata", showMetadata);
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

		// Add path element
		if (showTargetPath) {			
			String path = "/ " + StringHelper.escapeHtml(uploadVFSContainer.getName());
			VFSContainer container = uploadVFSContainer.getParentContainer();
			while (container != null) {
				path = "/ " + StringHelper.escapeHtml(container.getName()) + " " + path;
				container = container.getParentContainer();
			}
			
			pathEl = uifactory.addStaticTextElement("ul.target", path,fileUpload);
			
			if (subfolderPath != null) {
				targetSubPath = uifactory.addInlineTextElement("ul.target.child", subfolderPath, fileUpload, this);	
				targetSubPath.setLabel("ul.target.child", null);
			}
		}

		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "fileEl", "ul.file", fileUpload);
		fileEl.addActionListener(FormEvent.ONCHANGE);
		
		setMaxUploadSizeKB((uploadLimitKB < remainingQuotKB ? uploadLimitKB : remainingQuotKB));
		fileEl.setMandatory(true, "NoFileChoosen");
		if (mimeTypes != null && mimeTypes.size() > 0) {
			fileEl.limitToMimeType(mimeTypes, "WrongMimeType", new String[]{mimeTypes.toString()});					
		}

		if(resizeImg) {
			FormLayoutContainer resizeCont;
			if (showMetadata) {
				resizeCont = FormLayoutContainer.createDefaultFormLayout("resize_image_wrapper", getTranslator());
			} else {
				resizeCont = FormLayoutContainer.createVerticalFormLayout("resize_image_wrapper", getTranslator());
			}
			formLayout.add(resizeCont);

			String[] values = new String[]{translate("resize_image")};
			resizeEl = uifactory.addCheckboxesHorizontal("resize_image", resizeCont, resizeKeys, values);
			resizeEl.setLabel(null, null);
			resizeEl.select(resizeKeys[0], true);
			resizeEl.setVisible(false);
		}
		
		// Check remaining quota
		if (remainingQuotKB == 0) {
			fileEl.setEnabled(false);
			getWindowControl().setError(translate("QuotaExceeded"));
		}
		
		
		if (showMetadata) {
			metaDataCtr = new MetaInfoFormController(ureq, getWindowControl(), mainForm, true);
			formLayout.add("metadata", metaDataCtr.getFormItem());
			listenTo(metaDataCtr);
		}
		
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
		buttonGroupLayout.setElementCssClass("o_sel_upload_buttons");
		uifactory.addFormSubmitButton("ul.upload", buttonGroupLayout);
		if (showCancel) {
			uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());			
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(fileEl == source) {
			if(FileElementEvent.DELETE.equals(event.getCommand())) {
				fileEl.reset();
				fileEl.setDeleteEnabled(false);
				fileEl.clearError();
				if(resizeImg && resizeEl != null) {
					resizeEl.setVisible(false);
				}
			} else  {
				String filename = fileEl.getUploadFileName();
				if(metaDataCtr != null) {
					if(filename == null) {
						metaDataCtr.getFilenameEl().setExampleKey("mf.filename.warning", null);	
					} else if(!FileUtils.validateFilename(filename)) {
						String suffix = FileUtils.getFileSuffix(filename);
						if(suffix != null && suffix.length() > 0) {
							filename = filename.substring(0, filename.length() - suffix.length() - 1);
						}
						filename = FileUtils.normalizeFilename(filename) + "." + suffix;
						metaDataCtr.getFilenameEl().setExampleKey("mf.filename.warning", null);
					}
					metaDataCtr.setFilename(filename);
				}
				
				if(resizeImg) {
					boolean isImg = false;
					if(filename != null) {
						isImg = imageExtPattern.matcher(filename.toLowerCase()).find();
					}
					if(resizeEl != null) {
						resizeEl.setVisible(isImg);
						resizeEl.select(resizeKeys[0], true);
					}
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(targetSubPath != null) setUploadRelPath(targetSubPath.getValue());
		if ( fileEl.isUploadSuccess()) {
			doUpload(ureq);
		} else {
			if (mainForm.getLastRequestError() == Form.REQUEST_ERROR_GENERAL ) {
				showError("failed");				
			} else if (mainForm.getLastRequestError() == Form.REQUEST_ERROR_FILE_EMPTY ) {
				showError("failed");				
			} else if (mainForm.getLastRequestError() == Form.REQUEST_ERROR_UPLOAD_LIMIT_EXCEEDED) {
				showError("QuotaExceeded");				
			}
			status = FolderCommandStatus.STATUS_FAILED;
			fireEvent(ureq, Event.FAILED_EVENT);					
		}
	}
	
	@Override	
	protected void formCancelled(UserRequest ureq) {
		status = FolderCommandStatus.STATUS_CANCELED;
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == overwriteDialog) {
			if (event instanceof ButtonClickedEvent) {
				ButtonClickedEvent buttonClickedEvent = (ButtonClickedEvent) event;
				if (buttonClickedEvent.getPosition() == 0) { //ok
					doFinishOverwrite(ureq);
				} else if (buttonClickedEvent.getPosition() == 1) { //not ok
					// Upload renamed. Since we've already uploaded the file with a changed name, don't do anything much here...
					fileOverwritten = true;
					// ... and notify listeners.
					finishUpload(ureq, true);
				} else if (buttonClickedEvent.getPosition() == 2) { // cancel
					// Cancel. Remove the new file since it has already been uploaded. Note that we don't have to explicitly close the
					// dialog box since it closes itself whenever something gets clicked.
					doCancel(ureq);
				}
			}
		} else if (source == lockedFileDialog) {
			if (event instanceof ButtonClickedEvent) {
				ButtonClickedEvent buttonClickedEvent = (ButtonClickedEvent) event;
				if(buttonClickedEvent.getPosition() == 0) {
					//upload the file with a new name
					fileOverwritten = true;
					// ... and notify listeners.
					finishUpload(ureq, true);
				} else if(buttonClickedEvent.getPosition() == 1) {
					doCancel(ureq);
				}
			}
		} else if (source == commentVersionCtr) {
			doFinishComment(ureq);
		} else if (source == unlockCtr) {
			// Overwrite...
			String fileName = existingVFSItem.getName();
			if(!unlockCtr.keepLocked()) {
				vfsLockManager.unlock(existingVFSItem, VFSLockApplicationType.vfs);
			}
			unlockDialogBox.deactivate();
			
			existingVFSItem.delete();
			newFile.rename(fileName);
			// ... and notify listeners.
			finishUpload(ureq, true);
			
		} else if (source == revisionListDialogBox) {
			removeAsListenerAndDispose(revisionListCtr);
			revisionListCtr = null;
			removeAsListenerAndDispose(revisionListDialogBox);
			revisionListDialogBox = null;
			doCancel(ureq);
		} else if (source == revisionListCtr) {
			revisionListDialogBox.deactivate();
			removeAsListenerAndDispose(revisionListDialogBox);
			revisionListDialogBox = null;
			
			if(FolderCommandStatus.STATUS_CANCELED == revisionListCtr.getStatus()) {
				//don't want to delete revisions, clean the temporary file
				doCancel(ureq);
			} else if (existingVFSItem.canVersion() == VFSConstants.YES) {
				doFinishRevisionList(ureq);
			}
		}
	}
	
	/**
	 * Delete the uploaded file and send cancel event
	 * @param ureq
	 */
	private void doCancel(UserRequest ureq) {
		if(newFile != null) {
			newFile.deleteSilently();
		}
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doFinishOverwrite(UserRequest ureq) {
		if (existingVFSItem.canVersion() == VFSConstants.YES) {
			//new version
			int maxNumOfRevisions = getMaxNumOfRevisionsOfExistingVFSItem();
			if(maxNumOfRevisions == 0) {
				//someone play with the configuration
				// Overwrite...
				String fileName = existingVFSItem.getName();
				existingVFSItem.delete();
				newFile.rename(fileName);
				
				// ... and notify listeners.
				finishUpload(ureq, false);
			} else {
				askForComment(ureq);
			}
		} else {
			//if the file is locked, ask for unlocking it
			if(vfsLockManager.isLocked(existingVFSItem, VFSLockApplicationType.vfs, null)) {
				askForUnlock(ureq);
				
			} else {
				// Overwrite...
				String fileName = existingVFSItem.getName();
				existingVFSItem.delete();
				newFile.rename(fileName);
				
				// ... and notify listeners.
				finishUpload(ureq, false);
			}
		}
	}
	
	private void doFinishComment(UserRequest ureq) {
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
		if(existingVFSItem instanceof VFSLeaf && existingVFSItem.canVersion() == VFSConstants.YES) {
			boolean ok = vfsRepositoryService.addVersion((VFSLeaf)existingVFSItem, ureq.getIdentity(), false, comment, newFile.getInputStream());
			if(ok) {
				newFile.deleteSilently();
				//what can i do if existingVFSItem is a container
				newFile = (VFSLeaf)existingVFSItem;
			}
		}
		finishUpload(ureq, false);
	}
	
	private void doFinishRevisionList(UserRequest ureq) {
		if(existingVFSItem.getParentContainer() != null) {
			existingVFSItem = existingVFSItem.getParentContainer().resolve(existingVFSItem.getName());
		}
		
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(existingVFSItem);
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
		int maxNumOfRevisions = getMaxNumOfRevisionsOfExistingVFSItem();
		if(maxNumOfRevisions < 0 || maxNumOfRevisions > revisions.size()) {
			askForComment(ureq);
		} else {
			askToReduceRevisionList(ureq, (VFSLeaf)existingVFSItem);
		}
	}
	
	private int getMaxNumOfRevisionsOfExistingVFSItem() {
		return versionsModule.getMaxNumberOfVersions();
	}
	
	private void askToReduceRevisionList(UserRequest ureq, VFSLeaf versionable) {
		removeAsListenerAndDispose(revisionListCtr);
		removeAsListenerAndDispose(revisionListDialogBox);
		
		int maxNumOfRevisions = getMaxNumOfRevisionsOfExistingVFSItem();
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(versionable);
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
		
		int numOfRevisions = revisions.size();
		String[] params = new String[]{ Integer.toString(maxNumOfRevisions), Integer.toString(numOfRevisions) };
		String title = translate("ul.tooManyRevisions.title", params);
		String description = translate("ul.tooManyRevisions.description", params);
		
		revisionListCtr = new RevisionListController(ureq, getWindowControl(), versionable, null, description, false);
		listenTo(revisionListCtr);
		
		revisionListDialogBox = new CloseableModalController(getWindowControl(), translate("delete"), revisionListCtr.getInitialComponent(), true, title);
		listenTo(revisionListDialogBox);
		revisionListDialogBox.activate();
	}
	
	private void askForUnlock(UserRequest ureq) {
		removeAsListenerAndDispose(unlockCtr);
		removeAsListenerAndDispose(unlockDialogBox);
		
		unlockCtr = new VersionCommentController(ureq,getWindowControl(), true, false);
		listenTo(unlockCtr);
		String title = unlockCtr.getAndRemoveFormTitle();
		unlockDialogBox = new CloseableModalController(getWindowControl(), translate("ok"), unlockCtr.getInitialComponent(), true, title);
		listenTo(unlockDialogBox);
		unlockDialogBox.activate();
	}
	
	private void askForComment(UserRequest ureq) {
		removeAsListenerAndDispose(commentVersionCtr);
		removeAsListenerAndDispose(commentVersionDialogBox);

		boolean locked = vfsLockManager.isLocked(existingVFSItem, VFSLockApplicationType.vfs, null);
		commentVersionCtr = new VersionCommentController(ureq, getWindowControl(), locked, true);
		listenTo(commentVersionCtr);
		String title = commentVersionCtr.getAndRemoveFormTitle();
		commentVersionDialogBox = new CloseableModalController(getWindowControl(), translate("save"), commentVersionCtr.getInitialComponent(), true, title);
		listenTo(commentVersionDialogBox);
		commentVersionDialogBox.activate();
	}
	
	private void doUpload(UserRequest ureq) {
		// check for available space
		if (remainingQuotKB != -1 && (fileEl.getUploadFile().length() / 1024 > remainingQuotKB)) {
			fileEl.setErrorKey("QuotaExceeded", null);
			FileUtils.deleteFile(fileEl.getUploadFile());
			return;			
		}
		
		String fileName = fileEl.getUploadFileName();
		if(metaDataCtr != null && StringHelper.containsNonWhitespace(metaDataCtr.getFilename())) {
			fileName = metaDataCtr.getFilename();
		}
		
		File uploadedFile = fileEl.getUploadFile();
		if(resizeImg && fileName != null && imageExtPattern.matcher(fileName.toLowerCase()).find()
				&& resizeEl.isSelected(0)) {
			String extension = FileUtils.getFileSuffix(fileName);
			File imageScaled = new File(uploadedFile.getParentFile(), "scaled_" + uploadedFile.getName() + "." + extension);
			if(imageHelper.scaleImage(uploadedFile, extension, imageScaled, 1280, 1280, false) != null) {
				//problem happen, special GIF's (see bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6358674)
				//don't try to scale if not all ok 
				uploadedFile = imageScaled;
			}
		}
		
		// check if such a filename does already exist
		existingVFSItem = uploadVFSContainer.resolve(fileName);
		if (existingVFSItem == null) {
			uploadNewFile(ureq, uploadedFile, fileName);
		} else {
			// file already exists... upload anyway with new filename and
			// in the folder manager status.
			// rename file and ask user what to do
			if ( ! (existingVFSItem instanceof LocalImpl)) {
				throw new AssertException("Can only LocalImpl VFS items, don't know what to do with file of type::" + existingVFSItem.getClass().getCanonicalName());
			}

			String renamedFilename = VFSManager.rename(uploadVFSContainer, existingVFSItem.getName());
			newFile = uploadVFSContainer.createChildLeaf(renamedFilename);

			// Copy content to tmp file
			boolean success = false;
			try(InputStream in = new FileInputStream(uploadedFile);
					BufferedOutputStream out = new BufferedOutputStream(newFile.getOutputStream(false)))  {
				FileUtils.cpio(in, out, "");
				success = true;
			} catch (IOException e) {
				success = false;
			}				
			FileUtils.deleteFile(uploadedFile);
			
			if (success) {
				boolean locked = vfsLockManager.isLockedForMe(existingVFSItem, getIdentity(), VFSLockApplicationType.vfs, null);
				if (locked) {
					//the file is locked and cannot be overwritten
					lockedFileDialog(ureq, renamedFilename);
				} else if (existingVFSItem.canVersion() == VFSConstants.YES) {
					uploadVersionedFile(ureq, renamedFilename);
				} else {
					askOverwriteOrRename(ureq, renamedFilename);
				}
			} else {
				showError("failed");
				status = FolderCommandStatus.STATUS_FAILED;
				fireEvent(ureq, Event.FAILED_EVENT);					
			}
		}
	}
	
	private void lockedFileDialog(UserRequest ureq, String renamedFilename) {
		removeAsListenerAndDispose(lockedFileDialog);
		String title = translate("ul.lockedFile.title");
		String text = translate("ul.lockedFile.text", new String[] { existingVFSItem.getName(), renamedFilename} );
		lockedFileDialog = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), title, text,
				asList(translate("ul.overwrite.threeoptions.rename", renamedFilename), translate("ul.overwrite.threeoptions.cancel")));
		listenTo(lockedFileDialog);
		lockedFileDialog.activate();
	}
	
	private void uploadVersionedFile(UserRequest ureq, String renamedFilename) {
		VFSMetadata metadata = vfsRepositoryService.getMetadataFor(existingVFSItem);
		List<VFSRevision> revisions = vfsRepositoryService.getRevisions(metadata);
		int maxNumOfRevisions = getMaxNumOfRevisionsOfExistingVFSItem();
		if(maxNumOfRevisions == 0) {
			//it's possible if someone change the configuration
			// let calling method decide what to do.
			askOverwriteOrRename(ureq, renamedFilename);
		} else if(revisions.isEmpty() || maxNumOfRevisions < 0 || maxNumOfRevisions > revisions.size()) {
			// let calling method decide what to do.
			askNewVersionOrRename(ureq, renamedFilename);
		} else {
			//too many revisions
			askToReduceRevisionList(ureq, (VFSLeaf)existingVFSItem);
		}
	}
	
	private void askOverwriteOrRename(UserRequest ureq, String renamedFilename) {
		removeAsListenerAndDispose(overwriteDialog);
		// let calling method decide what to do.
		// for this, we put a list with "existing name" and "new name"
		overwriteDialog = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(),
				translate("ul.overwrite.threeoptions.title"), translate("ul.overwrite.threeoptions.text", new String[] {existingVFSItem.getName(), renamedFilename} ),
				asList(translate("ul.overwrite.threeoptions.overwrite"), translate("ul.overwrite.threeoptions.rename", renamedFilename), translate("ul.overwrite.threeoptions.cancel")));
		listenTo(overwriteDialog);
		overwriteDialog.activate();
	}
	
	private void askNewVersionOrRename(UserRequest ureq, String renamedFilename) {
		removeAsListenerAndDispose(overwriteDialog);
		overwriteDialog = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(),
				translate("ul.overwrite.threeoptions.title"), translate("ul.versionoroverwrite", new String[] {existingVFSItem.getName(), renamedFilename} ),
				asList(translate("ul.overwrite.threeoptions.newVersion"), translate("ul.overwrite.threeoptions.rename", renamedFilename), translate("ul.overwrite.threeoptions.cancel")));
		listenTo(overwriteDialog);
		overwriteDialog.activate();
	}
	
	private void uploadNewFile(UserRequest ureq, File uploadedFile, String filename) {
		// save file and finish
		newFile = uploadVFSContainer.createChildLeaf(filename);
		
		boolean success = true;
		if(newFile == null) {
			// FXOLAT-409 somehow "createChildLeaf" did not succeed...
			// if so, there is alread a error-msg in log (vfsContainer.createChildLeaf)
			success = false;
		} else {
			try(InputStream in = new FileInputStream(uploadedFile);
				OutputStream out = newFile.getOutputStream(false)) {
				FileUtils.cpio(in, out, "uploadTmpFileToDestFile");
			} catch (IOException e) {
				success = false;
			}
			FileUtils.deleteFile(uploadedFile);
		}
		
		if (success) {
			String filePath = (uploadRelPath == null ? "" : uploadRelPath + "/") + newFile.getName();
			finishSuccessfullUpload(filePath, newFile, true, ureq);
			fireEvent(ureq, Event.DONE_EVENT);										
		} else {
			showError("failed");
			status = FolderCommandStatus.STATUS_FAILED;
			fireEvent(ureq, Event.FAILED_EVENT);					
		}
	}
	
	private void finishUpload(UserRequest ureq, boolean forceMetdata) {
		// in both cases the upload must be finished and notified with a FolderEvent
		String filePath = (uploadRelPath == null ? "" : uploadRelPath + "/") + newFile.getName();
		VFSItem item = currentContainer.resolve(filePath);
		if(item != null) {
			finishSuccessfullUpload(filePath, item, forceMetdata, ureq);
		} else {
			logWarn("Upload with error:" + filePath, null);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	/**
	 * Internal helper to finish the upload and add metadata
	 */
	private void finishSuccessfullUpload(String filePath, VFSItem item, boolean forceMetadata, UserRequest ureq) {
		if (item instanceof VFSLeaf && item.canMeta() == VFSConstants.YES) {
			// create meta data
			VFSMetadata meta = item.getMetaInfo();
			if (metaDataCtr != null) {
				meta = metaDataCtr.getMetaInfo(meta, forceMetadata);
			}
			if (meta instanceof VFSMetadataImpl) {
				((VFSMetadataImpl)meta).setFileInitializedBy(getIdentity());
			}
			//clear write the meta
			vfsRepositoryService.updateMetadata(meta);
			vfsRepositoryService.itemSaved((VFSLeaf)item, getIdentity());
			vfsRepositoryService.resetThumbnails((VFSLeaf)item);
		}
		
		if(item == null) {
			logError("File cannot be uploaded: " + filePath, null);
		} else {
			ThreadLocalUserActivityLogger.log(FolderLoggingAction.FILE_UPLOADED, getClass(), CoreLoggingResourceable.wrapUploadFile(filePath));
			// Notify listeners about upload
			fireEvent(ureq, new FolderEvent(FolderEvent.UPLOAD_EVENT, item));
		}
	}

	/**
	 * @return The uploaded file or NULL if nothing uploaded
	 */
	public VFSLeaf getUploadedFile(){
		return newFile;
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
	public void setMaxUploadSizeKB(long uploadLimitKB) {
		this.uploadLimitKB = uploadLimitKB;
		String supportAddr = WebappHelper.getMailConfig("mailQuota");
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
		// Set upload directory from path
		uploadVFSContainer = VFSManager.resolveOrCreateContainerFromPath(currentContainer, uploadRelPath);
		if (uploadVFSContainer == null) {
			logError("Can not create upload rel path::" + uploadRelPath + ", fall back to current container", null);
			uploadVFSContainer = currentContainer;
		}
		
		// Update the destination path in the GUI
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
	
	public VFSContainer getUploadContainer() {
		return uploadVFSContainer;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		// Check sub path
		if (targetSubPath != null) {
			String subPath = targetSubPath.getValue();
			if (subPath != null) {
				// Cleanup first
				subPath = subPath.toLowerCase().trim();
				if (!validSubPathPattern.matcher(subPath).matches()) {
					targetSubPath.setErrorKey("subpath.error.characters", null);
					return false;
				} else {
					// Fix mess with slashes and dots
					// reduce doubled slashes with single slash
					subPath = subPath.replaceAll("\\.*\\/+\\.*", "\\/");
					// do it a second time to catch the double slashes created by previous replacement
					subPath = subPath.replaceAll("\\/+", "\\/");
					// remove slash at end
					if (subPath.endsWith("/")) {
						subPath = subPath.substring(0, subPath.length()-1);
					}
					// single slash means no sub-directory
					if (subPath.length() == 1 && subPath.startsWith("/")) {
						subPath = "";
					}				
					// fix missing slash at start
					if (subPath.length() > 0 && !subPath.startsWith("/")) {
						subPath = "/" + subPath;
					}
					// update in GUI so user sees how we optimized
					targetSubPath.setValue(subPath);
				}
				// Now check if this path does not already exist
				if (StringHelper.containsNonWhitespace(subPath)){
					// Try to resolve given rel path from current container
					VFSItem uploadDir = currentContainer.resolve(subPath);
					if (uploadDir != null) {
						// already exists. this is fine, as long as it is a directory and not a file
						if (!(uploadDir instanceof VFSContainer)) {
							// error
							targetSubPath.setErrorKey("subpath.error.dir.is.file", new String[] {subPath});
							return false;
						}
					}
				}
				targetSubPath.clearError();
			}
		}
		// Check file name
		if(metaDataCtr != null && StringHelper.containsNonWhitespace(metaDataCtr.getFilename())) {
			return validateFilename(metaDataCtr.getFilename(), metaDataCtr.getFilenameEl());
		}

		return validateFilename(fileEl);
	}
	
	private boolean validateFilename(FileElement itemEl) {
		boolean allOk = true;
		if(itemEl.validate()) {
			String filename = itemEl.getUploadFileName();
			if (!StringHelper.containsNonWhitespace(filename)) {
				itemEl.setErrorKey("NoFileChosen", null);
				allOk &= false;
			} else if(!FileUtils.validateFilename(filename)) {
				itemEl.setErrorKey("cfile.name.notvalid", null);
				allOk &= false;
			} else {
				allOk &= validateUri(filename, itemEl);
			}
			allOk &= validateQuota(itemEl);
		}
		
		itemEl.setDeleteEnabled(!allOk);
		return allOk;
	}
	
	private boolean validateFilename(String filename, FormItem itemEl) {
		itemEl.clearError();
		
		boolean allOk = true;
		if (!StringHelper.containsNonWhitespace(filename)) {
			itemEl.setErrorKey("NoFileChosen", null);
			allOk &= false;
		} else if(!FileUtils.validateFilename(filename)) {
			itemEl.setErrorKey("cfile.name.notvalid", null);
			allOk &= false;
		} else {
			allOk &= validateUri(filename, itemEl);
		}
		
		allOk &= validateQuota(fileEl);
		return allOk;
	}
	
	private boolean validateUri(String filename, FormItem itemEl) {
		boolean allOk = true;
		
		if(uriValidation) {
			try {
				URI uri = new URI(filename);
				uri.normalize();
			} catch(Exception e) {
				itemEl.setErrorKey("cfile.name.notvalid.uri", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	private boolean validateQuota(FileElement itemEl) {
		if (remainingQuotKB != -1  && itemEl.getUploadFile() != null
				&& itemEl.getUploadFile().length() / 1024 > remainingQuotKB) {
			String supportAddr = WebappHelper.getMailConfig("mailQuota");
			getWindowControl().setError(translate("ULLimitExceeded", new String[] { Formatter.roundToString((uploadLimitKB+0f) / 1000, 1), supportAddr }));
			return false;
		}
		return true;
	}
}
