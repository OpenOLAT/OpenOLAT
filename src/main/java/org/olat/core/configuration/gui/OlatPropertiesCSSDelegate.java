package org.olat.core.configuration.gui;

import org.olat.core.configuration.model.OlatPropertiesTableModel.OlatPropertiesTableColumn;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;

public class OlatPropertiesCSSDelegate implements FlexiTableCssDelegate {

	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		if (pos == OlatPropertiesTableColumn.defaultValue.ordinal()
				|| pos == OlatPropertiesTableColumn.overwriteValue.ordinal() 
				|| pos == OlatPropertiesTableColumn.systemProperty.ordinal()) {
			return "o_admin_property_table_column";
		}
		return null;
	}

}