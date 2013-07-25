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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.control.Disposable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.ValidationStatusImpl;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;

import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
import com.oreilly.servlet.multipart.FileRenamePolicy;

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

public class FileElementImpl extends FormItemImpl implements FileElement, Disposable {
	private static final OLog log = Tracing.createLoggerFor(FileElementImpl.class);
	
	protected FileElementComponent component;
	//
	private File initialFile, tempUploadFile;
	private Set<String> mimeTypes;
	private int maxUploadSizeKB = UPLOAD_UNLIMITED;
	private String uploadFilename;
	private String uploadMimeType;
	//
	private boolean checkForMaxFileSize = false;
	private boolean checkForMimeTypes = false;
	// error keys
	private String i18nErrMandatory;
	private String i18nErrMaxSize;
	private String i18nErrMimeType;
	private String[] i18nErrMaxSizeArgs;
	private String[] i18nErrMimeTypeArgs;

	/**
	 * Constructor for a file element. Use the limitToMimeType and setter methods
	 * to configure the element
	 * 
	 * @param name
	 */
	public FileElementImpl(String name) {
		super(name);
		this.component = new FileElementComponent(this);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#evalFormRequest(org.olat.core.gui.UserRequest)
	 */
	public void evalFormRequest(UserRequest ureq) {
		Set<String> keys = getRootForm().getRequestMultipartFilesSet();
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
			File tmpRequestFile = getRootForm().getRequestMultipartFile(component.getFormDispatchId());
			// Move file to internal temp location
			boolean success = tmpRequestFile.renameTo(tempUploadFile);
			if (!success) {
				// try to move file by copying it, command above might fail
				// when source and target are on different volumes
				FileUtils.copyFileToFile(tmpRequestFile, tempUploadFile, true);
			}

			uploadFilename = getRootForm().getRequestMultipartFileName(component.getFormDispatchId());
			//prevent an issue with Firefox
			uploadFilename = Normalizer.normalize(uploadFilename, Normalizer.Form.NFKC);
			uploadMimeType = getRootForm().getRequestMultipartFileMimeType(component.getFormDispatchId());
			if (uploadMimeType == null) {
				// use fallback: mime-type form file name
				uploadMimeType = WebappHelper.getMimeType(uploadFilename);
			}
			// Mark associated component dirty, that it gets rerendered
			component.setDirty(true);
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#getFormItemComponent()
	 */
	protected Component getFormItemComponent() {
		return this.component;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#reset()
	 */
	public void reset() {
		if (tempUploadFile != null && tempUploadFile.exists()) {
			tempUploadFile.delete();
			tempUploadFile = null;
		}
		uploadFilename = null;
		uploadMimeType = null;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#rootFormAvailable()
	 */
	protected void rootFormAvailable() {
	//
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#setMandatory(boolean,
	 *      java.lang.String)
	 */
	public void setMandatory(boolean mandatory, String i18nErrKey) {
		super.setMandatory(mandatory);
		this.i18nErrMandatory = i18nErrKey;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormItemImpl#validate(java.util.List)
	 */
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
		} else if (isMandatory() && (
										(initialFile == null && (tempUploadFile == null || !tempUploadFile.exists()))
								|| 	(initialFile != null && tempUploadFile != null && !tempUploadFile.exists()))  
									) {
			setErrorKey(i18nErrMandatory, null);
			validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
			return;

			// check for file size of current file
		} else if (checkForMaxFileSize && tempUploadFile != null && tempUploadFile.exists() && tempUploadFile.length() > maxUploadSizeKB * 1024l) {
			setErrorKey(i18nErrMaxSize, i18nErrMaxSizeArgs);
			validationResults.add(new ValidationStatusImpl(ValidationStatus.ERROR));
			return;

			// check for mime types
		} else if (checkForMimeTypes && tempUploadFile != null && tempUploadFile.exists()) {
			boolean found = false;
			// Fix problem with upload mimetype: if the mimetype differs from the
			// mimetype the webapp helper generates from the file name the match won't work
			String mimeFromWebappHelper = WebappHelper.getMimeType(uploadFilename);
			if (uploadMimeType != null || mimeFromWebappHelper != null) {
				for (String validType : mimeTypes) {
					if (validType.equals(uploadMimeType) || validType.equals(mimeFromWebappHelper)) {
						// exact match: image/jpg
						found = true;
						break;
					} else if (validType.endsWith("/*")) {
						// wildcard match: image/*
						if (uploadMimeType != null && uploadMimeType.startsWith(validType.substring(0, validType.length() - 2))) {
							found = true;
							break;
						} else if (mimeFromWebappHelper != null && mimeFromWebappHelper.startsWith(validType.substring(0, validType.length() - 2))) {
							// fallback to mime type from filename
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

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#setInitialFile(java.io.File)
	 */
	public void setInitialFile(File initialFile) {
		this.initialFile = initialFile;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#getInitialFile()
	 */
	public File getInitialFile() {
		return this.initialFile;
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
		if (mimeTypes == null) mimeTypes = new HashSet<String>();
		return mimeTypes;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#setMaxUploadSizeKB(int,
	 *      java.lang.String, java.lang.String[])
	 */
	public void setMaxUploadSizeKB(int maxUploadSizeKB, String i18nErrKey, String[] i18nArgs) {
		this.maxUploadSizeKB = maxUploadSizeKB;
		this.checkForMaxFileSize = (maxUploadSizeKB == UPLOAD_UNLIMITED ? false : true);
		this.i18nErrMaxSize = i18nErrKey;
		this.i18nErrMaxSizeArgs = i18nArgs;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormMultipartItem#getMaxUploadSizeKB()
	 */
	public int getMaxUploadSizeKB() {
		return this.maxUploadSizeKB;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#isUploadSuccess()
	 */
	public boolean isUploadSuccess() {
		if (tempUploadFile != null && tempUploadFile.exists()) { return true; }
		return false;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#getUploadFileName()
	 */
	public String getUploadFileName() {
		return this.uploadFilename;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#getUploadMimeType()
	 */
	public String getUploadMimeType() {
		return this.uploadMimeType;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#getUploadFile()
	 */
	public File getUploadFile() {
		return this.tempUploadFile;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#getUploadInputStream()
	 */
	public InputStream getUploadInputStream() {
		if (this.tempUploadFile == null) return null;
		try {
			return new FileInputStream(this.tempUploadFile);
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
				FileRenamePolicy frp = new DefaultFileRenamePolicy();
				File tmpF = new File(uploadFilename);
				uploadFilename = frp.rename(tmpF).getName();
			}
			// Move file now
			File targetFile = new File(destinationDir, uploadFilename);
			if (FileUtils.copyFileToFile(tempUploadFile, targetFile, true)) { return targetFile; }
		}
		return null;
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.elements.FileElement#moveUploadFileTo(org.olat.core.util.vfs.VFSContainer)
	 */
	public VFSLeaf moveUploadFileTo(VFSContainer destinationContainer) {
		VFSLeaf targetLeaf = null;
		if (tempUploadFile != null && tempUploadFile.exists()) {
			// Check if such a file does already exist, if yes rename new file
			VFSItem existsChild = destinationContainer.resolve(uploadFilename);
			if (existsChild != null) {
				// Use standard rename policy
				FileRenamePolicy frp = new DefaultFileRenamePolicy();
				File tmpF = new File(uploadFilename);
				uploadFilename = frp.rename(tmpF).getName();
				if(log.isDebug()) {
					log.debug("FileElement rename policy::" + tmpF.getName() + " -> " + uploadFilename);
				}
			}
			// Create target leaf file now and delete original temp file
			if (destinationContainer instanceof LocalFolderImpl) {
				// Optimize for local files (don't copy, move instead)
				LocalFolderImpl folderContainer = (LocalFolderImpl) destinationContainer;
				File destinationDir = folderContainer.getBasefile();
				File targetFile = new File(destinationDir, uploadFilename);
				if (FileUtils.copyFileToFile(tempUploadFile, targetFile, true)) { 
					targetLeaf = (VFSLeaf) destinationContainer.resolve(targetFile.getName());
					if(targetLeaf == null) {
						log.error("Error after copying content from temp file, cannot resolve copied file::" 
								+ (tempUploadFile == null ? "NULL" : tempUploadFile) + " - " + (targetFile == null ? "NULL" : targetFile), null);
					}
				}	else {
					log.error("Error after copying content from temp file, cannot copy file::" 
							+ (tempUploadFile == null ? "NULL" : tempUploadFile) + " - " + (targetFile == null ? "NULL" : targetFile), null);
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
					// Delete original temp file after copy to simulate move behavior
					tempUploadFile.delete();				
					targetLeaf = leaf;
				}
			}
		} else if(log.isDebug()) {
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
