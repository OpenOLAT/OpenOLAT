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
package org.olat.modules.edusharing.model;

import org.olat.modules.edusharing.NodeIdentifier;

/**
 * 
 * Initial date: 5 Dec 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NodeIdentifierImpl implements NodeIdentifier {

	private final String repositoryId;
	private final String nodeId;
	
	public NodeIdentifierImpl(String repositoryId, String nodeId) {
		this.repositoryId = repositoryId;
		this.nodeId = nodeId;
	}

	@Override
	public String getRepositoryId() {
		return repositoryId;
	}

	@Override
	public String getNodeId() {
		return nodeId;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NodeIdentifier [repositoryId=");
		builder.append(repositoryId);
		builder.append(", nodeId=");
		builder.append(nodeId);
		builder.append("]");
		return builder.toString();
	}

}
