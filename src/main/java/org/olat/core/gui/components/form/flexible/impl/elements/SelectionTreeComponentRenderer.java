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
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.Map;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.tree.INodeFilter;

/**
 * @author patrickb
 *
 */
class SelectionTreeComponentRenderer extends DefaultComponentRenderer {
	
	@Override
	public void render(Renderer renderer, StringOutput sb, Component source,
			URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {
		
		SelectionTreeComponent stc = (SelectionTreeComponent)source;
		Map<String,CheckboxElement> checkboxes = stc.getSubComponents();
		
		TreeModel tm = stc.getTreeModel();
		TreeNode rootNode = tm.getRootNode();
		if(rootNode.getChildCount() == 1) {
			rootNode = (TreeNode)rootNode.getChildAt(0);
		}
		INodeFilter selectableFilter = stc.getSelectableFilter();

		sb.append("<div class='o_selection_tree'><ul class='o_selection_tree_l0'>");
		renderNode(rootNode, rootNode, 0, stc.hashCode(), sb, renderer, stc, checkboxes, selectableFilter, args);		
		sb.append("</ul></div>");
	}
	
	private void renderNode(TreeNode currentNode, TreeNode root, int level, int treeID, StringOutput sb, Renderer renderer,
			SelectionTreeComponent stc, Map<String,CheckboxElement> checkboxes,
			INodeFilter selectableFilter, String[] args) {

		if(selectableFilter == null || selectableFilter.isVisible(currentNode)) {
			sb.append("<li><div>");
			// append radio or checkbox if selectable
			if (currentNode.isAccessible()) {
				renderCheckbox(sb, currentNode, level, checkboxes.get(currentNode.getIdent()), stc);
			} else {
				// node title (using css if available)
				String cssClass = currentNode.getCssClass();
				sb.append("<span class='o_tree_l").append(level).append(" ")
				  .append(" o_disabled", currentNode.isAccessible()).append("'>");
				renderNodeIcon(sb, currentNode);
				if(cssClass != null) {
					sb.append("<i class='").append(cssClass).append("'> </i> ");
				}
				sb.appendHtmlEscaped(currentNode.getTitle());
				renderNodeDecorator(sb, currentNode);
				sb.append("</span>");
			}
			sb.append("</div>");

			// do the same for all children
			int childcnt = currentNode.getChildCount();
			if(childcnt > 0) {
				int childLevel = level + 1;
				sb.append("<ul class='o_tree_l").append(childLevel).append("'>");
				for (int i = 0; i < childcnt; i++) {
					renderNode((TreeNode)currentNode.getChildAt(i), root, childLevel, treeID, sb, renderer, stc, checkboxes, selectableFilter, args);
				}
				sb.append("</ul>");
			}
			sb.append("</li>");
		}
	}
	
	private void renderCheckbox(StringOutput sb, TreeNode node, int level, CheckboxElement check, SelectionTreeComponent stc) {
		MultiSelectionTreeImpl stF = stc.getSelectionElement();
		
		String subStrName = "name=\"" + check.getGroupingName() + "\"";
		
		String key = check.getKey();
		String value = check.getValue();
		if(stF.isEscapeHtml()){
			key = StringHelper.escapeHtml(key);
			value = StringHelper.escapeHtml(value);
		}
		
		boolean selected = check.isSelected();
		//read write view
		sb.append("<div class='checkbox o_tree_l").append(level).append("'>")
		  .append(" <label for=\"").append(stc.getFormDispatchId()).append("\">");
		
		String cssClass = check.getCssClass(); //optional CSS class
		String iconLeftCSS = check.getIconLeftCSS();
		
		if(!check.isTextOnly()) {
			sb.append("<input type='checkbox' id='").append(stc.getFormDispatchId()).append("' ")
			  .append(subStrName)
			  .append(" value='").append(key).append("'");
			if (selected) {
				sb.append(" checked='checked' ");
			}
			if(check.isEnabled()){
				//use the selection form dispatch id and not the one of the element!
				sb.append(FormJSHelper.getRawJSFor(stF.getRootForm(), check.getSelectionElementFormDispatchId(), check.getAction()));
			} else {
				sb.append(" disabled='disabled' ");
			}
			sb.append(" />");
		}
		renderNodeIcon(sb, node);
		if (StringHelper.containsNonWhitespace(iconLeftCSS) || StringHelper.containsNonWhitespace(cssClass)) {
			sb.append(" <i class='").append(iconLeftCSS, iconLeftCSS != null)
			  .append(" ").append(cssClass, cssClass != null).append("'> </i> ");
		}
		if (StringHelper.containsNonWhitespace(value)) {
			sb.append(value);		
		}
		renderNodeDecorator(sb, node);
		sb.append("</label></div>");
		
		if(stc.isEnabled()){
			//add set dirty form only if enabled
			FormJSHelper.appendFlexiFormDirtyForCheckbox(sb, stF.getRootForm(), stc.getFormDispatchId());
		}
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
			sb.append("<i class=\"").append(iconCssClass).append("\"> </i> ");
		}
	}
		
	private void renderNodeDecorator(StringOutput sb, TreeNode node) {
		String deco1 = node.getIconDecorator1CssClass();
		if (deco1 != null) {
			sb.append("<span class='badge ").append(deco1).append(" pull-right'><i class=\"").append(deco1).append("\"> </i></span>");
		}
		String deco2 = node.getIconDecorator2CssClass();
		if (deco2 != null) {
			sb.append("<span class='badge ").append(deco2).append(" pull-right'><i class=\"").append(deco2).append("\"> </i></span>");
		}
		String deco3 = node.getIconDecorator3CssClass();
		if (deco3 != null) {
			sb.append("<span class='badge ").append(deco3).append(" pull-right'><i class=\"").append(deco3).append("\"> </i></span>");
		}
		String deco4 = node.getIconDecorator4CssClass();
		if (deco4 != null) {
			sb.append("<span class='badge ").append(deco4).append(" pull-right'><i class=\"").append(deco4).append("\"> </i></span>");
		}
	}
}