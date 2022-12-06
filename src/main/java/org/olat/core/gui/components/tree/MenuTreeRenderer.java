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
import static org.olat.core.gui.components.tree.MenuTree.COMMAND_TREENODE_INSERT_DOWN;
import static org.olat.core.gui.components.tree.MenuTree.COMMAND_TREENODE_INSERT_REMOVE;
import static org.olat.core.gui.components.tree.MenuTree.COMMAND_TREENODE_INSERT_UNDER;
import static org.olat.core.gui.components.tree.MenuTree.COMMAND_TREENODE_INSERT_UP;
import static org.olat.core.gui.components.tree.MenuTree.NODE_IDENT;
import static org.olat.core.gui.components.velocity.VelocityContainer.COMMAND_ID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.tree.InsertionPoint.Position;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
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

	@Override
	public void renderComponent(Renderer renderer, StringOutput target, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		MenuTree tree = (MenuTree) source;
		if(tree.getMenuTreeItem() != null) {
			tree.getMenuTreeItem().clearVisibleNodes();
		}
		
		TreeNode root = tree.getTreeModel().getRootNode();
		if (root == null) return; // tree is completely empty
		INode selNode = tree.getSelectedNode();
		Collection<String> openNodeIds = tree.getOpenNodeIds();

		List<INode> selPath = new ArrayList<>(5);
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
		if(tree.isInsertToolEnabled()) {
			target.append(" o_tree_insert_tool");
		}
		// add element CSS
		if(StringHelper.containsNonWhitespace(tree.getElementCssClass())) {
			target.append(" ").append(tree.getElementCssClass());
		}
		target.append("' role='navigation'><ul class=\"o_tree_l0\">");
		if(tree.isRootVisible()) {
			renderLevel(renderer, target, 0, root, selPath, openNodeIds, elements, ubu, flags, tree, true);
		} else {
			selPath.remove(0);
			int chdCnt = root.getChildCount();
			for (int i = 0; i < chdCnt; i++) {
				TreeNode curChd = (TreeNode)root.getChildAt(i);
				boolean lastLevelNode = i == chdCnt-1;
				renderLevel(renderer, target, 0, curChd, selPath, openNodeIds, elements, ubu, flags, tree, lastLevelNode);
			}
		}
		target.append("</ul>");
		appendDragAndDropScript(elements, tree, target)
		      .append("</div>");
	}

	private void renderLevel(Renderer renderer, StringOutput target, int level, TreeNode curRoot, List<INode> selPath,
			Collection<String> openNodeIds, List<DndElement> dndElements, URLBuilder ubu, AJAXFlags flags,
			MenuTree tree, boolean lastLevelNode) {

		INode curSel = null;
		if (level < selPath.size()) {
			curSel = selPath.get(level);
		}
		
		boolean selected = (!selPath.isEmpty() && selPath.get(selPath.size() - 1) == curRoot);
		boolean hasInsertionPoint = isInsertionPointUnderNode(curRoot, tree);
		boolean hasChildren = hasVisibleChildren(curRoot, tree);
		boolean renderChildren = isRenderChildren(curSel, curRoot, selected, tree, openNodeIds)
				|| hasInsertionPoint;
		boolean lastTreeNode = lastLevelNode && !renderChildren;

		renderInsertionPoint(target, Position.up, level, curRoot, ubu, flags, tree);

		// item icon css class and icon decorator (for each icon quadrant a div, eclipse style)
		String cssClass = curRoot.getCssClass();
		target.append("<li class='");
		// add custom css class
		target.append(cssClass, cssClass != null);
		if (lastTreeNode) {
			target.append(" o_last_node");
		}
		if (tree.isHighlightSelection()) {
			if (selected) {
				target.append(" active");
			} else if (curSel == curRoot) {
				target.append(" active_parent");
			}
		}
		if (hasChildren && renderChildren) {
			// add css class to identify opened elements
			target.append(" children_visible");	
		}
		String ident = curRoot.getIdent();
		target.append("' data-nodeid='").append(ident).append("'>");			
		target.append("<div id='dd").append(ident).append("' class='o_tree_l").append(level);
		if(tree.isDragEnabled() || tree.isDropEnabled()) {
			target.append(" o_dnd_item");
		}
		if (tree.isHighlightSelection()) {
			if(selected) {
				target.append(" active");
			}
		}
		target.append("'>");
		
		if(tree.isDragEnabled() || tree.isDropEnabled()) {
			appendDragAndDropElement(curRoot, tree, dndElements, ubu, flags);
		}

		if(hasChildren || hasInsertionPoint) {
			renderOpenClose(curRoot, target, level, renderChildren, ubu, flags, tree);
		}
		// Render menu item as link, also for active elements
		// mark active item as strong for accessibility reasons
		renderLink(renderer, curRoot, level, selected, renderChildren, curSel, target, ubu, flags, tree);
		
		if(selected && tree.isInsertToolEnabled()) {
			renderInsertCallout(target, curRoot, ubu, flags, tree);
		}
		target.append("</div>");
		
		//append div to drop as sibling
		if(!renderChildren && (tree.isDragEnabled() || tree.isDropSiblingEnabled())) {
			appendSiblingDropObj(curRoot, level, tree, target, false);
		}
		
		if (renderChildren) {
			//open / close ul
			renderChildren(renderer, target, level, curRoot, selPath, openNodeIds, dndElements, ubu, flags, tree, lastLevelNode);
			
			//append div to drop as sibling after the children
			if(tree.isDragEnabled() || tree.isDropSiblingEnabled()) {
				appendSiblingDropObj(curRoot, level, tree, target, true);
			}
		}
		
		//	 close item level
		target.append("</li>");
		
		renderInsertionPoint(target, Position.down, level, curRoot, ubu, flags, tree);
	}
	
	private void renderCheckbox(StringOutput sb, TreeNode node, MenuTree tree) {
		MenuTreeItem treeItem = tree.getMenuTreeItem();
		if(treeItem != null) {
			boolean enabled = treeItem.isEnabled();
			boolean selected = tree.isSelected(node);
			boolean intermediate = treeItem.isIndeterminate(node);
			String groupingName = "tcb_ms";
			String id = "cb" + node.getIdent();
			sb.append("<input type='checkbox' id='").append(id).append("' ")
			  .append(" name='").append(groupingName).append("'")
			  .append(" value='").append(node.getIdent()).append("'");
			if (selected) {
				sb.append(" checked='checked' ");
			}
			if(enabled){
				//use the selection form dispatch id and not the one of the element!
				sb.append(FormJSHelper.getRawJSFor(treeItem.getRootForm(), treeItem.getFormDispatchId(), FormEvent.ONCLICK));
			} else {
				sb.append(" disabled='disabled' ");
			}
			sb.append(" />");
			if(intermediate) {
				sb.append("<script>\n")
				  .append("/* <![CDATA[ */\n")
				  .append("jQuery(function() {\n")
				  .append("  jQuery('#").append(id).append("').prop('indeterminate', true);")
				  .append("});\n")
				  .append("/* ]]> */")
				  .append("</script>\n");
			}
		}
	}
	
	private void renderInsertionPoint(StringOutput sb, Position positionToRender, int level, TreeNode node,
			URLBuilder ubu, AJAXFlags flags, MenuTree tree) {
		if(tree.getInsertionPoint() != null
				&& tree.getInsertionPoint().getPosition() == positionToRender
				&& tree.getInsertionPoint().getNodeId().equals(node.getIdent())) {
			
			sb.append("<li><div class='o_tree_l").append(level).append("'>")
			  .append("<span class=\"o_tree_leaf o_tree_oc_l").append(level).append("\">&nbsp;</span>")
			  .append("<span class='o_tree_link o_tree_l").append(level).append(" o_insertion_point'><a href=\"");
			
			ubu.buildHrefAndOnclick(sb, null, flags.isIframePostEnabled(), false, false,
					new NameValuePair(COMMAND_ID, COMMAND_TREENODE_INSERT_REMOVE),
					new NameValuePair(NODE_IDENT, node.getIdent()));

			Translator pointTranslator = Util.createPackageTranslator(MenuTreeRenderer.class, tree.getTranslator().getLocale());
			String pointTranslation = pointTranslator.translate("insertion.point");
			sb.append("><span>").append(pointTranslation).append(" <i class='o_icon o_icon_remove'> </i></span>")
			  .append("</a></span></div></li>");
		}	
	}
	
	private void renderOpenClose(TreeNode curRoot, StringOutput target, int level, boolean renderChildren, URLBuilder ubu, AJAXFlags flags, MenuTree tree) {
		int chdCnt = curRoot.getChildCount();
		// expand icon
		// add ajax support and real open/close function
		if (((tree.isRootVisible() && level != 0) || !tree.isRootVisible()) && chdCnt > 0) { // root has not open/close icon,  append open / close icon only if there is children
			target.append("<a ");

			ubu.buildHrefAndOnclick(target, null, flags.isIframePostEnabled(), tree.getMenuTreeItem() != null, true,
					new NameValuePair(COMMAND_ID, COMMAND_TREENODE_CLICKED),
					new NameValuePair(NODE_IDENT, curRoot.getIdent()),
					new NameValuePair(COMMAND_TREENODE, renderChildren ? MenuTree.TREENODE_CLOSE : MenuTree.TREENODE_OPEN));

			String openCloseCss = renderChildren ? "close" : "open";
			target.append(" class='o_tree_oc_l").append(level).append("'><i class='o_icon o_icon_").append(openCloseCss).append("_tree'></i></a>");
		} else if (level != 0 && chdCnt == 0) {
			target.append("<span class=\"o_tree_leaf o_tree_oc_l").append(level).append("\">&nbsp;</span>");
		}
	}
	
	private void renderLink(Renderer renderer, TreeNode curRoot, int level, boolean selected, boolean renderChildren, INode curSel,
			StringOutput target, URLBuilder ubu, AJAXFlags flags, MenuTree tree) {
		int chdCnt = curRoot.getChildCount();
		boolean iframePostEnabled = flags.isIframePostEnabled();
		
		if(tree.getMenuTreeItem() != null) {
			tree.getMenuTreeItem().trackVisibleNode(curRoot);
		}
		
		target.append("<span class='o_tree_link o_tree_l").append(level);
		
		if (tree.isHighlightSelection()) {
			if (selected) {
				target.append(" active");
			} else if (curSel == curRoot) {
				target.append(" active_parent");
			}
		}
		
		boolean insertionSource = (tree.getTreeModel() instanceof InsertionTreeModel
				&& ((InsertionTreeModel)tree.getTreeModel()).isSource(curRoot));
		if(insertionSource) {
			target.append(" o_insertion_source");
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
		target.append("'>");
		
		if(tree.isMultiSelect() && tree.getMenuTreeItem() != null) {
			renderCheckbox(target, curRoot, tree);
		}

		// Build menu item URI
		target.append("<a ");	
		boolean dirtyCheck = tree.getMenuTreeItem() == null || !tree.getMenuTreeItem().isNoDirtyCheckOnClick();
		ubu.buildHrefAndOnclick(target, null, iframePostEnabled, dirtyCheck, true,
				new NameValuePair(COMMAND_ID, COMMAND_TREENODE_CLICKED),
				new NameValuePair(NODE_IDENT, curRoot.getIdent()));
		
		// Add menu item title as alt hoover text
		String alt = curRoot.getAltText();
		if (alt != null) {
			target.append(" title=\"")
			      .appendHtmlEscaped(alt)
			      .append("\"");
		}
		target.append(">");

		String iconCssClass = curRoot.getIconCssClass();
		if (iconCssClass != null) {
			target.append("<i class='o_icon o_icon-fw ").append(iconCssClass).append("'></i> ");			
		}
		renderDisplayTitle(renderer, target, curRoot, tree);
		// display title and close menu item
		
		appendDecorators(curRoot, target);
		target.append("</a></span>");
	}
	
	private void renderDisplayTitle(Renderer renderer, StringOutput target, TreeNode node, MenuTree tree) {
		target.append("<span ");
		if(tree.isDragEnabled() || tree.isDropEnabled()) {
			if(tree.isDragEnabled()) {
				target.append(" class='o_dnd_item'");
			} else {
				target.append(" class='o_tree_item'");
			}
			target.append(" id='da").append(node.getIdent()).append("'");
		} else {
			target.append(" class='o_tree_item'");
		}
		target.append(">");

		// render link
		String title = node.getTitle();
		if(title != null && title.equals("")) {
			target.append("&nbsp;");
		} else {
			StringHelper.escapeHtml(target, title);
		}
		target.append("</span>");
		//render badge
		if(node.getBadge() != null) {
			target.append("&nbsp;");
			renderer.render(node.getBadge(), target, new String[] {});
		}
	}
	
	private void renderInsertCallout(StringOutput sb, TreeNode node, URLBuilder ubu, AJAXFlags flags, MenuTree tree) {
		Position[] positionArr;
		if(tree.getTreeModel() instanceof InsertionTreeModel) {
			positionArr = ((InsertionTreeModel)tree.getTreeModel()).getInsertionPosition(node);
		} else if(tree.getTreeModel().getRootNode() != node) {
			positionArr = new Position[] { Position.under };
		} else {
			positionArr = new Position[] { Position.up, Position.down, Position.under };
		}
		if(positionArr.length > 0) {
			sb.append("<div class='popover right show'>")
			  .append("<div class='arrow'></div>")
			  .append("<div class='popover-content btn-group'>");

			if(Position.hasPosition(Position.up, positionArr)) {
				renderInsertCalloutButton(COMMAND_TREENODE_INSERT_UP, "o_icon_node_before", sb, node, ubu, flags);
			}
			if(Position.hasPosition(Position.down, positionArr)) {
				renderInsertCalloutButton(COMMAND_TREENODE_INSERT_DOWN, "o_icon_node_after", sb, node, ubu, flags);
			}
			if(Position.hasPosition(Position.under, positionArr)) {
				renderInsertCalloutButton(COMMAND_TREENODE_INSERT_UNDER, "o_icon_node_under o_icon-rotate-180", sb, node, ubu, flags);
			}
			
			sb.append("</div></div>");
		}
	}
	
	private void renderInsertCalloutButton(String cmd, String cssClass,  StringOutput sb, TreeNode node, URLBuilder ubu, AJAXFlags flags) {
		sb.append("<a class='btn btn-default small' ");
		ubu.buildHrefAndOnclick(sb, flags.isIframePostEnabled(),
				new NameValuePair(COMMAND_ID, cmd),
				new NameValuePair(NODE_IDENT, node.getIdent()));
		sb.append("><i class='o_icon ").append(cssClass).append("'> </i></a>");
	}
	
	private void renderChildren(Renderer renderer, StringOutput target, int level, TreeNode curRoot, List<INode> selPath, Collection<String> openNodeIds,
			List<DndElement> dndElements, URLBuilder ubu, AJAXFlags flags, MenuTree tree, boolean parentIsLastNode) {
		int chdCnt = curRoot.getChildCount();
		// render children as new level
		target.append("\n<ul class=\"");
		// add css class to identify level
		target.append(" o_tree_l").append(level + 1)
		      .append("\">");

		renderInsertionPoint(target, Position.under, level + 1, curRoot, ubu, flags, tree);

		// render all the nodes from this level
		List<TreeNode> visibleNodes = new ArrayList<>();
		for (int i = 0; i < chdCnt; i++) {
			TreeNode curChd = (TreeNode) curRoot.getChildAt(i);
			if(tree.getFilter().isVisible(curChd)) {
				visibleNodes.add(curChd);
			}
		}
		for (int j = 0; j < visibleNodes.size(); j++) {
			TreeNode treeNode = visibleNodes.get(j);
			boolean lastNode = parentIsLastNode && j == visibleNodes.size()-1;
			renderLevel(renderer, target, level + 1, treeNode, selPath, openNodeIds, dndElements, ubu, flags, tree, lastNode);
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
		sb.append("<script>\n")
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
		appendDecorator(node.getIconDecorator1CssClass(), sb, 1);
		appendDecorator(node.getIconDecorator2CssClass(), sb, 2);
		appendDecorator(node.getIconDecorator3CssClass(), sb, 3);
		appendDecorator(node.getIconDecorator4CssClass(), sb, 4);
	}

	private void appendDecorator(String decorator, StringOutput sb, int num) {
		if (decorator != null && decorator.length() > 0) {
			sb.append("<span class='badge o_badge_").append(num).append(" ").append(decorator).append("'><i class='o_icon ").append(decorator).append("'></i></span>");
		}
	}
	
	private boolean isRenderChildren(INode curSel, TreeNode curRoot, boolean selected, MenuTree tree, Collection<String> openNodeIds) {
		if(!hasVisibleChildren(curRoot, tree)) {
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
	
	private boolean hasVisibleChildren(TreeNode curRoot, MenuTree tree) {
		boolean hasVisibleChild = false;
		if(curRoot.getChildCount() == 0) {
			//nothing to do
		} else if(tree.getFilter() != MenuTree.DEF_FILTER) {
			for(int i=curRoot.getChildCount(); i-->0 && !hasVisibleChild; ) {
				if(tree.getFilter().isVisible(curRoot.getChildAt(i))) {
					hasVisibleChild = true;
				}
			}
		} else {
			hasVisibleChild = true;
		}
		return hasVisibleChild;
	}
	
	private boolean isInsertionPointUnderNode(TreeNode curRoot, MenuTree tree) {
		return tree.getInsertionPoint() != null
				&& tree.getInsertionPoint().getPosition() == Position.under
				&& tree.getInsertionPoint().getNodeId().equals(curRoot.getIdent());
	}
}