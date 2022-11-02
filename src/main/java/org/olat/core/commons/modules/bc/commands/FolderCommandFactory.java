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

package org.olat.core.commons.modules.bc.commands;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.AutoCreator;

public class FolderCommandFactory {

	public static final String COMMAND_BROWSE = "browse";

	public static final String COMMAND_UPLOAD = "ul";
	public static final String COMMAND_CREATEFOLDER = "cf";
	public static final String COMMAND_CREATEFILE = "cfile";
	public static final String COMMAND_COPYFILE = "copyfile";
	public static final String COMMAND_SERV = "serv";
	public static final String COMMAND_EDIT = "edt";
	public static final String COMMAND_EDIT_CONTENT = "editContent";
	public static final String COMMAND_EDIT_QUOTA = "editQuota";
	public static final String COMMAND_RESORT = "resort";
	public static final String COMMAND_VIEW_VERSION = "viewVersion";
	public static final String COMMAND_ADD_EPORTFOLIO = "addToEPortfolio";
	public static final String COMMAND_DELETED_FILES = "dfiles";
	public static final String COMMAND_SERV_THUMBNAIL = "tfiles";
	
	
	public static final String COMMAND_MOVE = "move";
	public static final String COMMAND_COPY = "copy";
	public static final String COMMAND_DEL = "del";
	public static final String COMMAND_MAIL = "mail";
	public static final String COMMAND_DOWNLOAD_ZIP = "dzip";
	public static final String COMMAND_ZIP = "zip";
	public static final String COMMAND_UNZIP = "unzip";
	public static final String COMMAND_VIEW_AUDIO_VIDEO = "viewAudioVideo";

	public static final FolderCommandFactory INSTANCE = new FolderCommandFactory();
	
	private FolderCommandFactory() {
		// singleton
	}
	
	public static final FolderCommandFactory getInstance() {
		return INSTANCE;
	}
	
	public FolderCommand getCommand(String command, UserRequest ureq, WindowControl wControl) {
		if (command == null) return null;
		FolderCommand cmd = null;
		if (command.equals(COMMAND_CREATEFOLDER)) cmd = new CmdCreateFolder(ureq,wControl);
		else if (command.equals(COMMAND_CREATEFILE)) cmd = new CmdCreateFile(ureq,wControl);
		else if (command.equals(COMMAND_COPYFILE)) cmd = new CmdCopyFile(ureq,wControl);
		else if (command.equals(COMMAND_UPLOAD)) cmd = new CmdUpload(ureq, wControl, true);
		else if (command.equals(COMMAND_SERV)) cmd = new CmdServeResource();
		else if (command.equals(COMMAND_SERV_THUMBNAIL)) cmd = new CmdServeThumbnailResource();
		else if (command.equals(COMMAND_EDIT)) cmd = new CmdEditMeta(ureq, wControl);
		else if (command.equals(COMMAND_EDIT_CONTENT)) cmd = new CmdOpenContent(ureq, wControl);
		else if (command.equals(COMMAND_EDIT_QUOTA)) cmd = new CmdEditQuota(ureq, wControl);
		else if (command.equals(COMMAND_DEL)) cmd = new CmdDelete(ureq, wControl);
		else if (command.equals(COMMAND_MAIL)) {
			AutoCreator controllerCreator = (AutoCreator)CoreSpringFactory.getBean("sendDocumentByEMailControllerCreator");
			cmd = (CmdSendMail)controllerCreator.createController(ureq, wControl);
		}
		else if (command.equals(COMMAND_MOVE)) cmd = new CmdMoveCopy(wControl, true);
		else if (command.equals(COMMAND_COPY)) cmd = new CmdMoveCopy(wControl, false);
		else if (command.equals(COMMAND_ZIP)) cmd = new CmdZip(ureq,wControl);
		else if (command.equals(COMMAND_DOWNLOAD_ZIP)) cmd = new CmdDownloadZip();
		else if (command.equals(COMMAND_UNZIP)) cmd = new CmdUnzip(ureq,wControl);
		else if (command.equals(COMMAND_VIEW_VERSION)) cmd = new CmdViewRevisions(ureq,wControl);
		else if (command.equals(COMMAND_ADD_EPORTFOLIO)) {
			AutoCreator controllerCreator = (AutoCreator)CoreSpringFactory.getBean("folderCMDAddToEPortfolio");
			cmd = (CmdAddToEPortfolio)controllerCreator.createController(ureq, wControl);
		}
		else if (command.equals(COMMAND_DELETED_FILES)) cmd = new CmdDeletedFiles(ureq,wControl);
		else if (command.equals(COMMAND_VIEW_AUDIO_VIDEO)) cmd = new CmdViewAudioVideo(ureq, wControl);
		return cmd;
	}
}
