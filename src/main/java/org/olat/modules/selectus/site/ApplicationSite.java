/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.site;

import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.DefaultNavElement;
import org.olat.core.gui.control.navigation.NavElement;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.StateSite;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.ApplyToApplicationMainController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * The site for applicants
 * <P>
 * Initial Date:  12 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ApplicationSite implements SiteInstance {
	
	private static final OLATResourceable positionsOres = OresHelper.createOLATResourceableInstance("Positions", 0l);
	private static final String positionsPath = OresHelper.toBusinessPath(positionsOres);
	
	private static final Logger log = Tracing.createLoggerFor(ApplicationSite.class);
	
	private DefaultNavElement curNavElem;
	private NavElement origNavElem;

	@Autowired
	private RecruitingService erFrontendManager;
	
	public ApplicationSite(Locale locale) {
		Translator trans = Util.createPackageTranslator(ApplyToApplicationMainController.class, locale);
		origNavElem = new DefaultNavElement(positionsPath, trans.translate("topnav.apply"), trans.translate("topnav.apply.alt"), "o_site_application");
		curNavElem = new DefaultNavElement(origNavElem);
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	public NavElement getNavElement() {
		return curNavElem;
	}

	@Override
	public Controller createController(UserRequest ureq, WindowControl wControl) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Apply", 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, ores, new StateSite(this), wControl, true);

		String uriPrefix = ureq.getUriPrefix();
		if(uriPrefix != null && (uriPrefix.contains("/positiondetails/") || uriPrefix.contains("/position/"))) {
			Position position =  getPosition(ureq);
			
			return new ApplyToApplicationMainController(ureq, bwControl, position);
		}
		List<Position> positions = erFrontendManager.getPublishedPositions();
		return new ApplyToApplicationMainController(ureq, bwControl, positions);
	}

	@Override
	public boolean isKeepState() {
		return false;
	}

	@Override
	public void reset() {
		curNavElem = new DefaultNavElement(origNavElem);
	}
	
	private Position getPosition(UserRequest ureq) {
		String requestUri = ureq.getHttpReq().getRequestURI();
		String uriPrefix = ureq.getUriPrefix();
		if(uriPrefix.length() < requestUri.length()) {
			requestUri = requestUri.substring(uriPrefix.length());
		}
		
		if(requestUri.startsWith("/")) {
			requestUri = requestUri.substring(1, requestUri.length());
		}
			
		int slashFromRestUrlIndex = requestUri.indexOf('/');
		if(slashFromRestUrlIndex > 0 && slashFromRestUrlIndex + 1 < requestUri.length()) {
			requestUri = requestUri.substring(slashFromRestUrlIndex + 1, requestUri.length());
		}
		if(StringHelper.isLong(requestUri)) {
			try {
				Position position = erFrontendManager.getPosition(Long.parseLong(requestUri));
				if(position != null && position.isValid()) {
					return position;
				}
			} catch (NumberFormatException e) {
				log.warn("Cannot parse position key: {}", requestUri, e);
			}
		}
		return null;
	}
}