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
package org.olat.ims.qti21.ui.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.Group;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
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
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.QTICourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.statistic.StatisticResourceNode;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.QTI21StatisticSearchParams;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 05.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21StatisticsToolController extends BasicController implements Activateable2, TooledController {

	private MenuTree courseTree;
	private Controller currentCtrl;
	private final TooledStackedPanel stackPanel;
	private LayoutMain3ColsController layoutCtr;

	private final ArchiveOptions options;
	private final RepositoryEntry testEntry;
	private final RepositoryEntry courseEntry;
	private final NodeAccessType nodeAccessType;
	private QTI21StatisticResourceResult result;
	private QTI21StatisticsSecurityCallback secCallback;

	private final QTI21StatisticSearchParams searchParams;
	
	@Autowired
	private QTI21Service qtiService;
	
	/**
	 * This is the statistics tool for the assessment tool.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param stackPanel
	 * @param courseEnv
	 * @param asOptions
	 * @param courseNode
	 */
	public QTI21StatisticsToolController(UserRequest ureq, WindowControl wControl, 
			TooledStackedPanel stackPanel, CourseEnvironment courseEnv,
			AssessmentToolOptions asOptions, QTICourseNode courseNode) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		options = new ArchiveOptions();
		options.setGroup(asOptions.getGroup());
		options.setIdentities(asOptions.getIdentities());
		courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		testEntry = courseNode.getReferencedRepositoryEntry();
		nodeAccessType = NodeAccessType.of(courseEnv);
		
		QTI21DeliveryOptions deliveryOptions = qtiService.getDeliveryOptions(testEntry);
		secCallback = new QTI21StatisticsSecurityCallback(asOptions.isAdmin(), asOptions.isAdmin() && deliveryOptions.isAllowAnonym(), false);
		
		searchParams = new QTI21StatisticSearchParams(testEntry, courseEntry, courseNode.getIdent());
		searchParams.setViewAnonymUsers(false);//In assessment tool, no user allowed
		searchParams.setViewMembers(false);
		searchParams.setViewNonMembers(false);
		searchParams.setViewFakeParticipants(false);
		
		if(asOptions.getGroup() != null) {// filter by business group
			List<Group> bGroups = Collections.singletonList(asOptions.getGroup().getBaseGroup());
			searchParams.setLimitToGroups(bGroups);
		} else if(asOptions.getAlternativeGroupsOfIdentities() != null) {
			searchParams.setLimitToGroups(asOptions.getAlternativeGroupsOfIdentities());
		} else if(asOptions.getIdentities() != null && !asOptions.getIdentities().isEmpty()) {
			searchParams.setLimitToIdentities(asOptions.getIdentities());
		} else {
			searchParams.setViewMembers(true);
			searchParams.setViewNonMembers(asOptions.isNonMembers());
		}
		
		result = new QTI21StatisticResourceResult(testEntry, courseEntry, courseNode, searchParams, secCallback);
		result.setWithFilter(false);
		
		GenericTreeModel treeModel = new GenericTreeModel();
		StatisticResourceNode rootTreeNode = new StatisticResourceNode(courseNode, result);
		treeModel.setRootNode(rootTreeNode);
		
		TreeNode subRootNode = result.getSubTreeModel().getRootNode();
		List<INode> subNodes = new ArrayList<>();
		for(int i=0; i<subRootNode.getChildCount(); i++) {
			subNodes.add(subRootNode.getChildAt(i));
		}
		for(INode subNode:subNodes) {
			rootTreeNode.addChild(subNode);
		}

		courseTree = new MenuTree("qti21StatisticsTree");
		courseTree.setTreeModel(treeModel);
		courseTree.addListener(this);
		
		layoutCtr = new LayoutMain3ColsController(ureq, wControl, courseTree, new Panel("empty"), null);
		listenTo(layoutCtr);
		putInitialPanel(layoutCtr.getInitialComponent());
		doSelectNode(ureq, courseTree.getTreeModel().getRootNode());
	}

	@Override
	public void initTools() {
		if(currentCtrl instanceof TooledController) {
			((TooledController)currentCtrl).initTools();
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

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(courseTree == source) {
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
	
	private void doSelectNode(UserRequest ureq, TreeNode selectedNode) {
		removeAsListenerAndDispose(currentCtrl);
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(selectedNode.getIdent(), 0l), null);
		if (selectedNode instanceof StatisticResourceNode) {
			StatisticResourceNode statisticResourceNode = (StatisticResourceNode) selectedNode;
			statisticResourceNode.getCourseNode().updateModuleConfigDefaults(false, statisticResourceNode.getCourseNode(), nodeAccessType);
		}
		currentCtrl = result.getController(ureq, swControl, stackPanel, selectedNode);
		if(currentCtrl != null) {
			listenTo(currentCtrl);
			layoutCtr.setCol3(currentCtrl.getInitialComponent());
		} else {
			layoutCtr.setCol3(new Panel("empty"));
		}
	}
}