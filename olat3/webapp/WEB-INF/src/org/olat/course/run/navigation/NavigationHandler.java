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

package org.olat.course.run.navigation;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Formatter;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.TreeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description: <br>
 * TODO: Felix Jost Class Description for NavigationHandler
 * Initial Date: 19.01.2005 <br>
 * @author Felix Jost
 */
public class NavigationHandler {
	OLog log = Tracing.createLoggerFor(NavigationHandler.class);
	
	private static final String LOG_NODE_ACCESS = "NODE_ACCESS";
	private static final String LOG_NODE_NO_ACCESS = "NODE_NO_ACCESS";

	private final UserCourseEnvironment userCourseEnv;
	private final boolean previewMode;

	// remember so subsequent click to a subtreemodel's node has a handler
	private ControllerEventListener subtreemodelListener = null;

	/**
	 * @param userCourseEnv
	 * @param previewMode
	 */
	public NavigationHandler(UserCourseEnvironment userCourseEnv, boolean previewMode) {
		this.userCourseEnv = userCourseEnv;
		this.previewMode = previewMode;
	}

	/**
	 * to be called upon entering a course. <br>
	 * 
	 * @param ureq
	 * @param wControl
	 * @return NodeClickedRef
	 * @param calledCourseNode the coursenode to jump to; if null, the root
	 *          coursenode is selected
	 * @param listeningController
	 */
	public NodeClickedRef evaluateJumpToCourseNode(UserRequest ureq, WindowControl wControl, CourseNode calledCourseNode,
			ControllerEventListener listeningController, String nodecmd) {
		CourseNode cn;
		if (calledCourseNode == null) {
			// indicate to jump to root course node
			cn = userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();
		} else {
			cn = calledCourseNode;
		}
		return doEvaluateJumpTo(ureq, wControl, cn, listeningController, nodecmd);
	}

