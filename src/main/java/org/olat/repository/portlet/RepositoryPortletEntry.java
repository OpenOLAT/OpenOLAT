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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.repository.portlet;

import org.olat.core.gui.control.generic.portal.PortletEntry;
import org.olat.repository.RepositoryEntry;

/**
 * Description:<br>
 * Wrapper for a repository entry
 * 
 * <P>
 * Initial Date: 06.03.2009 <br>
 * 
 * @author gnaegi
 */
public class RepositoryPortletEntry implements PortletEntry<RepositoryEntry> {
	private RepositoryEntry value;
	private Long key;

	public RepositoryPortletEntry(RepositoryEntry repoEntry) {
		value = repoEntry;
		key = repoEntry.getKey();
	}

	public Long getKey() {
		return key;
	}

	public RepositoryEntry getValue() {
		return value;
	}
}
