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
package org.olat.core.util.vfs.lock;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.manager.VFSMetadataDAO;
import org.olat.core.commons.services.webdav.manager.VFSResource;
import org.olat.core.commons.services.webdav.servlets.WebResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("vfsLockManager")
public class VFSLockManagerImpl implements VFSLockManager {
	
	private static final Logger log = Tracing.createLoggerFor(VFSLockManagerImpl.class);
	
	//one year is enough for a long loc
    private static final long vfsExpireAt = (System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000));
    private static final long collaborationExpireAt = (System.currentTimeMillis() + (24 * 60 * 60 * 1000));
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private FolderModule folderModule;
	@Autowired
	private VFSMetadataDAO metadataDao;
    @Autowired
    private VFSRepositoryService vfsRepositoryService;

    /**
     * Repository of the locks put on single resources.
     * <p>
     * Key : path <br>
     * Value : LockInfo
     */
    private CacheWrapper<File,LockInfo> fileLocks;

    /**
     * Repository of the lock-null resources.
     * <p>
     * Key : path of the collection containing the lock-null resource<br>
     * Value : Vector of lock-null resource which are members of the
     * collection. Each element of the Vector is the path associated with
     * the lock-null resource.
     */
    private CacheWrapper<String,List<String>> lockNullResources;


    /**
     * Vector of the heritable locks.
     * <p>
     * Key : path <br>
     * Value : LockInfo
     */
    private List<LockInfo> collectionLocks = new Vector<>();
    
    
    @Autowired
    public VFSLockManagerImpl(CoordinatorManager coordinator) {
    	lockNullResources = coordinator.getCoordinator().getCacher().getCache(VFSLockManager.class.getSimpleName(), "lock-nulls");
    	fileLocks = coordinator.getCoordinator().getCacher().getCache(VFSLockManager.class.getSimpleName(), "file-locks");
    }

	@Override
	public boolean isLocked(VFSItem item, VFSLockApplicationType type, String appName) {
		return isLocked(item, null, type, appName);
	}

	@Override
	public boolean isLocked(VFSItem item, VFSMetadata metadata, VFSLockApplicationType type, String appName) {
		LockInfo lock = getLockInfo(item, metadata);
		return lock != null && lock.isLocked();
	}
	
	@Override
	public boolean isLockedForMe(VFSItem item, Identity me, VFSLockApplicationType type, String appName) {
		return isLockedForMe(item, null, me, type, appName);
	}

	/**
	 * @return true If the lock owner is someone else or if it's a WebDAV lock
	 */
	@Override
	public boolean isLockedForMe(VFSItem item, VFSMetadata loadedInfo, Identity me, VFSLockApplicationType type, String appName) {
		LockInfo lock = getLockInfo(item, loadedInfo);
		
		boolean locked;
		if(lock == null) {
			locked = false;
		} else if(lock.isLocked()) {
			locked = isLockedForMe(lock, me, type, appName);
    	} else {
    		locked = false;
    	}
		return locked;
	}
	
	private boolean isLockedForMe(LockInfo lock, Identity me, VFSLockApplicationType type, String appName) {
		boolean locked;
		if(me != null && me.getKey().equals(lock.getLockedBy())) {
			if(lock.isWebDAVLock()) {
				locked = (type == VFSLockApplicationType.vfs || type == VFSLockApplicationType.collaboration);
			} else if(lock.isCollaborationLock()) {
				locked = (type == VFSLockApplicationType.vfs || type == VFSLockApplicationType.webdav
						|| (type == VFSLockApplicationType.collaboration && !lock.getAppName().equals(appName)));
			} else if(lock.isVfsLock()) {
				locked = (type == VFSLockApplicationType.webdav || type == VFSLockApplicationType.collaboration);
			} else {
				locked = false;
			}
		} else if(lock.isVfsLock() || lock.isWebDAVLock()) {
			locked = true;// I can only if me is the user who locks
		} else if(lock.isCollaborationLock() && type == VFSLockApplicationType.collaboration) {
			locked = lock.getAppName() != null && !lock.getAppName().equals(appName);
		} else {
			locked = lock.isLocked();
		}
		return locked;
	}
	
	/**
     * Check to see if a resource is currently write locked.
     *
     * @param path Path of the resource
     * @param ifHeader "If" HTTP header which was included in the request
     * @return boolean true if the resource is locked (and no appropriate
     * lock token has been found for at least one of the non-shared locks which
     * are present on the resource).
     */
    public boolean isLocked(WebResource resource, String ifHeader, Identity identity) {
        // Checking resource locks
    	
    	File file = extractFile(resource);
    	if(file == null) {
    		return false;//lock only file
    	}
    	
    	//check if someone else as not set a lock on the resource
    	LockInfo lock = getLock(resource);
    	if(lock == null) {
    		return false;
    	}
    	if(lock.isCollaborationLock()) {
    		return true;
    	}
    	if(lock.isVfsLock() && lock.getLockedBy() != null && !lock.getLockedBy().equals(identity.getKey())) {
    		return true;
    	}
        
        // At least one of the tokens of the locks must have been given
        boolean tokenMatch = false;
        for (Iterator<String> tokenList = lock.tokens(); tokenList.hasNext(); ) {
            String token = tokenList.next();
            if (ifHeader.indexOf(token) != -1) {
                tokenMatch = true;
                break;
            }
        }
        if (!tokenMatch) {
            return true;
        }
  
    	String path = resource.getPath();
        // Checking inheritable collection locks
        for (Iterator<LockInfo> collectionLocksList = collectionLocks.iterator(); collectionLocksList.hasNext(); ) {
            lock = collectionLocksList.next();
            if (lock.hasExpired()) {
            	collectionLocksList.remove();
            } else if (path.startsWith(lock.getWebPath())) {

            	Iterator<String> tokenList = lock.tokens();
                boolean tokenCollectionMatch = false;
                while (tokenList.hasNext()) {
                    String token = tokenList.next();
                    if (ifHeader.indexOf(token) != -1) {
                    	tokenCollectionMatch = true;
                        break;
                    }
                }
                if (!tokenCollectionMatch) {
                    return true;
                }
            }
        }

        return false;
    }

	@Override
    public LockInfo getLock(VFSItem item) {
    	return getLockInfo(item, null);
	}
	
    public LockInfo getLock(WebResource resource) {
    	LockInfo lockInfo = null;
    	if(resource instanceof VFSResource) {
    		VFSResource vfsResource = (VFSResource)resource;
    		if(vfsResource.getItem() != null) {
    			lockInfo = getLockInfo(vfsResource.getItem(), null);
    		}
    	}
    	if(lockInfo != null) {
    		lockInfo.setWebResource(resource);
    	}
    	return lockInfo;
	}
	
	/**
	 * The method doesn't reload the metadata if they are specified.
	 * 
	 * @param item The file
	 * @param metadata
	 * @return The lock information or null
	 */
	private LockInfo getLockInfo(VFSItem item, VFSMetadata metadata) {
		final File file = extractFile(item);
		if(file == null) {
			log.debug("Lock only real file: {}", item);
			return null;// we only lock real files
		}
		
		LockInfo lock = fileLocks.get(file);
		if(lock == null && (metadata == null || metadata.isLocked())) {
			lock = fileLocks.computeIfAbsent(file, f -> {
				VFSMetadata theMetadata;
				if(metadata == null) {
					theMetadata = vfsRepositoryService.getMetadataFor(file);
				} else {
					theMetadata = metadata;
				}
				if(theMetadata != null && theMetadata.isLocked()) {
					LockInfo lockInfo = new LockInfo(theMetadata.getLockedBy(), VFSLockApplicationType.vfs, null);
					lockInfo.setCreationDate(theMetadata.getLockedDate());
					lockInfo.setOwner(Settings.getServerContextPathURI() + "/Identity/" + theMetadata.getLockedBy().getKey());
					lockInfo.setDepth(1);
					lockInfo.addToken(generateLockToken(lockInfo, theMetadata.getLockedBy()));
					return lockInfo;
				}
				return null;
			});
		}
		if(lock != null && lock.hasExpired()) {
			if(lock.isVfsLock()) {
				lock.setWebDAVLock(false);
				lock.setCollaborationLock(false);
				lock.setExpiresAt(0l);
				lock.clearTokens();
			} else {
				log.debug("Not VFS Lock has expired: {}", item);
				fileLocks.remove(file);
            	lock = null;
			}
        }
		return lock;
	}
	
	/**
	 * Determine if the file can be locked or not. It's almost the same as the canMeta()
	 * implementation but some extra permission for specific WebDAV help files used by
	 * MacOS, Word...
	 * 
	 * @param item The file
	 * @param type The type of application which want the lock
	 * @return true if it can locked
	 */
	public final boolean canLock(VFSItem item, VFSLockApplicationType type) {
		if (item == null) return false;
		
		if(item.canMeta() == VFSConstants.YES) {
			return true;
		}
		
		if(type == VFSLockApplicationType.webdav) {
			File file = extractFile(item);
			if(file != null) {
				Path bFile = ((LocalImpl)item).getBasefile().toPath();
				if(bFile.startsWith(FolderConfig.getCanonicalRootPath())) {
					String filename = item.getName();
					return filename.startsWith(".~") || (filename.startsWith("._") && !filename.startsWith("._oo_"));
				}
			}
		}
		return false;
	}
	
    @Override
	public LockResult lock(VFSItem item, Identity identity, VFSLockApplicationType type, String appName) {
		if (!canLock(item, type)) {
			return LockResult.LOCK_FAILED;
		}

		final File file = extractFile(item);	
		final TokenWrapper generatedToken = new TokenWrapper();
		LockInfo lockInfo = fileLocks.computeIfAbsent(file, f -> {
			String relativePath = getMetadataRelativePath(f);
			VFSMetadata metadata = metadataDao.getMetadata(relativePath, f.getName(), f.isDirectory());
			if(metadata != null && metadata.isLocked()) {
				LockInfo mLockInfo = new LockInfo(metadata.getLockedBy(), VFSLockApplicationType.vfs, null);
				mLockInfo.setCreationDate(metadata.getLockedDate());
				mLockInfo.setOwner(Settings.getServerContextPathURI() + "/Identity/" + metadata.getLockedBy().getKey());
				mLockInfo.setDepth(1);
				mLockInfo.setExpiresAt(vfsExpireAt);
				String token = generateLockToken(mLockInfo, metadata.getLockedBy());
				generatedToken.setToken(token);
				mLockInfo.addToken(token);
				return mLockInfo;
			}
			
			LockInfo loc = new LockInfo(identity, type, appName);
			// WebDAV make this alone
			if(type == VFSLockApplicationType.vfs) {
				if(metadata == null) {
					metadata = vfsRepositoryService.getMetadataFor(file);
				}
				metadata.setLockedBy(identity);
				metadata.setLockedDate(new Date());
				metadata.setLocked(true);
				vfsRepositoryService.updateMetadata(metadata);
				dbInstance.commit();
			}
			if(type == VFSLockApplicationType.vfs || type == VFSLockApplicationType.collaboration) {
				loc.setCreationDate(new Date());
				loc.setOwner(Settings.getServerContextPathURI() + "/Identity/" + loc.getLockedBy());
				loc.setDepth(1);
				if(type == VFSLockApplicationType.vfs) {
					loc.setExpiresAt(vfsExpireAt);
				} else if (type == VFSLockApplicationType.collaboration) {
					loc.setExpiresAt(collaborationExpireAt);
				}
				String token = generateLockToken(loc, identity);
				generatedToken.setToken(token);
				loc.addToken(token);
			}
			return loc;
		});
		
		boolean lockAcquired;
		if(lockInfo == null) {
			lockAcquired = false;
		} else if(lockInfo.isLocked()) {
			lockAcquired = !isLockedForMe(lockInfo, identity, type, appName);
			// add a new token 
			if(lockAcquired && VFSLockApplicationType.collaboration == type && generatedToken.getToken() == null) {
				String token = generateLockToken(lockInfo, identity);
				generatedToken.setToken(token);
				lockInfo.addToken(token);
			}
			
		} else {
			lockAcquired = false;
		}
		return new LockResult(lockAcquired, lockInfo, type, generatedToken.getToken());
	}
    
	/**
	 * The relative path contains /bcroot/ saved in the metadata field "Relative path".
	 * For a file ../bcroot/repository/432687436/_sharedfolder_/Image.jp will the emthod
	 * return repository/432687436/_sharedfolder_/
	 * 
	 * @param file The file
	 * @return A value compatible with the relative path saved in metadata to search it.
	 */
	private String getMetadataRelativePath(File file) {
		return folderModule.getCanonicalRootPath().relativize(file.getParentFile().toPath()).toString();
	}
    
    public LockResult lock(WebResource resource, Identity identity) {
    	if(resource instanceof VFSResource) {
    		VFSResource vfsResource = (VFSResource)resource;
    		return lock(vfsResource.getItem(), identity, VFSLockApplicationType.webdav, null);
    	}
    	return LockResult.LOCK_FAILED;
    }

    /**
     * Unlock the VFS lock only
     * 
     * 
     */
	@Override
	public boolean unlock(VFSItem item, VFSLockApplicationType type) {
		if (item != null && item.canMeta() == VFSConstants.YES) {
			VFSMetadata info = item.getMetaInfo();
			if(info == null) return false;
			return unlock(item, info, type, null);
		}
		return false;
	}
	
	@Override
	public boolean unlock(VFSItem item, LockResult result) {
		if (item != null && item.canMeta() == VFSConstants.YES) {
			VFSMetadata info = item.getMetaInfo();
			if(info == null) return false;
			return unlock(item, info, result.getType(), result.getToken());
		}
		return false;
	}
	
	private boolean unlock(VFSItem item, VFSMetadata info, VFSLockApplicationType type, String token) {
		if(type == VFSLockApplicationType.vfs) {
			info.setLockedBy(null);
			info.setLockedDate(null);
			info.setLocked(false);
			vfsRepositoryService.updateMetadata(info);
			dbInstance.commit();
		}
		
		boolean unlocked = false;
		File file = extractFile(item);
		if(file != null) {
			LockInfo lock = fileLocks.get(file);
			if(lock == null) {
				unlocked = true;
			} else {
				if(token != null) {
					lock.removeToken(token);
				}
				
				if(lock.isWebDAVLock()) {// WebDAV make it alone
					lock.setVfsLock(false);
					lock.setCollaborationLock(false);
				} else if(lock.isCollaborationLock()) {
					lock.setVfsLock(false);
					lock.setWebDAVLock(false);
					if(lock.getTokensSize() == 0) {
						fileLocks.remove(file);
						unlocked = true;
					}
				} else {
					fileLocks.remove(file);
					unlocked = true;
				}
			}
		} else {
			unlocked = true;
		}
		return unlocked;
	}

	public List<LockInfo> getResourceLocks() {
		int cacheSize = fileLocks.size();
		List<LockInfo> infos = new ArrayList<>(cacheSize);
		for(File file:fileLocks.getKeys()) {
			LockInfo fileLock = fileLocks.get(file);
			if(fileLock != null) {
				infos.add(fileLock);
			}
		}
		return infos;
	}
	


	/**
	 * I replace the method from Tomcat with a classic UUID. The method
	 * from tTomcat based on a MD5 hash of severals different informations
	 * + time in milliseconds seems to disturb the WebDAV client of Mac OS X 10.9
	 */
	@Override
    public String generateLockToken(LockInfo lock, Identity identity) {
    	return UUID.randomUUID().toString();
    }
    
    public void putResourceLock(WebResource resource, LockInfo lock) {
		File file = extractFile(resource);
		if(file != null) {
			fileLocks.put(file, lock);
		}
	}
    
    private File extractFile(WebResource resource) {
    	if(resource instanceof VFSResource) {
			VFSResource vResource = (VFSResource)resource;
			return extractFile(vResource.getItem());
		}
    	return null;
    }
    
    private File extractFile(VFSItem item) {
    	if(item instanceof LocalImpl) {
    		LocalImpl resource = (LocalImpl)item;
			return resource.getBasefile();
		}
    	return null;
    }
    
    public void removeResourceLock(WebResource resource) {
    	//LOCK
		File file = extractFile(resource);
		if(file != null) {
	    	LockInfo lock = fileLocks.get(file);
	    	if(lock != null) {
	    		if(lock.isVfsLock()) {
	    			lock.setWebDAVLock(false);
	    			lock.setCollaborationLock(false);
	    		} else {
					fileLocks.remove(file);
	    		}
	    	}
		}
	}

	public List<String> getLockNullResource(WebResource resource) {
		return lockNullResources.get(resource.getPath());
	}
	
	public List<String> removeLockNullResource(WebResource resource) {
		return lockNullResources.remove(resource.getPath());
	}

	public void putLockNullResource(String path, List<String> resources) {
		lockNullResources.put(path, resources);
	}

	public Iterator<LockInfo> getCollectionLocks() {
		return collectionLocks.iterator();
	}

	public void addCollectionLock(LockInfo collectionLock) {
		collectionLocks.add(collectionLock);
	}
	
	public void removeCollectionLock(LockInfo collectionLock) {
		collectionLocks.remove(collectionLock);
	}
	
	private static class TokenWrapper {
		
		private String token;

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}
	}
}
