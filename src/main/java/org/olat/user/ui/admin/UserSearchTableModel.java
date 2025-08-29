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
package org.olat.user.ui.admin;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityPropertiesRow;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataSourceModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExportableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExportableFlexiTableDataModelDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.DateUtils;
import org.olat.modules.lecture.ui.TeacherRollCallController;
import org.olat.user.UserLifecycleManager;
import org.olat.user.UserModule;
import org.olat.user.ui.identity.UserXlsFlexiTableExporter;

/**
 * 
 * Initial date: 21 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSearchTableModel extends DefaultFlexiTableDataSourceModel<IdentityPropertiesRow> implements ExportableFlexiTableDataModel {
	
	private static final UserCols[] COLS = UserCols.values();
	
	private final Date now;
	private final UserModule userModule;
	private final UserLifecycleManager lifecycleManager;
	private final Set<Long> guestsKeys;
	
	public UserSearchTableModel(FlexiTableDataSourceDelegate<IdentityPropertiesRow> source, Collection<Long> guestsKeys,
			FlexiTableColumnModel columnModel, UserModule userModule, UserLifecycleManager lifecycleManager) {
		super(source, columnModel);
		this.userModule = userModule;
		this.guestsKeys = new HashSet<>(guestsKeys);
		this.lifecycleManager = lifecycleManager;
		now = CalendarUtils.startOfDay(new Date());
	}
	
	public IdentityPropertiesRow getObject(IdentityRef identity) {
		List<IdentityPropertiesRow> rows = getObjects();
		if(identity != null && rows != null) {
			final Long identityKey = identity.getKey();
			return rows.stream()
					.filter(Objects::nonNull)
					.filter(row -> row.getIdentityKey().equals(identityKey))
					.findFirst()
					.orElse(null);
		}
		return null;
	}
	
	public boolean ensureLoaded(int row, FlexiTableElement tableEl) {
		if(isRowLoaded(row) ) {
			return true;
		}
		tableEl.preloadPageOfObjectIndex(row);
		return isRowLoaded(row);
	}
	
	@Override
	public MediaResource export(FlexiTableComponent ftC) {
		List<FlexiColumnModel> columns = ExportableFlexiTableDataModelDelegate.getColumnModels(ftC.getFormItem());
		return new UserXlsFlexiTableExporter().export(ftC, columns, ftC.getTranslator());
	}

	@Override
	public Object getValueAt(int row, int col) {
		IdentityPropertiesRow userRow = getObject(row);
		if(col < UserSearchTableController.USER_PROPS_OFFSET) {
			switch(COLS[col]) {
				case id: return userRow.getIdentityKey();
				case creationDate: return userRow.getCreationDate();
				case lastLogin: return getLastLogin(userRow);
				case status: return userRow.getStatus();
				case expirationDate: return userRow.getExpirationDate();
				case inactivationDate: return userRow.getInactivationDate();
				case daysToInactivation: return getDaysToInactivation(userRow);
				case daysToDeletion: return getDaysToDeletion(userRow);
				case organisations: return userRow.getOrganisations();
				default: return null;
			}
		} else if(col < TeacherRollCallController.CHECKBOX_OFFSET) {
			int propPos = col - TeacherRollCallController.USER_PROPS_OFFSET;
			return userRow.getIdentityProp(propPos);
		}
		return null;
	}

	private Date getLastLogin(IdentityPropertiesRow userRow) {
		if(guestsKeys.contains(userRow.getIdentityKey())) {
			return null;
		}
		return userRow.getLastLogin();
	}

	private Long getDaysToInactivation(IdentityPropertiesRow userRow) {
		Date date = userRow.getPlannedInactivationDate();
		if(date != null
				&& !guestsKeys.contains(userRow.getIdentityKey())
				&& userModule.isUserAutomaticDeactivation()) {
			long days = DateUtils.countDays(now, date);
			if(days <= 0) {
				days = 1;
			}
			return days;
		}
		return null;
	}
	
	private Long getDaysToDeletion(IdentityPropertiesRow userRow) {
		Date date = userRow.getPlannedDeletionDate();
		if(date != null && !guestsKeys.contains(userRow.getIdentityKey())) {
			return DateUtils.countDays(now, date);
		}
		return null;
	}
	
	@Override
	public DefaultFlexiTableDataSourceModel<IdentityPropertiesRow> createCopyWithEmptyList() {
		return new UserSearchTableModel(null, guestsKeys, getTableColumnModel(), userModule, lifecycleManager);
	}
	
	public enum UserCols implements FlexiSortableColumnDef {
		id("table.identity.id"),
		creationDate("table.identity.creationdate"),
		lastLogin("table.identity.lastlogin"),
		action("table.header.action"),
		status("table.identity.status"),
		inactivationDate("table.identity.inactivation.date"),
		daysToInactivation("table.identity.days.inactivation"),
		daysToDeletion("table.identity.days.deletion"),
		expirationDate("table.identity.expiration.date"),
		organisations("table.identity.organisations");
		
		private final String i18nKey;
		
		private UserCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return !this.equals(action) && !this.equals(organisations);
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
