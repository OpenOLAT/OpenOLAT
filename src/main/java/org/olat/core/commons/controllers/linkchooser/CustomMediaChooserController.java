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

/**
 * Description:<br>
 * This abstract class describes what a custom media chooser that can be used in
 * the HTML editor must provide
 * <P>
 * Initial Date: Mar 13 2007 <br>
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.WindowControl;

public abstract class CustomMediaChooserController extends DefaultController {

	/**
	 * Pass to parent controller
	 * 
	 * @param wControl
	 */
	protected CustomMediaChooserController(WindowControl wControl) {
		super(wControl);
	}

	/**
	 * @return Title for media chooser tabbed pane
	 */
	 public abstract String getTabbedPaneTitle();
}
