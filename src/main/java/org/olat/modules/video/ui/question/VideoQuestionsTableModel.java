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
package org.olat.modules.video.ui.question;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.modules.video.VideoQuestion;

/**
 * 
 * Initial date: 4 déc. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoQuestionsTableModel extends DefaultFlexiTableDataModel<VideoQuestion> {
	
	public VideoQuestionsTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		VideoQuestion question = getObject(row);
		switch(QuestionCols.values()[col]) {
			case start: return question.getBegin();
			case type: return QTI21QuestionType.safeValueOf(question.getType());
			case title: return question.getTitle();
			case style: return question.getStyle();
			case score: return question.getMaxScore();
			default: return "ERROR";
		}
	}
	
	public enum QuestionCols implements FlexiSortableColumnDef {
		start("marker.table.header.start"),
		type("question.table.header.type"),
		title("question.table.header.title"),
		style("marker.table.header.style"),
		score("question.table.header.score");
		
		private final String i18nKey;
		
		private QuestionCols(String i18nKey) {
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
