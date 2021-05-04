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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.archiver.NodeTableDataModel.NodeCols;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.TACourseNode;
import org.olat.group.BusinessGroup;


/**
 * @author schnider Comment: Archives the User selected wiki's to the personal
 *         folder of this user.
 * @author fkiefer
 */
public class GenericArchiveController extends FormBasicController {
	
	private FormLink selectButton;
	private FlexiTableElement tableEl;
	private NodeTableDataModel nodeTableModel;
	private FormLink downloadOptionsButton;
	
	private CloseableModalController cmc;
	private ChooseGroupController chooseGroupCtrl;
	private ExportOptionsController exportOptionsCtrl; 
	
	private final ArchiveOptions options;
	private final boolean withOptions;
	private final OLATResourceable ores;
	private final CourseNode[] nodeTypes;

	/**
	 * Constructor for the assessment tool controller.
	 * 
	 * @param ureq The user request
	 * @param wControl The window control	
	 * @param ores The resourceable of the course
	 * @param options Allow to configure the archive options
	 * @param nodeTypes The node types to export
	 */
	public GenericArchiveController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, boolean withOptions,  CourseNode... nodeTypes) {
		super(ureq, wControl, "nodechoose");
		this.ores = ores;
		this.nodeTypes = nodeTypes;
		this.withOptions = withOptions;
		options = new ArchiveOptions();
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("nodeType", nodeTypes[0].getType());
			String cssClass = CourseNodeFactory.getInstance()
					.getCourseNodeConfigurationEvenForDisabledBB(nodeTypes[0].getType()).getIconCSSClass();
			layoutCont.contextPut("iconCss", cssClass);

			FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NodeCols.data, new IndentedNodeRenderer()));
			DefaultFlexiColumnModel selectColumn = new DefaultFlexiColumnModel("archive", NodeCols.select.ordinal(), "select",
					new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("archive"), "select"), null));	
			columnsModel.addFlexiColumnModel(selectColumn);
			
			nodeTableModel = new NodeTableDataModel(columnsModel, getTranslator());
			
			tableEl = uifactory.addTableElement(getWindowControl(), "nodeTable", nodeTableModel, 1024, false, getTranslator(), layoutCont);
			tableEl.setExportEnabled(false);
			tableEl.setNumOfRowsEnabled(false);
			tableEl.setCustomizeColumns(false);
			tableEl.setMultiSelect(true);
			tableEl.setSelectAllEnable(true);
			tableEl.setEmptyTableMessageKey("nodesoverview.nonodes");
			
			selectButton = uifactory.addFormLink("archive", formLayout, Link.BUTTON);
			if(withOptions) {
				downloadOptionsButton = uifactory.addFormLink("download.options", layoutCont, Link.BUTTON_SMALL);
				downloadOptionsButton.setIconLeftCSS("o_icon o_icon_tools");
			}
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if ("select".equals(se.getCommand())) {
					AssessmentNodeData nodeData = nodeTableModel.getObject(se.getIndex());
					doSelectNode(ureq, nodeData);
				}
			}
		} else if(source == selectButton) {
			doMultiSelectNodes(ureq);
		} else if (source == downloadOptionsButton) {
			doOpenExportOptios(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == chooseGroupCtrl) {
			cmc.deactivate();
			List<CourseNode> courseNodes = chooseGroupCtrl.getCourseNodes();
			BusinessGroup group = chooseGroupCtrl.getSelectedGroup();
			cleanUpPopups();
			if(Event.DONE_EVENT == event) {
				archiveNode(ureq, courseNodes, group);
			}
		} else if (source == exportOptionsCtrl) {
			cmc.deactivate();
			cleanUpPopups();
		} else if (source == cmc) {
			cleanUpPopups();
		}
	}
	
	/**
	 * Aggressive clean up all popup controllers
	 */
	protected void cleanUpPopups() {
		removeAsListenerAndDispose(exportOptionsCtrl);
		removeAsListenerAndDispose(chooseGroupCtrl);
		removeAsListenerAndDispose(cmc);
		exportOptionsCtrl = null;
		chooseGroupCtrl = null;
		cmc = null;
	}
	
	private void loadModel() {
		ICourse course = CourseFactory.loadCourse(ores);
		CourseNode rootNode = course.getRunStructure().getRootNode();
		List<AssessmentNodeData> nodesTableObjectArrayList = addNodesAndParentsToList(0, rootNode);
		flc.contextPut("hasNodes", Boolean.valueOf(!nodesTableObjectArrayList.isEmpty()));
		nodeTableModel.setObjects(nodesTableObjectArrayList);
		tableEl.reset(true, true, true);
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
		if (!childrenData.isEmpty() || matchType) {
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
		return new ArrayList<>();
	}
	
	private boolean matchTypes(CourseNode courseNode) {
		boolean match = false;
		for(CourseNode nodeType:nodeTypes) {
			match |= courseNode.getType().equals(nodeType.getType());
		}
		return match;
	}
	
	private void doMultiSelectNodes(UserRequest ureq) {
		ICourse course = CourseFactory.loadCourse(ores);
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		List<CourseNode> nodes = new ArrayList<>(selectedIndex.size());
		for(Integer index:selectedIndex) {
			AssessmentNodeData nodeData = nodeTableModel.getObject(index.intValue());
			CourseNode node = course.getRunStructure().getNode(nodeData.getIdent());
			if(matchTypes(node)) {
				nodes.add(node);
			}
		}
		
		if(nodes.isEmpty()) {
			showWarning("warning.atleast.node");
		} else if(nodes.get(0) instanceof TACourseNode) {
			CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
			List<BusinessGroup> relatedGroups = cgm.getAllBusinessGroups();
			if(relatedGroups.isEmpty()) {
				archiveNode(ureq, nodes, null);
			} else {
				doSelectBusinessGroup(ureq, nodes, relatedGroups);
			}
		} else {
			archiveNode(ureq, nodes, null);
		}
	}
	
	private void doSelectNode(UserRequest ureq, AssessmentNodeData nodeData) {
		ICourse course = CourseFactory.loadCourse(ores);
		CourseNode node = course.getRunStructure().getNode(nodeData.getIdent());
		List<CourseNode> nodes = Collections.singletonList(node);
		//some node can limit the archive to a business group
		if(node instanceof TACourseNode) {
			CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
			List<BusinessGroup> relatedGroups = cgm.getAllBusinessGroups();
			if(relatedGroups.isEmpty()) {
				archiveNode(ureq, nodes, null);
			} else {
				doSelectBusinessGroup(ureq, nodes, relatedGroups);
			}
		} else {
			archiveNode(ureq, nodes, null);
		}
	}
	
	private void doSelectBusinessGroup(UserRequest ureq, List<CourseNode> nodes, List<BusinessGroup> relatedGroups) {
		chooseGroupCtrl = new ChooseGroupController(ureq, getWindowControl(), nodes, relatedGroups);
		listenTo(chooseGroupCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), chooseGroupCtrl.getInitialComponent(),
				true, translate("select.group"));
		cmc.activate();
		listenTo(cmc);
	}

	private void archiveNode(UserRequest ureq, List<CourseNode> nodes, BusinessGroup group) {
		options.setGroup(group);
		options.setExportFormat(FormatConfigHelper.loadExportFormat(ureq));
		
		ArchiveResource aResource = new ArchiveResource(nodes, ores, options, getLocale());
		ureq.getDispatchResult().setResultingMediaResource(aResource);
	}
	
	private void doOpenExportOptios(UserRequest ureq) {
		exportOptionsCtrl = new ExportOptionsController(ureq, getWindowControl());
		listenTo(exportOptionsCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), exportOptionsCtrl.getInitialComponent(),
				true, translate("download.options"));
		cmc.activate();
		listenTo(cmc);
	}
}