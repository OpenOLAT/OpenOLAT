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
package org.olat.core.util.cluster;

import java.io.Serializable;

/**
 * Description:<br>
 * information about a cluster.
 * contains data such as the id of the cluster to identify where messages come from and other data.
 * 
 * 
 * <P>
 * Initial Date:  10.10.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class ClusterConfig implements Serializable {

	private static final long serialVersionUID = 2413005126081334743L;
	private Integer nodeId;
	private long startupTime;

	public ClusterConfig() {
		startupTime = System.currentTimeMillis();
	}
	
	public Integer getNodeId() {
		return nodeId;
	}

	/**
	 * 
	 * @param nodeId an Integer identifying a cluster node, between 1 and 63, must be unique in the olat cluster.
	 */
	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * 
	 * @return the time when this node (jmv) was started 
	 */
	public long getStartupTime() {
		return startupTime;
	}
}
