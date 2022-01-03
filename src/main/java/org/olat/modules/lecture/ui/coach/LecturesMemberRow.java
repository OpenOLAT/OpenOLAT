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

import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 9 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesMemberRow extends UserPropertiesRow {
	
	private final Double requiredRate;
	private final Double attendanceRate;

	public LecturesMemberRow(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		this(identity, userPropertyHandlers, null, null, locale);
	}
	
	public LecturesMemberRow(Identity identity, List<UserPropertyHandler> userPropertyHandlers,
			Double attendanceRate, Double requiredRate, Locale locale) {
		super(identity, userPropertyHandlers, locale);
		this.requiredRate = requiredRate;
		this.attendanceRate = attendanceRate;
	}

	public Double getRequiredRate() {
		return requiredRate;
	}

	public Double getAttendanceRate() {
		return attendanceRate;
	}
}
