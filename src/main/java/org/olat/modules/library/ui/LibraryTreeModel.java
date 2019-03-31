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
package org.olat.modules.library.ui;

import java.util.Comparator;
import java.util.Locale;

import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.control.generic.folder.OlatRootFolderTreeModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSItemFilter;

/**
 * 
 * Description:<br>
 * This implementation add a node in the tree for notification of
 * new documents
 * 
 * <P>
 * Initial Date:  4 sept. 2009 <br>
 *
 * @author srosse
 */
public class LibraryTreeModel extends OlatRootFolderTreeModel {

	private static final long serialVersionUID = -7124740847074791402L;
	public static final String NEW_DOCS_USER_OBJ = "new.docs";
	private Translator translator;
	
	public LibraryTreeModel(VFSContainer root, Locale locale, boolean newDocNode) {
		super(root);
		if(newDocNode) {
			additionNodes(locale);
		}
	}

	public LibraryTreeModel(VFSContainer root, VFSItemFilter filter, Locale locale, boolean newDocNode) {
		super(root,filter);
		if(newDocNode) {
			additionNodes(locale);
		}
	}

	public LibraryTreeModel(VFSContainer root, VFSItemFilter filter, Comparator<VFSItem> comparator, Locale locale, boolean newDocNode) {
		super(root, filter, comparator);
		if(newDocNode) {
			additionNodes(locale);
		}
	}
	
	/**
	 * Set an css class on all nodes in the model
	 * @param iconCssClass
	 */
	protected void setIconCssClass(String iconCssClass) {
		setIconCssClass (getRootNode(), iconCssClass);
	}
	
	private void setIconCssClass(GenericTreeNode node, String iconCssClass) {
		node.setIconCssClass(iconCssClass);
		int numOfChildren = node.getChildCount();
		for(int i=0; i<numOfChildren; i++) {
			setIconCssClass((GenericTreeNode)node.getChildAt(i), iconCssClass);
		}
	}
	
	protected void additionNodes(Locale locale) {
		translator = Util.createPackageTranslator(this.getClass(), locale);
		
		GenericTreeNode rootNode = getRootNode();
		GenericTreeNode newDocNode = new GenericTreeNode();
		newDocNode.setTitle(translator.translate("library.notification.title"));
		newDocNode.setUserObject(NEW_DOCS_USER_OBJ);
		rootNode.addChild(newDocNode);
	}
}
