/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.assessment.ui.tool;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.Structure;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.bulk.PassedCellRenderer;
import org.olat.course.assessment.bulk.PassedOverridenCellRenderer;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.assessment.ui.tool.IdentityAssessmentOverviewTableModel.NodeCols;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<BR>
 * This controller provides an overview to the users course assessment. Two constructors are
 * available, one for the students read-only view and one for the coach/course-admins assessment
 * tool. In the second case a node can be selected which results in a EVENT_NODE_SELECTED event. 
 * <BR>
 * Use the IdentityAssessmentEditController to edit the users assessment data instead of this one.
 * <P>
 * Initial Date:  Oct 28, 2004
 *
 * @author gnaegi 
 */
public class IdentityAssessmentOverviewController extends FormBasicController implements Activateable2 {

	private static final String CMD_SELECT_NODE = "cmd.select.node"; 
	/** Event fired when a node has been selected, meaning when a row in the table has been selected **/
	public static final Event EVENT_NODE_SELECTED = new Event("event.node.selected");

	private Structure runStructure;
	private boolean nodesSelectable;
	private boolean discardEmptyNodes;
	private boolean allowTableFiltering;

	private FlexiTableElement tableEl;
	private IdentityAssessmentOverviewTableModel tableModel;

	private boolean loadNodesFromCourse;
	private final boolean followUserResultsVisibility;
	private CourseNode selectedCourseNode;
	private List<AssessmentNodeData> preloadedNodesList;
	private UserCourseEnvironment userCourseEnvironment;
	private boolean hasPassedOverridable;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	/**
	 * Constructor for the identity assessment overview controller to be used in the assessment tool or in the users
	 * course overview page
	 * @param ureq The user request
	 * @param wControl
	 * @param userCourseEnvironment The assessed identities user course environment
	 * @param nodesSelectable configuration switch: true: user may select the nodes, e.g. to edit the nodes result, false: readonly view (user view)
	 * @param discardEmptyNodes filtering default value: true: do not show nodes that have no value. false: show all assessable nodes
	 * @param allowTableFiltering configuration switch: true: allow user to filter table all nodes/only nodes with data
	 */
	public IdentityAssessmentOverviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnvironment, 
			boolean nodesSelectable, boolean discardEmptyNodes, boolean allowTableFiltering) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		
		this.runStructure = userCourseEnvironment.getCourseEnvironment().getRunStructure();
		this.nodesSelectable = nodesSelectable;
		this.discardEmptyNodes = discardEmptyNodes;
		this.allowTableFiltering = allowTableFiltering;
		this.userCourseEnvironment = userCourseEnvironment;		
		loadNodesFromCourse = true;
		followUserResultsVisibility = false;
		this.hasPassedOverridable = hasPassedOverridable(userCourseEnvironment.getCourseEnvironment().getRunStructure().getRootNode());

		initForm(ureq);
		loadModel();
	}

	private boolean hasPassedOverridable(CourseNode courseNode) {
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		if (assessmentConfig.isPassedOverridable()) {
			return true;
		} 
		int childCount = courseNode.getChildCount();
		for (int i = 0; i < childCount; i++) {
			INode child = courseNode.getChildAt(i);
			if (child instanceof CourseNode) {
				CourseNode childCourseNode = (CourseNode) child;
				return hasPassedOverridable(childCourseNode);
			}
		}
		return false;
	}

	/**
	 * Internal constructor used by the efficiency statement: uses a precompiled list of node data information
	 * instead of fetching everything from the database for each node
	 * @param ureq
	 * @param wControl
	 * @param assessmentCourseNodes List of maps containing the node assessment data using the AssessmentManager keys
	 */
	public IdentityAssessmentOverviewController(UserRequest ureq, WindowControl wControl, List<AssessmentNodeData> assessmentCourseNodes) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		
		runStructure = null;
		nodesSelectable = false;
		discardEmptyNodes = true;
		allowTableFiltering = false;
		userCourseEnvironment = null;		
		loadNodesFromCourse = false;
		followUserResultsVisibility = true;
		preloadedNodesList = assessmentCourseNodes;
	
		initForm(ureq);
		loadModel();
	}
	
	public boolean isRoot(CourseNode node) {
		return node != null && node.getIdent().equals(runStructure.getRootNode().getIdent());
	}
	
	public int getNumberOfNodes() {
		return tableModel.getRowCount();
	}
	
	public CourseNode getNode(int row) {
		AssessmentNodeData data = tableModel.getObject(row);
		return getNodeByIdent(data.getIdent());
	}
	
	public CourseNode getNodeByIdent(String ident) {
		return runStructure.getNode(ident);
	}
	
	public CourseNode getNextNode(CourseNode node) {
		int index = getIndexOf(node);
		
		String nodeIdent = null; 
		if(index >= 0) {
			int nextIndex = index + 1;//next
			if(nextIndex >= 0 && nextIndex < tableModel.getRowCount()) {
				nodeIdent = tableModel.getObject(nextIndex).getIdent();
			} else if(tableModel.getRowCount() > 0) {
				nodeIdent = tableModel.getObject(0).getIdent();
			}
		}
		
		if(nodeIdent != null) {
			return runStructure.getNode(nodeIdent);
		}
		return null;
	}
	
	public CourseNode getPreviousNode(CourseNode node) {
		int index = getIndexOf(node);
		
		String nodeIdent = null; 
		if(index >= 0) {
			int previousIndex = index - 1;//next
			if(previousIndex >= 0 && previousIndex < tableModel.getRowCount()) {
				nodeIdent = tableModel.getObject(previousIndex).getIdent();
			} else if(tableModel.getRowCount() > 0) {
				nodeIdent = tableModel.getObject(tableModel.getRowCount() - 1).getIdent();
			}
		}
		
		if(nodeIdent != null) {
			return runStructure.getNode(nodeIdent);
		}
		return null;
	}
	
	public int getIndexOf(CourseNode node) {
		for(int i=tableModel.getRowCount(); i-->0; ) {
			Object rowIdentityKey = tableModel.getObject(i).getIdent();
			if(rowIdentityKey.equals(node.getIdent())) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Returns the selected course node. Call this method after getting the EVENT_NODE_SELECTED
	 * to get the selected node
	 * @return AssessableCourseNode
	 */
	public CourseNode getSelectedCourseNode() {
		if (selectedCourseNode == null) {
			throw new AssertException("Selected course node was null. Maybe getSelectedCourseNode called prior to EVENT_NODE_SELECTED has been fired?");
		}
		return selectedCourseNode;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NodeCols.node, new IndentedNodeRenderer() {
			@Override
			public boolean isIndentationEnabled() {
				return tableEl.getOrderBy() == null || tableEl.getOrderBy().length == 0;
			}
		}));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NodeCols.attempts));
		if(!followUserResultsVisibility) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NodeCols.userVisibility, new UserVisibilityCellRenderer()));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NodeCols.score, new ScoreCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NodeCols.min, new ScoreCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NodeCols.max, new ScoreCellRenderer()));
		if (hasPassedOverridable) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, NodeCols.passedOverriden, new PassedOverridenCellRenderer()));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NodeCols.passed, new PassedCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NodeCols.numOfAssessmentDocs));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NodeCols.status, new AssessmentStatusCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, NodeCols.lastModified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NodeCols.lastUserModified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, NodeCols.lastCoachModified));
		
		if(nodesSelectable) {
			DefaultFlexiColumnModel selectCol = new DefaultFlexiColumnModel("select", NodeCols.select.ordinal(), CMD_SELECT_NODE,
					new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("select"), CMD_SELECT_NODE), null));
			selectCol.setExportable(false);
			columnsModel.addFlexiColumnModel(selectCol);
		}

		tableModel = new IdentityAssessmentOverviewTableModel(columnsModel); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 250, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setEmptyTableMessageKey("nodesoverview.emptylist");
		tableEl.setBordered(true);
		tableEl.setNumOfRowsEnabled(false);
		
		if (allowTableFiltering) {
			List<FlexiTableFilter> filters = new ArrayList<>();
			filters.add(new FlexiTableFilter(translate("filter.showAll"), "showAll", true));
			filters.add(FlexiTableFilter.SPACER);
			filters.add(new FlexiTableFilter(translate("filter.passed"), "passed"));
			filters.add(new FlexiTableFilter(translate("filter.failed"), "failed"));
			filters.add(new FlexiTableFilter(translate("filter.inProgress"), "inProgress"));
			filters.add(new FlexiTableFilter(translate("filter.inReview"), "inReview"));
			filters.add(new FlexiTableFilter(translate("filter.done"), "done"));
			tableEl.setFilters("", filters, false);
		}
	}
	
	protected void loadModel() {
		List<AssessmentNodeData> nodesTableList;
		if (loadNodesFromCourse) {
			// get list of course node and user data and populate table data model 	
			nodesTableList = AssessmentHelper.getAssessmentNodeDataList(userCourseEnvironment, null, followUserResultsVisibility, discardEmptyNodes, true);
		} else {
			// use list from efficiency statement 
			nodesTableList = preloadedNodesList;
		}
		tableModel.setObjects(nodesTableList);
		tableEl.reset(true, true, true);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(state instanceof AssessedIdentityListState) {
			AssessedIdentityListState listState = (AssessedIdentityListState)state;
			listState.setValuesToFilter(tableEl.getExtendedFilters());
			loadModel();
		}	
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				AssessmentNodeData nodeData = tableModel.getObject(se.getIndex());
				if(CMD_SELECT_NODE.equals(cmd)) {
					selectedCourseNode = runStructure.getNode(nodeData.getIdent());
					fireEvent(ureq, EVENT_NODE_SELECTED);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
}
