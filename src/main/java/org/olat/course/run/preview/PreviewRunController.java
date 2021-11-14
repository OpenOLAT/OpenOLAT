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

package org.olat.course.run.preview;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.id.IdentityEnvironment;
import org.olat.course.condition.Condition;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.RunMainController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.navigation.NavigationHandler;
import org.olat.course.run.navigation.NodeClickedRef;
import org.olat.course.run.scoring.NoEvaluationAccounting;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.area.BGArea;

import de.bps.course.nodes.CourseNodePasswordManagerImpl;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class PreviewRunController extends MainLayoutBasicController {
	private MenuTree luTree;
	private Panel content;

	private final NavigationHandler navHandler;
	private final UserCourseEnvironment uce;
	private CourseNode currentCourseNode;

	private Controller currentNodeController; // the currently open node
	private VelocityContainer detail;
	private TreeModel treeModel;
	private Link configButton;

	/**
	 * Constructor for the run main controller
	 * 
	 * @param ureq The user request
	 * @param wControl The current window controller
	 * @param identEnv
	 * @param cenv
	 */
	public PreviewRunController(UserRequest ureq, WindowControl wControl, IdentityEnvironment identEnv, CourseEnvironment cenv, String role, LayoutMain3ColsController previewLayoutCtr) { 
		super(ureq, wControl);
		// set up the components
		luTree = new MenuTree(null, "luTreeRun", this);
		luTree.setScrollTopOnClick(true);

		// build up the running structure for this user
		ScoreAccounting noScoreAccounting = new NoEvaluationAccounting();
		uce = new UserCourseEnvironmentImpl(identEnv, cenv, noScoreAccounting);
		navHandler = new NavigationHandler(uce, null, true);
		
		// evaluate scoring
		uce.getScoreAccounting().evaluateAll();
		
		// build menu (treemodel)
		NodeClickedRef nclr = navHandler.evaluateJumpToCourseNode(ureq, getWindowControl(), null, this, null);
		if (!nclr.isVisible()) {
			getWindowControl().setWarning(translate("rootnode.invisible"));
			VelocityContainer noaccess = createVelocityContainer("noaccess");		
			configButton = LinkFactory.createButton("command.config", noaccess, this);
			previewLayoutCtr.setCol3(noaccess);
			return;
		}
			
		treeModel = nclr.getTreeModel();
		luTree.setTreeModel(treeModel);
		previewLayoutCtr.setCol1(luTree);
		
		detail = createVelocityContainer("detail");
		
		configButton = LinkFactory.createButton("command.config", detail, this);
		
		content = new Panel("building_block_content");
		currentNodeController = nclr.getRunController();
		currentCourseNode = nclr.getCalledCourseNode();
		currentNodeController.addControllerListener(this);
		content.setContent(currentNodeController.getInitialComponent());
		detail.put("content", content);
		detail.contextPut("time", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ureq.getLocale())
				.format(new Date(uce.getCourseEnvironment().getCurrentTimeMillis())));
		CourseGroupManager cgm = uce.getCourseEnvironment().getCourseGroupManager();
		detail.contextPut("groups", assembleNamesFromGroupList(cgm.getAllBusinessGroups()));
		detail.contextPut("areas", assembleNamesFromAreaList(cgm.getAllAreas()));
		detail.contextPut("asRole",role);
		previewLayoutCtr.setCol3(detail);
	}

	private String assembleNamesFromGroupList(List<BusinessGroup> groups) {
		StringBuilder sb = new StringBuilder();
		for (BusinessGroup group:groups) {
			if(sb.length() > 0) sb.append(',');
			sb.append(group.getName());
		}
		return sb.toString();
	}
	
	private String assembleNamesFromAreaList(List<BGArea> areas) {
		StringBuilder sb = new StringBuilder();
		for (BGArea area:areas) {
			if(sb.length() > 0) sb.append(',');
			sb.append(area.getName());
		}
		return sb.toString();
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == luTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeEvent tev = (TreeEvent)event;
				
				// goto node:
				// after a click in the tree, evaluate the model anew, set the tree
				// model anew, and set the selection of the tree again
				NodeClickedRef nclr = navHandler.evaluateJumpToTreeNode(ureq, getWindowControl(), treeModel, tev, this, null, currentNodeController, false);
				if (!nclr.isVisible()) {
					getWindowControl().setWarning(translate("warn.notvisible"));
					return;
				}
				if (nclr.isHandledBySubTreeModelListener()) {
					//not used:
					return;
				}
				
				// set the new treemodel
				treeModel = nclr.getTreeModel();
				luTree.setTreeModel(treeModel);
				
				// set the new tree selection
				luTree.setSelectedNodeId(nclr.getSelectedNodeId());
				luTree.setOpenNodeIds(nclr.getOpenNodeIds());

				// get the controller (in this case it is a preview controller). Dispose only if not already disposed in navHandler.evaluateJumpToTreeNode()
				if(nclr.getRunController() != null) {
					if (currentNodeController != null && !currentNodeController.isDisposed()  && !navHandler.isListening(currentNodeController)){
						currentNodeController.dispose();
					}
					currentNodeController = nclr.getRunController();
				}
				
				CourseNode cn = nclr.getCalledCourseNode();
				if(cn != null) {
					Condition c = cn.getPreConditionVisibility();
					String visibilityExpr = (c.getConditionExpression() == null? translate("details.visibility.none") : c.getConditionExpression());
					detail.contextPut("visibilityExpr", visibilityExpr);
					detail.contextPut("coursenode", cn);
					currentCourseNode = cn;
				}

				Component nodeComp = currentNodeController.getInitialComponent();
				content.setContent(nodeComp);
			}
		} else if (source == configButton){
			fireEvent(ureq, new Event("command.config"));
		}
	}


	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == currentNodeController) {
			if (event instanceof OlatCmdEvent) {
				OlatCmdEvent oe = (OlatCmdEvent) event;
				String cmd = oe.getCommand();
				if (cmd.equals(OlatCmdEvent.GOTONODE_CMD)) {
					String subcmd = oe.getSubcommand(); // "69680861018558";
					CourseNode identNode = uce.getCourseEnvironment().getRunStructure().getNode(subcmd);
					updateTreeAndContent(ureq, identNode);
					oe.accept();
				}
			} else if (RunMainController.REBUILD.equals(event.getCommand())) {
				updateTreeAndContent(ureq, currentCourseNode);
			}
		}
	}

	/**
	 * side-effecty to content and luTree
	 * 
	 * @param ureq
	 * @param calledCourseNode the node to jump to, if null = jump to root node
	 * @return true if the node jumped to is visible
	 */
	private boolean updateTreeAndContent(UserRequest ureq, CourseNode calledCourseNode) {
		// build menu (treemodel)
		NodeClickedRef nclr = navHandler.evaluateJumpToCourseNode(ureq, getWindowControl(), calledCourseNode, this, null);
		if (!nclr.isVisible()) {
			// if not root -> fallback to root. e.g. when a direct node jump fails
			if (calledCourseNode != null) {
				nclr = navHandler.evaluateJumpToCourseNode(ureq, getWindowControl(), null, this, null);
			}
			if (!nclr.isVisible()) {
				getWindowControl().setWarning(translate("msg.nodenotavailableanymore"));
				content.setContent(null);
				luTree.setTreeModel(new GenericTreeModel());
				return false;
			}
		}

		treeModel = nclr.getTreeModel();
		luTree.setTreeModel(treeModel);
		String selNodeId = nclr.getSelectedNodeId();
		luTree.setSelectedNodeId(selNodeId);
		luTree.setOpenNodeIds(nclr.getOpenNodeIds());

		// dispose old node controller
		if (currentNodeController != null && !currentNodeController.isDisposed() && !navHandler.isListening(currentNodeController)) {
			currentNodeController.dispose();
		}
		currentNodeController = nclr.getRunController();
		content.setContent(currentNodeController.getInitialComponent());
		return true;
	}

	@Override
	protected void doDispose() {
		if (currentNodeController != null) {
			currentNodeController.dispose();
			currentNodeController = null;
		}
		CourseNodePasswordManagerImpl.getInstance().removeAnswerContainerFromCache(uce.getIdentityEnvironment().getIdentity());
		navHandler.dispose();
        super.doDispose();
	}
}