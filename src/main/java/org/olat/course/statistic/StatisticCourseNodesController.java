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
package org.olat.course.statistic;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.Group;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StatisticCourseNodesController extends BasicController implements Activateable2, TooledController {
	private final MenuTree courseTree;
	private final TooledStackedPanel stackPanel;
	private final LayoutMain3ColsController layoutCtr;
	private Controller currentCtrl;
	
	private final StatisticType type;
	private final StatisticResourceOption options;
	
	@Autowired
	private RepositoryService repositoryService;
	
	public StatisticCourseNodesController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntrySecurity reSecurity, UserCourseEnvironment userCourseEnv, StatisticType type) {
		super(ureq, wControl);

		this.type = type;
		this.stackPanel = stackPanel;
		options = new StatisticResourceOption();

		if(!reSecurity.isEntryAdmin() && !reSecurity.isOwner()) {
			List<Group> groups = getCoachedGroups(reSecurity, userCourseEnv);
			options.setParticipantsGroups(groups);
		}

		courseTree = new MenuTree("assessmentStatisticsTree");
		courseTree.setTreeModel(buildTreeModel(ureq, userCourseEnv));
		courseTree.setRootVisible(false);
		courseTree.addListener(this);
		
		Panel empty = new Panel("splashScreen");
		layoutCtr = new LayoutMain3ColsController(ureq, wControl, courseTree, empty, null);
		listenTo(layoutCtr);
		putInitialPanel(layoutCtr.getInitialComponent());

		// activate first child
		TreeModel tree = courseTree.getTreeModel();
		if (tree != null && tree.getRootNode().getChildCount() > 0) {
			doSelectNode(ureq, (TreeNode)tree.getRootNode().getChildAt(0));
		}
	}
	
	private List<Group> getCoachedGroups(RepositoryEntrySecurity reSecurity, UserCourseEnvironment userCourseEnv) {
		List<Group> groups = new ArrayList<>();
		if(reSecurity.isCourseCoach()) {
			Group bGroup = repositoryService.getDefaultGroup(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
			groups.add(bGroup);
		}
		if(reSecurity.isGroupCoach()) {
			List<BusinessGroup> businessGroups = userCourseEnv.getCoachedGroups();
			for(BusinessGroup businessGroup:businessGroups) {
				groups.add(businessGroup.getBaseGroup());
			}
		}
		if(reSecurity.isCurriculumCoach()) {
			List<CurriculumElement> curriculumElements = userCourseEnv.getCoachedCurriculumElements();
			for(CurriculumElement curriculumElement:curriculumElements) {
				groups.add(curriculumElement.getGroup());
			}
		}
		return groups;
	}
	
	@Override
	public void initTools() {
		if(currentCtrl instanceof TooledController) {
			((TooledController)currentCtrl).initTools();
		}
	}
	
	private TreeModel buildTreeModel(final UserRequest ureq, final UserCourseEnvironment userCourseEnv) {
		final GenericTreeModel gtm = new GenericTreeModel();
		final GenericTreeNode rootTreeNode = new GenericTreeNode();
		rootTreeNode.setTitle("start");
		gtm.setRootNode(rootTreeNode);
		
		ICourse course = CourseFactory.loadCourse(userCourseEnv.getCourseEnvironment().getCourseResourceableId());

		new TreeVisitor(new Visitor() {
			@Override
			public void visit(INode node) {
				CourseNode courseNode = (CourseNode)node;
				StatisticResourceResult result = courseNode.createStatisticNodeResult(ureq, getWindowControl(), userCourseEnv, options, type);
				if(result != null) {
					StatisticResourceNode courseNodeTreeNode = new StatisticResourceNode(courseNode, result);
					rootTreeNode.addChild(courseNodeTreeNode);
					
					TreeModel subTreeModel = result.getSubTreeModel();
					if(subTreeModel != null) {
						TreeNode subRootNode = subTreeModel.getRootNode();
						List<INode> subNodes = new ArrayList<>();
						for(int i=0; i<subRootNode.getChildCount(); i++) {
							subNodes.add(subRootNode.getChildAt(i));
						}
						for(INode subNode:subNodes) {
							courseNodeTreeNode.addChild(subNode);
						}
					}
				}
			}

		}, course.getRunStructure().getRootNode(), true).visitAll();
		return gtm;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == courseTree) {
			if(event instanceof TreeEvent) {
				TreeEvent te = (TreeEvent)event;
				if(MenuTree.COMMAND_TREENODE_CLICKED.equals(te.getCommand())) {
					String ident = te.getNodeId();
					TreeNode selectedNode = courseTree.getTreeModel().getNodeById(ident);
					doSelectNode(ureq, selectedNode);
					initTools();
				}
			}
		}
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		if(entry.getOLATResourceable() != null && entry.getOLATResourceable().getResourceableTypeName() != null) {
			String nodeId = entry.getOLATResourceable().getResourceableTypeName();
			TreeNode nclr = courseTree.getTreeModel().getNodeById(nodeId);
			if(nclr != null) {
				String selNodeId = nclr.getIdent();
				courseTree.setSelectedNodeId(selNodeId);
				doSelectNode(ureq, nclr);
				initTools();
			}
		}
	}

	private void doSelectNode(UserRequest ureq, TreeNode selectedNode) {
		removeAsListenerAndDispose(currentCtrl);
		currentCtrl = null;
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstanceWithoutCheck(selectedNode.getIdent(), 0l), null);
		if(selectedNode instanceof StatisticResourceNode) {
			StatisticResourceNode node = (StatisticResourceNode)selectedNode;
			node.getCourseNode().updateModuleConfigDefaults(false, node.getCourseNode().getParent());
			currentCtrl = node.getResult().getController(ureq, swControl, stackPanel, node);
		} else {
			StatisticResourceNode node = getStatisticNodeInParentLine(selectedNode);
			if(node != null) {
				node.getCourseNode().updateModuleConfigDefaults(false, node.getCourseNode().getParent());
				currentCtrl = node.getResult().getController(ureq, swControl, stackPanel, selectedNode);
			}
		}
		
		if(currentCtrl != null) {
			listenTo(currentCtrl);
			layoutCtr.setCol3(currentCtrl.getInitialComponent());
		} else {
			layoutCtr.setCol3(new Panel("empty"));
		}
		
		// also select in GUI
		courseTree.setSelectedNode(selectedNode);
	}
	
	private StatisticResourceNode getStatisticNodeInParentLine(TreeNode selectedNode) {
		INode parent = selectedNode.getParent();
		for( ; parent!= null && !(parent instanceof StatisticResourceNode); ) {
			parent = parent.getParent();
		}
		if(parent instanceof StatisticResourceNode) {
			return (StatisticResourceNode)parent;
		}
		return null;
	}
}
