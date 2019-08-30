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
package org.olat.modules.lecture.ui.component;

import java.util.Comparator;
import java.util.Date;

import org.olat.modules.lecture.model.LectureBlockWithTeachers;

/**
 * 
 * Initial date: 29 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockWithTeachersComparator implements Comparator<LectureBlockWithTeachers> {

	@Override
	public int compare(LectureBlockWithTeachers o1, LectureBlockWithTeachers o2) {
		Date s1 = o1.getLectureBlock().getStartDate();
		Date s2 = o2.getLectureBlock().getStartDate();
		if(s1 == null && s2 == null) return 0;
		if(s1 == null) return 1;
		if(s2 == null) return -1;
		return s1.compareTo(s2);
	}
}
