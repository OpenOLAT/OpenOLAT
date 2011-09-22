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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.course.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tree.SelectionTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.course.tree.InsertTreeModel;
import org.olat.course.tree.TreePosition;
import org.olat.util.logging.activity.LoggingResourceable;
/**
 * 
 * Description:<br>
 * TODO: guido Class Description for MoveCopySubtreeController
 * 
 */
public class MoveCopySubtreeController extends BasicController {
	
	private static final String LOG_COURSENODE_COPIED = "COURSENODE_COPIED";
	private static final String LOG_COURSENODE_MOVED = "COURSENODE_MOVED";
	
	private CourseEditorTreeNode moveCopyFrom;
	private boolean copy;

	private SelectionTree insertTree;
	private InsertTreeModel insertModel;
	private String copyNodeId = null;
	private OLATResourceable ores;

	public MoveCopySubtreeController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, CourseEditorTreeNode moveCopyFrom, boolean copy) {
		super(ureq, wControl);
		this.ores = ores;
		this.moveCopyFrom = moveCopyFrom;
		this.copy = copy;

		ICourse course = CourseFactory.getCourseEditSession(ores.getResourceableId());
		addLoggingResourceable(LoggingResourceable.wrap(course));
		addLoggingResourceable(LoggingResourceable.wrap(moveCopyFrom.getCourseNode()));

		insertTree = new SelectionTree("copy_node_selection", getTranslator());
		insertTree.setFormButtonKey("insertAtSelectedTreepos");
		insertTree.addListener(this);
		insertModel = new InsertTreeModel(course.getEditorTreeModel());
		insertTree.setTreeModel(insertModel);		
		
		VelocityContainer mainVC = createVelocityContainer("moveCopyNode");
		
		if(insertModel.totalNodeCount() > CourseModule.getCourseNodeLimit()){
			String msg = getTranslator().translate("warning.containsXXXormore.nodes",
					new String[]{String.valueOf(insertModel.totalNodeCount()),String.valueOf(CourseModule.getCourseNodeLimit()+1)});
			Controller tmp = MessageUIFactory.createWarnMessage(ureq, wControl, null, msg);
			listenTo(tmp);
			mainVC.put("nodelimitexceededwarning", tmp.getInitialComponent());
		}
		
		mainVC.put("selection", insertTree);
		
		this.putInitialPanel(mainVC);
	}

	public void event(UserRequest ureq, Component source, Event event) {
		ICourse course = CourseFactory.getCourseEditSession(ores.getResourceableId());
		copyNodeId = null; // initialize copyNodeId with null because a new event happens and old value is invalid.
		if (source == insertTree) {
			TreeEvent te = (TreeEvent) event;
			if (te.getCommand().equals(TreeEvent.COMMAND_TREENODE_CLICKED)) {
				// user chose a position to insert a new node
				String nodeId = te.getNodeId();
				TreePosition tp = insertModel.getTreePosition(nodeId);
				CourseNode selectedNode = insertModel.getCourseNode(tp.getParentTreeNode());
				CourseEditorTreeNode insertParent = course.getEditorTreeModel().getCourseEditorNodeById(selectedNode.getIdent());

				// check if insert position is within the to-be-copied tree
				if (checkIfIsChild(insertParent, moveCopyFrom)) {					
					this.showError("movecopynode.error.overlap");
					fireEvent(ureq, Event.CANCELLED_EVENT);
					return;
				}

				int insertPos = tp.getChildpos();
				if (copy) { // do a copy
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
					TreeVisitor tv = new TreeVisitor( new Visitor() {
						public void visit(INode node) {
							CourseEditorTreeNode cetn = (CourseEditorTreeNode)node;
							cetn.setDirty(true);
						}
					},moveCopyFrom,true);
					tv.visitAll();					
					CourseFactory.saveCourseEditorTreeModel(course.getResourceableId()); // TODO: pb: Review : Add by chg to FIX OLAT-1662
					this.showInfo("movecopynode.info.condmoved");

					ThreadLocalUserActivityLogger.log(CourseLoggingAction.COURSE_EDITOR_NODE_MOVED, getClass());
					fireEvent(ureq, Event.DONE_EVENT);
				}
			} else {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		}
	}

	private void recursiveCopy(CourseEditorTreeNode copyFrom2, CourseEditorTreeNode insertParent, int pos, boolean firstIteration, ICourse course) {		
		// create copy of course node
		CourseNode copyOfNode = copyFrom2.getCourseNode().createInstanceForCopy(firstIteration);
		copyNodeId = copyOfNode.getIdent();
		// Insert at desired position		
		course.getEditorTreeModel().insertCourseNodeAt(copyOfNode, insertParent.getCourseNode(), pos);
		CourseEditorTreeNode insertedEditorTreeNode = course.getEditorTreeModel().getCourseEditorNodeById(copyOfNode.getIdent());
		for (int i = 0; i < copyFrom2.getChildCount(); i++) {
			recursiveCopy(course.getEditorTreeModel().getCourseEditorNodeById(copyFrom2.getChildAt(i).getIdent()), insertedEditorTreeNode, i, false, course);
		}
	}

	/**
	 * Check if prospectChild is a child of sourceTree.
	 * 
	 * @param prospectChild
	 * @param sourceTree
	 * @return
	 */
	private boolean checkIfIsChild(CourseEditorTreeNode prospectChild, CourseEditorTreeNode sourceTree) {
		// FIXME:ms:b would it be simpler to check the parents?
		// INode par;
		// for (par = prospectChild.getParent(); par != null && par != sourceTree;
		// par = par.getParent());
		// return (par == sourceTree);
		ICourse course = CourseFactory.getCourseEditSession(ores.getResourceableId());
		if (sourceTree.getIdent().equals(prospectChild.getIdent())) return true;
		for (int i = 0; i < sourceTree.getChildCount(); i++) {
			INode child = sourceTree.getChildAt(i);
			if (checkIfIsChild(prospectChild, course.getEditorTreeModel().getCourseEditorNodeById(child.getIdent()))) return true;
		}
		return false;
	}

	protected void doDispose() {
    // nothing to dispose
	}

	/**
	 * Returns node-id of a new copied node.
	 * @return Returns null when no copy-workflow happens. 
	 */
	public String getCopyNodeId() {
		return copyNodeId;
	}

}
