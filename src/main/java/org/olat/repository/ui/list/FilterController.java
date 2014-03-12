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

import java.util.ArrayList;
import java.util.List;

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
import org.olat.repository.SearchMyRepositoryEntryViewParams.Filter;

/**
 * 
 * Initial date: 20.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FilterController extends BasicController {
	
	private final Link currentCoursesLink, upcomingCoursesLink, oldCoursesLink;
	private final Link participantLink, coachLink, authorLink, nothingLink;
	private final Link passedLink, notPassedLink, withoutPassedInfosLink;
	
	private final List<Filter> filters = new ArrayList<>();
	
	public FilterController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		VelocityContainer mainVC = createVelocityContainer("filters");
		
		//lifecycle
		currentCoursesLink = LinkFactory.createCustomLink("current.courses", "current.courses", "filter.current.courses", Link.LINK, mainVC, this);
		currentCoursesLink.setUserObject(Filter.currentCourses);
		upcomingCoursesLink = LinkFactory.createCustomLink("upcoming.courses", "upcoming.courses", "filter.upcoming.courses", Link.LINK, mainVC, this);
		upcomingCoursesLink.setUserObject(Filter.upcomingCourses);
		oldCoursesLink = LinkFactory.createCustomLink("old.courses", "old.courses", "filter.old.courses", Link.LINK, mainVC, this);
		oldCoursesLink.setUserObject(Filter.oldCourses);
		
		//membership
		participantLink = LinkFactory.createCustomLink("as.participant", "as.participant", "filter.booked.participant", Link.LINK, mainVC, this);
		participantLink.setUserObject(Filter.asParticipant);
		coachLink = LinkFactory.createCustomLink("as.coach", "as.coach", "filter.booked.coach", Link.LINK, mainVC, this);
		coachLink.setUserObject(Filter.asCoach);
		authorLink = LinkFactory.createCustomLink("as.author", "as.author", "filter.booked.author", Link.LINK, mainVC, this);
		authorLink.setUserObject(Filter.asAuthor);
		nothingLink = LinkFactory.createCustomLink("not.booked", "not.booked", "filter.not.booked", Link.LINK, mainVC, this);
		nothingLink.setUserObject(Filter.notBooked);
		
		//efficiency statment
		passedLink = LinkFactory.createCustomLink("passed", "passed", "filter.passed", Link.LINK, mainVC, this);
		passedLink.setUserObject(Filter.passed);
		notPassedLink = LinkFactory.createCustomLink("not.passed", "not.passed", "filter.not.passed", Link.LINK, mainVC, this);
		notPassedLink.setUserObject(Filter.notPassed);
		withoutPassedInfosLink = LinkFactory.createCustomLink("without", "without", "filter.without.passed.infos", Link.LINK, mainVC, this);
		withoutPassedInfosLink.setUserObject(Filter.withoutPassedInfos);
		
		mainVC.contextPut("filters", filters);
		
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link) {
			Link link = (Link)source;
			Filter filter = (Filter)link.getUserObject();
			toggleFilter(filter);
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
	
	public List<Filter> getSelectedFilters() {
		return filters;
	}
	
	private void toggleFilter(Filter filter) {
		if(filters.contains(filter)) {
			filters.remove(filter);
		} else {
			filters.add(filter);
		}
	}
}
