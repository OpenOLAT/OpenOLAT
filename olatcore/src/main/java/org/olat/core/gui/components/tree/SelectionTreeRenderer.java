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

import org.olat.core.gui.GUIInterna;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.components.form.FormRenderer;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.tree.TreeHelper;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * enclosing_type Description: <br>
 * 
 * @author Felix Jost
 */
public class SelectionTreeRenderer implements ComponentRenderer {
	/**
	 * <code>PACKAGE</code>
	 */
	public static final String PACKAGE = org.olat.core.util.Util.getPackageName(SelectionTreeRenderer.class);

	private static String imgDots = "<div class=\"b_selectiontree_line\"></div>";
	private static String imgDots_spacer = "<div class=\"b_selectiontree_space\"></div>";
	private static String imgDots_nt = "<div class=\"b_selectiontree_junction\"></div>";
	private static String imgDots_nl = "<div class=\"b_selectiontree_end\"></div>";
	/**
	 * <code>ATTR_SELECTION</code>
	 */
	public static final String ATTR_SELECTION = "sel";

	private static final String SCRIPT_SINGLE_PRE = "<script type=\"text/javascript\">\n function seltree_check() {\n"
			+ "var i;\n" + "var numselected = 0;\n" + "for (i=0; document.seltree.elements[i]; i++) {\n"
			+ "	if (document.seltree.elements[i].type == \"radio\" &&\n" + "		document.seltree.elements[i].name == \"" + ATTR_SELECTION
			+ "\" &&\n" + "		document.seltree.elements[i].checked) {\n" + "		numselected++;\n" + "	}\n" + "}\n"
			+ "if (numselected < 1) {\n	alert(\"";

	private static final String SCRIPT_MULTI_PRE = "<script type=\"text/javascript\">\n function seltree_check() {\n"
			+ "var i;\n" + "var numselected = 0;\n" + "for (i=0; document.seltree.elements[i]; i++) {\n"
			+ "	if (document.seltree.elements[i].type == \"checkbox\" &&\n" + "		document.seltree.elements[i].checked) {\n"
			+ "		numselected++;\n" + "	}\n" + "}\n" + "if (numselected < 1) {\n	alert(\"";

	private static final String SCRIPT_POST = "\");\n return false;\n" +
		"}\n" + "return true;\n}\n" +
		"function checkall(checked) {\n" +
		"len = document.seltree.elements.length;\n" +
		"var i;\n" +
		"for (i=0; i < len; i++) {\n" +
		"if (document.seltree.elements[i].name.indexOf('" + ATTR_SELECTION + "') != -1) {\n" +
		"document.seltree.elements[i].checked=checked;\n" +
		"}\n" +
		"}\n" +
		"}\n" +
		"</script>";
	

	/**
	 * Constructor for TableRenderer. Singleton and must be reentrant There must
	 * be an empty contructor for the Class.forName() call
	 */
	public SelectionTreeRenderer() {
		super();
	}

