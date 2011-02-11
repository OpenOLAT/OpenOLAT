package org.olat.ldap.ui;

import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;

public class IdentityFlexiTableModel  extends DefaultTableDataModel {
		private int columnCount = 0;
		
		public IdentityFlexiTableModel(List objects, int columnCount){
			super(objects);
			this.columnCount = columnCount;
		}
		
		public int getColumnCount() {
			return columnCount;
		}


		public Object getValueAt(int row, int col) {
			List entry = (List)objects.get(row);
			Object value = entry.get(col);
			return (value == null ? "n/a" : value);
		}
	}


