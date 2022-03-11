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
package org.olat.core.gui.control.generic.layout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.extensions.ExtManager;
import org.olat.core.extensions.Extension;
import org.olat.core.extensions.action.ActionExtension;
import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.util.logging.activity.LoggingResourceable;

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
 * @author patrickb, www.uzh.ch, slightly changed to allow specialised forms of
 *         GenericActionExtension
 */
public abstract class GenericMainController extends MainLayoutBasicController {

	private static final String GMCMT = "GMCMenuTree";

	private MenuTree olatMenuTree;
	private Panel content;
	private BreadcrumbPanel stackVC;
	private LayoutMain3ColsController columnLayoutCtr;
	private Controller contentCtr;
	private final List<GenericTreeNode> nodesToAppend;
	private final List<GenericTreeNode> nodesToPrepend;
	private final String className;
	

	public GenericMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		nodesToAppend = new ArrayList<>();
		nodesToPrepend = new ArrayList<>();
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
		TreeModel tm = buildTreeModel(ureq);
		olatMenuTree.setTreeModel(tm);
		content = new Panel("content");
		TreeNode firstNode = tm.getRootNode();
		TreeNode nodeToSelect = getLastDelegate(firstNode);

		olatMenuTree.setSelectedNodeId(nodeToSelect.getIdent());
		olatMenuTree.addListener(this);
		
		// default is to not display the root element and to let user open/close sub elements
		olatMenuTree.setRootVisible(false);
		olatMenuTree.setExpandSelectedNode(false);


		Object uobject = nodeToSelect.getUserObject();
		contentCtr = getContentCtr(uobject, ureq);
		listenTo(contentCtr); // auto dispose later
		Component resComp = contentCtr.getInitialComponent();
		content.setContent(resComp);

		columnLayoutCtr = new LayoutMain3ColsController(ureq, getWindowControl(), olatMenuTree, content, className);
		listenTo(columnLayoutCtr); // auto dispose later
		
		//create the stack
		stackVC = new TooledStackedPanel("genericStack", getTranslator(), this);
		((TooledStackedPanel)stackVC).setToolbarAutoEnabled(true);
		stackVC.pushController("content", columnLayoutCtr);

