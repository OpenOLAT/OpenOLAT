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
package org.olat.course.nodes;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.util.prefs.Preferences;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 25 Jul 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeSegmentPrefs {
	
	public enum CourseNodeSegment {
		
		overview,
		participants,
		preview,
		reminders

	}

	private static Set<String> SEGMENT_NAMES = Arrays.stream(CourseNodeSegment.values())
			.map(CourseNodeSegment::name)
			.collect(Collectors.toSet());

	private RepositoryEntryRef courseEntry;
	
	public CourseNodeSegmentPrefs(RepositoryEntryRef courseEntry) {
		this.courseEntry = courseEntry;
	}

	public void setSegment(UserRequest ureq, CourseNodeSegment segment) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.putAndSave(CourseNodeSegmentPrefs.class, getKey(), segment.name());
		}
	}
	
	public CourseNodeSegment getSegment(UserRequest ureq) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		String segmentName = (String) guiPrefs.get(CourseNodeSegmentPrefs.class, getKey());
		return SEGMENT_NAMES.contains(segmentName)? CourseNodeSegment.valueOf(segmentName): null;
	}

	private String getKey() {
		return "course.node.segment::" + courseEntry.getKey();
	}

}
