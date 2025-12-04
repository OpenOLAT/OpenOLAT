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
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementType;

/**
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
class CurriculumTreeModel extends GenericTreeModel implements InsertionTreeModel {

	private static final long serialVersionUID = 2911319509933144413L;

	public static final String LEVEL_PREFIX = "cur-el-lev-";
	
	private final CurriculumElement source;
	private final CurriculumElementType sourceType;
	private Map<CurriculumElementType, List<CurriculumElementType>> allTypes;

	public CurriculumTreeModel(CurriculumElement rootElement, CurriculumElement source,
			Map<CurriculumElementType, List<CurriculumElementType>> allTypes) {
		GenericTreeNode root = new GenericTreeNode();
		root.setTitle(rootElement.getDisplayName());
		root.setUserObject(rootElement);
		this.allTypes = allTypes;
		this.source = source;
		sourceType = source.getType();
		setRootNode(root);
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
			if(parentElement.equals(getRootNode().getUserObject()) || !filter.test(parentElement)) {
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
		if(node instanceof GenericTreeNode gNode) {
			return gNode.getUserObject() instanceof CurriculumElement &&  gNode.getUserObject().equals(source);
		}
		return false;
	}
	
	public boolean isInParentLine(TreeNode node) {
		for(INode iteratorNode=node; node.getParent() != null && iteratorNode != null; iteratorNode=iteratorNode.getParent()) {
			if(iteratorNode instanceof TreeNode treeNode && isSource(treeNode)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isSingleElement(TreeNode node) {
		if(node instanceof GenericTreeNode gNode) {
			return gNode.getUserObject() instanceof CurriculumElement element
					&& element.getType() != null && element.getType().isSingleElement();
		}
		return false;
	}

	@Override
	public Position[] getInsertionPosition(TreeNode node) {
		List<Position> positions = new ArrayList<>(5);
		
		if(node.getUserObject() instanceof CurriculumElement element) {
			CurriculumElementType type = element.getType();
			if(type == null || isAllowed(element)) {
				positions.add(Position.under);	
			}
				
			if(element.getParent() != null) {
				CurriculumElementType parentType = element.getParent().getType();
				if(parentType == null || isAllowed(element.getParent())) {
					positions.add(Position.up);
					positions.add(Position.down);
				}
			}
		}
		
		return positions.toArray(new Position[positions.size()]);
	}
	
	public List<CurriculumElementType> getAllowedSubTypes(CurriculumElement parentElement) {
		final CurriculumElementType type = parentElement.getType();
		List<CurriculumElementType> subTypes = allTypes.get(type);
		return subTypes == null ? List.of() : subTypes;
	}
	
	private boolean isAllowed(CurriculumElement parentElement) {
		final CurriculumElementType type = parentElement.getType();
		List<CurriculumElementType> subTypes = allTypes.get(type);
		return subTypes != null && subTypes.contains(sourceType);
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
