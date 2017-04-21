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

package org.olat.course.archiver;

import java.util.ArrayList;
import java.util.List;

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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.group.BusinessGroup;

/**
 * @author schnider Comment: Archives the User selected wiki's to the personal
 *         folder of this user.
 * @author fkiefer
 */
public class GenericArchiveController extends BasicController {
	
	private static final String CMD_SELECT_NODE = "cmd.select.node";
	
	private final Panel main;
	private final VelocityContainer nodeChoose;
	private TableController nodeListCtr;
	private NodeTableDataModel nodeTableModel;
	private CloseableModalController cmc;
	private ChooseGroupController chooseGroupCtrl;
	
	private boolean hideTitle;
	private ArchiveOptions options;
	
	private final CourseNode[] nodeTypes;
	private final OLATResourceable ores;

	/**
	 * Constructor for the assessment tool controller.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param course
	 */
	public GenericArchiveController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, CourseNode... nodeTypes) {
		super(ureq, wControl);

		this.ores = ores;
		this.nodeTypes = nodeTypes;
		
		main = new Panel("main");
		nodeChoose = createVelocityContainer("nodechoose");
		nodeChoose.contextPut("nodeType", nodeTypes[0].getType());
		
		options = new ArchiveOptions();

		doNodeChoose(ureq, nodeChoose);		
		putInitialPanel(main);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// no interesting events
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == nodeListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent)event;
				String actionid = te.getActionId();
				if (actionid.equals(CMD_SELECT_NODE)) {
					AssessmentNodeData nodeData = nodeTableModel.getObject(te.getRowId());
					doSelectNode(ureq, nodeData);
				}
			}
		} else if(source == chooseGroupCtrl) {
			cmc.deactivate();
			CourseNode courseNode = chooseGroupCtrl.getCourseNode();
			BusinessGroup group = chooseGroupCtrl.getSelectedGroup();
			cleanUpPopups();
			if(Event.DONE_EVENT == event) {
				archiveNode(ureq, courseNode, group);
			}
		} else if (source == cmc) {
			cleanUpPopups();
		}
	}
	
	/**
	 * Aggressive clean up all popup controllers
	 */
	protected void cleanUpPopups() {
		removeAsListenerAndDispose(chooseGroupCtrl);
		removeAsListenerAndDispose(cmc);
		chooseGroupCtrl = null;
		cmc = null;
	}

	/**
	 * @param ureq
	 */
	private void doNodeChoose(UserRequest ureq, VelocityContainer nodeChoose) {
		// table configuraton
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("nodesoverview.nonodes"));
		tableConfig.setDownloadOffered(false);
		tableConfig.setSortingEnabled(false);
		tableConfig.setDisplayTableHeader(true);
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);

		removeAsListenerAndDispose(nodeListCtr);
		nodeListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(nodeListCtr);
		
		// table columns
		nodeListCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.node", 0, null, getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, new IndentedNodeRenderer()));
		nodeListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.action.select", 1, CMD_SELECT_NODE, getLocale()));

		// get list of course node data and populate table data model
		ICourse course = CourseFactory.loadCourse(ores);
		CourseNode rootNode = course.getRunStructure().getRootNode();
		List<AssessmentNodeData> nodesTableObjectArrayList = addNodesAndParentsToList(0, rootNode);

		// only populate data model if data available
		if (nodesTableObjectArrayList == null) {
			nodeChoose.contextPut("hasNodes", Boolean.FALSE);
		} else {
			nodeChoose.contextPut("hasNodes", Boolean.TRUE);
			nodeTableModel = new NodeTableDataModel(nodesTableObjectArrayList, getTranslator());
			nodeListCtr.setTableDataModel(nodeTableModel);
			nodeChoose.put("nodeTable", nodeListCtr.getInitialComponent());
		}

		// set main content to nodechoose, do not use wrapper
		main.setContent(nodeChoose);
	}

	/**
	 * Recursive method that adds nodes and all its parents to a list
	 * 
	 * @param recursionLevel
	 * @param courseNode
	 * @return A list of maps containing the node data
	 */
	private List<AssessmentNodeData> addNodesAndParentsToList(int recursionLevel, CourseNode courseNode) {
		// 1) Get list of children data using recursion of this method
		List<AssessmentNodeData> childrenData = new ArrayList<>();
		for (int i = 0; i < courseNode.getChildCount(); i++) {
			CourseNode child = (CourseNode) courseNode.getChildAt(i);
			List<AssessmentNodeData> childData = addNodesAndParentsToList((recursionLevel + 1), child);
			if (childData != null) {
				childrenData.addAll(childData);
			}
		}

		boolean matchType = matchTypes(courseNode);
		if (childrenData.size() > 0 || matchType) {
			// Store node data in map. This map array serves as data model for
			// the tasks overview table. Leave user data empty since not used in
			// this table. (use only node data)
			AssessmentNodeData nodeData = new AssessmentNodeData(recursionLevel, courseNode);
			nodeData.setSelectable(matchType);
			
			List<AssessmentNodeData> nodeAndChildren = new ArrayList<>();
			nodeAndChildren.add(nodeData);
			nodeAndChildren.addAll(childrenData);
			return nodeAndChildren;
		}
		return null;
	}
	
	private boolean matchTypes(CourseNode courseNode) {
		boolean match = false;
		for(CourseNode nodeType:nodeTypes) {
			match |= courseNode.getType().equals(nodeType.getType());
		}
		return match;
	}
	
	private void doSelectNode(UserRequest ureq, AssessmentNodeData nodeData) {
		ICourse course = CourseFactory.loadCourse(ores);
		CourseNode node = course.getRunStructure().getNode(nodeData.getIdent());
		//some node can limit the archive to a business group
		if(node instanceof TACourseNode) {
			CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
			List<BusinessGroup> relatedGroups = cgm.getAllBusinessGroups();
			if(relatedGroups.isEmpty()) {
				archiveNode(ureq, node, null);
			} else {
				doSelectBusinessGroup(ureq, node, relatedGroups);
			}
		} else {
			archiveNode(ureq, node, null);
		}
	}
	
	private void doSelectBusinessGroup(UserRequest ureq, CourseNode node, List<BusinessGroup> relatedGroups) {
		chooseGroupCtrl = new ChooseGroupController(ureq, getWindowControl(), node, relatedGroups);
		listenTo(chooseGroupCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), chooseGroupCtrl.getInitialComponent(),
				true, translate("select.group"));
		cmc.activate();
		listenTo(cmc);
	}

	private void archiveNode(UserRequest ureq, CourseNode node, BusinessGroup group) {
		options.setGroup(group);
		ArchiveResource aResource = new ArchiveResource(node, ores, options, getLocale());
		ureq.getDispatchResult().setResultingMediaResource(aResource);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}

	public boolean isHideTitle() {
		return hideTitle;
	}

	public void setHideTitle(boolean hideTitle) {
		this.hideTitle = hideTitle;
		nodeChoose.contextPut("hideTitle", hideTitle);
	}

	public void setOptions(ArchiveOptions options) {
		this.options = options;
	}
}