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

import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.util.CompatibilityHints;

import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  21 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CalendarModule extends AbstractOLATModule {
	
	private static final OLog log = Tracing.createLoggerFor(CalendarModule.class);
	
	private TimeZone defaultTimeZone;
	private TimeZoneRegistry timeZoneRegistry;
	
	@Override
	public void init() {
		//some computers have no Internet access, the host can be down and we must get the default time zone
		System.setProperty("net.fortuna.ical4j.timezone.update.enabled", "false");
		System.setProperty(CompatibilityHints.KEY_RELAXED_UNFOLDING, "true");
		System.setProperty(CompatibilityHints.KEY_RELAXED_PARSING, "true");
		String defaultTimeZoneID = java.util.TimeZone.getDefault().getID();
		log.info("Calendar time zone: " + defaultTimeZoneID);
		timeZoneRegistry = TimeZoneRegistryFactory.getInstance().createRegistry();
		defaultTimeZone = timeZoneRegistry.getTimeZone(defaultTimeZoneID);
		if(defaultTimeZone == null) {
			log.error("Cannot match the JVM default time zone to an ical4j time zone: " + defaultTimeZoneID);
		}
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

	@Override
	protected void initDefaultProperties() {
		//
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	public TimeZone getDefaultTimeZone() {
		return defaultTimeZone;
	}
}
