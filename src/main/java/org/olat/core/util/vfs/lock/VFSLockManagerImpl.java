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
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.webdav.manager.VFSResource;
import org.olat.core.commons.services.webdav.servlets.WebResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("vfsLockManager")
public class VFSLockManagerImpl implements VFSLockManager {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private VFSRepositoryService fileService;

    /**
     * Repository of the locks put on single resources.
     * <p>
     * Key : path <br>
     * Value : LockInfo
     */
    private Map<File,LockInfo> fileLocks = new ConcurrentHashMap<>();

    /**
     * Repository of the lock-null resources.
     * <p>
     * Key : path of the collection containing the lock-null resource<br>
     * Value : Vector of lock-null resource which are members of the
     * collection. Each element of the Vector is the path associated with
     * the lock-null resource.
     */
    private Map<String,Vector<String>> lockNullResources = new ConcurrentHashMap<>();


    /**
     * Vector of the heritable locks.
     * <p>
     * Key : path <br>
     * Value : LockInfo
     */
    private Vector<LockInfo> collectionLocks = new Vector<>();
    
    @Autowired
    private VFSRepositoryService vfsRepositoryService;

	@Override
	public boolean isLocked(VFSItem item, VFSLockApplicationType type) {
		return isLocked(item, null, type);
	}

	@Override
	public boolean isLocked(VFSItem item, VFSMetadata loadedInfo, VFSLockApplicationType type) {
		File file = extractFile(item);
    	if(file != null && fileLocks.containsKey(file)) {
    		LockInfo lock = fileLocks.get(file);
    		if (lock != null && lock.hasExpired()) {
                //LOCK resourceLocks.remove(lock.getWebPath());
                fileLocks.remove(file);
            } else {
            	return true;
            }
    	}

		Identity lockedBy = getMetaLockedBy(item, loadedInfo);
		return (lockedBy != null);
	}
	
	@Override
	public boolean isLockedForMe(VFSItem item, Identity me, Roles roles, VFSLockApplicationType type) {
		return isLockedForMe(item, null, me, roles, type);
	}

	/**
	 * @return true If the lock owner is someone else or if it's a WebDAV lock
	 */
	@Override
	public boolean isLockedForMe(VFSItem item, VFSMetadata loadedInfo, Identity me, Roles roles, VFSLockApplicationType type) {//TODO metadata need the name/instance of the collaboration app.
		File file = extractFile(item);
    	if(file != null && fileLocks.containsKey(file)) {
    		LockInfo lock = fileLocks.get(file);
    		if(lock == null) {
    			return false;
    		} else if (lock != null && lock.hasExpired()) {
    			//LOCK resourceLocks.remove(lock.getWebPath());
                fileLocks.remove(file);
            } else if(lock.isCollaborationLock() && type == VFSLockApplicationType.collaboration) {
            	return false;
            } else {
        		Long lockedBy = lock.getLockedBy();
            	return (lockedBy != null && !lockedBy.equals(me.getKey())) || lock.isWebDAVLock() || lock.isCollaborationLock();
            }
    	}

		Identity lockedBy = getMetaLockedBy(item, loadedInfo);
		return (lockedBy != null && !lockedBy.getKey().equals(me.getKey()));
	}

	private Identity getMetaLockedBy(VFSItem item, VFSMetadata loadedInfo) {
		VFSMetadata info = loadedInfo == null ? getMetaInfo(item) : loadedInfo;
		Identity lockedBy = null;
		if(info != null) {
			if(!info.isLocked()) {
				return null;
			}
			lockedBy = info.getLockedBy();
		}
		return lockedBy;
	}
	
	private VFSMetadata getMetaInfo(VFSItem item) {
		VFSMetadata info = null;
		if (item != null && item.canMeta() == VFSConstants.YES) {
			info = fileService.getMetadataFor(item);
		}
		return info;
	}
	
    @Override
	public boolean lock(VFSItem item, Identity identity, Roles roles, VFSLockApplicationType type) {
		if (item != null && item.canMeta() == VFSConstants.YES) {
			if(type == VFSLockApplicationType.vfs) {
				VFSMetadata info = item.getMetaInfo();
				info.setLockedBy(identity);
				info.setLockedDate(new Date());
				info.setLocked(true);
				vfsRepositoryService.updateMetadata(info);
				dbInstance.commit();
			}
			
			File file = extractFile(item);
			if(file != null && fileLocks.containsKey(file)) {
				LockInfo lock = fileLocks.get(file);
				if(lock != null) {
					if(type == VFSLockApplicationType.collaboration) {
						lock.setCollaborationLock(true);
					} else if(type == VFSLockApplicationType.vfs) {
						lock.setVfsLock(true);
					}
				}
			}
			
			return true;
		}
		return false;
	}

    /**
     * Unlock the VFS lock only
     * 
     * 
     */
	@Override
	public boolean unlock(VFSItem item, Identity identity, Roles roles, VFSLockApplicationType type) {
		if (item != null && item.canMeta() == VFSConstants.YES) {
			VFSMetadata info = item.getMetaInfo();
			if(info == null) return false;
			
			info.setLockedBy(null);
			info.setLockedDate(null);
			info.setLocked(false);
			vfsRepositoryService.updateMetadata(info);
			dbInstance.commit();
			
			boolean unlocked = false;
			File file = extractFile(item);
			if(file != null && fileLocks.containsKey(file)) {
				LockInfo lock = fileLocks.get(file);
				if(lock.isWebDAVLock()) {
					lock.setVfsLock(false);
					lock.setCollaborationLock(false);
				} else if(lock.isCollaborationLock()) {
					lock.setVfsLock(false);
					lock.setWebDAVLock(false);
				} else {
					if(lock.getWebPath() != null) {
						//LOCK resourceLocks.remove(lock.getWebPath());
					}
					fileLocks.remove(file);
					unlocked = true;
				}
			} else {
				unlocked = true;
			}
			return unlocked;
		}
		return false;
	}

