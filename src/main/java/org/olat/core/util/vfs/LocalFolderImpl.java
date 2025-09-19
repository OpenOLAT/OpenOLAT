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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.util.vfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryModule;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.core.util.vfs.filters.VFSRevisionsAndThumbnailsFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;

/**
 * Description:<br>
 * VFSContainer implementation that is based on a java.io.File from a local
 * filesystem. It implements a regular directory
 * 
 * <P>
 * Initial Date: 23.06.2005 <br>
 * 
 * @author Felix Jost
 */
public class LocalFolderImpl extends LocalImpl implements VFSContainer {
	private static final Logger log = Tracing.createLoggerFor(LocalFolderImpl.class);

	private String iconCSS;
	private VFSItemFilter defaultFilter;
	
	/**
	 * @param folderfile
	 */
	private LocalFolderImpl() {
		super(null, null);
		throw new AssertException("Cannot instantiate LocalFolderImpl().");
	}
	
	/**
	 * Constructor
	 * @param folderFile The real file of type directory wrapped by this VFSContainer
	 */
	public LocalFolderImpl(File folderFile) {
		this(folderFile, null);
	}
	
	/**
	 * @param folderfile
	 */
	public LocalFolderImpl(File folderfile, VFSContainer parent) {
		super(folderfile, parent);
		boolean alreadyExists = folderfile.exists();
		boolean succesfullCreated = alreadyExists || folderfile.mkdirs();
		//check against concurrent creation of the folder, mkdirs return false if the directory exists
		if (!alreadyExists && !succesfullCreated && folderfile.exists()) {
			succesfullCreated = true;
		}
		if (!alreadyExists && !succesfullCreated) {
			throw new AssertException("Cannot create directory of LocalFolderImpl with reason (exists= ): "+alreadyExists+" && created= "+succesfullCreated+") path: " + folderfile.getAbsolutePath());
		}
	}

	@Override
	public String getIconCSS() {
		if (StringHelper.containsNonWhitespace(iconCSS)) {
			return iconCSS;
		}
		return VFSContainer.super.getIconCSS();
	}

	public void setIconCSS(String iconCSS) {
		this.iconCSS = iconCSS;
	}

	@Override
	public List<VFSItem> getItems() {
		return getItems(null);
	}

	@Override
	public List<VFSItem> getItems(VFSItemFilter filter) {
		File aFolder = getBasefile();
		if(! aFolder.isDirectory()){
			throw new AssertException("basefile is not a directory: "+aFolder.getAbsolutePath());			
		}
		File[] children = aFolder.listFiles();
		if(children == null) {
			children = new File[0];
		}
		int len = children.length;
		List<VFSItem> res = new ArrayList<>(len);

		for (int i = 0; i < len; i++) {
			File af = children[i];
			VFSItem item;
			if (af.isDirectory()) {
				LocalFolderImpl folderItem = new LocalFolderImpl(af, this);
				folderItem.setDefaultItemFilter(defaultFilter);
				item = folderItem;
			} else {
				item = new LocalFileImpl(af, this);
			}
			if ((defaultFilter == null || defaultFilter.accept(item))
					&& (filter == null || filter.accept(item))) {
				res.add(item);
			}
		}
		return res;
	}

	@Override
	public VFSStatus canDescendants() {
		return canMeta();
	}
	
	@Override
	public List<VFSItem> getDescendants(VFSItemFilter filter) {
		List<VFSItem> allItems = new ArrayList<>();
		loadItemsAndChildren(allItems, this, filter);
		return allItems;
		
	}
	
	private void loadItemsAndChildren(List<VFSItem> allItems, VFSContainer vfsContainer, VFSItemFilter vfsFilter) {
		List<VFSItem> items = vfsContainer.getItems(vfsFilter);
		allItems.addAll(items);
		
		items.forEach(item -> {
			if (item instanceof VFSContainer childContainer) {
				loadItemsAndChildren(allItems, childContainer, vfsFilter);
			}
		});
	}

	@Override
	public void setDefaultItemFilter(VFSItemFilter defaultFilter){
		this.defaultFilter = defaultFilter;
	}

	@Override
	public VFSSuccess copyFrom(VFSItem source, Identity savedBy) {
		return copyFrom(source, true, savedBy);
	}
	
