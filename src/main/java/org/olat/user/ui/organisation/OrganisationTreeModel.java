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
package org.olat.user.ui.organisation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;

/**
 * 
 * Initial date: 8 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationTreeModel extends GenericTreeModel {

	private static final long serialVersionUID = 2911319509933144413L;

	public static final String LEVEL_PREFIX = "org-lev-";

	public OrganisationTreeModel() {
		GenericTreeNode root = new GenericTreeNode();
		root.setTitle("ROOT");
		setRootNode(root);
	}
	
	public void loadTreeModel(List<Organisation> organisations) {
		Map<Long,GenericTreeNode> fieldKeyToNode = new HashMap<>();
		for(Organisation organisation:organisations) {
			Long key = organisation.getKey();
			GenericTreeNode node = fieldKeyToNode.computeIfAbsent(key, k -> {
				GenericTreeNode newNode = new GenericTreeNode(nodeKey(organisation));
				newNode.setTitle(organisation.getDisplayName());
				newNode.setIconCssClass("o_icon_organisation");
				newNode.setUserObject(organisation);
				return newNode;
			});

			Organisation parentLevel = organisation.getParent();
			if(parentLevel == null) {
				//this is a root
				getRootNode().addChild(node);
			} else {
				Long parentKey = parentLevel.getKey();
				GenericTreeNode parentNode = fieldKeyToNode.computeIfAbsent(parentKey, k -> {
					GenericTreeNode newNode = new GenericTreeNode(nodeKey(parentLevel));
					newNode.setTitle(parentLevel.getDisplayName());
					newNode.setIconCssClass("o_icon_organisation");
					newNode.setUserObject(parentLevel);
					return newNode;
				});
				
				if(parentNode == null) {
					fieldKeyToNode.put(parentKey, parentNode);
				} else {
					parentNode.addChild(node);
				}
			}
		}
	}
	
	public static final String nodeKey(OrganisationRef taxonomyLevel) {
		return LEVEL_PREFIX + taxonomyLevel.getKey();
	}
	
	
	

}
