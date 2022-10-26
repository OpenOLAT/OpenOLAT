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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.util.ContainerAndFile;

public class VFSManager {
	
	private static final Logger log = Tracing.createLoggerFor(VFSManager.class);
	
	public static final String SEP = "/";
	
	/**
	 * The method create an instance of VFSLeaf
	 * but doesn't create the file.
	 * 
	 * @param fileRelPath The relative path to bcroot.
	 * @return An instance of VFSLeaf
	 */
	public static LocalFileImpl olatRootLeaf(String fileRelPath) {
		File file = new File(FolderConfig.getCanonicalRoot() + fileRelPath);
		return new LocalFileImpl(file, null);
	}
	
	public static LocalFileImpl olatRootLeaf(String relPath, String filename) {
		File file = new File(FolderConfig.getCanonicalRoot() + relPath, filename);
		return new LocalFileImpl(file, null);
	}
	
	public static LocalFolderImpl olatRootContainer(String fileRelPath) {
		File file = new File(FolderConfig.getCanonicalRoot() + fileRelPath);
		return new LocalFolderImpl(file, null);
	}
	
	public static LocalFolderImpl olatRootContainer(String fileRelPath, VFSContainer parentContainer) {
		File file = new File(FolderConfig.getCanonicalRoot() + fileRelPath);
		return new LocalFolderImpl(file, parentContainer);
	}
	
	public static File olatRootDirectory(String fileRelPath) {
		File file = new File(FolderConfig.getCanonicalRoot() + fileRelPath);
		return new LocalFolderImpl(file, null).getBasefile();
	}
	
	public static File olatRootFile(String fileRelPath) {
		File file = new File(FolderConfig.getCanonicalRoot() + fileRelPath);
		return new LocalFileImpl(file, null).getBasefile();
	}
	
	/**
	 * Make sure we always have a path that starts with a "/".
	 * 
	 * @param path
	 * @return
	 */
	public static String sanitizePath(String path) {
		// check for "empty" paths
		if (path == null || path.length() == 0) return "/";
		// prepend "/" if missing
		if (path.charAt(0) != '/') path = "/" + path;
		// cut trailing slash if any
		if (path.length() > 1 && path.charAt(path.length() - 1) == '/')
			path = path.substring(0, path.length() - 1);
		return path;
	}
	
	/**
	 * Extract the next subfolder (e.g. /foo/bla/gnu.txt -> "foo"
	 * PRE: a sanitized path, has a child
	 * @param path
	 * @return Next child.
	 */
	public static String extractChild(String path) {
		int slPos = path.indexOf('/', 1);
		String childName = null;
		if (slPos == -1) { // no subpath
			childName = path.substring(1);
		} else {
			childName = path.substring(1, slPos);
		}
		return childName;
	}
	
	/**
	 * Check if descendant is indeed a descendant of root..
	 * @param parent
	 * @param child
	 * @return
	 */
	public static boolean isContainerDescendantOrSelf(VFSContainer descendant, VFSContainer root) {
		if (root.isSame(descendant)) return true;
		VFSContainer parentContainer = descendant.getParentContainer();
		while (parentContainer != null) {
			if (parentContainer.isSame(root)) return true;
			parentContainer = parentContainer.getParentContainer();
		}
		return false;
	}
	
	/**
	 * Check if descendant is child of parent or same as parent.
	 * @param descendant
	 * @param root
	 * @return
	 */
	public static boolean isSelfOrParent(VFSContainer descendant, VFSContainer parent) {
		if (parent.isSame(descendant)) return true;
		VFSContainer parentContainer = descendant.getParentContainer();		
		if (parentContainer!=null && parentContainer.isSame(parent)) return true;
					
		return false;
	}
	
	public static boolean isDirectoryAndNotEmpty(VFSItem directory){
		if(directory instanceof VFSContainer) {
			List<VFSItem> children = ((VFSContainer)directory).getItems();
			return !children.isEmpty();
		}
		return false; 
	}
	
