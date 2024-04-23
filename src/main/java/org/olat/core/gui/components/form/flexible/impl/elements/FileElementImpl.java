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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
import org.olat.core.gui.components.form.flexible.elements.FileElementInfos;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.MultipartFileInfos;
import org.olat.core.gui.components.image.ImageFormItem;
import org.olat.core.gui.control.Disposable;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
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
		implements FileElement, FormItemCollection, Disposable {

	private static final Logger log = Tracing.createLoggerFor(FileElementImpl.class);

	private final FileElementComponent component;
	private ImageFormItem initialPreviewEl;

	private File initialFile;
	private String preferredUploadFilename;
	private final List<FileElementInfos> tempUploadFiles = new ArrayList<>();
	private Set<String> mimeTypes;
	private long maxUploadSizeKB = UPLOAD_UNLIMITED;
	private int maxUploadFiles = -1;
	private int maxFilenameLength = -1;

	private boolean preview;
	private boolean multiFileUpload;
	private boolean dragAndDropForm;
	private boolean buttonsEnabled = true;
	private boolean replaceButton;
	private boolean deleteEnabled;
	private boolean showInputIfFileUploaded = true;

	private boolean checkForMaxFileSize = false;
	private boolean checkForMimeTypes = false;
	private boolean cropSelectionEnabled = false;
	// error keys
	private String i18nErrMandatory;
	private String i18nErrMaxSize;
	private String i18nErrMimeType;
	private String i18nErrMaxFiles;
	private String[] i18nErrMaxSizeArgs;
	private String[] i18nErrMimeTypeArgs;
	
	private String fileExampleKey;
	private String[] fileExampleParams;
	
	private String dndInformations;

	private Identity savedBy;

	/**
	 * Constructor for a file element. Use the limitToMimeType and setter
	 * methods to configure the element
	 * 
	 * @param name
	 */
	public FileElementImpl(Identity savedBy, String name) {
		super(name);
		this.savedBy = savedBy;
		component = new FileElementComponent(this);
		setElementCssClass(null); // trigger default css 
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		Form form = getRootForm();

		String dispatchuri = form.getRequestParameter("dispatchuri");
		String uploadDirectory = form.getRequestParameter("upload-folder");
		if (dispatchuri != null && dispatchuri.equals(component.getFormDispatchId())
				&& "delete".equals(form.getRequestParameter("delete"))) {
			File file = getFileByFilename(form.getRequestParameter("filename"));
			getRootForm().fireFormEvent(ureq, new DeleteFileElementEvent(this, file, FormEvent.ONCLICK));
		}
		
		List<MultipartFileInfos> list = form.getRequestMultipartFileInfosList(component.getFormDispatchId());
		if (!list.isEmpty()) {
			// Remove old files first
			if (!multiFileUpload && !tempUploadFiles.isEmpty()) {
				deleteTempUploadFiles();
			}
			
			List<File> tempUploadFileList = new ArrayList<>(list.size());
			for(MultipartFileInfos fileInfos:list) {
				File tempUploadFile = evaluateFile(fileInfos, ureq.getUserSession());
				tempUploadFileList.add(tempUploadFile);
			}

			// Mark associated component dirty, that it gets rerendered
			component.setDirty(true);
			validate();
			
			getRootForm().fireFormEvent(ureq, new UploadFileElementEvent(this, tempUploadFileList, uploadDirectory, FormEvent.ONCHANGE));
		}
		
		if(initialPreviewEl != null && initialFile != null) {
			initialPreviewEl.setVisible(tempUploadFiles.isEmpty());
		}
	}
	
	private File evaluateFile(MultipartFileInfos fileInfos, UserSession usess) {
		// Move file from a temporary request scope location to a location
		// with a
		// temporary form item scope. The file must be moved later using the
		// moveUploadFileTo() method to the final destination.
		File tempUploadFile = new File(WebappHelper.getTmpDir(), CodeHelper.getUniqueID());
		
		// Move file to internal temp location
		boolean success = fileInfos.file().renameTo(tempUploadFile);
		if (!success) {
			// try to move file by copying it, command above might fail
			// when source and target are on different volumes
			FileUtils.copyFileToFile(fileInfos.file(), tempUploadFile, true);
		}

		String uploadFilename = fileInfos.fileName();
		// prevent an issue with different operation systems and .
		uploadFilename = FileUtils.cleanFilename(uploadFilename, maxFilenameLength);
		// use mime-type from file name to have deterministic mime types

		String uploadMimeType = WebappHelper.getMimeType(uploadFilename);
		if (uploadMimeType == null) {
			// use browser mime type as fallback if unknown
			uploadMimeType = fileInfos.contentType();
		}
		if (uploadMimeType == null) {
			// use application fallback for worst case
			uploadMimeType = "application/octet-stream";
		}

		ImageFormItem previewEl = null;
		if (preview && uploadMimeType != null
				&& (uploadMimeType.startsWith("image/") || uploadMimeType.startsWith("video/"))
				&& (!checkForMimeTypes || (isMimeTypeAllowed(uploadMimeType)))) {
			VFSLeaf media = new LocalFileImpl(tempUploadFile);
			
			previewEl = new ImageFormItem(usess, getName() + "_PREVIEW");
			previewEl.setRootForm(getRootForm());
			previewEl.setMedia(media, uploadMimeType);
			previewEl.setCropSelectionEnabled(cropSelectionEnabled);
			previewEl.setMaxWithAndHeightToFitWithin(300, 200);
			previewEl.setVisible(true);
		}
		
		tempUploadFiles.add(new FileElementInfos(tempUploadFile, uploadFilename, tempUploadFile.length(), uploadMimeType, previewEl));
		return tempUploadFile;
	}
	
	private File getFileByFilename(String filename) {
		if(filename == null) return null;
		
		if(initialFile != null && filename.equalsIgnoreCase(initialFile.getName())) {
			return initialFile;
		}
		for(FileElementInfos tempUploadFile:tempUploadFiles) {
			if(filename.equalsIgnoreCase(tempUploadFile.fileName())) {
				return tempUploadFile.file();
			}
		}
		return null;
	}

	@Override
	public Iterable<FormItem> getFormItems() {
		List<FormItem> items = new ArrayList<>(tempUploadFiles.size() + 1);
		if (initialPreviewEl != null) {
			items.add(initialPreviewEl);
		}
		for(FileElementInfos fileInfos:tempUploadFiles) {
			if(fileInfos.previewEl() != null) {
				items.add(fileInfos.previewEl());
			}
		}
		return items;
	}

	@Override
	public FormItem getFormComponent(String name) {
		if (initialPreviewEl != null && initialPreviewEl.getName().equals(name)) {
			return initialPreviewEl;
		}
		for(FileElementInfos fileInfos:tempUploadFiles) {
			if(fileInfos.previewEl() != null && fileInfos.previewEl().getName().equals(name)) {
				return fileInfos.previewEl();
			}
		}
		return null;
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	protected ImageFormItem getInitialPreviewFormItem() {
		return initialPreviewEl;
	}
	
	protected List<ImageFormItem> getPreviewsFormItems() {
		List<ImageFormItem> items = new ArrayList<>(tempUploadFiles.size());
		for(FileElementInfos fileInfos:tempUploadFiles) {
			if(fileInfos.previewEl() != null) {
				items.add(fileInfos.previewEl());
			}
		}
		return items;
	}
	
	@Override
	public void resetTempFile(File file) {
		if(file != null && !tempUploadFiles.isEmpty()) {
			for(Iterator<FileElementInfos> it=tempUploadFiles.iterator(); it.hasNext(); ) {
				FileElementInfos fileInfos = it.next();
				if(file.equals(fileInfos.file())) {
					FileUtils.deleteFile(fileInfos.file());
					component.setDirty(true);
					it.remove();
				}
			}
		}
	}

	@Override
	public void reset() {
		deleteTempUploadFiles();
		if (initialPreviewEl != null) {
			if (initialFile != null) {
				VFSLeaf media = new LocalFileImpl(initialFile);
				initialPreviewEl.setMedia(media);
				initialPreviewEl.setMaxWithAndHeightToFitWithin(300, 200);
				initialPreviewEl.setVisible(true);
			} else {
				initialPreviewEl.setVisible(false);
			}
		}
	}

	@Override
	protected void rootFormAvailable() {
		if (initialPreviewEl != null && initialPreviewEl.getRootForm() != getRootForm()) {
			initialPreviewEl.setRootForm(getRootForm());
		}
		for(FileElementInfos fileInfos:tempUploadFiles) {
			if(fileInfos.previewEl() != null && fileInfos.previewEl().getRootForm() != getRootForm()) {
				fileInfos.previewEl().setRootForm(getRootForm());
			}
		}
	}

	@Override
	public void setMandatory(boolean mandatory, String i18nErrKey) {
		super.setMandatory(mandatory);
		this.i18nErrMandatory = i18nErrKey;
	}

	@Override
	public boolean validate() {
		int lastFormError = getRootForm().getLastRequestError();
		if (lastFormError == Form.REQUEST_ERROR_UPLOAD_LIMIT_EXCEEDED) {
			// check if total upload limit is exceeded (e.g. sum of files)
			setErrorKey(i18nErrMaxSize, i18nErrMaxSizeArgs);
			return false;
		}
		
		// check for a general error
		if (lastFormError == Form.REQUEST_ERROR_GENERAL) {
			setErrorKey("file.element.error.general");
			return false;
			// check if uploaded at all
		}
		
		if (isMandatory() && ((initialFile == null && !isUploadSuccess())
				|| (initialFile != null && !tempUploadFiles.isEmpty() && !isUploadSuccess()))) {
			setErrorKey(i18nErrMandatory);
			return false;
		}
		
		if (multiFileUpload && maxUploadFiles > 0 && tempUploadFiles.size() > maxUploadFiles) {
			setErrorKey(i18nErrMaxFiles, Integer.toString(maxUploadFiles));
			return false;
		}
		
		// check for file size of current file
		if (checkForMaxFileSize && !tempUploadFiles.isEmpty() && getUploadSize() > maxUploadSizeKB * 1024l) {
			setErrorKey(i18nErrMaxSize, i18nErrMaxSizeArgs);
			return false;
		}
		
		// check for mime types
		if (checkForMimeTypes && !tempUploadFiles.isEmpty()) {
			boolean allOk = true;
			for (FileElementInfos tempUploadFile:tempUploadFiles) {
				allOk &= isMimeTypeAllowed(tempUploadFile.contentType());
			}
			
			if (!allOk) {
				setErrorKey(i18nErrMimeType, i18nErrMimeTypeArgs);
				return false;
			}
		}
		// No error, clear errors from previous attempts
		clearError();
		return true;
	}
	
	private boolean isMimeTypeAllowed(String mimeType) {
		boolean found = false;
		
		for (String validType : getMimeTypeLimitations()) {
			if (validType.equals(mimeType)) {
				// exact match: image/jpg
				found = true;
				break;
			} else if (validType.endsWith("/*")) {
				// wildcard match: image/*
				if (mimeType != null
						&& mimeType.startsWith(validType.substring(0, validType.length() - 2))) {
					found = true;
					break;
				}
			}
		}
		
		return found;
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
	public String getDndInformations() {
		return dndInformations;
	}

	@Override
	public void setDndInformations(String dndInformations) {
		this.dndInformations = dndInformations;
	}

	@Override
	public void setPreview(UserSession usess, boolean enable) {
		this.preview = enable;
		if (enable) {
			initialPreviewEl = new ImageFormItem(usess, this.getName() + "_PREVIEW");
			initialPreviewEl.setRootForm(getRootForm());
		} else {
			initialPreviewEl = null;
		}
	}

	@Override
	public boolean isMultiFileUpload() {
		return multiFileUpload;
	}

	@Override
	public void setMultiFileUpload(boolean multiFileUpload) {
		this.multiFileUpload = multiFileUpload;
	}

	@Override
	public boolean isDragAndDropForm() {
		return dragAndDropForm;
	}

	@Override
	public void setDragAndDropForm(boolean enable) {
		dragAndDropForm = enable;
	}

	/**
	 * Render the file element in a preview style where the actual file upload
	 * functionality is disabled but the element looks like an actual upload
	 * element and not like the disabled element. 
	 * 
	 * @return
	 */
	@Override
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
	public void setInitialFile(File initialFile) {
		this.initialFile = initialFile;
		if (initialFile != null && initialPreviewEl != null) {
			VFSLeaf media = new LocalFileImpl(initialFile);
			initialPreviewEl.setMedia(media);
			initialPreviewEl.setMaxWithAndHeightToFitWithin(300, 200);
			initialPreviewEl.setVisible(true);
		} else if (initialPreviewEl != null) {
			initialPreviewEl.setVisible(false);
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
	public void setMaxNumberOfFiles(int maxNumber, String i18nErrKey) {
		maxUploadFiles = maxNumber;
		this.i18nErrMaxFiles = i18nErrKey == null ? "form.error.max.files" : i18nErrKey;
	}

	@Override
	public void setMaxUploadSizeKB(long maxUploadSizeKB, String i18nErrKey, String[] i18nArgs) {
		this.maxUploadSizeKB = maxUploadSizeKB;
		this.checkForMaxFileSize = maxUploadSizeKB != UPLOAD_UNLIMITED;
		this.i18nErrMaxSize = i18nErrKey;
		this.i18nErrMaxSizeArgs = i18nArgs;
	}

	@Override
	public void setMaxFilenameLength(int length) {
		this.maxFilenameLength = length;
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

	public boolean isReplaceButton() {
		return replaceButton;
	}

	@Override
	public void setReplaceButton(final boolean replaceButton) {
		this.replaceButton = replaceButton;
	}

	@Override
	public boolean isShowInputIfFileUploaded() {
		return showInputIfFileUploaded;
	}

	@Override
	public void setShowInputIfFileUploaded(boolean showInputIfFileUploaded) {
		this.showInputIfFileUploaded = showInputIfFileUploaded;
	}

	@Override
	public boolean isUploadSuccess() {
		boolean allOk = true;
		if(tempUploadFiles.isEmpty()) {
			allOk &= false;
		} else {
			for(FileElementInfos tempUploadFile:tempUploadFiles) {
				if (!tempUploadFile.file().exists()) {
					allOk &= false;
				}
			}
		}
		return allOk;
	}

	@Override
	public String getUploadFileName() {
		FileElementInfos tempUploadFile = getFirstFileInfos();
		return tempUploadFile == null ? null : tempUploadFile.fileName();
	}

	@Override
	public void setUploadFileName(String fileName) {
		preferredUploadFilename = fileName;
	}

	@Override
	public String getUploadMimeType() {
		FileElementInfos tempUploadFile = getFirstFileInfos();
		return tempUploadFile == null ? null : tempUploadFile.contentType();
	}

	@Override
	public File getUploadFile() {
		FileElementInfos tempUploadFile = getFirstFileInfos();
		return tempUploadFile == null ? null : tempUploadFile.file();
	}

	@Override
	public InputStream getUploadInputStream() {
		FileElementInfos tempUploadFile = getFirstFileInfos();
		if (tempUploadFile == null) {
			return null;
		}
		
		try {
			return new BufferedInputStream(new FileInputStream(tempUploadFile.file()), FileUtils.BSIZE);
		} catch (FileNotFoundException e) {
			log.error("Could not open stream for file element::{}", getName(), e);
		}
		return null;
	}
	
	@Override
	public List<FileElementInfos> getUploadFilesInfos() {
		return List.copyOf(tempUploadFiles) ;
	}

	@Override
	public long getUploadSize() {	
		long length = 0;
		if (!tempUploadFiles.isEmpty()) {
			for(FileElementInfos tempUploadFile:tempUploadFiles) {
				if (tempUploadFile.file().exists()) {
					length += tempUploadFile.file().length();
				}
			}
		} else if (initialFile != null && initialFile.exists()) {
			length = initialFile.length();
		} 
		return length;
	}
	
	private FileElementInfos getFirstFileInfos() {
		if(tempUploadFiles.isEmpty()) {
			return null;
		}
		return tempUploadFiles.get(0);
	}

	@Override
	public File moveUploadFileTo(File destinationDir) {
		File file = null;
		if (!tempUploadFiles.isEmpty()) {
			destinationDir.mkdirs();
			
			for(FileElementInfos tempUploadFile:tempUploadFiles) {
				if(tempUploadFile.exists()) {
					String fileName = tempUploadFile.fileName();
					if(preferredUploadFilename != null) {
						fileName = preferredUploadFilename;
					}
					
					// Check if such a file does already exist, if yes rename new file
					File existsFile = new File(destinationDir, fileName);
					if (existsFile.exists()) {
						// Use standard rename policy
						File tmpF = new File(destinationDir, fileName);
						fileName = FileUtils.rename(tmpF);
					}
					// Move file now
					File targetFile = new File(destinationDir, fileName);
					if (FileUtils.copyFileToFile(tempUploadFile.file(), targetFile, true)) {
						return targetFile;
					}
				}
			}
		}
		return file;
	}

	@Override
	public VFSLeaf moveUploadFileTo(VFSContainer destinationContainer) {
		return moveUploadFileTo(destinationContainer, false);
	}

	@Override
	public VFSLeaf moveUploadFileTo(VFSContainer destinationContainer, boolean crop) {
		VFSLeaf lastLeaf = null;
		for(FileElementInfos tempUploadFile:tempUploadFiles) {
			lastLeaf = moveUploadFileTo(destinationContainer, tempUploadFile, crop);
		}
		return lastLeaf;
	}
	
	private VFSLeaf moveUploadFileTo(VFSContainer destinationContainer, FileElementInfos tempUploadFile, boolean crop) {
		VFSLeaf targetLeaf = null;
		if (tempUploadFile != null && tempUploadFile.exists()) {
			File uploadFile = tempUploadFile.file();
			String uploadFilename = tempUploadFile.fileName();
			if(preferredUploadFilename != null) {
				uploadFilename = preferredUploadFilename;
			}
			
			// Check if such a file does already exist, if yes rename new file
			VFSItem existsChild = destinationContainer.resolve(uploadFilename);
			if (existsChild != null) {
				// Use standard rename policy
				uploadFilename = VFSManager.rename(destinationContainer, uploadFilename);
			}
			// Create target leaf file now and delete original temp file
			if (destinationContainer instanceof LocalFolderImpl folderContainer) {
				// Optimize for local files (don't copy, move instead)
				File destinationDir = folderContainer.getBasefile();
				File targetFile = new File(destinationDir, uploadFilename);

				ImageFormItem previewEl = tempUploadFile.previewEl();
				Crop cropSelection = previewEl == null ? null : previewEl.getCropSelection();
				if (crop && cropSelection != null) {
					CoreSpringFactory.getImpl(ImageService.class).cropImage(uploadFile, targetFile, cropSelection);
					targetLeaf = (VFSLeaf) destinationContainer.resolve(targetFile.getName());
					CoreSpringFactory.getImpl(VFSRepositoryService.class).itemSaved(targetLeaf, savedBy);
				} else if (FileUtils.copyFileToFile(uploadFile, targetFile, true)) {
					targetLeaf = (VFSLeaf) destinationContainer.resolve(targetFile.getName());
					CoreSpringFactory.getImpl(VFSRepositoryService.class).itemSaved(targetLeaf, savedBy);
				} else {
					log.error("Error after copying content from temp file, cannot copy file::{} _ {}"
							,tempUploadFile, targetFile);
				}

				if (targetLeaf == null) {
					log.error("Error after copying content from temp file, cannot resolve copied file::{} - {}"
							,tempUploadFile, targetFile);
				}
			} else {
				// Copy stream in case the destination is a non-local container
				VFSLeaf leaf = destinationContainer.createChildLeaf(uploadFilename);
				boolean success = false;
				try {
					success = VFSManager.copyContent(uploadFile, leaf, savedBy);
				} catch (Exception e) {
					log.error("Error while copying content from temp file: {}", uploadFile, e);
				}
				if (success) {
					// Delete original temp file after copy to simulate move
					// behavior
					FileUtils.deleteFile(uploadFile);
					targetLeaf = leaf;
				}
			}
		} else {
			log.debug("Error while copying content from temp file, no temp file:: {}", tempUploadFile);
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
		deleteTempUploadFiles();
	}
	
	private void deleteTempUploadFiles() {
		if (!tempUploadFiles.isEmpty()) {
			for(FileElementInfos fileInfos:tempUploadFiles) {
				FileUtils.deleteFile(fileInfos.file());
			}
			tempUploadFiles.clear();
		}
	}
}
