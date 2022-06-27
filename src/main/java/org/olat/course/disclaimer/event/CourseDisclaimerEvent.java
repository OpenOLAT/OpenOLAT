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
package org.olat.course.disclaimer.event;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 19 May 2022<br>
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class CourseDisclaimerEvent extends Event {
	private static final long serialVersionUID = -1981898294669087019L;
	
	public static final Event ACCEPTED = new CourseDisclaimerEvent("course-disclaimer-accepted");
	public static final Event REJECTED = new CourseDisclaimerEvent("course-disclaimer-rejected");
	
	private CourseDisclaimerEvent(String status) {
		super(status);
	}

}