	/**
	 * @see org.olat.core.util.vfs.VFSItem#resolveFile(java.lang.String)
	 */
	public static VFSItem resolveFile(VFSContainer rootContainer, String path) {
		
		path = VFSManager.sanitizePath(path);
		if (path.equals("/")) { // slash or empty path -> return this vfsitem
			return rootContainer;
		}

		// The following code block eliminates directory scans on file-systems,
		// which are done in the original code in the next block, which is left
		// there as a fall-back in case there are non-file-system implementations
		// of OLAT-VFS.
		// OLAT file-systems can be very large and directories can contain
		// quite numerous files. Scanning these can take a lot of time.
		// Just put together the paths of both arguments
		// and ask the file-system whether such an entry
		// exists. If yes, this entry must be exactly what is
		// to be returned as, the proper type of, VFSItem.
		if (rootContainer instanceof LocalFolderImpl) {
			String childName = extractChild(path);
			LocalFolderImpl l = (LocalFolderImpl) rootContainer;
			File t = new File (l.getBasefile().getAbsolutePath(), childName);
			if (t.exists()) {
				if (t.isDirectory()) {
					VFSContainer subContainer = new LocalFolderImpl (t, rootContainer);
					String subPath = path.substring(childName.length() + 1);
					return resolveFile(subContainer, subPath);
				} else {
					return new LocalFileImpl(t, rootContainer);
				}
			} else {
				return null;
			}
		}

		//leave original code block as fall-back for non-file-system-based implementations
		String childName = extractChild(path);
		List<VFSItem> children = rootContainer.getItems();
		for (VFSItem child : children) {
			String curName = child.getName();
			if (childName.equals(curName)) { // found , let child further resolve if needed
				return child.resolve(path.substring(childName.length() + 1));
			}
		}
		return null;
	}

	/**
	 * Resolves a directory path in the base container or creates this
	 * directory. The method creates any missing directories.
	 * 
	 * @param baseContainer
	 *            The base directory. User must have write permissions on this
	 *            container
	 * @param relContainerPath
	 *            The path relative to the base container. Must start with a
	 *            '/'. To separate sub directories use '/'
	 * @return The resolved or created container or NULL if a problem happened
	 */
	public static VFSContainer resolveOrCreateContainerFromPath(VFSContainer baseContainer, String relContainerPath) {		
		VFSContainer resultContainer = baseContainer;
		if (!VFSConstants.YES.equals(baseContainer.canWrite())) {
			VFSItem resolvedPath = baseContainer.resolve(relContainerPath.trim());
			if(resolvedPath instanceof VFSContainer) {
				resultContainer = (VFSContainer)resolvedPath;
			} else {
				log.error("Could not create relPath::{}, base container::{} not writable",
						relContainerPath, getRealPath(baseContainer));
				resultContainer = null;
			}
		} else if (StringHelper.containsNonWhitespace(relContainerPath)){
			// Try to resolve given rel path from current container
			VFSItem resolvedPath = baseContainer.resolve(relContainerPath.trim());
			if (resolvedPath == null) {
				// Does not yet exist - create subdir
				String[] pathSegments = relContainerPath.split("/");
				for (int i = 0; i < pathSegments.length; i++) {
					String segment = pathSegments[i].trim();
					if (StringHelper.containsNonWhitespace(segment)) {
						resolvedPath = resultContainer.resolve(segment);
						if (resolvedPath == null) {
							resultContainer = resultContainer.createChildContainer(segment);
							if (resultContainer == null) {
								log.error("Could not create container with name::{} in relPath::{} in base container::{}",
										segment, relContainerPath, getRealPath(baseContainer));
								break;
							}						
						} else {
							if (resolvedPath instanceof VFSContainer) {
								resultContainer = (VFSContainer) resolvedPath;							
							} else {
								resultContainer = null;
								log.error("Could not create container with name::{} in relPath::{}, a file with this name exists (but not a directory) in base container::{}",
										segment, relContainerPath, getRealPath(baseContainer));
								break;
							}
						}
					}
				} 
			} else {
				// Parent dir already exists,  make sure this is really a container and not a file!
				if (resolvedPath instanceof VFSContainer) {
					resultContainer = (VFSContainer) resolvedPath;
				} else {
					resultContainer = null;
					log.error("Could not create relPath::{}, a file with this name exists (but not a directory) in base container::{}",
							relContainerPath, getRealPath(baseContainer));
				}
				
			}
		}
		return resultContainer;
	}
	
