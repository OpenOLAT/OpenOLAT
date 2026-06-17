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
