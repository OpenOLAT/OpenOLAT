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

package org.olat.admin.site;

import java.util.List;

import org.olat.core.extensions.ExtensionResource;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteDefinition;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteInstance;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for HomeSite
 * <P>
 * Initial Date:  12.07.2005 <br>
 *
 * @author Felix Jost
 */
public class AdminSiteDef extends AbstractSiteDefinition implements SiteDefinition {

	/**
	 * 
	 */
	public AdminSiteDef() {
		super();
	}

	/**
	 * @see org.olat.core.extensions.OLATExtension#getName()
	 */
	public String getName() {
		return "adminsite";
	}

	

	/**
	 * @see org.olat.core.extensions.OLATExtension#getExtensionResources()
	 */
	public List getExtensionResources() {
		// no ressources, part of main css
		return null;
	}

	/**
	 * @see org.olat.core.extensions.OLATExtension#getExtensionCSS()
	 */
	public ExtensionResource getExtensionCSS() {
		// no ressources, part of main css
		return null;
	}

	/**
	 * @see org.olat.navigation.SiteDefinition#createSite(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public SiteInstance createSite(UserRequest ureq, WindowControl wControl) {
		SiteInstance si = null;
		if (ureq.getUserSession().getRoles().isOLATAdmin()) {
			// only open for olat-admins
			si = new AdminSite(ureq.getLocale());
		} 
		return si;
	}

}

