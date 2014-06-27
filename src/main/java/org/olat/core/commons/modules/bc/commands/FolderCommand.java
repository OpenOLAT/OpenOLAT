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

import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;

public interface FolderCommand {

	public static final Event FOLDERCOMMAND_FINISHED = new Event("fc_finished");
	
	/**
	 * Execute the command.
	 * 
	 * May provide a controller if the command needs to
	 * do more complex stuff. In such case, add this controller as listener and
	 * listen for FOLDERCOMMAND_FINISHED events. If no controller is provided, the
	 * command is executed right away.
	 * 
	 * After execution, get any status return codes with getStatus().
	 *
	 * @param folderComponent
	 * @param ureq
	 * @param wControl
	 * @param translator
	 * @return
	 */
	public Controller execute(FolderComponent folderComponent, UserRequest ureq, WindowControl wControl, Translator translator);
	
	public int getStatus();
	
	/**
	 * @return true if this command provides its own modal dialog. false if it
	 *         is a normal controller that can be wrapped in a modal dialog by
	 *         the parent controller
	 */
	public boolean runsModal();
	
	public String getModalTitle();
}