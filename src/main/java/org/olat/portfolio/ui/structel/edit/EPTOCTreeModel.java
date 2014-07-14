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
import org.olat.portfolio.model.structel.EPAbstractMap;
import org.olat.portfolio.model.structel.EPPage;
import org.olat.portfolio.model.structel.EPStructureElement;
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
	private final PortfolioStructureMap map;
	
	public EPTOCTreeModel(PortfolioStructureMap map, String tocLabel) {
		ePFMgr = CoreSpringFactory.getImpl(EPFrontendManager.class);
		this.map = map;

		GenericTreeNode rootNode = new GenericTreeNode("toc", tocLabel, null);
		rootNode.setIconCssClass("o_st_icon");
		loadNode(map, rootNode);
		setRootNode(rootNode);
	}
	
	protected void reload() {
		getRootNode().removeAllChildren();
		loadNode(map, getRootNode());
	}
	
	private GenericTreeNode loadNode(PortfolioStructure structure, TreeNode parentNode) {
		String ident = structure.getKey().toString();
		GenericTreeNode structureNode = new GenericTreeNode(ident, structure.getTitle(), structure);
		structureNode.setIconCssClass(structure.getIcon());
		parentNode.addChild(structureNode);
		loadChildNode(structure, structureNode);
		return structureNode;
	}
	
	protected void loadChildNode(PortfolioStructure structure, TreeNode structureNode) {
		structureNode.removeAllChildren();
		
		List<PortfolioStructure> structs = ePFMgr.loadStructureChildren(structure);
		for (PortfolioStructure portfolioStructure : structs) {
			loadNode(portfolioStructure, structureNode);
		}
		
		List<AbstractArtefact> artList = ePFMgr.getArtefacts(structure);
		for (AbstractArtefact artefact : artList) {
			String artefactIdent = structureNode.getIdent() + artefact.getKey().toString();
			GenericTreeNode artefactNode = new GenericTreeNode(artefactIdent, artefact.getTitle(), artefact);
			artefactNode.setIconCssClass("o_icon o_ep_artefact " + artefact.getIcon());
			structureNode.addChild(artefactNode);
		}
	}

	@Override
	public boolean isNodeDroppable(TreeNode node) {
		return !getRootNode().getIdent().equals(node.getIdent());
	}

	@Override
	public boolean isNodeDraggable(TreeNode node) {
		return !getRootNode().getIdent().equals(node.getIdent())
				&& !getRootNode().getIdent().equals(node.getParent().getIdent()) ;
	}

	/**
	 * The check is done in javascript, the purpose of this method is only
	 * historically and for analyse
	 * 
	 * @deprecated
	 * @param droppedNode
	 * @param targetNode
	 * @param sibling
	 * @return
	 */
	public boolean canDrop(TreeNode droppedNode, TreeNode targetNode, boolean sibling) {
		Object droppedObj = droppedNode.getUserObject();
		Object droppedParentObj = null;
		if(droppedNode.getParent() != null) {
			droppedParentObj = ((TreeNode)droppedNode.getParent()).getUserObject();
		}
		
		Object targetParentObj = null;
		if(targetNode.getParent() != null) {
			targetParentObj = ((TreeNode)targetNode.getParent()).getUserObject();
		}

		Object targetObj = targetNode.getUserObject();
		if (droppedObj instanceof AbstractArtefact) {
			AbstractArtefact droppedArtefact = (AbstractArtefact)droppedObj;
			if (checkArtefactTarget(droppedParentObj, droppedArtefact, targetObj, targetParentObj, sibling)) {
				return true;
			} else if(droppedParentObj.equals(targetObj)) {	
				return true;
			} else {	
				return false;
			}
		} else if (droppedObj instanceof PortfolioStructure) {
			PortfolioStructure droppedStructure = (PortfolioStructure)droppedObj;
			if (checkStructureTarget(droppedParentObj, droppedStructure, targetObj, targetParentObj, sibling)) {
				return true;
			} else {				
				return false;
			}
		} else {
			return false;
		}
	}
	
	private boolean checkArtefactTarget(Object droppedParentObj, AbstractArtefact artefact,
			Object targetObj, Object targetParentObj, boolean sibling) {
		PortfolioStructure newParStruct;
		if (targetObj instanceof EPAbstractMap ) {
			return false;
		} else if(targetObj instanceof PortfolioStructure) {
			newParStruct = (PortfolioStructure)targetObj;
		} else if (sibling) {
			if(targetObj instanceof AbstractArtefact && targetParentObj instanceof PortfolioStructure) {
				if(droppedParentObj != null && droppedParentObj.equals(targetParentObj)) {
					return true; //reorder
				} else {
					newParStruct = (PortfolioStructure)targetParentObj;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}

		boolean sameTarget = ePFMgr.isArtefactInStructure(artefact, newParStruct);
		if (sameTarget) {
			return false;
		}
		return true;
	}
	
	private boolean checkStructureTarget(Object droppedParentObj, PortfolioStructure droppedStructure,
			Object targetObj, Object targetParentObj, boolean sibling){
	
		if(targetObj == null || droppedParentObj == null) {
			return false;
		}

		if(sibling) {
			if(targetParentObj instanceof PortfolioStructure) {
				if(droppedParentObj != null && targetParentObj != null && droppedParentObj.equals(targetParentObj)) {
					return true; //reorder
				} else {
					return false;
				}
			}
		} else {
			if (droppedParentObj.equals(targetObj)) {
				return true; // seems only to be a move in order
			}
			if (droppedStructure instanceof EPPage && targetObj instanceof EPPage) {
				return false;
			}
			if (droppedStructure instanceof EPStructureElement && !(targetObj instanceof EPPage)) {
				return false;
			}
		}
		return true;
	}
}
