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

import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.util.nodes.INode;
import org.olat.course.editor.StatusDescription;
import org.olat.course.learningpath.model.ModuleLearningPathConfigs;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 */
public class CourseEditorTreeNode extends GenericTreeNode {

	private static final long serialVersionUID = 3324118582289106497L;

	/*
	 * The course node... Important: coursenode's structure is not updated!
	 */
	private CourseNode cn;

	/*
	 * Status flags
	 */
	private boolean dirty;
	private boolean deleted;
	private boolean newnode;

	public CourseEditorTreeNode(CourseNode cn) {
		this.cn = cn;
		setIdent(cn.getIdent());

		dirty = false;
		deleted = false;
		newnode = false;
	}

	public CourseEditorTreeNode(CourseEditorTreeNode cetn) {
		cn = cetn.cn;
		setIdent(cetn.getIdent());

		dirty = cetn.dirty;
		deleted = cetn.deleted;
		newnode = cetn.newnode;
	}

	@Override
	public String getTitle() {
		return cn.getShortTitle();
	}

	@Override
	public void setTitle(String title) {
		throw new UnsupportedOperationException("title is given by associated coursenode's shorttitle");
	}

	@Override
	public String getAltText() {
		return cn.getLongTitle() + " (id:" + getIdent() + ")";
	}

	@Override
	public void setAltText(String altText) {
		throw new UnsupportedOperationException("alttext is given by associated coursenode's longtitle");
	}
	
	@Override
	public String getCssClass() {
		if (deleted) {
			return "o_deleted";
		}
		return null;
	}

	@Override
	public String getIconCssClass() {
		if (getParent() == null) {
			// Spacial case for root node
			return "o_CourseModule_icon";
		} else {
			CourseNodeConfiguration cnConfig = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(cn.getType());
			return cnConfig.getIconCSSClass();
		}
	}

	@Override
	public String getIconDecorator1CssClass() {
		StatusDescription sd = cn.isConfigValid();
		
		String cssClass = null;
		if (deleted) {
			cssClass = "o_middel";
		} else if (sd.isError()) {
			cssClass = "o_miderr";
		} else if (!sd.isError() && dirty) {
			cssClass = "o_midpub";
		}
		return cssClass;
	}

	@Override
	public String getIconDecorator3CssClass() {
		//do not show errors if marked for deletion
		if(deleted) return null;
		//
		StatusDescription sd = cn.isConfigValid();
		//warnings only
		if (sd.isWarning()) return "o_midwarn";
		return null;
	}

	@Override
	public String getIconDecorator2CssClass() {
		//do not show errors if marked for deletion
		String cssClass = null;
		if(deleted) {
			cssClass = null;
		} else if(cn.getConditionExpressions().size() > 0) {
			cssClass = "o_midlock";
		} else if ((cn.getModuleConfiguration().has(ModuleLearningPathConfigs.CONFIG_KEY_EXCEPTIONAL_OBLIGATIONS))) {
			// Checking the key in the module configuration is a cheat of checking the LerningPathConfig.getExceptionalObligation()
			cssClass = "o_midlpexob";
		}
		return cssClass;
	}

	/**
	 * @param pos
	 * @return the CourseEditorTreeNode which is the child at position pos
	 */
	public CourseEditorTreeNode getCourseEditorTreeNodeChildAt(int pos) {
		return (CourseEditorTreeNode) getChildAt(pos);
	}

	/**
	 * @return the attached course node
	 */
	public CourseNode getCourseNode() {
		return cn;
	}

	public void moveUpInChildlist() {
		CourseEditorTreeNode parentNode = (CourseEditorTreeNode) getParent();
		if (parentNode == null) return;
		int pos = getPosition();
		if (pos == 0) return; // upmost children cannot be moved up further
		removeFromParent();
		parentNode.insert(this, pos - 1);
		setDirty(true);
	}

	public void moveDownInChildlist() {
		CourseEditorTreeNode parentNode = (CourseEditorTreeNode) getParent();
		if (parentNode == null) return;
		int pos = getPosition();
		if (pos == parentNode.getChildCount() - 1) return; // latest children
		// cannot be moved down
		// further
		removeFromParent();
		parentNode.insert(this, pos + 1);
		setDirty(true);
	}

	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean b) {
		dirty = b;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean b) {
		deleted = b;
	}

	public boolean isNewnode() {
		return newnode;
	}

	public void setNewnode(boolean b) {
		newnode = b;
	}

	@Override
	public String toString() {
		return "editorId: " + getIdent() + ", " + cn.toString();
	}

	/**
	 * @return true if this editornode has publishable changes
	 */
	public boolean hasPublishableChanges() { 
		boolean configIsValid = !cn.isConfigValid().isError();
		if(configIsValid && !isDeleted() && getParent()!=null) {
			/*
			 * if my config is valid I have to check if all parents up to the root are
			 * also valid. But only if I am not deleted and I am not the root.
			 */
			configIsValid = isAllParentsConfigValid((CourseEditorTreeNode)getParent());
		}
		boolean hasDelta = (isDirty() || isNewnode());
		return (configIsValid && hasDelta) || isDeleted();
	}
	/**
	 * recursively ask all nodes towards the root if their config is valid. This
	 * is a helper method to decide if a course node has publishable changes.<br>
	 * If one of the course nodes along the path has a config error, all the
	 * childrens publishable changes are no longer publishable. A more relaxed
	 * version would fail only if the config error happens in a new course node.
	 * 
	 * @param child
	 * @return <code>true</code> if all course node configurations are valid,
	 *         <code>false</code> if at least one course node has a config
	 *         error.
	 */
	private boolean isAllParentsConfigValid(CourseEditorTreeNode child) {
		INode parent = child.getParent();
		if(parent==null) {
			return !child.getCourseNode().isConfigValid().isError();
		}else {
			boolean myConfigIsValid = !child.getCourseNode().isConfigValid().isError();
			if(myConfigIsValid) {
				return isAllParentsConfigValid((CourseEditorTreeNode)parent);
			}else {
				return false;
			}
		}
	}
}