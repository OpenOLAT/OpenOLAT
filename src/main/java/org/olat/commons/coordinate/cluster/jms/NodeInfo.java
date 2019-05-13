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
package org.olat.commons.coordinate.cluster.jms;

import java.io.Serializable;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.cluster.ClusterConfig;

/**
 * Description:<br>
 * extended info about a cluster node. 
 * the node info as seen from another node and enhanced with some stats.
 * 
 * <P>
 * Initial Date:  23.10.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class NodeInfo implements Serializable {
	private static final long serialVersionUID = 4043790055849121385L;
	private static final Logger log = Tracing.createLoggerFor(NodeInfo.class);
	
	// the id of this node
	private final Integer nodeId;
	
	// the latest received msgId from this node here.
	private long latestReceivedMsgId = -1;
	
	// the total number of received messages from this node
	private long numOfReceivedMessages = 0;

	// the configuration of the cluster. updated on each reception of a clusterinfoevent
	private ClusterConfig config;

	// the number of missed messages from this node
	private long numOfMissedMsgs;

	NodeInfo(Integer nodeId) {
		this.nodeId = nodeId;		
	}

	/**
	 * @return the nodeid
	 */
	public Integer getNodeId() {
		return nodeId;
	}

	/**
	 * @param cie
	 * synchronized for the rare case a clusterinfoevent was delay and arrives together with the next one.
	 */
	public synchronized void update(ClusterInfoEvent cie) {//cluster_ok is per vm only
		// rewrite the config to reflect config changes that might have happened in other nodes
		config = cie.getConfig();		
	}
	
	/**
	 * @param jmsWrapper
	 * synchronized: multiple messages can come in at the same time
	 * 
	 */
	public synchronized boolean update(JMSWrapper jmsWrapper) {//cluster_ok is per vm only
		numOfReceivedMessages++;
		long currentId = jmsWrapper.getMsgId();
		long expected = latestReceivedMsgId + 1;
		
		boolean success = true;
		if (expected==0) {
			// don't compare expected with actual since we only just started and we just take the
			// first message as the correct initial.
			// see OLAT-3630
			// hence: do nothing - don't warn or error
		} else if (currentId > expected) {
			// we missed a message -> warn and adjust jmx data
			numOfMissedMsgs++;
			log.warn("missed a msg from node '" + nodeId + "': expected "+expected+", but received:" + currentId + ", nodeInfo: " + this);
			success = false;
		} else if (currentId < expected) {
			// the foreign node has 
			// a) been restarted and thus started with olat-msg-ids starting from 0.
			// b) a packet was delayed so that it arrived later than a more recently sent packet.
			log.info("node with id "+nodeId+" was restarted or packet was delayed, we received a msg: expected "+expected+", but received:" + currentId);
		} // else fine
		
		// ajust in all cases to reset error condition
		latestReceivedMsgId = currentId;		
		return success;
	}
	
	// getters
	public synchronized long getNumOfReceivedMessages() {//cluster_ok is per vm only
		return numOfReceivedMessages;
	}

	public synchronized long getLatestReceivedMsgId() {//cluster_ok is per vm only
		return latestReceivedMsgId;
	}
	
	public synchronized long getNumOfMissedMsgs() {//cluster_ok is per vm only
		return numOfMissedMsgs;
	}
	
	public ClusterConfig getConfig() {
		return config;
	}

}

