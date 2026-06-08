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
package org.olat.modules.selectus.ui.feedback.appsfeedback.wizard;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.feedback.appsfeedback.FeedbackHelper;
import org.olat.modules.selectus.ui.feedback.appsfeedback.PositionFeedbacksController;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 6 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OverviewMembersToContactDataModel extends DefaultFlexiTableDataModel<Identity>
implements SortableFlexiTableDataModel<Identity> {
	
	private static final OverviewFeedCols[] COLS = OverviewFeedCols.values();
	
	private final Locale locale;
	private final Translator translator;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	public OverviewMembersToContactDataModel(FlexiTableColumnModel columnsModel, List<UserPropertyHandler> userPropertyHandlers,
			Translator translator, Locale locale) {
		super(columnsModel);
		this.locale = locale;
		this.translator = translator;
		this.userPropertyHandlers = userPropertyHandlers;
	}

	@Override
	public void sort(SortKey sortKey) {
		//
	}

	@Override
	public Object getValueAt(int row, int col) {
		Identity identity = getObject(row);
		return getValueAt(identity, col);
	}

	@Override
	public Object getValueAt(Identity row, int col) {
		if(col >= 0 && col < COLS.length) {
			switch(COLS[col]) {
				case fullName: return RecruitingHelper.formatLastnameFirstName(row);
				case type: return getType();
				case email: return FeedbackHelper.getEmail(row);
				default: return "ERROR";
			}
		} else if(col >= PositionFeedbacksController.USER_PROP_OFFSET) {
			int propIndex = col - PositionFeedbacksController.USER_PROP_OFFSET;
			UserPropertyHandler prop = userPropertyHandlers.get(propIndex);
			return prop.getUserProperty(row.getUser(), locale);
		}
		return "ERROR";
	}
	
	private String getType() {
		return translator.translate("role.faculty.member");
	}
	
	public enum OverviewFeedCols implements FlexiSortableColumnDef {
		fullName("table.header.feedback.fullname"),
		type("table.header.reference.type"),
		email("table.header.reference.email");
		
		private final String i18nKey;
		
		private OverviewFeedCols(String i18nKey) {
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
