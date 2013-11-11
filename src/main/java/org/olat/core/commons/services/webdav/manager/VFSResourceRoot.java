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
package org.olat.core.commons.services.webdav.manager;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.io.IOUtils;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.MetaInfoHelper;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.commons.services.webdav.servlets.WebResource;
import org.olat.core.commons.services.webdav.servlets.WebResourceRoot;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.UserSession;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.SubscriptionContext;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.version.Versionable;
import org.olat.core.util.vfs.version.VersionsManager;


/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSResourceRoot implements WebResourceRoot  {
	
	private static final OLog log = Tracing.createLoggerFor(VFSResourceRoot.class);
	
	private final Identity identity;
	private final VFSContainer base;
	private UserSession userSession;
	
	public VFSResourceRoot(Identity identity, VFSContainer root) {
		this.identity = identity;
		this.base = root;
	}
	
	public Identity getIdentity() {
		return identity;
	}

	public VFSContainer getRoot() {
		return base;
	}
	
	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}
	
	@Override
	public boolean canWrite(String name) {
		// resolve item if it already exists
		VFSItem item = resolveFile(name);
		if (item == null) {
			// try to resolve parent in case the item does not yet exist
			int lastSlash = name.lastIndexOf("/");
			if (lastSlash > 0) {
				String containerName = name.substring(0, lastSlash);
				item = resolveFile(containerName);
			}
		}
		if (item == null) {
			return false;
		}
		
		VFSStatus status;
		if (item instanceof VFSContainer) {
			status = item.canWrite();
		} else {
			// read/write is not defined on item level, only on directory level
			status = item.getParentContainer().canWrite();
		}
		return VFSConstants.YES.equals(status);
	}
	
	@Override
	public boolean canRename(String name) {
		VFSItem item = resolveFile(name);
		if (item != null && VFSConstants.YES.equals(item.canRename())) {
			return !MetaInfoHelper.isLocked(item, userSession);
		} else {
			return false;
		}
	}	

	@Override
	public boolean canDelete(String path) {
		VFSItem item = resolveFile(path);
		if (item != null && VFSConstants.YES.equals(item.canDelete())) {
			return !MetaInfoHelper.isLocked(item, userSession);
		} else {
			return false;
		}
	}

	@Override
	public WebResource getResource(String path) {
		VFSItem file = resolveFile(path);
		if(file == null) {
			return EmptyWebResource.EMPTY_WEB_RESOURCE;
		}
		return new VFSResource(file);
	}

	@Override
	public Collection<VFSItem> list(String path) {
		VFSItem file = resolveFile(path);
		if(file instanceof VFSContainer) {
			VFSContainer container = (VFSContainer)file;
			List<VFSItem> children = container.getItems();
			return children;
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public boolean mkdir(String path) {
		//remove trailing /
		if(path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		
		int lastSlash = path.lastIndexOf('/');
		if (lastSlash == -1) return false;
		
		String parentPath = path.substring(0, lastSlash);
		VFSItem parentItem = resolveFile(parentPath);
		if (parentItem instanceof VFSLeaf) {
			return false;
		} else if (parentItem instanceof VFSContainer) {
			String name = path.substring(lastSlash + 1);
			VFSContainer folder = (VFSContainer)parentItem;
			VFSContainer dir = folder.createChildContainer(name);
			return dir != null && dir.exists();
		}
		return false;
	}

	@Override
	public boolean write(String path, InputStream is, boolean overwrite, WebResource movedFrom) {
		VFSLeaf childLeaf;
		VFSItem file = resolveFile(path);
		if (file instanceof VFSLeaf) {
			if(overwrite) {
				//overwrite the file
				childLeaf = (VFSLeaf)file;
				
				//versioning
				if(childLeaf instanceof Versionable && ((Versionable)childLeaf).getVersions().isVersioned()) {
					if(childLeaf.getSize() == 0) {
						VersionsManager.getInstance().createVersionsFor(childLeaf, true);
					} else {
						VersionsManager.getInstance().addToRevisions((Versionable)childLeaf, identity, "");
					}
				}
			} else {
				return false;
			}
		} else if (file instanceof VFSContainer) {
			return false;
		} else {
			//create a new file
			int lastSlash = path.lastIndexOf('/');
			if (lastSlash == -1) return false;
			String parentPath = path.substring(0, lastSlash);
			VFSItem parentItem = resolveFile(parentPath);
			if(parentItem instanceof VFSContainer) {
				VFSContainer folder = (VFSContainer)parentItem;
				String name = path.substring(lastSlash + 1);
				childLeaf = folder.createChildLeaf(name);
			} else {
				return false;
			}
		}
		
		if(childLeaf == null) {
			return false;
		}
		
		try {
			byte[] content = IOUtils.toByteArray(is);
			ByteArrayInputStream in = new ByteArrayInputStream(content);
			FileUtils.copy(in, childLeaf.getOutputStream(false));
		} catch (IOException e) {
			log.error("", e);
			return false;
		}

		VFSContainer folder = childLeaf.getParentContainer();
		VFSSecurityCallback callback = folder.getLocalSecurityCallback();
		if(callback != null && callback.getSubscriptionContext() != null) {
			SubscriptionContext subContext = callback.getSubscriptionContext();
			NotificationsManager.getInstance().markPublisherNews(subContext, null, true);
		}
		
		if(childLeaf instanceof MetaTagged && identity != null) {
			MetaInfo infos = ((MetaTagged)childLeaf).getMetaInfo();
			if(infos != null && infos.getAuthorIdentity() == null) {
				infos.setAuthor(identity);
				infos.clearThumbnails();
				infos.write();
			}
		}
		
		if(movedFrom instanceof VFSResource) {
			VFSResource vfsResource = (VFSResource)movedFrom;
			if(vfsResource.getItem() instanceof Versionable
					&& ((Versionable)vfsResource.getItem()).getVersions().isVersioned()) {
				VFSLeaf currentVersion = (VFSLeaf)vfsResource.getItem();
				VersionsManager.getInstance().move(currentVersion, childLeaf, identity);
			}
		}
		
		return true;
	}
	
	@Override
	public boolean delete(WebResource resource) {
		boolean deleted = false;
		if(resource instanceof VFSResource) {
			VFSResource vfsResource = (VFSResource)resource;
			VFSItem item = vfsResource.getItem();
			if (item != null && VFSConstants.YES.equals(item.canDelete())
					&& !MetaInfoHelper.isLocked(item, userSession)) {
				VFSStatus status = item.delete();
				deleted = (status == VFSConstants.YES || status == VFSConstants.SUCCESS);
			}
		}
		return deleted;
	}

	/**
	 * Resolve a file relative to this base.
	 * Make sure, paths are relative
	 */
	private VFSItem resolveFile(String name) {
		if (name == null) name = "";
		if (name.length() > 0 && name.charAt(0) == '/') name = name.substring(1);
		return base.resolve(name);
	}
}
