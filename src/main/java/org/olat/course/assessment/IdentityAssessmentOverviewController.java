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

package org.olat.course.assessment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.ShortName;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.logging.AssertException;
import org.olat.course.Structure;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;

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
public class IdentityAssessmentOverviewController extends BasicController {

	private static final String CMD_SELECT_NODE = "cmd.select.node"; 
	/** Event fired when a node has been selected, meaning when a row in the table has been selected **/
	public static final Event EVENT_NODE_SELECTED = new Event("event.node.selected");

	private Panel main = new Panel("assessmentOverviewPanel");
	private Structure runStructure;
	private boolean nodesSelectable;
	private boolean discardEmptyNodes;
	private boolean allowTableFiltering;
	private NodeAssessmentTableDataModel nodesTableModel;
	private TableController tableFilterCtr;
	

	private UserCourseEnvironment userCourseEnvironment;
	private AssessableCourseNode selectedCourseNode;
	private List<ShortName> nodesoverviewTableFilters;
	private ShortName discardEmptyNodesFilter;
	private ShortName showAllNodesFilter;
	private ShortName currentTableFilter;
	private List<Map<String, Object>> preloadedNodesList;
	private boolean loadNodesFromCourse;

	/**
	 * Constructor for the identity assessment overview controller to be used in the assessment tool or in the users
	 * course overview page
	 * @param ureq The user request
	 * @param wControl
	 * @param userCourseEnvironment The assessed identitys user course environment
	 * @param nodesSelectable configuration switch: true: user may select the nodes, e.g. to edit the nodes result, false: readonly view (user view)
	 * @param discardEmptyNodes filtering default value: true: do not show nodes that have no value. false: show all assessable nodes
	 * @param allowTableFiltering configuration switch: true: allow user to filter table all nodes/only nodes with data
	 */
	public IdentityAssessmentOverviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnvironment, 
			boolean nodesSelectable, boolean discardEmptyNodes, boolean allowTableFiltering) {
		super(ureq, wControl);
		this.runStructure = userCourseEnvironment.getCourseEnvironment().getRunStructure();
		this.nodesSelectable = nodesSelectable;
		this.discardEmptyNodes = discardEmptyNodes;
		this.allowTableFiltering = allowTableFiltering;
		this.userCourseEnvironment = userCourseEnvironment;		
		this.loadNodesFromCourse = true;
				
		if (this.allowTableFiltering) initNodesoverviewTableFilters();

		doIdentityAssessmentOverview(ureq);		
		putInitialPanel(main);   
	}

	/**
	 * Internal constructor used by the efficiency statement: uses a precompiled list of node data information
	 * instead of fetching everything from the database for each node
	 * @param ureq
	 * @param wControl
	 * @param assessmentCourseNodes List of maps containing the node assessment data using the AssessmentManager keys
	 */
	public IdentityAssessmentOverviewController(UserRequest ureq, WindowControl wControl, List<Map<String,Object>> assessmentCourseNodes) {
		super(ureq, wControl);
		this.runStructure = null;
		this.nodesSelectable = false;
		this.discardEmptyNodes = true;
		this.allowTableFiltering = false;
		this.userCourseEnvironment = null;		
		this.loadNodesFromCourse = false;
		this.preloadedNodesList = assessmentCourseNodes;
		
		doIdentityAssessmentOverview(ureq);		
		putInitialPanel(main);
	}
	
	public CourseNode getNextNode(CourseNode node) {
		int index = getIndexOf(node);
		
		String nodeIdent = null; 
		if(index >= 0) {
			int nextIndex = index + 1;//next
			if(nextIndex >= 0 && nextIndex < nodesTableModel.getRowCount()) {
				nodeIdent = (String)nodesTableModel.getObject(nextIndex).get(AssessmentHelper.KEY_IDENTIFYER);
			} else if(nodesTableModel.getRowCount() > 0) {
				nodeIdent = (String)nodesTableModel.getObject(0).get(AssessmentHelper.KEY_IDENTIFYER);
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
			if(previousIndex >= 0 && previousIndex < nodesTableModel.getRowCount()) {
				nodeIdent = (String)nodesTableModel.getObject(previousIndex).get(AssessmentHelper.KEY_IDENTIFYER);
			} else if(nodesTableModel.getRowCount() > 0) {
				nodeIdent = (String)nodesTableModel.getObject(nodesTableModel.getRowCount() - 1).get(AssessmentHelper.KEY_IDENTIFYER);
			}
		}
		
		if(nodeIdent != null) {
			return runStructure.getNode(nodeIdent);
		}
		return null;
	}
	
	private int getIndexOf(CourseNode node) {
		for(int i=nodesTableModel.getRowCount(); i-->0; ) {
			Object rowIdentityKey = nodesTableModel.getObject(i).get(AssessmentHelper.KEY_IDENTIFYER);
			if(rowIdentityKey.equals(node.getIdent())) {
				return i;
			}
		}
		return -1;
	}
	

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
	// no events to catch
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == tableFilterCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals(CMD_SELECT_NODE)) {
					int rowid = te.getRowId();
					Map<String,Object> nodeData = nodesTableModel.getObject(rowid);
					CourseNode node = runStructure.getNode((String) nodeData.get(AssessmentHelper.KEY_IDENTIFYER));
					this.selectedCourseNode = (AssessableCourseNode) node;
					// cast should be save, only assessable nodes are selectable
					fireEvent(ureq, EVENT_NODE_SELECTED);
				}
			} else if (event.equals(TableController.EVENT_FILTER_SELECTED)) {
				this.currentTableFilter = tableFilterCtr.getActiveFilter();
				if (this.currentTableFilter.equals(this.discardEmptyNodesFilter)) this.discardEmptyNodes = true;
				else if (this.currentTableFilter.equals(this.showAllNodesFilter)) this.discardEmptyNodes = false;
				doIdentityAssessmentOverview(ureq);
			}
		}
	}

	private void doIdentityAssessmentOverview(UserRequest ureq) {
		List<Map<String, Object>> nodesTableList;
		if (loadNodesFromCourse) {
			// get list of course node and user data and populate table data model 
			CourseNode rootNode = runStructure.getRootNode();		
			nodesTableList = AssessmentHelper.addAssessableNodeAndDataToList(0, rootNode, userCourseEnvironment, this.discardEmptyNodes, false);
		} else {
			// use list from efficiency statement 
			nodesTableList = preloadedNodesList;
		}
			// only populate data model if data available
		if (nodesTableList == null) {
			String text = translate("nodesoverview.emptylist");
			Controller messageCtr = MessageUIFactory.createSimpleMessage(ureq, getWindowControl(), text);
			main.setContent(messageCtr.getInitialComponent());
		} 
		else {

			TableGuiConfiguration tableConfig = new TableGuiConfiguration();
			tableConfig.setDownloadOffered(false);
			tableConfig.setSortingEnabled(true);
			tableConfig.setDisplayTableHeader(true);
			tableConfig.setDisplayRowCount(false);
			tableConfig.setPageingEnabled(false);
			tableConfig.setTableEmptyMessage(translate("nodesoverview.emptylist"));
			tableConfig.setPreferencesOffered(true, "assessmentIdentityNodeList");
			tableConfig.setDisplayTableGrid(true);

			removeAsListenerAndDispose(tableFilterCtr);
			if (allowTableFiltering) {
				tableFilterCtr = new TableController(tableConfig, ureq, getWindowControl(), 
						this.nodesoverviewTableFilters, this.currentTableFilter, 
						translate("nodesoverview.filter.title"), null,getTranslator());
			} else {
				tableFilterCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
			}
			listenTo(tableFilterCtr);
			
			final IndentedNodeRenderer nodeRenderer = new IndentedNodeRenderer() {
				@Override
				public boolean isIndentationEnabled() {
					return tableFilterCtr.getTableSortAsc() && tableFilterCtr.getTableSortCol() == 0;
				}
			};
			
			// table columns
			tableFilterCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.node", 0, null, 
					ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, nodeRenderer){
					@Override
					public int compareTo(int rowa, int rowb) {
						return rowa - rowb;
					}
			});
			tableFilterCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.details",1, null, ureq.getLocale()));
			tableFilterCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.attempts", 2, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT));
			tableFilterCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.score", 3, null, ureq.getLocale(),
					ColumnDescriptor.ALIGNMENT_RIGHT, new ScoreCellRenderer()));
			tableFilterCtr.addColumnDescriptor(false, new CustomRenderColumnDescriptor("table.header.min", 6, null, ureq.getLocale(), 
					ColumnDescriptor.ALIGNMENT_RIGHT, new ScoreCellRenderer()));
			tableFilterCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.max", 7, null, ureq.getLocale(),
					ColumnDescriptor.ALIGNMENT_RIGHT, new ScoreCellRenderer()));
			tableFilterCtr.addColumnDescriptor(new BooleanColumnDescriptor("table.header.passed", 4, translate("passed.true"), translate("passed.false")));
			// node selection only available if configured
			if (nodesSelectable) {
				tableFilterCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.action.select",5 ,CMD_SELECT_NODE, ureq.getLocale()) {
					@Override
					public boolean isSortingAllowed() {
						return false;
					}
				});
			}
			
			nodesTableModel = new NodeAssessmentTableDataModel(nodesTableList, getTranslator(), nodesSelectable);
			tableFilterCtr.setTableDataModel(nodesTableModel);

			main.setContent(tableFilterCtr.getInitialComponent());
		}
	}
	
	private void initNodesoverviewTableFilters(){
		// create filter for only nodes with values
		this.discardEmptyNodesFilter = new FilterName(translate("nodesoverview.filter.discardEmptyNodes"));
		// create filter for all nodes, even with no values
		this.showAllNodesFilter = new FilterName(translate("nodesoverview.filter.showEmptyNodes"));
		// add this two filter to the filters list
		this.nodesoverviewTableFilters = new ArrayList<ShortName>();
		this.nodesoverviewTableFilters.add(discardEmptyNodesFilter);
		this.nodesoverviewTableFilters.add(showAllNodesFilter);
		// set the current table filter according to configuration
		if (this.discardEmptyNodes)
			this.currentTableFilter = this.discardEmptyNodesFilter;
		else
			this.currentTableFilter = this.showAllNodesFilter;			
	}
	
	/**
	 * Returns the selected assessable course node. Call this method after getting the EVENT_NODE_SELECTED
	 * to get the selected node
	 * @return AssessableCourseNode
	 */
	public AssessableCourseNode getSelectedCourseNode() {
		if (selectedCourseNode == null)
			throw new AssertException("Selected course node was null. Maybe getSelectedCourseNode called prior to EVENT_NODE_SELECTED has been fired?");
		return selectedCourseNode;
	}
	
	
	/** 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
  	//
	}

}
