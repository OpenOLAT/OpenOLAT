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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
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

/**
 * enclosing_type Description: <br>
 * 
 * @author Felix Jost
 */
public class MenuTreeScreenreaderRenderer implements ComponentRenderer {

	/**
	 * Constructor for TableRenderer. Singleton and must be reentrant There must
	 * be an empty contructor for the Class.forName() call
	 */
	public MenuTreeScreenreaderRenderer() {
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
		String compPrefix = Renderer.getComponentPrefix(tree);
		
		INode selNode = tree.getSelectedNode();
		TreeNode root = tree.getTreeModel().getRootNode();
		if (root == null) return; // tree is completely empty

		List<INode> selPath = new ArrayList<INode>(5);
		INode cur = selNode;
		if (cur == null) cur = root; // if no selection, select the first node to
		// expand the children
		// add all elems from selected path to reversed list -> first elem is
		// selected nodeid of the root node
		
		while (cur != null) {
			selPath.add(0, cur);
			cur = cur.getParent();
		}
		
		// render overview information for screenreaders
		if (tree.isDirtyForUser() && selNode != null) {
			// this is the case when a new subtree expanded (parent is the node we just clicked).
			// print out the children so that screenreaders know what has changed.
			int cnt = selNode.getChildCount();
			// brasato:: fix it
			//Translator comptrans = tree.getCompTrans();
			//target.append(comptrans.translate("sr.intro", new String[] {String.valueOf(cnt)}));
			for (int i = 0; i < cnt; i++) {
				TreeNode tn = (TreeNode) selNode.getChildAt(i);
				StringOutput so = new StringOutput();
				so.append("<a href=\"");
				ubu.buildURI(so, new String[] { MenuTree.NODE_IDENT }, new String[] { tn.getIdent() });
				so.append("\" title=\"").append(StringEscapeUtils.escapeHtml(tn.getAltText())).append("\">").append(tn.getTitle()).append("</a>");
				
				//target.append(comptrans.translate("sr.treenode", new String[] {so.toString(), tn.getAltText()}));
			}
			//target.append(comptrans.translate("sr.endintro"));
		}
		
		renderLevel(target, 0, root, selPath, ubu, renderer.getGlobalSettings().getAjaxFlags(), compPrefix);
	}

	/**
	 * @param target
	 */
	private void renderLevel(StringOutput target, int level, TreeNode curRoot, List<INode> selPath, URLBuilder ubu, AJAXFlags flags, String componentPrefix) {

		INode curSel = null;
		if (level < selPath.size()) {
			curSel = selPath.get(level);
		}

		target.append("<ul><li>");
		String title = curRoot.getTitle();
		title = StringEscapeUtils.escapeHtml(title).toString();
		
		if (curSel == curRoot && level == selPath.size() -1) {
			target.append("X ");
		}
		
		target.append("<a onclick=\"return o2cl()\" href=\"");			
		
		boolean iframePostEnabled = flags.isIframePostEnabled();
		if (iframePostEnabled) {
			ubu.buildURI(target, new String[] { MenuTree.NODE_IDENT }, new String[] { curRoot.getIdent() }, AJAXFlags.MODE_TOBGIFRAME);
		} else {
			ubu.buildURI(target, new String[] { MenuTree.NODE_IDENT }, new String[] { curRoot.getIdent() });
		}		
		
		target.append("\" title=\"");
		target.append(curRoot.getAltText() == null ? title : StringEscapeUtils.escapeHtml(curRoot.getAltText()).toString());
		target.append("\"");
		if (iframePostEnabled) {
			ubu.appendTarget(target);
		}
		target.append(">");
		target.append(title).append("</a>");
		
		int chdCnt = curRoot.getChildCount();
		if (curSel == curRoot) {
			// render children
			for (int i = 0; i < chdCnt; i++) {
				TreeNode curChd = (TreeNode) curRoot.getChildAt(i);
				renderLevel(target, level + 1, curChd, selPath, ubu, flags, componentPrefix);
			}
		}
		
		//	 close item level
		target.append("</li></ul>");
		
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

}