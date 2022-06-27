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
package org.olat.course.nodes.practice.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.practice.PracticeAssessmentItemGlobalRef;
import org.olat.course.nodes.practice.manager.SearchPracticeItemHelper;

/**
 * 
 * Initial date: 12 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeComposeTableModel extends DefaultFlexiTableDataModel<PracticeComposeItemRow>
implements SortableFlexiTableDataModel<PracticeComposeItemRow> {
	
	private static final ComposeCols[] COLS = ComposeCols.values();
	
	private final Locale locale;
	private final int numOfLevels;
	private List<PracticeComposeItemRow> backupRows;
	
	public PracticeComposeTableModel(FlexiTableColumnModel columnsModel, int numOfLevels, Locale locale) {
		super(columnsModel);
		this.locale = locale;
		this.numOfLevels = numOfLevels;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<PracticeComposeItemRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}
	
	public void filter(String searchString, boolean notAnswered, List<String> taxonomyLevelsKeyPaths, boolean includeWithoutTaxonomy,
			List<Long> levels, Double correctFrom, Double correctTo) {
		if(StringHelper.containsNonWhitespace(searchString) || notAnswered
				|| (levels != null && !levels.isEmpty())
				|| (taxonomyLevelsKeyPaths != null && !taxonomyLevelsKeyPaths.isEmpty())
				|| includeWithoutTaxonomy
				|| (correctFrom != null && correctTo != null)) {
			if(searchString != null) {
				searchString = searchString.toLowerCase();
			}
			List<PracticeComposeItemRow> filteredRows = new ArrayList<>(backupRows.size());
			for(PracticeComposeItemRow backupRow:backupRows) {
				if(accept(searchString, notAnswered, taxonomyLevelsKeyPaths, includeWithoutTaxonomy, levels, correctFrom, correctTo, backupRow)) {
					filteredRows.add(backupRow);
				}
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backupRows);
		}
	}
	
	private boolean accept(String searchString, boolean notAnswered, List<String> taxonomyLevelsKeyPaths, boolean includeWithoutTaxonomy,
			List<Long> levels, Double correctFrom, Double correctTo, PracticeComposeItemRow row) {

		final String displayName = row.getItem().getDisplayName();
		if(notAnswered && row.isAnswered()) {
			return false;
		}
		
		if(!SearchPracticeItemHelper.accept(row.getItem(), taxonomyLevelsKeyPaths, false, includeWithoutTaxonomy)) {
			return false;
		}
		
		if(levels != null && !levels.isEmpty()) {
			Long level = row.getLevel();
			if(!levels.contains(level)) {
				return false;
			}
		}
		
		if(correctFrom != null && correctTo != null) {
			double correct = row.getCorrect();
			if(correct < correctFrom.doubleValue() || correct > correctTo.doubleValue()) {
				return false;
			}
		}
		
		if(StringHelper.containsNonWhitespace(searchString)) {
			if(displayName == null || !displayName.toLowerCase().contains(searchString)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Object getValueAt(int row, int col) {
		PracticeComposeItemRow itemRow = getObject(row);
		return getValueAt(itemRow, col);
	}

	@Override
	public Object getValueAt(PracticeComposeItemRow row, int col) {
		switch(COLS[col]) {
			case question: return row.getItem().getDisplayName();
			case level: return getLevels(row.getItemGlobalRef());
			case correct: return getCorrectness(row.getItemGlobalRef());
			case lastAttempts: return getLastAttempts(row.getItemGlobalRef());
			default: return "ERROR";
		}
	}
	
	private Integer getLevels(PracticeAssessmentItemGlobalRef globalRef) {
		if(globalRef == null || globalRef.getLevel() < 0) {
			return null;
		}
		if(globalRef.getLevel() > numOfLevels) {
			return Integer.valueOf(numOfLevels);
		}
		return Integer.valueOf(globalRef.getLevel());
	}
	
	private Date getLastAttempts(PracticeAssessmentItemGlobalRef globalRef) {
		if(globalRef == null) {
			return null;
		}
		return globalRef.getLastAttempts();
	}
	
	private Double getCorrectness(PracticeAssessmentItemGlobalRef globalRef) {
		if(globalRef == null || globalRef.getAttempts() <= 0) {
			return null;
		}
		
		int correct = globalRef.getCorrectAnswers();
		return Double.valueOf(correct / (double)globalRef.getAttempts());
	}
	
	@Override
	public void setObjects(List<PracticeComposeItemRow> objects) {
		backupRows = new ArrayList<>(objects);
		super.setObjects(objects);
	}

	public enum ComposeCols implements FlexiSortableColumnDef {
		question("table.header.item"),
		level("table.header.level"),
		correct("table.header.correct.percent"),
		lastAttempts("table.header.last.attempts");
		
		private final String i18nKey;
		
		private ComposeCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
