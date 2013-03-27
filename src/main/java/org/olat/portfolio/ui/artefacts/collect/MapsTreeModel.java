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
package org.olat.portfolio.ui.artefacts.collect;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.ElementType;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.StructureStatusEnum;

/**
 * 
 * Initial date: 27.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MapsTreeModel extends GenericTreeModel {

	private static final long serialVersionUID = 6367006359564581412L;
	private final EPFrontendManager ePFMgr;
	
	public MapsTreeModel(Identity identity, Translator translator) {
		ePFMgr = CoreSpringFactory.getImpl(EPFrontendManager.class);
		GenericTreeNode rootNode = new GenericTreeNode(EPCollectStepForm04.ROOT_NODE_IDENTIFIER, translator.translate("step4.my.maps"), null);
		rootNode.setIconCssClass("o_st_icon");
		
		GenericTreeNode noMapNode = new GenericTreeNode(EPCollectStepForm04.NO_MAP_CHOOSEN, translator.translate("no.map.as.target"), null);
		noMapNode.setIconCssClass("b_ep_collection_icon");
		rootNode.addChild(noMapNode);

		loadMaps(identity, rootNode);
		setRootNode(rootNode);
	}
	
	private void loadMaps(Identity identity, GenericTreeNode parentNode) {
		List<PortfolioStructure> structs = ePFMgr.getStructureElementsForUser(identity, ElementType.STRUCTURED_MAP, ElementType.DEFAULT_MAP);
		for(PortfolioStructure struct:structs) {
			// FXOLAT-436 : skip templateMaps that are closed
			if (struct instanceof EPStructuredMap) {
				EPStructuredMap map = (EPStructuredMap)struct;
				if(map.getStatus() != null && map.getStatus().equals(StructureStatusEnum.CLOSED)){
					continue;
				}
			}

			loadStructure(struct, parentNode);
		}
	}
	
	private void loadStructure(PortfolioStructure struct, GenericTreeNode parentNode) {
		String ident = struct.getKey().toString();
		GenericTreeNode structureNode = new GenericTreeNode(ident, struct.getTitle(), struct);
		structureNode.setIconCssClass(struct.getIcon());
		parentNode.addChild(structureNode);

		List<PortfolioStructure> structs  = ePFMgr.loadStructureChildren(struct);
		for(PortfolioStructure childStruct:structs) {
			loadStructure(childStruct, structureNode);
		}
	}

/*
	private boolean firstLevelDone = false;

	@SuppressWarnings("synthetic-access")
	@Override
	public List<AjaxTreeNode> getChildrenFor(String nodeId) {
		List<AjaxTreeNode> children = new ArrayList<AjaxTreeNode>();
		AjaxTreeNode child;
		try {
			List<PortfolioStructure> structs = null;
			if (nodeId.equals(ROOT_NODE_IDENTIFIER)) {
				structs = ePFMgr.getStructureElementsForUser(getIdentity(), ElementType.STRUCTURED_MAP, ElementType.DEFAULT_MAP);
				firstLevelDone = false;
			} else {
				PortfolioStructure selStruct = ePFMgr.loadPortfolioStructureByKey(new Long(nodeId));
				structs = ePFMgr.loadStructureChildren(selStruct);
			}
			if (structs == null || structs.size() == 0) { return null; }
			// add a fake map to choose if no target should be set
			if (!firstLevelDone){
				child = new AjaxTreeNode(NO_MAP_CHOOSEN, translate("no.map.as.target"));
				child.put(AjaxTreeNode.CONF_LEAF, true);
				child.put(AjaxTreeNode.CONF_IS_TYPE_LEAF, true);
				child.put(AjaxTreeNode.CONF_ALLOWDRAG, false);
				child.put(AjaxTreeNode.CONF_ALLOWDROP, false);
				child.put(AjaxTreeNode.CONF_ICON_CSS_CLASS, "b_ep_collection_icon");
				child.put(AjaxTreeNode.CONF_QTIP, translate("no.map.as.target.desc"));
				if (isUsedInStepWizzard()) children.add(child);
				firstLevelDone = true;
			}
			for (PortfolioStructure portfolioStructure : structs) {
				// FXOLAT-436 : skip templateMaps that are closed
				if (portfolioStructure instanceof EPStructuredMap) {
					if( ((EPStructuredMap) portfolioStructure).getStatus() != null && ((EPStructuredMap) portfolioStructure).getStatus().equals(StructureStatusEnum.CLOSED)){
						continue;
					}
				}
				
				String title = portfolioStructure.getTitle();
				if (!isUsedInStepWizzard() && oldStructure.getKey().equals(portfolioStructure.getKey())) {
					title = portfolioStructure.getTitle() + "&nbsp; &nbsp; <-- " + translate("move.artefact.actual.node");
				}						
				child = new AjaxTreeNode(String.valueOf(portfolioStructure.getKey()), title);
				boolean hasChilds = ePFMgr.countStructureChildren(portfolioStructure) > 0;	
				child.put(AjaxTreeNode.CONF_LEAF, !hasChilds);
				child.put(AjaxTreeNode.CONF_IS_TYPE_LEAF, !hasChilds);
				child.put(AjaxTreeNode.CONF_ALLOWDRAG, false);
				child.put(AjaxTreeNode.CONF_ALLOWDROP, false);
				child.put(AjaxTreeNode.CONF_EXPANDED, true);
				child.put(AjaxTreeNode.CONF_DISABLED, portfolioStructure instanceof EPAbstractMap);
				child.put(AjaxTreeNode.CONF_ICON_CSS_CLASS, portfolioStructure.getIcon());
				child.put(AjaxTreeNode.CONF_QTIP, portfolioStructure.getDescription());
				
				children.add(child);
			}
		} catch (JSONException e) {
			throw new OLATRuntimeException("Error while creating tree model for map/page/structure selection", e);
		}
		return children;
	}
};
model.setCustomRootIconCssClass("o_st_icon");
*/

}
