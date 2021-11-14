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
package org.olat.core.gui.control.generic.portal;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.PersonalRSSUtil;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.HtmlHeaderComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.home.HomeMainController;

/**
 * 
 * Initial date: 27.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PortalMainController extends BasicController {
	
	private final VelocityContainer welcome;
	private final Link portalBackButton;
	private final Link portalEditButton;
	private PortalImpl myPortal;
	
	public PortalMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(HomeMainController.class, ureq.getLocale()));
		// start screen
		welcome = createVelocityContainer("welcome");
		portalBackButton = LinkFactory.createButtonSmall("command.portal.back", welcome, this);
		portalBackButton.setIconLeftCSS("o_icon o_icon_edit");
		portalBackButton.setElementCssClass("pull-right");
		portalBackButton.setPrimary(true);
		portalEditButton = LinkFactory.createButtonSmall("command.portal.edit", welcome, this);
		portalEditButton.setIconLeftCSS("o_icon o_icon_edit");
		portalEditButton.setElementCssClass("pull-right");

		// rss link
		String rssLink = PersonalRSSUtil.getPersonalRssLink(ureq);
		welcome.contextPut("rssLink", rssLink);
		StringOutput staticUrl = new StringOutput();
		StaticMediaDispatcher.renderStaticURI(staticUrl, "js/egg.js");
		welcome.put("htmlHeader", new HtmlHeaderComponent("rss", null, "<link rel=\"alternate\" type=\"application/rss+xml\" title=\""
				+ translate("welcome.rss") + "\" href=\"" + rssLink + "\" />\n" + "<script src=\""
				+ staticUrl.toString() + "\"></script>"));

		// add portal
		if (myPortal == null) {
			Roles roles = ureq.getUserSession().getRoles();
			PortalImpl portalTemplate;
			if(roles.isGuestOnly()){
				portalTemplate = ((PortalImpl)CoreSpringFactory.getBean("guestportal"));
				portalEditButton.setEnabled(false);
				portalEditButton.setVisible(false);
			} else if(isConsideredManager(roles) && CoreSpringFactory.containsBean("authorportal")) {
				portalTemplate = ((PortalImpl)CoreSpringFactory.getBean("authorportal"));
			} else {
				portalTemplate = ((PortalImpl)CoreSpringFactory.getBean("homeportal"));
			}
			myPortal = portalTemplate.createInstance(getWindowControl(), ureq);
		}
		
		welcome.put("myPortal", myPortal.getInitialComponent());
		welcome.contextPut("portalEditMode", Boolean.FALSE);

		putInitialPanel(welcome);
	}
	
	private boolean isConsideredManager(Roles roles) {
		return roles.isAdministrator() || roles.isGroupManager()
				|| roles.isUserManager() || roles.isRolesManager()
				|| roles.isLearnResourceManager() ||  roles.isCurriculumManager()
				|| roles.isPoolManager() || roles.isQualityManager()
				|| roles.isLectureManager() || roles.isLineManager()
				|| roles.isPrincipal();
	}
	
	@Override
	protected void doDispose() {
		if (myPortal != null) {
			myPortal.dispose();
			myPortal = null;
		}
        super.doDispose();
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == portalBackButton){
			myPortal.setIsEditMode(ureq, false);
			welcome.contextPut("portalEditMode", Boolean.FALSE);
		} else if (source == portalEditButton){
			myPortal.setIsEditMode(ureq, true);
			welcome.contextPut("portalEditMode", Boolean.TRUE);
		} 
	}
}