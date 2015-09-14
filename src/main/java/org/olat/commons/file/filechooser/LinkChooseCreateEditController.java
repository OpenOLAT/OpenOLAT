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

package org.olat.commons.file.filechooser;

import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.vfs.VFSContainer;

/**
 * Controller to create link chooser (select link to file or select internal link e.g. link to course node).
 * 
 * @author Christian Guretzki
 */
public class LinkChooseCreateEditController extends FileChooseCreateEditController{
		
	private CustomLinkTreeModel customLinkTreeModel;


	/**
	 * 
	 */
	public LinkChooseCreateEditController(UserRequest ureq, WindowControl wControl, String chosenFile, Boolean allowRelativeLinks, VFSContainer rootContainer, String target, String fieldSetLegend, CustomLinkTreeModel customLinkTreeModel) {
		super(ureq, wControl, chosenFile, allowRelativeLinks, rootContainer, target, fieldSetLegend);
		this.customLinkTreeModel = customLinkTreeModel;
	}

	/**
	 * Creates a Controller with internal-link support. 
	 * @see org.olat.commons.file.filechooser.FileChooseCreateEditController#createWysiwygController(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl, org.olat.core.util.vfs.VFSContainer, java.lang.String)
	 */
	protected Controller createWysiwygController(UserRequest ureq, WindowControl windowControl, VFSContainer rootContainer, String chosenFile) {
	  return WysiwygFactory.createWysiwygControllerWithInternalLink(ureq, windowControl, rootContainer, chosenFile, true, customLinkTreeModel);
	}
}



	
