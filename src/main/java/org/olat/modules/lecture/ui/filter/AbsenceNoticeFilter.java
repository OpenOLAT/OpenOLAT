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
package org.olat.modules.lecture.ui.filter;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.ui.coach.AbsenceNoticeRow;
import org.olat.repository.RepositoryEntry;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 16 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceNoticeFilter implements Predicate<AbsenceNoticeRow> {
	
	private final Locale locale;
	private final String searchString;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public AbsenceNoticeFilter(String searchString, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		this.searchString = searchString.toLowerCase();
		this.userPropertyHandlers = userPropertyHandlers;
		this.locale = locale;
	}
	
	@Override
	public boolean test(AbsenceNoticeRow row) {
		List<RepositoryEntry> entries = row.getEntriesList();
		if(entries != null && !entries.isEmpty()) {
			for(RepositoryEntry entry:entries) {
				if(FilterHelper.test(entry, searchString)) {
					return true;
				}
			}
		}
		
		List<LectureBlock> lectureBlocks = row.getLectureBlocks();
		if(lectureBlocks != null && !lectureBlocks.isEmpty()) {
			for(LectureBlock lectureBlock:lectureBlocks) {
				if(FilterHelper.test(lectureBlock, searchString)) {
					return true;
				}
			}
		}
		
		List<Identity> teachers = row.getTeachers();
		if(teachers != null && !teachers.isEmpty()) {
			for(Identity teacher:teachers) {
				if(FilterHelper.test(teacher, searchString)) {
					return true;
				}
			}
		}
		
		return FilterHelper.test(row.getAbsentIdentity().getUser(), searchString, userPropertyHandlers, locale);
	}
}
