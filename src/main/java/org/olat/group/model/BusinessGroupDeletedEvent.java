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
package org.olat.group.model;

import java.util.List;

import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 17 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupDeletedEvent extends MultiUserEvent {
	
	private static final long serialVersionUID = -3427749486335676686L;

	public static final String RESOURCE_DELETED_EVENT = "resource.deleted.event";
	
	private final List<Long> memberKeys;
	private final List<Long> repositoryEntryKeys;
	
	public BusinessGroupDeletedEvent(String cmd, List<Long> memberKeys, List<Long> repositoryEntryKeys) {
		super(cmd);
		this.memberKeys = memberKeys;
		this.repositoryEntryKeys = repositoryEntryKeys;
	}

	public List<Long> getMemberKeys() {
		return memberKeys;
	}

	public List<Long> getRepositoryEntryKeys() {
		return repositoryEntryKeys;
	}
}
