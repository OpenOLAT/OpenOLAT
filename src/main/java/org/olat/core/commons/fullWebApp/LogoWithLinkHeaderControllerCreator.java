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
package org.olat.core.commons.fullWebApp;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;

/**
 * <h3>Description:</h3>
 * This controller creator creates instances of the
 * LogoWithLinkHeaderController.
 * <p>
 * <h3>Spring properties</h3>
 * <ul>
 * <li>linkURI</li>
 * <li>imgURI</li>
 * <li>imgAltText</li>
 * </ul>
 * <p>
 * Initial Date: 10.10.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class LogoWithLinkHeaderControllerCreator implements ControllerCreator {
	private String linkURI, imgURI, imgAltText; // spring injected values

	/**
	 * @see org.olat.core.gui.control.creator.ControllerCreator#createController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	public Controller createController(UserRequest ureq, WindowControl wControl) {
		return new LogoWithLinkHeaderController(ureq, wControl, linkURI,
				imgURI, imgAltText);
	}

	/**
	 * spring setter
	 * 
	 * @param imgAltText
	 */
	public void setImgAltText(String imgAltText) {
		this.imgAltText = imgAltText;
	}

	/**
	 * spring setter
	 * 
	 * @param imgURI
	 */
	public void setImgURI(String imgURI) {
		this.imgURI = imgURI;
	}

	/**
	 * spring setter
	 * 
	 * @param linkURI
	 */
	public void setLinkURI(String linkURI) {
		this.linkURI = linkURI;
	}

}
