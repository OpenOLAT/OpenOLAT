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

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.modules.todo.ui.ToDoTaskRow;
import org.olat.modules.todo.ui.ToDoTaskRowGrouping;

/**
 * 
 * Initial date: 17 Nov 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeToDoTaskRowGrouping implements ToDoTaskRowGrouping {
	
	private final Supplier<List<ToDoTaskRow>> groupRowsSupplier;
	private List<ToDoTaskRow> groupRows;
	private Map<String, ToDoTaskRow> toDoTypeToGroupRow;
	
	public CourseNodeToDoTaskRowGrouping(Supplier<List<ToDoTaskRow>> groupRowsSupplier) {
		this.groupRowsSupplier = groupRowsSupplier;
	}

	@Override
	public boolean isGrouping() {
		return true;
	}

	@Override
	public boolean isShowEmptyGroups() {
		return true;
	}

	@Override
	public List<ToDoTaskRow> group(List<ToDoTaskRow> rows, List<ToDoTaskRow> groupCandiatades, FlexiFiltersTab selectedTab, Locale locale) {
		groupRows = groupRowsSupplier.get();
		toDoTypeToGroupRow = groupRows.stream().collect(Collectors.toMap(ToDoTaskRow::getType, Function.identity()));
		
		for (ToDoTaskRow row : rows) {
			if (!row.isOriginDeleted()) {
				ToDoTaskRow groupRow = toDoTypeToGroupRow.get(row.getType());
				if (groupRow != null) {
					groupRow.addChild(row);
				}
			}
		}
		
		return groupRows;
	}

}