		putInitialPanel(stackVC);
	}

	/**
	 * get the last delegate of a TreeNode.
	 * 
	 * @param node
	 * @return the first treeNode in the hierarchy -under the given node- that
	 *         does not have a delegate
	 */
	private TreeNode getLastDelegate(TreeNode node) {
		if (node.getDelegate() == null) return node;
		return getLastDelegate(node.getDelegate());
	}
	
	protected void addCssClassToMain(String cssClass) {
		columnLayoutCtr.addCssClassToMain(cssClass);
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

	private TreeModel buildTreeModel(UserRequest ureq) {

		GenericTreeNode rootTreeNode = new GenericTreeNode();
		rootTreeNode.setTitle(getTranslator().translate("main.menu.title"));
		rootTreeNode.setAltText(getTranslator().translate("main.menu.title.alt"));
		GenericTreeModel gtm = new GenericTreeModel();
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
		int j = 0;
		GenericTreeNode gtnChild;
		Map<GenericTreeNode, String> subMenuNodes = new LinkedHashMap<>();
		for (Extension anExt : extm.getExtensions()) {
			// check for sites
			ActionExtension ae = (ActionExtension) anExt.getExtensionFor(className, ureq);
			if (ae != null && ae instanceof GenericActionExtension) {
				if(anExt.isEnabled()){
					GenericActionExtension gAe = (GenericActionExtension) ae;
					gtnChild = gAe.createMenuNode(ureq);
					
					if(StringHelper.containsNonWhitespace(gAe.getNavigationKey())) {
						gtnChild.setCssClass("o_sel_" + gAe.getNavigationKey());
					}

					if (gAe.getNodeIdentifierIfParent() != null) {
						// it's a parent-node, set identifier
						gtnChild.setIdent(gAe.getNodeIdentifierIfParent());
					}

					if (j == 0 && !rootNodeSet) {
						// first node, set as delegate of rootTreenode
						rootTreeNode.setDelegate(gtnChild);
						rootTreeNode.setUserObject(gAe);
						rootTreeNode.addChild(gtnChild);
					}

					// fixdiff FXOLAT-250 :: make genericMainController aware of multi-level
					// navigation (submenues)
					else if (gAe.getParentTreeNodeIdentifier() != null) {
						// this is a sub-menu-node, do not add to tree-model already, since
						// parent tree may not yet be in model
						// (parent could be "after" child, in ActionExtensions-Collection)
						String parentNodeID = gAe.getParentTreeNodeIdentifier();
						subMenuNodes.put(gtnChild, parentNodeID);
					}
					// "normal" menu-entry
					else {
						rootTreeNode.addChild(gtnChild);
					}

					j++;
				}else{
					logInfo("found disabled GenericActionExtension for " + className);
				}
			}
		}// loop over extensions

		// fixdiff FXOLAT-250 :: make genericMainController aware of multi-level
		// navigation (submenues)
		// loop over submenuNodes and add to their parents
		for (Entry<GenericTreeNode, String> childNodeEntry : subMenuNodes.entrySet()) {
			GenericTreeNode childNode = childNodeEntry.getKey();
			GenericTreeNode parentNode = (GenericTreeNode) gtm.getNodeById(childNodeEntry.getValue());
			if (parentNode != null) {
				parentNode.addChild(childNode);
				if (parentNode.getDelegate() == null  ) {
					boolean addDelegate = true;
					
					//add delegate only if hte parent hasn't not a controller defined
					Object uo = parentNode.getUserObject();
					if(uo instanceof GenericActionExtension) {
						GenericActionExtension gae = (GenericActionExtension)uo;
						if(StringHelper.containsNonWhitespace(gae.getClassNameOfCorrespondingController())) {
							addDelegate = false;
						}
					}
					
					if(addDelegate) {
						parentNode.setDelegate(childNode);
						parentNode.setUserObject(childNode.getUserObject());
					}
				}
			} else {
				logWarn("Could not add navigation-menu (" + childNode.getTitle() + ") to parent:: " + childNodeEntry.getValue(), null);
				// make it at least appear on top level
				rootTreeNode.addChild(childNode);
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
			if (event instanceof TreeEvent && event.getCommand().equals(MenuTree.COMMAND_TREENODE_CLICKED)) {
				TreeEvent te = (TreeEvent)event;
				if(te.getSubCommand() != null) {
					// filter open/close events
				} else {
					// process menu commands
					TreeNode selTreeNode = olatMenuTree.getSelectedNode();
					// cleanup old content controller (never null)
					removeAsListenerAndDispose(contentCtr);
	
					// create new content controller
					// Following cases:
					// 1a) Simple Action Extension using only ureq and windowControl ->
					// handled by default implementation of createController
					// 1b) Specialised Action Extension which needs some more internals ->
					// handled by the class extending GenericMainController, by overwriting
					// createController
					// 2) uobject is something special which needs evaluation by class
					// extending GenericMainController
					Object uobject = selTreeNode.getUserObject();
					TreeNode delegatee = selTreeNode.getDelegate();
					if (delegatee != null) {
						olatMenuTree.setSelectedNode(delegatee);
					}
					contentCtr = getContentCtr(uobject, ureq);
					listenTo(contentCtr);
					Component resComp = contentCtr.getInitialComponent();
					content.setContent(resComp);
					addToHistory(ureq, contentCtr);
				}
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
		// default implementation for simple case where action extension.
		WindowControl bwControl = getWindowControl();
		if (olatMenuTree.getTreeModel() instanceof GenericTreeModel) {
			if (ae instanceof Extension) {
				Extension nE = (Extension) ae;

				// get our ores for the extension
				OLATResourceable ores;
				if (ae instanceof GenericActionExtension && StringHelper.containsNonWhitespace(((GenericActionExtension) ae).getNavigationKey())) {
					// there is a navigation-key, use the nice way
					ores = OresHelper.createOLATResourceableInstance(((GenericActionExtension) ae).getNavigationKey(), 0L);
				} else {
					ores = OresHelper.createOLATResourceableInstance(GMCMT, CodeHelper.getUniqueIDFromString(nE.getUniqueExtensionID()));
				}
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
				bwControl = addToHistory(ureq, ores, null);
			}
		}

		Controller ctrl = ae.createController(ureq, bwControl, null);
		if(ctrl instanceof BreadcrumbPanelAware) {
			((BreadcrumbPanelAware)ctrl).setBreadcrumbPanel(stackVC);
		}
		return ctrl;
	}

	private Controller getContentCtr(Object uobject, UserRequest ureq) {
		Controller contentCtr1Tmp = null;
		if (uobject instanceof ActionExtension) {
			ActionExtension ae = (ActionExtension) uobject;
			contentCtr1Tmp = createController(ae, ureq);
		} else {
			contentCtr1Tmp = handleOwnMenuTreeEvent(uobject, ureq);
		}
		if (contentCtr1Tmp == null) { throw new AssertException(
				"Node must either be an ActionExtension or implementation must handle this MenuTreeEvent: " + (uobject == null ? "NULL" : uobject.toString())); }
		return contentCtr1Tmp;
	}
	
	/**
	 * activates the correct treenode for a given ActionExtension
	 * @param ureq
	 * @param ae
	 */
	private void activateTreeNodeByActionExtension(UserRequest ureq, ActionExtension ae) {
		TreeNode node = ((GenericTreeModel) olatMenuTree.getTreeModel()).findNodeByUserObject(ae);
		if (node != null) {
			olatMenuTree.setSelectedNodeId(node.getIdent());
			TreeEvent te = new TreeEvent(MenuTree.COMMAND_TREENODE_CLICKED, node.getIdent());
			event(ureq, olatMenuTree, te);
		}
	}
	

	// fxdiff BAKS-7 Resume function
	private void activate(UserRequest ureq, String viewIdentifier) {
		ActionExtension ae;
		if (viewIdentifier != null && viewIdentifier.startsWith(GMCMT)) {
			Long extensionID = Long.parseLong(viewIdentifier.substring(viewIdentifier.indexOf(':') + 1));
			Extension ee = ExtManager.getInstance().getExtensionByID(extensionID);
			if(ee == null){
				logWarn("ExtManager did not find an Extension for extensionID '"+extensionID+"'. Activate canceled..." , null);
				return;
			}
			ae = (ActionExtension) ee.getExtensionFor(className, ureq);
		} else {
			int vwindex = viewIdentifier.lastIndexOf(":"); 
			String naviKey = viewIdentifier;
			if(vwindex >= 0){
				naviKey = viewIdentifier.substring(0,viewIdentifier.indexOf(':'));
			}
			ae = ExtManager.getInstance().getActionExtensionByNavigationKey(className, naviKey);
			if(ae == null){
				// this happens, if someone uses a navigation key, that no actionExtension uses...
				logWarn("couldn't find an ActionExtension for  navigationKey '"+naviKey+"' . I suggest adjusting spring configuration for GenericMainController.." , null);
			}
		}

		if(ae == null){
			// no action extension to activate...
			return;
		}
			
		try {
			if (olatMenuTree.getTreeModel() instanceof GenericTreeModel) {
				activateTreeNodeByActionExtension(ureq, ae);
			} else {
				// just for precaution (treenode selection won't work, but correct
				// content is displayed)
				contentCtr = getContentCtr(ae, ureq);
				listenTo(contentCtr);
				Component resComp = contentCtr.getInitialComponent();
				content.setContent(resComp);
				// fxdiff BAKS-7 Resume function
				addToHistory(ureq, contentCtr);
			}
		} catch (Exception e) {
			logWarn("", e);
		}
	}

	protected void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) return;

		ContextEntry entry = entries.get(0);
		TreeNode selectedNode = getMenuTree().getSelectedNode();
		String node = entry.getOLATResourceable().getResourceableTypeName();
		if (node != null && node.startsWith(GMCMT)) {
			activate(ureq, node + ":" + entries.get(0).getOLATResourceable().getResourceableId());
			if (entries.size() >= 1) {
				entries = entries.subList(1, entries.size());
			}
			if (contentCtr instanceof Activateable2) {
				((Activateable2)contentCtr).activate(ureq, entries, entry.getTransientState());
			}
		} else {
			// maybe the node is a GAE-NavigationKey ?
			GenericActionExtension gAE = ExtManager.getInstance().getActionExtensionByNavigationKey(className, node);
			if (gAE != null) {
				//if the controller is already selected, only activate it, don't reinstanciate it
				if(selectedNode != null && selectedNode.getUserObject() != gAE) {
					activateTreeNodeByActionExtension(ureq, gAE);
				}

				if (entries.size() >= 1) {
					entries = entries.subList(1, entries.size());
				}
				if (contentCtr instanceof Activateable2) {
					((Activateable2) contentCtr).activate(ureq, entries, entry.getTransientState());
				}
			}
		}
	}

	public MenuTree getMenuTree() {
		return olatMenuTree;
	}
}