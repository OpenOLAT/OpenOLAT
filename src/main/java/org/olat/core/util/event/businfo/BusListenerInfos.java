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
package org.olat.core.util.event.businfo;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.olat.core.id.OLATResourceable;

/**
 * Description:<br>
 * Helper class for the cluster mode to keep track and sum data about listeners cnt on a olat event bus channel.
 * listener count on a channel is used as an informative message mostly for authors (e.g. to decide whether or not they want to
 * publish a course which invalidates the course and thus kicks out all users of this course.)
 * 
 * in the cluster mode, the clusterInfoEvent from a node also contains information about how many listeners listen to a resource.
 * summing up all listeners from all nodes gives the current total. 
 * 
 * <P>
 * Initial Date:  05.11.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class BusListenerInfos {
	// key: nodeId, values: a map with keys: derivedString of a olatresourceable; values: listener count of this node
	private Map<Integer, BusListenerInfo> nodeBusInfos = new ConcurrentHashMap<>();
	
	public int getListenerCountFor(OLATResourceable ores) {
		synchronized (nodeBusInfos) {//cluster_ok
			// sum up current counts from all nodes
			int total = 0;
			for (BusListenerInfo businfo : nodeBusInfos.values()) {
				total+= businfo.getCountFor(ores);
			}			
			return total;
		}
	}
	
	/**
	 * overrides/replace any previous buslistenerInfo for the given nodeId
	 * @param nodeId the nodeId of the cluster to which the buslistenerinfo belongs to.
	 * @param info the buslistenerInfo
	 */
	public void updateInfoFor(Integer nodeId, BusListenerInfo info) {
		synchronized (nodeBusInfos) {//cluster_ok
			nodeBusInfos.put(nodeId, info);
		}
	}
	
	public String getAsString() {
		StringBuilder sb = new StringBuilder();
		Set<String> allNodesDerivedStrings = new TreeSet<>(); // the derived strings are sorted then
		for (BusListenerInfo busInfo : nodeBusInfos.values()) {
			allNodesDerivedStrings.addAll(busInfo.getAllDerivedStrings());
		}
		
		// for each derived-string, print out the total number of listeners, and the contribution of each node
		for (String derived : allNodesDerivedStrings) {
			int total = 0;
			sb.append(derived).append(" : ");
			for (Entry<Integer, BusListenerInfo> businfoEntry : nodeBusInfos.entrySet()) {
				Integer nodeId = businfoEntry.getKey();
				int cnt = businfoEntry.getValue().getCountFor(derived); 
				sb.append("'").append(nodeId).append("':").append(cnt).append("; ");
				total+= cnt;
			}
			sb.append(" Sum:").append(total).append("<br />");
		}
		return sb.toString();
	}
}
