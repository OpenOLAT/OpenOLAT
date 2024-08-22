/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui.peerreview;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;

/**
 * 
 * Initial date: 6 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAParticipantPeerReviewTableModel extends DefaultFlexiTableDataModel<ParticipantPeerReviewAssignmentRow>
implements SortableFlexiTableDataModel<ParticipantPeerReviewAssignmentRow> {
	
	private static final ReviewCols[] COLS = ReviewCols.values();

	private final Locale locale;
	
	public GTAParticipantPeerReviewTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		this.locale = locale;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<ParticipantPeerReviewAssignmentRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(views);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		ParticipantPeerReviewAssignmentRow session = getObject(row);
		return getValueAt(session, col);
	}

	@Override
	public Object getValueAt(ParticipantPeerReviewAssignmentRow row, int col) {
		return switch(COLS[col]) {
			case assessedIdentity -> row.getAssessedIdentityName();
			case reviewerIdentity -> row.getReviewerName();
			case numOfDocuments -> row.getNumOfDocumentsToReview();
			case plot -> row.getBoxPlot();
			case ratingYesNo, ratingStars -> row.getRatingItem();
			case sessionStatus -> row;
			case executeReview -> row.getExecuteSessionLink();
			case viewReview -> row.getViewSessionLink();
			default -> "ERROR";
		};
	}
	
	
	public enum ReviewCols implements FlexiSortableColumnDef {
		assessedIdentity("table.header.reviewed.identity"),
		reviewerIdentity("table.header.reviewer.identity"),
		numOfDocuments("table.header.num.of.documents.to.review"),
		plot("table.header.review.plot"),
		ratingYesNo("table.header.review.rating.helpful"),
		ratingStars("table.header.review.rating.stars"),
		sessionStatus("table.header.review.status"),
		executeReview("table.header.review.execute"),
		viewReview("table.header.review.view"),
		;
		
		private final String i18nKey;
		
		private ReviewCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != plot && this != executeReview && this != viewReview;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
