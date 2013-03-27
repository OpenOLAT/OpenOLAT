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
package org.olat.portfolio.ui.structel.edit;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.tree.DnDTreeModel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;

/**
 * 
 * Initial date: 26.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EPTOCTreeModel extends GenericTreeModel implements DnDTreeModel {

	private static final long serialVersionUID = 7389921072899475506L;
	private final EPFrontendManager ePFMgr;
	
	public EPTOCTreeModel(PortfolioStructureMap map, String tocLabel) {
		ePFMgr = CoreSpringFactory.getImpl(EPFrontendManager.class);

		GenericTreeNode rootNode = new GenericTreeNode("toc", tocLabel, null);
		rootNode.setIconCssClass("o_st_icon");
		rootNode.addChild(loadNode(map));
		setRootNode(rootNode);
	}
	
	private GenericTreeNode loadNode(PortfolioStructure structure) {
		String ident = structure.getKey().toString();
		GenericTreeNode structureNode = new GenericTreeNode(ident, structure.getTitle(), structure);
		structureNode.setIconCssClass(structure.getIcon());
		
		List<PortfolioStructure> structs = ePFMgr.loadStructureChildren(structure);
		for (PortfolioStructure portfolioStructure : structs) {
			GenericTreeNode childNode = loadNode(portfolioStructure);
			structureNode.addChild(childNode);
		}
		
		List<AbstractArtefact> artList = ePFMgr.getArtefacts(structure);
		for (AbstractArtefact artefact : artList) {
			String artefactIdent = artefact.getKey().toString();
			GenericTreeNode artefactNode = new GenericTreeNode(artefactIdent, artefact.getTitle(), artefact);
			artefactNode.setIconCssClass(artefact.getIcon());
			structureNode.addChild(artefactNode);
		}
		
		return structureNode;
	}

	@Override
	public boolean canDrop(TreeNode droppedNode, TreeNode targetNode, boolean sibling) {
		return false;
	}
	
	

	/*
	TreeModel model = new GenericTreeModel(ROOT_NODE_IDENTIFIER) {

		@Override
		public List<AjaxTreeNode> getChildrenFor(String nodeId) {
			List<AjaxTreeNode> children = new ArrayList<AjaxTreeNode>();
			AjaxTreeNode child;
			boolean isRoot = false;
			PortfolioStructure selStruct = null;
			try {
				List<PortfolioStructure> structs = new ArrayList<PortfolioStructure>();
				if (nodeId.equals(ROOT_NODE_IDENTIFIER)) {
					structs.add(rootNode);
					isRoot = true;
				} else if (!nodeId.startsWith(ARTEFACT_NODE_IDENTIFIER)){
					selStruct = ePFMgr.loadPortfolioStructureByKey(new Long(nodeId));
					structs = ePFMgr.loadStructureChildren(selStruct);
				} else {
					// its an artefact -> no childs anymore
					return null;
				}
				if (structs != null && structs.size() != 0) { 
					for (PortfolioStructure portfolioStructure : structs) {
						String childNodeId = String.valueOf(portfolioStructure.getKey());
						boolean hasStructureChild = eSTMgr.countStructureChildren(portfolioStructure) > 0;
						boolean hasArtefacts = eSTMgr.countArtefacts(portfolioStructure) > 0;
						boolean hasChilds = hasStructureChild || hasArtefacts;
						child = new AjaxTreeNode(childNodeId, portfolioStructure.getTitle());
						if (isLogDebugEnabled()){
							child = new AjaxTreeNode(childNodeId, portfolioStructure.getTitle() + "drop:" + !isRoot + "drag:" + !isRoot + "leaf:"+!hasChilds);
						}
						// seems to be a bug, nothing can be dropped on a leaf, therefore we need to tweak with expanded/expandable ourself!
//						child.put(AjaxTreeNode.CONF_LEAF, !hasChilds);
						child.put(AjaxTreeNode.CONF_IS_TYPE_LEAF, !hasChilds);
						child.put(AjaxTreeNode.CONF_ALLOWDRAG, !isRoot);
			
						child.put(AjaxTreeNode.CONF_EXPANDED, hasStructureChild);
						child.put(AjaxTreeNode.CONF_EXPANDABLE, hasChilds);
						child.put(AjaxTreeNode.CONF_ALLOWDROP, true);
						child.put(AjaxTreeNode.CONF_ISTARGET, !isRoot); 
						
						child.put(AjaxTreeNode.CONF_ICON_CSS_CLASS, portfolioStructure.getIcon());
						String description = FilterFactory.getHtmlTagAndDescapingFilter().filter(portfolioStructure.getDescription());
						child.put(AjaxTreeNode.CONF_QTIP, description);
						children.add(child);
						
						String path;
						if(isRoot) {
							path = "/" + ROOT_NODE_IDENTIFIER;
						} else {
							path = idToPath.get(selStruct.getKey()); 
						}

						idToPath.put(portfolioStructure.getKey(), path + "/" + childNodeId);
					}
				} 
				if (selStruct != null && ePFMgr.countArtefactsRecursively(selStruct) != 0){
					List<AbstractArtefact> artList = ePFMgr.getArtefacts(selStruct);
					for (AbstractArtefact abstractArtefact : artList) {
						//include struct also, to still be unique if an artefact is linked multiple times
						String childNodeId = ARTEFACT_NODE_IDENTIFIER + String.valueOf(selStruct.getKey()) + "_" + String.valueOf(abstractArtefact.getKey());
						child = new AjaxTreeNode(childNodeId, abstractArtefact.getTitle());
						child.put(AjaxTreeNode.CONF_LEAF, true);
						child.put(AjaxTreeNode.CONF_IS_TYPE_LEAF, true);
						child.put(AjaxTreeNode.CONF_ALLOWDRAG, true);
						child.put(AjaxTreeNode.CONF_EXPANDED, false);
						child.put(AjaxTreeNode.CONF_ALLOWDROP, false);
						child.put(AjaxTreeNode.CONF_ICON_CSS_CLASS, abstractArtefact.getIcon());
						String description = FilterFactory.getHtmlTagAndDescapingFilter().filter(abstractArtefact.getDescription());
						child.put(AjaxTreeNode.CONF_QTIP, description);
						children.add(child);
						
						String path = idToPath.get(selStruct.getKey());
						
						String artefactPath = path + "/" + childNodeId;
						idToPath.put(abstractArtefact.getKey(), artefactPath);
						pathToStructure.put(artefactPath, selStruct);
					}						
				} 
			} catch (JSONException e) {
				throw new OLATRuntimeException("Error while creating tree model for map/page/structure selection", e);
			}
			return children;
		}
	};
	model.setCustomRootIconCssClass("o_st_icon");
	return model;
	*/


}
