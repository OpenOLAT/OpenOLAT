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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.image.Crop;
import org.olat.core.commons.services.image.ImageService;
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
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
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

	private static final OLog log = Tracing.createLoggerFor(FileElementImpl.class);

	private final FileElementComponent component;
	private ImageFormItem previewEl;

	private File initialFile, tempUploadFile;
	private Set<String> mimeTypes;
	private long maxUploadSizeKB = UPLOAD_UNLIMITED;
	private String uploadFilename;
	private String uploadMimeType;

	private boolean deleteEnabled;
	private boolean confirmDelete;

	private boolean checkForMaxFileSize = false;
	private boolean checkForMimeTypes = false;
	private boolean cropSelectionEnabled = false;
	// error keys
	private String i18nErrMandatory;
	private String i18nErrMaxSize;
	private String i18nErrMimeType;
	private String[] i18nErrMaxSizeArgs;
	private String[] i18nErrMimeTypeArgs;

	private WindowControl wControl;
	private DialogBoxController dialogCtr;

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
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#evalFormRequest(org.olat.core.gui.UserRequest)
	 */
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
				tempUploadFile.delete();
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
			// prevent an issue with Firefox
			uploadFilename = Normalizer.normalize(uploadFilename, Normalizer.Form.NFKC);
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
		Translator translator = Util.createPackageTranslator(FileElementImpl.class, ureq.getLocale(), getTranslator());
		String title = translator.translate("confirm.delete.file.title");
		String text = translator.translate("confirm.delete.file");
		dialogCtr = DialogBoxUIFactory.createOkCancelDialog(ureq, wControl, title, text);
		dialogCtr.addControllerListener(this);
		dialogCtr.activate();
	}

	@Override
	public Iterable<FormItem> getFormItems() {
		if (previewEl != null) {
			return Collections.<FormItem> singletonList(previewEl);
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

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#getFormItemComponent()
	 */
	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	protected ImageFormItem getPreviewFormItem() {
		return previewEl;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#reset()
	 */
	@Override
	public void reset() {
		if (tempUploadFile != null && tempUploadFile.exists()) {
			tempUploadFile.delete();
		}
		tempUploadFile = null;
		if (previewEl != null) {
			if (initialFile != null) {
				VFSLeaf media = new LocalFileImpl(initialFile);
				previewEl.setMedia(media);
				previewEl.setMaxWithAndHeightToFitWithin(300, 200);
				previewEl.setVisible(true);
			} else if (previewEl != null) {
				previewEl.setVisible(false);
			}
		}
		uploadFilename = null;
		uploadMimeType = null;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#rootFormAvailable()
	 */
	@Override
	protected void rootFormAvailable() {
		if (previewEl != null && previewEl.getRootForm() != getRootForm()) {
			previewEl.setRootForm(getRootForm());
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#setMandatory(boolean,
	 *      java.lang.String)
	 */
	@Override
	public void setMandatory(boolean mandatory, String i18nErrKey) {
		super.setMandatory(mandatory);
		this.i18nErrMandatory = i18nErrKey;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#validate(java.util.List)
	 */
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
				for (String validType : mimeTypes) {
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
	public void setPreview(UserSession usess, boolean enable) {
		if (enable) {
			previewEl = new ImageFormItem(usess, this.getName() + "_PREVIEW");
			previewEl.setRootForm(getRootForm());
		} else {
			previewEl = null;
		}
	}

	@Override
	public void setCropSelectionEnabled(boolean enable) {
		this.cropSelectionEnabled = enable;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#setInitialFile(java.io.File)
	 */
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

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#getInitialFile()
	 */
	public File getInitialFile() {
		return initialFile;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#limitToMimeType(java.util.Set,
	 *      java.lang.String, java.lang.String[])
	 */
	public void limitToMimeType(Set<String> mimeTypes, String i18nErrKey, String[] i18nArgs) {
		this.mimeTypes = mimeTypes;
		this.checkForMimeTypes = true;
		this.i18nErrMimeType = i18nErrKey;
		this.i18nErrMimeTypeArgs = i18nArgs;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#getMimeTypeLimitations()
	 */
	public Set<String> getMimeTypeLimitations() {
		if (mimeTypes == null)
			mimeTypes = new HashSet<String>();
		return mimeTypes;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#setMaxUploadSizeKB(int,
	 *      java.lang.String, java.lang.String[])
	 */
	public void setMaxUploadSizeKB(long maxUploadSizeKB, String i18nErrKey, String[] i18nArgs) {
		this.maxUploadSizeKB = maxUploadSizeKB;
		this.checkForMaxFileSize = (maxUploadSizeKB == UPLOAD_UNLIMITED ? false : true);
		this.i18nErrMaxSize = i18nErrKey;
		this.i18nErrMaxSizeArgs = i18nArgs;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormMultipartItem#getMaxUploadSizeKB()
	 */
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

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#isUploadSuccess()
	 */
	public boolean isUploadSuccess() {
		if (tempUploadFile != null && tempUploadFile.exists()) {
			return true;
		}
		return false;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#getUploadFileName()
	 */
	public String getUploadFileName() {
		return uploadFilename;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#getUploadMimeType()
	 */
	public String getUploadMimeType() {
		return uploadMimeType;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#getUploadFile()
	 */
	public File getUploadFile() {
		return tempUploadFile;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#getUploadInputStream()
	 */
	public InputStream getUploadInputStream() {
		if (tempUploadFile == null)
			return null;
		try {
			return new FileInputStream(tempUploadFile);
		} catch (FileNotFoundException e) {
			log.error("Could not open stream for file element::" + getName(), e);
		}
		return null;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#getUploadSize()
	 */
	public long getUploadSize() {
		if (tempUploadFile != null && tempUploadFile.exists()) {
			return tempUploadFile.length();
		} else if (initialFile != null && initialFile.exists()) {
			return initialFile.length();
		} else {
			return 0;
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#moveUploadFileTo(java.io.File)
	 */
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

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#moveUploadFileTo(org.olat.core.util.vfs.VFSContainer)
	 */
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
				} else if (FileUtils.copyFileToFile(tempUploadFile, targetFile, true)) {
					targetLeaf = (VFSLeaf) destinationContainer.resolve(targetFile.getName());
				} else {
					log.error("Error after copying content from temp file, cannot copy file::"
							+ (tempUploadFile == null ? "NULL" : tempUploadFile) + " - "
							+ (targetFile == null ? "NULL" : targetFile), null);
				}

				if (targetLeaf == null) {
					log.error("Error after copying content from temp file, cannot resolve copied file::"
							+ (tempUploadFile == null ? "NULL" : tempUploadFile) + " - "
							+ (targetFile == null ? "NULL" : targetFile), null);
				}
			} else {
				// Copy stream in case the destination is a non-local container
				VFSLeaf leaf = destinationContainer.createChildLeaf(uploadFilename);
				boolean success = false;
				try {
					success = VFSManager.copyContent(new FileInputStream(tempUploadFile), leaf);
				} catch (FileNotFoundException e) {
					log.error("Error while copying content from temp file::"
							+ (tempUploadFile == null ? "NULL" : tempUploadFile.getAbsolutePath()), e);
				}
				if (success) {
					// Delete original temp file after copy to simulate move
					// behavior
					tempUploadFile.delete();
					targetLeaf = leaf;
				}
			}
		} else if (log.isDebug()) {
			log.debug("Error while copying content from temp file, no temp file::"
					+ (tempUploadFile == null ? "NULL" : tempUploadFile.getAbsolutePath()));
		}
		return targetLeaf;

	}

	/**
	 * @see org.olat.core.gui.control.Disposable#dispose()
	 */
	public void dispose() {
		if (tempUploadFile != null && tempUploadFile.exists()) {
			tempUploadFile.delete();
		}
	}

}
