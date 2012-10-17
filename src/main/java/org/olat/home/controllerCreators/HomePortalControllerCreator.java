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
package org.olat.home.controllerCreators;

import org.olat.commons.rss.RSSUtil;
import org.olat.core.CoreSpringFactory;
import org.olat.core.defaults.dispatcher.StaticMediaDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.HtmlHeaderComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.gui.control.generic.portal.PortalImpl;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.util.Util;
import org.olat.home.InviteeHomeMainController;


/**
 * 
 * <h3>Description:</h3>
 * Wrapper to create the notification in home
 * with a panel for quick jump to other areas of Olat
 * <p>
 * Initial Date:  29 nov. 2010 <br>
 * @author srosse, srosse@frentix.com, www.frentix.com
 */
public class HomePortalControllerCreator extends AutoCreator  {

	/**
	 * @see org.olat.core.gui.control.creator.AutoCreator#getClassName()
	 */
	@Override
	public String getClassName() {
		return this.getClass().getCanonicalName();
	}

	public HomePortalControllerCreator() {
		super();
	}	
	
	/**
	 * @see org.olat.core.gui.control.creator.ControllerCreator#createController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public Controller createController(UserRequest ureq, WindowControl lwControl) {
		return new HomePortalController(ureq, lwControl);
	}
	
	public class HomePortalController extends BasicController {
		
		private final VelocityContainer welcome;
		private final Link portalBackButton;
		private final Link portalEditButton;
		private PortalImpl myPortal;
		
		public HomePortalController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl, Util.createPackageTranslator(InviteeHomeMainController.class, ureq.getLocale()));
			// start screen
			welcome = createVelocityContainer("welcome");
			portalBackButton = LinkFactory.createButtonXSmall("command.portal.back", welcome, this);
			portalEditButton = LinkFactory.createButtonXSmall("command.portal.edit", welcome, this);
			
			if(CoreSpringFactory.containsBean("baksModule")){
				welcome.contextPut("isbaks", true);
			}else{
				welcome.contextPut("isbaks", false);
			}
			
			// rss link
			String rssLink = RSSUtil.getPersonalRssLink(ureq);
			welcome.contextPut("rssLink", rssLink);
			StringOutput staticUrl = new StringOutput();
			StaticMediaDispatcher.renderStaticURI(staticUrl, "js/egg.js");
			welcome.put("htmlHeader", new HtmlHeaderComponent("rss", null, "<link rel=\"alternate\" type=\"application/rss+xml\" title=\""
					+ translate("welcome.rss") + "\" href=\"" + rssLink + "\" />\n" + "<script type=\"text/javascript\" src=\""
					+ staticUrl.toString() + "\"></script>"));

			// add portal
			if (myPortal == null) {
				if(ureq.getUserSession().getRoles().isGuestOnly()){
					myPortal = ((PortalImpl)CoreSpringFactory.getBean("guestportal")).createInstance(getWindowControl(), ureq);
					portalEditButton.setEnabled(false);
					portalEditButton.setVisible(false);
				}else{
					myPortal = ((PortalImpl)CoreSpringFactory.getBean("homeportal")).createInstance(getWindowControl(), ureq);
				}
			}
			
			welcome.put("myPortal", myPortal.getInitialComponent());
			welcome.contextPut("portalEditMode", Boolean.FALSE);

			putInitialPanel(welcome);
		}
		
		@Override
		protected void doDispose() {
			if (myPortal != null) {
				myPortal.dispose();
				myPortal = null;
			}
		}

		@Override
		public void event(UserRequest ureq, Component source, Event event) {
			if (source == portalBackButton){
				this.myPortal.setIsEditMode(ureq, Boolean.FALSE);
				welcome.contextPut("portalEditMode", Boolean.FALSE);
			} else if (source == portalEditButton){
				this.myPortal.setIsEditMode(ureq, Boolean.TRUE);
				welcome.contextPut("portalEditMode", Boolean.TRUE);
			} 
		}
	}
}
