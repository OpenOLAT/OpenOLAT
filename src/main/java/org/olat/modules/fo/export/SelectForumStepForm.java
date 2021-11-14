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
package org.olat.modules.fo.export;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
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
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.FOCourseNode;
import org.olat.repository.RepositoryEntry;

/**
 * Initial Date: 15.02.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class SelectForumStepForm extends StepFormBasicController {

	private List<CourseNode> nodeTypes = new ArrayList<>();
	
	private FlexiTableElement tableEl;
	private NodeTableDataModel tableModel;
	
	private List<CourseNode> courseNodes;
	private ICourse course;
	
	
	public SelectForumStepForm(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
			String customLayoutPageName) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, customLayoutPageName);
		nodeTypes.add(new FOCourseNode());
		
		RepositoryEntry courseEntry = (RepositoryEntry) (getFromRunContext(SendMailStepForm.COURSE) != null 
				? getFromRunContext(SendMailStepForm.COURSE) : getFromRunContext(SendMailStepForm.START_COURSE)); 
		if (courseEntry != null) {
			course = CourseFactory.loadCourse(courseEntry);
			if (course != null) {
				CourseNode rootNode = course.getRunStructure().getRootNode();
				addToRunContext(SendMailStepForm.ICOURSE, course);
				// get list of course node data and populate table data model
				courseNodes = addNodesAndParentsToList(0, rootNode);				
			}
		}
		initForm(ureq);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				int index = se.getIndex();
				CourseNode node = tableModel.getObject(index);
				addToRunContext(SendMailStepForm.FORUM, node);
				fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}


	@Override
	protected boolean validateFormLogic(UserRequest ureq) {

		return super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}


	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.forum));
		tableColumnModel.addFlexiColumnModel(
				new DefaultFlexiColumnModel(Cols.select, "forum.select", new StaticFlexiCellRenderer(
						translate("forum.select"), "forum.select", "", "o_icon o_icon_select o_icon-fw")));
		
		tableModel = new NodeTableDataModel(courseNodes);
		tableModel.setTableColumnModel(tableColumnModel);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
	}
	
	private enum Cols implements FlexiSortableColumnDef{
		forum("forum.forum"),
		select("forum.select");
		
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
	
	
	private static class NodeTableDataModel extends DefaultTableDataModel<CourseNode> implements FlexiTableDataModel<CourseNode> {
		private FlexiTableColumnModel columnModel;
		
		public NodeTableDataModel(List<CourseNode> nodes) {
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
			CourseNode node = getObject(row);
			switch(Cols.values()[col]) {
				case forum: return node.getShortName();
				case select: return node;
				default: return null;
			}
		}
	}
	
	private List<CourseNode> addNodesAndParentsToList(int recursionLevel, CourseNode courseNode) {
		// 1) Get list of children data using recursion of this method
		List<CourseNode> childrenData = new ArrayList<>();
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			CourseNode child = (CourseNode) courseNode.getChildAt(i);
			List<CourseNode> childData = addNodesAndParentsToList((recursionLevel + 1), child);
			if (childData != null) {
				childrenData.addAll(childData);
			}
		}

		boolean matchType = matchTypes(courseNode);
		if (childrenData.size() > 0 || matchType) {
			List<CourseNode> nodeAndChildren = new ArrayList<>();
			if (courseNode instanceof FOCourseNode) {
				nodeAndChildren.add(courseNode);				
			}
			nodeAndChildren.addAll(childrenData);
			return nodeAndChildren;
		}
		return null;
	}
	
	private boolean matchTypes(CourseNode courseNode) {
		boolean match = false;
		for (CourseNode nodeType : nodeTypes) {
			match |= courseNode.getType().equals(nodeType.getType());
		}
		return match;
	}
}


