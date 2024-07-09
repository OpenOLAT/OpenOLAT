/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.cemedia.ui;

import java.util.Collection;

import org.olat.modules.cemedia.MediaHandler;
import org.olat.modules.cemedia.model.SearchMediaParameters.Access;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 15 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public record MediaCenterConfig(boolean withSelect, boolean withAddMedias, boolean withHelp, boolean withUploadCard, boolean withMediaSelection, boolean withMultiSelect, boolean withMultiSelectDelete,
		boolean withQuota, String preselectedType, Collection<String> restrictedTypes, String defaultFilterTab, Access access, RepositoryEntry repositoryEntry) {

	public static final MediaCenterConfig valueOfChooser(RepositoryEntry repositoryEntry, boolean withAddMedias, boolean withMediaSelection) {
		return new MediaCenterConfig(true, withAddMedias, true, false, true, true, true, withMediaSelection, null, null,
				(repositoryEntry == null ? MediaCenterController.SHARED_TAB_WITH_ME_ID : MediaCenterController.SHARED_TAB_WITH_ENTRY),
				Access.DIRECT, repositoryEntry);
	}
	
	public static final MediaCenterConfig valueOfUploader(MediaHandler handler,
			boolean withUploadCard, RepositoryEntry repositoryEntry) {
		return new MediaCenterConfig(true, false, false, withUploadCard, true, false, false, true, handler.getType(), null,
				(repositoryEntry == null ? MediaCenterController.SHARED_TAB_WITH_ME_ID : MediaCenterController.SHARED_TAB_WITH_ENTRY),
				Access.DIRECT, repositoryEntry);
	}
	
	public static final MediaCenterConfig valueOfMy() {
		return new MediaCenterConfig(false, true, true, false, true, true, true, true, null, null, MediaCenterController.MY_TAB_ID, Access.DIRECT, null);
	}
	
	public static final MediaCenterConfig managementConfig() {
		return new MediaCenterConfig(false, false, true, false, true, true, true, false, null, null, MediaCenterController.ALL_TAB_ID, Access.INDIRECT, null);
	}
}
