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
package org.olat.course.assessment.ui.inspection;

import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.id.Identity;
import org.olat.core.util.session.UserSessionManager;
import org.olat.instantMessaging.model.Presence;

/**
 * 
 * Initial date: 18 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionOverviewListModel extends DefaultFlexiTableDataModel<AssessmentInspectionRow>
implements SortableFlexiTableDataModel<AssessmentInspectionRow> {
	
	private static final OverviewCols[] COLS = OverviewCols.values();

	private final Locale locale;
	private final Identity identity;
	private UserSessionManager sessionManager;
	
	public AssessmentInspectionOverviewListModel(FlexiTableColumnModel columnsModel, Identity identity,
			UserSessionManager sessionManager, Locale locale) {
		super(columnsModel);
		this.locale = locale;
		this.identity = identity;
		this.sessionManager = sessionManager;
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<AssessmentInspectionRow> rows = new AssessmentInspectionOverviewListModelSortDelegate(orderBy, this, locale).sort();
			super.setObjects(rows);
		}
	}

	@Override
	public Object getValueAt(int row, int col) {
		AssessmentInspectionRow inspectionRow = getObject(row);
		return getValueAt(inspectionRow, col);
	}

	@Override
	public Object getValueAt(AssessmentInspectionRow row, int col) {
		switch(COLS[col]) {
			case portrait: return row.getAssessedIdentity();
			case onlineStatus: return getOnlineStatus(row);
			case participant: return row.getFullName();
			case courseNode: return row;
			case assessmentStatus: return row.getAssessmentStatus();
			case inspectionPeriod: return row;
			case inspectionDuration: return getDuration(row);
			case inspectionStatus: return row;
			case comment: return row.getComment();
			case effectiveDuration: return row.getEffectiveDuration();
			case accessCode: return row.getAccessCode();
			case configuration: return row.getConfiguration().getName();
			case cancel: return row.getCancelButton();
			case tools: return row.getToolsButton();
			default: return "ERROR";
		}
	}
	
	private String getOnlineStatus(AssessmentInspectionRow row) {
		Identity assessedIdentity = row.getAssessedIdentity();
		if(identity.equals(assessedIdentity)) {
			return "me";
		}
		if(sessionManager.isOnline(assessedIdentity.getKey())) {
			return Presence.available.name();
		}
		return Presence.unavailable.name();
	}
	
	private Integer getDuration(AssessmentInspectionRow row) {
		Integer configurationDuration = row.getInspectionDuration();
		Integer extraTime = row.getInspection().getExtraTime();
		int totalDuration = (configurationDuration == null ? 0 : configurationDuration.intValue())
				+ (extraTime == null ? 0 : extraTime.intValue());
		return Integer.valueOf(totalDuration / 60);
	}
	
	public enum OverviewCols implements FlexiSortableColumnDef {
		portrait("table.header.portrait"),
		onlineStatus("table.header.online.status"),
		participant("table.header.participant"),
		courseNode("table.header.course.node"),
		assessmentStatus("table.header.assessmentStatus"),
		inspectionPeriod("table.header.inspection.period"),
		inspectionDuration("table.header.duration"),
		inspectionStatus("table.header.inspection.status"),
		comment("table.header.comment"),
		effectiveDuration("table.header.duration.effective"),
		accessCode("table.header.access.code"),
		configuration("table.header.configuration"),
		cancel("table.header.cancel"),
		tools("table.header.tools");
		
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
			return this != cancel && this != tools && this != portrait;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
