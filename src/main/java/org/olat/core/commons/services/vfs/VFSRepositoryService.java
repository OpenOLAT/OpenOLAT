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
import java.util.List;
import java.util.Optional;

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
	
	public void itemSaved(VFSLeaf leaf);
	
	public VFSMetadata updateMetadata(VFSMetadata data);
	
	public void deleteMetadata(VFSMetadata data);
	
	public void deleteMetadata(File file);

	/**
	 * Copy the metadata from one file to the other.
	 * 
	 * @param source The source file
	 * @param target The target file
	 * @param parentTarget The parent container of the target (useful if the target is not fully initialized)
	 */
	public void copyTo(VFSLeaf source, VFSLeaf target, VFSContainer parentTarget);
	
	public VFSMetadata rename(VFSMetadata data, String newName);
	
	public void increaseDownloadCount(VFSItem item);
	
	/**
	 * Copy the binaries data saved in ZIP files.
	 * 
	 * @param metadata The metadata
	 * @param binaries The binaries
	 */
	public void copyBinaries(VFSMetadata metadata, byte[] binaries);
	
	
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
	
	
	
	public License getLicense(VFSMetadata meta);
	
	public License getOrCreateLicense(VFSMetadata meta, Identity itentity);
	

	public boolean hasEditor(String suffix);
	
	public Optional<VFSLeafEditor> getEditor(String editorType);
	
	/**
	 * Get all enabled editors which support the file of the vfsLeaf. Support means usually edit or read.
	 *
	 * @param vfsLeaf
	 * @return
	 */
	public List<VFSLeafEditor> getEditors(VFSLeaf vfsLeaf);

}
