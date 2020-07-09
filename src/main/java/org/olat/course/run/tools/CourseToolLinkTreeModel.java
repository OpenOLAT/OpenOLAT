/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.run.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.run.CourseRuntimeController;

/**
 * 
 * Initial date: 7 Jul 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseToolLinkTreeModel extends CustomLinkTreeModel {

	private static final long serialVersionUID = -1348173404039562757L;
	
	private static final String ROOT = "root";

	private final Translator translator;
	private TreeNode rootNode;

	public CourseToolLinkTreeModel(Locale locale) {
		super("toollinktreenode");
		translator = Util.createPackageTranslator(CourseRuntimeController.class, locale);
		buildTree();
	}

	private void buildTree() {
		rootNode = createTreeNode(ROOT, translator.translate("tool.link.root"));
		List<TreeNode> toolNodes = new ArrayList<>(CourseTool.values().length);
		for (CourseTool courseTool : CourseTool.values()) {
			//TODO uh Leistungsnachweis verlinken
			TreeNode node = createTreeNode(courseTool);
			toolNodes.add(node);
		}
		toolNodes.sort((n1, n2) -> n1.getTitle().compareToIgnoreCase(n2.getTitle()));
		for (TreeNode node: toolNodes) {
			rootNode.addChild(node);
		}
	}
	
	private GenericTreeNode createTreeNode(CourseTool tool) {
		GenericTreeNode node = createTreeNode(tool.name(), translator.translate(tool.getI18nKey()));
		node.setIconCssClass(tool.getIconCss());
		return node;
	}
	
	private GenericTreeNode createTreeNode(String ident, String title) {
		GenericTreeNode treeNode = new GenericTreeNode(ident);
		treeNode.setTitle(title);
		return treeNode;
	}

	@Override
	public TreeNode getRootNode() {
		return rootNode;
	}

	@Override
	public TreeNode getNodeById(String nodeId) {
		return findNode(nodeId, rootNode);
	}

	private TreeNode findNode(String nodeId, TreeNode node) {
		if (node.getIdent().equals(nodeId)) return node;
		int childcnt = node.getChildCount();
		for (int i = 0; i < childcnt; i++) {
			TreeNode child = (TreeNode) node.getChildAt(i);
			TreeNode result = findNode(nodeId, child);
			if (result != null) return result;
		}
		return null;
	}

	@Override
	public String getInternalLinkUrlFor(String nodeId) {
		return ROOT.equals(nodeId)? "": "javascript:parent.gototool('" + nodeId + "')";
	}

}