	/**
	 * Resolves a file path in the base container or creates this file under the
	 * given path. The method creates any missing directories.
	 * 
	 * @param baseContainer
	 *            The base directory. User must have write permissions on this
	 *            container
	 * @param relFilePath
	 *            The path relative to the base container. Must start with a
	 *            '/'. To separate sub directories use '/'
	 * @return The resolved or created leaf or NULL if a problem happened
	 */
	public static VFSLeaf resolveOrCreateLeafFromPath(VFSContainer baseContainer, String relFilePath) {
		if (StringHelper.containsNonWhitespace(relFilePath)) {
			int lastSlash = relFilePath.lastIndexOf("/");
			String relDirPath = relFilePath;
			String fileName = null;
			if (lastSlash == -1) {
				// relFilePath is the file name - no directories involved
				relDirPath = null;
				fileName = relFilePath;				
			} else if (lastSlash == 0) {
				// Remove start slash from file name
				relDirPath = null;
				fileName = relFilePath.substring(1, relFilePath.length());				
			} else {
				relDirPath = relFilePath.substring(0, lastSlash);
				fileName = relFilePath.substring(lastSlash);
			}
			
			// Create missing directories and set parent dir for later file creation
			VFSContainer parent = baseContainer;
			if (StringHelper.containsNonWhitespace(relDirPath)) {
				parent = resolveOrCreateContainerFromPath(baseContainer, relDirPath);				
			}
			// Now create file in that dir
			if (StringHelper.containsNonWhitespace(fileName)) {			
				VFSLeaf leaf = null;
				VFSItem resolvedFile = parent.resolve(fileName);
				if (resolvedFile == null) {
					leaf = parent.createChildLeaf(fileName);
					if (leaf == null) {
						log.error("Could not create leaf with relPath::" + relFilePath + " in base container::" + getRealPath(baseContainer));
					}
				} else {
					if (resolvedFile instanceof VFSLeaf) {
						leaf = (VFSLeaf) resolvedFile;
					} else {
						leaf = null;
						log.error("Could not create relPath::" + relFilePath + ", a directory with this name exists (but not a file) in base container::" + getRealPath(baseContainer));
					}
				}
				return leaf;			
			}
		}
		return null;
	}
	
	
	/**
	 * Get the security callback which affects this item. This searches up the path
	 * of parents to see wether it can find any callback. If no callback
	 * can be found, null is returned.
	 * 
	 * @param vfsItem
	 * @return
	 */
	public static VFSSecurityCallback findInheritedSecurityCallback(VFSItem vfsItem) {
		VFSItem inheritingItem = findInheritingSecurityCallbackContainer(vfsItem);
		if (inheritingItem != null) return inheritingItem.getLocalSecurityCallback();
		return null;
	}
	
	/**
	 * Get the container which security callback affects this item. This searches up the path
	 * of parents to see wether it can find any container with a callback. If no callback
	 * can be found, null is returned.
	 * 
	 * @param vfsItem
	 * @return
	 */
	public static VFSContainer findInheritingSecurityCallbackContainer(VFSItem vfsItem) {
		if (vfsItem == null) return null;
		// first resolve delegates of any NamedContainers to get the actual container (might be a MergeSource)
		if (vfsItem instanceof NamedContainerImpl) return findInheritingSecurityCallbackContainer(((NamedContainerImpl)vfsItem).getDelegate());
		// special treatment for MergeSource
		if (vfsItem instanceof MergeSource) {
			MergeSource mergeSource = (MergeSource)vfsItem;
			VFSContainer rootWriteContainer = mergeSource.getRootWriteContainer();
			if (rootWriteContainer != null && rootWriteContainer.getLocalSecurityCallback() != null) {
				// if the root write container has a security callback set, it will always override
				// any local securitycallback set on the mergesource
				return rootWriteContainer;
			} else if (mergeSource.getLocalSecurityCallback() != null) {
				return mergeSource;
			} else if (mergeSource.getParentContainer() != null) {
				return findInheritingSecurityCallbackContainer(mergeSource.getParentContainer());
			}
		} else {
			if ((vfsItem instanceof VFSContainer) && (vfsItem.getLocalSecurityCallback() != null)) return (VFSContainer)vfsItem;
			if (vfsItem.getParentContainer() != null) return findInheritingSecurityCallbackContainer(vfsItem.getParentContainer());
		}
		return null;
	}
	
