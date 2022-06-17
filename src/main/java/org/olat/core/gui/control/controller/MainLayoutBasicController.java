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
package org.olat.core.gui.control.controller;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * The man layout basic controller implements the MainLayout interface and
 * offers all convenient methods form the basic controller.
 * <P>
 * Initial Date: 09.10.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public abstract class MainLayoutBasicController extends BasicController implements MainLayoutController {
	private CustomCSS customCSS;
	
	/**
	 * @param ureq
	 * @param wControl
	 */
	public MainLayoutBasicController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
	}

	/**
	 * Constructor with fallback translator
	 * @param ureq
	 * @param wControl
	 * @param fallbackTranslator
	 */
	public MainLayoutBasicController(UserRequest ureq, WindowControl wControl, Translator fallbackTranslator) {
		super(ureq, wControl, fallbackTranslator);
	}

	/**
	 * @see org.olat.core.gui.components.htmlheader.jscss.CustomCSSProvider#getCustomCSS()
	 */
	@Override
	public CustomCSS getCustomCSS() {
		if (isLogDebugEnabled()) {
			if (customCSS == null) logDebug("No custom CSS set for this main layout");
			else logDebug("Custom CSS set for this main layout, pointing to URL::" + customCSS.getCSSURL());	
		}
		return customCSS;
	}

	@Override
	public void setCustomCSS(CustomCSS newCustomCSS) {
		if (isLogDebugEnabled()) {
			if (newCustomCSS == null) logDebug("Setting empty custom CSS for this main layout");
			else logDebug("Setting custom CSS for this main layout, pointing to URL::" + newCustomCSS.getCSSURL());	
		}
		// cleanup if one already exists
		if (customCSS != null && customCSS != newCustomCSS) {
			customCSS.dispose();
		}
		this.customCSS = newCustomCSS;		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#dispose()
	 */
	@Override
	public synchronized void dispose() {
		// first execute dispose from basic controller
		super.dispose();
		// now dispose the custom css
		if (customCSS != null) {
			customCSS.dispose();
			customCSS = null;
		}
	}
	
}
