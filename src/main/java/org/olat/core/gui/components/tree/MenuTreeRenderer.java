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
import org.olat.core.util.StringHelper;
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

		List<INode> selPath = new ArrayList<INode>(5);
		INode cur = selNode;
		if (cur == null && !tree.isUnselectNodes()) {
			cur = root; 
		} else if(cur == null && tree.isUnselectNodes() && openNodeIds.isEmpty()) {
			openNodeIds.add(root.getIdent());//always open the root
		}
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
		if(tree.isDragEnabled() || tree.isDropEnabled()) {
			target.append(" b_dd_ct");
		}
		if(!tree.isRootVisible()) {
			target.append(" b_tree_root_hidden");
		}
		target.append("'><ul class=\"b_tree_l0\">");
		if(tree.isRootVisible()) {
			renderLevel(target, 0, root, selPath, openNodeIds, ubu, flags, tree);
		} else {
			selPath.remove(0);
			int chdCnt = root.getChildCount();
			for (int i = 0; i < chdCnt; i++) {
				TreeNode curChd = (TreeNode)root.getChildAt(i);
				renderLevel(target, 0, curChd, selPath, openNodeIds, ubu, flags, tree);
			}
		}
		target.append("</ul>").append("</div>");
	}

	private void renderLevel(StringOutput target, int level, TreeNode curRoot, List<INode> selPath,
			Collection<String> openNodeIds, URLBuilder ubu, AJAXFlags flags, MenuTree tree) {	

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
		} else if (curSel == curRoot) {
			// add css class to identify parents of active element
			target.append(" b_tree_selected_parents");			
		}
		String ident = curRoot.getIdent();
		target.append("\"><div id='dd").append(ident).append("' class=\"b_tree_item_wrapper");
		if(tree.isDragEnabled() || tree.isDropEnabled()) {
			target.append(" b_dd_item");
		}
		if(selected) {
			target.append(" b_tree_selected");
		}
		target.append("\">");
		
		if(tree.isDragEnabled() || tree.isDropEnabled()) {
			appendDragAndDropObj(curRoot, tree, target, ubu, flags);
		}

		// expand icon
		// add ajax support and real open/close function
		if (((tree.isRootVisible() && level != 0) || !tree.isRootVisible()) && chdCnt > 0) { // root has not open/close icon,  append open / close icon only if there is children
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
		//target.append("\" onclick=\"try {if(o2cl()){$('html, body').animate({scrollTop: $('#b_top').offset().top}, 500); return true;} else {return false;}} catch(e){return false}\" href=\"");					
		target.append("\" onclick=\"try {if(o2cl()){ return true;} else {return false;}} catch(e){return false}\" href=\"");					
		
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
		String alt = curRoot.getAltText();
		if (alt != null) {
			target.append("\" title=\"");
			target.append(StringEscapeUtils.escapeHtml(alt).toString());
		}
		
		target.append("\"");
		
		if (iframePostEnabled) {
			ubu.appendTarget(target);
		}
		target.append(">");

		
		appendDecorators(curRoot, target);
		
		// display title and close menu item
		target.append("<span");
		if(tree.isDragEnabled() || tree.isDropEnabled()) {
			if(tree.isDragEnabled()) {
				target.append(" class='b_dd_item'");
			}
			target.append(" id='da").append(ident).append("'");
		}
		target.append(">");

		// render link
		String title = curRoot.getTitle();
		if(title != null && title.equals("")) {
			target.append("&nbsp;");
		} else {
			StringHelper.escapeHtml(target, title);
		}
		target.append("</span></a>");
		// mark active item as strong for accessablity reasons
		target.append(selected ? "</strong>" : "");
		target.append("</div>");
		
		//append div to drop as sibling
		if(!renderChildren && (tree.isDragEnabled() || tree.isDropSiblingEnabled())) {
			appendSiblingDropObj(curRoot, level, tree, target, ubu, flags, false);
		}
		
		if (renderChildren) {
			//open / close ul
			renderChildren(target, level, curRoot, selPath, openNodeIds, ubu, flags, tree);
			
			//append div to drop as sibling after the children
			if(tree.isDragEnabled() || tree.isDropSiblingEnabled()) {
				appendSiblingDropObj(curRoot, level, tree, target, ubu, flags, true);
			}
		}
		
		//	 close item level
		target.append("</li>");
	}
	
	//fxdiff VCRP-9: drag and drop in menu tree
	private void renderChildren(StringOutput target, int level, TreeNode curRoot, List<INode> selPath, Collection<String> openNodeIds, URLBuilder ubu, AJAXFlags flags, MenuTree tree) {
		int chdCnt = curRoot.getChildCount();
		// render children as new level
		target.append("\n<ul class=\"");
		// add css class to identify level
		target.append(" b_tree_l").append(level + 1);		
		target.append("\">");
		// render all the nodes from this level
		for (int i = 0; i < chdCnt; i++) {
			TreeNode curChd = (TreeNode) curRoot.getChildAt(i);
			renderLevel(target, level + 1, curChd, selPath, openNodeIds, ubu, flags, tree);
		}
		target.append("</ul>");
	}
	
	//fxdiff VCRP-9: drag and drop in menu tree
	private void appendSiblingDropObj(TreeNode node, int level, MenuTree tree, StringOutput target, URLBuilder ubu, AJAXFlags flags, boolean after) {
		boolean drop = tree.isDropEnabled() && ((DnDTreeModel)tree.getTreeModel()).isNodeDroppable(node);
		if(drop) {
			String id = (after ? "dt" : "ds") + node.getIdent();
			target.append("<div id='").append(id).append("' class='b_dd_sibling b_dd_sibling_l").append(level).append("'>")
				.append("<script type='text/javascript'>jQuery('#").append(id).append("')");
			appendDroppable(node, tree, target, ubu, flags);
			target.append("</script>&nbsp;&nbsp;</div>");
		}
	}
	
	//fxdiff VCRP-9: drag and drop in menu tree
	private void appendDragAndDropObj(TreeNode node, MenuTree tree, StringOutput target, URLBuilder ubu, AJAXFlags flags) {
		String id = node.getIdent();
		boolean drag = tree.isDragEnabled() && ((DnDTreeModel)tree.getTreeModel()).isNodeDraggable(node);
		boolean drop = tree.isDropEnabled() && ((DnDTreeModel)tree.getTreeModel()).isNodeDroppable(node);
		if(drag || drop) {
			target.append("<script type='text/javascript'>");
			if(drag) {
				target.append("jQuery('#dd").append(id).append("')");
				appendDraggable(target);
				target.append("jQuery('#da").append(id).append("')");
				appendDraggable(target);
			}
			if(drop) {
				target.append("jQuery('#dd").append(id).append("')");
				appendDroppable(node, tree, target, ubu, flags);
			}
			target.append("</script>");
		}
	}
	
	private void appendDroppable(TreeNode node, MenuTree tree, StringOutput sb, URLBuilder ubu, AJAXFlags flags) {
		String feedBackUri = tree.getDndFeedbackUri();
		StringOutput endUrl = new StringOutput(64);
		String acceptMethod = tree.getDndAcceptJSMethod();
		if(acceptMethod == null) {
			acceptMethod = "treeAcceptDrop";
		}
		ubu.buildURI(endUrl, new String[] { COMMAND_ID, NODE_IDENT }, new String[] { COMMAND_TREENODE_DROP, node.getIdent() }, flags.isIframePostEnabled() ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		sb.append(".droppable({ fbUrl: '").append(feedBackUri).append("', endUrl: '").append(endUrl)
		  .append("', hoverClass:'b_dd_over', accept: ")
		  .append(acceptMethod).append(", drop:onTreeDrop});");
	}
	
	private void appendDraggable(StringOutput sb) {
		sb.append(".draggable({start:onTreeStartDrag, stop: onTreeStopDrag, delay:100, distance:5, revert:'invalid' });");
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

	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
		//
	}

	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
		//
	}
}