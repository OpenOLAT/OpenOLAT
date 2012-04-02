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
package org.olat.user;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.olat.core.commons.modules.bc.FileUploadController;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;

/**
 * Description:
 * <p>
 * This controller shows the users uploaded portrait and offers a way to upload
 * a new portrait.
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>Event.DONE</li>
 * <li>PORTRAIT_DELETED_EVENT</li>
 * </ul>
 * 
 * @author Alexander Schneider
 */
public class PortraitUploadController extends BasicController {
	public static final Event PORTRAIT_DELETED_EVENT = new Event("portraitdeleted");

	private VelocityContainer folderContainer;
	private Link deleteButton;
	private DisplayPortraitController dpc;
	private FileUploadController uploadCtr;
	
	private Identity portraitIdent;
	private File uploadDir;
	private File newFile = null;
	private int limitKB; // max UL limit
	
	private final DisplayPortraitManager dps;
		
	/**
	 * Display upload form to upload a file to the given currentPath.
	 * @param uploadDir  
	 * @param wControl 
	 * @param translator
	 * @param limitKB
	 */
	public PortraitUploadController(UserRequest ureq, WindowControl wControl, Identity portraitIdent, long limitKB) {
		super(ureq, wControl);
		dps = DisplayPortraitManager.getInstance();
		this.portraitIdent = portraitIdent;
		this.uploadDir = dps.getPortraitDir(portraitIdent);
		this.limitKB = (int) limitKB;
		
		folderContainer = createVelocityContainer("portraitupload");
		deleteButton = LinkFactory.createButtonSmall("command.delete", this.folderContainer, this);
		
		MediaResource mr = dps.getSmallPortraitResource(portraitIdent);
		if (mr != null) folderContainer.contextPut("hasPortrait", Boolean.TRUE);
		else folderContainer.contextPut("hasPortrait", Boolean.FALSE);
		
		displayPortrait(ureq, portraitIdent, true);
		
		// Init upload controller
		Set<String> mimeTypes = new HashSet<String>();
		mimeTypes.add("image/gif");
		mimeTypes.add("image/jpg");
		mimeTypes.add("image/jpeg");
		mimeTypes.add("image/png");
		VFSContainer uploadContainer = new LocalFolderImpl(uploadDir);
		uploadCtr = new FileUploadController(getWindowControl(), uploadContainer, ureq, this.limitKB, this.limitKB, mimeTypes, false, false, false, true);
		uploadCtr.hideTitleAndFieldset();
		listenTo(uploadCtr);
		folderContainer.put("uploadCtr", uploadCtr.getInitialComponent());
		putInitialPanel(folderContainer);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == deleteButton){
    	FileUtils.deleteDirsAndFiles(uploadDir, false, false);
    	folderContainer.contextPut("hasPortrait", Boolean.FALSE);
    	uploadCtr.reset();
    	fireEvent(ureq, PORTRAIT_DELETED_EVENT);
		}
		displayPortrait(ureq, portraitIdent, true);		
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
				newFile = new File(uploadDir, uploadFileName);
				if (!newFile.exists()) {
					showError("Failed");
				} else {
					dps.setPortrait(newFile, portraitIdent);
					// Cleanup original file
					newFile.delete();
					// And finish workflow
					fireEvent(ureq, Event.DONE_EVENT);
					folderContainer.contextPut("hasPortrait", Boolean.TRUE);					
				}
			}
			// redraw image
			displayPortrait(ureq, portraitIdent, true);
		}
	}
	
	
	private void displayPortrait(UserRequest ureq, Identity portraitIdent, boolean useLarge){	
			if (dpc != null) removeAsListenerAndDispose(dpc);
			dpc = new DisplayPortraitController(ureq, getWindowControl(), portraitIdent, useLarge, false);
			listenTo(dpc);
			folderContainer.put("portrait", dpc.getInitialComponent());
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// controllers autodisposed by basic cotntroller
	}

}
