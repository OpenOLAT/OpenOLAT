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
package org.olat.core.util.vfs.version;

import java.io.InputStream;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.util.async.ProgressDelegate;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date: 21 sept. 2009 <br>
 * 
 * @author srosse
 */
public abstract class VersionsManager {

	protected static VersionsManager INSTANCE;

	public static VersionsManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Get or create the versions datas of this file
	 * 
	 * @param a file
	 * @return
	 */
	public abstract Versions createVersionsFor(VFSLeaf leaf);
	
	/**
	 * Get or create the versions datas of this file
	 * 
	 * @param a file
	 * @param force the creation of the file
	 * @return
	 */
	public abstract Versions createVersionsFor(VFSLeaf leaf, boolean force);

	/**
	 * Return the list of deleted files in this container.
	 * 
	 * @param container
	 * @return
	 */
	public abstract List<Versions> getDeletedFiles(VFSContainer container);

	/**
	 * Only used internally
	 * 
	 * @param versions
	 * @return
	 */
	public abstract String getNextRevisionNr(Versions versions);

	/**
	 * Add a new version of the file. The current version will be saved and
	 * secured, The new version replaced the old one.
	 * 
	 * @param currentVersion
	 * @param author
	 * @param comment
	 * @param newVersion
	 * @return
	 */
	public abstract boolean addVersion(Versionable currentVersion, Identity author, String comment, InputStream newVersion);

	/**
	 * Add a new revision to the files. The method check the number of revisions against the absolute
	 * maximum limit for the instance.
	 * @param currentVersion
	 * @param author
	 * @param comment
	 * @return
	 */
	public abstract boolean addToRevisions(Versionable currentVersion, Identity author, String comment);
	
	/**
	 * Move a versioned file to the target container
	 * 
	 * @param currentVersion
	 * @param target container
	 * @return
	 */
	public abstract boolean move(Versionable currentVersion, VFSContainer container);
	
	/**
	 * Move a versioned file to an other (WebDAV only!!!)
	 * 
	 * @param currentVersion
	 * @param oldVersion
	 * @return
	 */
	public abstract boolean move(VFSLeaf currentFile, VFSLeaf targetFile, Identity author);

	/**
	 * Restore a versioned file to the selected revision. The current version is
	 * secured before being replaced by the revision's file
	 * 
	 * @param currentVersion
	 * @param version
	 * @return
	 */
	public abstract boolean restore(Versionable currentVersion, VFSRevision version, String comment);

	/**
	 * Restore a revision in the target container, usefull to restore deleted
	 * files
	 * 
	 * @param target container
	 * @param selected revision
	 * @return
	 */
	public abstract boolean restore(VFSContainer container, VFSRevision revision);

	/**
	 * Delete a list of revisions from a file
	 * 
	 * @param currentVersion
	 * @param revisionsToDelete
	 * @return
	 */
	public abstract boolean deleteRevisions(Versionable currentVersion, List<VFSRevision> revisionsToDelete);
	
	/**
	 * Delete and remove from versioning a list of deleted versions (files)
	 * @param versions
	 * @return
	 */
	public abstract boolean deleteVersions(List<Versions> versions);

	/**
	 * Delete a full container
	 * 
	 * @param container
	 * @param force, if true delete it definitely (the deleted files don't appear
	 *          in the list of deleted files)
	 * @return
	 */
	public abstract boolean delete(VFSItem item, boolean force);

	/**
	 * Rename a file and propagate the change to the version.
	 * 
	 * @param item
	 * @param newname
	 * @return
	 */
	public abstract boolean rename(VFSItem item, String newname);
	
	/**
	 * @return The list of orphans
	 */
	public abstract List<OrphanVersion> orphans();
	
	/**
	 * @param orphan
	 * @return
	 */
	public abstract boolean delete(OrphanVersion orphan);
	
	/**
	 * Delete the orphans
	 * @return
	 */
	public abstract boolean deleteOrphans(ProgressDelegate progress);
	
	
	public abstract void pruneHistory(long historyLength, ProgressDelegate progress);

	
	public abstract int countDirectories();
}
