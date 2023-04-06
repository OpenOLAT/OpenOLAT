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
package org.olat.modules.todo.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.todo.ui.ToDoTaskDataModel.ToDoTaskCols;

/**
 * 
 * Initial date: 29 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoTaskRowSortDelegate extends SortableFlexiTableModelDelegate<ToDoTaskRow> {

	public ToDoTaskRowSortDelegate(SortKey orderBy, SortableFlexiTableDataModel<ToDoTaskRow> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}
	
	@Override
	protected void sort(List<ToDoTaskRow> rows) {
		int columnIndex = getColumnIndex();
		ToDoTaskCols column = ToDoTaskCols.values()[columnIndex];
		switch(column) {
			case title: Collections.sort(rows, new TitleComporator()); break;
			case expenditureOfWork: Collections.sort(rows, new ExpenditureOfWorkComporator()); break;
			case dueDate: Collections.sort(rows, new DueDateComporator()); break;
			case due: Collections.sort(rows, new DueComporator()); break;
			default: {
				super.sort(rows);
			}
		}
	}
	
	private class TitleComporator implements Comparator<ToDoTaskRow> {
		@Override
		public int compare(ToDoTaskRow r1, ToDoTaskRow r2) {
			return compareString(r1.getTitle(), r2.getTitle());
		}
	}
	
	private class ExpenditureOfWorkComporator implements Comparator<ToDoTaskRow> {
		@Override
		public int compare(ToDoTaskRow r1, ToDoTaskRow r2) {
			return compareLongs(r1.getExpenditureOfWork(), r2.getExpenditureOfWork());
		}
	}
	
	private class DueDateComporator implements Comparator<ToDoTaskRow> {
		@Override
		public int compare(ToDoTaskRow r1, ToDoTaskRow r2) {
			return compareDateAndTimestamps(r1.getDueDate(), r2.getDueDate(), false);
		}
	}
	
	private class DueComporator implements Comparator<ToDoTaskRow> {
		@Override
		public int compare(ToDoTaskRow r1, ToDoTaskRow r2) {
			Date dueDate1 = r1.isOverdue()? r1.getDueDate(): null;
			Date dueDate2 = r2.isOverdue()? r2.getDueDate(): null;
			return compareDateAndTimestamps(dueDate1, dueDate2, false);
		}
	}

}
