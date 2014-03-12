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
package org.olat.repository.ui.list;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryManager;
import org.olat.repository.SearchMyRepositoryEntryViewParams.OrderBy;

/**
 * 
 * Initial date: 20.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrderByController extends BasicController {
	
	private final Link automaticLink, favoritLink, lastVisitedLink, passedLink, scoreLink;
	private final Link titleLink, lifecycleLink, authorLink, creationDateLink, lastModifiedLink, ratingLink;
	
	private OrderBy orderBy;
	
	public OrderByController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		VelocityContainer mainVC = createVelocityContainer("orderby");
		
		automaticLink = LinkFactory.createLink("orderby.automatic", mainVC, this);
		automaticLink.setUserObject(OrderBy.automatic);
		favoritLink = LinkFactory.createLink("orderby.favorit", mainVC, this);
		favoritLink.setUserObject(OrderBy.favorit);
		lastVisitedLink = LinkFactory.createLink("orderby.lastVisited", mainVC, this);
		lastVisitedLink.setUserObject(OrderBy.lastVisited);
		scoreLink = LinkFactory.createLink("orderby.score", mainVC, this);
		scoreLink.setUserObject(OrderBy.score);
		passedLink = LinkFactory.createLink("orderby.passed", mainVC, this);
		passedLink.setUserObject(OrderBy.passed);
		
		titleLink = LinkFactory.createLink("orderby.title", mainVC, this);
		titleLink.setUserObject(OrderBy.title);
		lifecycleLink = LinkFactory.createLink("orderby.lifecycle", mainVC, this);
		lifecycleLink.setUserObject(OrderBy.lifecycle);
		authorLink = LinkFactory.createLink("orderby.author", mainVC, this);
		authorLink.setUserObject(OrderBy.author);
		creationDateLink = LinkFactory.createLink("orderby.creationDate", mainVC, this);
		creationDateLink.setUserObject(OrderBy.creationDate);
		lastModifiedLink = LinkFactory.createLink("orderby.lastModified", mainVC, this);
		lastModifiedLink.setUserObject(OrderBy.lastModified);
		ratingLink = LinkFactory.createLink("orderby.rating", mainVC, this);
		ratingLink.setUserObject(OrderBy.rating);
		
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	public OrderBy getOrderBy() {
		return orderBy;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link) {
			Link link = (Link)source;
			orderBy = (OrderBy)link.getUserObject();
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
}
