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
package org.olat.modules.lecture.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
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
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.modules.lecture.LectureBlock;


/**
 * 
 * Initial date: Jan 24, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class LectureBlocksTimelineController extends BasicController {
	
	private static final int FIRST_ONLY_LIMIT = 5;

	private Link showAllLink;
	
	private LightboxController lightboxCtrl;
	private LectureBlocksTimelineController allCtrl;
	
	private final List<LectureBlock> lectureBlocks;

	public LectureBlocksTimelineController(UserRequest ureq, WindowControl wControl, List<LectureBlock> lectureBlocks, boolean showFirstOnly) {
		super(ureq, wControl);
		this.lectureBlocks = lectureBlocks;
		
		VelocityContainer mainVC = createVelocityContainer("timeline");
		putInitialPanel(mainVC);
		
		Formatter formatter = Formatter.getInstance(getLocale());
		
		if (lectureBlocks.isEmpty()) {
			return;
		}
		
		if (showFirstOnly) {
			showAllLink = LinkFactory.createButton("show.all.events", mainVC, this);
			showAllLink.setIconLeftCSS("o_icon o_icon_events");
			showAllLink.setGhost(true);
		}
		
		boolean timelineComplete = !showFirstOnly || lectureBlocks.size() <= FIRST_ONLY_LIMIT;
		mainVC.contextPut("timelineComplete", timelineComplete);
		
		List<LectureBlock> sortedLectureBlocks = lectureBlocks.stream()
				.filter(lb -> lb.getStartDate() != null)
				.sorted(new LectureBlockStartComparator())
				.limit(showFirstOnly? FIRST_ONLY_LIMIT: 10000)
				.toList();
		
		Date currentDate = lectureBlocks.get(0).getStartDate();
		int currentYear = getYear(currentDate);
		List<TimelineLectureBlock> timelineLectureBlocks = new ArrayList<>(2);
		List<TimelineDay> days = new ArrayList<>();
		List<TimelineYear> years = new ArrayList<>(1);
		
		for (LectureBlock lectureBlock : sortedLectureBlocks) {
			String period = formatTimePeriod(formatter, lectureBlock);
			TimelineLectureBlock timelineLectureBlock = new TimelineLectureBlock(lectureBlock.getTitle(), period, lectureBlock.getLocation());
			if (!DateUtils.isSameDay(currentDate, lectureBlock.getStartDate()) ) {
				days.add(createDay(formatter, currentDate, timelineLectureBlocks));
				
				int year = getYear(lectureBlock.getStartDate());
				if (currentYear != year) {
					years.add(createYear(currentYear, days));
					currentYear = year;
					days = new ArrayList<>();
				}
				
				currentDate = lectureBlock.getStartDate();
				timelineLectureBlocks = new ArrayList<>(2);
			}
			timelineLectureBlocks.add(timelineLectureBlock);
		}
		
		years.add(createYear(currentYear, days));
		days.add(createDay(formatter, currentDate, timelineLectureBlocks));
		mainVC.contextPut("years", years);
	}

	private TimelineYear createYear(int currentYear, List<TimelineDay> days) {
		return new TimelineYear(currentYear, days);
	}

	private TimelineDay createDay(Formatter formatter, Date currentDate,
			List<TimelineLectureBlock> timelineLectureBlocks) {
		return new TimelineDay(getMonthName(currentDate), formatter.dayOfWeekShort(currentDate), getDayOfMonth(currentDate), timelineLectureBlocks);
	}

	private int getYear(Date currentDate) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(currentDate);
		return calendar.get(Calendar.YEAR);
	}
	
	private int getDayOfMonth(Date currentDate) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(currentDate);
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	private String getMonthName(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.getDisplayName(Calendar.MONTH, Calendar.SHORT_FORMAT, getLocale());
	}

	private String formatTimePeriod(Formatter formatter, LectureBlock lectureBlock) {
		String period = formatter.formatTimeShort(lectureBlock.getStartDate());
		if (lectureBlock.getEndDate() != null) {
			period += " - " + formatter.formatTimeShort(lectureBlock.getEndDate());
		}
		return period;
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
		}
	}
	
	private void doShowAll(UserRequest ureq) {
		if (guardModalController(allCtrl)) return;
		
		removeAsListenerAndDispose(allCtrl);
		
		allCtrl = new LectureBlocksTimelineController(ureq, getWindowControl(), lectureBlocks, false);
		listenTo(allCtrl);
		
		lightboxCtrl = new LightboxController(ureq, getWindowControl(), allCtrl);
		listenTo(lightboxCtrl);
		lightboxCtrl.activate();
	}

	public static final class TimelineYear {
		
		private final Integer year;
		private final List<TimelineDay> days;
		
		public TimelineYear(Integer year, List<TimelineDay> days) {
			this.year = year;
			this.days = days;
		}

		public Integer getYear() {
			return year;
		}

		public List<TimelineDay> getDays() {
			return days;
		}
		
	}
	
	public static final class TimelineDay {
		
		private final String monthName;
		private final String dayName;
		private final Integer dayOfMonth;
		private final List<TimelineLectureBlock> lectureBlocks;
		
		public TimelineDay(String monthName, String dayName, Integer dayOfMonth, List<TimelineLectureBlock> lectureBlocks) {
			this.monthName = monthName;
			this.dayName = dayName;
			this.dayOfMonth = dayOfMonth;
			this.lectureBlocks = lectureBlocks;
		}

		public String getMonthName() {
			return monthName;
		}
		
		public String getDayName() {
			return dayName;
		}

		public Integer getDayOfMonth() {
			return dayOfMonth;
		}

		public List<TimelineLectureBlock> getLectureBlocks() {
			return lectureBlocks;
		}
		
	}
	
	public static final class TimelineLectureBlock {
		
		private final String title;
		private final String timePeriod;
		private final String location;
		
		public TimelineLectureBlock(String title, String timePeriod, String location) {
			this.title = title;
			this.timePeriod = timePeriod;
			this.location = location;
		}
		
		public String getTitle() {
			return title;
		}
		
		public String getTimePeriod() {
			return timePeriod;
		}
		
		public String getLocation() {
			return location;
		}
		
	}
	
	public class LectureBlockStartComparator implements Comparator<LectureBlock> {

		@Override
		public int compare(LectureBlock o1, LectureBlock o2) {
			Date s1 = o1.getStartDate();
			Date s2 = o2.getStartDate();
			if(s1 == null && s2 == null) return 0;
			if(s1 == null) return 1;
			if(s2 == null) return -1;
			return s1.compareTo(s2);
		}
	}

}
