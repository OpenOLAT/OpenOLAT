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
package org.olat.commons.calendar;

import org.apache.logging.log4j.Logger;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.util.CompatibilityHints;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  21 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("calendarModule")
public class CalendarModule extends AbstractSpringModule {
	
	private static final Logger log = Tracing.createLoggerFor(CalendarModule.class);
	
	private static final String CALENDAR_ENABLED = "calendar.enable";
	private static final String CALENDAR_PERSONAL_ENABLED = "calendar.personal.enabled";
	private static final String CALENDAR_GROUP_ENABLED = "calendar.group.enabled";
	private static final String CALENDAR_COURSE_TOOL_ENABLED = "calendar.course.tool.enabled";
	private static final String CALENDAR_COURSE_ELEMENT_ENABLED = "calendar.course.element.enabled";
	private static final String MANAGED_CAL_ENABLED = "managedCalendars";
	
	private TimeZone defaultTimeZone;
	private TimeZoneRegistry timeZoneRegistry;
	
	@Value("${calendar.enabled:true}")
	private boolean enabled;
	@Value("${calendar.personal.enabled:true}")
	private boolean enablePersonalCalendar;
	@Value("${calendar.group.enabled:true}")
	private boolean enableGroupCalendar;
	@Value("${calendar.course.tool.enabled:true}")
	private boolean enableCourseToolCalendar;
	@Value("${calendar.course.element.enabled:true}")
	private boolean enableCourseElementCalendar;

	@Value("${calendar.managed:false}")
	private boolean managedCalendars;
	
	@Autowired
	public CalendarModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	@Override
	public void init() {
		// infinispan as JCache throw some exception. So we use the internal cache implementation
		System.setProperty("net.fortuna.ical4j.timezone.cache.impl", "net.fortuna.ical4j.util.MapTimeZoneCache");
		//some computers have no Internet access, the host can be down and we must get the default time zone
		System.setProperty("net.fortuna.ical4j.timezone.update.enabled", "false");
		System.setProperty(CompatibilityHints.KEY_RELAXED_UNFOLDING, "true");
		System.setProperty(CompatibilityHints.KEY_RELAXED_PARSING, "true");
		String defaultTimeZoneID = java.util.TimeZone.getDefault().getID();
		log.info("Calendar time zone: {}", defaultTimeZoneID);
		timeZoneRegistry = TimeZoneRegistryFactory.getInstance().createRegistry();
		defaultTimeZone = timeZoneRegistry.getTimeZone(defaultTimeZoneID);
		if(defaultTimeZone == null) {
			log.error("Cannot match the JVM default time zone to an ical4j time zone: {}", defaultTimeZoneID);
		}
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		String enabledObj = getStringPropertyValue(CALENDAR_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String personalEnabledObj = getStringPropertyValue(CALENDAR_PERSONAL_ENABLED, true);
		if(StringHelper.containsNonWhitespace(personalEnabledObj)) {
			enablePersonalCalendar = "true".equals(personalEnabledObj);
		}
		
		String groupEnabledObj = getStringPropertyValue(CALENDAR_GROUP_ENABLED, true);
		if(StringHelper.containsNonWhitespace(groupEnabledObj)) {
			enableGroupCalendar = "true".equals(groupEnabledObj);
		}
		
		String courseToolEnabledObj = getStringPropertyValue(CALENDAR_COURSE_TOOL_ENABLED, true);
		if(StringHelper.containsNonWhitespace(courseToolEnabledObj)) {
			enableCourseToolCalendar = "true".equals(courseToolEnabledObj);
		}
		
		String courseElementEnabledObj = getStringPropertyValue(CALENDAR_COURSE_ELEMENT_ENABLED, true);
		if(StringHelper.containsNonWhitespace(courseElementEnabledObj)) {
			enableCourseElementCalendar = "true".equals(courseElementEnabledObj);
		}
		
		String managedCalEnabledObj = getStringPropertyValue(MANAGED_CAL_ENABLED, true);
		if(StringHelper.containsNonWhitespace(managedCalEnabledObj)) {
			managedCalendars = "true".equals(managedCalEnabledObj);
		}
	}
	
	public TimeZone getDefaultTimeZone() {
		return defaultTimeZone;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(CALENDAR_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isEnablePersonalCalendar() {
		return enablePersonalCalendar;
	}

	public void setEnablePersonalCalendar(boolean enablePersonalCalendar) {
		this.enablePersonalCalendar = enablePersonalCalendar;
		setStringProperty(CALENDAR_PERSONAL_ENABLED, Boolean.toString(enablePersonalCalendar), true);
	}

	public boolean isEnableGroupCalendar() {
		return enableGroupCalendar;
	}

	public void setEnableGroupCalendar(boolean enableGroupCalendar) {
		this.enableGroupCalendar = enableGroupCalendar;
		setStringProperty(CALENDAR_GROUP_ENABLED, Boolean.toString(enableGroupCalendar), true);
	}

	public boolean isEnableCourseToolCalendar() {
		return enableCourseToolCalendar;
	}

	public void setEnableCourseToolCalendar(boolean enableCourseToolCalendar) {
		this.enableCourseToolCalendar = enableCourseToolCalendar;
		setStringProperty(CALENDAR_COURSE_TOOL_ENABLED, Boolean.toString(enableCourseToolCalendar), true);
	}

	public boolean isEnableCourseElementCalendar() {
		return enableCourseElementCalendar;
	}

	public void setEnableCourseElementCalendar(boolean enableCourseElementCalendar) {
		this.enableCourseElementCalendar = enableCourseElementCalendar;
		setStringProperty(CALENDAR_COURSE_ELEMENT_ENABLED, Boolean.toString(enableCourseElementCalendar), true);
	}
	
	public boolean isManagedCalendars() {
		return managedCalendars;
	}

	public void setManagedCalendars(boolean enabled) {
		this.managedCalendars = enabled;
		setStringProperty(MANAGED_CAL_ENABLED, Boolean.toString(enabled), true);
	}
}