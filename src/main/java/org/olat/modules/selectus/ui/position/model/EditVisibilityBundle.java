/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position.model;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

import org.olat.modules.selectus.ui.position.PositionEditProfileVisibilityController;
import org.olat.modules.selectus.ui.position.PositionEditProfileVisibilityController.VisibilityDataModel;

public class EditVisibilityBundle {
	
	private FlexiTableElement tableEl;
	private VisibilityDataModel tableModel;
	private SingleSelection visibilityEl;
	private FormLayoutContainer tableLayoutCont;
	
	private EditVisibilityBundle() {
		//
	}
	
	public static final EditVisibilityBundle valueOf(FlexiTableElement tableElement, VisibilityDataModel dataModel,
			SingleSelection visibilityElement, FormLayoutContainer layoutCont) {
		EditVisibilityBundle bundle = new EditVisibilityBundle();
		bundle.tableEl = tableElement;
		bundle.tableModel = dataModel;
		bundle.visibilityEl = visibilityElement;
		bundle.tableLayoutCont = layoutCont;
		visibilityElement.setUserObject(bundle);
		tableElement.setUserObject(bundle);
		
		String selectedKey = visibilityElement.getSelectedKey();
		boolean customize = PositionEditProfileVisibilityController.CUSTOMIZE.equals(selectedKey);
		tableElement.setVisible(customize);
		layoutCont.setVisible(customize);
		return bundle;
	}
	
	public SingleSelection visibilityEl() {
		return visibilityEl;
	}

	public FlexiTableElement tableEl() {
		return tableEl;
	}

	public VisibilityDataModel tableModel() {
		return tableModel;
	}
	
	public FlexiTableColumnModel columnsModel() {
		return tableModel.getTableColumnModel();
	}
	
	public FormLayoutContainer tableLayoutCont() {
		return tableLayoutCont;
	}
}
