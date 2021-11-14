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
package org.olat.course.assessment.bulk;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.manager.BulkAssessmentTask;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 18.11.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectCourseNodeStepForm extends StepFormBasicController {
	
	private final RepositoryEntry courseEntry;
	
	private FlexiTableElement tableEl;
	private NodeTableDataModel tableModel;
	
	public SelectCourseNodeStepForm(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "select_node");
		this.courseEntry = courseEntry;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		CourseNode rootNode = course.getRunStructure().getRootNode();
		List<Node> courseNodes = addManualTaskNodesAndParentsToList(0, rootNode);
		tableModel = new NodeTableDataModel(courseNodes);

		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, "table.header.node",
				Cols.node.ordinal(), false, null, FlexiColumnModel.ALIGNMENT_LEFT, new CourseNodeRenderer()));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.action.select",
				Cols.assessable.ordinal(), "select",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.action.select"), "select"), null)));
		tableModel.setTableColumnModel(tableColumnModel);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "nodeList", tableModel, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				int index = se.getIndex();
				Node node = tableModel.getObject(index);
				addToRunContext("courseNode", node.getNode());
				fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private List<Node> addManualTaskNodesAndParentsToList(int recursionLevel, CourseNode courseNode) {
		// 1) Get list of children data using recursion of this method
		List<Node> childrenData = new ArrayList<>();
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			CourseNode child = (CourseNode)courseNode.getChildAt(i);
			List<Node> childData = addManualTaskNodesAndParentsToList( (recursionLevel + 1),  child);
			if (childData != null) {
				childrenData.addAll(childData);
			}
		}
		
		boolean bulkAssessability = BulkAssessmentTask.isBulkAssessable(courseNode);

		if (childrenData.size() > 0 || bulkAssessability) {
			Node nodeData = new Node(courseNode, bulkAssessability, recursionLevel);
			childrenData.add(0, nodeData);
			return childrenData;
		}
		return null;
	}
	
	private enum Cols {
		node,
		assessable,
	}
	
	public static class Node {
		private final int indent;
		private final CourseNode node;
		private final boolean assessable;
		
		public Node(CourseNode node, boolean assessable, int indent) {
			this.node = node;
			this.indent = indent;
			this.assessable = assessable;
		}

		public int getIndent() {
			return indent;
		}

		public CourseNode getNode() {
			return node;
		}

		public boolean isAssessable() {
			return assessable;
		}
	}
	
	private static class NodeTableDataModel extends DefaultTableDataModel<Node> implements FlexiTableDataModel<Node> {
		private FlexiTableColumnModel columnModel;
		
		public NodeTableDataModel(List<Node> nodes) {
			super(nodes);
		}

		@Override
		public FlexiTableColumnModel getTableColumnModel() {
			return columnModel;
		}

		@Override
		public void setTableColumnModel(FlexiTableColumnModel tableColumnModel) {
			this.columnModel = tableColumnModel;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public Object getValueAt(int row, int col) {
			Node node = getObject(row);
			switch(Cols.values()[col]) {
				case node: return node;
				case assessable: return node.isAssessable() ? Boolean.TRUE : Boolean.FALSE;
				default: return null;
			}
		}
	}
}
