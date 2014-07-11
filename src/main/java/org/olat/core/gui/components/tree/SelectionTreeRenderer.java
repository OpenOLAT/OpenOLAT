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

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.GUIInterna;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.tree.TreeHelper;

/**
 * enclosing_type Description: <br>
 * 
 * @author Felix Jost
 */
public class SelectionTreeRenderer extends DefaultComponentRenderer {
	/**
	 * <code>ATTR_SELECTION</code>
	 */
	public static final String ATTR_SELECTION = "sel";

	private static final String SCRIPT_SINGLE_PRE = "<script type=\"text/javascript\">\n function seltree_check() {\n"
			+ "var i;\n" + "var numselected = 0;\n" + "for (i=0; document.seltree.elements[i]; i++) {\n"
			+ "	if (document.seltree.elements[i].type == \"radio\" &&\n" + "		document.seltree.elements[i].name == \"" + ATTR_SELECTION
			+ "\" &&\n" + "		document.seltree.elements[i].checked) {\n" + "		numselected++;\n" + "	}\n" + "}\n"
			+ "if (numselected < 1) {\n	alert(\"";

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
	 * @param renderer
	 * @param target
	 * @param source
	 * @param ubu
	 * @param translator
	 * @param renderResult
	 * @param args
	 */
	@Override
	public void render(Renderer renderer, StringOutput target, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {

		SelectionTree tree = (SelectionTree) source;
		Translator internalTranslator = tree.getTranslator();
		TreeNode root = tree.getTreeModel().getRootNode();

		target.append(SCRIPT_SINGLE_PRE);
		target.append(translator.translate("alert"));
		target.append(SCRIPT_POST);
		target.append("<div class=\"");
		if(StringHelper.containsNonWhitespace(tree.getElementCssClass())) {
			target.append(" ").append(tree.getElementCssClass());
		}
		target.append("\"><form method=\"post\" name=\"seltree\" action=\"");

		boolean iframePostEnabled = renderer.getGlobalSettings().getAjaxFlags().isIframePostEnabled();
		ubu.buildURI(target, null, null, iframePostEnabled? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		target.append("\"");
		if (iframePostEnabled) {
			ubu.appendTarget(target);
		}
		target.append(" id=\"").append(Form.JSFORMID).append(tree.hashCode()).append("\"")
		  .append(">")
		  .append("<div class='o_selection_tree'><ul class='o_selection_tree_l0'>");
		boolean atLeastOneIsAccessible = atLeastOneIsAccessible(root);
		if (root.getChildCount() != 0) {
			renderNode(root, root, tree.hashCode(), 1, target, tree);
		} else {
			target.append(internalTranslator.translate("selectiontree.noentries"));
		}
		target.append("</ul></div><div class='o_button_group'>");
		if (atLeastOneIsAccessible && tree.getFormButtonKey() != null) {
			target.append("<button type=\"submit\" class=\"btn btn-primary o_sel_submit_selection\" name=\"" + Form.SUBMIT_IDENTIFICATION + "\" value=\"");
			target.append(StringEscapeUtils.escapeHtml(translator.translate(tree.getFormButtonKey())));
			if (!tree.isAllowEmptySelection()) {
				target.append("\" onclick=\"return seltree_check();\" onkeypress=\"return seltree_check();\">");
			} else {
				target.append("\">");
			}
			target.append(translator.translate(tree.getFormButtonKey())).append("</button>");
		}
		if(tree.isShowCancelButton()){
			target.append(" <button type=\"submit\" class=\"btn btn-default o_sel_cancel_selection\" name=\"" + Form.CANCEL_IDENTIFICATION + "\" value=\"");
			target.append(StringEscapeUtils.escapeHtml(translator.translate("cancel"))).append("\">");
			target.append(translator.translate("cancel")).append("</button>");
		}
		target.append("</div></form></div>");
	}

	private boolean atLeastOneIsAccessible(TreeNode node) {
		if (node.isAccessible()) return true;
		for (int i = 0; i < node.getChildCount(); i++) {
			TreeNode child = (TreeNode)node.getChildAt(i);
			if (atLeastOneIsAccessible(child)) return true;
		}
		return false;
	}
	
	private void renderNode(TreeNode root, TreeNode child, int treeID, 
			int level, StringOutput sb, SelectionTree tree) {

		sb.append("<li><div>");

		// node title (using css if available)
		String cssClass = child.getCssClass();
		
		// append radio or checkbox if selectable
		if (child.isAccessible()) {
			
			sb.append("<div class='radio o_tree_l").append(level).append("'>")
			  .append("<label class='").append(cssClass, cssClass != null).append("'>");
			
			sb.append("<input type='radio' name=\"" + ATTR_SELECTION + "\" value=\"");
			if (GUIInterna.isLoadPerformanceMode()) {
				sb.append(TreeHelper.buildTreePath(child));
			} else {
				sb.append(child.getIdent());
			}
			sb.append("\" onchange=\"return setFormDirty('").append(Form.JSFORMID).append(treeID).append("')\" ")
			  .append(" onclick=\"return setFormDirty('").append(Form.JSFORMID).append(treeID).append("')\" />");
			
			renderNodeIcon(sb, child);				
			if(tree.isEscapeHtml()) {						
			  sb.append(StringEscapeUtils.escapeHtml(child.getTitle()));
			} else {						
			  sb.append(child.getTitle());
			}
			if(!StringHelper.containsNonWhitespace(child.getTitle())) {
				sb.append("&nbsp;&nbsp;");
			}
			renderNodeDecorators(sb, child);
			sb.append("</label></div>");
		} else {
			sb.append("<span class='o_tree_l").append(level).append("'>");
			renderNodeIcon(sb, child);				
			if(tree.isEscapeHtml()) {						
			  sb.append(StringEscapeUtils.escapeHtml(child.getTitle()));
			} else {						
			  sb.append(child.getTitle());
			}					
			renderNodeDecorators(sb, child);
			sb.append("</span>");
		}
		sb.append("</div>");

		int numOfChildren = child.getChildCount();
		if(numOfChildren > 0) {
			int childLevel = level + 1;
			sb.append("<ul class='o_tree_l").append(childLevel).append("'>");
			for (int i = 0; i < numOfChildren; i++) {
				renderNode(root, (TreeNode)child.getChildAt(i), treeID, childLevel, sb, tree);
			}
			sb.append("</ul>");
		}
		
		sb.append("</li>");
	}
	
	/**
	 * Renders the node icons if available
	 * @param sb
	 * @param node
	 */
	private void renderNodeIcon(StringOutput sb, TreeNode node) {
		// item icon css class and icon decorator (for each icon quadrant a div, eclipse style)
		String iconCssClass = node.getIconCssClass();
		if (iconCssClass != null) {
			sb.append("<div title=\"").append(StringEscapeUtils.escapeHtml(node.getAltText()))
			  .append("\"><i class='o_icon ").append(iconCssClass).append("'> </i></div>");
		}
	}
	private void renderNodeDecorators(StringOutput sb, TreeNode node) {	
		String deco1 = node.getIconDecorator1CssClass();
		if (deco1 != null) {
			sb.append("<span class='badge pull-right ").append(deco1).append("'></span>");
		}
		String deco2 = node.getIconDecorator2CssClass();
		if (deco2 != null) {
			sb.append("<span class='badge pull-right ").append(deco2).append("'></span>");
		}
		String deco3 = node.getIconDecorator3CssClass();
		if (deco3 != null) {
			sb.append("<span class='badge pull-right ").append(deco3).append("'></span>");
		}
		String deco4 = node.getIconDecorator4CssClass();
		if (deco4 != null) {
			sb.append("<span class='badge pull-right").append(deco4).append("'></span>");
		}
	}

	/**
	 * @param parameterName
	 * @return boolean
	 */
	public static boolean isMultiSelectParameter(String parameterName) {
		return parameterName.startsWith(ATTR_SELECTION);
	}
}