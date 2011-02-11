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
* <p>
*/

package ch.goodsolutions.demoextension;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

import ch.goodsolutions.demoextension.controller.DemoMainLayoutController;
/**
 * Description:<br>
 * TODO: Felix Jost Class Description for HomeSite
 * 
 * <P>
 * Initial Date:  19.07.2005 <br>
 *
 * @author Felix Jost
 */
public class DemoSite implements SiteInstance {
	
	private NavElement origNavElem;
	private NavElement curNavElem;
	
	
	/**
	 * @param loc
	 */
	public DemoSite(Locale loc) {
		Translator trans = Util.createPackageTranslator(this.getClass(), loc);
		origNavElem = new DefaultNavElement(trans.translate("site.title"), trans.translate("site.title.alt"), "site_demo_icon");			
		curNavElem = new DefaultNavElement(origNavElem);
	}

	/**
	 * @see org.olat.navigation.SiteInstance#getNavElement()
	 */
	public NavElement getNavElement() {
		return curNavElem;
	}

	/**
	 * @see org.olat.navigation.SiteInstance#createController(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public MainLayoutController createController(UserRequest userRequest, final WindowControl wControl) {
		return new DemoMainLayoutController(userRequest, wControl);
	}

	/**
	 * @see org.olat.navigation.SiteInstance#isKeepState()
	 */
	public boolean isKeepState() {
		return true;
	}
	
	/**
	 * @see org.olat.navigation.SiteInstance#reset()
	 */
	public void reset() {
		curNavElem = new DefaultNavElement(origNavElem);
	}

}

