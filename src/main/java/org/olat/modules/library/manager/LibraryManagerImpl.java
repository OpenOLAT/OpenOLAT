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
package org.olat.modules.library.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.events.NewIdentityCreatedEvent;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.manager.VFSMetadataDAO;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSLeafButSystemFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.modules.library.LibraryEvent;
import org.olat.modules.library.LibraryManager;
import org.olat.modules.library.LibraryModule;
import org.olat.modules.library.site.LibrarySite;
import org.olat.modules.library.ui.CatalogItem;
import org.olat.modules.sharedfolder.SharedFolderManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This is the library manager. The library contains a catalog. The
 * catalog is a hierarchical structure that contains files at its leafs. The
 * implementation is based on a shared folder resource. The resource or
 * repository entry associated with the catalog is saved in a property. There's
 * only one shared folder associated with the catalog.<P>
 * Special properties ares stored in a persisted properties at
 * system/configuration/com.frentix.olat.library.LibraryManager.properties:
 * <ul>
 * 	<li>notify.afterupload</li>
 * </ul>
 * <P>
 * Initial Date: Jun 17, 2009 <br>
 * 
 * @author gwassmann
 */
@Service("libraryManager")
public class LibraryManagerImpl implements LibraryManager, InitializingBean, GenericEventListener {
	
	private static final Logger log = Tracing.createLoggerFor(LibraryManagerImpl.class);
	
	private static final String RES_NAME = OresHelper.calculateTypeName(LibrarySite.class);
	private static final OLATResourceable IDENTITY_EVENT_CHANNEL = OresHelper.lookupType(Identity.class);

	private static final String LIBRARY_UPLOAD_FOLDER_NAME = "/library_upload";

	private static final String NO_FOLDER_INDEXING_LOCKFILE = ".noFolderIndexing";
	
	
	// Local cache of shared folder reference
	private LocalFolderImpl sharedFolder;
	private String sharedFolderPath;

	/** Upload folder */
	private LocalFolderImpl uploadFolder;
	
	private boolean autoSubscribe = false;
	
	@Autowired
	private VFSMetadataDAO metadataDao;
	@Autowired
	private LibraryModule libraryModule;
	@Autowired
	private CoordinatorManager coordinator;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private NotificationsManager notificationsManager;

	@Override
	public void afterPropertiesSet() throws Exception {
		// Instantiate handle for library upload folder, which is stored directly in
		// the bcroot folder (hence the null parent argument).
		uploadFolder = VFSManager.olatRootContainer(LIBRARY_UPLOAD_FOLDER_NAME, null);
		uploadFolder.setDefaultItemFilter(new VFSLeafButSystemFilter());
		
		coordinator.getCoordinator().getEventBus().registerFor(this, null, IDENTITY_EVENT_CHANNEL);
	}

	/**
	 * Only one shared folder repository entry can be designated as catalog.
	 * 
	 * @return The repository entry of the catalog
	 */
	@Override
	public RepositoryEntry getCatalogRepoEntry() {
		RepositoryEntry repoEntry = null;
		String libraryEntryKey = libraryModule.getLibraryEntryKey();
		if (StringHelper.isLong(libraryEntryKey)) {
			repoEntry = repositoryEntryDao.loadByKey(Long.valueOf(libraryEntryKey));
		}
		return repoEntry;
	}

	/**
	 * @return The shared folder (aka catalog) belonging to the library or NULL if
	 *         no folder is configured
	 */
	@Override
	public LocalFolderImpl getSharedFolder() {
		if(sharedFolder == null && libraryModule.getLibraryEntryKey() != null) {
			RepositoryEntry repoEntry = getCatalogRepoEntry();
			if (repoEntry != null) {
				OLATResourceable ores = repoEntry.getOlatResource();
				LocalFolderImpl loadedFolder = SharedFolderManager.getInstance().getSharedFolder(ores);	
				if(loadedFolder != null) {
					loadedFolder.setDefaultItemFilter(new VFSSystemItemFilter());
					sharedFolder = loadedFolder;
					sharedFolderPath = "/repository/" + repoEntry.getOlatResource().getResourceableId() + "/" + loadedFolder.getName();
							
				}
			}
		}
		return sharedFolder;
	}

	@Override
	public String getDirectoryPath() {
		return sharedFolderPath;
	}

	/**
	 * @param repoEntry The shared folder repository entry associated with the
	 *          catalog in the library
	 */
	@Override
	public synchronized void setCatalogRepoEntry(RepositoryEntry repoEntry) {
		updateSubscriptionContext(getCatalogRepoEntry(), repoEntry);
		
		if (repoEntry == null) {
			// Case 1) remove any existing configuration
			libraryModule.setLibraryEntryKey(null);
			sharedFolder = null;
			sharedFolderPath = null;
		} else {
			// case 2) update or create new configuration
			libraryModule.setLibraryEntryKey(repoEntry.getKey().toString());
			sharedFolder = null;
			sharedFolderPath = null;
			getSharedFolder();//update cache
		}
	}

