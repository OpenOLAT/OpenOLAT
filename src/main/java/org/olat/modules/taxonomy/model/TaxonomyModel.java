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
package org.olat.modules.taxonomy.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeHelper;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.manager.TaxonomyAllTreesBuilder;
import org.olat.modules.taxonomy.manager.TaxonomyTreeNodeComparator;

/**
 * 
 * Initial date: 12 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyModel extends GenericTreeModel {

	private static final long serialVersionUID = 7829882002630340923L;

	public void sort(INode parent) {
		parent.sort(new TaxonomyTreeNodeComparator());
		for(int i=parent.getChildCount(); i-->0; ) {
			sort(parent.getChildAt(i));
		}
	}
	
	public String nodeKey(TaxonomyLevelRef taxonomyLevel) {
		return TaxonomyAllTreesBuilder.nodeKey(taxonomyLevel);
	}
	
	public List<TreeNode> getDescendants(TreeNode parentNode) {
		List<TreeNode> descendants = new ArrayList<>();
		for(int i=0; i<parentNode.getChildCount(); i++) {
			TreeHelper.makeTreeFlat((TreeNode)parentNode.getChildAt(i), descendants);
		}
		return descendants;
	}
	
	/**
	 * Check the selection of levels doesn't contains
	 * a select parent with only part of its children selected.
	 * No children selected is allowed, all children selected
	 * are allowed but not part of them.
	 * 
	 * 
	 * @param levels The list of levels
	 * @return true if the selection is continous
	 */
	public boolean validateContinuousSelection(List<? extends TaxonomyLevelRef> levels) {
		boolean allOk = true;
		
		Set<String> selectedNodeIds = new HashSet<>();
		for(TaxonomyLevelRef level:levels) {
			selectedNodeIds.add(nodeKey(level));
		}
		
		for(TaxonomyLevelRef level:levels) {
			String nodeId = nodeKey(level);
			TreeNode node = getNodeById(nodeId);
			if(node.getChildCount() > 0) {
				int count = node.getChildCount();
				for(int i=count; i-->0; ) {
					String childId = node.getChildAt(i).getIdent();
					if(selectedNodeIds.contains(childId)) {
						count--;
					}
				}
				if(count > 0 && count != node.getChildCount()) {
					allOk &= false;
				}
			}
		}
		
		return allOk;
	}
}
