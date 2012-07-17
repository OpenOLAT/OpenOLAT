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
*/

package org.olat.course;

import java.io.Serializable;

import org.olat.course.nodes.CourseNode;

/**
 * Description:<BR/> The structure is ordered tree like hierarchical structure
 * of course nodes. It is used in OLAT to represent the course structure. <P/>
 * Initial Date: Jan 28, 2004
 * 
 * @author Mike Stock
 */
public class Structure implements Serializable {

	private CourseNode rootNode = null;
	transient private final static int CURRENTVERSION = 3;
	private int version;
	

	/**
	 * Constructor for the OLAT course structure
	 */
	public Structure() {
	// nothing to do
		this.version = CURRENTVERSION;
	}

	/**
	 * @return The root node
	 */
	public CourseNode getRootNode() {
		return rootNode;
	}

	/**
	 * @param node The node that should be the new root node
	 */
	public void setRootNode(CourseNode node) {
		rootNode = node;
	}

	/**
	 * Searches for a course node with the given node id starting at the root node
	 * 
	 * @param nodeId
	 * @return The found course node or null if no such node exists
	 */
	public CourseNode getNode(String nodeId) {
		return findNode(rootNode, nodeId);
	}

	/**
	 * Searches for a course node with the given node id starting at the given
	 * course node
	 * 
	 * @param node
	 * @param nodeId
	 * @return The found course node or null if no such node exists
	 */
	private CourseNode findNode(CourseNode node, String nodeId) {
		if (node.getIdent().equals(nodeId)) return node;
		int childcnt = node.getChildCount();
		for (int i = 0; i < childcnt; i++) {
			CourseNode element = (CourseNode) node.getChildAt(i);
			CourseNode foo = findNode(element, nodeId);
			if (foo != null) return foo;
		}
		return null;
	}

	

	/**
	 * @return Returns the version.
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @param version The version to set.
	 */
	public void setVersion(int version) {
		this.version = version;
	}
	
	public boolean isVersionUpToDate(){
		if (Integer.valueOf(version) == null || version < CURRENTVERSION) return false;
		return true;
	}
}