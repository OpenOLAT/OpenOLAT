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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */
package org.olat.core.gui.control.generic.layout;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.extensions.ExtManager;
import org.olat.core.extensions.Extension;
import org.olat.core.extensions.action.ActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.nodes.INode;

/**
 * Description:<br>
 * This generic Controller gets menu-items configured for a site If any other
 * than configured (spring: olat_extensions.xml) items need to be in menu, use
 * addChildNodeToAppend/Prepend() before init(). init() needs to be called to
 * put content to panel.
 * <P>
 * Initial Date: 02.07.2008 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 * @author patrickb, www.uzh.ch, slightly changed to allow specialised forms of GenericActionExtension
 */
public abstract class GenericMainController extends MainLayoutBasicController {

	private MenuTree olatMenuTree;
	private Panel content;
	private LayoutMain3ColsController columnLayoutCtr;
	private Controller contentCtr;
	private Controller toolCtr;
	private List<GenericTreeNode> nodesToAppend;
	private List<GenericTreeNode> nodesToPrepend;
	private String className;

	public GenericMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		nodesToAppend = new ArrayList<GenericTreeNode>();
		nodesToPrepend = new ArrayList<GenericTreeNode>();
		className = this.getClass().getName();
	}

	/**
	 * use after optional addChildNodeToAppend() or addChildNodeToPrepend() calls
	 * to initialize MainController and set Panel
	 * 
	 * @param ureq
	 */
	public void init(UserRequest ureq) {
		olatMenuTree = new MenuTree("olatMenuTree");
		TreeModel tm = buildTreeModel();
		olatMenuTree.setTreeModel(tm);
		content = new Panel("content");
		INode firstNode = tm.getRootNode();
		olatMenuTree.setSelectedNodeId(firstNode.getIdent());
		olatMenuTree.addListener(this);

		Object uobject = tm.getRootNode().getUserObject();
		contentCtr = getContentCtr(uobject, ureq);
		listenTo(contentCtr); // auto dispose later
		Component resComp = contentCtr.getInitialComponent();
		content.setContent(resComp);
		Component toolContent = toolCtr != null ? toolCtr.getInitialComponent() : null; 
		
		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), olatMenuTree, toolContent, content, className);
		listenTo(columnLayoutCtr); // auto dispose later
		putInitialPanel(columnLayoutCtr.getInitialComponent());
	}
	
	/**
	 * set the column2 or the rightmost column (in left-to-right) for the tool controller. Previous toolcontroller is disposed. 
	 * @param toolController
	 */
	protected void setToolController(Controller toolController){
		if(toolCtr != null){
			// there is already a tool controller, dispose it
			toolCtr.dispose();
		}
		toolCtr = toolController;
		if(columnLayoutCtr != null){
			columnLayoutCtr.setCol2(toolCtr.getInitialComponent());
		}//else method called from within constructor before columnLayoutCtr is initialized, see init(..) which sets the toolcontroller content
	}
	
	/**
	 * build a node before with sth. like: GenericTreeNode gtnA = new
	 * GenericTreeNode(); gtnA.setTitle("appended"); //or with translate
	 * gtnA.setAltText("alternative text"); gtnA.setUserObject("identifier or
	 * object to use"); then add it with this method
	 * 
	 * @param nodeToAppend node to add, besides the configured ones
	 */
	public void addChildNodeToAppend(GenericTreeNode nodeToAppend) {
		checkNodeToAdd(nodeToAppend);
		nodesToAppend.add(nodeToAppend);
	}

	/**
	 * build a node before with sth. like: GenericTreeNode gtnA = new
	 * GenericTreeNode(); gtnA.setTitle("appended"); //or with translate
	 * gtnA.setAltText("alternative text"); gtnA.setUserObject("identifier or
	 * object to use"); then add it with this method
	 * 
	 * @param nodeToPrepend node to add, besides the configured ones
	 */
	public void addChildNodeToPrepend(GenericTreeNode nodeToPrepend) {
		checkNodeToAdd(nodeToPrepend);
		nodesToPrepend.add(nodeToPrepend);
	}

	/**
	 * checks if a userObject has been set, is needed to find the required
	 * controller
	 * 
	 * @param node
	 */
	private void checkNodeToAdd(GenericTreeNode node) {
		if (node.getUserObject() == null) { throw new AssertException(
				"GenericTreeNode to append/prepend needs to have a UserObject set! Please use setUserObject()."); }
	}

	private TreeModel buildTreeModel() {
		GenericTreeNode gtnChild, rootTreeNode;
		Translator translator = getTranslator();

		GenericTreeModel gtm = new GenericTreeModel();
		rootTreeNode = new GenericTreeNode();
		//there should be i18n key for main.menu.title for the rootNode in package of implementing type
		rootTreeNode.setTitle(translator.translate("main.menu.title"));
		rootTreeNode.setAltText(translator.translate("main.menu.title.alt"));
		gtm.setRootNode(rootTreeNode);

		// Prepend
		boolean rootNodeSet = false;
		if (nodesToPrepend.size() != 0) {
			for (GenericTreeNode node : nodesToPrepend) {
				rootTreeNode.addChild(node);
				if (!rootNodeSet) {
					rootTreeNode.setDelegate(node);
					rootTreeNode.setUserObject(node.getUserObject());
					rootNodeSet = true;
				}
			}
		}

		// add extension menues
		ExtManager extm = ExtManager.getInstance();
		int cnt = extm.getExtensionCnt();
		int j = 0;
		for (int i = 0; i < cnt; i++) {
			Extension anExt = extm.getExtension(i);
			// check for sites
			ActionExtension ae = (ActionExtension) anExt
					.getExtensionFor(className);
			if (ae != null && anExt.isEnabled()) {
				gtnChild = new GenericTreeNode();
				String menuText = ae.getActionText(getLocale());
				gtnChild.setTitle(menuText);
				gtnChild.setAltText(ae.getDescription(getLocale()));
				gtnChild.setUserObject(ae);
				// load first node on root node
				if (j == 0 && !rootNodeSet) {
					rootTreeNode.setDelegate(gtnChild);
					rootTreeNode.setUserObject(ae);
				} else {
					rootTreeNode.addChild(gtnChild);
				}
				j++;
			}
		}

		// Append
		if (nodesToAppend.size() != 0) {
			for (GenericTreeNode node : nodesToAppend) {
				rootTreeNode.addChild(node);
			}
		}

		return gtm;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == olatMenuTree) {
			if (event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) { 
				// process menu commands
				TreeNode selTreeNode = olatMenuTree.getSelectedNode();
				// cleanup old content controller (never null)
				removeAsListenerAndDispose(contentCtr);
				
				// create new content controller
				// Following cases:
				// 1a) Simple Action Extension using only ureq and windowControl -> handled by default implementation of createController
				// 1b) Specialised Action Extension which needs some more internals -> handled by the class extending GenericMainController, by overwriting createController
				// 2)  uobject is something special which needs evaluation by class extending GenericMainController
				Object uobject = selTreeNode.getUserObject();
				contentCtr = getContentCtr(uobject, ureq);
				listenTo(contentCtr);
				Component resComp = contentCtr.getInitialComponent();
				content.setContent(resComp);
			} else { // the action was not allowed anymore
				content.setContent(null); // display an empty field (empty panel)
			}
		} else {
			logWarn("Unhandled olatMenuTree event: " + event.getCommand(), null);
		}
	}

	/**
	 * needs to be implemented to return corresponding controller for menu items
	 * which were not generated by a GenericActionExtension
	 * 
	 * @param uobject
	 * @param ureq
	 * @return corresponding controller to be opened by click to menu-item / null
	 *         if none has been defined
	 */
	protected abstract Controller handleOwnMenuTreeEvent(Object uobject, UserRequest ureq);

	/**
	 * creates Controller for clicked Node, default implementation.
	 * 
	 * @param ae
	 * @param ureq
	 * @return corresponding controller
	 */
	protected Controller createController(ActionExtension ae, UserRequest ureq) {
		//default implementation for simple case where action extension.
		return ae.createController(ureq, getWindowControl(), null);
	}

	
	private Controller getContentCtr(Object uobject, UserRequest ureq){
		Controller contentCtr1Tmp = null;
		if (uobject instanceof ActionExtension) {
			ActionExtension ae = (ActionExtension) uobject;
			contentCtr1Tmp = createController(ae, ureq);
		} else {
			contentCtr1Tmp = handleOwnMenuTreeEvent(uobject, ureq);
		}
		if (contentCtr1Tmp == null) { throw new AssertException(
				"Node must either be an ActionExtension or implementation must handle this MenuTreeEvent: " + uobject.toString()); }
		return contentCtr1Tmp;
	}
	
	@Override
	protected void doDispose() {
	// nothing to do
	}

}
