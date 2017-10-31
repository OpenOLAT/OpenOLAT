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
package org.olat.modules.qpool.ui.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 28.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyTreeModel extends GenericTreeModel {

	private static final long serialVersionUID = 3032222581990406868L;
	private final QPoolService qpoolService;
	
	public static final String ROOT = "root";

	public TaxonomyTreeModel(String rootLabel) {
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		buildTree(rootLabel);
	}
	
	private void buildTree(String rootLabel) {
		GenericTreeNode rootNode = new GenericTreeNode(rootLabel, ROOT);
		setRootNode(rootNode);

		List<TaxonomyLevel> fields = qpoolService.getTaxonomyLevels();
		Map<Long,GenericTreeNode> fieldKeyToNode = new HashMap<Long, GenericTreeNode>();
		for(TaxonomyLevel field:fields) {
			Long key = field.getKey();
			GenericTreeNode node = fieldKeyToNode.get(key);
			if(node == null) {
				node = new GenericTreeNode(field.getDisplayName(), field);
				fieldKeyToNode.put(key, node);
			}

			TaxonomyLevel parentField = field.getParent();
			if(parentField == null) {
				//this is a root
				rootNode.addChild(node);
			} else {
				Long parentKey = parentField.getKey();
				GenericTreeNode parentNode = fieldKeyToNode.get(parentKey);
				if(parentNode == null) {
					parentNode = new GenericTreeNode(parentField.getDisplayName(), parentField);
					fieldKeyToNode.put(parentKey, parentNode);
				}
				parentNode.addChild(node);
			}
		}
	}
}
