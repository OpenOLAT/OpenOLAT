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
package org.olat.modules.lecture.ui.coach;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.lecture.ui.event.OpenRepositoryEntryEvent;
import org.olat.repository.RepositoryEntry;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * 
 * Initial date: 5 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntriesCalloutController extends BasicController {
	
	private int counter = 0;
	
	public RepositoryEntriesCalloutController(UserRequest ureq, WindowControl wControl, List<RepositoryEntry> entries) {
		super(ureq, wControl);
		
		List<RepositoryEntry> orderedEntries = new ArrayList<>(entries);
		Collections.sort(orderedEntries, new RepositoryEntryComparator(getLocale()));
		
		VelocityContainer mainVC = createVelocityContainer("tools_entries");
		List<Link> links = new ArrayList<>(entries.size());
		for(RepositoryEntry entry:orderedEntries) {
			Link link = addLink(entry, mainVC);
			links.add(link);
		}
		mainVC.contextPut("links", links);
		putInitialPanel(mainVC);
	}
	
	private Link addLink(RepositoryEntry entry, VelocityContainer mainVC) {
		String name = "entry_" + (++counter);
		Link link = LinkFactory.createCustomLink(name, "entry", entry.getDisplayname(), Link.LINK | Link.NONTRANSLATED, mainVC, this);
		link.setUserObject(entry);
		mainVC.put(name, link);
		return link;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link) {
			Link link = (Link)source;
			if("entry".equals(link.getCommand())) {
				RepositoryEntry entry = (RepositoryEntry)link.getUserObject();
				fireEvent(ureq, new OpenRepositoryEntryEvent(entry));
			}
		}
	}
	
	private static class RepositoryEntryComparator implements Comparator<RepositoryEntry> {
		
		private final Collator collator;
		
		public RepositoryEntryComparator(Locale locale) {
			collator = Collator.getInstance(locale);
		}
		
		@Override
		public int compare(RepositoryEntry o1, RepositoryEntry o2) {
			String d1 = o1.getDisplayname();
			String d2 = o2.getDisplayname();
			return collator.compare(d1, d2);
		}
	}
}
