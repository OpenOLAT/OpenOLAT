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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.InsertionPoint.Position;
import org.olat.core.gui.components.tree.InsertionTreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.util.nodes.INode;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;

/**
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumTreeModel extends GenericTreeModel implements InsertionTreeModel {

	private static final long serialVersionUID = 2911319509933144413L;

	public static final String LEVEL_PREFIX = "cur-el-lev-";
	
	private final List<CurriculumElement> sources;
	
	public CurriculumTreeModel() {
		this(null, new ArrayList<CurriculumElement>());
	}
	
	public CurriculumTreeModel(Curriculum curriculum) {
		this(curriculum, new ArrayList<CurriculumElement>());
	}

	public CurriculumTreeModel(Curriculum curriculum, List<CurriculumElement> sources) {
		GenericTreeNode root = new GenericTreeNode();
		String title = curriculum == null ? "ROOT" : curriculum.getDisplayName();
		root.setTitle(title);
		this.sources = new ArrayList<>(sources);
		setRootNode(root);
	}
	
	public void loadTreeModel(List<CurriculumElement> elements) {
		loadTreeModel(elements, c -> true);
	}
	
	public void loadTreeModel(List<CurriculumElement> elements, Predicate<CurriculumElement> filter) {
		Map<Long,GenericTreeNode> fieldKeyToNode = new HashMap<>();
		for(CurriculumElement element:elements) {
			if(!filter.test(element)) {
				continue;
			}
			
			Long key = element.getKey();
			GenericTreeNode node = fieldKeyToNode.computeIfAbsent(key, k -> {
				GenericTreeNode newNode = new GenericTreeNode(nodeKey(element));
				newNode.setTitle(element.getDisplayName());
				newNode.setIconCssClass("o_icon_curriculum_element");
				newNode.setUserObject(element);
				return newNode;
			});

			CurriculumElement parentElement = element.getParent();
			if(parentElement == null || !filter.test(parentElement)) {
				//this is a root
				getRootNode().addChild(node);
			} else {
				Long parentKey = parentElement.getKey();
				GenericTreeNode parentNode = fieldKeyToNode.computeIfAbsent(parentKey, k -> {
					GenericTreeNode newNode = new GenericTreeNode(nodeKey(parentElement));
					newNode.setTitle(parentElement.getDisplayName());
					newNode.setIconCssClass("o_icon_curriculum_element");
					newNode.setUserObject(parentElement);
					return newNode;
				});
				
				if(parentNode == null) {
					fieldKeyToNode.put(parentKey, parentNode);
				} else {
					parentNode.addChild(node);
				}
			}
		}
		
		CurriculumElementComparator comparator = new CurriculumElementComparator();
		getRootNode().sort(comparator);
		for(GenericTreeNode node:fieldKeyToNode.values()) {
			node.sort(comparator);
		}	
	}
	
	@Override
	public boolean isSource(TreeNode node) {
		if(node instanceof GenericTreeNode) {
			GenericTreeNode gNode = (GenericTreeNode)node;
			return gNode.getUserObject() instanceof CurriculumElement && sources.contains(gNode.getUserObject());
		}
		return false;
	}
	
	public boolean isInParentLine(TreeNode node) {
		for(INode iteratorNode=node; node.getParent() != null && iteratorNode != null; iteratorNode=iteratorNode.getParent()) {
			if(iteratorNode instanceof TreeNode && isSource((TreeNode)iteratorNode)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Position[] getInsertionPosition(TreeNode node) {
		Position[] positions;
		if(isSource(node)) {
			positions = new Position[0];
		} else if(getRootNode() == node) {
			positions = new Position[] { Position.under };
		} else if(isInParentLine(node)) {
			positions = new Position[0];
		} else if(node.getIconCssClass() != null && node.getIconCssClass().contains("o_icon_node_up_down")) {
			positions = new Position[] { Position.up, Position.down };
		} else {
			positions = new Position[] { Position.up, Position.down, Position.under };
		}
		return positions;
	}

	public static final String nodeKey(CurriculumElementRef element) {
		return LEVEL_PREFIX + element.getKey();
	}
	
	private static class CurriculumElementComparator implements Comparator<INode> {

		@Override
		public int compare(INode n1, INode n2) {
			CurriculumElement o1 = (CurriculumElement)((TreeNode)n1).getUserObject();
			CurriculumElement o2 = (CurriculumElement)((TreeNode)n2).getUserObject();

			Integer p1 = null;
			Integer p2 = null;
			
			if(o1.getParent() == null && o2.getParent() == null) {
				p1 = o1.getPosCurriculum();
				p2 = o2.getPosCurriculum();
			} else if(o1.getParent() != null && o2.getParent() != null) {
				p1 = o1.getPos();
				p2 = o2.getPos();
			}
			
			int c = 0;
			if(p1 != null && p2 != null) {
				c = p1.compareTo(p2);
			} else if(p1 != null) {
				c = -1;
			} else if(p2 != null) {
				c = 1;
			}
			return c;
		}
	}
}
