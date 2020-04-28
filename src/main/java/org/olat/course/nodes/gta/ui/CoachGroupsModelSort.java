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
package org.olat.course.nodes.gta.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.course.nodes.gta.TaskLight;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.ui.CoachGroupsTableModel.CGCols;

/**
 * 
 * Initial date: 30.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachGroupsModelSort extends SortableFlexiTableModelDelegate<CoachedGroupRow> {
	
	public CoachGroupsModelSort(SortKey orderBy, SortableFlexiTableDataModel<CoachedGroupRow> tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
	}

	@Override
	protected void sort(List<CoachedGroupRow> rows) {
		int columnIndex = getColumnIndex();
		CGCols column = CGCols.values()[columnIndex];
		switch(column) {
			case taskStatus: Collections.sort(rows, new TaskStatusComparator()); break;
			case taskName: Collections.sort(rows, new TaskNameComparator()); break;
			default: {
				super.sort(rows);
			}
		}
	}
	
	private class TaskNameComparator implements Comparator<CoachedGroupRow> {
		@Override
		public int compare(CoachedGroupRow o1, CoachedGroupRow o2) {
			String n1 = o1.getTaskName();
			String n2 = o2.getTaskName();
			return compareString(n1, n2);
		}
	}
	
	private static class TaskStatusComparator implements Comparator<CoachedGroupRow> {
		@Override
		public int compare(CoachedGroupRow o1, CoachedGroupRow o2) {
			TaskLight t1 = o1.getTask();
			TaskLight t2 = o2.getTask();
			
			if(t1 == null) {
				if(t2 == null) return 0;
				return -1;
			}
			if(t2 == null) return 1;
			
			TaskProcess s1 = t1.getTaskStatus();
			TaskProcess s2 = t2.getTaskStatus();
			
			if(s1 == null) {
				if(s2 == null) return 0;
				return -1;
			}
			if(s2 == null) return 1;
			
			return s1.ordinal() - s2.ordinal();
		}
	}
}
