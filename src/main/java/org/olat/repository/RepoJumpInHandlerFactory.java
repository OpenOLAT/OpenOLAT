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

package org.olat.repository;

import org.olat.core.dispatcher.jumpin.JumpInHandlerFactory;
import org.olat.core.dispatcher.jumpin.JumpInReceptionist;
import org.olat.core.dispatcher.jumpin.JumpInResult;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;

/**
 * Description: <br>
 * 
 * Initial Date: 23.02.2005 <br>
 * @author Felix Jost
 * @deprecated Use business path instead
 */
public class RepoJumpInHandlerFactory implements JumpInHandlerFactory {
	private static final String CONST_RID = "rid";
	static final String CONST_PAR = "par";

	/**
	 * 
	 */
	public RepoJumpInHandlerFactory() {
	// nothing to do
	}

	/**
	 * @see org.olat.core.dispatcher.jumpin.JumpInHandlerFactory#createJumpInHandler(org.olat.core.gui.UserRequest)
	 */
	public JumpInReceptionist createJumpInHandler(UserRequest ureq) {
		RepositoryManager rm = RepositoryManager.getInstance();
		//e.g. http://localhost/olat/auth/repo/go?rid=123
		String constRID = ureq.getParameter(CONST_RID);
		if (constRID == null) return null;
		Long repoKey = null;
		try {
			repoKey = new Long(constRID.trim());
		} catch (NumberFormatException nfe) {
			return null;
		}
		RepositoryEntry re = rm.lookupRepositoryEntry(repoKey);
		if (re == null) { // no repoentry with this key found
			return null;
		}
		String displayName = re.getDisplayname();
		OLATResourceable ores = re.getOlatResource();
		return new RepoJumpInReceptionist(displayName, ores, re);

	}


	
	
}

class RepoJumpInReceptionist implements JumpInReceptionist {
	/**
	 * 
	 */
	private final String title;
	private final OLATResourceable ores;
	private final RepositoryEntry re;

	/**
	 * @param title
	 * @param ores
	 * @param re
	 */
	public RepoJumpInReceptionist(String title, OLATResourceable ores, RepositoryEntry re) {
		this.title = title;
		this.ores = ores;
		this.re = re;
		
	}
	/**
	 * @see org.olat.core.dispatcher.jumpin.JumpInReceptionist#getTitle()
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @see org.olat.core.dispatcher.jumpin.JumpInReceptionist#getOLATResourceable()
	 */
	public OLATResourceable getOLATResourceable() {
		return ores;
	}

	/**
	 * @see org.olat.core.dispatcher.jumpin.JumpInReceptionist#createController(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public JumpInResult createJumpInResult(UserRequest ureq, WindowControl wControl) {
		String initialViewId = ureq.getParameter(RepoJumpInHandlerFactory.CONST_PAR);
		Controller resC = RepositoyUIFactory.createLaunchController(re, initialViewId, ureq, wControl);
		return new JumpInResult(resC, initialViewId);
	}
	
	/**
	 * @see org.olat.core.dispatcher.jumpin.JumpInReceptionist#extractActiveViewId(org.olat.core.gui.UserRequest)
	 */
	public String extractActiveViewId(UserRequest ureq) {
		String initialViewId = ureq.getParameter(RepoJumpInHandlerFactory.CONST_PAR);
		return initialViewId;
	}
	
}