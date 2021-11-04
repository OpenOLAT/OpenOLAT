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
package org.olat.course.editor.importnodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.editor.importnodes.SelectCourseNodesTableModel.SelectCols;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 1 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectCourseNodesController extends StepFormBasicController {

	private FlexiTableElement tableEl;
	private SelectCourseNodesTableModel dataModel;
	
	private ImportCourseNodesContext importCourseContext;
	
	public SelectCourseNodesController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, ImportCourseNodesContext importCourseContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "select_nodes");
		this.importCourseContext = importCourseContext;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		IndentedNodeRenderer intendedNodeRenderer = new IndentedNodeRenderer();
		intendedNodeRenderer.setIndentationEnabled(false);
		DefaultFlexiColumnModel nodeModel = new DefaultFlexiColumnModel(SelectCols.node);
		nodeModel.setCellRenderer(new TreeNodeFlexiCellRenderer(intendedNodeRenderer));
		nodeModel.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(nodeModel);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectCols.longTitle));
		
		dataModel = new SelectCourseNodesTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 250, false, getTranslator(), formLayout);
		tableEl.setEmptyTableMessageKey("table.empty");
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);	
	}
	
	private void loadModel() {
		RepositoryEntry entry = importCourseContext.getEntry();
		ICourse course = CourseFactory.loadCourse(entry);
		
		TreeNode rootNode = course.getEditorTreeModel().getRootNode();
		List<SelectCourseNodeRow> rows = new ArrayList<>();
		forgeRows(rows, rootNode, 0, null);
		dataModel.setObjects(rows);
		tableEl.reset(true, false, true);
	}
	
	private void forgeRows(List<SelectCourseNodeRow> rows, INode node, int recursionLevel, SelectCourseNodeRow parent) {
		if (node instanceof CourseEditorTreeNode) {
			CourseEditorTreeNode editorNode = (CourseEditorTreeNode)node;
			SelectCourseNodeRow row = new SelectCourseNodeRow(editorNode, parent, recursionLevel);
			rows.add(row);
			
			int childCount = editorNode.getChildCount();
			for (int i = 0; i < childCount; i++) {
				INode child = editorNode.getChildAt(i);
				forgeRows(rows, child, ++recursionLevel, row);
			}
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(tableEl.getMultiSelectedIndex().isEmpty() && !importCourseContext.hasNodes()) {
			tableEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else {
			tableEl.clearError();
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
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
		List<ImportCourseNode> nodes = new ArrayList<>();
		List<ImportCourseNode> currentNodesList = importCourseContext.getSelectedNodes();
		Map<String,ImportCourseNode> currentNodesMap = currentNodesList.stream()
				.collect(Collectors.toMap(ImportCourseNode::getIdent, n -> n));
		
		Set<Integer> indexes = tableEl.getMultiSelectedIndex();
		for(Integer index:indexes) {
			SelectCourseNodeRow row = dataModel.getObject(index.intValue());
			ImportCourseNode node = currentNodesMap.get(row.getEditorTreeNode().getIdent());
			if(node == null) {
				node = new ImportCourseNode(row.getEditorTreeNode());
			}
			nodes.add(node);
		}
		importCourseContext.setNodes(nodes);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private void doSelectionUpdate(int index, boolean checked) {
		Set<Integer> indexes = tableEl.getMultiSelectedIndex();
		Set<Integer> newIndexes = new HashSet<>(indexes);
		
		SelectCourseNodeRow selectedNode = dataModel.getObject(index);
		for(int i=index+1; i<dataModel.getRowCount(); i++) {
			if(hasParent(dataModel.getObject(i), selectedNode)) {
				if(checked) {
					newIndexes.add(Integer.valueOf(i));
				} else {
					newIndexes.remove(Integer.valueOf(i));
				}
			} else {
				break;
			}
		}
		
		tableEl.setMultiSelectedIndex(newIndexes);
		tableEl.reloadData();	
	}
	
	private boolean hasParent(SelectCourseNodeRow node, SelectCourseNodeRow reference) {
		for(FlexiTreeTableNode parent=node.getParent(); parent != null; parent=parent.getParent()) {
			if(parent == reference) {
				return true;
			}
		}
		return false;
	}
}
