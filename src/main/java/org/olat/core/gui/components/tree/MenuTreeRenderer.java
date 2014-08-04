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

import static org.olat.core.gui.components.tree.MenuTree.COMMAND_TREENODE;
import static org.olat.core.gui.components.tree.MenuTree.COMMAND_TREENODE_CLICKED;
import static org.olat.core.gui.components.tree.MenuTree.COMMAND_TREENODE_DROP;
import static org.olat.core.gui.components.tree.MenuTree.NODE_IDENT;
import static org.olat.core.gui.components.velocity.VelocityContainer.COMMAND_ID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.nodes.INode;

/**
 * enclosing_type Description: <br>
 * 
 * @author Felix Jost, Florian Gnaegi
 */
public class MenuTreeRenderer extends DefaultComponentRenderer {

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
		
		List<DndElement> elements = new ArrayList<>();
		AJAXFlags flags = renderer.getGlobalSettings().getAjaxFlags();
		target.append("\n<div class='o_tree");
		// marker classes to differentiate rendering when root node is visible 
		if(!tree.isRootVisible()) {
			target.append(" o_tree_root_hidden");
		}
		else {
			target.append(" o_tree_root_visible");
		}
		target.append("'><ul class=\"o_tree_l0\">");
		if(tree.isRootVisible()) {
			renderLevel(target, 0, root, selPath, openNodeIds, elements, ubu, flags, tree);
		} else {
			selPath.remove(0);
			int chdCnt = root.getChildCount();
			for (int i = 0; i < chdCnt; i++) {
				TreeNode curChd = (TreeNode)root.getChildAt(i);
				renderLevel(target, 0, curChd, selPath, openNodeIds, elements, ubu, flags, tree);
			}
		}
		target.append("</ul>");
		appendDragAndDropScript(elements, tree, target)
		      .append("</div>");
	}

	private void renderLevel(StringOutput target, int level, TreeNode curRoot, List<INode> selPath,
			Collection<String> openNodeIds, List<DndElement> dndElements, URLBuilder ubu, AJAXFlags flags, MenuTree tree) {	

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
		target.append("\n<li class='");
		// add custom css class
		target.append(cssClass, cssClass != null);
		if(selected) {
			target.append(" active");
		} else if (curSel == curRoot) {
			// add css class to identify parents of active element
			target.append(" active_parent");			
		}
		String ident = curRoot.getIdent();
		target.append("'><div id='dd").append(ident).append("' class='o_tree_l").append(level);
		if(tree.isDragEnabled() || tree.isDropEnabled()) {
			target.append(" o_dnd_item");
		}
		if(selected) {
			target.append(" active");
		}
		target.append("'>");
		
		if(tree.isDragEnabled() || tree.isDropEnabled()) {
			appendDragAndDropElement(curRoot, tree, dndElements, ubu, flags);
		}

		// expand icon
		// add ajax support and real open/close function
		if (((tree.isRootVisible() && level != 0) || !tree.isRootVisible()) && chdCnt > 0) { // root has not open/close icon,  append open / close icon only if there is children
			target.append("<a onclick='o2cl_secure()' href=\"");
			
			// Build menu item URI
			String cmd = renderChildren ? MenuTree.TREENODE_CLOSE : MenuTree.TREENODE_OPEN;
			if (iframePostEnabled) {
				ubu.buildURI(target, new String[] { COMMAND_ID, NODE_IDENT, COMMAND_TREENODE }, new String[] { COMMAND_TREENODE_CLICKED, curRoot.getIdent(), cmd }, AJAXFlags.MODE_TOBGIFRAME);
			} else {
				ubu.buildURI(target, new String[] { COMMAND_ID, NODE_IDENT, cmd }, new String[] { COMMAND_TREENODE_CLICKED, curRoot.getIdent(), cmd });
			}
			
			target.append("\"");
			if(iframePostEnabled) {
				ubu.appendTarget(target);
			}
			String openCloseCss = renderChildren ? "close" : "open";
			target.append(" class='o_tree_oc_l").append(level).append("'><i class='o_icon o_icon_").append(openCloseCss).append("_tree'></i></a>");
		} else if (level != 0 && chdCnt == 0) {
			target.append("<span class=\"o_tree_leaf o_tree_oc_l").append(level).append("\">&nbsp;</span>");
		}
		
		// Render menu item as link, also for active elements
		// mark active item as strong for accessablity reasons
		
		target.append("<strong>", selected);
		target.append("<a class='o_tree_link o_tree_l").append(level);
		
		// add icon css class
		if (selected) {
			// add css class to identify active element
			target.append(" active");			
		} else if (curSel == curRoot) {
			// add css class to identify parents of active element
			target.append(" active_parent");			
		}
		
		//reapply the same rules to the second link
		if(level != 0 && chdCnt > 0) {
			if (renderChildren) {
				target.append(" o_tree_level_label_close");
			} else  {
				target.append(" o_tree_level_label_open");
			}
		} else if (level != 0 && chdCnt == 0) {
			target.append(" o_tree_level_label_leaf");
		}
		
		// add css class to identify level, FireFox script
		target.append("' onclick='o2cl_secure()' href=\"");					
		
		// Build menu item URI
		if (iframePostEnabled) {
			ubu.buildURI(target, new String[] { COMMAND_ID, NODE_IDENT }, new String[] { COMMAND_TREENODE_CLICKED, curRoot.getIdent() }, AJAXFlags.MODE_TOBGIFRAME);
		} else {
			ubu.buildURI(target, new String[] { COMMAND_ID, NODE_IDENT }, new String[] { COMMAND_TREENODE_CLICKED, curRoot.getIdent() });
		}

		target.append("\"");	
		
		// Add menu item title as alt hoover text
		String alt = curRoot.getAltText();
		if (alt != null) {
			target.append(" title=\"")
			      .append(StringEscapeUtils.escapeHtml(alt).toString())
			      .append("\"");
		}

		if (iframePostEnabled) {
			ubu.appendTarget(target);
		}
		target.append(">");

		String iconCssClass = curRoot.getIconCssClass();
		if (iconCssClass != null) {
			target.append("<i class='o_icon ").append(iconCssClass).append("'></i> ");			
		}
		
		// display title and close menu item
		target.append("<span");
		if(tree.isDragEnabled() || tree.isDropEnabled()) {
			if(tree.isDragEnabled()) {
				target.append(" class='o_dnd_item'");
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
		target.append("</span>");
		appendDecorators(curRoot, target);
		target.append("</a>").append("</strong>", selected)
		      .append("</div>");
		
		//append div to drop as sibling
		if(!renderChildren && (tree.isDragEnabled() || tree.isDropSiblingEnabled())) {
			appendSiblingDropObj(curRoot, level, tree, target, false);
		}
		
		if (renderChildren) {
			//open / close ul
			renderChildren(target, level, curRoot, selPath, openNodeIds, dndElements, ubu, flags, tree);
			
			//append div to drop as sibling after the children
			if(tree.isDragEnabled() || tree.isDropSiblingEnabled()) {
				appendSiblingDropObj(curRoot, level, tree, target, true);
			}
		}
		
		//	 close item level
		target.append("</li>");
	}
	
	private void renderChildren(StringOutput target, int level, TreeNode curRoot, List<INode> selPath, Collection<String> openNodeIds,
			List<DndElement> dndElements, URLBuilder ubu, AJAXFlags flags, MenuTree tree) {
		int chdCnt = curRoot.getChildCount();
		// render children as new level
		target.append("\n<ul class=\"");
		// add css class to identify level
		target.append(" o_tree_l").append(level + 1);		
		target.append("\">");
		// render all the nodes from this level
		for (int i = 0; i < chdCnt; i++) {
			TreeNode curChd = (TreeNode) curRoot.getChildAt(i);
			renderLevel(target, level + 1, curChd, selPath, openNodeIds, dndElements, ubu, flags, tree);
		}
		target.append("</ul>");
	}
	
	private void appendSiblingDropObj(TreeNode node, int level, MenuTree tree, StringOutput target, boolean after) {
		boolean drop = tree.isDropEnabled() && ((DnDTreeModel)tree.getTreeModel()).isNodeDroppable(node);
		if(drop) {
			String id = (after ? "dt" : "ds") + node.getIdent();
			target.append("<div id='").append(id).append("' class='o_dnd_sibling o_dnd_l").append(level).append("'>&nbsp;</div>");
		}
	}
	
	private void appendDragAndDropElement(TreeNode node, MenuTree tree, List<DndElement> target, URLBuilder ubu, AJAXFlags flags) {
		String id = node.getIdent();
		boolean drag = tree.isDragEnabled() && ((DnDTreeModel)tree.getTreeModel()).isNodeDraggable(node);
		boolean drop = tree.isDropEnabled() && ((DnDTreeModel)tree.getTreeModel()).isNodeDroppable(node);
		if(drag || drop) {
			DndElement el = new DndElement();
			el.setId(id);
			if(drag) {
				el.setDrag(drag);
			}
			if(drop) {
				el.setDrop(true);
				StringOutput endUrl = new StringOutput(64);
				ubu.buildURI(endUrl, new String[] { COMMAND_ID, NODE_IDENT }, new String[] { COMMAND_TREENODE_DROP, node.getIdent() }, flags.isIframePostEnabled() ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
				el.setEndUrl(endUrl.toString());
			}
			target.add(el);
		}
	}

	private StringOutput appendDragAndDropScript(List<DndElement> elements, MenuTree tree, StringOutput sb) {
		if(elements == null || elements.isEmpty()) return sb;
		sb.append("<script type='text/javascript'>\n")
		  .append("/* <![CDATA[ */\n")
		  .append("jQuery(function() {\n");
		
		StringBuilder dragIds = new StringBuilder("[");
		StringBuilder dropIds = new StringBuilder("[");
		for(DndElement element:elements) {
			if(element.isDrag()) {
				if(dragIds.length() > 1) {
					dragIds.append(",");
				}
				dragIds.append("'").append(element.getId()).append("'");
			}
			if(element.isDrop()) {
				if(dropIds.length() > 1) {
					dropIds.append(",");
				}
				dropIds.append("['").append(element.getId()).append("','").append(element.getEndUrl()).append("']");
			}
		}
		dragIds.append("]");
		dropIds.append("]");
		
		if(dragIds.length() > 2) {
			sb.append("  jQuery.each(").append(dragIds).append(", function(index, value) {\n")
			  .append("    jQuery('#da' + value)");
			appendDraggable(sb).append("\n")
			  .append("    jQuery('#dd' + value)");
			appendDraggable(sb).append("\n")
			  .append("  });\n");
		}
		if(dropIds.length() > 2) {
			String acceptMethod = tree.getDndAcceptJSMethod();
			if(acceptMethod == null) {
				acceptMethod = "treeAcceptDrop";
			}
			sb.append("  jQuery.each(").append(dropIds).append(", function(index, value) {\n")
			  .append("    jQuery('#dd' + value[0]).droppable({ endUrl:value[1],hoverClass:'o_dnd_over',").append("accept:").append(acceptMethod).append(",drop:onTreeDrop});\n");
		    if(tree.isDropSiblingEnabled()) {
		    	sb.append("    jQuery('#dt' + value[0]).droppable({ endUrl:value[1],hoverClass:'o_dnd_over',").append("accept:").append(acceptMethod).append(",drop:onTreeDrop});\n")
		    	  .append("    jQuery('#ds' + value[0]).droppable({ endUrl:value[1],hoverClass:'o_dnd_over',").append("accept:").append(acceptMethod).append(",drop:onTreeDrop});\n");
		    }
		    sb.append("  });\n");
		}
		
		sb.append("});\n")
		  .append("/* ]]> */")
		  .append("</script>\n");
		return sb;
	}
	
	private StringOutput appendDraggable(StringOutput sb) {
		sb.append(".draggable({start:onTreeStartDrag, stop: onTreeStopDrag, delay:100, distance:5, revert:'invalid' });");
		return sb;
	}
	
	private static class DndElement {
		private String id;
		private String endUrl;
		private boolean drag,drop;
		
		public String getId() {
			return id;
		}
		
		public void setId(String id) {
			this.id = id;
		}
		
		public String getEndUrl() {
			return endUrl;
		}
		
		public void setEndUrl(String endUrl) {
			this.endUrl = endUrl;
		}
		
		public boolean isDrag() {
			return drag;
		}
		
		public void setDrag(boolean drag) {
			this.drag = drag;
		}
		
		public boolean isDrop() {
			return drop;
		}
		
		public void setDrop(boolean drop) {
			this.drop = drop;
		}

	}
	
	private void appendDecorators(TreeNode node, StringOutput sb) {
		appendDecorator(node.getIconDecorator1CssClass(), sb);
		appendDecorator(node.getIconDecorator2CssClass(), sb);
		appendDecorator(node.getIconDecorator3CssClass(), sb);
		appendDecorator(node.getIconDecorator4CssClass(), sb);
	}

	private void appendDecorator(String decorator, StringOutput sb) {
		if (decorator != null && decorator.length() > 0) {
			sb.append("<span class='badge ").append(decorator).append("'><i class='o_icon ").append(decorator).append("'></i></span>");
		}
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
}