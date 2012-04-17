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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
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
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
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
	private final FileUploadController uploadCtr;
	private final RepositoryEntry repositoryEntry;

	//private File newFile = null;
	
	private final RepositoryManager repositoryManager;

	
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
		
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);

		this.repositoryEntry = repositoryEntry;
		this.vContainer = createVelocityContainer("imageupload");
		// Init upload controller
		Set<String> mimeTypes = new HashSet<String>();
		mimeTypes.add("image/gif");
		mimeTypes.add("image/jpg");
		mimeTypes.add("image/jpeg");
		mimeTypes.add("image/png");
		File uploadDir = new File(FolderConfig.getCanonicalTmpDir());
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
			repositoryManager.deleteImage(repositoryEntry);
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
				VFSContainer tmpHome = new LocalFolderImpl(new File(FolderConfig.getCanonicalTmpDir()));
				VFSItem newFile = tmpHome.resolve(folderEvent.getFilename());
				if (newFile instanceof VFSLeaf) {
					boolean ok = repositoryManager.setImage((VFSLeaf)newFile, repositoryEntry);
					// Cleanup original file
					newFile.delete();
					// And finish workflow
					if (ok) {			
						fireEvent(ureq, Event.DONE_EVENT);
					} else {
						showError("NoImage");
					}
				} else {
					showError("Failed");
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
		ImageComponent ic = getImageComponentForRepositoryEntry("image", repositoryEntry);
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
	 * Check if the repo entry does have an images and if yes create an image
	 * component that displays the image of this repo entry.
	 * 
	 * @param componentName
	 * @param repositoryEntry
	 * @return The image component or NULL if the repo entry does not have an
	 *         image
	 */
	private ImageComponent getImageComponentForRepositoryEntry(String componentName, RepositoryEntry repositoryEntry) {
		VFSLeaf img = repositoryManager.getImage(repositoryEntry);
		if (img == null) {
			return null;
		}
		ImageComponent imageComponent = new ImageComponent(componentName);
		imageComponent.setMediaResource(new VFSMediaResource(img));
		return imageComponent;
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// controllers autodisposed by basic controller
	}
}
