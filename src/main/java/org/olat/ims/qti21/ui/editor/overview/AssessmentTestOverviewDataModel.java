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
package org.olat.ims.qti21.ui.editor.overview;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.util.Formatter;
import org.olat.ims.qti21.model.xml.AssessmentTestFactory;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.ControlObject;

/**
 * 
 * Initial date: 15 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestOverviewDataModel extends DefaultFlexiTableDataModel<ControlObjectRow> {
	
	public AssessmentTestOverviewDataModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		ControlObjectRow partRow = getObject(row);
		switch(PartCols.values()[col]) {
			case title: return partRow;
			case maxScore: return partRow.getEstimatedMaxScore();
			case attempts: return partRow.getAttemptOption();
			case skipping: return partRow.getSkipping();
			case comment: return partRow.getComment();
			case review: return partRow.getReview();
			case solution: return partRow.getSolution();
			case type: return partRow.getType();
			case identifier: return partRow.getIdentifier();
			case feedback: return partRow.getFeedbacks();
			case learningTime: return partRow.getLearningTime();
			case license: return shortenedLicense(partRow);
			case shuffled: return isShuffled(partRow);
			case correctionTime: return partRow.getCorrectionTime();
			default: return "ERROR";
		}
	}
	
	private Boolean isShuffled(ControlObjectRow partRow) {
		ControlObject<?> obj = partRow.getControlObject();
		if(obj instanceof AssessmentSection) {
			AssessmentSection section = (AssessmentSection)obj;
			return AssessmentTestFactory.shuffledSections(section.getParent());
		}
		if(obj instanceof AssessmentItemRef) {
			AssessmentItemRef itemRef = (AssessmentItemRef)obj;
			AssessmentSection section = itemRef.getParentSection();
			return section != null && section.getOrdering() != null && section.getOrdering().getShuffle();
		}
		return null;
	}
	
	private String shortenedLicense(ControlObjectRow partRow) {
		String text = partRow.getLicense();
		if(text != null && text.length() > 32) {
			text = Formatter.truncate(text, 32);
		}
		return text;
	}
	
	public enum PartCols implements FlexiSortableColumnDef {
		
		title("table.header.title"),
		maxScore("table.header.points"),
		attempts("table.header.attempts"),
		skipping("table.header.skipping"),
		comment("table.header.comment"),
		review("table.header.review"),
		solution("table.header.solution"),
		type("table.header.type"),
		identifier("table.header.identifier"),
		feedback("table.header.feedback"),
		learningTime("table.header.learning.time"),
		correctionTime("table.header.correction.time"),
		license("table.header.license"),
		shuffled("table.header.shuffled");
		
		private final String i18nKey;
		
		private PartCols(String i18nKey) {
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
