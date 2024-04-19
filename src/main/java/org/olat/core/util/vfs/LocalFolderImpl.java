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
import org.olat.core.util.vfs.filters.VFSOrFilter;
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
	public VFSStatus copyFrom(VFSItem source, Identity savedBy) {
		return copyFrom(source, true, savedBy);
	}
	
	@Override
	public VFSStatus copyContentOf(VFSContainer container, Identity savedBy) {
		VFSStatus status = VFSStatus.YES;
		for(VFSItem item:container.getItems(new VFSSystemItemFilter())) {
			status = copyFrom(item, true, savedBy);
		}
		return status;
	}

	/**
	 * Internal copy from, preventing quota checks on subfolders.
	 * 
	 * @param source
	 * @param checkQuota
	 * @param savedBy 
	 * @return
	 */
	private VFSStatus copyFrom(VFSItem source, boolean checkQuota, Identity savedBy) {
		if (source.canCopy() != VFSStatus.YES) {
			log.warn("Cannot copy file {} security denied", source);
			return VFSStatus.NO_SECURITY_DENIED;
		}
		
		String sourcename = source.getName();
		File basefile = getBasefile();

		// check if there is already an item with the same name...
		if (resolve(sourcename) != null) {
			log.warn("Cannot copy file {} name already used", sourcename);
			return VFSStatus.ERROR_NAME_ALREDY_USED;
		}
		
		// add either file bla.txt or folder blu as a child of this folder
		if (source instanceof VFSContainer) {
			// copy recursively
			VFSContainer sourcecontainer = (VFSContainer)source;
			// check if this is a containing container...
			if (VFSManager.isSelfOrParent(sourcecontainer, this)) {
				log.warn("Cannot copy file {}  overlapping", this);
				return VFSStatus.ERROR_OVERLAPPING;
			}
			
			// "copy" the container means creating a folder with that name
			// and let the children copy

			// create the folder
			LocalFolderImpl rootcopyfolder = new LocalFolderImpl(new File(basefile, sourcename), this);
			List<VFSItem> children = sourcecontainer.getItems(new VFSOrFilter(List.of(new VFSRevisionsAndThumbnailsFilter(), new VFSSystemItemFilter())));
			for (VFSItem chd:children) {
				VFSStatus status = rootcopyfolder.copyFrom(chd, false, savedBy);
				if (status != VFSStatus.SUCCESS) {
					log.warn("Cannot copy file {} with status {}", chd, status);
				}
			}
		} else if (source instanceof VFSLeaf) {
			// copy single item
			VFSLeaf s = (VFSLeaf) source;
			// check quota
			if (checkQuota) {
				long quotaLeft = VFSManager.getQuotaLeftKB(this);
				if(quotaLeft != Quota.UNLIMITED && quotaLeft < (s.getSize() / 1024)) {
					log.warn("Cannot copy file {} quota exceeded {}", s, quotaLeft);
					return VFSStatus.ERROR_QUOTA_EXCEEDED;
				}
			}
			
			File fTarget = new File(basefile, sourcename);
			try(InputStream in=s.getInputStream()) {
				FileUtils.bcopy(in, fTarget, "VFScopyFrom");
			} catch (Exception e) {
				return VFSStatus.ERROR_FAILED;
			}
			
			VFSItem target = resolve(sourcename);
			if (target instanceof VFSLeaf vfsLeaf) {
				if (vfsLeaf.canMeta() == VFSStatus.YES) {
					VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
					vfsRepositoryService.itemSaved(vfsLeaf, savedBy);
					vfsRepositoryService.copyTo(s, vfsLeaf, this, savedBy);
				}
			}
		} else {
			throw new RuntimeException("neither a leaf nor a container!");
		}
		return VFSStatus.SUCCESS;
	}

	@Override
	public VFSStatus canWrite() {
		VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(this);
		if (inheritingContainer != null && !inheritingContainer.getLocalSecurityCallback().canWrite())
			return VFSStatus.NO_SECURITY_DENIED;
		return VFSStatus.YES;
	}
	
	@Override
	public VFSStatus canVersion() {
		return VFSRepositoryModule.canVersion(getBasefile());
	}

	@Override
	public VFSStatus rename(String newname) {
		CoreSpringFactory.getImpl(VFSRepositoryService.class).rename(this, newname);
		
		File f = getBasefile();
		File par = f.getParentFile();
		File nf = new File(par, newname);
		boolean ren = f.renameTo(nf);
		if (ren) {
			// f.renameTo() does NOT modify the path contained in the object f!!
			// The guys at sun consider this a feature and not a bug...
			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4094022
			// We need to manually reload the new basefile and set it in our parent
			super.setBasefile(new File(nf.getAbsolutePath()));
			return VFSStatus.YES; 
		}
		return VFSStatus.NO;
	}

	@Override
	public VFSStatus delete() {
		File f = getBasefile();
		if (!f.exists()) {
			return VFSStatus.YES;
		}
		File parentFile = f.getParentFile();
		if (VFSRepositoryService.TRASH_NAME.equals(parentFile.getName())) {
			return VFSStatus.YES;
		}
		
		VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
		Identity doer = ThreadLocalUserActivityLogger.getLoggedIdentity();
		VFSStatus status = null;
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
					status = VFSStatus.YES;
				} else {
					status = VFSStatus.NO;
				}
			} else {
				status = VFSStatus.NO;
			}
			
			if (vfsMetadata != null) {
				vfsRepositoryService.markAsDeleted(doer, vfsMetadata, getBasefile());
			}
		}
		
		List<VFSItem> children = getItems();
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
	public VFSStatus restore(VFSContainer targetContainer) {
		if (targetContainer.canWrite() != VFSStatus.YES) {
			return VFSStatus.NO;
		}
		if (canMeta() != VFSStatus.YES) {
			return VFSStatus.NO;
		}
		File file = getBasefile();
		if (!file.exists()) {
			return VFSStatus.NO;
		}
		
		// Not in trash
		if (!getRelPath().contains(VFSRepositoryService.TRASH_NAME)) {
			return VFSStatus.NO;
		}
		
		VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
		Identity doer = ThreadLocalUserActivityLogger.getLoggedIdentity();
		
		VFSMetadata vfsMetadata = vfsRepositoryService.getMetadataFor(this);
		if (vfsMetadata == null || !vfsMetadata.isDeleted()) {
			return VFSStatus.NO;
		}
		
		long usage = VFSManager.getUsageKB(this);
		long quotaLeft = VFSManager.getQuotaLeftKB(targetContainer);
		if (quotaLeft != Quota.UNLIMITED && quotaLeft < usage) {
			return VFSStatus.ERROR_QUOTA_EXCEEDED;
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
			
			return VFSStatus.YES;
		}
		
		return VFSStatus.NO;
	}

	@Override
	public VFSStatus deleteSilently() {
		if(!getBasefile().exists()) {
			return VFSStatus.YES;  // already non-existent
		}
		// we must empty the folders and subfolders first
		List<VFSItem> children = getItems();
		for (VFSItem child:children) {
			child.deleteSilently(); 
		}
		
		if(canMeta() == VFSStatus.YES) {
			CoreSpringFactory.getImpl(VFSRepositoryService.class).deleteMetadata(getMetaInfo());
		}
		// now delete the directory itself
		return deleteBasefile();
	}
	
	private VFSStatus deleteBasefile() {
		log.debug("Delete basefile {}", this);
		
		VFSStatus status = VFSStatus.NO;
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
			status = VFSStatus.YES;
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
