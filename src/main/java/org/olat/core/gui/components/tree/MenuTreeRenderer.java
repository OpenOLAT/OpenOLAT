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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.gui.components.tree;

import java.util.ArrayList;
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
	public void render(Renderer renderer, StringOutput target, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		MenuTree tree = (MenuTree) source;
		
		// unique ID used for DOM component identification
		String compPrefix = renderer.getComponentPrefix(tree);
		
		INode selNode = tree.getSelectedNode();
		TreeNode root = tree.getTreeModel().getRootNode();
		if (root == null) return; // tree is completely empty

		if (tree.isExpandServerOnly()) { 
			// render only the expanded path using no javascript
			List selPath = new ArrayList(5);
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
			target.append("\n<div class=\"b_tree\">");
			target.append("<ul class=\"b_tree_l0\">");
			renderLevel(target, 0, root, selPath, ubu, renderer.getGlobalSettings().getAjaxFlags(), compPrefix, tree.markingTreeNode);
			target.append("</ul>");
			target.append("\n</div>");
		} else {			
			throw new RuntimeException("Currently only server side menus implemented, sorry!");
		}
	}

	private void renderLevel(StringOutput target, int level, TreeNode curRoot, List selPath, URLBuilder ubu, AJAXFlags flags, String componentPrefix, TreeNode markedNode) {	
		//TODO make performant
		INode curSel = null;
		if (level < selPath.size()) {
			curSel = (INode) selPath.get(level);
		}

		// get css class

		// item icon css class and icon decorator (for each icon quadrant a div, eclipse style)

		// open menu item
		String cssClass = curRoot.getCssClass();
		target.append("\n<li class=\"");
		// add custom css class
		target.append((cssClass == null ? "" : cssClass));
		target.append("\">");
		
		/*if (menuitemsDraggable && curSel == curRoot) {
			//TODO:collect all ids as done in constructor and "[1,435,323].each(function(val){new Draggabl...."
			target.append("<script type=\"text/javascript\">new Draggable('").append(curId).append("', {revert:function(el){o_indrag=true;return true;}});</script>");
		}*/
		// render link
		String title = curRoot.getTitle();
		title = StringEscapeUtils.escapeHtml(title).toString();

		if (markedNode != null && markedNode == curRoot) {
			target.append("<span style=\"border:2px solid red;\">");
		}
		
		// Render menu item as link, also for active elements
		// mark active item as strong for accessablity reasons
		target.append(selPath.get(selPath.size()-1) == curRoot ? "<strong>" : "");
		target.append("<a class=\"");
		// add icon css class
		String iconCssClass = curRoot.getIconCssClass();
		if (iconCssClass != null) {
			target.append(" b_tree_icon ").append(iconCssClass);			
		}
		if (selPath.get(selPath.size()-1) == curRoot) {
			// add css class to identify active element
			target.append(" b_tree_selected");			
		} else if (curSel == curRoot) {
			// add css class to identify parents of active element
			target.append(" b_tree_selected_parents");			
		}
		// add css class to identify level
		target.append(" b_tree_l").append(level);		
		
		// fix needed for firefox bug when fast clicking: the onclick="try{return o2cl()}catch(e){return false}"  -> when the document is reloaded, all function js gets unloaded, but the old link can still be clicked.			
		target.append("\" onclick=\"try {if(o2cl()){Effect.ScrollTo('b_top'); return true;} else {return false;}} catch(e){return false}\" href=\"");					
		
		// Build menu item URI
		boolean iframePostEnabled = flags.isIframePostEnabled();
		if (GUIInterna.isLoadPerformanceMode()) {
			//	 if in load perf test -> generate the treeposition and include it as param, since the nodeid itself is random and thus not replayable			
			String treePath = TreeHelper.buildTreePath(curRoot);			
			ubu.buildURI(target, new String[] { "en" }, new String[] { treePath });
		} else {
			if (iframePostEnabled) {
				ubu.buildURI(target, new String[] { MenuTree.NODE_IDENT }, new String[] { curRoot.getIdent() }, AJAXFlags.MODE_TOBGIFRAME);
			} else {
				ubu.buildURI(target, new String[] { MenuTree.NODE_IDENT }, new String[] { curRoot.getIdent() });
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

		
		int chdCnt = curRoot.getChildCount();
		if (iconCssClass != null) {
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
		// expand icon
		// FIXME:FG: add ajax support and real open/close function
		if (level != 0) { // RootNode has no open / close icon
			if (chdCnt > 0) { // append open / close icon
				if (curSel == curRoot) {
					target.append("<span class=\"b_tree_level_close\"></span>");
				} else {
					target.append("<span class=\"b_tree_level_open\"></span>");
				}
			}
		}
		
		// display title and close menu item
		if(title != null && title.equals("")) title = "&nbsp;";
		target.append(title).append("</a>");
		// mark active item as strong for accessablity reasons
		target.append(selPath.get(selPath.size()-1) == curRoot ? "</strong>" : "");


		if (markedNode != null && markedNode == curRoot) {
			target.append("</span>");
		}
		
		if (curRoot.getChildCount() > 0 && curSel == curRoot) {
			// render children as new level
			target.append("\n<ul class=\"");
			// add css class to identify level
			target.append(" b_tree_l").append(level + 1 );		
			target.append("\">");
			// render all the nodes from this level
			for (int i = 0; i < chdCnt; i++) {
				TreeNode curChd = (TreeNode) curRoot.getChildAt(i);
				renderLevel(target, level + 1, curChd, selPath, ubu, flags, componentPrefix, markedNode);
			}
			target.append("</ul>");
		}
		
		//	 close item level
		target.append("</li>");
		
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