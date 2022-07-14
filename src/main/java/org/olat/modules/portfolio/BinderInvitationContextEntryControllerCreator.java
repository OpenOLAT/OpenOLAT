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
package org.olat.modules.portfolio;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.ui.PortfolioInvitationController;

/**
 * 
 * Initial date: 29.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderInvitationContextEntryControllerCreator extends DefaultContextEntryControllerCreator {

	private Binder cachedBinder;
	
	@Override
	public ContextEntryControllerCreator clone() {
		return new BinderInvitationContextEntryControllerCreator();
	}

	@Override
	public Controller createController(List<ContextEntry> ces, UserRequest ureq, WindowControl wControl) {
		if(!ureq.getUserSession().getRoles().isInvitee()) {
			return null;
		}
		
		Binder binder = getBinderFromContext(ces.get(0));
		BinderConfiguration config = BinderConfiguration.createInvitationConfig();
		List<AccessRights> rights = CoreSpringFactory.getImpl(PortfolioService.class)
				.getAccessRights(binder, ureq.getIdentity());
		BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getCallbackForInvitation(rights);
		Controller binderCtrl = new PortfolioInvitationController(ureq, wControl, secCallback,  binder, config);
		
		LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, binderCtrl);
		layoutCtr.addDisposableChildController(binderCtrl);
		return layoutCtr;
	}

	@Override
	public String getTabName(ContextEntry ce, UserRequest ureq) {
		Binder binder = getBinderFromContext(ce);
		if(binder != null) {
			return binder.getTitle();
		}
		return null;
	}

	@Override
	public boolean validateContextEntryAndShowError(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
		if(!ureq.getUserSession().getRoles().isInvitee()) {
			return false;
		}
		
		Binder binder = getBinderFromContext(ce);
		if (binder == null) {
			return false;
		}
		
		final PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
		return portfolioService.isMember(binder, ureq.getIdentity(), PortfolioRoles.invitee.name());
	}
	
	/**
	 * @param ContextEntry
	 * @return the loaded map or null if not found
	 */
	private Binder getBinderFromContext(final ContextEntry ce) {
		if(cachedBinder == null) {
			Long mapKey = ce.getOLATResourceable().getResourceableId();
			cachedBinder = CoreSpringFactory.getImpl(PortfolioService.class).getBinderByKey(mapKey);
		}
		return cachedBinder;
	}
}