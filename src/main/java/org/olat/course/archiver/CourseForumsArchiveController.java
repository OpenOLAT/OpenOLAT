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
* <p>
*/ 

package org.olat.course.archiver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.NodeTableDataModel;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.FOCourseNode;
import org.olat.user.UserManager;

/**
 * @author schneider
 * Comment:  
 * Archives  the user selected forum of a course to the personal folder of this user.
 */
public class CourseForumsArchiveController extends BasicController {
	private static final String CMD_SELECT_NODE = "cmd.select.node";
	
	private Panel main;	
	private VelocityContainer nodeChoose;
	private NodeTableDataModel nodeTableModel;
	private TableController nodeListCtr;
	private FOCourseNode currentCourseNode;

	private OLATResourceable ores;
	
	/**
	 * Constructor for the assessment tool controller. 
	 * @param ureq
	 * @param wControl
	 * @param course
	 */
	public CourseForumsArchiveController(UserRequest ureq, WindowControl wControl, OLATResourceable ores) {
		super(ureq, wControl);
		this.ores = ores;
		main = new Panel("main");
		nodeChoose = createVelocityContainer("fonodechoose");
		doNodeChoose(ureq);		
		putInitialPanel(main);
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		//no interesting events
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == nodeListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals(CMD_SELECT_NODE)) {
					int rowid = te.getRowId();
					Map nodeData = (Map) nodeTableModel.getObject(rowid);
					ICourse course = CourseFactory.loadCourse(ores);
					CourseNode node = course.getRunStructure().getNode((String) nodeData.get(AssessmentHelper.KEY_IDENTIFYER));
					this.currentCourseNode = (FOCourseNode) node;
					// cast should be save, only ta course nodes nodes are selectable
					boolean successfullyArchived = archiveForumNode(ureq);
					if (successfullyArchived) {
					  showInfo("archive.fo.successfully");	  
					} else {
						showWarning("archive.fo.notsuccessfully");	
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param ureq
	 */
	private void doNodeChoose(UserRequest ureq){
	    //table configuraton
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("fonodesoverview.nonodes"));
		tableConfig.setDownloadOffered(false);
		tableConfig.setColumnMovingOffered(false);
		tableConfig.setSortingEnabled(false);
		tableConfig.setDisplayTableHeader(true);
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		
		removeAsListenerAndDispose(nodeListCtr);
		nodeListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(nodeListCtr);
		
		// table columns		
		nodeListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.node", 0, 
				null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new IndentedNodeRenderer()));
		nodeListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.action.select", 1,
				CMD_SELECT_NODE, ureq.getLocale()));
		
		// get list of course node data and populate table data model
		ICourse course = CourseFactory.loadCourse(ores);
		CourseNode rootNode = course.getRunStructure().getRootNode();
		List nodesTableObjectArrayList = addForumNodesAndParentsToList(0, rootNode);
		
		// only populate data model if data available
		if (nodesTableObjectArrayList == null) {
			nodeChoose.contextPut("hasForumNodes", Boolean.FALSE);
		} else {
		    nodeChoose.contextPut("hasForumNodes", Boolean.TRUE);
			nodeTableModel = new NodeTableDataModel(nodesTableObjectArrayList, getTranslator());
			nodeListCtr.setTableDataModel(nodeTableModel);
			nodeChoose.put("fonodeTable", nodeListCtr.getInitialComponent());
		}
		
		// set main content to nodechoose, do not use wrapper
		main.setContent(nodeChoose);
		
	}
	
	/**
	 * Recursive method that adds forum nodes and all its parents to a list
	 * @param recursionLevel
	 * @param courseNode
	 * @return A list of maps containing the node data
	 */
	@SuppressWarnings("unchecked")
	private List addForumNodesAndParentsToList(int recursionLevel, CourseNode courseNode) {
		// 1) Get list of children data using recursion of this method
		List childrenData = new ArrayList();
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			CourseNode child = (CourseNode) courseNode.getChildAt(i);
			List childData = addForumNodesAndParentsToList( (recursionLevel + 1),  child);
			if (childData != null)
				childrenData.addAll(childData);
		}
		
		if (childrenData.size() > 0 || courseNode instanceof FOCourseNode) {
			// Store node data in map. This map array serves as data model for 
			// the tasks overview table. Leave user data empty since not used in
			// this table. (use only node data)
			Map nodeData = new HashMap();
			// indent	
			nodeData.put(AssessmentHelper.KEY_INDENT, new Integer(recursionLevel));
			// course node data
			nodeData.put(AssessmentHelper.KEY_TYPE, courseNode.getType());
			nodeData.put(AssessmentHelper.KEY_TITLE_SHORT, courseNode.getShortTitle());
			nodeData.put(AssessmentHelper.KEY_TITLE_LONG, courseNode.getLongTitle());
			nodeData.put(AssessmentHelper.KEY_IDENTIFYER, courseNode.getIdent());
			
			if (courseNode instanceof FOCourseNode){
				nodeData.put(AssessmentHelper.KEY_SELECTABLE, Boolean.TRUE);
			}
			else {
				nodeData.put(AssessmentHelper.KEY_SELECTABLE, Boolean.FALSE);
			}
			
			List nodeAndChildren = new ArrayList();
			nodeAndChildren.add(nodeData);

			nodeAndChildren.addAll(childrenData);
			return nodeAndChildren;
		}
		return null;
	}
	
	private boolean archiveForumNode(UserRequest ureq){
		ICourse course = CourseFactory.loadCourse(ores);
    File exportDir = CourseFactory.getOrCreateDataExportDirectory(ureq.getIdentity(), course.getCourseTitle());
    UserManager um = UserManager.getInstance();
    String charset = um.getUserCharset(ureq.getIdentity());
    return  currentCourseNode.archiveNodeData(ureq.getLocale(), course, exportDir, charset);    
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}
}
