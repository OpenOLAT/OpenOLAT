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
package org.olat.modules.coach.ui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.modules.assessment.ui.component.LearningProgressCompletionCellRenderer;
import org.olat.modules.assessment.ui.component.LearningProgressCompletionCellRenderer.CompletionPassed;
import org.olat.modules.coach.model.EfficiencyStatementEntry;

/**
 * 
 * Initial date: 26 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EfficiencyStatementEntrySortDelegate extends SortableFlexiTableModelDelegate<EfficiencyStatementEntry> {

	private EfficiencyStatementEntryTableDataModel tableModel;

	public EfficiencyStatementEntrySortDelegate(SortKey orderBy,
			EfficiencyStatementEntryTableDataModel tableModel, Locale locale) {
		super(orderBy, tableModel, locale);
		this.tableModel = tableModel;
	}
	
	@Override
	protected void sort(List<EfficiencyStatementEntry> rows) {
		int columnIndex = getColumnIndex();
		EfficiencyStatementEntryTableDataModel.Columns column = EfficiencyStatementEntryTableDataModel.COLS[columnIndex];
		switch(column) {
			case completion: Collections.sort(rows, new CompletionPassedComparator()); break;
			default: super.sort(rows); break;
		}
	}
	
	private final class CompletionPassedComparator implements Comparator<EfficiencyStatementEntry> {
		
		private final Comparator<CompletionPassed> completionPassedComparator;
		
		public CompletionPassedComparator() {
			completionPassedComparator = LearningProgressCompletionCellRenderer.createComparator();
		}
		
		@Override
		public int compare(EfficiencyStatementEntry o1, EfficiencyStatementEntry o2) {
			return completionPassedComparator.compare(tableModel.createCompletionPassed(o1), tableModel.createCompletionPassed(o2));
		}
	}

}
