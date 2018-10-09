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
package org.olat.modules.webFeed.dispatching;

/**
 * 
 * Initial date: 9 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FeedPathKey {
	
	private final Long identityKey;
	private final Long ressourceId;
	private final String nodeId;
	
	public FeedPathKey(Long identityKey, Long ressourceId, String nodeId) {
		this.identityKey = identityKey;
		this.ressourceId = ressourceId;
		this.nodeId = nodeId;
	}
	
	public Long getResourceId() {
		return ressourceId;
	}
	
	@Override
	public int hashCode() {
		return ressourceId.hashCode()
				+ (identityKey == null ? 1819879 : identityKey.hashCode())
				+ (nodeId == null ? 52387 : nodeId.hashCode());
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof FeedPathKey) {
			FeedPathKey key = (FeedPathKey)obj;
			return ressourceId.equals(key.ressourceId)
					&& ((identityKey == null && key.identityKey == null) || (identityKey != null && identityKey.equals(key.identityKey)))
					&& ((nodeId == null && key.nodeId == null) || (nodeId != null && nodeId.equals(key.nodeId)));
			
		}
		return false;
	}
}
