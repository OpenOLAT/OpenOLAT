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
*/

package org.olat.test.site;

import java.util.List;

import org.olat.core.extensions.ExtensionResource;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteDefinition;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.helpers.Settings;

/**
 * 
 * Description:<br>
 * TODO: Lavinia Dumitrescu Class Description for GUIDemoSiteDef
 * 
 * <P>
 * Initial Date:  11.09.2007 <br>
 * @author Lavinia Dumitrescu
 */
public class GUIDemoSiteDef extends AbstractSiteDefinition implements SiteDefinition {


	/**
	 * 
	 */
	public GUIDemoSiteDef() {
		//
	}

	/**
	 * @see org.olat.core.extensions.OLATExtension#getName()
	 */
	public String getName() {
		return "testsite";
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
		if (Settings.isDebuging() && ureq.getUserSession().getRoles().isOLATAdmin()) {
			// only open for olat-admins
			si = new GUIDemoSite(ureq.getLocale());
		} 
		return si;
	}

}

