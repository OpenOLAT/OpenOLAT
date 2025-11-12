/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.tree;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.olat.core.util.nodes.INode;

/**
 * 
 * Initial date: Sep 11, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class GenericTreeModelBuilder<T> {

	private final Function<T, String> keyExtractor;
	private final Function<T, T> parentExtractor;
	private final Function<T, GenericTreeNode> toNode;

	public GenericTreeModelBuilder(Function<T, String> keyExtractor, Function<T, T> parentExtractor, Function<T, GenericTreeNode> toNode) {
		this.keyExtractor = keyExtractor;
		this.parentExtractor = parentExtractor;
		this.toNode = toNode;
	}
	
	public GenericTreeModel build(Collection<T> ts) {
		GenericTreeModel model = new GenericTreeModel();
		
		GenericTreeNode root = new GenericTreeNode();
		root.setTitle("ROOT");
		model.setRootNode(root);
		
		loadTreeModel(model, ts);
		
		return model;
	}
	
	private void loadTreeModel(GenericTreeModel model, Collection<T> ts) {
		Map<String, GenericTreeNode> keyToNode = new HashMap<>();
		for (T t: ts) {
			String key = keyExtractor.apply(t);
			GenericTreeNode node = keyToNode.computeIfAbsent(key, k -> {
				GenericTreeNode newNode = toNode.apply(t);
				newNode.setIdent(k);
				return newNode;
			});
			
			T parent = parentExtractor.apply(t);
			if (parent == null) {
				//this is a root
				model.getRootNode().addChild(node);
			} else {
				String parentKey = keyExtractor.apply(parent);
				GenericTreeNode parentNode = keyToNode.computeIfAbsent(parentKey, k -> {
					GenericTreeNode newNode = toNode.apply(parent);
					newNode.setIdent(k);
					return newNode;
				});
				
				if (parentNode == null) {
					keyToNode.put(parentKey, parentNode);
				} else {
					parentNode.addChild(node);
				}
			}
		}
		
		Comparator<INode> comparator = new TreeNodeTitleComparator();
		model.getRootNode().sort(comparator);
		for (GenericTreeNode node : keyToNode.values()) {
			node.sort(comparator);
		}
	}

}