	@Override
	public VFSLeaf getFileByUUID(String uuid) {
		if(!StringHelper.containsNonWhitespace(uuid)) return null;
		
		int nameIndex = uuid.indexOf('/');
		if(nameIndex > 0) {
			//something is the filename added to the uuid
			uuid = uuid.substring(0, nameIndex);
		}
		
		VFSMetadata metadata = vfsRepositoryService.getMetadataByUUID(uuid);
		VFSItem item = vfsRepositoryService.getItemFor(metadata);
		return item instanceof VFSLeaf ? (VFSLeaf)item : null;
	}
	
	@Override
	public CatalogItem getCatalogItemByUUID(String uuid, Locale locale) {
		if(!StringHelper.containsNonWhitespace(uuid)) return null;
		
		VFSMetadata metadata = vfsRepositoryService.getMetadataByUUID(uuid);
		VFSItem item = vfsRepositoryService.getItemFor(metadata);
		if(item instanceof VFSLeaf) {
			boolean thumbnailAvaliable = vfsRepositoryService.isThumbnailAvailable(item, metadata);
			return new CatalogItem((VFSLeaf)item, metadata, thumbnailAvaliable, locale);
		}
		return null;
	}

	/**
	 * @return The newest catalog items
	 */
	@Override
	public List<CatalogItem> getNewestCatalogItems(Locale locale, int maxResult) {
		VFSContainer container = getSharedFolder();
		List<VFSMetadata> newestData = metadataDao.getNewest(toMetadataRelativePath(container), maxResult);
		
		List<CatalogItem> items = new ArrayList<>(maxResult);
		for(VFSMetadata metadata:newestData) {
			VFSItem item = vfsRepositoryService.getItemFor(metadata);
			if(item instanceof VFSLeaf) {
				items.add(new CatalogItem((VFSLeaf)item, metadata, false, locale));
			}
		}
		return items;
	}
	
	@Override
	public List<CatalogItem> getNewCatalogItems(Date from, Locale locale) {
		VFSContainer container = getSharedFolder();
		List<VFSMetadata> newestData = metadataDao.getNewest(toMetadataRelativePath(container), from);
		
		List<CatalogItem> items = new ArrayList<>();
		for(VFSMetadata metadata:newestData) {
			VFSItem item = vfsRepositoryService.getItemFor(metadata);
			if(item instanceof VFSLeaf) {
				items.add(new CatalogItem((VFSLeaf)item, metadata, false, locale));
			}
		}
		return items;
	}

	/**
	 * @return The most viewed catalog items
	 */
	@Override
	public List<CatalogItem> getMostViewedCatalogItems(Locale locale, int maxResult) {
		VFSContainer container = getSharedFolder();
		List<VFSMetadata> mostDownloadedData = metadataDao.getMostDownloaded(toMetadataRelativePath(container), maxResult);
		
		List<CatalogItem> items = new ArrayList<>(maxResult);
		for(VFSMetadata metadata:mostDownloadedData) {
			VFSItem item = vfsRepositoryService.getItemFor(metadata);
			if(item instanceof VFSLeaf) {
				items.add(new CatalogItem((VFSLeaf)item, metadata, false, locale));
			}
		}
		return items;
	}
	
	private String toMetadataRelativePath(VFSItem item) {
		String relPath = null;
		if(item instanceof VFSContainer) {
			relPath = item.getRelPath();
		}
		
		if(relPath != null && relPath.startsWith("/")) {
			relPath = relPath.substring(1, relPath.length());
		}
		return relPath;
	}
	
	@Override
	public void increaseDownloadCount(VFSLeaf leaf) {
		if(!new VFSSystemItemFilter().accept(leaf)) return;

		vfsRepositoryService.increaseDownloadCount(leaf);
		
		Long resId = getCatalogRepoEntry().getOlatResource().getResourceableId();
		OLATResourceable libraryOres = OresHelper.createOLATResourceableInstance(LibrarySite.class, resId);
		coordinator.getCoordinator().getEventBus().fireEventToListenersOf(new LibraryEvent(LibraryEvent.DOWNLOAD), libraryOres);
	}

	@Override
	public LocalFolderImpl getUploadFolder() {
		return uploadFolder;
	}

	// a library folder is indexed twice (LibraryIndexer, FolderIndexer). prevent this with a lockFile.
	@Override
	public void lockFolderAndPreventDoubleIndexing() {
		File lockFile = new File(getSharedFolder().getBasefile(), NO_FOLDER_INDEXING_LOCKFILE);
		try {
			if(!lockFile.createNewFile()) {
				log.error("Cannot create lock file: {}", lockFile);
			}
		} catch (IOException e) {
			log.error("could not create lock-file in shared folder for library.", e);
		}
	}

