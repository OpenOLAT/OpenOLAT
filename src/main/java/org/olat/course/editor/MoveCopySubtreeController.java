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

package org.olat.course.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.InsertEvent;
import org.olat.core.gui.components.tree.InsertionPoint.Position;
import org.olat.core.gui.components.tree.InsertionTreeModel;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.tree.TreePosition;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.tree.TreeHelper;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * @author: guido
 */
public class MoveCopySubtreeController extends BasicController {

	private MenuTree insertTree;
	private Link selectButton;
	private Link cancelButton;
	private InsertTreeModel insertModel;

	private boolean copy;
	private String copyNodeId;
	private OLATResourceable ores;
	private CourseEditorTreeNode moveCopyFrom;

	public MoveCopySubtreeController(UserRequest ureq, WindowControl wControl, OLATResourceable ores,
			CourseEditorTreeNode moveCopyFrom, boolean copy) {
		super(ureq, wControl);
		this.ores = ores;
		this.moveCopyFrom = moveCopyFrom;
		this.copy = copy;

		ICourse course = CourseFactory.getCourseEditSession(ores.getResourceableId());
		addLoggingResourceable(LoggingResourceable.wrap(course));
		addLoggingResourceable(LoggingResourceable.wrap(moveCopyFrom.getCourseNode()));

		insertTree = new MenuTree(null, "copy_node_selection", this);
		insertTree.enableInsertTool(true);
		insertModel = new InsertTreeModel(course.getEditorTreeModel().getRootNode(), moveCopyFrom);
		insertTree.setTreeModel(insertModel);		
		
		VelocityContainer mainVC = createVelocityContainer("moveCopyNode");
		
		selectButton = LinkFactory.createButton("insertAtSelectedTreepos", mainVC, this);
		selectButton.setCustomEnabledLinkCSS("btn btn-primary");
		selectButton.setCustomDisabledLinkCSS("btn btn-default");
		selectButton.setEnabled(false);
		cancelButton = LinkFactory.createButton("cancel", mainVC, this);
		
		int numOfNodes = TreeHelper.totalNodeCount(insertModel.getRootNode());
		if(numOfNodes > CourseModule.getCourseNodeLimit()){
			String msg = getTranslator().translate("warning.containsXXXormore.nodes",
					new String[]{String.valueOf(numOfNodes),String.valueOf(CourseModule.getCourseNodeLimit()+1)});
			Controller tmp = MessageUIFactory.createWarnMessage(ureq, wControl, null, msg);
			listenTo(tmp);
			mainVC.put("nodelimitexceededwarning", tmp.getInitialComponent());
		}
		
		mainVC.put("selection", insertTree);
		putInitialPanel(mainVC);
	}

	/**
	 * Returns node-id of a new copied node.
	 * @return Returns null when no copy-workflow happens. 
	 */
	public String getCopyNodeId() {
		return copyNodeId;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == cancelButton) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if(source == selectButton) {
			TreePosition tp = insertTree.getInsertionPosition();
			if(tp != null) {
				doInsert(ureq, tp);
			}
		} else if(event instanceof InsertEvent) {
			boolean canSelect = insertTree.getInsertionPoint() != null;
			selectButton.setEnabled(canSelect);
		}
	}
	
	private void doInsert(UserRequest ureq, TreePosition tp) {
		ICourse course = CourseFactory.getCourseEditSession(ores.getResourceableId());
		
		int insertPos = tp.getChildpos();
		CourseNode selectedNode = getCourseNode(tp.getParentTreeNode());
		CourseEditorTreeNode insertParent = course.getEditorTreeModel().getCourseEditorNodeById(selectedNode.getIdent());

		// check if insert position is within the to-be-copied tree
		if (course.getEditorTreeModel().checkIfIsChild(insertParent, moveCopyFrom)) {					
			showError("movecopynode.error.overlap");
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if (copy) { // do a copy
			// copy subtree and save model
			recursiveCopy(moveCopyFrom, insertParent, insertPos, true, CourseFactory.getCourseEditSession(ores.getResourceableId()));					
			CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());

			ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_NODE_COPIED, getClass());
			fireEvent(ureq, Event.DONE_EVENT);
		} else { // move only
			if (insertParent.getIdent().equals(moveCopyFrom.getParent().getIdent())) {
				// same parent, adjust insertPos
				if (insertPos > moveCopyFrom.getPosition()) insertPos--;
			}
			insertParent.insert(moveCopyFrom, insertPos);

			moveCopyFrom.setDirty(true);
			//mark subtree as dirty
			TreeVisitor tv = new TreeVisitor(node -> {
				CourseEditorTreeNode cetn = (CourseEditorTreeNode)node;
				cetn.setDirty(true);
			}, moveCopyFrom, true);
			tv.visitAll();					
			CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());

			ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_NODE_MOVED, getClass());
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
	
	private CourseNode getCourseNode(TreeNode tn) {
		CourseEditorTreeNode ctn = (CourseEditorTreeNode) tn;
		return ctn.getCourseNode();
	}

	private void recursiveCopy(CourseEditorTreeNode copyFrom2, CourseEditorTreeNode insertParent, int pos, boolean firstIteration, ICourse course) {		
		// create copy of course node
		CourseNode copyOfNode = copyFrom2.getCourseNode().createInstanceForCopy(firstIteration, course, getIdentity());
		copyNodeId = copyOfNode.getIdent();
		// Insert at desired position		
		course.getEditorTreeModel().insertCourseNodeAt(copyOfNode, insertParent.getCourseNode(), pos);
		CourseEditorTreeNode insertedEditorTreeNode = course.getEditorTreeModel().getCourseEditorNodeById(copyOfNode.getIdent());
		for (int i = 0; i < copyFrom2.getChildCount(); i++) {
			recursiveCopy(course.getEditorTreeModel().getCourseEditorNodeById(copyFrom2.getChildAt(i).getIdent()), insertedEditorTreeNode, i, false, course);
		}
	}
	
	private static class InsertTreeModel extends GenericTreeModel implements InsertionTreeModel {

		private static final long serialVersionUID = -3079238081023850884L;
		private CourseEditorTreeNode source;
		
		public InsertTreeModel(TreeNode rootNode, CourseEditorTreeNode source) {
			this.setRootNode(rootNode);
			this.source = source;
		}

		@Override
		public boolean isSource(TreeNode node) {
			return source == node;
		}

		@Override
		public Position[] getInsertionPosition(TreeNode node) {
			Position[] positions;
			if(source == node) {
				positions = new Position[0];
			} else if(getRootNode() == node) {
				positions = new Position[] { Position.under };
			} else if(TreeHelper.isInParentLine(node, source)) {
				positions = new Position[0];
			} else {
				positions = new Position[] { Position.up, Position.down, Position.under };
			}
			return positions;
		}
	}
}