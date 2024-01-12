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
package org.olat.course.todo.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.course.todo.manager.CourseCollectionElementToDoTaskProvider;
import org.olat.course.todo.manager.CourseCollectionToDoTaskProvider;
import org.olat.course.todo.manager.CourseIndividualToDoTaskProvider;
import org.olat.modules.todo.ui.ToDoTaskRow;
import org.olat.modules.todo.ui.ToDoTaskRowGrouping;

/**
 * 
 * Initial date: 4 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseToDoTaskRowGrouping implements ToDoTaskRowGrouping {
	
	private final boolean showEmptyGroups;

	public CourseToDoTaskRowGrouping(boolean showEmptyGroups) {
		this.showEmptyGroups = showEmptyGroups;
	}

	@Override
	public boolean isGrouping() {
		return true;
	}

	@Override
	public boolean isShowEmptyGroups() {
		return showEmptyGroups;
	}

	@Override
	public List<ToDoTaskRow> group(List<ToDoTaskRow> rows, Locale locale) {
		List<ToDoTaskRow> groupRows = new ArrayList<>();
		Map<Long, ToDoTaskRow> collectionKeyToGroupRow = new HashMap<>();
		
		for (ToDoTaskRow row : rows) {
			if (CourseIndividualToDoTaskProvider.TYPE.equals(row.getType())) {
				groupRows.add(row);
			} else if (CourseCollectionToDoTaskProvider.TYPE.equals(row.getType())) {
				groupRows.add(row);
				row.setGroup(true);
				row.clearChildren();
				collectionKeyToGroupRow.put(row.getKey(), row);
			}
		}
		
		for (ToDoTaskRow row : rows) {
			if (CourseCollectionElementToDoTaskProvider.TYPE.equals(row.getType())) {
				ToDoTaskRow collectionRow = collectionKeyToGroupRow.get(row.getCollectionKey());
				if (collectionRow != null) {
					collectionRow.addChild(row);
				}
			}
		}
		
		return groupRows;
	}

}
