package org.olat.user.ui.organisation;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;

/**
 * 
 * Initial date: 15 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationUserManagementTableModel extends DefaultFlexiTableDataModel<OrganisationUserRow>
implements SortableFlexiTableDataModel<OrganisationUserRow> {
	
	public OrganisationUserManagementTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public void sort(SortKey sortKey) {
		//
	}

	@Override
	public Object getValueAt(int row, int col) {
		OrganisationUserRow member = getObject(row);
		return getValueAt(member, col);
	}

	@Override
	public Object getValueAt(OrganisationUserRow row, int col) {
		if(col >= 0 && col < MemberCols.values().length) {
			switch(MemberCols.values()[col]) {
				case username: return row.getIdentityName();
				case role: return row.getRole();
				default : return "ERROR";
			}
		}
		
		int propPos = col - OrganisationUserManagementController.USER_PROPS_OFFSET;
		return row.getIdentityProp(propPos);
	}

	@Override
	public DefaultFlexiTableDataModel<OrganisationUserRow> createCopyWithEmptyList() {
		return new OrganisationUserManagementTableModel(getTableColumnModel());
	}
	
	public enum MemberCols implements FlexiSortableColumnDef {
		username("table.header.username"),
		role("table.header.role");
		
		private final String i18nKey;
		
		private MemberCols(String i18nKey) {
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