	@Override
	public VFSSuccess copyContentOf(VFSContainer container, Identity savedBy) {
		VFSSuccess status = VFSSuccess.SUCCESS;
		for(VFSItem item:container.getItems(new VFSSystemItemFilter())) {
			status = copyFrom(item, true, savedBy);
		}
		return status;
	}
	
	private VFSSuccess copyFrom(VFSItem source, boolean checkQuota, Identity savedBy) {
		if (source.canCopy() != VFSStatus.YES) {
			log.warn("Cannot copy file {} security denied", source);
			return VFSSuccess.ERROR_SECURITY_DENIED;
		}
		
		String sourcename = source.getName();
		File basefile = getBasefile();

		// check if there is already an item with the same name...
		if (resolve(sourcename) != null) {
			log.warn("Cannot copy file {} name already used", sourcename);
			return VFSSuccess.ERROR_NAME_ALREDY_USED;
		}
		
		if (source instanceof VFSContainer sourceContainer) {
			if (isSame(sourceContainer)) {
				log.warn("Cannot copy {} to {}: it is the same", sourceContainer, this);
				return VFSSuccess.ERROR_OVERLAPPING;
			}
			
			// "copy" the container means creating a folder with that name
			// and let the children copy
			
			// create the folder
			LocalFolderImpl targetContainer = new LocalFolderImpl(new File(basefile, sourcename), this);
			if (targetContainer.canMeta() == VFSStatus.YES) {
				VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
				vfsRepositoryService.copyTo(sourceContainer, targetContainer, this, savedBy);
			}
			
			// copy recursively
			List<VFSItem> children = sourceContainer.getItems(new VFSSystemItemFilter());
			for (VFSItem child : children) {
				VFSSuccess status = targetContainer.copyFrom(child , checkQuota, savedBy);
				if (status != VFSSuccess.SUCCESS) {
					log.warn("Cannot copy file {} with status {}", child , status);
					return status;
				}
			}
		} else if (source instanceof VFSLeaf sourceLeaf) {
			if (checkQuota) {
				long quotaLeft = VFSManager.getQuotaLeftKB(this);
				if(quotaLeft != Quota.UNLIMITED && quotaLeft < (sourceLeaf.getSize() / 1024)) {
					log.warn("Cannot copy file {} quota exceeded {}", sourceLeaf, quotaLeft);
					return VFSSuccess.ERROR_QUOTA_EXCEEDED;
				}
			}
			
			File fTarget = new File(basefile, sourcename);
			try(InputStream in = sourceLeaf.getInputStream()) {
				FileUtils.bcopy(in, fTarget, "VFScopyFrom");
			} catch (Exception e) {
				return VFSSuccess.ERROR_FAILED;
			}
			
			VFSItem target = resolve(sourcename);
			if (target instanceof VFSLeaf targetLeaf) {
				if (targetLeaf.canMeta() == VFSStatus.YES) {
					VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
					vfsRepositoryService.itemSaved(targetLeaf, savedBy);
					vfsRepositoryService.copyTo(sourceLeaf, targetLeaf, this, savedBy);
				}
			}
		} else {
			throw new RuntimeException("neither a leaf nor a container!");
		}
		return VFSSuccess.SUCCESS;
	}

	@Override
	public VFSStatus canWrite() {
		VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(this);
		if (inheritingContainer != null && !inheritingContainer.getLocalSecurityCallback().canWrite())
			return VFSStatus.NO;
		return VFSStatus.YES;
	}
	
	@Override
	public VFSStatus canVersion() {
		return VFSRepositoryModule.canVersion(getBasefile());
	}

	@Override
	public VFSSuccess rename(String newname) {
		File f = getBasefile();
		if(!f.exists()) {
			return VFSSuccess.ERROR_FAILED;
		}
		
		if(canMeta() == VFSStatus.YES) {
			CoreSpringFactory.getImpl(VFSRepositoryService.class).rename(this, newname);
		}
		
		File par = f.getParentFile();
		File nf = new File(par, newname);
		boolean ren = f.renameTo(nf);
		if (ren) {
			// f.renameTo() does NOT modify the path contained in the object f!!
			// The guys at sun consider this a feature and not a bug...
			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4094022
			// We need to manually reload the new basefile and set it in our parent
			super.setBasefile(new File(nf.getAbsolutePath()));
			return VFSSuccess.SUCCESS;
		}
		return VFSSuccess.ERROR_FAILED;
	}

