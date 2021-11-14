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
package org.olat.course.nodes.livestream.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.course.nodes.LiveStreamCourseNode;
import org.olat.course.nodes.cal.CourseCalendars;
import org.olat.course.nodes.livestream.LiveStreamEvent;
import org.olat.course.nodes.livestream.LiveStreamModule;
import org.olat.course.nodes.livestream.LiveStreamService;
import org.olat.course.nodes.livestream.paella.PlayerProfile;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LiveStreamViewersController extends BasicController {

	private final VelocityContainer mainVC;
	
	private final LiveStreamViewerController displayCtrl0;
	private final LiveStreamViewerController displayCtrl1;
	private final LiveStreamViewerController displayCtrl2;
	private final LiveStreamViewerController displayCtrl3;
	private final LiveStreamViewerController displayCtrl4;
	private final LiveStreamViewerController displayCtrl5;
	private final LiveStreamViewerController displayCtrl6;
	private final LiveStreamViewerController displayCtrl7;
	private final LiveStreamViewerController displayCtrl8;
	private final LiveStreamViewerController displayCtrl9;
	
	private final CourseCalendars calendars;
	private final int bufferBeforeMin;
	private final int bufferAfterMin;
	
	private final List<DisplayWrapper> displayWrappers;
	private Boolean noLiveStream;
	
	@Autowired
	private LiveStreamModule liveStreamModule;
	@Autowired
	private LiveStreamService liveStreamService;
	
	public LiveStreamViewersController(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfiguration,
			CourseCalendars calendars) {
		super(ureq, wControl);
		this.calendars = calendars;
		
		bufferBeforeMin = moduleConfiguration.getIntegerSafe(LiveStreamCourseNode.CONFIG_BUFFER_BEFORE_MIN, 0);
		bufferAfterMin = moduleConfiguration.getIntegerSafe(LiveStreamCourseNode.CONFIG_BUFFER_AFTER_MIN, 0);
		PlayerProfile playerProfile = getPlayerProfile(moduleConfiguration);
		
		mainVC = createVelocityContainer("viewers");
		
		displayWrappers = new ArrayList<>();
		displayCtrl0 = new LiveStreamViewerController(ureq, wControl, playerProfile);
		listenTo(displayCtrl0);
		mainVC.put("display0", displayCtrl0.getInitialComponent());
		displayWrappers.add(new DisplayWrapper(displayCtrl0));
		
		displayCtrl1 = new LiveStreamViewerController(ureq, wControl, playerProfile);
		listenTo(displayCtrl1);
		mainVC.put("display1", displayCtrl1.getInitialComponent());
		displayWrappers.add(new DisplayWrapper(displayCtrl1));
		
		displayCtrl2 = new LiveStreamViewerController(ureq, wControl, playerProfile);
		listenTo(displayCtrl2);
		mainVC.put("display2", displayCtrl2.getInitialComponent());
		displayWrappers.add(new DisplayWrapper(displayCtrl2));
		
		displayCtrl3 = new LiveStreamViewerController(ureq, wControl, playerProfile);
		listenTo(displayCtrl3);
		mainVC.put("display3", displayCtrl3.getInitialComponent());
		displayWrappers.add(new DisplayWrapper(displayCtrl3));
		
		displayCtrl4 = new LiveStreamViewerController(ureq, wControl, playerProfile);
		listenTo(displayCtrl4);
		mainVC.put("display4", displayCtrl4.getInitialComponent());
		displayWrappers.add(new DisplayWrapper(displayCtrl4));
		
		displayCtrl5 = new LiveStreamViewerController(ureq, wControl, playerProfile);
		listenTo(displayCtrl5);
		mainVC.put("display5", displayCtrl5.getInitialComponent());
		displayWrappers.add(new DisplayWrapper(displayCtrl5));
		
		displayCtrl6 = new LiveStreamViewerController(ureq, wControl, playerProfile);
		listenTo(displayCtrl6);
		mainVC.put("display6", displayCtrl6.getInitialComponent());
		displayWrappers.add(new DisplayWrapper(displayCtrl6));
		
		displayCtrl7 = new LiveStreamViewerController(ureq, wControl, playerProfile);
		listenTo(displayCtrl7);
		mainVC.put("display7", displayCtrl7.getInitialComponent());
		displayWrappers.add(new DisplayWrapper(displayCtrl7));
		
		displayCtrl8 = new LiveStreamViewerController(ureq, wControl, playerProfile);
		listenTo(displayCtrl8);
		mainVC.put("display8", displayCtrl8.getInitialComponent());
		displayWrappers.add(new DisplayWrapper(displayCtrl8));
		
		displayCtrl9 = new LiveStreamViewerController(ureq, wControl, playerProfile);
		listenTo(displayCtrl9);
		mainVC.put("display9", displayCtrl9.getInitialComponent());
		displayWrappers.add(new DisplayWrapper(displayCtrl9));
		
		refresh(ureq.getUserSession());
		
		putInitialPanel(mainVC);
	}
	
	private PlayerProfile getPlayerProfile(ModuleConfiguration moduleConfiguration) {
		PlayerProfile playerProfile = PlayerProfile.stream1;
		
		if (liveStreamModule.isMultiStreamEnabled()) {
			String nodePlayerProfile = moduleConfiguration.getStringValue(LiveStreamCourseNode.CONFIG_PLAYER_PROFILE);
			if (StringHelper.containsNonWhitespace(nodePlayerProfile)) {
				playerProfile = PlayerProfile.valueOf(nodePlayerProfile);
			} else {
				playerProfile = PlayerProfile.valueOf(liveStreamModule.getPlayerProfile());
			}
		}
		return playerProfile;
	}
	
	void refresh(UserSession usess) {
		List<? extends LiveStreamEvent> events = liveStreamService.getRunningEvents(calendars, bufferBeforeMin, bufferAfterMin);
		putNoLiveStreamToMainVC(events);
		
		// This component should not get dirty, if a new live stream is started or ended to
		// avoid restart of other running live streams. So we hat some slots. If a new
		// live stream starts, it is put to the next free slot. If no slot is left, bad
		// luck.
		events = removeOverlappingWithSameUrl(events);
		Collections.sort(events, (e1, e2) -> e1.getBegin().compareTo(e2.getBegin()));
		displayStartedEvents(usess, events);
		removeEndedEvents(usess, events);
	}

	private void putNoLiveStreamToMainVC(List<? extends LiveStreamEvent> events) {
		Boolean newNoLiveStream = events.isEmpty()? Boolean.TRUE: Boolean.FALSE;
		if (!newNoLiveStream.equals(noLiveStream)) {
			noLiveStream = newNoLiveStream;
			mainVC.contextPut("noLiveStream", noLiveStream);
		}
	}

	private List<? extends LiveStreamEvent> removeOverlappingWithSameUrl(List<? extends LiveStreamEvent> events) {
		List<LiveStreamEvent> remainingEvents = new ArrayList<>();
		List<LiveStreamEvent> removedEvents = new ArrayList<>();
		for (LiveStreamEvent event: events) {
			if (!removedEvents.contains(event)) {
				List<? extends LiveStreamEvent> sameUrlEvents = getEventsWithSameUrl(events, event);
				if (sameUrlEvents.size() > 1) {
					LiveStreamEvent runningEvent = getExcatlyRunning(sameUrlEvents);
					if (runningEvent != null) {
						for (LiveStreamEvent sameUrlEvent : sameUrlEvents) {
							if (runningEvent.equals(sameUrlEvent)) {
								remainingEvents.add(sameUrlEvent);
							} else {
								removedEvents.add(sameUrlEvent);
							}
						}
					}
				} else {
					remainingEvents.add(event);
				}
			}
		}
		return remainingEvents;
	}

	private LiveStreamEvent getExcatlyRunning(List<? extends LiveStreamEvent> events) {
		Collections.sort(events, (e1, e2) -> e1.getBegin().compareTo(e2.getBegin()));
		Date now = new Date();
		for (LiveStreamEvent event: events) {
			if (event.getEnd().after(now)) {
				return event;
			}
		}
		return null;
	}

	private List<? extends LiveStreamEvent> getEventsWithSameUrl(List<? extends LiveStreamEvent> events,
			LiveStreamEvent event) {
		List<LiveStreamEvent> sameUrlEvents = new ArrayList<>();
		for (LiveStreamEvent liveStreamEvent : events) {
			if (liveStreamEvent.getLiveStreamUrl().equalsIgnoreCase(event.getLiveStreamUrl())) {
				sameUrlEvents.add(liveStreamEvent);
			}
		}
		return sameUrlEvents;
	}

	private void displayStartedEvents(UserSession usess, List<? extends LiveStreamEvent> events) {
		for (LiveStreamEvent event: events) {
			DisplayWrapper displayWrapper = getDisplayWrapper(event);
			if (displayWrapper != null) {
				updateEvent(usess, displayWrapper, event);
			} else {
				addToNextDisplay(usess, event);
			}
		}
	}
	
	private DisplayWrapper getDisplayWrapper(LiveStreamEvent event) {
		for (DisplayWrapper displayWrapper : displayWrappers) {
			if (displayWrapper.getEvent() != null && displayWrapper.getEvent().getLiveStreamUrl().equals(event.getLiveStreamUrl())) {
				return displayWrapper;
			}
		}
		return null;
	}

	private void addToNextDisplay(UserSession usess, LiveStreamEvent event) {
		DisplayWrapper nextDisplay = getNextFreeDisplay();
		if (nextDisplay != null ) {
			updateEvent(usess, nextDisplay, event);
		}
	}

	private DisplayWrapper getNextFreeDisplay() {
		DisplayWrapper nextDisplay = null;
		for (int i = displayWrappers.size() - 1; i >= 0; i--) {
			if (displayWrappers.get(i).getEvent() == null) {
				nextDisplay = displayWrappers.get(i);
			} else {
				return nextDisplay;
			}
		}
		return nextDisplay;
	}

	private void removeEndedEvents(UserSession usess, List<? extends LiveStreamEvent> events) {
		for (DisplayWrapper displayWrapper : displayWrappers) {
			LiveStreamEvent wrappedEvent = displayWrapper.getEvent();
			if (hasEnded(wrappedEvent, events)) {
				updateEvent(usess, displayWrapper, null);
			}
		}
	}

	private boolean hasEnded(LiveStreamEvent event, List<? extends LiveStreamEvent> runningEvents) {
		if (event == null) return false;
		
		for (LiveStreamEvent runningEvent : runningEvents) {
			if (runningEvent.getLiveStreamUrl().equals(event.getLiveStreamUrl())) {
				return false;
			}
		}
		return true;
	}

	private void updateEvent(UserSession usess, DisplayWrapper displayWrapper, LiveStreamEvent event) {
		displayWrapper.setEvent(event);
		displayWrapper.getController().setEvent(usess, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	private static class DisplayWrapper {
		
		private final LiveStreamViewerController controller;
		private LiveStreamEvent event;
		
		private DisplayWrapper(LiveStreamViewerController controller) {
			this.controller = controller;
		}

		public LiveStreamViewerController getController() {
			return controller;
		}

		public LiveStreamEvent getEvent() {
			return event;
		}

		public void setEvent(LiveStreamEvent event) {
			this.event = event;
		}

	}

}
