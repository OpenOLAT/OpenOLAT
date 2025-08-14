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
package org.olat.course.assessment.ui.tool;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.run.scoring.ScoreScalingHelper;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 21 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityAssessmentOverviewTableModel extends DefaultFlexiTableDataModel<AssessmentNodeData>
	implements SortableFlexiTableDataModel<AssessmentNodeData>, FilterableFlexiTableModel {
	
	private static final NodeCols[] COLS = NodeCols.values();

	private final Locale locale;
	private final Translator translator;
	private boolean allGradesNummeric;
	private List<AssessmentNodeData> backups;
	
	public IdentityAssessmentOverviewTableModel(FlexiTableColumnModel columnModel, Translator translator, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.translator = translator;
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
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
			IdentityAssessmentOverviewSorter sorter = new IdentityAssessmentOverviewSorter(orderBy, this, locale);
			sorter.setAllGradesNummeric(allGradesNummeric);
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
		switch (COLS[col]) {
			case node: return nodeData;// rendered using the indentedNodeRenderer
			case details: return nodeData.getShortTitle();
			case attempts: return nodeData.getAttempts();
			case userVisibility: return nodeData;
			case score: return nodeData.getScoreDesc() != null? nodeData.getScoreDesc(): nodeData.getRoundedScore();
			case weightedScore: return nodeData.getWeightedScoreDesc() != null? nodeData.getWeightedScoreDesc(): nodeData.getRoundedWeightedScore();
			case grade: return nodeData;
			case passedOverriden: return nodeData.getPassedOverridable() == null
					? Boolean.FALSE
					: Boolean.valueOf(nodeData.getPassedOverridable().isOverridden());
			case passed: return nodeData;
			case minMax: return nodeData;
			case weightedMinMax: return nodeData;
			case scoreScale: return getScoreScale(nodeData);
			case status: return nodeData;
			case numOfAssessmentDocs: return getNumberOfAssessmentDocs(nodeData);
			case select: return nodeData.isSelectable();
			case lastModified: return nodeData.getLastModified();
			case lastUserModified: return nodeData.getLastUserModified();
			case lastCoachModified: return nodeData.getLastCoachModified();
			default: return "ERROR";
		}
	}
	
	private Integer getNumberOfAssessmentDocs(AssessmentNodeData nodeData) {
		if(nodeData.getNumOfAssessmentDocs() <= 0) {
			return null;
		}
		return nodeData.getNumOfAssessmentDocs();
	}
	
	private String getScoreScale(AssessmentNodeData nodeData) {
		if(StringHelper.containsNonWhitespace(nodeData.getScoreScaleConfig())) {
			String config = nodeData.getScoreScaleConfig();
			if(ScoreScalingHelper.isFractionScale(config)) {
				return config;
			}
			BigDecimal scale = ScoreScalingHelper.getScoreScale(config);
			if(scale != null) {
				String val = AssessmentHelper.getRoundedScore(scale);
				return translator.translate("weighted", val);
			}
		}
		BigDecimal scale = nodeData.getDecimalScoreScale();
		if(scale != null) {
			String val = AssessmentHelper.getRoundedScore(scale);
			return translator.translate("weighted", val);
		}
		return null;
	}

	@Override
	public void setObjects(List<AssessmentNodeData> objects) {
		allGradesNummeric = objects.stream().allMatch(this::isNumeric);
		super.setObjects(objects);
		backups = objects;
	}

	private boolean isNumeric(AssessmentNodeData assessmentnodedata) {
		if (assessmentnodedata.getGrade() == null) {
			return true;
		}
		if (StringHelper.containsNonWhitespace(assessmentnodedata.getPerformanceClassIdent())) {
			return false;
		}
		try {
			BigDecimal grade = new BigDecimal(assessmentnodedata.getGrade());
			return grade != null;
		} catch (Exception e) {
			return false;
		}
	}

	public enum NodeCols implements FlexiSortableColumnDef {
		
		node("table.header.node", true),
		details("table.header.details", true),
		attempts("table.header.attempts", true),
		userVisibility("table.header.userVisibility", true),
		score("table.header.score", true),
		weightedScore("table.header.weighted.score", true),
		minMax("table.header.min.max", true),
		weightedMinMax("table.header.weighted.min.max", true),
		scoreScale("table.header.score.scale", true),
		grade("table.header.grade", true),
		status("table.header.status", true),
		passedOverriden("table.header.passed.overriden", true),
		passed("table.header.passed", true),
		select("table.action.select", false),
		numOfAssessmentDocs("table.header.num.assessmentDocs", true),
		lastModified("table.header.lastScoreDate", true),
		lastUserModified("table.header.lastUserModificationDate", true),
		lastCoachModified("table.header.lastCoachModificationDate", true);
		
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
