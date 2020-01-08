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

import java.util.List;
import java.util.Set;

import org.olat.repository.RepositoryEntry;
import org.olat.resource.accesscontrol.provider.auto.IdentifierKey;

/**
 * Strategy to handle the IdentifierKeys
 *
 * Initial date: 14.08.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
interface IdentifierKeyHandler {

	/**
	 * Specifies for which identifier the handler can be used.
	 */
	public IdentifierKey getIdentifierKey();

	/**
	 * Find the RepositoryEntries by a given value.
	 *
	 * @param value the value to search for
	 * @return the RepositoryEntries found for the given value
	 */
	public List<RepositoryEntry> find(String value);

	/**
	 * Finds the appropriate values in the RepositoryEntry.
	 *
	 * @param entry
	 * @return
	 */
	public Set<String> getRepositoryEntryValue(RepositoryEntry entry);

}
