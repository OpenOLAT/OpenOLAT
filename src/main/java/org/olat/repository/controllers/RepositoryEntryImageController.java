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
*/

package org.olat.repository.controllers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.olat.core.commons.modules.bc.FileUploadController;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.FileUtils;
import org.olat.core.util.ImageHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * <h3>Description:</h3>
 * <p>
 * The repository entry image upload controller offers a workflow to upload an
 * image for a learning resource
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>CANCELLED_EVENT</li>
 * <li>DONE_EVENT</li>
 * </ul>
 * 
 * @author Ingmar Kroll
 */
public class RepositoryEntryImageController extends BasicController {
	private VelocityContainer vContainer;
	private Link deleteButton; 
	private FileUploadController uploadCtr;
	private RepositoryEntry repositoryEntry;

	private File repositoryEntryImageFile = null;
	private File newFile = null;
	private final int PICTUREWIDTH = 570;

	
	/**
	 * Display upload form to upload a file to the given currentPath.
	 * @param uploadDir  
	 * @param wControl 
	 * @param translator
	 * @param limitKB
	 */
	public RepositoryEntryImageController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry, Translator translator,
			int limitKB) {
		super(ureq, wControl, translator);
		// use velocity files and translations from folder module package
		setBasePackage(RepositoryManager.class);
		
		this.repositoryEntryImageFile = new File(new File(FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome()),
				getImageFilename(repositoryEntry));
		this.repositoryEntry = repositoryEntry;
		this.vContainer = createVelocityContainer("imageupload");
		// Init upload controller
		Set<String> mimeTypes = new HashSet<String>();
		mimeTypes.add("image/gif");
		mimeTypes.add("image/jpg");
		mimeTypes.add("image/jpeg");
		mimeTypes.add("image/png");
		File uploadDir = new File(FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome());
		VFSContainer uploadContainer = new LocalFolderImpl(uploadDir);
		uploadCtr = new FileUploadController(getWindowControl(), uploadContainer, ureq, limitKB, Quota.UNLIMITED, mimeTypes, false, false, false, true);
		uploadCtr.hideTitleAndFieldset();
		listenTo(uploadCtr);
		vContainer.put("uploadCtr", uploadCtr.getInitialComponent());
		// init the delete button
		deleteButton = LinkFactory.createButtonSmall("cmd.delete", this.vContainer, this);
		// init the image itself
		vContainer.contextPut("hasPortrait", Boolean.FALSE);
		displayImage();
		// finished
		putInitialPanel(vContainer);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == deleteButton){
			repositoryEntryImageFile.delete();
		}
		displayImage();
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == uploadCtr) {
			// catch upload event
			if (event instanceof FolderEvent && event.getCommand().equals(FolderEvent.UPLOAD_EVENT)) {
				FolderEvent folderEvent = (FolderEvent) event;
				// Get file from temp folder location
				String uploadFileName = folderEvent.getFilename();
				File uploadDir = new File(FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome());
				newFile = new File(uploadDir, uploadFileName);
				if (!newFile.exists()) {
					showError("Failed");
				} else {
					// Scale uploaded image
					File pBigFile = new File(uploadDir, getImageFilename(repositoryEntry));
					boolean ok = ImageHelper.scaleImage(newFile, pBigFile, PICTUREWIDTH,PICTUREWIDTH);
					// Cleanup original file
					newFile.delete();
					// And finish workflow
					if (ok) {			
						fireEvent(ureq, Event.DONE_EVENT);
					} else {
						showError("NoImage");
					}					
				}
			}
			// redraw image
			displayImage();
		}
	}

	
	
	/**
	 * Internal helper to create the image component and push it to the view
	 */
	private void displayImage() {
		ImageComponent ic = getImageComponentForRepositoryEntry("image",this.repositoryEntry);
		if (ic != null) {
			// display only within 400x200 in form
			ic.setMaxWithAndHeightToFitWithin(400, 200);
			vContainer.put("image", ic);
			vContainer.contextPut("hasImage",Boolean.TRUE);
		}else{
			vContainer.contextPut("hasImage",Boolean.FALSE);
		}
	}

	/**
	 * Internal helper to create the image name
	 * @param re
	 * @return
	 */
	public static String getImageFilename(RepositoryEntry re) {
		return re.getResourceableId() + ".jpg";
	}

	/**
	 * Check if the repo entry does have an images and if yes create an image
	 * component that displays the image of this repo entry.
	 * 
	 * @param componentName
	 * @param repositoryEntry
	 * @return The image component or NULL if the repo entry does not have an
	 *         image
	 */
	public static ImageComponent getImageComponentForRepositoryEntry(String componentName, RepositoryEntry repositoryEntry){
		File repositoryEntryImageFile= new File(new File(FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome()),
				getImageFilename(repositoryEntry));
		if (!repositoryEntryImageFile.exists()) {
			return null;
		}
		ImageComponent imageComponent = new ImageComponent(componentName);
		imageComponent.setMediaResource(new FileMediaResource(repositoryEntryImageFile));
		return imageComponent;
	}

	/**
	 * Copy the repo entry image from the source to the target repository entry.
	 * If the source repo entry does not exists, nothing will happen
	 * 
	 * @param src
	 * @param target
	 * @return
	 */
	public static boolean copyImage(RepositoryEntry src, RepositoryEntry target) {
		File srcFile = new File(new File(FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome()),
			getImageFilename(src));
		File targetFile = new File(new File(FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome()),
			getImageFilename(target));
		if (srcFile.exists()) {
			try {
				FileUtils.bcopy(srcFile, targetFile, "copyRepoImageFile");
			} catch (IOException ioe) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// controllers autodisposed by basic controller
	}

	/**
	 * FIXME: this code belongs to a manager and not to a controller!
	 * Method to remove the image for the given repo entry from disk
	 * @param re
	 */
	public static void deleteImage(RepositoryEntry re) {
		File srcFile = new File(new File(FolderConfig.getCanonicalRoot() + FolderConfig.getRepositoryHome()),
			getImageFilename(re));
		if (srcFile.exists()) srcFile.delete();
	}

}
