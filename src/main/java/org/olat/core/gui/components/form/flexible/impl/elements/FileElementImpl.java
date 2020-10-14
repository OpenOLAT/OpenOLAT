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

package org.olat.core.gui.components.form.flexible.impl.elements;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.image.Crop;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.image.ImageFormItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.ValidationStatusImpl;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;

/**
 * <h3>Description:</h3>
 * <p>
 * Implementation of the file element. See the interface for more documentation.
 * <p>
 * The class implements the disposable interface to cleanup temporary files on
 * form disposal.
 * <p>
 * Initial Date: 08.12.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class FileElementImpl extends FormItemImpl
		implements FileElement, FormItemCollection, ControllerEventListener, Disposable {

	private static final Logger log = Tracing.createLoggerFor(FileElementImpl.class);

	private final FileElementComponent component;
	private ImageFormItem previewEl;

	private File initialFile;
	private File tempUploadFile;
	private Set<String> mimeTypes;
	private long maxUploadSizeKB = UPLOAD_UNLIMITED;
	private String uploadFilename;
	private String uploadMimeType;

	private boolean buttonsEnabled = true;
	private boolean deleteEnabled;
	private boolean confirmDelete;

	private boolean checkForMaxFileSize = false;
	private boolean checkForMimeTypes = false;
	private boolean cropSelectionEnabled = false;
	private boolean area = true;
	// error keys
	private String i18nErrMandatory;
	private String i18nErrMaxSize;
	private String i18nErrMimeType;
	private String[] i18nErrMaxSizeArgs;
	private String[] i18nErrMimeTypeArgs;
	
	private String fileExampleKey;
	private String[] fileExampleParams;

	private WindowControl wControl;

	/**
	 * Constructor for a file element. Use the limitToMimeType and setter
	 * methods to configure the element
	 * 
	 * @param name
	 */
	public FileElementImpl(WindowControl wControl, String name) {
		super(name);
		this.wControl = wControl;
		component = new FileElementComponent(this);
		setElementCssClass(null); // trigger default css 
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		Form form = getRootForm();

		String dispatchuri = form.getRequestParameter("dispatchuri");
		if (dispatchuri != null && dispatchuri.equals(component.getFormDispatchId())) {
			if ("delete".equals(form.getRequestParameter("delete"))) {
				if (isConfirmDelete()) {
					doConfirmDelete(ureq);
				} else {
					getRootForm().fireFormEvent(ureq,
							new FileElementEvent(FileElementEvent.DELETE, this, FormEvent.ONCLICK));
				}
			}
		}
		Set<String> keys = form.getRequestMultipartFilesSet();
		if (keys.size() > 0 && keys.contains(component.getFormDispatchId())) {
			// Remove old files first
			if (tempUploadFile != null && tempUploadFile.exists()) {
				FileUtils.deleteFile(tempUploadFile);
			}
			// Move file from a temporary request scope location to a location
			// with a
			// temporary form item scope. The file must be moved later using the
			// moveUploadFileTo() method to the final destination.
			tempUploadFile = new File(WebappHelper.getTmpDir(), CodeHelper.getUniqueID());
			File tmpRequestFile = form.getRequestMultipartFile(component.getFormDispatchId());
			// Move file to internal temp location
			boolean success = tmpRequestFile.renameTo(tempUploadFile);
			if (!success) {
				// try to move file by copying it, command above might fail
				// when source and target are on different volumes
				FileUtils.copyFileToFile(tmpRequestFile, tempUploadFile, true);
			}

			uploadFilename = form.getRequestMultipartFileName(component.getFormDispatchId());
			// prevent an issue with different operation systems and .
			uploadFilename = FileUtils.cleanFilename(uploadFilename);
			// use mime-type from file name to have deterministic mime types
			uploadMimeType = WebappHelper.getMimeType(uploadFilename);
			if (uploadMimeType == null) {
				// use browser mime type as fallback if unknown
				uploadMimeType = form.getRequestMultipartFileMimeType(component.getFormDispatchId());
			}
			if (uploadMimeType == null) {
				// use application fallback for worst case
				uploadMimeType = "application/octet-stream";
			}

			if (previewEl != null && uploadMimeType != null
					&& (uploadMimeType.startsWith("image/") || uploadMimeType.startsWith("video/"))) {
				VFSLeaf media = new LocalFileImpl(tempUploadFile);
				previewEl.setMedia(media, uploadMimeType);
				previewEl.setCropSelectionEnabled(cropSelectionEnabled);
				previewEl.setMaxWithAndHeightToFitWithin(300, 200);
				previewEl.setVisible(true);
			} else if(previewEl != null) {
				previewEl.setVisible(false);
			}
			// Mark associated component dirty, that it gets rerendered
			component.setDirty(true);
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
			getRootForm().fireFormEvent(ureq, new FileElementEvent(FileElementEvent.DELETE, this, FormEvent.ONCLICK));
		}
	}

	private void doConfirmDelete(UserRequest ureq) {
		Translator fileTranslator = Util.createPackageTranslator(FileElementImpl.class, ureq.getLocale(),
				getTranslator());
		String title = fileTranslator.translate("confirm.delete.file.title");
		String text = fileTranslator.translate("confirm.delete.file");
		DialogBoxController dialogCtr = DialogBoxUIFactory.createOkCancelDialog(ureq, wControl, title, text);
		dialogCtr.addControllerListener(this);
		dialogCtr.activate();
	}

	@Override
	public Iterable<FormItem> getFormItems() {
		if (previewEl != null) {
			return Collections.<FormItem>singletonList(previewEl);
		}
		return Collections.emptyList();
	}

	@Override
	public FormItem getFormComponent(String name) {
		if (previewEl != null && previewEl.getName().equals(name)) {
			return previewEl;
		}
		return null;
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	protected ImageFormItem getPreviewFormItem() {
		return previewEl;
	}

	@Override
	public void reset() {
		if (tempUploadFile != null && tempUploadFile.exists()) {
			FileUtils.deleteFile(tempUploadFile);
		}
		tempUploadFile = null;
		if (previewEl != null) {
			if (initialFile != null) {
				VFSLeaf media = new LocalFileImpl(initialFile);
				previewEl.setMedia(media);
				previewEl.setMaxWithAndHeightToFitWithin(300, 200);
				previewEl.setVisible(true);
			} else {
				previewEl.setVisible(false);
			}
		}
		uploadFilename = null;
		uploadMimeType = null;
	}

	@Override
	protected void rootFormAvailable() {
		if (previewEl != null && previewEl.getRootForm() != getRootForm()) {
			previewEl.setRootForm(getRootForm());
		}
	}

	@Override
	public void setMandatory(boolean mandatory, String i18nErrKey) {
		super.setMandatory(mandatory);
		this.i18nErrMandatory = i18nErrKey;
	}

	@Override
	public void validate(List<ValidationStatus> validationResults) {
		int lastFormError = getRootForm().getLastRequestError();
		if (lastFormError == Form.REQUEST_ERROR_UPLOAD_LIMIT_EXCEEDED) {
			// check if total upload limit is exceeded (e.g. sum of files)
			setErrorKey(i18nErrMaxSize, i18nErrMaxSizeArgs);
			validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
			return;

			// check for a general error
		} else if (lastFormError == Form.REQUEST_ERROR_GENERAL) {
			setErrorKey("file.element.error.general", null);
			validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
			return;

			// check if uploaded at all
		} else if (isMandatory() && ((initialFile == null && (tempUploadFile == null || !tempUploadFile.exists()))
				|| (initialFile != null && tempUploadFile != null && !tempUploadFile.exists()))) {
			setErrorKey(i18nErrMandatory, null);
			validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
			return;

			// check for file size of current file
		} else if (checkForMaxFileSize && tempUploadFile != null && tempUploadFile.exists()
				&& tempUploadFile.length() > maxUploadSizeKB * 1024l) {
			setErrorKey(i18nErrMaxSize, i18nErrMaxSizeArgs);
			validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
			return;

			// check for mime types
		} else if (checkForMimeTypes && tempUploadFile != null && tempUploadFile.exists()) {
			boolean found = false;
			if (uploadMimeType != null) {
				for (String validType : getMimeTypeLimitations()) {
					if (validType.equals(uploadMimeType)) {
						// exact match: image/jpg
						found = true;
						break;
					} else if (validType.endsWith("/*")) {
						// wildcard match: image/*
						if (uploadMimeType != null
								&& uploadMimeType.startsWith(validType.substring(0, validType.length() - 2))) {
							found = true;
							break;
						}
					}
				}
			}
			if (!found) {
				setErrorKey(i18nErrMimeType, i18nErrMimeTypeArgs);
				validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
				return;
			}
		}
		// No error, clear errors from previous attempts
		clearError();
	}

	@Override
	public String getExampleText() {
		if (fileExampleKey != null) {
			if (fileExampleParams != null) {
				return translator.translate(fileExampleKey, fileExampleParams);
			}
			return translator.translate(fileExampleKey);
		}
		return null;
	}

	@Override
	public void setExampleKey(String exampleKey, String[] params) {
		this.fileExampleKey = exampleKey;
		this.fileExampleParams = params;
	}

	@Override
	public void setPreview(UserSession usess, boolean enable) {
		if (enable) {
			previewEl = new ImageFormItem(usess, this.getName() + "_PREVIEW");
			previewEl.setRootForm(getRootForm());
		} else {
			previewEl = null;
		}
	}

	/**
	 * Render the file element in a preview style where the actual file upload
	 * functionality is disabled but the element looks like an actual upload
	 * element and not like the disabled element. 
	 * 
	 * @return
	 */
	public boolean isButtonsEnabled() {
		return buttonsEnabled;
	}

	@Override
	public void setButtonsEnabled(boolean buttonsEnabled) {
		this.buttonsEnabled = buttonsEnabled;
	}

	@Override
	public void setCropSelectionEnabled(boolean enable) {
		this.cropSelectionEnabled = enable;
	}

	@Override
	public boolean isArea() {
		return area;
	}

	@Override
	public void setArea(boolean area) {
		this.area = area;
	}

	@Override
	public void setInitialFile(File initialFile) {
		this.initialFile = initialFile;
		if (initialFile != null && previewEl != null) {
			VFSLeaf media = new LocalFileImpl(initialFile);
			previewEl.setMedia(media);
			previewEl.setMaxWithAndHeightToFitWithin(300, 200);
			previewEl.setVisible(true);
		} else if (previewEl != null) {
			previewEl.setVisible(false);
		}
	}

	@Override
	public File getInitialFile() {
		return initialFile;
	}

	@Override
	public void limitToMimeType(Set<String> mimeTypes, String i18nErrKey, String[] i18nArgs) {
		if (mimeTypes != null) {
			this.mimeTypes = mimeTypes;
			this.checkForMimeTypes = true;
		} else {
			this.mimeTypes = new HashSet<>();
			this.checkForMimeTypes = false;
		}
		this.i18nErrMimeType = i18nErrKey;
		this.i18nErrMimeTypeArgs = i18nArgs;
	}

	@Override
	public Set<String> getMimeTypeLimitations() {
		if (mimeTypes == null)
			mimeTypes = new HashSet<>();
		return mimeTypes;
	}

	@Override
	public void setMaxUploadSizeKB(long maxUploadSizeKB, String i18nErrKey, String[] i18nArgs) {
		this.maxUploadSizeKB = maxUploadSizeKB;
		this.checkForMaxFileSize = (maxUploadSizeKB == UPLOAD_UNLIMITED ? false : true);
		this.i18nErrMaxSize = i18nErrKey;
		this.i18nErrMaxSizeArgs = i18nArgs;
	}

	@Override
	public long getMaxUploadSizeKB() {
		return maxUploadSizeKB;
	}

	@Override
	public boolean isDeleteEnabled() {
		return deleteEnabled;
	}

	@Override
	public void setDeleteEnabled(boolean deleteEnabled) {
		this.deleteEnabled = deleteEnabled;
	}

	public boolean isConfirmDelete() {
		return confirmDelete;
	}

	public void setConfirmDelete(boolean confirmDelete) {
		this.confirmDelete = confirmDelete;
	}

	@Override
	public boolean isUploadSuccess() {
		if (tempUploadFile != null && tempUploadFile.exists()) {
			return true;
		}
		return false;
	}

	@Override
	public String getUploadFileName() {
		return uploadFilename;
	}

	@Override
	public void setUploadFileName(String uploadFileName) {
		this.uploadFilename = uploadFileName;
		this.uploadMimeType = WebappHelper.getMimeType(uploadFilename);
	}

	@Override
	public String getUploadMimeType() {
		return uploadMimeType;
	}

	@Override
	public File getUploadFile() {
		return tempUploadFile;
	}

	@Override
	public InputStream getUploadInputStream() {
		if (tempUploadFile == null)
			return null;
		try {
			return new BufferedInputStream(new FileInputStream(tempUploadFile), FileUtils.BSIZE);
		} catch (FileNotFoundException e) {
			log.error("Could not open stream for file element::" + getName(), e);
		}
		return null;
	}

	@Override
	public long getUploadSize() {
		if (tempUploadFile != null && tempUploadFile.exists()) {
			return tempUploadFile.length();
		} else if (initialFile != null && initialFile.exists()) {
			return initialFile.length();
		} else {
			return 0;
		}
	}

	@Override
	public File moveUploadFileTo(File destinationDir) {
		if (tempUploadFile != null && tempUploadFile.exists()) {
			destinationDir.mkdirs();
			// Check if such a file does already exist, if yes rename new file
			File existsFile = new File(destinationDir, uploadFilename);
			if (existsFile.exists()) {
				// Use standard rename policy
				File tmpF = new File(uploadFilename);
				uploadFilename = FileUtils.rename(tmpF);
			}
			// Move file now
			File targetFile = new File(destinationDir, uploadFilename);
			if (FileUtils.copyFileToFile(tempUploadFile, targetFile, true)) {
				return targetFile;
			}
		}
		return null;
	}

	@Override
	public VFSLeaf moveUploadFileTo(VFSContainer destinationContainer) {
		return moveUploadFileTo(destinationContainer, false);
	}

	@Override
	public VFSLeaf moveUploadFileTo(VFSContainer destinationContainer, boolean crop) {
		VFSLeaf targetLeaf = null;
		if (tempUploadFile != null && tempUploadFile.exists()) {
			// Check if such a file does already exist, if yes rename new file
			VFSItem existsChild = destinationContainer.resolve(uploadFilename);
			if (existsChild != null) {
				// Use standard rename policy
				uploadFilename = VFSManager.rename(destinationContainer, uploadFilename);
			}
			// Create target leaf file now and delete original temp file
			if (destinationContainer instanceof LocalFolderImpl) {
				// Optimize for local files (don't copy, move instead)
				LocalFolderImpl folderContainer = (LocalFolderImpl) destinationContainer;
				File destinationDir = folderContainer.getBasefile();
				File targetFile = new File(destinationDir, uploadFilename);

				Crop cropSelection = previewEl == null ? null : previewEl.getCropSelection();
				if (crop && cropSelection != null) {
					CoreSpringFactory.getImpl(ImageService.class).cropImage(tempUploadFile, targetFile, cropSelection);
					targetLeaf = (VFSLeaf) destinationContainer.resolve(targetFile.getName());
					CoreSpringFactory.getImpl(VFSRepositoryService.class).itemSaved(targetLeaf);
				} else if (FileUtils.copyFileToFile(tempUploadFile, targetFile, true)) {
					targetLeaf = (VFSLeaf) destinationContainer.resolve(targetFile.getName());
					CoreSpringFactory.getImpl(VFSRepositoryService.class).itemSaved(targetLeaf);
				} else {
					log.error("Error after copying content from temp file, cannot copy file::"
							+ (tempUploadFile == null ? "NULL" : tempUploadFile) + " - "
							+ (targetFile == null ? "NULL" : targetFile));
				}

				if (targetLeaf == null) {
					log.error("Error after copying content from temp file, cannot resolve copied file::"
							+ (tempUploadFile == null ? "NULL" : tempUploadFile) + " - "
							+ (targetFile == null ? "NULL" : targetFile));
				}
			} else {
				// Copy stream in case the destination is a non-local container
				VFSLeaf leaf = destinationContainer.createChildLeaf(uploadFilename);
				boolean success = false;
				try {
					success = VFSManager.copyContent(tempUploadFile, leaf);
				} catch (Exception e) {
					log.error("Error while copying content from temp file: {}", tempUploadFile, e);
				}
				if (success) {
					// Delete original temp file after copy to simulate move
					// behavior
					FileUtils.deleteFile(tempUploadFile);
					targetLeaf = leaf;
				}
			}
		} else if (log.isDebugEnabled()) {
			log.debug("Error while copying content from temp file, no temp file::"
					+ (tempUploadFile == null ? "NULL" : tempUploadFile.getAbsolutePath()));
		}
		return targetLeaf;

	}

	@Override
	public void setElementCssClass(String elementCssClass) {
		// make sure the o_fileElement class is always set to trigger special
		// rendering for all file elements (error handling)
		if (StringHelper.containsNonWhitespace(elementCssClass)) {
			super.setElementCssClass(elementCssClass + " o_fileElement");
		} else {
			super.setElementCssClass("o_fileElement");
		}
	}

	@Override
	public void dispose() {
		if (tempUploadFile != null && tempUploadFile.exists()) {
			FileUtils.deleteFile(tempUploadFile);
		}
	}

}
