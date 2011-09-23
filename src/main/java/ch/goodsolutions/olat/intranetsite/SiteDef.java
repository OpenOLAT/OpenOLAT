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

package ch.goodsolutions.olat.intranetsite;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteDefinition;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteInstance;

/**
 * 
 * Description:<br>
 * TODO: Felix Jost Class Description for HomeSite
 * 
 * <P>
 * Initial Date:  12.07.2005 <br>
 *
 * @author Felix Jost
 */
public class SiteDef extends AbstractSiteDefinition implements SiteDefinition {
	private String repositorySoftKey;
	
	/**
	 * 
	 */
	public SiteDef() {
		//
	}

	/**
	 * @see org.olat.navigation.SiteDefinition#createSite(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public SiteInstance createSite(UserRequest ureq, WindowControl wControl) {
		// create the site for all users except for guests
		if (ureq.getUserSession().getRoles().isGuestOnly()) return null;
		return new Site(ureq.getLocale(), repositorySoftKey);
	}

	/**
	 * for SPRING
	 * @param repositorySoftKey The repositorySoftKey to set.
	 */
	public void setRepositorySoftKey(String repositorySoftKey) {
		this.repositorySoftKey = repositorySoftKey;
	}

}

