/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.gui.demo.guidemo;

import java.io.File;

import org.olat.core.commons.controllers.filechooser.FileChoosenEvent;
import org.olat.core.commons.controllers.filechooser.FileChooserController;
import org.olat.core.commons.controllers.filechooser.FileChooserUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.dev.controller.SourceViewController;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;

/**
 * Description:<br>
 * Demo of the filechooser. For this demo we can browse through the olat app
 * code
 * 
 * <P>
 * Initial Date: 12.06.2008 <br>
 * 
 * @author gnaegi
 */
public class GuiDemoFileChooserController extends BasicController {
	private final FileChooserController chooserCtr;
	private final File webappRootFile;
	
	/**
	 * Constructor for the file chooser gui demo
	 * @param ureq
	 * @param wControl
	 */
	public GuiDemoFileChooserController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		// Main view is a velocity container
		VelocityContainer contentVC = createVelocityContainer("guidemo-filechooser");

		// Create File chooser for the web app root directory
		webappRootFile = new File(WebappHelper.getContextRealPath("/static"));
		VFSContainer webappRoot = new LocalFolderImpl(webappRootFile);	
		// don't show cvs and hidden files meta files
		VFSItemFilter filter = new VFSSystemItemFilter();
		// create file chooser with the filter and the web app root
		chooserCtr = FileChooserUIFactory.createFileChooserController(ureq, getWindowControl(), webappRoot, filter, false);
		listenTo(chooserCtr);		
		contentVC.put("chooserCtr", chooserCtr.getInitialComponent());
		
		//add source view control
		Controller sourceview = new SourceViewController(ureq, wControl, this.getClass(), contentVC);
		contentVC.put("sourceview", sourceview.getInitialComponent());

		putInitialPanel(contentVC);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == chooserCtr) {
			// catch the events from the file chooser controller here
			if (event instanceof FileChoosenEvent) {
				// User pressed select button, get selected item from controller
				VFSItem selectedItem = FileChooserUIFactory.getSelectedItem((FileChoosenEvent)event);
				// for this demo just get file path and display an info message
				LocalImpl localFile = (LocalImpl) selectedItem;
				String absPath = localFile.getBasefile().getAbsolutePath();
				String relPath = absPath.substring(webappRootFile.getAbsolutePath().length());
				getWindowControl().setInfo("user selected /static" +  relPath);
				
			}	else if (event == Event.CANCELLED_EVENT) {
				// user pressed cancel
				getWindowControl().setInfo("user cancelled");

			}	else if (event == Event.FAILED_EVENT) {
				// selection failed for unknown reason
				getWindowControl().setError("Ups, an error occured");
			}
		}

	}
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// no events to catch
	}
}