	/**
	 * Remove old lock file before changing linked resource folder
	 */
	@Override
	public void removeExistingLockFile(){
		LocalFolderImpl folder = getSharedFolder();
		if(folder != null && folder.exists()) {
			File lockFile = new File(folder.getBasefile(), NO_FOLDER_INDEXING_LOCKFILE);
			FileUtils.deleteFile(lockFile);
		}
	}
	
	/**
	 * Receive the event after the creation of new identities
	 */
	@Override
	public void event(Event event) {
		if (event instanceof NewIdentityCreatedEvent && autoSubscribe) {
			NewIdentityCreatedEvent e = (NewIdentityCreatedEvent)event;
			Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(e.getIdentityId());
			subscribe(identity);
		}
	}
	
	@Override
	public SubscriptionContext getSubscriptionContext() {
		RepositoryEntry repoEntry = getCatalogRepoEntry();
		if(repoEntry == null) {
			return null;
		}
		Long key = repoEntry.getKey();
		return new SubscriptionContext(RES_NAME, key, key.toString());
	}
	
	private void updateSubscriptionContext(RepositoryEntry oldRepoEntry, RepositoryEntry newRepoEntry) {
		if(oldRepoEntry == null) return; //nothing to update

		Long oldKey = oldRepoEntry.getKey();
		SubscriptionContext oldContext = new SubscriptionContext(RES_NAME, oldKey, oldKey.toString());
		Publisher publisher = notificationsManager.getPublisher(oldContext);
		if(publisher != null) {
			if(newRepoEntry != null) {
				Long newKey = newRepoEntry.getKey();
				publisher.setResId(newKey);
				publisher.setSubidentifier(newKey.toString());
				//FIXME SR Publisher
				//				NotificationsManager.getInstance().updatePublisher(publisher);
			} else {
				notificationsManager.delete(oldContext);
			}
		}
	}
	
	@Override
	public PublisherData getPublisherData() {
		if(sharedFolder == null) {
			return null;
		}
		
		String data = sharedFolder.getRelPath();
		Long resId = getCatalogRepoEntry().getOlatResource().getResourceableId();
		String businessPath = "[LibrarySite:" + resId + "]";
		return new PublisherData(RES_NAME, data, businessPath);
	}
	
	@Override
	public Subscriber getSubscriber(Identity identity) {
		SubscriptionContext context = getSubscriptionContext();
		if(context == null) return null;
		Publisher publisher = notificationsManager.getPublisher(context);
		if(publisher == null) {
			return null;
		}
		return notificationsManager.getSubscriber(identity, publisher);
	}
	
	public void subscribe(Identity identity) {
		PublisherData data = getPublisherData();
		SubscriptionContext context = getSubscriptionContext();
		if(context != null) {
			notificationsManager.subscribe(identity, context, data);
		}
	}
	
	public void unsubscribe(Identity identity) {
		SubscriptionContext context = getSubscriptionContext();
		if(context != null) {
			notificationsManager.unsubscribe(identity, context);
		}
	}
	
	@Override
	public void markPublisherNews() {
		SubscriptionContext context = getSubscriptionContext();
		if(context != null) {
			//FIXME SR Publisher
//			NotificationsManager.getInstance().markPublisherNews(context, null);
		}
	}
	
	public class FileItem {
		private final VFSLeaf file;
		private final int download;
		private final long lastModifiedDate;
		private final String uuid;
		
		public FileItem(VFSLeaf file) {
			this.file = file;
			VFSMetadata metaInfo = file.getMetaInfo();
			download = metaInfo.getDownloadCount();
			lastModifiedDate = file.getLastModified();
			uuid = metaInfo.getUuid();
		}
		
		public VFSLeaf getFile() {
			return file;
		}

		public String getUuid() {
			return uuid;
		}

		public int getDownloadCount() {
			return download;
		}
		
		public long getLastModifiedDate() {
			return lastModifiedDate;
		}
		
		public boolean exists() {
			return file.exists();
		}
	}
	
	public class DescendingLastModifiedItemComparator implements Comparator<FileItem> {
		@Override
		public int compare(FileItem o1, FileItem o2) {
			long o1mod = o1.getLastModifiedDate();
			long o2mod = o2.getLastModifiedDate();
			return o1mod > o2mod ? -1 : 1;
		}
	}
	
	public class DescendingDownloadCountItemComparator implements Comparator<FileItem> {
		@Override
		public int compare(FileItem o1, FileItem o2) {
			int d1 = o1.getDownloadCount();
			int d2 = o2.getDownloadCount();
			return d1 > d2 ? -1 : 1;
		}
	}
}
