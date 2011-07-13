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
 * Copyright (c) 2007 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.core.gui.control.generic.ajax.tree;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <h3>Description:</h3>
 * The AjaxTreeNode is a wrapper object for tree nodes used in the dynamic tree
 * model. There are various configuration flags that can be set for this node.
 * Use the treeNode.put(key,value) method and the static Strings to set those
 * flags.
 * <p>
 * The actual node from your data model is not in this node, the only reference
 * to the real data object is the node identifyer. This AjaxTreeNode is only used
 * for displaying purposes.
 * <p>
 * Initial Date: 04.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class AjaxTreeNode extends JSONObject {
	public static String CONF_ALLOWDRAG = "allowDrag";

	public static String CONF_ALLOWDROP = "allowDrop";

	public static String CONF_CLS = "cls";

	public static String CONF_HREF = "href";

	public static String CONF_HREFTARGET = "hrefTarget";

	public static String CONF_ICON = "icon";

	public static String CONF_ICON_CSS_CLASS = "iconCls";	

	public static String CONF_ID = "id";

	/** false: adds + icon next to item */
	public static String CONF_LEAF = "leaf";

	public static String CONF_QTIP = "qtip";

	public static String CONF_QTIPCFG = "qtipCfg";

	public static String CONF_SINGLECLICKEXPAND = "singleClickExpand";

	public static String CONF_TEXT = "text";

	public static String CONF_UIPROVIDER = "uiProvider";

	public static String CONF_DISABLED = "disabled";
	
	/** true to start the node expanded */
	public static String CONF_EXPANDED = "expanded"; 
	
	/** force to show +/- icon */
	public static String CONF_EXPANDABLE = "expandable";

	/** disallow element itself as target but allow to add/move children (allowdrop=true) */
	public static String CONF_ISTARGET = "isTarget";
	
	// custom attributes
	
  /** orders leafs below folders */
	public static String CONF_IS_TYPE_LEAF = "isTypeLeaf"; 
	
	/**
	 * Constructor
	 * 
	 * @param id
	 *            The ID of this tree node. Must be unique within the entire
	 *            tree
	 * @param attributes
	 *            Node specific attributes. Use this for your proprietary values
	 * @param text
	 *            The text lable of the node
	 * @throws JSONException
	 */
	public AjaxTreeNode(String id, String text)
			throws JSONException {
		super.put(CONF_ID, id);
		super.put(CONF_TEXT, text);
	}
}
