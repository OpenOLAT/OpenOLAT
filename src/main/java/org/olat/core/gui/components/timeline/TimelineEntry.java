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


/**
 * Initial date: Mar 17, 2025
 * Represents a single entry in a timeline
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface TimelineEntry {

	/**
	 * Returns the primary title or heading of this timeline entry
	 *
	 * @return a non-null string representing the entry
	 */
	String getTitle();

	/**
	 * Returns a formatted string indicating the relevant time period or timestamp of this entry
	 *
	 * @return a string representation of the time period (e.g., "at 10:00 AM" or "um 10:00 Uhr").
	 */
	String getTimePeriod();

	/**
	 * Returns the location associated with this timeline entry
	 * Can be empty or null if no location information is available
	 *
	 * @return a string representing the location, or null/empty if unspecified
	 */
	String getLocation();
}

