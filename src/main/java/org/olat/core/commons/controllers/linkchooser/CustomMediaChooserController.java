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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
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
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.activity.IUserActivityLogger;
import org.olat.core.util.vfs.VFSContainer;

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
	 * Factory method to create a custom media chooser controller from a (Spring)
	 * instance
	 * 
	 * @param ureq
	 * @param wControl
	 * @param rootDir
	 * @param suffixes
	 * @param fileName
	 * @param userActivityLogger
	 * @return true if success, false if no success, e.g. because user has no
	 *         access right to start this controller
	 */
	abstract public CustomMediaChooserController getInstance(UserRequest ureq, WindowControl wControl, VFSContainer rootDir,
			String[] suffixes, String fileName);

	/**
	 * @return Title for media chooser tabbed pane
	 */
	abstract public String getTabbedPaneTitle();

}
