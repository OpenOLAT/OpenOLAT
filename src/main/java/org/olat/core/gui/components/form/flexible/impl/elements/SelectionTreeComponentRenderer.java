/**
 * 
 */
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * @author patrickb
 *
 */
class SelectionTreeComponentRenderer implements ComponentRenderer {

	private static String imgDots = "<div class=\"b_selectiontree_line\"></div>";
	private static String imgDots_spacer = "<div class=\"b_selectiontree_space\"></div>";
	private static String imgDots_nt = "<div class=\"b_selectiontree_junction\"></div>";
	private static String imgDots_nl = "<div class=\"b_selectiontree_end\"></div>";

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.ComponentRenderer#render(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component, org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator, org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	public void render(Renderer renderer, StringOutput sb, Component source,
			URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {

		String[] clonedArgs = args != null ? args.clone() : new String[1];
		int cnt = clonedArgs.length;
		clonedArgs[cnt-1] = CheckboxElementComponent.RENDERARG_ESCAPEHTML;//
		
		SelectionTreeComponent stc = (SelectionTreeComponent)source;
		Map<String,Component> checkboxes = stc.getSubComponents();
		
		TreeModel tm = stc.getTreeModel();
		TreeNode rootNode = tm.getRootNode();
		
		sb.append("<div class=\"b_selectiontree\">");		
		renderRootNode(rootNode, sb);
		renderChildNodes(rootNode, "", stc.hashCode(), sb, renderer, checkboxes, args);		
		sb.append("</div>");
	}

	private void renderRootNode(TreeNode root, StringOutput target) {
		target.append("\n<div class=\"b_selectiontree_item\">");
		renderNodeIcon(target, root);
		target.append("<div class=\"b_selectiontree_content\">");
		// text using css if available
		String cssClass = root.getCssClass();
		if (cssClass != null) target.append("<span class=\"").append(cssClass).append("\">");
		target.append(StringEscapeUtils.escapeHtml(root.getTitle()));
		if (cssClass != null) target.append("</span>");
		target.append("</div></div>");
	}

	private void renderChildNodes(TreeNode root, String indent, int treeID, StringOutput sb, Renderer renderer, Map<String,Component> checkboxes, String[] args) {
		String newIndent = indent + imgDots;

		// extract directories
		int childcnt = root.getChildCount();
		for (int i = 0; i < childcnt; i++) {
			TreeNode child = (TreeNode) root.getChildAt(i);
			// BEGIN  of choice div
			sb.append("\n<div class=\"b_selectiontree_item\">");
			// render all icons first
			// indent and dots-images
			sb.append(indent);
			if (i < childcnt - 1) {
				sb.append(imgDots_nt);
			} else {
				sb.append(imgDots_nl);
			}
			// custom icon if available
			renderNodeIcon(sb, child);
			sb.append("<div class=\"b_selectiontree_content\">");
			
			// append radio or checkbox if selectable
			if (child.isAccessible()) {
				 // render radioboxes
				// StaticTextElement or CheckboxElementComponent
				renderer.render(sb, checkboxes.get(child.getIdent()), args);
			} else {
				// node title (using css if available)
				String cssClass = child.getCssClass();
				if (cssClass != null) sb.append("<span class=\"").append(cssClass).append("\">");
				if (!child.isAccessible()) {
					sb.append("<span class=\"b_disabled\">");
					sb.append(StringEscapeUtils.escapeHtml(child.getTitle()));											
					sb.append("</span>");
				} else {
					if (child.getAltText() != null) {
//						sb.append("<span ");
//						sb.append(" onmouseover=\"o_showEventDetails(' ', '");
//						sb.append(child.getAltText());
//						sb.append("');\" onmouseout=\"return nd();\" onclick=\"return nd();\">");
						sb.append("<span>");
						sb.append(StringEscapeUtils.escapeHtml(child.getTitle()));						
						sb.append("</span>");
					} else {
						sb.append(StringEscapeUtils.escapeHtml(child.getTitle()));						
					}
				}
				if (cssClass != null) sb.append("</span>");
			}
			// END of choice div
			sb.append("</div></div>"); 

			// do the same for all children
			if (i < childcnt - 1) {
				renderChildNodes(child, newIndent, treeID, sb, renderer, checkboxes, args);
			} else {
				renderChildNodes(child, indent + imgDots_spacer, treeID, sb, renderer, checkboxes, args);
			}

		} // for recursion
	} // buildTargets

	/**
	 * @param rootNode
	 * @param renderer
	 * @param sb
	 * @param args
	 */
//	private void renderNode(TreeNode node, Renderer renderer,
//			StringOutput sb, String[] args) {
//		
//		sb.append("\n<div class=\"b_selectiontree_item\">");
//		sb.append("<div class=\"b_selectiontree_icons\">");
//		//render the checkbox component
//		
//		// StaticTextElement or CheckboxElementComponent
//		renderer.render(sb, checkboxes.get(node.getIdent()), args);
//		
//		int childCount = node.getChildCount();
//		for(int i = 0; i < childCount; i++){
//			renderNode((TreeNode)node.getChildAt(i), renderer, sb, args);
//		}
//		sb.append("</div>&nbsp;</div>");
//		
//	}

	/**
	 * Renders the node icons if available
	 * @param sb
	 * @param node
	 */
	private void renderNodeIcon(StringOutput sb, TreeNode node) {
		// item icon css class and icon decorator (for each icon quadrant a div, eclipse style)
		String iconCssClass = node.getIconCssClass();
		if (iconCssClass != null) {
			sb.append("<div class=\"").append(iconCssClass).append("\" title=\"").append(StringEscapeUtils.escapeHtml(node.getAltText())).append("\">");
			
			String deco1 = node.getIconDecorator1CssClass();
			if (deco1 != null)
				sb.append("<span class=\"").append(deco1).append("\"></span>");
			
			String deco2 = node.getIconDecorator2CssClass();
			if (deco2 != null)
				sb.append("<span class=\"").append(deco2).append("\"></span>");
			
			String deco3 = node.getIconDecorator3CssClass();
			if (deco3 != null)
				sb.append("<span class=\"").append(deco3).append("\"></span>");
			
			String deco4 = node.getIconDecorator4CssClass();
			if (deco4 != null)
				sb.append("<span class=\"").append(deco4).append("\"></span>");

			sb.append("</div>");			
		}
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.ComponentRenderer#renderBodyOnLoadJSFunctionCall(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component, org.olat.core.gui.render.RenderingState)
	 */
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer,
			StringOutput sb, Component source, RenderingState rstate) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.ComponentRenderer#renderHeaderIncludes(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component, org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator, org.olat.core.gui.render.RenderingState)
	 */
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb,
			Component source, URLBuilder ubu, Translator translator,
			RenderingState rstate) {
		// TODO Auto-generated method stub

	}

}
