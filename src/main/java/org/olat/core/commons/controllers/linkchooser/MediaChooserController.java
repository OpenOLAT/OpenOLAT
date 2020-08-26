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

package org.olat.core.commons.controllers.linkchooser;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.vfs.VFSContainer;

/**
 * 
 * <h3>Description:</h3>
 * This is a link chooser which can be embedded in a standard olat popup window
 * <p>
 * <h4>Events fired by this Controller</h4>
 * <ul>
 * <li>URLChooseEvent</li>
 * </ul>
 * <p>
 * Initial Date:  15 déc. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class MediaChooserController extends LinkChooserController {


	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param rootDir Root directory for file-chooser.
	 * @param uploadRelPath The relative path within the rootDir where uploaded
	 *          files should be put into. If NULL, the root Dir is used
	 * @param suffixes Supported file suffixes for file-chooser.
	 * @param fileName Base file-path for file-chooser.
	 * @param userActivityLogger
	 * @param internalLinkTreeModel Model with internal links e.g. course-node
	 *          tree model. The internal-link chooser tab won't be shown when the
	 *          internalLinkTreeModel is null.
	 */
	public MediaChooserController(UserRequest ureq, WindowControl wControl, VFSContainer rootDir, String uploadRelPath, String[] suffixes, String fileName,
			CustomLinkTreeModel customLinkTreeModel, boolean allowCustomMediaFactory) {
		super(ureq, wControl, rootDir, uploadRelPath, null, suffixes, true, false, fileName, customLinkTreeModel, null, allowCustomMediaFactory);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {		
		fireEvent(ureq, event);
	}
}