	@Override
	public VFSSuccess delete() {
		File f = getBasefile();
		if (!f.exists()) {
			return VFSSuccess.SUCCESS;
		}
		File parentFile = f.getParentFile();
		if (VFSRepositoryService.TRASH_NAME.equals(parentFile.getName()) || VFSRepositoryService.TRASH_NAME.equals(getName())) {
			return VFSSuccess.SUCCESS;
		}
		
		VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
		Identity doer = ThreadLocalUserActivityLogger.getLoggedIdentity();
		VFSSuccess status = null;
		if (canMeta() == VFSStatus.YES) {
			VFSMetadata vfsMetadata = vfsRepositoryService.getMetadataFor(this);
			
			if (!getRelPath().contains(VFSRepositoryService.TRASH_NAME)) {
				File trashFile = new File(parentFile, VFSRepositoryService.TRASH_NAME);
				if (!trashFile.exists()) {
					trashFile.mkdirs();
				}
				
				LocalFolderImpl trashContainer = new LocalFolderImpl(trashFile);
				String filenameInTrash = VFSManager.similarButNonExistingName(trashContainer, f.getName(), "_");
				File fileInTrash = new File(trashFile, filenameInTrash);
				boolean renamed = f.renameTo(fileInTrash);
				
				if (renamed) {
					super.setBasefile(new File(fileInTrash.getAbsolutePath()));
					status = VFSSuccess.SUCCESS;
				} else {
					status = VFSSuccess.ERROR_FAILED;
				}
			} else {
				status = VFSSuccess.ERROR_FAILED;
			}
			
			if (vfsMetadata != null) {
				vfsRepositoryService.markAsDeleted(doer, vfsMetadata, getBasefile());
			}
		}
		
		List<VFSItem> children = getItems(new VFSRevisionsAndThumbnailsFilter());
		for (VFSItem child:children) {
			child.delete();
		}
		
		if (status != null) {
			// Folder was moved to trash
			VFSMetadata vfsMetadata = vfsRepositoryService.getMetadataFor(this);
			vfsRepositoryService.cleanTrash(doer, vfsMetadata);
			return status;
		}
		
		return deleteBasefile();
	}

	@Override
	public VFSSuccess restore(VFSContainer targetContainer) {
		if (targetContainer.canWrite() != VFSStatus.YES) {
			return VFSSuccess.ERROR_FAILED;
		}
		if (canMeta() != VFSStatus.YES) {
			return VFSSuccess.ERROR_FAILED;
		}
		File file = getBasefile();
		if (!file.exists()) {
			return VFSSuccess.ERROR_FAILED;
		}
		
		// Not in trash
		if (!getRelPath().contains(VFSRepositoryService.TRASH_NAME)) {
			return VFSSuccess.ERROR_FAILED;
		}
		
		VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
		Identity doer = ThreadLocalUserActivityLogger.getLoggedIdentity();
		
		VFSMetadata vfsMetadata = vfsRepositoryService.getMetadataFor(this);
		if (vfsMetadata == null || !vfsMetadata.isDeleted()) {
			return VFSSuccess.ERROR_FAILED;
		}
		
		long usage = VFSManager.getUsageKB(this);
		long quotaLeft = VFSManager.getQuotaLeftKB(targetContainer);
		if (quotaLeft != Quota.UNLIMITED && quotaLeft < usage) {
			return VFSSuccess.ERROR_QUOTA_EXCEEDED;
		}
		
		String restoredFilename = VFSManager.similarButNonExistingName(targetContainer, file.getName(), "_");
		
		if (targetContainer instanceof LocalFolderImpl localFolder) {
			File folder = localFolder.getBasefile();
			File fileRestored = new File(folder, restoredFilename);
			boolean renamed = file.renameTo(fileRestored);
			if (renamed) {
				super.setBasefile(new File(fileRestored.getAbsolutePath()));
			}
			vfsRepositoryService.unmarkFromDeleted(doer, vfsMetadata, fileRestored);
			
			List<VFSItem> children = getItems();
			for (VFSItem child : children) {
				child.restore(targetContainer);
			}
			
			return VFSSuccess.SUCCESS;
		}
		
		return VFSSuccess.ERROR_FAILED;
	}

