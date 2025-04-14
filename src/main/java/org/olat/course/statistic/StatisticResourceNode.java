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

import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.util.nodes.INode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StatisticResourceNode extends GenericTreeNode {
	private static final long serialVersionUID = -1528483744004133623L;
	private final CourseNode courseNode;
	private final StatisticResourceResult result;
	
	private boolean opened;
	
	public StatisticResourceNode(CourseNode courseNode, StatisticResourceResult result) {
		super("sn" + courseNode.getIdent());
		this.result = result;
		this.courseNode = courseNode;
		
		setTitle(courseNode.getShortTitle());
		setAltText(courseNode.getLongTitle());
		String type = courseNode.getType();
		CourseNodeConfiguration cnConfig = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(type);
		setIconCssClass(cnConfig.getIconCSSClass());
		setUserObject(courseNode);
	}

	public CourseNode getCourseNode() {
		return courseNode;
	}

	public StatisticResourceResult getResult() {
		return result;
	}

	@Override
	public int getChildCount() {
		if(opened) {
			return super.getChildCount();
		}
		return 1;
	}

	@Override
	public INode getChildAt(int childIndex) {
		if(opened) {
			return super.getChildAt(childIndex);
		}
		return null;
	}
	
	public void opened() {
		opened = true;
	}
	
	public void openNode() {
		if(!opened) {
			opened = true;
			TreeModel subTreeModel = result.getSubTreeModel();
			if(subTreeModel != null) {
				TreeNode subRootNode = subTreeModel.getRootNode();
				List<INode> subNodes = new ArrayList<>();
				for(int i=0; i<subRootNode.getChildCount(); i++) {
					subNodes.add(subRootNode.getChildAt(i));
				}
				for(INode subNode:subNodes) {
					addChild(subNode);
				}
			}
		}
	}
	
}