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
package org.olat.core.gui.components.timeline;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.lightbox.LightboxController;
import org.olat.core.gui.components.timeline.TimelineModel.*;
import org.olat.core.gui.translator.Translator;

/**
 * Initial date: Mar 17, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TimelineController extends BasicController {

	private Link showAllLink;
	private Link downloadLink;
	private LightboxController lightboxCtrl;
	private TimelineController allCtrl;

	private final List<TimelineYear> fullTimelineYears;
	private final boolean hasDownloadBtn;

	/**
	 * @param ureq
	 * @param wControl
	 * @param timelineYears the timeline model to display (could be partial)
	 * @param fullTimelineYears the complete timeline model (for "show all")
	 * @param showFirstOnly if true, a limited set of events is shown initially
	 */
	public TimelineController(UserRequest ureq, WindowControl wControl,
							  Translator translator,
							  List<TimelineYear> timelineYears,
							  List<TimelineYear> fullTimelineYears,
							  boolean showFirstOnly,
							  boolean hasDownloadBtn) {
		super(ureq, wControl, translator);
		this.fullTimelineYears = fullTimelineYears != null ? fullTimelineYears : List.of();
		this.hasDownloadBtn = hasDownloadBtn;

		VelocityContainer mainVC = createVelocityContainer("timeline");
		putInitialPanel(mainVC);

		timelineYears = timelineYears != null ? timelineYears : List.of();
		if (timelineYears.isEmpty()) {
			return;
		}

		int totalEventsFull = getTotalEvents(this.fullTimelineYears);
		int totalEventsDisplayed = getTotalEvents(timelineYears);
		if (showFirstOnly && totalEventsFull > totalEventsDisplayed) {
			showAllLink = LinkFactory.createButton("show.all.events", mainVC, this);
			showAllLink.setIconLeftCSS("o_icon o_icon_events");
			showAllLink.setGhost(true);
		}

		if (hasDownloadBtn) {
			downloadLink = LinkFactory.createCustomLink("download.timeline", "download.timeline", null, Link.NONTRANSLATED + Link.BUTTON, mainVC, this);
			downloadLink.setIconLeftCSS("o_icon o_icon_download");
			downloadLink.setElementCssClass("o_sel_timeline_download");
			downloadLink.setGhost(true);
			mainVC.put("downloadLink", downloadLink);
		}

		boolean timelineComplete = !showFirstOnly || (totalEventsFull <= totalEventsDisplayed);
		mainVC.contextPut("timelineComplete", timelineComplete);
		mainVC.contextPut("years", timelineYears);
	}

	private int getTotalEvents(List<TimelineYear> years) {
		int count = 0;
		for (TimelineYear year : years) {
			for (TimelineDay day : year.getDays()) {
				count += day.getEntries().size();
			}
		}
		return count;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == lightboxCtrl) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(allCtrl);
		removeAsListenerAndDispose(lightboxCtrl);
		allCtrl = null;
		lightboxCtrl = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == showAllLink) {
			doShowAll(ureq);
		} else if (source == downloadLink) {
			fireEvent(ureq, new Event("downloadTimeline"));
		}
	}

	private void doShowAll(UserRequest ureq) {
		if (guardModalController(allCtrl)) return;
		removeAsListenerAndDispose(allCtrl);

		allCtrl = new TimelineController(ureq, getWindowControl(), getTranslator(),
				fullTimelineYears, fullTimelineYears, false, hasDownloadBtn);
		listenTo(allCtrl);

		lightboxCtrl = new LightboxController(ureq, getWindowControl(), allCtrl);
		listenTo(lightboxCtrl);
		lightboxCtrl.activate();
	}
}

