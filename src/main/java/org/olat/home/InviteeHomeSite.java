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
package org.olat.home;

import java.util.Locale;

import org.olat.core.commons.chiefcontrollers.BaseChiefController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.navigation.AbstractSiteInstance;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.util.Util;

/**
 * 
 * Description:<br>
 * Create the home site for invitee
 * 
 * <P>
 * Initial Date:  7 déc. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InviteeHomeSite extends AbstractSiteInstance {
	private NavElement origNavElem;
	private NavElement curNavElem;

	public InviteeHomeSite(SiteDefinition siteDef, Locale loc) {
		super(siteDef);
		Translator trans = Util.createPackageTranslator(BaseChiefController.class, loc);
		origNavElem = new DefaultNavElement(trans.translate("topnav.guesthome"), trans.translate("topnav.guesthome.alt"), "o_site_home");		
		curNavElem = new DefaultNavElement(origNavElem);
	}

	/**
	 * @see org.olat.navigation.SiteInstance#getNavElement()
	 */
	public NavElement getNavElement() {
		return curNavElem;
	}

	@Override
	protected MainLayoutController createController(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		if (!ureq.getUserSession().getRoles().isInvitee()) {
			throw new OLATSecurityException("Tried to launch a InviteeMainController, but is not an invitee " + ureq.getUserSession().getRoles());
		}
		return new InviteeHomeMainController(ureq, wControl);
	}

	/**
	 * @see org.olat.navigation.SiteInstance#isKeepState()
	 */
	public boolean isKeepState() {
		return true;
	}
	
	public void reset() {
		curNavElem = new DefaultNavElement(origNavElem);
	}
}
