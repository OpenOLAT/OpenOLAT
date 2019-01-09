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
package org.olat.repository.ui.settings;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.vfs.VFSItem;
import org.olat.modules.edusharing.EdusharingProvider;
import org.olat.modules.edusharing.UsageMetadata;
import org.olat.modules.edusharing.VFSEdusharingProvider;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * 
 * Initial date: 8 Jan 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LazyRepositoryEdusharingProvider implements VFSEdusharingProvider {
	
	private final Long repositoryEntryKey;
	private String subPath;
	private EdusharingProvider edusharingProvider;
	
	public LazyRepositoryEdusharingProvider(Long repositoryEntryKey) {
		this(repositoryEntryKey, null);
	}

	public LazyRepositoryEdusharingProvider(Long repositoryEntryKey, String subPath) {
		this.repositoryEntryKey = repositoryEntryKey;
		this.subPath = subPath;
	}

	private EdusharingProvider getEdusharingProvider() {
		if (edusharingProvider == null) {
			RepositoryManager repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
			RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(repositoryEntryKey);
			edusharingProvider = new RepositoryEdusharingProvider(repositoryEntry, null);
		}
		return edusharingProvider;
	}

	@Override
	public void setSubPath(VFSItem item) {
		this.subPath = "file-meta-uuid-" + item.getMetaInfo().getUUID();
	}

	@Override
	public OLATResourceable getOlatResourceable() {
		return getEdusharingProvider().getOlatResourceable();
	}

	@Override
	public String getSubPath() {
		return subPath;
	}

	@Override
	public UsageMetadata getUsageMetadata() {
		return getEdusharingProvider().getUsageMetadata();
	}

}
