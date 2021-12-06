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

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.id.User;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 19.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupAssessmentModel extends DefaultFlexiTableDataModel<AssessmentRow>
	implements SortableFlexiTableDataModel<AssessmentRow> {
	
	private final Locale locale;
	private final List<UserPropertyHandler> userPropertyHandlers;

	public GroupAssessmentModel(List<UserPropertyHandler> userPropertyHandlers, Locale locale,
			FlexiTableColumnModel columnModel) {
		super(columnModel);
		this.locale = locale;
		this.userPropertyHandlers = userPropertyHandlers;
	}
	
	@Override
	public void sort(SortKey sortKey) {
		if(sortKey != null) {
			List<AssessmentRow> views = new SortableFlexiTableModelDelegate<>(sortKey, this, null).sort();
			super.setObjects(views);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		AssessmentRow assessmentRow = getObject(row);
		return getValueAt(assessmentRow, col);
	}

	@Override
	public Object getValueAt(AssessmentRow row, int col) {
		if(col == Cols.scoreVal.ordinal()) {
			return row.getScore();
		} else if(col == Cols.passedVal.ordinal()) {
			return row.getPassed();
		} else if(col == Cols.assessmentDocsVal.ordinal()) {
			return row.getAssessmentDocsTooltipLink();
		} else if(col == Cols.commentVal.ordinal()) {
			return row.getCommentTooltipLink();
		} else if(col == Cols.scoreEl.ordinal()) {
			return row.getScoreEl();
		} else if(col == Cols.passedEl.ordinal()) {
			return row.getPassedEl();
		} else if(col == Cols.assessmentDocsEl.ordinal()) {
			return row.getAssessmentDocsEditLink();
		} else if(col == Cols.commentEl.ordinal()) {
			return row.getCommentEditLink();
		} else if(col >= GTACoachedGroupGradingController.USER_PROPS_OFFSET) {
			int propIndex = col - GTACoachedGroupGradingController.USER_PROPS_OFFSET;
			User user = row.getIdentity().getUser();
			return userPropertyHandlers.get(propIndex).getUserProperty(user, locale);
		}
		return row;
	}

	public enum Cols {
		passedVal("table.header.passed"),
		scoreVal("table.header.score"),
		assessmentDocsVal("table.header.assessment.docs"),
		commentVal("table.header.comment"),
		passedEl("table.header.passed"),
		scoreEl("table.header.score"),
		assessmentDocsEl("table.header.assessment.docs"),
		commentEl("table.header.comment");
		
		private final String i18nKey;
		
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}