	/**
	 * to be called when the users clickes on a node when in the course
	 * 
	 * @param ureq
	 * @param wControl
	 * @param treeModel
	 * @param treeEvent
	 * @param listeningController
	 * @param nodecmd null or a subcmd which activates a node-specific view (e.g. opens a certain uri in a contentpackaging- buildingblock)
	 * @return the NodeClickedRef
	 * @return currentNodeController the current node controller that will be dispose before creating the new one
	 */
	public NodeClickedRef evaluateJumpToTreeNode(UserRequest ureq, WindowControl wControl, TreeModel treeModel, TreeEvent treeEvent,
			ControllerEventListener listeningController, String nodecmd, Controller currentNodeController) {
		NodeClickedRef ncr;
		String treeNodeId = treeEvent.getNodeId();
		TreeNode selTN = treeModel.getNodeById(treeNodeId);
		if (selTN == null) throw new AssertException("no treenode found:" + treeNodeId);

		// check if appropriate for subtreemodelhandler
		Object userObject = selTN.getUserObject();
		if (!(userObject instanceof NodeEvaluation)) {
			// yes, appropriate
			if (subtreemodelListener == null) throw new AssertException("no handler for subtreemodelcall!");
			if (log.isDebug()){
				log.debug("delegating to handler: treeNodeId = " + treeNodeId);
			}
			// null as controller source since we are not a controller
			subtreemodelListener.dispatchEvent(ureq, null, treeEvent);
			// no node construction result indicates handled
			ncr = new NodeClickedRef(null, true, null, null, null);
		} else {
			// normal dispatching to a coursenode.
			// get the courseNode that was called
			NodeEvaluation prevEval = (NodeEvaluation) selTN.getUserObject();
			if (!prevEval.isVisible()) throw new AssertException("clicked on a node which is not visible: treenode=" + selTN.getIdent() + ", "
					+ selTN.getTitle());
			CourseNode calledCourseNode = prevEval.getCourseNode();
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(calledCourseNode));
			// dispose old node controller before creating the NodeClickedRef which creates 
			// the new node controller. It is important that the old node controller is 
			// disposed before the new one to not get conflicts with cacheable mappers that
			// might be used in both controllers with the same ID (e.g. the course folder)
			if (currentNodeController != null) {
				currentNodeController.dispose();
			}
			ncr = doEvaluateJumpTo(ureq, wControl, calledCourseNode, listeningController, nodecmd);
		}
		return ncr;

	}

	private NodeClickedRef doEvaluateJumpTo(UserRequest ureq, WindowControl wControl, CourseNode courseNode,
			ControllerEventListener listeningController, String nodecmd) {
		NodeClickedRef nclr;
		if (log.isDebug()){
			log.debug("evaluateJumpTo courseNode = " + courseNode.getIdent() + ", " + courseNode.getShortName());
		}

		// build the new treemodel by evaluating the preconditions
		TreeEvaluation treeEval = new TreeEvaluation();
		GenericTreeModel treeModel = new GenericTreeModel();
		CourseNode rootCn = userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();
		NodeEvaluation rootNodeEval = rootCn.eval(userCourseEnv.getConditionInterpreter(), treeEval);
		TreeNode treeRoot = rootNodeEval.getTreeNode();
		treeModel.setRootNode(treeRoot);

		// find the treenode that corresponds to the node (!= selectedTreeNode since
		// we built the TreeModel anew in the meantime
		TreeNode newCalledTreeNode = treeEval.getCorrespondingTreeNode(courseNode);
		if (newCalledTreeNode == null) {
			// the clicked node is not visible anymore!
			// if the new calculated model does not contain the selected node anymore
			// (because of visibility changes of at least one of the ancestors
			// -> issue an user infomative msg
			// nclr: the new treemodel, not visible, no selected nodeid, no
			// calledcoursenode, no nodeconstructionresult
			nclr = new NodeClickedRef(treeModel, false, null, null, null);
		} else {
			// calculate the NodeClickedRef
			// 1. get the correct (new) nodeevaluation
			NodeEvaluation nodeEval = (NodeEvaluation) newCalledTreeNode.getUserObject();
			if (nodeEval.getCourseNode() != courseNode) throw new AssertException("error in structure");
			if (!nodeEval.isVisible()) throw new AssertException("node eval not visible!!");
			// 2. start with the current NodeEvaluation, evaluate overall accessiblity
			// per node bottom-up to see if all ancestors still grant access to the
			// desired node
			boolean mayAccessWholeTreeUp = mayAccessWholeTreeUp(nodeEval);
			String newSelectedNodeId = newCalledTreeNode.getIdent();
			if (!mayAccessWholeTreeUp) {
				// we cannot access the node anymore (since e.g. a time constraint
				// changed), so give a (per-node-configured) explanation why and what
				// the access conditions would be (a free form text, should be
				// nontechnical).
				// NOTE: we do not take into account what node caused the non-access by
				// being !isAtLeastOneAccessible, but always state the
				// NoAccessExplanation of the Node originally called by the user
				String explan = courseNode.getNoAccessExplanation();
				String sExplan = (explan == null ? "" : Formatter.formatLatexFormulas(explan));
				Controller controller = MessageUIFactory.createInfoMessage(ureq, wControl, null, sExplan);
				// write log information
				ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_NAVIGATION_NODE_NO_ACCESS, getClass(),
						LoggingResourceable.wrap(courseNode));
				NodeRunConstructionResult ncr = new NodeRunConstructionResult(controller, null, null, null);
				// nclr: the new treemodel, visible, selected nodeid, calledcoursenode,
				// nodeconstructionresult
				nclr = new NodeClickedRef(treeModel, true, newSelectedNodeId, courseNode, ncr);
			} else { // access ok
				// access the node, display its result in the right pane
				NodeRunConstructionResult ncr;
				
				// calculate the new businesscontext for the coursenode being called.	
				// type: class of node; key = node.getIdent;
				
				Class<CourseNode> oresC = CourseNode.class; // don't use the concrete instance since for the course: to jump to a coursenode with a given id is all there is to know
				Long oresK = new Long(Long.parseLong(courseNode.getIdent()));
				final OLATResourceable ores = OresHelper.createOLATResourceableInstance(oresC, oresK);
				
				//REVIEW:pb:this is responsible for building up the jumpable businesspath/REST URL kind of
				ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ores);
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ce, wControl);
				
				if (previewMode) {
					ncr = new NodeRunConstructionResult(courseNode.createPreviewController(ureq, bwControl, userCourseEnv, nodeEval));
				} else {
					ncr = courseNode.createNodeRunConstructionResult(ureq, bwControl, userCourseEnv, nodeEval, nodecmd);

					// remember as instance variable for next click
					subtreemodelListener = ncr.getSubTreeListener();
					if (subtreemodelListener != null) {
						addSubTreeModel(newCalledTreeNode, ncr.getSubTreeModel());
					}
				}

				// nclr: the new treemodel, visible, selected nodeid, calledcoursenode,
				// nodeconstructionresult
				nclr = new NodeClickedRef(treeModel, true, newSelectedNodeId, courseNode, ncr);
				// attach listener; we know we have a runcontroller here
				if (listeningController != null) {
					nclr.getRunController().addControllerListener(listeningController);
				}
				// write log information
				ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_NAVIGATION_NODE_ACCESS, getClass(),
						LoggingResourceable.wrap(courseNode));
			}
		}
		return nclr;
	}

	private void addSubTreeModel(TreeNode parent, TreeModel modelToAppend) {
		// ignore root and directly add children.
		// need to clone children so that are not detached from their original
		// parent (which is the cp treemodel)
		// parent.addChild(modelToAppend.getRootNode());
		TreeNode root = modelToAppend.getRootNode();
		int chdCnt = root.getChildCount();
		
		// full cloning of ETH webclass energie takes about 4/100 of a second
		for (int i = chdCnt; i > 0; i--) {
			TreeNode chd = (TreeNode) root.getChildAt(i-1);
			TreeNode chdc = (TreeNode) XStreamHelper.xstreamClone(chd);
			// always insert before already existing course building block children
			parent.insert(chdc, 0);
		}
	}
	/**
	 * @param ne
	 * @return
	 */
	public static boolean mayAccessWholeTreeUp(NodeEvaluation ne) {
		NodeEvaluation curNodeEval = ne;
		boolean mayAccess;
		do {
			mayAccess = curNodeEval.isAtLeastOneAccessible();
			curNodeEval = (NodeEvaluation) curNodeEval.getParent();
		} while (curNodeEval != null && mayAccess);
		// top reached or may not access node
		return mayAccess;
	}

}
