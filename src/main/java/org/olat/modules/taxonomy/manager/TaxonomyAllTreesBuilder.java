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
package org.olat.modules.taxonomy.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;

/**
 * 
 * Initial date: 27 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyAllTreesBuilder {

	public static final String ROOT = "root";
	public static final String LEVEL_PREFIX = "level-";

	private final TaxonomyService taxonomyService;
	
	public TaxonomyAllTreesBuilder() {
		taxonomyService = CoreSpringFactory.getImpl(TaxonomyService.class);
	}
	
	public GenericTreeModel buildTreeModel() {
		GenericTreeModel taxonomyTreesModel = new GenericTreeModel();
		loadTreeModel(taxonomyTreesModel);
		return taxonomyTreesModel;
	}
	
	public GenericTreeModel buildTreeModel(Taxonomy taxonomy) {
		GenericTreeModel taxonomyTreesModel = new GenericTreeModel();
		loadTreeModel(taxonomyTreesModel, taxonomy);
		return taxonomyTreesModel;
	}
	
	public void loadTreeModel(GenericTreeModel taxonomyTreesModel, Taxonomy taxonomy) {
		Map<Taxonomy, GenericTreeNode> rootNodesMap = new HashMap<>();

		GenericTreeNode rootNode = new GenericTreeNode("taxonomy-" + taxonomy.getKey());
		rootNode.setTitle(taxonomy.getDisplayName());
		rootNode.setIconCssClass("o_icon_taxonomy");
		rootNode.setUserObject(taxonomy);
		taxonomyTreesModel.setRootNode(rootNode);
		rootNodesMap.put(taxonomy, rootNode);

		loadTreeModel(rootNodesMap, taxonomy);
	}
	
	public void loadTreeModel(GenericTreeModel taxonomyTreesModel) {
		List<Taxonomy> taxonomyList = taxonomyService.getTaxonomyList();
		GenericTreeNode rootNode = new GenericTreeNode("Root", ROOT);
		taxonomyTreesModel.setRootNode(rootNode);
		Map<Taxonomy, GenericTreeNode> rootNodesMap = new HashMap<>();
		for(Taxonomy taxonomy:taxonomyList) {
			GenericTreeNode node = new GenericTreeNode("taxonomy-" + taxonomy.getKey());
			node.setTitle(taxonomy.getDisplayName());
			node.setIconCssClass("o_icon_taxonomy");
			node.setUserObject(taxonomy);
			rootNode.addChild(node);
			rootNodesMap.put(taxonomy, node);
		}
		loadTreeModel(rootNodesMap, null);
	}

	private void loadTreeModel(Map<Taxonomy, GenericTreeNode> rootNodesMap, Taxonomy taxonomy) {
		List<TaxonomyLevel> taxonomyLevels = taxonomyService.getTaxonomyLevels(taxonomy);
		Map<Long,GenericTreeNode> fieldKeyToNode = new HashMap<Long, GenericTreeNode>();
		for(TaxonomyLevel taxonomyLevel:taxonomyLevels) {
			Long key = taxonomyLevel.getKey();
			GenericTreeNode node = fieldKeyToNode.get(key);
			if(node == null) {
				node = new GenericTreeNode(LEVEL_PREFIX + taxonomyLevel.getKey());
				node.setTitle(taxonomyLevel.getDisplayName());
				node.setIconCssClass("o_icon_taxonomy_level");
				node.setUserObject(taxonomyLevel);
				fieldKeyToNode.put(key, node);
			}

			TaxonomyLevel parentLevel = taxonomyLevel.getParent();
			if(parentLevel == null) {
				//this is a root
				GenericTreeNode taxonomyNode = rootNodesMap.get(taxonomyLevel.getTaxonomy());
				taxonomyNode.addChild(node);
			} else {
				Long parentKey = parentLevel.getKey();
				GenericTreeNode parentNode = fieldKeyToNode.get(parentKey);
				if(parentNode == null) {
					parentNode = new GenericTreeNode("level-" + parentLevel.getKey());
					parentNode.setTitle(parentLevel.getDisplayName());
					parentNode.setIconCssClass("o_icon_taxonomy_level");
					parentNode.setUserObject(parentLevel);
					fieldKeyToNode.put(parentKey, parentNode);
				}
				parentNode.addChild(node);
			}
		}
	}
}
