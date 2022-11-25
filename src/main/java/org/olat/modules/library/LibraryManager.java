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

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.library.model.CatalogItem;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 29 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface LibraryManager {
	
	public static final String URL_PREFIX = "[LibrarySite:0][path=";
	
	public String getDirectoryPath();
	
	public VFSContainer getSharedFolder();

	public OLATResourceable getLibraryResourceable();
	
	public void removeExistingLockFile();
	
	public void lockFolderAndPreventDoubleIndexing();
	
	public VFSContainer getUploadFolder();
	
	public RepositoryEntry getCatalogRepoEntry();
	
	public void setCatalogRepoEntry(RepositoryEntry repoEntry);
	
	public VFSLeaf getFileByUUID(String uuid);
	
	public CatalogItem getCatalogItemByUUID(String uuid, IdentityRef identity);

	public List<CatalogItem> getCatalogItems(VFSMetadata parentMetadata, IdentityRef identity);
	
	public CatalogItem getCatalogItemsByUrl(String businessPath, IdentityRef identity);
	
	public List<CatalogItem> getMostRatedCatalogItems(int numOfItems, IdentityRef identity);
	
	public List<CatalogItem> getNewCatalogItems(Date from, IdentityRef identity);
	
	public List<CatalogItem> getNewestCatalogItems(int maxResult, IdentityRef identity);
	
	public List<CatalogItem> getMostViewedCatalogItems(int maxResult, IdentityRef identity);
	
	
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
