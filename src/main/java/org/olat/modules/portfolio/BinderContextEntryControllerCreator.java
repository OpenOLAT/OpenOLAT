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
package org.olat.modules.portfolio;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.core.id.context.TabContext;
import org.olat.core.util.resource.OresHelper;
import org.olat.home.HomeSite;
import org.olat.modules.ceditor.ContentRoles;
import org.olat.modules.portfolio.model.BinderRefImpl;

/**
 * 
 * Initial date: 11.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class BinderContextEntryControllerCreator extends DefaultContextEntryControllerCreator {

	private Binder cachedBinder;
	
	@Override
	public ContextEntryControllerCreator clone() {
		return new BinderContextEntryControllerCreator();
	}
	
	@Override
	public String getSiteClassName(List<ContextEntry> ces, UserRequest ureq) {
		return HomeSite.class.getName();
	}

	@Override
	public boolean validateContextEntryAndShowError(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
		if(ureq.getUserSession().getRoles().isInvitee()) {
			return false;
		}
		
		Binder binder = getBinderFromContext(ce);
		if (binder == null) {
			return false;
		}
		
		final PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
		boolean visible = portfolioService.isMember(binder, ureq.getIdentity(),
				ContentRoles.owner.name(), ContentRoles.coach.name(), ContentRoles.reviewer.name());
		return visible;
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
	
	@Override
	public TabContext getTabContext(UserRequest ureq, OLATResourceable ores, ContextEntry mainEntry, List<ContextEntry> entries) {
		Identity identity = ureq.getIdentity();
		PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
		Long binderKey = mainEntry.getOLATResourceable().getResourceableId();
		BinderRef binder = new BinderRefImpl(binderKey);
		
		List<ContextEntry> rewrittenEntries = new ArrayList<>();
		OLATResourceable homeRes = OresHelper.createOLATResourceableInstance("HomeSite", identity.getKey());
		rewrittenEntries.add(BusinessControlFactory.getInstance().createContextEntry(homeRes));
		rewrittenEntries.add(BusinessControlFactory.getInstance()
				.createContextEntry(OresHelper.createOLATResourceableInstance("PortfolioV2", 0L)));
		if(portfolioService.isMember(binder, identity, ContentRoles.owner.name())) {
			rewrittenEntries.add(BusinessControlFactory.getInstance()
					.createContextEntry(OresHelper.createOLATResourceableInstance("MyBinders", 0L)));
		} else if(portfolioService.isMember(binder, identity, ContentRoles.coach.name(), ContentRoles.reviewer.name())) {
			rewrittenEntries.add(BusinessControlFactory.getInstance()
					.createContextEntry(OresHelper.createOLATResourceableInstance("SharedWithMe", 0L)));
		}
	
		rewrittenEntries.add(mainEntry);//Binder
		if(entries != null && !entries.isEmpty()) {
			rewrittenEntries.addAll(entries);//more details
		}
		// -> HomeSite
		return new TabContext("", homeRes, rewrittenEntries);
	}
}