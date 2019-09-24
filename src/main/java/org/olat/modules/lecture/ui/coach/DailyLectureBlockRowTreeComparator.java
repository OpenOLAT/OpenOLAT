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

import java.util.Date;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeNodeComparator;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;

/**
 * 
 * Initial date: 29 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DailyLectureBlockRowTreeComparator extends FlexiTreeNodeComparator  {

	@Override
	protected int compareNodes(FlexiTreeTableNode o1, FlexiTreeTableNode o2) {
		if(o1 == null || o2 == null) {
			return compareNullObjects(o1, o2);
		}
		
		DailyLectureBlockRow d1 = (DailyLectureBlockRow)o1;
		DailyLectureBlockRow d2 = (DailyLectureBlockRow)o2;
		Date start1 = d1.isSeparator() ? d1.getSeparatorStartDate() : d1.getLectureStartDate();
		Date start2 = d2.isSeparator() ? d2.getSeparatorStartDate() : d2.getLectureStartDate();
		if(start1 == null || start2 == null) {
			return compareNullObjects(o1, o2);
		}
		return start1.compareTo(start2);
	}
}
