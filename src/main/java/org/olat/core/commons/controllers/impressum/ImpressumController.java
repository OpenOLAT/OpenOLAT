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
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.iframe.IFrameDisplayController;
import org.olat.core.util.i18n.I18nModule;
import org.springframework.beans.factory.annotation.Autowired;

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

	@Autowired
	private ImpressumModule impressumModule;
	@Autowired
	private I18nModule i18nModule;

	private static final String index_html = "index_%s.html";

	/**
	 * @param ureq
	 * @param control
	 */
	public ImpressumController(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		File baseFolder = impressumModule.getImpressumDirectory();
		IFrameDisplayController iframe = new IFrameDisplayController(ureq, getWindowControl(), baseFolder);
		listenTo(iframe);
		
		String langCode = ureq.getLocale().getLanguage();
		String fileName = String.format(index_html, langCode);
		if (new File (baseFolder, fileName).exists()){
			iframe.setCurrentURI(fileName);
		} else {
			langCode = I18nModule.getDefaultLocale().getLanguage();
			fileName = String.format(index_html, langCode);
			if (new File(baseFolder, fileName).exists()) {
				iframe.setCurrentURI(fileName);
			} else {
				for (String lang : i18nModule.getEnabledLanguageKeys()) {
					fileName = String.format(index_html, lang);
					if (new File(baseFolder, fileName).exists()) {
						iframe.setCurrentURI(fileName);
						break;
					}
				}
			}
		}
		
		putInitialPanel(iframe.getInitialComponent());
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// Do nothing.
	}
}
