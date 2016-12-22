package org.olat.course.assessment.ui.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 21 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityAssessmentOverviewTableModel extends DefaultFlexiTableDataModel<AssessmentNodeData>
	implements SortableFlexiTableDataModel<AssessmentNodeData>, FilterableFlexiTableModel {

	private List<AssessmentNodeData> backups;
	
	public IdentityAssessmentOverviewTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public void filter(List<FlexiTableFilter> filters) {
		String key = filters == null || filters.isEmpty() || filters.get(0) == null ? null : filters.get(0).getFilter();
		if(StringHelper.containsNonWhitespace(key)) {
			List<AssessmentNodeData> filteredRows;
			if("passed".equals(key)) {
				filteredRows = backups.stream()
					.filter(node -> node.getPassed() != null && node.getPassed().booleanValue())
					.collect(Collectors.toList());
			} else if("failed".equals(key)) {
				filteredRows = backups.stream()
						.filter(node -> node.getPassed() != null && !node.getPassed().booleanValue())
						.collect(Collectors.toList());
			} else if(AssessmentEntryStatus.isValueOf(key)) {
				filteredRows = backups.stream()
						.filter(node -> node.getAssessmentStatus() != null && key.equals(node.getAssessmentStatus().name()))
						.collect(Collectors.toList());
			} else {
				filteredRows = new ArrayList<>(backups);
			}
			super.setObjects(filteredRows);
		} else {
			super.setObjects(backups);
		}
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			IdentityAssessmentOverviewSorter sorter = new IdentityAssessmentOverviewSorter(orderBy, this, null);
			List<AssessmentNodeData> views = sorter.sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		AssessmentNodeData data = getObject(row);
		return getValueAt(data, col);
	}

	@Override
	public Object getValueAt(AssessmentNodeData nodeData, int col) {
		switch (NodeCols.values()[col]) {
			case node: return nodeData;// rendered using the indentedNodeRenderer
			case details: return nodeData.getShortTitle();
			case attempts: return nodeData.getAttempts();
			case score: return nodeData.getScore();
			case passed: return nodeData.getPassed();
			case min: return nodeData.getMinScore();
			case max: return nodeData.getMaxScore();
			case status: return nodeData.getAssessmentStatus();
			case select: return nodeData.isSelectable();
			default: return "ERROR";
		}
	}

	@Override
	public void setObjects(List<AssessmentNodeData> objects) {
		super.setObjects(objects);
		backups = objects;
	}

	@Override
	public DefaultFlexiTableDataModel<AssessmentNodeData> createCopyWithEmptyList() {
		return new IdentityAssessmentOverviewTableModel(getTableColumnModel());
	}

	public enum NodeCols implements FlexiSortableColumnDef {
		
		node("table.header.node", true),
		details("table.header.details", true),
		attempts("table.header.attempts", true),
		score("table.header.score", true),
		min("table.header.min", true),
		max("table.header.max", true),
		status("table.header.status", true),
		passed("table.header.passed", true),
		select("table.action.select", false);
		
		private final String i18nKey;
		private final boolean sortable;
		
		private NodeCols(String i18nKey, boolean sortable) {
			this.i18nKey = i18nKey;
			this.sortable = sortable;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return sortable;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
