/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.externalsite;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteInstance;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteSecurityCallback;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.externalsite.ui.ExternalSiteIFrameTunnelController;

/**
 * Initial date: Nov 10, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ExternalSite extends AbstractSiteInstance {

	private static final OLATResourceable externalSiteOres = OresHelper.createOLATResourceableInstance(ExternalSite.class, 0l);
	private static final String externalSiteBusinessPath = OresHelper.toBusinessPath(externalSiteOres);
	private final String height;

	private final NavElement origNavElem;
	private NavElement curNavElem;

	private final SiteSecurityCallback siteSecCallback;

	public ExternalSite(SiteDefinition siteDef, SiteSecurityCallback siteSecCallback,
						String titleKeyPrefix, String externalUrl, boolean isExternalUrlInIFrame, String height, String navIconCssClass) {
		super(siteDef);
		this.height = height;
		origNavElem = new DefaultNavElement(externalSiteBusinessPath, titleKeyPrefix, titleKeyPrefix, navIconCssClass);
		origNavElem.setExternalUrl(externalUrl);
		origNavElem.setExternalUrlInIFrame(isExternalUrlInIFrame);
		curNavElem = new DefaultNavElement(origNavElem);
		curNavElem.setExternalUrl(externalUrl);
		curNavElem.setExternalUrlInIFrame(isExternalUrlInIFrame);
		this.siteSecCallback = siteSecCallback;
	}

	@Override
	protected Controller createController(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		boolean hasAccess = false;

		if (siteSecCallback != null) {
			hasAccess = siteSecCallback.isAllowedToLaunchSite(ureq);
		}

		if (hasAccess) {
			return new ExternalSiteIFrameTunnelController(ureq, wControl, origNavElem, height);
		} else {
			return getAlternativeController(ureq, wControl, config);
		}
	}

	@Override
	public NavElement getNavElement() {
		return curNavElem;
	}

	@Override
	public void reset() {
		curNavElem = new DefaultNavElement(origNavElem);
	}
}
