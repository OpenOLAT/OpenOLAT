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
package org.olat.repository.model;

import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 13 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryStatusChangedEvent extends MultiUserEvent {

	private static final long serialVersionUID = -8624039692057985920L;
	
	public static final String STATUS_CHANGED = "repo.entry.status.changed";

	private Long repositoryEntryKey;

	public RepositoryEntryStatusChangedEvent(Long repositoryEntryKey) {
		super(STATUS_CHANGED);
		this.repositoryEntryKey = repositoryEntryKey;
	}

	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}
}
