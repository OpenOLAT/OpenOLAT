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
 * Initial date: 26 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryPermanentlyDeletedEvent extends MultiUserEvent {

	private static final long serialVersionUID = -5250866056350158178L;
	
	private Long repositoryEntryKey;

	public RepositoryEntryPermanentlyDeletedEvent(Long repositoryEntryKey) {
		super("re-permanently-deleted");
		this.repositoryEntryKey = repositoryEntryKey;
	}

	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}
}
