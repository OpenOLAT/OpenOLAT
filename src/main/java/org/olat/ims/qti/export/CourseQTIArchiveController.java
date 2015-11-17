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

package org.olat.ims.qti.export;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;

/**
 * 
 * @author schneider
 * 
 * Comment:  
 * Archives all QTI results from a specific QTI node in the personal folder
 * of the current user.  
 */
public class CourseQTIArchiveController extends BasicController {
		
	private static final String CMD_SELECT_NODE = "cmd.select.node";
	
	private VelocityContainer introVC;
	
	private TableController nodeListCtr;
	
	private QTIArchiveWizardController qawc;
	private CloseableModalController cmc;
	private OLATResourceable ores;
	
	private List<AssessmentNodeData> nodesTableObjectArrayList;
	private Link startExportDummyButton;
	private Link startExportButton;
	
	/**
	 * Constructor for the assessment tool controller. 
	 * @param ureq
	 * @param wControl
	 * @param course
	 */
	public CourseQTIArchiveController(UserRequest ureq, WindowControl wControl, OLATResourceable ores) { 
		
		super(ureq, wControl);	
		
		this.ores = ores;

		introVC = this.createVelocityContainer("intro");
		startExportDummyButton = LinkFactory.createButtonSmall("command.start.exportwizard.dummy", introVC, this);
		startExportButton = LinkFactory.createButtonSmall("command.start.exportwizard", introVC, this);
		
		nodesTableObjectArrayList = doNodeChoose(ureq);
		
		if (nodesTableObjectArrayList == null) {
			introVC.contextPut("hasQTINodes", Boolean.FALSE);
		} else {
			introVC.contextPut("hasQTINodes", Boolean.TRUE);
		}
		
		putInitialPanel(introVC);
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		ICourse course = CourseFactory.loadCourse(ores);
		if (source == startExportButton){
			qawc = new QTIArchiveWizardController(false, ureq, nodesTableObjectArrayList, course, getWindowControl());
		}	else if (source == startExportDummyButton){
			qawc = new QTIArchiveWizardController(true, ureq, nodesTableObjectArrayList, course, getWindowControl());
		}
		listenTo(qawc);
		String title = qawc.getAndRemoveWizardTitle();
		cmc = new CloseableModalController(getWindowControl(), translate("close"), qawc.getInitialComponent(), true, title);
		cmc.setContextHelp(getTranslator(), "Archiving Results of Tests and Questionnaires");
		cmc.activate();
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == qawc){
			if (event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
			}
		}
	}
	
	/**
	 * 
	 * @param ureq
	 * @return 
	 */
	private List<AssessmentNodeData> doNodeChoose(UserRequest ureq){
	    //table configuraton
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("nodesoverview.nonodes"));
		tableConfig.setDownloadOffered(false);
		tableConfig.setSortingEnabled(false);
		tableConfig.setDisplayTableHeader(true);
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		
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
		List<AssessmentNodeData> objectArrayList = addQTINodesAndParentsToList(0, rootNode);
		
		return objectArrayList;		
	}
	
	/**
	 * Recursive method that adds tasks nodes and all its parents to a list
	 * @param recursionLevel
	 * @param courseNode
	 * @return A list of Object[indent, courseNode, selectable]
	 */
	private List<AssessmentNodeData> addQTINodesAndParentsToList(int recursionLevel, CourseNode courseNode) {
		// 1) Get list of children data using recursion of this method
		List<AssessmentNodeData> childrenData = new ArrayList<>();
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			CourseNode child = (CourseNode) courseNode.getChildAt(i);
			List<AssessmentNodeData> childData = addQTINodesAndParentsToList( (recursionLevel + 1),  child);
			if (childData != null) {
				childrenData.addAll(childData);
			}
		}
		
		if (childrenData.size() > 0
		        || courseNode instanceof IQTESTCourseNode
		        || courseNode instanceof IQSELFCourseNode
		        || courseNode instanceof IQSURVCourseNode) {
			// Store node data in hash map. This hash map serves as data model for 
			// the tasks overview table. Leave user data empty since not used in
			// this table. (use only node data)
			AssessmentNodeData nodeData = new AssessmentNodeData(recursionLevel, courseNode);
			if (courseNode instanceof IQTESTCourseNode
			        || courseNode instanceof IQSELFCourseNode
			        || courseNode instanceof IQSURVCourseNode){
				nodeData.setSelectable(true);
			} else {
				nodeData.setSelectable(false);
			}
			
			List<AssessmentNodeData> nodeAndChildren = new ArrayList<>();
			nodeAndChildren.add(nodeData);
			nodeAndChildren.addAll(childrenData);
			return nodeAndChildren;
		}
		return null;
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {		
		//
	}
}

