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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderLicenseHandler;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.webdav.servlets.WebResource;
import org.olat.core.commons.services.webdav.servlets.WebResourceRoot;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaExceededException;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.meta.MetaInfo;
import org.olat.core.util.vfs.version.Versionable;
import org.olat.core.util.vfs.version.VersionsManager;


/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VFSResourceRoot implements WebResourceRoot  {
	
	private static final OLog log = Tracing.createLoggerFor(VFSResourceRoot.class);
	private static final int BUFFER_SIZE = 2048;
	
	private final Identity identity;
	private final VFSContainer base;
	
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
	
	@Override
	public boolean canWrite(String name) {
		// resolve item if it already exists
		VFSItem item = resolveFile(name);
		if (item == null) {
			// try to resolve parent in case the item does not yet exist
			int lastSlash = name.lastIndexOf('/');
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
		return item != null && VFSConstants.YES.equals(item.canRename());
	}	

	@Override
	public boolean canDelete(String path) {
		VFSItem item = resolveFile(path);
		return item != null && VFSConstants.YES.equals(item.canDelete());
	}

	@Override
	public WebResource getResource(String path) {
		VFSItem file = resolveFile(path);
		if(file == null) {
			return new EmptyWebResource(path);
		}
		return new VFSResource(file, path);
	}

	@Override
	public Collection<VFSItem> list(String path) {
		VFSItem file = resolveFile(path);
		if(file instanceof VFSContainer) {
			VFSContainer container = (VFSContainer)file;
			return container.getItems();
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
			if(folder.canWrite() == VFSConstants.YES) {
				VFSContainer dir = folder.createChildContainer(name);
				return dir != null && dir.exists();
			}
		}
		return false;
	}

	@Override
	public boolean write(String path, InputStream is, boolean overwrite, WebResource movedFrom)
	throws QuotaExceededException {
		VFSLeaf childLeaf;
		VFSItem file = resolveFile(path);
		if (file instanceof VFSLeaf) {
			if(overwrite) {
				//overwrite the file
				childLeaf = (VFSLeaf)file;
				
				//versioning
				if(childLeaf instanceof Versionable && ((Versionable)childLeaf).getVersions().isVersioned()) {
					if(childLeaf.getSize() == 0) {
						CoreSpringFactory.getImpl(VersionsManager.class).createVersionsFor(childLeaf, true);
					} else {
						CoreSpringFactory.getImpl(VersionsManager.class).addToRevisions((Versionable)childLeaf, identity, "");
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
	        copyVFS(childLeaf, is);
		} catch (QuotaExceededException e) {
			throw e;
		} catch (Exception e) {
			log.error("", e);
			return false;
		}

		VFSContainer inheritingCont = VFSManager.findInheritingSecurityCallbackContainer(childLeaf.getParentContainer());
		if(inheritingCont != null) {
			VFSSecurityCallback callback = inheritingCont.getLocalSecurityCallback();
			if(callback != null && callback.getSubscriptionContext() != null) {
				SubscriptionContext subContext = callback.getSubscriptionContext();
				NotificationsManager.getInstance().markPublisherNews(subContext, null, true);
			}
		}
		
		if(identity != null && childLeaf.canMeta() == VFSConstants.YES) {
			MetaInfo infos = childLeaf.getMetaInfo();
			if(infos != null && !infos.hasAuthorIdentity()) {
				infos.setAuthor(identity);
				addLicense(infos, identity);
				infos.clearThumbnails();
				//infos.write(); the clearThumbnails call write()
			}
		}
		
		if(movedFrom instanceof VFSResource) {
			VFSResource vfsResource = (VFSResource)movedFrom;
			if(vfsResource.getItem() instanceof Versionable
					&& ((Versionable)vfsResource.getItem()).getVersions().isVersioned()) {
				VFSLeaf currentVersion = (VFSLeaf)vfsResource.getItem();
				CoreSpringFactory.getImpl(VersionsManager.class).move(currentVersion, childLeaf, identity);
			}
		}
		
		return true;
	}

	private void addLicense(MetaInfo meta, Identity identity) {
		LicenseService licenseService = CoreSpringFactory.getImpl(LicenseService.class);
		LicenseModule licenseModule = CoreSpringFactory.getImpl(LicenseModule.class);
		FolderLicenseHandler licenseHandler = CoreSpringFactory.getImpl(FolderLicenseHandler.class);
		if (licenseModule.isEnabled(licenseHandler)) {
			License license = licenseService.createDefaultLicense(licenseHandler, identity);
			meta.setLicenseTypeKey(String.valueOf(license.getLicenseType().getKey()));
			meta.setLicenseTypeName(license.getLicenseType().getName());
			meta.setLicensor(license.getLicensor());
			meta.setLicenseText(LicenseUIFactory.getLicenseText(license));
		}
	}

	private void copyVFS(VFSLeaf file, InputStream is) throws IOException {
		// Try to get Quota
		long quotaLeft = -1;
		boolean withQuotaCheck = false;
		VFSContainer parentContainer = file.getParentContainer();
		if (parentContainer != null) {
			quotaLeft = VFSManager.getQuotaLeftKB(parentContainer);
			if (quotaLeft != Quota.UNLIMITED) {
				quotaLeft = quotaLeft * 1024; // convert from kB
				withQuotaCheck = true;
			} else {
				withQuotaCheck = false;
			}
		}
		// Open os
		byte[] buffer = new byte[BUFFER_SIZE];
		int len = -1;
		boolean quotaExceeded = false;
		try(OutputStream os = file.getOutputStream(false)) {
			while (true) {
				len = is.read(buffer);
				if (len == -1) break;
				if (withQuotaCheck) {
					// re-calculate quota and check
					quotaLeft = quotaLeft - len;
					if (quotaLeft < 0) {
						log.info("Quota exceeded: " + file);
						quotaExceeded = true;
						break;
					}
				}
				os.write(buffer, 0, len);
			}
			
			if(quotaExceeded) {
				file.delete();
				throw new QuotaExceededException("");
			}
		} catch (IOException e) {
			file.delete();
			throw e;
		}
	}
	
	@Override
	public boolean delete(WebResource resource) {
		boolean deleted = false;
		if(resource instanceof VFSResource) {
			VFSResource vfsResource = (VFSResource)resource;
			VFSItem item = vfsResource.getItem();
			if (item != null && VFSConstants.YES.equals(item.canDelete())) {
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
