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
package org.olat.portfolio;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.structel.PortfolioStructureMap;

/**
 * Description:<br>
 * load my maps menu-entry. config here instead of xml allows en-/disabling at
 * runtime
 * 
 * <P>
 * Initial Date: 03.08.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPMapOnInvitationExtension {

	public EPMapOnInvitationExtension() {

		NewControllerFactory.getInstance().addContextEntryControllerCreator("MapInvitation", new DefaultContextEntryControllerCreator(){
			
			@Override
			public Controller createController(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
				//fxdiff FXOLAT-151: better check invitation
				if(!ureq.getUserSession().getRoles().isInvitee()) {
					return null;
				}
				
				PortfolioStructureMap map = getMapFromContext(ce);
				EPSecurityCallback secCallback = new EPSecurityCallbackImpl(false, true);
				Controller epCtr = EPUIFactory.createMapViewController(ureq, wControl, map, secCallback);
				
				LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, null, null, epCtr.getInitialComponent(), null);
				layoutCtr.addDisposableChildController(epCtr);
				return layoutCtr;
			}

			@Override
			public String getTabName(ContextEntry ce, UserRequest ureq) {
				PortfolioStructureMap map = getMapFromContext(ce);
				return map.getTitle();
			}

			@Override
			public String getSiteClassName(ContextEntry ce, UserRequest ureq) {
				return null;
			}

			@Override
			public boolean validateContextEntryAndShowError(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
				//fxdiff FXOLAT-151: better check invitation
				if(!ureq.getUserSession().getRoles().isInvitee()) {
					return false;
				}
				
				final EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
				PortfolioStructureMap map = getMapFromContext(ce);
				if (map == null) return false;
				boolean visible = ePFMgr.isMapVisible(ureq.getIdentity(), map.getOlatResource());
				return visible;
			}
			
			/**
			 * @param ContextEntry
			 * @return the loaded map or null if not found
			 */
			private PortfolioStructureMap getMapFromContext(final ContextEntry ce) {
				final Long mapKey = ce.getOLATResourceable().getResourceableId();
				final EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
				final PortfolioStructureMap map = (PortfolioStructureMap) ePFMgr.loadPortfolioStructureByKey(mapKey);
				return map;
			}
			
		});	
	}
}
