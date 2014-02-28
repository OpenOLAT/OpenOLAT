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
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StatisticCourseNodesController extends BasicController implements Activateable2 {
	private final MenuTree courseTree;
	private final LayoutMain3ColsController layoutCtr;
	private Controller currentCtrl;
	
	private final StatisticResourceOption options;
	
	public StatisticCourseNodesController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseRe, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);

		options = new StatisticResourceOption();
		
		boolean admin = userCourseEnv.isAdmin();
		boolean coach = userCourseEnv.isCoach();
		if(coach && !admin) {
			UserCourseEnvironmentImpl userCourseEnvImpl = (UserCourseEnvironmentImpl)userCourseEnv;
			List<Group> coachedGroups = userCourseEnvImpl.getCoachedBaseGroups(true, true);
			if(coachedGroups == null || coachedGroups.isEmpty()) {
				options.setParticipantsGroups(coachedGroups);
			}
		}

		courseTree = new MenuTree("assessmentStatisticsTree");
		courseTree.setTreeModel(buildTreeModel(ureq, userCourseEnv));
		courseTree.setRootVisible(false);
		courseTree.addListener(this);
		
		Panel empty = new Panel("splashScreen");
		layoutCtr = new LayoutMain3ColsController(ureq, wControl, courseTree, null, empty, null);
		listenTo(layoutCtr);
		putInitialPanel(layoutCtr.getInitialComponent());
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
				StatisticResourceResult result = courseNode.createStatisticNodeResult(ureq, getWindowControl(), userCourseEnv, options);
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
			}
		}
	}

	private void doSelectNode(UserRequest ureq, TreeNode selectedNode) {
		removeAsListenerAndDispose(currentCtrl);
		currentCtrl = null;
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(selectedNode.getIdent(), 0l), null);
		if(selectedNode instanceof StatisticResourceNode) {
			StatisticResourceNode node = (StatisticResourceNode)selectedNode;
			currentCtrl = node.getResult().getController(ureq, swControl, node);
		} else {
			StatisticResourceNode node = getStatisticNodeInParentLine(selectedNode);
			currentCtrl = node.getResult().getController(ureq, swControl, selectedNode);
		}
		
		if(currentCtrl != null) {
			listenTo(currentCtrl);
			layoutCtr.setCol3(currentCtrl.getInitialComponent());
		} else {
			layoutCtr.setCol3(new Panel("empty"));
		}
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
