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

package org.olat.core.gui.components.form.flexible.elements;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

import org.olat.core.gui.components.form.flexible.FormMultipartItem;
import org.olat.core.util.UserSession;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * <h3>Description:</h3>
 * <p>
 * The FileElement represents a file within a flexi form. It offers a read only
 * view of files and an upload view.
 * <p>
 * Initial Date: 08.12.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public interface FileElement extends FormMultipartItem {
	public static final int UPLOAD_ONE_MEGABYTE = 1024 * 1024; // 1 Meg
	public static final int UPLOAD_UNLIMITED = -1; 
	
	/**
	 * Set an initial value for the file element. Optional. Use this to preload
	 * your file element with a previously submitted file.
	 * 
	 * @param initialFile
	 */
	public void setInitialFile(File initialFile);

	/**
	 * Get the initial file value
	 * 
	 * @return the file or NULL if not set
	 */
	public File getInitialFile();

	/**
	 * Set the KB that are allowed in the file upload. In case the user uploads
	 * too much, the error with the given key will be displayed.<br />
	 * Use UPLOAD_UNLIMITED to set no limit.
	 * 
	 * @param maxFileSizeKB
	 *            max file size in KB
	 * @param i18nErrKey
	 *            i18n key used in case user uploaded to big file
	 * @param i18nArgs
	 *            optional arguments for thei18nErrKey
	 */
	public void setMaxUploadSizeKB(long maxFileSizeKB, String i18nErrKey, String[] i18nArgs);

	/**
	 * Set a mime type limitation on which files are allowed in the upload
	 * process. Wildcards like image/* are also allowed.
	 * 
	 * @param mimeTypes
	 * @param i18nErrKey
	 *            i18n key used in case user uploaded wrong files
	 * @param i18nArgs
	 *            optional arguments for thei18nErrKey
	 */
	public void limitToMimeType(Set<String> mimeTypes, String i18nErrKey, String[] i18nArgs);
	
	/**
	 * Preview is possible only for images.
	 * 
	 * @param usess
	 * @param enable
	 */
	public void setPreview(UserSession usess, boolean enable);
	
	public boolean isButtonsEnabled();
	
	/**
	 * Enable or disable whether the upload and delete buttons are active. Disabled
	 * buttons are an alternative to the disabling of the whole element.
	 * 
	 * @param enable
	 */
	public void setButtonsEnabled(boolean enable);
	
	public void setCropSelectionEnabled(boolean enable);

	/**
	 * Get the set of the mime types limitation
	 * 
	 * @return Set containing mime types. Can be empty but is never NULL.
	 */
	public Set<String> getMimeTypeLimitations();

	/**
	 * Set this form element mandatory.
	 * 
	 * @param mandatory
	 *            true: is mandatory; false: is optional
	 * @param i18nErrKey
	 *            i18n key used in case user did not upload something
	 */
	public void setMandatory(boolean mandatory, String i18nErrKey);

	public boolean isDeleteEnabled();
	
	public void setDeleteEnabled(boolean enable);
	
	public void setReplaceButton(boolean replaceButton);
	
	public boolean isArea();
	
	public void setArea(boolean area);
	
	//
	// Methods that are used when a file has been uploaded

	/**
	 * @return true: file has been uploaded; false: file has not been uploaded
	 */
	public boolean isUploadSuccess();

	/**
	 * Get the size of the uploaded file
	 * 
	 * @return
	 */
	public long getUploadSize();

	/**
	 * @return The filename of the uploaded file
	 */
	public String getUploadFileName();

	/**
	 * Set the filename of the uploaded file. Use this if you want the final filename 
	 * to be something different than file name from the upload. Whenever a file is 
	 * uploaded again, this name is replaced again by the browser provided upload file name
	 * 
	 * @param the uploaded file name
	 */
	public void setUploadFileName(String uploadFileName);

	/**
	 * The mime type is first looked up by servletContext.getMimeType(). If no
	 * mime type is available, the browser supplied mime type is used.
	 * 
	 * @return The mime type of the uploaded file.
	 */
	public String getUploadMimeType();
	
	/**
	 * Use the upload file only for temporary checks on the file. Use the
	 * moveUploadFileTo() to move the file to the final destination. The temp
	 * file will be deleted on form disposal.
	 * 
	 * @return A reference to the uploaded file
	 */
	public File getUploadFile();

	/**
	 * Get the input stream of the uploaded file to copy it to some other place
	 * 
	 * @return
	 */
	public InputStream getUploadInputStream();

	/**
	 * Move the uploaded file from the temporary location to the given
	 * destination directory.
	 * <p>
	 * If in the destination a file with the given name does already exist,
	 * rename the file accordingly
	 * <p>
	 * Whenever possible use the second moveUploadFileTo method that takes
	 * a VFSContainer as an argument instead of the file. 
	 * 
	 * @param destinationDir
	 * @return A reference to the moved file or NULL if file could not be moved
	 */
	public File moveUploadFileTo(File destinationDir);
	
	/**
	 * Move the uploaded file from the temporary location to the given
	 * destination VFSContainer.
	 * <p>
	 * If in the destination a leaf with the given name does already exist,
	 * rename the leaf accordingly
	 * <p>
	 * The method optimizes for containers of type LocalFolderImpl in which 
	 * case the file is moved. In other cases the content is copied via the 
	 * file input stream.
	 * 
	 * @param destinationContainer
	 * @return A reference of the new leaf file or NULL if the file could not be created
	 */
	public VFSLeaf moveUploadFileTo(VFSContainer destinationContainer);
	
	/**
	 * Crop the image if there is one and a crop selection.
	 * @param destinationContainer
	 * @param crop
	 * @return
	 */
	public VFSLeaf moveUploadFileTo(VFSContainer destinationContainer, boolean crop);
	
}