	/**
	 * Check wether this container has a quota assigned to itself.
	 * 
	 * @param container
	 * @return Quota if this container has a Quota assigned, null otherwise.
	 */
	public static Quota isTopLevelQuotaContainer(VFSContainer container) {
		VFSSecurityCallback callback = container.getLocalSecurityCallback();
		if (callback != null && callback.getQuota() != null) return callback.getQuota();
		
		// extract delegate if this is a NamedContainer instance...
		if (container instanceof NamedContainerImpl) container = ((NamedContainerImpl)container).getDelegate();
		
		// check if this is a MergeSource with a root write container
		if (container instanceof MergeSource) {
			VFSContainer rwContainer = ((MergeSource)container).getRootWriteContainer();
			if (rwContainer != null && rwContainer.getLocalSecurityCallback() != null
				&& rwContainer.getLocalSecurityCallback().getQuota() != null)
				return rwContainer.getLocalSecurityCallback().getQuota();
		}
		return null;
	}
	
	/**
	 * Check the quota usage on this VFSContainer. If no security callback
	 * is provided, this returns -1 (meaning no quota on this folder).
	 * Similarly, if no quota is defined, VFSSecurityCallback.NO_QUOTA_DEFINED
	 * will be returned to signal no quota
	 * on this container.
	 * 
	 * @param securityCallback
	 * @param container
	 * @return
	 */
	public static long getQuotaLeftKB(VFSContainer container) {
		VFSContainer inheritingItem = findInheritingSecurityCallbackContainer(container);
		if (inheritingItem == null || inheritingItem.getLocalSecurityCallback().getQuota() == null)
			return Quota.UNLIMITED;
		long usageKB = getUsageKB(inheritingItem);
		return inheritingItem.getLocalSecurityCallback().getQuota().getQuotaKB().longValue() - usageKB;
	}
	
	/**
	 * Recursively traverse the container and sum up all leafs' sizes.
	 * 
	 * @param container
	 * @return
	 */
	public static long getUsageKB(VFSItem vfsItem) {
		if (vfsItem instanceof VFSContainer) {
			// VFSContainer
			if (vfsItem instanceof LocalFolderImpl)
				return FileUtils.getDirSize(((LocalFolderImpl)vfsItem).getBasefile()) / 1024;
			long usageKB = 0;
			List<VFSItem> children = ((VFSContainer)vfsItem).getItems();
			for (VFSItem child:children) {
				usageKB += getUsageKB(child);
			}
			return usageKB;
		} else {
			// VFSLeaf
			return ((VFSLeaf)vfsItem).getSize() / 1024;
		}
	}

	/**
	 * Returns the real path of the given VFS container. If the container is a
	 * named container, the delegate container is used. If the container is a
	 * merge source with a writable root container, then this one is used. In
	 * other cases the method returns null since the given container is not
	 * writable to any real file.
	 * 
	 * @param container
	 * @return String representing an absolute path for this container
	 */
	public static String getRealPath(VFSContainer container) {
		File file = getRealFile(container);
		if(file == null)
			return null;
		return file.getPath();
	}

	public static File getRealFile(VFSContainer container) {
		File realFile = null;
		LocalFolderImpl localFolder = null;
		if (container instanceof NamedContainerImpl)  {
			container = ((NamedContainerImpl)container).getDelegate();
		}
		if (container instanceof MergeSource) {
			container = ((MergeSource)container).getRootWriteContainer();
		}
		if (container != null && container instanceof LocalFolderImpl) {
			localFolder = (LocalFolderImpl) container;
			realFile = localFolder.getBasefile();
		}
		return realFile;
	}
	
