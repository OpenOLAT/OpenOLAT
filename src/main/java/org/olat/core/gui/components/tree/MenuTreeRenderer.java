/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.gui.components.tree;

import static org.olat.core.gui.components.velocity.VelocityContainer.COMMAND_ID;
import static org.olat.core.gui.components.tree.MenuTree.NODE_IDENT;
import static org.olat.core.gui.components.tree.MenuTree.COMMAND_TREENODE;
import static org.olat.core.gui.components.tree.MenuTree.COMMAND_TREENODE_CLICKED;
import static org.olat.core.gui.components.tree.MenuTree.COMMAND_TREENODE_DROP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.GUIInterna;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeHelper;

/**
 * enclosing_type Description: <br>
 * 
 * @author Felix Jost, Florian Gnaegi
 */
//fxdiff VCRP-9: there is here lots of change for drag and drop / open close in menu tree
public class MenuTreeRenderer implements ComponentRenderer {

	/**
	 * Constructor for TableRenderer. Singleton and must be reentrant There must
	 * be an empty contructor for the Class.forName() call
	 */
	public MenuTreeRenderer() {
		super();
	}

	/**
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#render(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator,
	 *      org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	@Override
	public void render(Renderer renderer, StringOutput target, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		MenuTree tree = (MenuTree) source;
		
		TreeNode root = tree.getTreeModel().getRootNode();
		if (root == null) return; // tree is completely empty
		INode selNode = tree.getSelectedNode();
		Collection<String> openNodeIds = tree.getOpenNodeIds();

		if (tree.isExpandServerOnly()) { 
			// render only the expanded path using no javascript
			List<INode> selPath = new ArrayList<INode>(5);
			INode cur = selNode;
			if (cur == null) cur = root; 
			// if no selection, select the first node to
			// expand the children
			// add all elems from selected path to reversed list -> first elem is
			// selected nodeid of the root node			
			while (cur != null) {
				selPath.add(0, cur);
				cur = cur.getParent();
			}
			
			AJAXFlags flags = renderer.getGlobalSettings().getAjaxFlags();
			target.append("\n<div id='dd1-ct' class='b_tree");
			if(tree.isDragAndDropEnabled()) {
				target.append(" b_dd_ct");
			}
			target.append("'>\n");
			target.append("<ul class=\"b_tree_l0\">");
			renderLevel(target, 0, root, selPath, openNodeIds, ubu, flags, tree.markingTreeNode, tree);
			target.append("</ul>");
			target.append("\n</div>");
		} else {			
			throw new RuntimeException("Currently only server side menus implemented, sorry!");
		}
	}

	private void renderLevel(StringOutput target, int level, TreeNode curRoot, List<INode> selPath, Collection<String> openNodeIds, URLBuilder ubu, AJAXFlags flags, TreeNode markedNode, MenuTree tree) {	
		//TODO make performant
		INode curSel = null;
		if (level < selPath.size()) {
			curSel = selPath.get(level);
		}
		
		int chdCnt = curRoot.getChildCount();
		boolean iframePostEnabled = flags.isIframePostEnabled();
		boolean selected = (!selPath.isEmpty() && selPath.get(selPath.size() - 1) == curRoot);
		boolean renderChildren = isRenderChildren(curSel, curRoot, selected, tree, openNodeIds);

		// get css class

		// item icon css class and icon decorator (for each icon quadrant a div, eclipse style)
		// open menu item
		String cssClass = curRoot.getCssClass();
		target.append("\n<li class=\"");
		// add custom css class
		target.append((cssClass == null ? "" : cssClass));
		if(selected) {
			target.append(" b_tree_selected");
		}
		String ident = curRoot.getIdent();
		target.append("\"><div id='dd").append(ident).append("' class=\"b_tree_item_wrapper");
		if(tree.isDragAndDropEnabled()) {
			target.append(" b_dd_item");
		}
		if(selected) {
			target.append(" b_tree_selected");
		}
		target.append("\">");
		
		if(tree.isDragAndDropEnabled()) {
			appendDragAndDropObj(curRoot, tree, target, ubu, flags);
		}
		
		// render link
		String title = curRoot.getTitle();
		title = StringEscapeUtils.escapeHtml(title).toString();

		if (markedNode != null && markedNode == curRoot) {
			target.append("<span style=\"border:2px solid red;\">");
		}
		// expand icon
		// add ajax support and real open/close function
		if (level != 0 && chdCnt > 0) { // root has not open/close icon,  append open / close icon only if there is children
			target.append("<a onclick=\"try {return o2cl()} catch(e){return false}\" href=\"");
			
			// Build menu item URI
			if (GUIInterna.isLoadPerformanceMode()) {
				//	 if in load perf test -> generate the treeposition and include it as param, since the nodeid itself is random and thus not replayable			
				String treePath = TreeHelper.buildTreePath(curRoot);			
				ubu.buildURI(target, new String[] { "en" }, new String[] { treePath });
			} else {
				String cmd = renderChildren ? MenuTree.TREENODE_CLOSE : MenuTree.TREENODE_OPEN;
				if (iframePostEnabled) {
					ubu.buildURI(target, new String[] { COMMAND_ID, NODE_IDENT, COMMAND_TREENODE }, new String[] { COMMAND_TREENODE_CLICKED, curRoot.getIdent(), cmd }, AJAXFlags.MODE_TOBGIFRAME);
				} else {
					ubu.buildURI(target, new String[] { COMMAND_ID, NODE_IDENT, cmd }, new String[] { COMMAND_TREENODE_CLICKED, curRoot.getIdent(), cmd });
				}
			}
			
			target.append("\"");
			if(iframePostEnabled) {
				ubu.appendTarget(target);
			}
			target.append(" class=\"");
			if (renderChildren) {
				target.append("b_tree_level_close");
			} else {
				target.append("b_tree_level_open");
			}
			target.append(" b_tree_oc_l").append(level).append("\"><span>&nbsp;&nbsp;</span></a>");
		} else if (level != 0 && chdCnt == 0) {
			target.append("<span class=\"b_tree_level_leaf b_tree_oc_l").append(level).append("\">&nbsp;&nbsp;</span>");
		}
		
		// Render menu item as link, also for active elements
		// mark active item as strong for accessablity reasons
		
		target.append(selected ? "<strong>" : "");
		target.append("<a class=\"");
		// add icon css class
		String iconCssClass = curRoot.getIconCssClass();
		if (iconCssClass != null) {
			target.append(" b_tree_icon ").append(iconCssClass);			
		}
		if (selected) {
			// add css class to identify active element
			target.append(" b_tree_selected");			
		} else if (curSel == curRoot) {
			// add css class to identify parents of active element
			target.append(" b_tree_selected_parents");			
		}
		
		//reapply the same rules to the second link
		if(level != 0 && chdCnt > 0) {
			if (renderChildren) {
				target.append(" b_tree_level_label_close");
			} else  {
				target.append(" b_tree_level_label_open");
			}
		} else if (level != 0 && chdCnt == 0) {
			target.append(" b_tree_level_label_leaf");
		}
		
		// add css class to identify level
		target.append(" b_tree_l").append(level);		
		
		// fix needed for firefox bug when fast clicking: the onclick="try{return o2cl()}catch(e){return false}"  -> when the document is reloaded, all function js gets unloaded, but the old link can still be clicked.			
		target.append("\" onclick=\"try {if(o2cl()){Effect.ScrollTo('b_top'); return true;} else {return false;}} catch(e){return false}\" href=\"");					
		
		// Build menu item URI
		if (GUIInterna.isLoadPerformanceMode()) {
			//	 if in load perf test -> generate the treeposition and include it as param, since the nodeid itself is random and thus not replayable			
			String treePath = TreeHelper.buildTreePath(curRoot);			
			ubu.buildURI(target, new String[] { "en" }, new String[] { treePath });
		} else {
			if (iframePostEnabled) {
				ubu.buildURI(target, new String[] { COMMAND_ID, NODE_IDENT }, new String[] { COMMAND_TREENODE_CLICKED, curRoot.getIdent() }, AJAXFlags.MODE_TOBGIFRAME);
			} else {
				ubu.buildURI(target, new String[] { COMMAND_ID, NODE_IDENT }, new String[] { COMMAND_TREENODE_CLICKED, curRoot.getIdent() });
			}
		}		
		// Add menu item title as alt hoover text
		target.append("\" title=\"");
		target.append(curRoot.getAltText() == null ? title : StringEscapeUtils.escapeHtml(curRoot.getAltText()).toString());
		target.append("\"");
		if (iframePostEnabled) {
			ubu.appendTarget(target);
		}
		target.append(">");

		
		appendDecorators(curRoot, target);
		
		// display title and close menu item
		target.append("<span");
		if(tree.isDragAndDropEnabled()) {
			target.append(" class='b_dd_item' id='da").append(ident).append("'");
		}
		target.append(">");
		if(title != null && title.equals("")) title = "&nbsp;";
		target.append(title).append("</span></a>");
		// mark active item as strong for accessablity reasons
		target.append(selected ? "</strong>" : "");

		if (markedNode != null && markedNode == curRoot) {
			target.append("</span>");
		}
		target.append("</div>");
		
		//append div to drop as sibling
		if(!renderChildren && tree.isDragAndDropEnabled()) {
			appendSiblingDropObj(curRoot, level, tree, target, false);
		}
		
		if (renderChildren) {
			//open / close ul
			renderChildren(target, level, curRoot, selPath, openNodeIds, ubu, flags, markedNode, tree);
			
			//append div to drop as sibling after the children
			if(tree.isDragAndDropEnabled()) {
				appendSiblingDropObj(curRoot, level, tree, target, true);
			}
		}
		
		//	 close item level
		target.append("</li>");
	}
	
	//fxdiff VCRP-9: drag and drop in menu tree
	private void renderChildren(StringOutput target, int level, TreeNode curRoot, List<INode> selPath, Collection<String> openNodeIds, URLBuilder ubu, AJAXFlags flags, TreeNode markedNode, MenuTree tree) {
		int chdCnt = curRoot.getChildCount();
		// render children as new level
		target.append("\n<ul class=\"");
		// add css class to identify level
		target.append(" b_tree_l").append(level + 1);		
		target.append("\">");
		// render all the nodes from this level
		for (int i = 0; i < chdCnt; i++) {
			TreeNode curChd = (TreeNode) curRoot.getChildAt(i);
			renderLevel(target, level + 1, curChd, selPath, openNodeIds, ubu, flags, markedNode, tree);
		}
		target.append("</ul>");
	}
	
	//fxdiff VCRP-9: drag and drop in menu tree
	private void appendSiblingDropObj(TreeNode node, int level, MenuTree tree, StringOutput target, boolean after) {
		String id = (after ? "dt" : "ds") + node.getIdent();
		String dndGroup = tree.getDragAndDropGroup();
		target.append("<div id='").append(id).append("' class='b_dd_sibling b_dd_sibling_l").append(level).append("'>")
			.append("<script type='text/javascript'>Ext.get('").append(id).append("').dd = new Ext.dd.DDTarget('").append(id).append("','").append(dndGroup).append("');</script>")
			.append("&nbsp;&nbsp;</div>");
	}
	
	//fxdiff VCRP-9: drag and drop in menu tree
	private void appendDragAndDropObj(TreeNode node, MenuTree tree, StringOutput target, URLBuilder ubu, AJAXFlags flags) {
		String id = node.getIdent();
		String dndGroup = tree.getDragAndDropGroup();
		String feedBackUri = tree.getDndFeedbackUri();
		StringOutput endUrl = new StringOutput();
		ubu.buildURI(endUrl, new String[] { COMMAND_ID, NODE_IDENT }, new String[] { COMMAND_TREENODE_DROP, id }, flags.isIframePostEnabled() ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		target.append("<script type='text/javascript'>")
		 .append("Ext.get('dd").append(id).append("').dd = new Ext.fxMenuTree.DDProxy('dd").append(id).append("','").append(dndGroup).append("','").append(endUrl).append("','").append(feedBackUri).append("');")
		 .append("Ext.get('da").append(id).append("').dd = new Ext.fxMenuTree.DDProxy('da").append(id).append("','").append(dndGroup).append("','").append(endUrl).append("','").append(feedBackUri).append("');")
		 .append("</script>");
	}
	
	//fxdiff VCRP-9: drag and drop in menu tree
	private void appendDecorators(TreeNode curRoot, StringOutput target) {
		String deco1 = curRoot.getIconDecorator1CssClass();
		if (deco1 != null)
			target.append("<span class=\"b_tree_icon_decorator ").append(deco1).append("\"></span>");
		
		String deco2 = curRoot.getIconDecorator2CssClass();
		if (deco2 != null)
			target.append("<span class=\"b_tree_icon_decorator ").append(deco2).append("\"></span>");
		
		String deco3 = curRoot.getIconDecorator3CssClass();
		if (deco3 != null)
			target.append("<span class=\"b_tree_icon_decorator ").append(deco3).append("\"></span>");
		
		String deco4 = curRoot.getIconDecorator4CssClass();
		if (deco4 != null)
			target.append("<span class=\"b_tree_icon_decorator ").append(deco4).append("\"></span>");
	}
	
	private boolean isRenderChildren(INode curSel, TreeNode curRoot, boolean selected, MenuTree tree, Collection<String> openNodeIds) {
		if(curRoot.getChildCount() == 0) {
			//nothing to do
			return false;
		}
		
		//open open nodes
		if(openNodeIds != null && !openNodeIds.isEmpty()) {
			if(openNodeIds.contains(curRoot.getIdent())) {
				return true;
			} else if (curRoot.getUserObject() instanceof String && openNodeIds.contains(curRoot.getUserObject())) {
				return true;
			}
		}
		
		//don't automatically open the children of the selected node
		if(selected && !tree.isExpandSelectedNode()) {
			return false;
		}
		//open the path of the selected node
		return (curSel == curRoot);
	}

	/**
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#renderHeaderIncludes(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator)
	 */
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
		// nothing to include
	}

	/**
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#renderBodyOnLoadJSFunctionCall(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component)
	 */
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
	//
	}

}