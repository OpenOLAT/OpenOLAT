/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.coach.model;

import java.util.List;

/**
 * 
 * Initial date: 3 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public record CoursesSummary(List<CourseStatEntry> courseStatistics, int numCourses, int numMarked, int numCoachPublished, int numPublished, int numFinished) {
	
	public int numOf(CoursesSummaryKeyFigures keyFigure) {
		if(keyFigure != null) {
			return switch(keyFigure) {
				case marked -> numMarked;
				case relevant -> numCoachPublished + numPublished;
				case published -> numPublished;
				case access -> numCoachPublished;
				case finished -> numFinished;
				default -> numCourses;
			};
		}
		return numCourses;
	}
}
