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
package org.olat.core.gui.control.generic.layout;

import org.olat.core.gui.components.htmlheader.jscss.CustomCSS;
import org.olat.core.gui.components.htmlheader.jscss.CustomCSSProvider;
import org.olat.core.gui.control.Controller;

/**
 * Description:<br>
 * A controller implementing this interface is responsible for offering
 * navigation within the main content area (not within header/footer, these are
 * handled by olatbasecontroller). <br>
 * Following:
 * <ul>
 * <li>this controller will never be used as a childcontroller. (a childcontroller
 * is a controller that typically is used either within a part of the screen or
 * within a modal dialog: e.g. a usersearchcontroller)</li>
 * <li>this controller has to implement decent look and feel such as to provide
 * margins for left & right.</li>
 * <li>it is part of the application layout, together with olatbasecontroller.</li>
 * <li>typical implementations are: LayoutMain3ColsController (menu, content,
 * toolbar), and popuplayoutcontroller (providing only a close icon)</li>
 * </ul>
 * <P>
 * Initial Date: 09.10.2007 <br>
 * 
 * @author Felix Jost, http://www.goodsolutions.ch
 * <br>
 * @author Florian Gnägi http://www.frentix.com
 */
public interface MainLayoutController extends Controller, CustomCSSProvider {

	/**
	 * Set the custom CSS for this main layout
	 * 
	 * @param customCSS the custom CSS or NULL if no CSS should be used
	 */
	void setCustomCSS(CustomCSS customCSS);

}
