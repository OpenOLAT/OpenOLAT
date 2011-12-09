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
package org.olat.core.gui.control.generic.ajax.tree;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.logging.AssertException;
import org.olat.core.util.ConsumableBoolean;
import org.olat.core.util.StringHelper;
import org.olat.core.util.URIHelper;
import org.olat.core.util.prefs.Preferences;

/**
 * <h3>Description:</h3>
 * The ajax tree controller provides a dynamic tree view with drag and drop
 * support. The datamodel is loaded dynamically on request for each hierarchy
 * level. <br>
 * You must provide a AjaxTreeModel object that implements such a dynamic data
 * model.
 * <p>
 * <h3>Events thrown by this controller:</h3>
 * <ul>
 * <li>new MoveTreeNodeEvent(node, oldParent, newParent, position)</li>
 * <li>new TreeNodeClickedEvent(node)</li>
 * <li>new TreeNodeModifiedEvent(node, modifiedValue)</li>
 * </ul>
 * <p>
 * Initial Date: 04.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class TreeController extends BasicController {
	private static final String CMD_PING = "ping";
	private static final String CMD_CLICK = "click";
	private static final String CMD_MOVE = "move";
	private static final String CMD_EDIT = "edit";
	private static final String CMD_EXPAND = "expand";
	private static final String CMD_COLLAPSE = "collapse";
	private static final String PARAM_INDEX = "index";
	private static final String PARAM_NEW_PARENT = "newParent";
	private static final String PARAM_OLD_PARENT = "oldParent";
	private static final String ENCODING_UTF_8 = "utf-8";
	private static final String CONTENT_TYPE_JAVASCRIPT = "application/javascript;";
	private static final String PARAM_NODE = "node";
	private static final String PARAM_PATH = "path";
	private static final String PARAM_VALUE = "value";

	private static final String GUI_PREFS_KEY_PREFIX = "expanded_";
	
	// This media resource is used as generic ok-return values for ajax requests
	private static final StringMediaResource okMediaResource;
	static {
		okMediaResource = new StringMediaResource();
		okMediaResource.setContentType(CONTENT_TYPE_JAVASCRIPT);
		okMediaResource.setEncoding(ENCODING_UTF_8);
		okMediaResource.setData("b_amt_status = true;");
	}

	
	private VelocityContainer mainVC, functionCallsVC;
	private String treePanelName;

	private Mapper treeDataMapper;
	private String guiPrefsKey;

	/**
	 * Constructor for a dynamic ajax tree controller.
	 * 
	 * @param ureq
	 *            The user request
	 * @param wControl
	 *            The window control
	 * @param titleRootNode
	 *            Title displayed on the root node
	 * @param dataModel
	 *            The dynamic datamodel for this tree
	 * @param nodeDragOverCallback
	 *            Javascript method used as callback when a node is draged and
	 *            moved around over the tree. The method must be defined
	 *            somewhere else in your code, in this case you provide the
	 *            method name. Alternatively you cen define the method right
	 *            here in the string with function(event){}. Use NULL if not
	 *            used.
	 */
	public TreeController(UserRequest ureq, WindowControl wControl, final String titleRootNode, final AjaxTreeModel dataModel,
			String nodeDragOverCallback) {
		super(ureq, wControl);
		
		// The velocity main container
		mainVC = createVelocityContainer("tree");
		// The javascript identifier for the tree panel
		treePanelName = "o_streePanel" + mainVC.getDispatchID();
		// Load javascript files
		JSAndCSSComponent jsComp = new JSAndCSSComponent("jsComp", this.getClass(), new String[]{"tree.js"}, null, false);
		mainVC.put("jsComp", jsComp);

		// The container that contains the JS code to select, remove or reload a
		// specific path. Is in a separate container so that a node can be
		// selected without making the mainVC dirty.
		functionCallsVC = createVelocityContainer("functioncalls");
		mainVC.put("functioncalls", functionCallsVC);
		// init with false values
		functionCallsVC.contextPut("selectPath", Boolean.FALSE);
		functionCallsVC.contextPut("reloadPath", Boolean.FALSE);
		functionCallsVC.contextPut("removePath", Boolean.FALSE);
		functionCallsVC.contextPut("treePanelName", treePanelName);

		// The data mapper provides the dynamic data model to the view
		treeDataMapper = new Mapper() {
			public MediaResource handle(String relPath, HttpServletRequest request) {
				// each call is done for a specific node: get the child elements
				// for this node
				String data;
				String nodeId = request.getParameter(PARAM_NODE);
				if (StringHelper.containsNonWhitespace(nodeId)) {
					JSONArray jsonData = new JSONArray();
					List<AjaxTreeNode> children = dataModel.getChildrenFor(nodeId);
					for (AjaxTreeNode child : children) {
						jsonData.put(child);
					}
					data = jsonData.toString();
				} else {
					// something is wrong, maybe some hacker attack
					logError("Got a tree data mapper request but node parameter is missing. Root node title was::" + titleRootNode, null);
					data = "";
				}
				StringMediaResource mediaResource = new StringMediaResource();
				mediaResource.setEncoding(ENCODING_UTF_8);
				mediaResource.setContentType(CONTENT_TYPE_JAVASCRIPT);
				mediaResource.setData(data);
				return mediaResource;
			}
		};
		String dataMapperUri = registerMapper(treeDataMapper);
		mainVC.contextPut("dataMapperUri", dataMapperUri);

		// Some variables for the tree
		String treeID = dataModel.getTreeModelIdentifyer();
		mainVC.contextPut("treeId", dataModel.getTreeModelIdentifyer());
		mainVC.contextPut("titleRootNode", titleRootNode);

		// Add the drag over callback. Can also be null
		mainVC.contextPut("nodeDragOverCallback", nodeDragOverCallback);
		
		// Set default sort order
		setTreeSorting(true, true, true);
		
		// Disable tree inline editing by default
		setTreeInlineEditing(false, null, null);
		
		// expand pathes from last time
		Set<String> expandedPathes = null;
		if (treeID != null) {
			guiPrefsKey = GUI_PREFS_KEY_PREFIX + treeID;
			Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
			expandedPathes = (Set<String>) guiPrefs.get(TreeController.class, guiPrefsKey);
			mainVC.contextPut("persistPathes", Boolean.TRUE);
		} else {
			mainVC.contextPut("persistPathes", Boolean.FALSE);
		}
		if (expandedPathes == null) {
			expandedPathes = new HashSet<String>();
			// expand root by default
			String ident = dataModel.getTreeModelIdentifyer();
			// expand also for models without identifyer
			if(ident == null) ident = "$treeId"; 
			expandedPathes.add("/" +ident);
		} 
		mainVC.contextPut("expandedPathes", expandedPathes);
		
		// Add the custom root icon if available
		String customRootIconCss = dataModel.getCustomRootIconCssClass();
		if (customRootIconCss == null) {
			mainVC.contextPut("hasCustomRootCSS", Boolean.FALSE);
		} else {
			mainVC.contextPut("hasCustomRootCSS", Boolean.TRUE);
			mainVC.contextPut("customRootCSS", customRootIconCss);
		}

		putInitialPanel(mainVC);
	}
	
	//fxdiff FXOLAT-132: alert unsaved changes in HTML editor
	public long getTreePanelID() {
		return mainVC.getDispatchID();
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == mainVC) {
			// First parse the request using the URI helper. By default tomcat
			// has problems to decode the URI parameters properly because by
			// definition the URI is ISO encoded. ureq.getParameter() does use
			// the standard tomcat behaviour. Quite a mess, see here:
			// http://marc.info/?l=tomcat-user&m=121762788714431
			// The URIHelper decodes the uri parameters using UTF-8
			// (OLAT-3846)
			URIHelper uriHelper;
			String reqUri = ureq.getHttpReq().getRequestURI();
			String query = ureq.getHttpReq().getQueryString();
			String getUri = reqUri + "?" + query;
			try {
				uriHelper = new URIHelper(getUri);
			} catch (URISyntaxException e) {
				logWarn("Could not generate URIHelper for URI::" + getUri, e);
				return;
			}

			// handle move events: fire event to parent controller and let him decide
			// if the move should be allowed or not
			if (event.getCommand().equals(CMD_MOVE)) {
				String node = uriHelper.getParameter(PARAM_NODE);
				String oldParent = uriHelper.getParameter(PARAM_OLD_PARENT);
				String newParent = uriHelper.getParameter(PARAM_NEW_PARENT);
				String index = uriHelper.getParameter(PARAM_INDEX);
				int position = Integer.parseInt(index);
				// notify parent controller about the move
				MoveTreeNodeEvent moveEvent = new MoveTreeNodeEvent(node, oldParent, newParent, position);
				fireEvent(ureq, moveEvent);
				// prepare response as javascript string
				StringMediaResource smr = new StringMediaResource();
				// content type javascript forces menu tree to eval result
				smr.setContentType(CONTENT_TYPE_JAVASCRIPT);
				smr.setEncoding(ENCODING_UTF_8);
				if (moveEvent.isResultSuccess()) {
					// send ok back
					smr.setData("b_amt_status=true;");
				} else {
					// send failure and some messages for the user
					smr.setData("b_amt_status_title=\"" + moveEvent.getResultFailureTitle() + "\", b_amt_status_msg=\""
							+ moveEvent.getResultFailureMessage() + "\"; b_amt_status=false;");
				}
				ureq.getDispatchResult().setResultingMediaResource(smr);

			} else if (event.getCommand().equals(CMD_CLICK)) {
				String node = uriHelper.getParameter(PARAM_NODE);
				TreeNodeClickedEvent clickedEvent = new TreeNodeClickedEvent(node);
				fireEvent(ureq, clickedEvent);

			} else if (event.getCommand().equals(CMD_EDIT)) {
				String node = uriHelper.getParameter(PARAM_NODE);
				String newValue = uriHelper.getParameter(PARAM_VALUE);
				TreeNodeModifiedEvent editedEvent = new TreeNodeModifiedEvent(node, newValue);
				fireEvent(ureq, editedEvent);

			} else if (event.getCommand().equals(CMD_EXPAND)) {
				Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
				String expandedPath = uriHelper.getParameter(PARAM_PATH);
				Set<String> oldPathes = (Set<String>) guiPrefs.get(TreeController.class, guiPrefsKey);
				if (oldPathes == null) oldPathes = new HashSet<String>();
				Set<String> newPathes = new HashSet();
				for (String oldPath : oldPathes) {
					// remove all parent pathes to reduce redundancy
					if (!expandedPath.startsWith(oldPath)) newPathes.add(oldPath);
				}
				// add newly expaned node
				newPathes.add(expandedPath);
				guiPrefs.putAndSave(TreeController.class, guiPrefsKey, newPathes);
				// return empty resource
				ureq.getDispatchResult().setResultingMediaResource(okMediaResource);
				
			} else if (event.getCommand().equals(CMD_COLLAPSE)) {
				Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
				String collapsedPath = uriHelper.getParameter(PARAM_PATH);
				Set<String> oldPathes = (Set<String>) guiPrefs.get(TreeController.class, guiPrefsKey);
				if (oldPathes == null) oldPathes = new HashSet<String>();
				Set<String> newPathes = new HashSet();
				for (String oldPath : oldPathes) {
					// remove all child pathes
					if (!oldPath.startsWith(collapsedPath)) newPathes.add(oldPath);
				}
				guiPrefs.putAndSave(TreeController.class, guiPrefsKey, newPathes);
				// return empty resource
				ureq.getDispatchResult().setResultingMediaResource(okMediaResource);

			} else if (event.getCommand().equals(CMD_PING)) {
				// Nothing special to do. If any component is dirty, the framework has
				// now the possibility to clean up, this is why this ping is necessary
			}
			
			// in all cases: don't re-render container, this are all ajax events!
			if(mainVC != null) {
				//check not null if an ajax-event survives the disposed method for a small time
				//this prevents unwanted errors and break in the commit chain
				mainVC.setDirty(false);
			}
		}
	}

	/**
	 * Enable sorting in tree
	 * 
	 * @param doSort
	 *            true: sorting enabled; false: no sorting
	 * @param asc
	 *            true: sort ascending; false: sort descending
	 * @param foldersFirst
	 *            true: show sorted folder items first (items with attribute
	 *            'isTypeLeaf') and then sorted leafs
	 */
	public void setTreeSorting(boolean doSort, boolean asc, boolean foldersFirst) {
		if ((asc || foldersFirst) && !doSort) {
			throw new AssertException("Programming error: can not sort ascending or folderFirst when sorting flag is set to false");
		}
		mainVC.contextPut("doSort", Boolean.valueOf(doSort));
		mainVC.contextPut("sortOrder", (asc ? "asc" : "desc"));
		mainVC.contextPut("foldersFirst", Boolean.valueOf(foldersFirst));		
	}
	
	/**
	 * Enable or disable the tree inline editing mode. Disabled by default.
	 * 
	 * @param treeEditingEnabled
	 *            true: enable tree editing; false: disable tree editing
	 * @param beforeStartEditCallback
	 *            Javascript method used as callback when a node editor is
	 *            started. The method must be defined somewhere else in your
	 *            code, in this case you provide the method name. Alternatively
	 *            you cen define the method right here in the string with
	 *            function(event){}. Use NULL if not used. The function can
	 *            return false to cancel the edit request (e.g. to implement
	 *            non-editable nodes)
	 * @param beforeCompleteCallback
	 *            Javascript method used as callback when a node is edited and
	 *            the user closes the editor to save the changes. The method
	 *            must be defined somewhere else in your code, in this case you
	 *            provide the method name. Alternatively you can define the
	 *            method right here in the string with function(event){}. Use
	 *            NULL if not used. The function can return false to stop the
	 *            edit request and revert to the original state (e.g. to check
	 *            for valid new values)
	 */
	public void setTreeInlineEditing(boolean treeEditingEnabled,
			String beforeStartEditCallback, String beforeCompleteCallback) {
		mainVC.contextPut("treeEditingEnabled", Boolean
				.valueOf(treeEditingEnabled));
		if (beforeStartEditCallback != null) {			
			mainVC.contextPut("beforeStartEditCallback", beforeStartEditCallback);
		}
		if (beforeCompleteCallback != null)
			mainVC.contextPut("beforeCompleteCallback", beforeCompleteCallback);
	}

	/**
	 * Select a certain node in the tree by the node path. When the tree is
	 * already rendered, the activation of the node in the GUI is only performed
	 * when the next AJAX render cycle is performed. 
	 * <br />
	 * Example path: /rootNodeId/firstLevelNodeId/secondLevelNodeId
	 * This will select the node with the ID secondLevelNodeId
	 * 
	 * @param selectedPath The path of the node or NULL
	 */
	public void selectPath(String selectedPath) {
		functionCallsVC.contextPut("selectPath", (selectedPath == null ? Boolean.FALSE : new ConsumableBoolean(true)));
		functionCallsVC.contextPut("selectedPath", selectedPath);
	}

	/**
	 * Remove a certain node in the tree by the node path. This is only a
	 * rendering helper method to remove a node in the GUI that has already been
	 * removed in the datamodel. It does not change the datamodel whatsoever. 
	 * <br />
	 * Example path: /rootNodeId/firstLevelNodeId/secondLevelNodeId 
	 * This will remove the node with the ID secondLevelNodeId and all its children from
	 * the rendered tree
	 * 
	 * @param removedPath The path of the node or NULL
	 */
	public void removePath(String removedPath) {
		functionCallsVC.contextPut("removePath", (removedPath == null ? Boolean.FALSE : new ConsumableBoolean(true)));
		functionCallsVC.contextPut("removedPath", removedPath);
	}

	/**
	 * Reload a certain node in the tree by the node path. This will force the
	 * AJAX tree to reload the datamodel for the given node. This can be used
	 * e.g. when on the server side a new node has been added to the model and
	 * you don't want to reload the entire tree.
	 * <br />
	 * Example path: /rootNodeId/firstLevelNodeId/secondLevelNodeId 
	 * This will reload the node with the ID secondLevelNodeId
	 * 
	 * @param reloadedPath the path of the node or NULL
	 */
	public void reloadPath(String reloadedPath) {
		functionCallsVC.contextPut("reloadPath", (reloadedPath == null ? Boolean.FALSE : new ConsumableBoolean(true)));
		functionCallsVC.contextPut("reloadedPath", reloadedPath);
	}

	/**
	 * Sets the root node title
	 * 
	 * @param title The title of the root node
	 */
	public void setRootNodeTitle(String title) {
		mainVC.contextPut("titleRootNode", title);
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// Cleanup on java script level
		
		// Cleanup javascript objects on browser side by triggering dispose
		// function
		if (mainVC != null) {		
			StringBuffer sb = new StringBuffer();
			sb.append("if (o_info.objectMap.containsKey('")
					.append(treePanelName)
					.append("')) {var oldTree = o_info.objectMap.removeKey('")
					.append(treePanelName)
					.append("');if (oldTree) { oldTree.destroy();} oldTree = null;}");
			JSCommand jsCommand = new JSCommand(sb.toString());
			getWindowControl().getWindowBackOffice().sendCommandTo(jsCommand);
		}
		
		// mapper is auto deregistered by basic controller
		mainVC = null;
		treeDataMapper = null;
	}
}
