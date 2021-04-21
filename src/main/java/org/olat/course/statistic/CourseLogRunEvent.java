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
package org.olat.course.statistic;

import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 21 avr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseLogRunEvent extends MultiUserEvent {

	private static final long serialVersionUID = -5955783690369293816L;

	public static final String COURSE_LOG_READY = "course-log-ready";
	
	private final Long identityKey;
	private final Long oresId;
	
	public CourseLogRunEvent(Long identityKey, Long oresId) {
		super(COURSE_LOG_READY);
		this.identityKey = identityKey;
		this.oresId = oresId;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public Long getOresId() {
		return oresId;
	}
}
