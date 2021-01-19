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
package org.olat.ims.qti.export;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.model.AssessmentNodeData;

/**
 * 
 * Select a course element. Implemented to work standalone or
 * wrapped in a step form controller.
 * 
 * Initial date: 20.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectTestOrSurveyController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private NodeTableDataModel tableModel;
	
	private final QTIArchiver archiver;
	private final List<AssessmentNodeData> nodes;
	
	public SelectTestOrSurveyController(UserRequest ureq, WindowControl wControl, QTIArchiver archiver, List<AssessmentNodeData> nodes) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.nodes = nodes;
		this.archiver = archiver;
		initForm(ureq);
	}
	
	public SelectTestOrSurveyController(UserRequest ureq, WindowControl wControl, QTIArchiver archiver, List<AssessmentNodeData> nodes, Form rootForm) {
		super(ureq, wControl, LAYOUT_BAREBONE, null, rootForm);
		this.nodes = nodes;
		this.archiver = archiver;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.node, new IndentedNodeRenderer()));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.action.select",
				Cols.select.ordinal(), "select",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.action.select"), "select"), null)));
		tableModel = new NodeTableDataModel(nodes, tableColumnModel);

		tableEl = uifactory.addTableElement(getWindowControl(), "nodeList", tableModel, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(archiver == null || !archiver.hasResults()) {
			showWarning("archive.noresults");
			allOk &= false;
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				AssessmentNodeData data = tableModel.getObject(se.getIndex());
				archiver.setData(data);
				if(validateFormLogic(ureq)) {
					fireEvent(ureq, new SelectTestOrSurveyEvent(archiver, data));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private enum Cols implements FlexiSortableColumnDef {
		node("table.header.node"),
		select("table.action.select");
		
		private final String i18nKey;
		
		private Cols(String i18nKey) {
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
	
	private static class NodeTableDataModel extends DefaultFlexiTableDataModel<AssessmentNodeData>  {

		public NodeTableDataModel(List<AssessmentNodeData> nodes, FlexiTableColumnModel columnModel) {
			super(nodes, columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			AssessmentNodeData node = getObject(row);
			switch(Cols.values()[col]) {
				case node: return node;
				case select: return node.isSelectable() ? Boolean.TRUE : Boolean.FALSE;
				default: return null;
			}
		}

		@Override
		public DefaultFlexiTableDataModel<AssessmentNodeData> createCopyWithEmptyList() {
			return new NodeTableDataModel(new ArrayList<>(), getTableColumnModel());
		}
	}
}
