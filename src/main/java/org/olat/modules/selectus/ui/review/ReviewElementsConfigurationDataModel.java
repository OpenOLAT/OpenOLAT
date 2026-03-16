/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.review;

import java.util.ArrayList;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

/**
 * 
 * Initial date: 4 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReviewElementsConfigurationDataModel extends DefaultFlexiTableDataModel<ElementDefinitionRow> {
	
	public ReviewElementsConfigurationDataModel(FlexiTableColumnModel columnsModel) {
		super(new ArrayList<>(), columnsModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		ElementDefinitionRow element = getObject(row);
		switch(ElementCols.values()[col]) {
			case label: return element.getLabelEl();
			case type: return element.getType();
			case up: return element.getUpLink();
			case down: return element.getDownLink();
			case delete: return element.getDeleteLink();
			default: return "ERROR";
		}
	}

	public enum ElementCols implements FlexiSortableColumnDef {
		label("table.header.label"),
		type("table.header.type"),
		up("table.header.up"),
		down("table.header.down"),
		delete("table.header.delete");

		private final String i18nKey;
		
		private ElementCols(String i18nKey) {
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
