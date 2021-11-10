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
package org.olat.core.commons.services.vfs;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.license.License;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 11 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface VFSRepositoryService {
	
	public VFSMetadata getMetadataFor(VFSItem path);
	
	public VFSMetadata getMetadataFor(File file);
	
	/**
	 * Resolve the context type for the given metadata. This method is "light
	 * weight" and does not trigger database queries
	 * 
	 * @param relativePath The vfs metadada relative path for which the context is
	 *                     resolved
	 * @param locale       The users locale
	 * @return A localized string or "Unknown" if not resolved
	 */
	public String getContextTypeFor(String relativePath, Locale locale);

	/**
	 * Resolve and build the detailed context information for the given metadata.
	 * This methods is "heavy weight" and might user multiple database queries to
	 * lookup all information. Use this only to lookup individual items.
	 * 
	 * @param relativePath The vfs metadada relative path for which the context is
	 *                     resolved
	 * @param locale       The users locale
	 * @return The resolved and localized context or the VFSContextInfoUnknown
	 */
	public VFSContextInfo getContextInfoFor(String relativePath, Locale locale);
	
	public VFSMetadata getMetadata(VFSMetadataRef ref);
	
	public VFSMetadata getMetadataByUUID(String uuid);
	
	public VFSItem getItemFor(VFSMetadata metdata);
	
	public VFSItem getItemFor(String uuid);
	
	/**
	 * The list of direct children in the specified
	 * relative path.
	 * 
	 * @param relativePath The relative path
	 * @return A list of metadata
	 */
	public List<VFSMetadata> getChildren(String relativePath);
	
	/**
	 * The list of direct children in the directory
	 * specified by the metadata.
	 * 
	 * @param metadata The metadata of the directory
	 * @return A list of metadata
	 */
	public List<VFSMetadata> getChildren(VFSMetadataRef parentMetadata);
	
	
	public void cleanMetadatas();
	
	public List<VFSMetadata> getMostDownloaded(VFSMetadata ancestorMetadata, int maxResults);
	
	/**
	 * 
	 * @param ancestorMetadata
	 * @param maxResults
	 * @return
	 */
	public List<VFSMetadata> getNewest(VFSMetadata ancestorMetadata, int maxResults);
	
	public void itemSaved(VFSLeaf leaf, Identity savedBy);
	
	public VFSMetadata updateMetadata(VFSMetadata data);
	
	/**
	 * The file is marked as deleted, but stay in the file system.
	 * 
	 * @param item The item to mark as deleted
	 * @param author The person wo deleted the file
	 */
	public void markAsDeleted(VFSItem item, Identity author);
	
	/**
	 * Delete the metadata (but not the file), the thumbnails and versions
	 * attached to this metadata.
	 * 
	 * @param data The metadata to remove
	 */
	public int deleteMetadata(VFSMetadata data);
	
	/**
	 * Delete the metadata (but not the file), the thumbnails and versions
	 * attached to this metadata.
	 * 
	 * @param data The metadata to remove
	 */
	public void deleteMetadata(File file);

	/**
	 * Copy the metadata from one file to the other.
	 * 
	 * @param source The source file
	 * @param target The target file
	 * @param parentTarget The parent container of the target (useful if the target is not fully initialized)
	 * @param savedBy 
	 */
	public void copyTo(VFSLeaf source, VFSLeaf target, VFSContainer parentTarget, Identity savedBy);
	
	/**
	 * This rename the metadata and the versions but not the file itself.
	 * 
	 * @param item The item which is renamed.
	 * @param newName
	 * @return
	 */
	public VFSMetadata rename(VFSItem item, String newName);
	
	public void increaseDownloadCount(VFSLeaf item);
	
	/**
	 * Copy the binaries data saved in ZIP files.
	 * 
	 * @param metadata The metadata
	 * @param binaries The binaries
	 */
	public void copyBinaries(VFSMetadata metadata, InputStream in);
	
	
	public boolean isThumbnailAvailable(VFSItem item);
	
	/**
	 * Use this if you have already collected the metadata has it is
	 * database query free.
	 * 
	 * @param item The file (mandatory)
	 * @param metadata The metadata object (mandatory)
	 * @return true if the thumbnail can eventually be generated or are available
	 */
	public boolean isThumbnailAvailable(VFSItem item, VFSMetadata metadata);
	
	public VFSLeaf getThumbnail(VFSLeaf file, int maxWidth, int maxHeight, boolean fill);
	
	/**
	 * This method prevent reloading the metadata
	 * 
	 * @param file The file
	 * @param metadata The metadata of the file if you already have it
	 * @param maxWidth
	 * @param maxHeight
	 * @param fill
	 * @return
	 */
	public VFSLeaf getThumbnail(VFSLeaf file, VFSMetadata metadata, int maxWidth, int maxHeight, boolean fill);
	
	public void resetThumbnails(VFSLeaf file);
	
	/**
	 * If the specified file is a directory, it will recursively
	 * reset the thumbnails of all files.
	 * 
	 * @param file A file in /bcroot/
	 */
	public void resetThumbnails(File file);
	
	/**
	 * Get the list of revisions for a specific file.
	 * 
	 * @param metadata The metadata object of the file.
	 * @return A list of revisions if the file is versioned.
	 */
	public List<VFSRevision> getRevisions(VFSMetadataRef metadata);
	
	public List<VFSRevision> getRevisions(List<VFSMetadataRef> metadatas);
	
	public VFSRevision getRevision(VFSRevisionRef ref);
	
	/**
	 * 
	 * @return the size in bytes
	 */
	public long getRevisionsTotalSize();
	
	public long getRevisionsTotalSizeOfDeletedFiles();
	
	/**
	 * @return A list of metadata references of deleted files with revisions.
	 */
	public List<VFSMetadataRef> getMetadataOfDeletedFiles();
	
	/**
	 * @return A list of metadata references of deleted files with revisions.
	 */
	public List<VFSRevision> getRevisionsOfDeletedFiles();
	
	/**
	 * @return A list of metadata references of deleted files with more than the specified number of revisions.
	 */
	public List<VFSMetadataRef> getMetadataWithMoreRevisionsThan(long numOfRevs);
	
	/**
	 * The current file will be added in the version history. The content of the
	 * specified input stream will replace the content in the current file.
	 * 
	 * Temporary versions are always "on top" of the versions stack.
	 * If a stable version is added, all temporary versions are deleted.
	 * 
	 * @param currentFile The file
	 * @param identity The acting identity
	 * @param tempVersion indicated whether it is a temporary version
	 * @param comment A comment
	 * @param newFile The new content for the current file
	 * @return true if successful
	 */
	public boolean addVersion(VFSLeaf currentFile, Identity identity, boolean tempVersion, String comment,
			InputStream newFile);

	/**
	 * Restore the specified revision and replace the current file. If the
	 * current file doesn't exist, it will recreate it.
	 * 
	 * @param identity The identity who make the operation
	 * @param revision The revision to restore
	 * @param comment A comment
	 * @return true if successful
	 */
	public boolean restoreRevision(Identity identity, VFSRevision revision, String comment);

	/**
	 * Delete definitively the revisions of a file.
	 * 
	 * @param identity The identity who makes the operation
	 * @param revisions The revisions to delete
	 * @return true if successful
	 */
	public boolean deleteRevisions(Identity identity, List<VFSRevision> revisions);
	
	/**
	 * Move the metadata and revisions from a path to the other.
	 * 
	 * @param currentFile The current file
	 * @param targetFile The target file where to move the metadata
	 * @param author The user which moved the data
	 * @return The merged metadata
	 */
	public VFSMetadata move(VFSLeaf currentFile, VFSLeaf targetFile, Identity author);
	
	public void migrate(VFSContainer container, VFSMetadata metadata);
	
	public File getRevisionFile(VFSRevision revision);
	
	
	public License getLicense(VFSMetadata meta);
	
	public License getOrCreateLicense(VFSMetadata meta, Identity itentity);
	
	public VFSStatistics getStatistics(boolean recalculateSizes);
	
	/**
	 * Returns the largest files from the VFS
	 * 
	 * @param maxResults
	 * @return
	 */
	public List<VFSMetadata> getLargestFiles(int maxResult, 
			Date createdAtNewer, Date createdAtOlder, 
			Date editedAtNewer, Date editedAtOlder, 
			Date lockedAtNewer, Date lockedAtOlder,
			String trashed, String locked,
			Integer downloadCount, Long revisionCount, 
			Integer size);
	
	/**
	 * Returns the largest revisions from the VFS
	 * 
	 * @param maxResults
	 * @return
	 */
	public List<VFSRevision> getLargestRevisions(int maxResult, 
			Date createdAtNewer, Date createdAtOlder, 
			Date editedAtNewer, Date editedAtOlder, 
			Date lockedAtNewer, Date lockedAtOlder,
			String trashed, String locked,
			Integer downloadCount, Long revisionCount,
			Integer Size);
	
	

}
