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
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.test.guidemo;

import java.io.IOException;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.textmarker.GlossaryMarkupItemController;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.modules.glossary.GlossaryManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * 
 * Description: Loads a test textmarker file and applies it to the content of
 * the html file
 * 
 * @author gnaegi Initial Date: Jul 14, 2006
 * 
 */
public class GuiDemoTextMarkerController extends BasicController {

	VelocityContainer vcMain;
	GlossaryMarkupItemController glossCtr;

	public GuiDemoTextMarkerController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		vcMain = createVelocityContainer("guidemo-textmarker");
		Resource resource = new ClassPathResource("/org/olat/test/_static/"+GlossaryManager.INTERNAL_FOLDER_NAME);
		String glossaryId = "guiDemoTestGlossary";
		try {
			glossCtr = new GlossaryMarkupItemController(ureq, getWindowControl(), vcMain, new LocalFolderImpl(resource.getFile()), glossaryId);
		} catch (IOException e) {
			showInfo("GuiDemoTextMarkerController.notWorking");
		}
		glossCtr.setTextMarkingEnabled(false);
		putInitialPanel(glossCtr.getInitialComponent());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
	// nothing to catch
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		if (glossCtr != null) {
			glossCtr.dispose();
			glossCtr = null;
		}
	}

}
