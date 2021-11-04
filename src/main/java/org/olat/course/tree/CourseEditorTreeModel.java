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

package org.olat.course.tree;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.tree.DnDTreeModel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ObjectCloner;
import org.olat.core.util.nodes.INode;
import org.olat.course.Structure;
import org.olat.course.nodes.CourseNode;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 * @Author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseEditorTreeModel extends GenericTreeModel implements DnDTreeModel {

	private static final long serialVersionUID = 7021325125068249016L;
	private long latestPublishTimestamp = -1;
	private long highestNodeId; // start at Long.MAX_VALUE - 1000000; if set to
															// zero -> meaning we read from an old
															// xml-structure which set it to zero, since it
															// did not exist
	private static final transient int CURRENTVERSION = 3;
	private int version;
	private static final Logger log = Tracing.createLoggerFor(CourseEditorTreeModel.class);

	/**
	 * 
	 */
	public CourseEditorTreeModel() {
		highestNodeId = Long.MAX_VALUE - 1000000;
		this.version = CURRENTVERSION;
	}

	/**
	 * @param nodeId
	 * @return the course node
	 */
	public CourseNode getCourseNode(String nodeId) {
		CourseEditorTreeNode ctn = (CourseEditorTreeNode)getNodeById(nodeId);
		return ctn == null ? null : ctn.getCourseNode();
	}

	/**
	 * @param newNode
	 * @param parentNode
	 * @param pos
	 */
	public CourseEditorTreeNode insertCourseNodeAt(CourseNode newNode, CourseNode parentNode, int pos) {
		// update editor tree model
		CourseEditorTreeNode ctnParent = (CourseEditorTreeNode) getNodeById(parentNode.getIdent());
		if (ctnParent == null) throw new AssertException("Corrupt CourseEditorTreeModel.");
		CourseEditorTreeNode newCetn = new CourseEditorTreeNode(newNode);
		newCetn.setNewnode(true);
		newCetn.setDirty(true);
		ctnParent.insert(newCetn, pos);
		log.debug("insertCourseNodeAt - nodeId: {}", newNode.getIdent());
		return newCetn;
	}
	
	/**
	 * append new course
	 * @param newNode
	 * @param parentNode
	 */
	public CourseEditorTreeNode addCourseNode(CourseNode newNode, CourseNode parentNode){
		//update editor tree model
		CourseEditorTreeNode ctnParent = (CourseEditorTreeNode) getNodeById(parentNode.getIdent());
		if (ctnParent == null) throw new AssertException("Corrupt CourseEditorTreeModel.");
		CourseEditorTreeNode newCetn = new CourseEditorTreeNode(newNode);
		newCetn.setNewnode(true);
		newCetn.setDirty(true);
		ctnParent.addChild(newCetn);
			log.debug("addCourseNode - nodeId: {}", newNode.getIdent());
		return newCetn;
}

	/**
	 * marks an couse node and all it's children as deleted
	 * @param courseNode
	 */
	public void markDeleted(CourseNode courseNode) {
		CourseEditorTreeNode cetNode = (CourseEditorTreeNode) getNodeById(courseNode.getIdent());
		if (cetNode == null) throw new AssertException("Corrupt CourseEditorTreeModel.");
		markDeleted(cetNode);
	}
	
	/**
	 * marks an couse node and all it's children as undeleted
	 * @param courseNode
	 */
	public void markUnDeleted(CourseNode courseNode) {
		CourseEditorTreeNode cetNode = (CourseEditorTreeNode) getNodeById(courseNode.getIdent());
		if (cetNode == null) throw new AssertException("Corrupt CourseEditorTreeModel.");
		markUnDeleted(cetNode);
	}
	

	private void markDeleted(CourseEditorTreeNode cetNode) {
		cetNode.setDeleted(true);
		cetNode.setDirty(true);
		if (cetNode.getChildCount() > 0) {
			for (int i = 0; i < cetNode.getChildCount(); i++) {
				markDeleted((CourseEditorTreeNode) cetNode.getChildAt(i));
			}
		}
	}
	
	public void markUnDeleted(CourseEditorTreeNode cetNode) {
		cetNode.setDeleted(false);
		cetNode.setDirty(true);
		if (cetNode.getChildCount() > 0) {
			for (int i = 0; i < cetNode.getChildCount(); i++) {
				markUnDeleted((CourseEditorTreeNode) cetNode.getChildAt(i));
			}
		}
	}

	/**
	 * @deprecated REVIEW:pb: no longer used? it is not referenced from java, and
	 *             also not found in velocity *.html
	 * @param courseNode
	 */
	@Deprecated
	public void removeCourseNode(CourseNode courseNode) {
		CourseEditorTreeNode cetNode = (CourseEditorTreeNode) getNodeById(courseNode.getIdent());
		if (cetNode == null) throw new AssertException("Corrupt CourseEditorTreeModel.");
		cetNode.removeFromParent();
	}

	/**
	 * @param nodeId
	 * @return null if not found, or the <code>CourseEditorTreeNode</code> with
	 *         the given nodeId
	 */
	public CourseEditorTreeNode getCourseEditorNodeById(String nodeId) {
		return (CourseEditorTreeNode) getNodeById(nodeId);
	}

	public CourseEditorTreeNode getCourseEditorNodeContaining(CourseNode cn) {
		String nodeId = cn.getIdent();
		return getCourseEditorNodeById(nodeId);
	}

	/**
	 * @param courseNode
	 */
	public void nodeConfigChanged(INode node) {
		CourseEditorTreeNode changedNode = (CourseEditorTreeNode) getNodeById(node.getIdent());
		if (changedNode == null) throw new AssertException("Corrupt course editor tree model.");
		changedNode.setDirty(true);
	}

	public long getLatestPublishTimestamp() {
		return latestPublishTimestamp;
	}

	/**
	 * @param latestPublishTimestamp The latestPublishTimestamp to set.
	 */
	public void setLatestPublishTimestamp(long latestPublishTimestamp) {
		this.latestPublishTimestamp = latestPublishTimestamp;
	}

	/**
	 * FIXME: use this method for node generation
	 * 
	 * @return the highest used node id so far
	 */
	public long getHighestNodeId() {
		return highestNodeId;
	}

	/**
	 * increments the highestnodeid: for the next new node in the editor. does not
	 * persist.
	 */
	public void incHighestNodeId() {
		highestNodeId++;
	}

	/**
	 * @return a deep clone of the current (run) structure of this editortreemodel
	 */
	public Structure createStructureForPreview() {
		CourseEditorTreeNode cetn = (CourseEditorTreeNode) getRootNode();
		CourseNode clone = buildUp(cetn);
		Structure structure = new Structure();
		structure.setRootNode(clone);
		return structure;
	}

	private CourseNode buildUp(CourseEditorTreeNode cetn) {
		CourseNode attachedNode = cetn.getCourseNode();
		// clone current
		CourseNode cloneCn = (CourseNode) ObjectCloner.deepCopy(attachedNode);
		// visit all children
		int chdCnt = cetn.getChildCount();
		for (int i = 0; i < chdCnt; i++) {
			CourseEditorTreeNode child = cetn.getCourseEditorTreeNodeChildAt(i);
			// only add if not deleted and configuration is valid
			if (!child.isDeleted() && !(child.getCourseNode().isConfigValid().isError())) {
				CourseNode res = buildUp(child);
				cloneCn.addChild(res);
			}
		}
		return cloneCn;
	}
	
	@Override
	public boolean isNodeDroppable(TreeNode node) {
		return true;
	}

	@Override
	public boolean isNodeDraggable(TreeNode node) {
		return !getRootNode().getIdent().equals(node.getIdent());
	}
	
	public boolean checkIfIsChild(CourseEditorTreeNode prospectChild, CourseEditorTreeNode sourceTree) {
		if (sourceTree.getIdent().equals(prospectChild.getIdent())) {
			return true;
		}
		for (int i = sourceTree.getChildCount(); i-->0; ) {
			INode child = sourceTree.getChildAt(i);
			if (checkIfIsChild(prospectChild, getCourseEditorNodeById(child.getIdent()))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return Returns the version.
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @param version The version to set.
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	public boolean isVersionUpToDate(){
		if (Integer.valueOf(version) == null || version < CURRENTVERSION) return false;
		return true;
	}
}
