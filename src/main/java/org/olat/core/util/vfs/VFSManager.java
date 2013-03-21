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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.vfs.OlatRootFileImpl;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.util.ContainerAndFile;

public class VFSManager extends BasicManager {
	private static final Pattern fileNamePattern = Pattern.compile("(.+)[.](\\w{3,4})");
	private static final OLog log = Tracing.createLoggerFor(VFSManager.class);
	private static final int BUFFER_SIZE = 2048;
	
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
				String bcroot = FolderConfig.getCanonicalRoot();
				String fsPath = t.getAbsolutePath();
				if (t.isDirectory()) {
					VFSContainer subContainer;
					if (fsPath.startsWith(bcroot)) {
						fsPath = fsPath.replace(bcroot,"");
						subContainer = new OlatRootFolderImpl(fsPath, rootContainer);
					} else {
						subContainer = new LocalFolderImpl (t, rootContainer);
					}
					String subPath = path.substring(childName.length() + 1);
					return resolveFile(subContainer, subPath);
				} else {
					if (fsPath.startsWith(bcroot)) {
						fsPath = fsPath.replace(bcroot,"");
						return new OlatRootFileImpl(fsPath, rootContainer);
					} else {
						return new LocalFileImpl(t, rootContainer);
					}
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
		String realPath = null;
		LocalFolderImpl localFolder = null;
		if (container instanceof NamedContainerImpl)  {
			container = ((NamedContainerImpl)container).getDelegate();
		}
		if (container instanceof MergeSource) {
			container = ((MergeSource)container).getRootWriteContainer();
		}
		if (container != null && container instanceof LocalFolderImpl) {
			localFolder = (LocalFolderImpl) container;
			realPath = localFolder.getBasefile().getPath();
		}		
		return realPath;
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
		if (rootDir != null && rootDir instanceof LocalFolderImpl) {
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
	public static String similarButNonExistingName(VFSContainer root,
			String name) {
		VFSItem existingItem = null;
		String newName = name;
		existingItem = root.resolve(newName);
		int i = 1;
		while (existingItem != null) {
			newName = appendNumberAtTheEndOfFilename(name, i++);
			existingItem = root.resolve(newName);
		}
		return newName;
	}

	/**
	 * Sticks together a new filename. If there's a match with a common filename
	 * with extension, add the counter to the end of the filename before the
	 * extension. Else just add the counter to the end of the name. E.g.:
	 * hello.xml => hello1.xml where 1 is the counter
	 * 
	 * @param name
	 * @param number
	 * @return The new name with the counter added
	 */
	public static String appendNumberAtTheEndOfFilename(String name, int number) {
		// Try to match the file to the pattern "[name].[extension]"
		Matcher m = fileNamePattern.matcher(name);
		StringBuffer newName = new StringBuffer();
		if (m.matches()) {
			newName.append(m.group(1)).append(number);
			newName.append(".").append(m.group(2));
		} else {
			newName.append(name).append(number);
		}
		return newName.toString();
	}

	/**
	 * Copies the content of the source to the target leaf.
	 * 
	 * @param source
	 * @param target
	 * @return True on success, false on failure
	 */
	public static boolean copyContent(VFSLeaf source, VFSLeaf target) {
		boolean successful;
		if (source != null && target != null) {
			InputStream in = new BufferedInputStream(source.getInputStream());
			OutputStream out = new BufferedOutputStream(target.getOutputStream(false));
			// write the input to the output
			try {
				byte[] buf = new byte[BUFFER_SIZE];
				int i = 0;
        while ((i = in.read(buf)) != -1) {
            out.write(buf, 0, i);
        }
				successful = true;
			} catch (IOException e) {
				// something went wrong.
				successful = false;
				log.error("Error while copying content from source: " + source.getName() + " to target: " + target.getName(), e);
			} finally {
				// Close streams
				try {
					if (out != null) {
						out.flush();
						out.close();
					}
					if (in != null) {
						in.close();
					}
				} catch (IOException ex) {
					log.error("Error while closing/cleaning up in- and output streams", ex);
				}
			}
		} else {
			// source or target is null
			successful = false;
			if (log.isDebug()) log.debug("Either the source or the target is null. Content of leaf cannot be copied.");
		}
		return successful;
	}
	
	/**
	 * Copy the content of the source container to the target container.
	 * @param source
	 * @param target
	 * @return
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
			LocalImpl localSource = (LocalImpl)source;
			LocalImpl localTarget = (LocalImpl)target;
			File localSourceFile = localSource.getBasefile();
			File localTargetFile = localTarget.getBasefile();
			return FileUtils.copyDirContentsToDir(localSourceFile, localTargetFile, false, "VFScopyDir");
		}
		return false;
	}
	
	/**
	 * Copies the stream to the target leaf.
	 * 
	 * @param source
	 * @param target
	 * @return True on success, false on failure
	 */
	public static boolean copyContent(InputStream inStream, VFSLeaf target) {
		return copyContent(inStream, target, true);
	}
	
	/**
	 * Copies the stream to the target leaf.
	 * 
	 * @param source
	 * @param target
	 * @param closeInput set to false if it's a ZipInputStream
	 * @return True on success, false on failure
	 */
	public static boolean copyContent(InputStream inStream, VFSLeaf target, boolean closeInput) {
		boolean successful;
		if (inStream != null && target != null) {
			InputStream in = new BufferedInputStream(inStream);
			OutputStream out = new BufferedOutputStream(target.getOutputStream(false));
			// write the input to the output
			try {
				byte[] buf = new byte[BUFFER_SIZE];
				int i = 0;
        while ((i = in.read(buf)) != -1) {
            out.write(buf, 0, i);
        }
				successful = true;
			} catch (IOException e) {
				// something went wrong.
				successful = false;
				log.error("Error while copying content from source: " + inStream + " to target: " + target.getName(), e);
			} finally {
				// Close streams
				try {
					if (out != null) {
						out.flush();
						out.close();
					}
					if (closeInput && in != null) {
						in.close();
					}
				} catch (IOException ex) {
					log.error("Error while closing/cleaning up in- and output streams", ex);
				}
			}
		} else {
			// source or target is null
			successful = false;
			if (log.isDebug()) log.debug("Either the source or the target is null. Content of leaf cannot be copied.");
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
		for(int count=0; newFile != null && count < 999 ; ) {
			count++;
			newName = appendNumberAtTheEndOfFilename(filename, count);
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
		}
		if(item instanceof LocalImpl) {
			LocalImpl localFile = (LocalImpl)item;
			return localFile.getBasefile() != null && localFile.getBasefile().exists();
		}
		return false;
	}
}