	/**
	 * @param renderer
	 * @param target
	 * @param source
	 * @param ubu
	 * @param translator
	 * @param renderResult
	 * @param args
	 */
	public void render(Renderer renderer, StringOutput target, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {

		SelectionTree tree = (SelectionTree) source;
		Translator internalTranslator = tree.getTranslator();
		TreeNode root = tree.getTreeModel().getRootNode();

		target.append(tree.isMultiselect() ? SCRIPT_MULTI_PRE : SCRIPT_SINGLE_PRE);
		target.append(translator.translate("alert"));
		target.append(SCRIPT_POST);
		target.append("<div class=\"b_selectiontree\"><form method=\"post\" name=\"seltree\" action=\"");
		boolean iframePostEnabled = renderer.getGlobalSettings().getAjaxFlags().isIframePostEnabled();
		ubu.buildURI(target, null, null, iframePostEnabled? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		target.append("\"");
		if (iframePostEnabled) {
			ubu.appendTarget(target);
		}
		target.append(" id=\"").append(FormRenderer.JSFORMID).append(tree.hashCode()).append("\"");
		target.append(">");
		// append root node
		renderRootNode(root, target);
		boolean atLeastOneIsAccessible = atLeastOneIsAccessible(root);
		if (root.getChildCount() != 0) {
			renderChildNodes(root, "", tree.hashCode(), tree.isMultiselect(), tree.getGreyOutNonSelectableEntries(), tree.isShowAltTextAsHoverOnTitle(), target, tree);
			if (tree.isMultiselect() && atLeastOneIsAccessible) {
				target.append("<div class=\"b_togglecheck\"><a href=\"javascript:checkall(true);setFormDirty('").append(FormRenderer.JSFORMID).append(tree.hashCode()).append("');\">");
				target.append("<input type=\"checkbox\" checked=\"checked\" disabled=\"disabled\" />");
				target.append(translator.translate("checkall"));
				target.append("</a>&nbsp;<a href=\"javascript:checkall(false);setFormDirty('").append(FormRenderer.JSFORMID).append(tree.hashCode()).append("\');\">");
				target.append("<input type=\"checkbox\" disabled=\"disabled\" />");
				target.append(translator.translate("uncheckall"));
				target.append("</a></div>");
			}
		}
		else target.append(internalTranslator.translate("selectiontree.noentries"));
		target.append("<br /><br />");
		if (atLeastOneIsAccessible) {
			target.append("<button type=\"submit\" class=\"b_button\" name=\"" + Form.SUBMIT_IDENTIFICATION + "\" value=\"");
			target.append(StringEscapeUtils.escapeHtml(translator.translate(tree.getFormButtonKey())));
			if (!tree.isAllowEmptySelection()) {
				target.append("\" onclick=\"return seltree_check();\" onkeypress=\"return seltree_check();\">");
			} else {
				target.append("\">");
			}
			target.append("<span>").append(translator.translate(tree.getFormButtonKey())).append("</span></button>");
			
		}
		if(tree.isShowCancelButton()){
			target.append("<button type=\"submit\" class=\"b_button\" name=\"" + Form.CANCEL_IDENTIFICATION + "\" value=\"");
			target.append(StringEscapeUtils.escapeHtml(translator.translate("cancel"))).append("\">");
			target.append("<span>").append(translator.translate("cancel")).append("</span></button>");
		}
		target.append("</form></div>");
	}

	private boolean atLeastOneIsAccessible(TreeNode node) {
		if (node.isAccessible()) return true;
		for (int i = 0; i < node.getChildCount(); i++) {
			TreeNode child = (TreeNode)node.getChildAt(i);
			if (atLeastOneIsAccessible(child)) return true;
		}
		return false;
	}

	private void renderRootNode(TreeNode root, StringOutput target) {
		target.append("\n<div class=\"b_selectiontree_item\">");
		renderNodeIcon(target, root);
		target.append("<div class=\"b_selectiontree_content\">");
		// text using css if available
		String cssClass = root.getCssClass();
		if (cssClass != null) target.append("<span class=\"").append(cssClass).append("\">");
		target.append(root.getTitle());
		if (cssClass != null) target.append("</span>");
		target.append("</div></div>");
	}

	private void renderChildNodes(TreeNode root, String indent, int treeID, boolean multiselect, boolean greyOutNonSelectable, 
			boolean showAltTextAsHoverOnTitle, StringOutput sb, SelectionTree tree) {
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
			sb.append("\n<div class=\"b_selectiontree_content\">");
			
			// append radio or checkbox if selectable
			if (child.isAccessible()) {
				if (multiselect) { // render chekcboxes
					sb.append("<input type=\"checkbox\" class=\"b_checkbox\" name=\"" + ATTR_SELECTION);
					if (GUIInterna.isLoadPerformanceMode()) {
						String tPath = TreeHelper.buildTreePath(child);
						sb.append(tPath);
					} else {
						sb.append(child.getIdent());
					}
					sb.append("\" ");
					if (child.isSelected())
						sb.append("checked ");
					sb.append("value=\"");
				} else { // render radioboxes
					sb.append("<input type=\"radio\" class=\"b_radio\" name=\"" + ATTR_SELECTION + "\" value=\"");
				}
				if (GUIInterna.isLoadPerformanceMode()) {
					sb.append(TreeHelper.buildTreePath(child));
				} else {
					sb.append(child.getIdent());
				}
				sb.append("\" onchange=\"return setFormDirty('").append(FormRenderer.JSFORMID).append(treeID).append("')\" ");
				sb.append(" onclick=\"return setFormDirty('").append(FormRenderer.JSFORMID).append(treeID).append("')\" />");
			}
			// node title (using css if available)
			String cssClass = child.getCssClass();
			if (cssClass != null) sb.append("<span class=\"").append(cssClass).append("\">");
			if (!child.isAccessible() && greyOutNonSelectable) {
				sb.append("<span class=\"b_disabled\">");
				if(tree.isEscapeHtml()) {					
				  sb.append(StringEscapeUtils.escapeHtml(child.getTitle()));
				} else {					
				  sb.append(child.getTitle());
				}
				sb.append("</span>");
			} else {
				if (child.getAltText() != null && showAltTextAsHoverOnTitle) {
					sb.append("<span ");
					sb.append(" onmouseover=\"o_showEventDetails(' ', '");
					sb.append(child.getAltText());
					sb.append("');\" onmouseout=\"return nd();\" onclick=\"return nd();\">");
					if(tree.isEscapeHtml()) {					
					  sb.append(StringEscapeUtils.escapeHtml(child.getTitle()));
					} else {				
					  sb.append(child.getTitle());
					}
					sb.append("</span>");
				} else {					
					if(tree.isEscapeHtml()) {						
					  sb.append(StringEscapeUtils.escapeHtml(child.getTitle()));
					} else {						
					  sb.append(child.getTitle());
					}					
				}
			}
			if (cssClass != null) sb.append("</span>");

			// END of choice div
			sb.append("</div></div>"); 

			// do the same for all children
			if (i < childcnt - 1) {
				renderChildNodes(child, newIndent, treeID, multiselect, greyOutNonSelectable, showAltTextAsHoverOnTitle, sb, tree);
			} else {
				renderChildNodes(child, indent + imgDots_spacer, treeID, multiselect, greyOutNonSelectable, showAltTextAsHoverOnTitle, sb, tree);
			}

		} // for recursion
	} // buildTargets

	
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

	/**
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#renderHeaderIncludes(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator)
	 */
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
	//
	}

	/**
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#renderBodyOnLoadJSFunctionCall(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component)
	 */
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
	//
	}

	/**
	 * @param parameterName
	 * @return boolean
	 */
	public static boolean isMultiSelectParameter(String parameterName) {
		return parameterName.startsWith(ATTR_SELECTION);
	}
	
}