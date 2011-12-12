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
package org.olat.core.commons.controllers.impressum;

import java.io.File;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.iframe.IFrameDisplayController;
import org.olat.core.util.WebappHelper;

/**
 * <h3>Description:</h3> This controller displays an impressum which it reads
 * from an external HTML file in the <code>olatdata</code> directory.
 * 
 * 
 * Initial Date: Aug 10, 2009 <br>
 * 
 * @author twuersch, frentix GmbH, http://www.frentix.com
 */
public class ImpressumController extends BasicController {

	public static final String IMPRESSUM_HTML_FOLDER = "/customizing/impressum/";
	private VelocityContainer content;
	private IFrameDisplayController impressumIframe;

	/**
	 * @param ureq
	 * @param control
	 */
	public ImpressumController(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		this.content = createVelocityContainer("impressum");
		File baseFolder = new File(WebappHelper.getUserDataRoot(), IMPRESSUM_HTML_FOLDER);		
		this.impressumIframe = new IFrameDisplayController(ureq, getWindowControl(), baseFolder);
		String langCode = ureq.getLocale().getLanguage();
		String fileName = "index_" + langCode + ".html";
		File termsFileInLang = new File (baseFolder, fileName);
		if (termsFileInLang.exists()){
			this.impressumIframe.setCurrentURI(fileName);
		} else {
			//default is german
			this.impressumIframe.setCurrentURI("index_de.html");
		}
		this.content.put("impressumIFrame", this.impressumIframe.getInitialComponent());
		putInitialPanel(content);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		this.impressumIframe.dispose();
		this.impressumIframe = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.olat.core.gui.control.DefaultController#event(org.olat.core.gui.
	 * UserRequest, org.olat.core.gui.components.Component,
	 * org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// Do nothing.
	}
}
