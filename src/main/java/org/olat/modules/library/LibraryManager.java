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
package org.olat.modules.library;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.library.ui.CatalogItem;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 29 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface LibraryManager {
	
	public String getDirectoryPath();
	
	public VFSContainer getSharedFolder();
	
	public void removeExistingLockFile();
	
	public void lockFolderAndPreventDoubleIndexing();
	
	public VFSContainer getUploadFolder();
	
	public RepositoryEntry getCatalogRepoEntry();
	
	public void setCatalogRepoEntry(RepositoryEntry repoEntry);
	
	public VFSLeaf getFileByUUID(String uuid);
	
	public CatalogItem getCatalogItemByUUID(String uuid, Locale locale);
	
	
	public List<CatalogItem> getNewCatalogItems(Date from, Locale locale);
	
	public List<CatalogItem> getNewestCatalogItems(Locale locale, int maxResult);
	
	public List<CatalogItem> getMostViewedCatalogItems(Locale locale, int maxResult);
	
	
	public PublisherData getPublisherData();
	
	/**
	 * Returns the subscriber, enabled or not of the
	 * specified identity.
	 * 
	 * @param identity The identity
	 * @return A subscriber or null
	 */
	public Subscriber getSubscriber(Identity identity);
	
	public SubscriptionContext getSubscriptionContext();
	
	public void markPublisherNews();
	
	public void increaseDownloadCount(VFSLeaf leaf);

}
