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
package org.olat.repository.ui.author.copy.wizard;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;

/**
 * Initial date: 19.08.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CopyCourseOverviewDataModel extends DefaultFlexiTreeTableDataModel<CopyCourseOverviewRow> {

	public CopyCourseOverviewDataModel(FlexiTableColumnModel columnsModel) {
		super(columnsModel);
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		//
	}
	
	@Override
	public boolean hasChildren(int row) {
		CopyCourseOverviewRow viewRow = getObject(row);
		return viewRow.hasChildren();
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		CopyCourseOverviewRow reason = getObject(row);
		return getValueAt(reason, col);
	}

	public Object getValueAt(CopyCourseOverviewRow row, int col) {
		switch(CopyCourseOverviewCols.values()[col]) {
			case node: return row;
			case hints: return row.getEditorNode();
			case dirty: return Boolean.valueOf(row.getEditorNode().isDirty());
			case newNode: return Boolean.valueOf(row.getEditorNode().isNewnode());
			case deleted: return Boolean.valueOf(row.getEditorNode().isDeleted());
			case shortTitle: return row.getCourseNode().getShortTitle();
			case longTitle: return row.getCourseNode().getLongTitle();
			case description: return row.getCourseNode().getDescription();
			case display: return row.getTranslatedDisplayOption();
			case duration: return row.getDuration();
			case obligation: return row.getTranslatedObligation();
			case start: return row.getStart();
			case end: return row.getEnd();
			case trigger: return row.getTranslatedTrigger();
			case score: return row.getAssessmentConfig().isAssessable()
					? Boolean.valueOf(Mode.none != row.getAssessmentConfig().getScoreMode())
					: null;
			case scoreMin: return row.getAssessmentConfig().isAssessable() && Mode.setByNode == row.getAssessmentConfig().getScoreMode()
					? row.getAssessmentConfig().getMinScore()
					: null;
			case scoreMax: return row.getAssessmentConfig().isAssessable() && Mode.setByNode == row.getAssessmentConfig().getScoreMode()
					? row.getAssessmentConfig().getMaxScore()
					: null;
			case passed: return row.getAssessmentConfig().isAssessable()
					? Boolean.valueOf(Mode.none != row.getAssessmentConfig().getPassedMode())
					: null;
			case passesCut: return row.getAssessmentConfig().isAssessable() && row.getAssessmentConfig().getPassedMode() != Mode.none
					? row.getAssessmentConfig().getCutValue()
					: null;
			case ignoreInCourseAssessment: return row.getAssessmentConfig().isAssessable()
					? Boolean.valueOf(row.getAssessmentConfig().ignoreInCourseAssessment())
					: null;
			case comment: return row.getAssessmentConfig().isAssessable()
					? Boolean.valueOf(row.getAssessmentConfig().hasComment())
					: null;
			case individualAsssessmentDocuments: return row.getAssessmentConfig().isAssessable()
					? Boolean.valueOf(row.getAssessmentConfig().hasIndividualAsssessmentDocuments())
					: null;
			case startChooser: return row.getNewStartDateChooser();
			case endChooser: return row.getNewEndDateChooser();
			case obligationChooser: return row.getObligationChooser();
			case resourceChooser: return row.getResourceChooser();
			default: return null;
		}
	}
	
	public enum CopyCourseOverviewCols implements FlexiColumnDef {
		node("table.header.node"),
		hints("table.header.hints"),
		dirty("table.header.dirty"),
		newNode("table.header.new"),
		deleted("table.header.deleted"),
		shortTitle("table.header.short.title"),
		longTitle("table.header.long.title"),
		description("table.header.description"),
		display("table.header.display"),
		duration("table.header.duration"),
		obligation("table.header.obligation"),
		start("table.header.start"),
		end("table.header.end"),
		trigger("table.header.trigger"),
		score("table.header.score"),
		scoreMin("table.header.score.min"),
		scoreMax("table.header.score.max"),
		passed("table.header.passed"),
		passesCut("table.header.passed.cut"),
		ignoreInCourseAssessment("table.header.ignore.in.course.assessment"),
		comment("table.header.comment"),
		individualAsssessmentDocuments("table.header.individual.documents"),
		startChooser("table.header.start"),
		endChooser("table.header.end"),
		obligationChooser("table.header.obligation"),
		resourceChooser("table.header.resource");
		
		private final String i18nKey;
		
		private CopyCourseOverviewCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

	}
}
