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
package org.olat.core.util.coordinate.filer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;

/**
 * Description:<br>
 * @deprecated work in progress
 * 
 * <P>
 * Initial Date:  12.11.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class DirectoryImpl implements Directory {
	private static final String DIR_USERSPACE = "u";

	/**
	 * true if the associated directory already existed on the filesystem before a method call on this DirectoryImpl created it.
	 * if true, then all file operations will take effect immediately, that is, the operations cannot take part in a transaction.
	 * The idea behind is that only newly created directories can be committed or rollbacked since only those are exclusively owned by this
	 * DirectoryImpl.
	 */
	private boolean realDirExisted;
	
	/**
	 * true if !realDirExisted and later call to getFileSystemDirectory() of this directory or any child directory 
	 * caused the directory to be created 
	 * (although it is a temporary directory in the same folder as the "real" one. the temp dir will be changed 
	 * (renamed) to the "real" one at commit time.
	 * 
	 */
	private boolean realDirCreated = false;

	/**
	 * the parent of this DirectoryImpl, used to prepare the real filesystem directory creation
	 */
	private final DirectoryImpl parent;

	/**
	 * reference to the impl of the Filer, for helper calls
	 */
	private FilerImpl filerImpl; 
	
	/**
	 * the children: a map with derived olatresourceables' strings as keys and directoryimpls as children.
	 */
	private Map<String, DirectoryImpl> childrenDirs = new HashMap<String, DirectoryImpl>();
	
	/**
	 * ref to the created userSpaceDir
	 */
	private transient File userSpaceDir = null;

	private final File fsDir;
	
	private long uidForChildren;
	
	/**
	 * 
	 * @param parent
	 * @param filerImpl
	 */
	DirectoryImpl(DirectoryImpl parent, File fsDir, boolean realDirExisting) {
		this.parent = parent;
		this.fsDir = fsDir;
		uidForChildren = System.currentTimeMillis();
	}
	
	
	public void delete() {
		//
	}

	/* (non-Javadoc)
	 * @see org.olat.core.util.coordinate.filer.Directory#getDirectoryFor(org.olat.core.id.OLATResourceable)
	 */
	public Directory getDirectoryFor(OLATResourceable ores) {
		String fsChildName = createFileSystemSafeStringRepresenting(ores);
		DirectoryImpl child;
		synchronized (childrenDirs) {
			// either create or lookup the entry
			child = childrenDirs.get(fsChildName);
			if (child == null) {
				File childDir = new File(fsDir, "d"+fsChildName);
				// if child file does exist, it has been accessed earlier.
				boolean childExistedBefore = childDir.exists();
				if (childExistedBefore) {
					// don't take part in a transaction - simply pass on the real dir
					child = new DirectoryImpl(this, childDir, true);
				} else {
					// not made yet - create a temp dir
					//
					String nodeId = "1";
					long uid = uidForChildren++;
					File tmpChildDir = new File(fsDir, "t"+nodeId+"_"+uid+"_"+fsChildName);
					if (tmpChildDir.exists()) {
						//
						// another thread concurrently 
					}
					//if (!tmpChildDir.mkdir();
					//childDir.mkdir(); // the invariant here is that parent directories have already been created.
					child = new DirectoryImpl(this, tmpChildDir, false);
				}
				childrenDirs.put(fsChildName, child);
			}
		}
		return child; 
	}



	/* (non-Javadoc)
	 * @see org.olat.core.util.coordinate.filer.Directory#getFileSystemDirectory()
	 */
	public File getFileSystemDirectory() {
		synchronized(this) {
			if (!realDirCreated) {
				// make sure the filesystem directory exists.
				File parentDir = doCreateDir();
				userSpaceDir = new File(parentDir, DIR_USERSPACE);
				realDirCreated = true;
			}
			return userSpaceDir;			
		}
	}
	
	
	/**
	 * creates the filesystem directory associated with this Directory object.
	 * the fs directory has the following structure.<br>
	 * . this directory<br>
	 * ./u the directory made available to the user (= the user calling getFileSystemDirectory())<br>
	 * ./dX, one per created child (after commit), with each X being a String made of [a-z0-9_] which represents the olatresourceable of the child Directory.
	 * ./tX, one per created child (while the transaction is in progress), with each X being a String which is unique at least within 
	 * this directory and contains<br>
	 * - the nodeId of the cluster (so the jvm knows which temp files to clean when booting)<br>
	 * - a directory-wide unique and recalcalutable id (the olatresourceable with a prefix is used)<br>
	 * The temp files will be cleared upon rollback and/or on startup of the jvm (to delete those files that were left during a transaction with a jvm crash).
	 * the clearing process may even run asynchronously in order to not prolong the startup time - in order to not delete newly created temp files, the dir.lastModified
	 * property is used (only deleting temp files which were created in the past)
	 * @return
	 */
	File doCreateDir() {
		// creates the directory in the filesystem.
		// 1. make sure that parent directory is created
		if (parent == null) throw new AssertException("cannot create files on the root node!");
		File myDir = null; //parent.doCreateDir(this);		
		// and return this directory
		return myDir;
	}
	
	/**
	 * converts a olatresourceable to a file system safe string.<br>
	 * Precondition: ores.getResourceableTypeName() only contains characters [a-z.@]
	 * 
	 * @param ores the olatresourceable to convert
	 * @return the created string
	 */
	private String createFileSystemSafeStringRepresenting(OLATResourceable ores) {
		return ores.getResourceableTypeName()+"_"+ores.getResourceableId();
	}

}
