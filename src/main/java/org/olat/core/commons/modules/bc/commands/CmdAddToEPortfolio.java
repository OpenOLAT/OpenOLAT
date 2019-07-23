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
package org.olat.core.commons.modules.bc.commands;

import org.olat.core.commons.modules.bc.components.FolderComponent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;

/**
 * Initial Date:  03.09.2010 <br>
 * @author rhaag
 */
public interface CmdAddToEPortfolio extends FolderCommand {

	/**
	 * @see org.olat.core.commons.modules.bc.commands.FolderCommand#execute(org.olat.core.commons.modules.bc.components.FolderComponent, org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl, org.olat.core.gui.translator.Translator)
	 */
	public Controller execute(FolderComponent folderComponent,
			UserRequest ureq, WindowControl wControl, Translator translator);

	/**
	 * @see org.olat.core.commons.modules.bc.commands.FolderCommand#getStatus()
	 */
	public int getStatus();

	/**
	 * @see org.olat.core.commons.modules.bc.commands.FolderCommand#runsModal()
	 */
	public boolean runsModal();

}
