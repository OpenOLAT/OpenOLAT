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
package org.olat.resource.accesscontrol.provider.auto.manager;

import java.util.ArrayList;
import java.util.List;

import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.resource.accesscontrol.provider.auto.IdentifierKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * Initial date: 15.08.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
class ExternalRefHandler implements IdentifierKeyHandler {

	@Autowired
	private RepositoryService repositoryService;

	@Override
	public IdentifierKey getIdentifierKey() {
		return IdentifierKey.externalRef;
	}

	@Override
	public List<RepositoryEntry> find(String value) {
		List<RepositoryEntry> entries = new ArrayList<>();

		try {
			entries = repositoryService.loadRepositoryEntriesByExternalRef(value);
		} catch (Exception e) {
			// nothing to add
		}

		return entries;
	}

	@Override
	public String getRepositoryEntryValue(RepositoryEntry entry) {
		return entry.getExternalRef();
	}

}
