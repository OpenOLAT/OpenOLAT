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
import org.olat.core.commons.services.vfs.VFSRepositoryModule;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.core.util.vfs.filters.VFSVersionsItemFilter;

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
	public void setDefaultItemFilter(VFSItemFilter defaultFilter){
		this.defaultFilter = defaultFilter;
	}

	@Override
	public VFSStatus copyFrom(VFSItem source, Identity savedBy) {
		return copyFrom(source, true, savedBy);
	}
	
	@Override
	public VFSStatus copyContentOf(VFSContainer container, Identity savedBy) {
		VFSStatus status = VFSConstants.YES;
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
		if (source.canCopy() != VFSConstants.YES) {
			log.warn("Cannot copy file {} security denied", source);
			return VFSConstants.NO_SECURITY_DENIED;
		}
		
		String sourcename = source.getName();
		File basefile = getBasefile();

		// check if there is already an item with the same name...
		if (resolve(sourcename) != null) {
			log.warn("Cannot copy file {} name already used", sourcename);
			return VFSConstants.ERROR_NAME_ALREDY_USED;
		}
		
		// add either file bla.txt or folder blu as a child of this folder
		if (source instanceof VFSContainer) {
			// copy recursively
			VFSContainer sourcecontainer = (VFSContainer)source;
			// check if this is a containing container...
			if (VFSManager.isSelfOrParent(sourcecontainer, this)) {
				log.warn("Cannot copy file {}  overlapping", this);
				return VFSConstants.ERROR_OVERLAPPING;
			}
			
			// "copy" the container means creating a folder with that name
			// and let the children copy

			// create the folder
			LocalFolderImpl rootcopyfolder = new LocalFolderImpl(new File(basefile, sourcename), this);
			List<VFSItem> children = sourcecontainer.getItems(new VFSVersionsItemFilter());
			for (VFSItem chd:children) {
				VFSStatus status = rootcopyfolder.copyFrom(chd, false, savedBy);
				if (status != VFSConstants.SUCCESS) {
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
					return VFSConstants.ERROR_QUOTA_EXCEEDED;
				}
			}
			
			File fTarget = new File(basefile, sourcename);
			try(InputStream in=s.getInputStream()) {
				FileUtils.bcopy(in, fTarget, "VFScopyFrom");
			} catch (Exception e) {
				return VFSConstants.ERROR_FAILED;
			}

			if(s.canMeta() == VFSConstants.YES || s.canVersion() == VFSConstants.YES) {
				VFSItem target = resolve(sourcename);
				if(target instanceof VFSLeaf && (target.canMeta() == VFSConstants.YES || s.canVersion() == VFSConstants.YES)) {
					VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
					vfsRepositoryService.itemSaved( (VFSLeaf)target, savedBy);
					vfsRepositoryService.copyTo(s, (VFSLeaf)target, this, savedBy);
				}
			}
		} else {
			throw new RuntimeException("neither a leaf nor a container!");
		}
		return VFSConstants.SUCCESS;
	}

	@Override
	public VFSStatus canWrite() {
		VFSContainer inheritingContainer = VFSManager.findInheritingSecurityCallbackContainer(this);
		if (inheritingContainer != null && !inheritingContainer.getLocalSecurityCallback().canWrite())
			return VFSConstants.NO_SECURITY_DENIED;
		return VFSConstants.YES;
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
			return VFSConstants.YES; 
		} else {
			return VFSConstants.NO;
		}
	}

	@Override
	public VFSStatus delete() {
		if(!getBasefile().exists()) {
			return VFSConstants.YES;  // already non-existent
		}
		// we must empty the folders and subfolders first
		List<VFSItem> children = getItems();
		for (VFSItem child:children) {
			child.delete();
		}
		
		// Versioning makes a copy of the metadata, delete metadata after it
		if(canMeta() == VFSConstants.YES) {
			Identity identity = ThreadLocalUserActivityLogger.getLoggedIdentity();
			CoreSpringFactory.getImpl(VFSRepositoryService.class).markAsDeleted(this, identity);
		}
		// now delete the directory itself
		return deleteBasefile();
	}

	@Override
	public VFSStatus deleteSilently() {
		if(!getBasefile().exists()) {
			return VFSConstants.YES;  // already non-existent
		}
		// we must empty the folders and subfolders first
		List<VFSItem> children = getItems();
		for (VFSItem child:children) {
			child.deleteSilently(); 
		}
		
		if(canMeta() == VFSConstants.YES) {
			CoreSpringFactory.getImpl(VFSRepositoryService.class).deleteMetadata(getMetaInfo());
		}
		// now delete the directory itself
		return deleteBasefile();
	}
	
	private VFSStatus deleteBasefile() {
		VFSStatus status = VFSConstants.NO;
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
			status = VFSConstants.YES;
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
		File fNewFile = new File(getBasefile(), name);
		if (!fNewFile.mkdir()) return null;
		LocalFolderImpl locFI =  new LocalFolderImpl(fNewFile, this);
		locFI.setDefaultItemFilter(defaultFilter);
		return locFI;
	}

	@Override
	public VFSLeaf createChildLeaf(String name) {
		File fNewFile = new File(getBasefile(), name);
		try {
			if(!fNewFile.getParentFile().exists()) {
				fNewFile.getParentFile().mkdirs();
			}
			if (!fNewFile.createNewFile()) {
				log.warn("Could not create a new leaf::" + name + " in container::" + getBasefile().getAbsolutePath() + " - file alreay exists");
				return null;
			} 
		} catch (Exception e) {
			log.error("Error while creating child leaf::" + name + " in container::" + getBasefile().getAbsolutePath(), e);
			return null;
		}
		return new LocalFileImpl(fNewFile, this);
	}

	@Override
	public VFSItemFilter getDefaultItemFilter() {
		return defaultFilter;
	}
}
