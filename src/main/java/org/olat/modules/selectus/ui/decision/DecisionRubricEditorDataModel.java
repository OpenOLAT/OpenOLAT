/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.decision;

import java.util.ArrayList;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

import org.olat.modules.selectus.ui.decision.DecisionRubricEditorController.RubricDefinitionRow;

/**
 * 
 * Initial date: 17 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DecisionRubricEditorDataModel extends DefaultFlexiTableDataModel<RubricDefinitionRow> {
	
	public DecisionRubricEditorDataModel(FlexiTableColumnModel columnsModel) {
		super(new ArrayList<>(), columnsModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		RubricDefinitionRow rubric = getObject(row);
		switch(RubricDefCols.values()[col]) {
			case name: return rubric.getNameEl();
			case type: return rubric.getTypeEl();
			case sum: return rubric.getSumEl();
			case weight: return rubric.getWeightEl();
			case delete: return rubric.getRemoveButton();
			case up: return rubric.getUpButton();
			case down: return rubric.getDownButton();
			default: return "ERROR";
		}
	}
	
	public enum RubricDefCols implements FlexiSortableColumnDef {
		name("rubric.displayname"),
		type("rubric.type"),
		sum("rubric.sum"),
		weight("rubric.weight"),
		up("table.header.rubric.up"),
		down("table.header.rubric.down"),
		delete("table.header.rubric.delete");
		
		private final String i18nKey;
		
		private RubricDefCols(String i18nKey) {
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