	@Override
	public VFSSuccess deleteSilently() {
		if(!getBasefile().exists()) {
			return VFSSuccess.SUCCESS;  // already non-existent
		}
		// we must empty the folders and subfolders first
		List<VFSItem> children = getItems();
		for (VFSItem child:children) {
			child.deleteSilently(); 
		}
		
		VFSMetadata metaInfo = null;
		if(canMeta() == VFSStatus.YES) {
			metaInfo = getMetaInfo();
			CoreSpringFactory.getImpl(VFSRepositoryService.class).deleteMetadata(metaInfo);
		}
		// now delete the directory itself
		VFSSuccess vfsSuccess = deleteBasefile();
		
		if (metaInfo != null) {
			CoreSpringFactory.getImpl(VFSRepositoryService.class).updateParentLastModified(metaInfo);
		}
		
		return vfsSuccess;
	}
	
	private VFSSuccess deleteBasefile() {
		log.debug("Delete basefile {}", this);
		
		VFSSuccess status = VFSSuccess.ERROR_FAILED;
		try {
			// walk tree make sure the directory is deleted once all files,
			// versions files and others are properly deleted
			Files.walkFileTree(getBasefile().toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
			status = VFSSuccess.SUCCESS;
		} catch(IOException e) {
			log.error("Cannot delete base file: " + this, e);
		}
		return status;
	}
	
	@Override
	public VFSItem resolve(String path) {
		VFSItem resolved = VFSManager.resolveFile(this, path);
		// set default filter on resolved file if it is a container
		if (resolved instanceof VFSContainer) {
			VFSContainer resolvedContainer = (VFSContainer) resolved;
			resolvedContainer.setDefaultItemFilter(defaultFilter);
		}
		return resolved;
	}

	@Override
	public boolean isInPath(String path) {
		Path bFile = getBasefile().toPath();
		Path filePath = bFile.resolve(path);
		Path normalizedPath = filePath.normalize();
		return normalizedPath.startsWith(bFile);
	}

	@Override
	public String getRelPath() {
		Path bFile = getBasefile().toPath();
		Path bcRoot = FolderConfig.getCanonicalRootPath();
		
		String relPath;
		if(bFile.startsWith(bcRoot)) {
			relPath = bcRoot.relativize(bFile).toString();
			if(relPath.endsWith("/")) {
				relPath = relPath.substring(0, relPath.length() - 1);
			}
			if(!relPath.startsWith("/")) {
				relPath = "/".concat(relPath);
			}
		} else {
			relPath = null;
		}
		return relPath;
	}
	
	@Override
	public String toString() {
		return "LFolder [base="+getBasefile()+"] ";
	}

	@Override
	public VFSContainer createChildContainer(String name) {
		name = cleanFilename(name); // backward compatibility
		File fNewFile = new File(getBasefile(), name);
		if(!isInPath(name)) {
			log.warn("Could not create a new container::{} in container::{} - file out of parent directory", name, getBasefile().getAbsolutePath());
			return null;
		}
		if (!fNewFile.mkdir()) {
			return null;
		}
		LocalFolderImpl locFI =  new LocalFolderImpl(fNewFile, this);
		locFI.setDefaultItemFilter(defaultFilter);
		locFI.getMetaInfo(); //init metadata
		return locFI;
	}

	@Override
	public VFSLeaf createChildLeaf(String name) {
		name = cleanFilename(name); // backward compatibility
		File fNewFile = new File(getBasefile(), name);
		try {
			if(!isInPath(name)) {
				log.warn("Could not create a new leaf::{} in container::{} - file out of parent directory", name, getBasefile().getAbsolutePath());
				return null;
			}
			if(!fNewFile.getParentFile().exists()) {
				fNewFile.getParentFile().mkdirs();
			}
			if (!fNewFile.createNewFile()) {
				log.warn("Could not create a new leaf::{} in container::{} - file already exists", name, getBasefile().getAbsolutePath());
				return null;
			} 
		} catch (Exception e) {
			log.error("Error while creating child leaf::{} in container::{}", name, getBasefile().getAbsolutePath(), e);
			return null;
		}
		return new LocalFileImpl(fNewFile, this);
	}
	
	/**
	 * It was allowed to have a filename starting with /, but
	 * it's not legal.
	 * 
	 * @param name The name
	 * @return Name without starting /
	 */
	private String cleanFilename(String name) {
		if(name != null && name.startsWith("/")) {
			name = name.substring(1, name.length());
		}
		return name;
	}

	@Override
	public VFSItemFilter getDefaultItemFilter() {
		return defaultFilter;
	}
}
