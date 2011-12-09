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
package org.olat.core.util.i18n.devtools;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.util.i18n.I18nModule;

/**
 * Description:<br>
 * TODO: rhaag Class Description for TranslationDevMainController
 * 
 * <P>
 * Initial Date:  23.09.2008 <br>
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class TranslationDevMainController extends MainLayoutBasicController {
	private VelocityContainer vc;
	private TranslationDevManager tDMan;
	
	/**
	 * @param ureq
	 * @param control
	 */
	public TranslationDevMainController(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		vc = createVelocityContainer("translationdev");
		tDMan = TranslationDevManager.getInstance();
		String srcPath = I18nModule.getTransToolApplicationLanguagesSrcDir().getAbsolutePath();
		vc.contextPut("srcPath", srcPath);
		//TODO RH: check for enabled debug-mode in order to prevent caching! 
//		vc.contextPut("cachingDisabled", I18nModule.isCachingEnabled());
		
//		formFactory.addTextElement(name, maxLen, initialValue, i18nLabel, formItemContainer)
		putInitialPanel(vc);
	}

	
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// TODO Auto-generated method stub

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
	// TODO Auto-generated method stub

	}

}
