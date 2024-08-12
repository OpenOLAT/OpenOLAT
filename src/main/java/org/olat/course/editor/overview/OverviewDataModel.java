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
package org.olat.course.editor.overview;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.translator.Translator;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.editor.overview.OverviewListController.Model;
import org.olat.course.nodes.CourseNodeHelper;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.STCourseNode;

/**
 * 
 * Initial date: 16 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OverviewDataModel extends DefaultFlexiTreeTableDataModel<OverviewRow> {
	
	private static final OverviewCols[] COLS = OverviewCols.values();
	private final Model usedModel;
	private final Translator translator;

	public OverviewDataModel(FlexiTableColumnModel columnsModel, Model usedModel, Translator translator) {
		super(columnsModel);
		this.usedModel = usedModel;
		this.translator = translator;
	}

	@Override
	public void filter(String searchString, List<FlexiTableFilter> filters) {
		//
	}
	
	@Override
	public boolean hasChildren(int row) {
		OverviewRow viewRow = getObject(row);
		return viewRow.hasChildren();
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		OverviewRow reason = getObject(row);
		return getValueAt(reason, col);
	}

	public Object getValueAt(OverviewRow row, int col) {
		switch(COLS[col]) {
			case node: return row;
			case hints: return row.getEditorNode();
			case dirty: return Boolean.valueOf(row.getEditorNode().isDirty());
			case newNode: return Boolean.valueOf(row.getEditorNode().isNewnode());
			case deleted: return Boolean.valueOf(row.getEditorNode().isDeleted());
			case shortTitle: return CourseNodeHelper.getCustomShortTitle(row.getCourseNode());
			case longTitle: return row.getCourseNode().getLongTitle();
			case description: return row.getCourseNode().getDescription();
			case objectives: return row.getCourseNode().getObjectives();
			case instruction: return row.getCourseNode().getInstruction();
			case instructionalDesign: return row.getCourseNode().getInstructionalDesign();
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
			case incorporateInCourseAssessment: return getIncorporateInCourseAssessment(row);
			case scoreScaling: return getScoreScaling(row);
			case comment: return row.getAssessmentConfig().isAssessable()
					? Boolean.valueOf(row.getAssessmentConfig().hasComment())
					: null;
			case individualAsssessmentDocuments: return row.getAssessmentConfig().isAssessable()
					? Boolean.valueOf(row.getAssessmentConfig().hasIndividualAsssessmentDocuments())
					: null;
			default: return null;
		}
	}
	
	public Object getIncorporateInCourseAssessment(OverviewRow row) {
		if(usedModel == Model.EDITOR) {
			return row.getIncorporateInCourseAssessmentEl();
		}
		return row.getAssessmentConfig().isAssessable() && !(row.getCourseNode() instanceof STCourseNode)
				? Boolean.valueOf(!row.getAssessmentConfig().ignoreInCourseAssessment())
				: null;
	}
	
	private Object getScoreScaling(OverviewRow row) {
		if(!row.getAssessmentConfig().isAssessable()
				|| row.getAssessmentConfig().ignoreInCourseAssessment()
				|| row.getCourseNode() instanceof STCourseNode) {
			return null;
		}
		
		if(usedModel == Model.EDITOR) {
			return row.getScoreScalingEl();
		}
		
		String scoreScaling = row.getCourseNode().getModuleConfiguration()
				.getStringValue(MSCourseNode.CONFIG_KEY_SCORE_SCALING, MSCourseNode.CONFIG_DEFAULT_SCORE_SCALING);
		if(scoreScaling.indexOf('/') >= 0) {
			return scoreScaling;
		}
		return translator.translate("score.scaling.decorator", scoreScaling);
	}
	
	public enum OverviewCols implements FlexiSortableColumnDef {
		node("table.header.node"),
		hints("table.header.hints"),
		dirty("table.header.dirty"),
		newNode("table.header.new"),
		deleted("table.header.deleted"),
		shortTitle("table.header.short.title"),
		longTitle("table.header.long.title"),
		description("table.header.description"),
		objectives("table.header.objectives"),
		instruction("table.header.instruction"),
		instructionalDesign("table.header.instructional.design"),
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
		incorporateInCourseAssessment("table.header.incorporate.in.course.assessment"),
		scoreScaling("table.header.score.scaling"),
		comment("table.header.comment"),
		individualAsssessmentDocuments("table.header.individual.documents");
		
		private final String i18nKey;
		
		private OverviewCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
