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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.course.todo.manager.CourseCollectionElementToDoTaskProvider;
import org.olat.course.todo.manager.CourseIndividualToDoTaskProvider;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ui.ToDoTaskListController;
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
	public List<ToDoTaskRow> group(List<ToDoTaskRow> rows, List<ToDoTaskRow> groupCandiatades, FlexiFiltersTab selectedTab, Locale locale) {
		Map<Long, ToDoTaskRow> candidateKeyToRow = groupCandiatades.stream().collect(Collectors.toMap(ToDoTaskRow::getKey, Function.identity()));
		
		List<ToDoTaskRow> groupRows = new ArrayList<>();
		Map<Long, ToDoTaskRow> collectionKeyToGroupRow = new HashMap<>();
		
		for (ToDoTaskRow row : rows) {
			if (CourseIndividualToDoTaskProvider.TYPE.equals(row.getType())) {
				groupRows.add(row);
			} else if (CourseCollectionElementToDoTaskProvider.TYPE.equals(row.getType())) {
				ToDoTaskRow collectionRow = collectionKeyToGroupRow.get(row.getCollectionKey());
				if (collectionRow == null) {
					collectionRow = candidateKeyToRow.get(row.getCollectionKey());
					if (collectionRow != null) {
						groupRows.add(collectionRow);
						collectionRow.setGroup(true);
						collectionRow.clearChildren();
						collectionKeyToGroupRow.put(row.getCollectionKey(), collectionRow);
					}
				}
				if (collectionRow != null) {
					collectionRow.addChild(row);
				}
			}
		}
		
		// Add empty collections at the end for the sake of completeness.
		if (showEmptyGroups && selectedTab != null) {
			for (ToDoTaskRow toDoTaskRow : groupCandiatades) {
				if (!collectionKeyToGroupRow.containsKey(toDoTaskRow.getKey())) {
					if (ToDoTaskListController.TAB_ID_ALL.equals(selectedTab.getId())) {
						if (ToDoStatus.deleted != toDoTaskRow.getStatus()) {
							groupRows.add(toDoTaskRow);
						}
					} else if (ToDoTaskListController.TAB_ID_DELETED.equals(selectedTab.getId())) {
						if (ToDoStatus.deleted == toDoTaskRow.getStatus()) {
							groupRows.add(toDoTaskRow);
						}
					}
				}
			}
		}
		
		return groupRows;
	}

}
