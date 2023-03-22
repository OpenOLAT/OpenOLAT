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
package org.olat.course.assessment.ui.reset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.ui.reset.ResetDataCourseElementsTableModel.ElementsCols;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 8 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResetDataCourseElementsSelectionController extends StepFormBasicController {

	private FlexiTableElement tableEl;
	private ResetDataCourseElementsTableModel dataModel;
	
	private final ResetDataContext dataContext;
	
	public ResetDataCourseElementsSelectionController(UserRequest ureq, WindowControl wControl,
			Form rootForm, StepsRunContext runContext, ResetDataContext dataContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "course_elements");
		this.dataContext = dataContext;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		IndentedNodeRenderer intendedNodeRenderer = new IndentedNodeRenderer();
		intendedNodeRenderer.setIndentationEnabled(false);
		DefaultFlexiColumnModel nodeModel = new DefaultFlexiColumnModel(ElementsCols.node);
		nodeModel.setCellRenderer(new TreeNodeFlexiCellRenderer(intendedNodeRenderer));
		nodeModel.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(nodeModel);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementsCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementsCols.longTitle));
		
		dataModel = new ResetDataCourseElementsTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 250, false, getTranslator(), formLayout);
		tableEl.setEmptyTableMessageKey("table.empty");
		tableEl.setSelection(true, true, true);
		tableEl.setSelectAllEnable(false);
	}
	
	private void loadModel() {
		RepositoryEntry entry = dataContext.getRepositoryEntry();
		ICourse course = CourseFactory.loadCourse(entry);
		
		CourseNode rootNode = course.getRunStructure().getRootNode();
		List<CourseNodeRow> rows = new ArrayList<>();
		forgeRows(rows, rootNode, 0, null);
		dataModel.setObjects(rows);
		tableEl.reset(true, false, true);
	}
	
	private void forgeRows(List<CourseNodeRow> rows, CourseNode node, int recursionLevel, CourseNodeRow parent) {
		CourseNodeRow row = new CourseNodeRow(node, parent, recursionLevel);
		rows.add(row);
		
		int childCount = node.getChildCount();
		for (int i = 0; i < childCount; i++) {
			INode child = node.getChildAt(i);
			if(child instanceof CourseNode childNode) {
				forgeRows(rows, childNode, ++recursionLevel, row);
			}
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		tableEl.clearError();
		if(dataModel.getSelectedTreeNodes().isEmpty()) {
			tableEl.setErrorKey("error.atleast.one.coursenode");
			allOk &= false;
		}
	
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				if(FlexiTableElement.ROW_CHECKED_EVENT.equals(se.getCommand())) {
					doSelectionUpdate(se.getIndex(), true);
				} else if(FlexiTableElement.ROW_UNCHECKED_EVENT.equals(se.getCommand())) {
					doSelectionUpdate(se.getIndex(), false);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formNext(UserRequest ureq) {
		List<CourseNodeRow> selectedList = dataModel.getSelectedTreeNodes();
		Set<CourseNodeRow> selectedRows = Set.copyOf(selectedList);
		List<CourseNode> selectedCourseNodes = selectedRows.stream()
				.map(CourseNodeRow::getCourseNode)
				.toList();
		dataContext.setCourseNodes(selectedCourseNodes);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private void doSelectionUpdate(int index, boolean checked) {
		CourseNodeRow selectedNode = dataModel.getObject(index);
		selectedNode.setSelected(checked);

		List<CourseNodeRow> allRows = dataModel.getAllRows();
		int allIndex = allRows.indexOf(selectedNode);
		for(int i=allIndex+1; i<allRows.size(); i++) {
			CourseNodeRow row = allRows.get(i);
			if(hasParent(row, selectedNode)) {
				row.setSelected(checked);
			} else {
				break;
			}
		}
		
		Set<Integer> newIndexes = new HashSet<>();
		for(int i=0; i<dataModel.getRowCount(); i++) {
			CourseNodeRow row = dataModel.getObject(i);
			if(row.isSelected()) {
				newIndexes.add(Integer.valueOf(i));
			}
		}
		tableEl.setMultiSelectedIndex(newIndexes);
		tableEl.reloadData();	
	}
	
	private boolean hasParent(CourseNodeRow node, CourseNodeRow reference) {
		for(FlexiTreeTableNode parent=node.getParent(); parent != null; parent=parent.getParent()) {
			if(parent == reference) {
				return true;
			}
		}
		return false;
	}
}
