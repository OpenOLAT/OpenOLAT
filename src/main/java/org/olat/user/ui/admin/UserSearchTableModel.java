package org.olat.user.ui.admin;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataSourceModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.modules.lecture.ui.TeacherRollCallController;
import org.olat.user.UserPropertiesRow;

/**
 * 
 * Initial date: 21 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSearchTableModel extends DefaultFlexiTableDataSourceModel<UserPropertiesRow> {
	
	public UserSearchTableModel(FlexiTableDataSourceDelegate<UserPropertiesRow> source, FlexiTableColumnModel columnModel) {
		super(source, columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		UserPropertiesRow userRow = getObject(row);
		if(col < UserSearchTableController.USER_PROPS_OFFSET) {
			switch(UserCols.values()[col]) {
				case id: return userRow.getIdentityKey();
				case username: return userRow.getIdentityName();
				default: return null;
			}
		} else if(col < TeacherRollCallController.CHECKBOX_OFFSET) {
			int propPos = col - TeacherRollCallController.USER_PROPS_OFFSET;
			return userRow.getIdentityProp(propPos);
		}
		return null;
	}
	
	@Override
	public DefaultFlexiTableDataSourceModel<UserPropertiesRow> createCopyWithEmptyList() {
		return new UserSearchTableModel(null, getTableColumnModel());
	}
	
	public enum UserCols implements FlexiSortableColumnDef {
		id("table.identity.id"),
		username("table.identity.name"),
		action("table.header.action");
		
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
			return !this.equals(action);
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