	/**
	 * Get the path as string of the given item relative to the root
	 * container and the relative base path
	 * 
	 * @param item the item for which the relative path should be returned
	 * @param rootContainer
	 *            The root container for which the relative path should be
	 *            calculated
	 * @param relativeBasePath
	 *            when NULL, the path will be calculated relative to the
	 *            rootContainer; when NOT NULL, the relativeBasePath must
	 *            represent a relative path within the root container that
	 *            serves as the base. In this case, the calculated relative item
	 *            path will start from this relativeBasePath
	 * @return 
	 */
	public static String getRelativeItemPath(VFSItem item, VFSContainer rootContainer, String relativeBasePath) {
		// 1) Create path absolute to the root container
		if (item == null) return null;
		String absPath = "";
		VFSItem tmpItem = item;		
		// Check for merged containers to fix problems with named containers, see OLAT-3848
		List<NamedContainerImpl> namedRootChilds = new ArrayList<>();
		for (VFSItem rootItem : rootContainer.getItems()) {
			if (rootItem instanceof NamedContainerImpl) {
				namedRootChilds.add((NamedContainerImpl) rootItem);
			}
		}
		// Check if root container is the same as the item and vice versa. It is
		// necessary to perform the check on both containers to catch all potential
		// cases with MergedSource and NamedContainer where the check in one
		// direction is not necessarily the same as the opposite check
		while ( tmpItem != null && !rootContainer.isSame(tmpItem) && !tmpItem.isSame(rootContainer)) {
			String itemFileName = tmpItem.getName();
			//fxdiff FXOLAT-125: virtual file system for CP
			if(tmpItem instanceof NamedLeaf) {
				itemFileName = ((NamedLeaf)tmpItem).getDelegate().getName();
			}

			// Special case: check if this is a named container, see OLAT-3848
			for (NamedContainerImpl namedRootChild : namedRootChilds) {
				if (namedRootChild.isSame(tmpItem)) {
					itemFileName = namedRootChild.getName();
				}
			}
			absPath = "/" + itemFileName + absPath;
			tmpItem = tmpItem.getParentContainer();
			if (tmpItem != null) {
				// test if this this is a merge source child container, see OLAT-5726
				VFSContainer grandParent = tmpItem.getParentContainer();
				if (grandParent instanceof MergeSource) {
					MergeSource mergeGrandParent = (MergeSource) grandParent;
					if (mergeGrandParent.isContainersChild((VFSContainer) tmpItem)) {
						// skip this parent container and use the merge grand-parent
						// instead, otherwise path contains the container twice
						tmpItem = mergeGrandParent;						
					}
				}
			}
		}
		
		if (relativeBasePath == null) {
			return absPath;
		}
		// 2) Compute rel path to base dir of the current file
		
		// selpath = /a/irwas/subsub/nochsub/note.html 5
		// filenam = /a/irwas/index.html 3
		// --> subsub/nochsub/note.gif

		// or /a/irwas/bla/index.html
		// to /a/other/b/gugus.gif
		// --> ../../ other/b/gugus.gif

		// or /a/other/b/main.html
		// to /a/irwas/bla/goto.html
		// --> ../../ other/b/gugus.gif

		String base = relativeBasePath; // assume "/" is here
		if (!(base.indexOf("/") == 0)) {
			base = "/" + base;
		}

		String[] baseA = base.split("/");
		String[] targetA = absPath.split("/");
		int sp = 1;
		for (; sp < Math.min(baseA.length, targetA.length); sp++) {
			if (!baseA[sp].equals(targetA[sp])) {
				break;
			}
		}
		// special case: self-reference
		if (absPath.equals(base)) {
			sp = 1;
		}
		StringBuilder buffer = new StringBuilder();
		for (int i = sp; i < baseA.length - 1; i++) {
			buffer.append("../");
		}
		for (int i = sp; i < targetA.length; i++) {
			buffer.append(targetA[i] + "/");
		}
		if(buffer.length() > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}

	/**
	 * This method takes a VFSContainer and a relative path to a file that exists
	 * within this container. The method checks if the given container is a
	 * writable container that can be used e.g. by the HTML editor as a base
	 * directory where to store some things. If the method detects that this is
	 * not the case it works against the relative file path and checks each
	 * directory in the path. <br>
	 * The result will be an object array that contains the corrected container
	 * and the new relative path. If no writable container could be found NULL is
	 * returned. <br>
	 * Limitations: the method stops at least after 20 iterations returning NULL
	 * 
	 * @param rootDir the container that should be checked
	 * @param relFilePath The valid file path within this container
	 * @return Object array that contains 1) a writable rootDir and 2) the
	 *         corrected relFilePath that mathes to the new rootDir. Can be NULL
	 *         if no writable root folder could be found.
	 */
	public static ContainerAndFile findWritableRootFolderFor(VFSContainer rootDir, String relFilePath){ 
		int level = 0;
		return findWritableRootFolderForRecursion(rootDir, relFilePath, level);
	}
	private static ContainerAndFile findWritableRootFolderForRecursion(VFSContainer rootDir, String relFilePath, int recursionLevel){
		recursionLevel++;
		if (recursionLevel > 20) {
			// Emergency exit condition: a directory hierarchy that has more than 20
			// levels? Probably not..
			log.warn("Reached recursion level while finding writable root Folder - most likely a bug. rootDir::" + rootDir
					+ " relFilePath::" + relFilePath);
			return null;
		}

		if (rootDir instanceof NamedContainerImpl)  {
			rootDir = ((NamedContainerImpl)rootDir).getDelegate();
		}
		if (rootDir instanceof MergeSource) {
			MergeSource mergedDir = (MergeSource)rootDir;
			//first check if the next level is not a second MergeSource
			int stop = relFilePath.indexOf("/", 1);
			if(stop > 0) {
				String nextLevel = extractChild(relFilePath);
				VFSItem item = mergedDir.resolve(nextLevel);
				if (item instanceof NamedContainerImpl)  {
					item = ((NamedContainerImpl)item).getDelegate();
				}
				if(item instanceof MergeSource) {
					rootDir = (MergeSource)item;
					relFilePath = relFilePath.substring(stop);
					return findWritableRootFolderForRecursion(rootDir, relFilePath, recursionLevel);
				}
				//very< special case for share folder in merged source
				if(item instanceof LocalFolderImpl && "_sharedfolder_".equals(item.getName())) {
					rootDir = (LocalFolderImpl)item;
					relFilePath = relFilePath.substring(stop);
					return findWritableRootFolderForRecursion(rootDir, relFilePath, recursionLevel);
				}
			}

			VFSContainer rootWriteContainer = mergedDir.getRootWriteContainer();
			if (rootWriteContainer == null) {
				// we have a merge source without a write container, try it one higher,
				// go through all children of this one and search the correct child in
				// the path
				List<VFSItem> children = rootDir.getItems();
				if (children.isEmpty()) {
					// ups, a merge source without children, no good, return null
					return null;
				}

				String nextChildName = relFilePath.substring(1, relFilePath.indexOf("/", 1));
				for (VFSItem child : children) {
					// look up for the next child in the path
					if (child.getName().equals(nextChildName)) {
						// use this child as new root and remove the child name from the rel
						// path
						if (child instanceof VFSContainer) {
							rootDir = (VFSContainer) child;
							relFilePath = relFilePath.substring(relFilePath.indexOf("/",1));
							break;							
						} else {
							// ups, a merge source with a child that is not a VFSContainer -
							// no good, return null
							return null;							
						}
					}
				}
			} else {
				// ok, we found a merge source with a write container
				rootDir = rootWriteContainer;
			}
		}
		if (rootDir instanceof LocalFolderImpl) {
			// finished, we found a local folder we can use to write
			return new ContainerAndFile(rootDir, relFilePath);
		} else {
			// do recursion
			return findWritableRootFolderForRecursion(rootDir, relFilePath, recursionLevel);
		}
	}

	/**
	 * Returns a similar but non existing file name in root based on the given
	 * name.
	 * 
	 * 
	 * @param root
	 * @param name
	 * @return A non existing name based on the given name in the root directory
	 */

	public static String similarButNonExistingName(VFSContainer root, String name) {
		return similarButNonExistingName(root, name, "");
	}
	
	public static String similarButNonExistingName(VFSContainer root, String name, String numberPrefix) {
		VFSItem existingItem = null;
		String newName = name;
		existingItem = root.resolve(newName);
		for(int i = 1; existingItem != null && i<1000; i++) {
			newName = FileUtils.appendNumberAtTheEndOfFilename(name, i, numberPrefix);
			existingItem = root.resolve(newName);
		}
		return newName;
	}

	public static VFSContainer getOrCreateContainer(VFSContainer parent, String name) {
		VFSItem item = parent.resolve(name);
		if(item instanceof VFSContainer) {
			return (VFSContainer)item;
		} else if(item != null) {
			return null;//problem
		} else {
			return parent.createChildContainer(name);
		}
	}


	/**
	 * Copies the content of the source to the target leaf.
	 * 
	 * @param source The source file
	 * @param target The target file
	 * @param withMetadata true if the metadata must be copied too
	 * @param savedBy user who copied the leaf. May be null, if without metadata
	 * @return True on success, false on failure
	 */
	public static boolean copyContent(VFSLeaf source, VFSLeaf target, boolean withMetadata, Identity savedBy) {
		boolean successful;
		if (source != null && target != null) {
			try(InputStream in = new BufferedInputStream(source.getInputStream());
				OutputStream out = new BufferedOutputStream(target.getOutputStream(false))) {
				FileUtils.cpio(in, out, "Copy content");
				successful = true;
			} catch(IOException e) {
				log.error("Error while copying content from source: " + source.getName() + " to target: " + target.getName(), e);
				successful = false;
			}
			
			if (target.canMeta() == VFSConstants.YES) {
				VFSRepositoryService vfsRepositoryService = CoreSpringFactory.getImpl(VFSRepositoryService.class);
				vfsRepositoryService.itemSaved(target, savedBy);
				if (withMetadata && source.canMeta() == VFSConstants.YES) {
					vfsRepositoryService.copyTo(source, target, target.getParentContainer(), savedBy);
				}
			}
		} else {
			// source or target is null
			successful = false;
			if (log.isDebugEnabled()) log.debug("Either the source or the target is null. Content of leaf cannot be copied.");
		}
		return successful;
	}
	
	/**
	 * Copy the content of the source container to the target container without
	 * versions or metadata.
	 * 
	 * @param source The source container
	 * @param target the target container
	 * @return true if successful
	 */
	public static boolean copyContent(VFSContainer source, VFSContainer target) {
		if(!source.exists()) {
			return false;
		}
		if(isSelfOrParent(source, target)) {
			return false;
		}
		
		if(source instanceof NamedContainerImpl) {
			source = ((NamedContainerImpl)source).getDelegate();
		}
		if(target instanceof NamedContainerImpl) {
			target = ((NamedContainerImpl)target).getDelegate();
		}
		
		if(source instanceof LocalImpl && target instanceof LocalImpl) {
			File localSourceFile = ((LocalImpl)source).getBasefile();
			File localTargetFile = ((LocalImpl)target).getBasefile();
			return FileUtils.copyDirContentsToDir(localSourceFile, localTargetFile, false, "VFScopyDir");
		}
		return false;
	}
	
	/**
	 * Copy the content of the file in the target leaf.
	 * @param source A file
	 * @param target The target leaf
	 * @param savedBy 
	 * @return
	 */
	public static boolean copyContent(File source, VFSLeaf target, Identity savedBy) {
		try(InputStream in = new FileInputStream(source);
				BufferedInputStream bis = new BufferedInputStream(in, FileUtils.BSIZE)) {
			return copyContent(bis, target, savedBy);
		} catch(IOException ex) {
			log.error("", ex);
			return false;
		}
	}

	/**
	 * Copies the stream to the target leaf.
	 * 
	 * @param source
	 * @param target
	 * @param savedBy 
	 * @return True on success, false on failure
	 */
	public static boolean copyContent(InputStream inStream, VFSLeaf target, Identity savedBy) {
		boolean successful;
		if (inStream != null && target != null) {
			// write the input to the output
			try(InputStream in = new BufferedInputStream(inStream);
					OutputStream out = new BufferedOutputStream(target.getOutputStream(false))) {
				FileUtils.cpio(in, out, "");
				CoreSpringFactory.getImpl(VFSRepositoryService.class).itemSaved(target, savedBy);
				successful = true;
			} catch (IOException e) {
				// something went wrong.
				successful = false;
				log.error("Error while copying content from source: {} to target: {}", inStream, target.getName(), e);
			}
		} else {
			// source or target is null
			successful = false;
			if (log.isDebugEnabled()) log.debug("Either the source or the target is null. Content of leaf cannot be copied.");
		}
		return successful;
	}
	
	public static boolean copyContent(VFSLeaf source, OutputStream outStream) {
		boolean successful;
		if (outStream != null && source != null) {
			// write the input to the output
			try(InputStream in = source.getInputStream()) {
				FileUtils.cpio(in, outStream, "");
				successful = true;
			} catch (IOException e) {
				// something went wrong.
				successful = false;
				log.error("Error while copying content from source: " + source + " to target stream.", e);
			}
		} else {
			// source or target is null
			successful = false;
			if (log.isDebugEnabled()) log.debug("Either the source or the target is null. Content of leaf cannot be copied.");
		}
		return successful;
	}
	
	/**
	 * 
	 * @param container
	 * @param filename
	 * @return
	 */
	public static String rename(VFSContainer container, String filename) {
		String newName = filename;
		VFSItem newFile = container.resolve(newName);
		for(int count=1; newFile != null && count < 999 ; count++) {
			newName = FileUtils.appendNumberAtTheEndOfFilename(filename, count);
		    newFile = container.resolve(newName);
		}
		if(newFile == null) {
			return newName;
		}
		return null;
	}
	
	/**
	 * Check if the file exist or not
	 * @param item
	 * @return
	 */
	public static boolean exists(VFSItem item) {
		if (item instanceof NamedContainerImpl)  {
			item = ((NamedContainerImpl)item).getDelegate();
		}
		if (item instanceof MergeSource) {
			MergeSource source = (MergeSource)item;
			item = source.getRootWriteContainer();
			if(item == null) {
				//no write container, but the virtual container exist
				return true;
			}
			if (item instanceof NamedContainerImpl)  {
				item = ((NamedContainerImpl)item).getDelegate();
			}
		}
		if(item instanceof LocalImpl) {
			LocalImpl localFile = (LocalImpl)item;
			return localFile.getBasefile() != null && localFile.getBasefile().exists();
		}
		return false;
	}
	
	public static final String trimSlash(String path) {
		if(path != null && path.startsWith(SEP)) {
			path = path.substring(1, path.length());
		}
		if(path != null && path.endsWith(SEP)) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}
	
	public static final String removeLeadingSlash(String path) {
		if(path != null && path.startsWith(SEP)) {
			path = path.substring(1, path.length());
		}
		return path;
	}
	
	public static final String appendLeadingSlash(String path) {
		if(path == null || path.length() == 0) {
			return SEP;
		}
		if(!path.startsWith(SEP)) {
			return SEP.concat(path);
		}
		return path;
	}
	
	/**
	 * 
	 * @param path The path of a directory
	 * @return Append slash at both ends of the path if needed
	 */
	public static final String appendDirectorySlash(String path) {
		if(path == null || path.length() == 0) {
			return SEP;
		}
		if(!path.startsWith(SEP)) {
			path = SEP.concat(path);
		}
		if(!path.endsWith(SEP)) {
			path += SEP;
		}
		return path;
	}
}
