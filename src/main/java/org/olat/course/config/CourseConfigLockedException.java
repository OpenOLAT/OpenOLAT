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
package org.olat.course.config;

/**
 * Thrown if the configuration of a course cannot be updated because
 * an edit session is already open for the course (e.g. the course
 * is opened in the course editor).
 *
 * Initial date: 2 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class CourseConfigLockedException extends RuntimeException {

	private static final long serialVersionUID = -6315530947447219470L;

	public CourseConfigLockedException(Long courseResourceableId) {
		super("There is already an edit session open for this course: " + courseResourceableId);
	}
}