	public Iterator<LockInfo> getResourceLocks() {
		return fileLocks.values().iterator();
	}
	
    public LockInfo getResourceLock(WebResource resource) {
    	/* LOCK if(resourceLocks.containsKey(resource.getPath())) {
    		return resourceLocks.get(resource.getPath());
    	}*/
    	
    	File file = extractFile(resource);
    	if(file != null && fileLocks.containsKey(file)) {
    		return fileLocks.get(file);
    	}
    	return null;
	}
    
    /**
     * 
     * Return a lock info base on the VFS lock
     * 
     * @param resource
     * @return
     */
    public LockInfo getVFSLock(WebResource resource) {
    	VFSItem item = extractItem(resource);
    	VFSMetadata info = getMetaInfo(item);
    	if(info != null && info.isLocked()) {
        	File file = extractFile(item);
        	LockInfo lock = null;
        	if(fileLocks.containsKey(file)) {
        		lock = fileLocks.get(file);
        		if(lock != null) {
        			lock.setVfsLock(true);
        		}        		
        	}
        	if(lock == null){
	    		lock = new LockInfo(info.getLockedBy(), false, true, false);
	    		lock.setWebResource(resource);
	    		lock.setCreationDate(info.getLockedDate());
	    		lock.setOwner(Settings.getServerContextPathURI() + "/Identity/" + info.getLockedBy().getKey());
	    		lock.setDepth(1);
	    		lock.addToken(generateLockToken(lock, info.getLockedBy()));
	    		fileLocks.put(file, lock);
        	}
    		return lock;
    	}
    	return null;
	}

	@Override
    public LockInfo getLock(VFSItem item) {
    	File file = extractFile(item);
    	if(file != null && fileLocks.containsKey(file)) {
    		LockInfo lock = fileLocks.get(file);
    		if(lock != null) {
    			return lock;
    		}
    	}
    	
    	VFSMetadata info = getMetaInfo(item);
    	if(info != null && info.isLocked()) {
    		LockInfo lock = new LockInfo(info.getLockedBy(), false, true, false);
    		lock.setCreationDate(info.getLockedDate());
    		lock.setOwner(Settings.getServerContextPathURI() + "/Identity/" + info.getLockedBy().getKey());
    		lock.setDepth(1);
    		lock.addToken(generateLockToken(lock, info.getLockedBy()));
    		fileLocks.put(file, lock);
    		return lock;
    	}
    	return null;
	}
	
	public LockInfo getCollaborationLock() {
		return null;
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
		//LOCK resourceLocks.put(resource.getPath(), lock);
		File file = extractFile(resource);
		if(file != null) {
			fileLocks.put(file, lock);
		}
	}
    
    private VFSItem extractItem(WebResource resource) {
    	if(resource instanceof VFSResource) {
			VFSResource vResource = (VFSResource)resource;
			return vResource.getItem();
		}
    	return null;
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

	public Vector<String> getLockNullResource(WebResource resource) {
		return lockNullResources.get(resource.getPath());
	}
	
	public Vector<String> removeLockNullResource(WebResource resource) {
		return lockNullResources.remove(resource.getPath());
	}

	public void putLockNullResource(String path, Vector<String> resources) {
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
    	String path = resource.getPath();
    	
    	//check if someone else as not set a lock on the resource
    	if(resource instanceof VFSResource) {
    		VFSResource vfsResource = (VFSResource)resource;
    		Identity lockedBy = getMetaLockedBy(vfsResource.getItem(), null);
    		if(lockedBy != null && !lockedBy.getKey().equals(identity.getKey())) {
    			return true;
    		}
    	}
    	
    	File file = extractFile(resource);
    	if(file == null) {
    		return false;//lock only file
    	}
    	
        LockInfo lock = fileLocks.get(file);
        if (lock != null && lock.hasExpired()) {
            fileLocks.remove(file);
        } else if (lock != null) {
            // At least one of the tokens of the locks must have been given
        	Iterator<String> tokenList = lock.tokens();
            boolean tokenMatch = false;
            while (tokenList.hasNext()) {
                String token = tokenList.next();
                if (ifHeader.indexOf(token) != -1) {
                    tokenMatch = true;
                    break;
                }
            }
            if (!tokenMatch)
                return true;

        }

        // Checking inheritable collection locks

        Enumeration<LockInfo> collectionLocksList = collectionLocks.elements();
        while (collectionLocksList.hasMoreElements()) {
            lock = collectionLocksList.nextElement();
            if (lock.hasExpired()) {
                collectionLocks.removeElement(lock);
            } else if (path.startsWith(lock.getWebPath())) {

            	Iterator<String> tokenList = lock.tokens();
                boolean tokenMatch = false;
                while (tokenList.hasNext()) {
                    String token = tokenList.next();
                    if (ifHeader.indexOf(token) != -1) {
                        tokenMatch = true;
                        break;
                    }
                }
                if (!tokenMatch)
                    return true;

            }
        }

        return false;
    }
}
