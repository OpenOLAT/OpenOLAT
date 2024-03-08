/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.folder.ui;

import java.io.File;
import java.util.Date;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.JavaIOItem;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 24 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FolderUIFactory {
	
	public static String getDisplayName(VFSItem vfsItem) {
		VFSMetadata vfsMetadata = vfsItem.getMetaInfo();
		if (vfsMetadata != null) {
			String title = vfsMetadata.getTitle();
			if (StringHelper.containsNonWhitespace(title)) {
				return title;
			}
		}
		
		return vfsItem.getName();
	}

	public static String getCreatedBy(UserManager userManager, VFSItem vfsItem) {
		if (vfsItem instanceof VFSLeaf vfsLeaf) {
			VFSMetadata vfsMetadata = vfsItem.getMetaInfo();
			if (vfsMetadata != null) {
				Identity fileInitializedBy = vfsMetadata.getFileInitializedBy();
				if (fileInitializedBy != null) {
					return userManager.getUserDisplayName(fileInitializedBy.getKey());
				}
			}
		}
		return null;
	}

	public static Date getLastModifiedDate(VFSItem vfsItem) {
		VFSMetadata vfsMetadata = vfsItem.getMetaInfo();
		if (vfsMetadata != null) {
			Date fileLastModified = vfsMetadata.getFileLastModified();
			if (fileLastModified != null) {
				return fileLastModified;
			}
		}
		return new Date(vfsItem.getLastModified());
	}

	public static String getLastModifiedBy(UserManager userManager, VFSItem vfsItem) {
		VFSMetadata vfsMetadata = vfsItem.getMetaInfo();
		if (vfsMetadata != null) {
			Identity fileLastModifiedBy = vfsMetadata.getFileLastModifiedBy();
			if (fileLastModifiedBy != null) {
				return userManager.getUserDisplayName(fileLastModifiedBy.getKey());
			}
		}
		return null;
	}

	public static String getModified(Formatter formatter, Date lastModifiedDate, String lastModifiedBy) {
		String modified  = null;
		if (lastModifiedDate != null) {
			modified = formatter.formatDateAndTime(lastModifiedDate);
			if (StringHelper.containsNonWhitespace(lastModifiedBy)) {
				modified += " - " + lastModifiedBy;
			}
		}
		return modified;
	}

	private static String getFilename(VFSLeaf vfsLeaf) {
		String filename = null;
		VFSMetadata vfsMetadata = vfsLeaf.getMetaInfo();
		if (vfsMetadata != null) {
			filename = vfsMetadata.getFilename();
		}
		if (!StringHelper.containsNonWhitespace(filename)) {
			if (vfsLeaf instanceof JavaIOItem ioItem) {
				File basefile = ioItem.getBasefile();
				if (basefile != null) {
					filename = basefile.getName();
				}
			}
		}
		return filename;
	}

	public static String getFileSuffix(VFSItem vfsItem) {
		if (vfsItem instanceof VFSLeaf vfsLeaf) {
			String filename = getFilename(vfsLeaf);
			if (StringHelper.containsNonWhitespace(filename)) {
				String fileSuffix = FileUtils.getFileSuffix(filename);
				if (StringHelper.containsNonWhitespace(fileSuffix)) {
					return fileSuffix.toLowerCase();
				}
			}
		}
		return null;
	}

	public static String getTranslatedType(Translator translator, VFSItem vfsItem) {
		if (vfsItem instanceof VFSContainer vfsContainer) {
			return translator.translate("type.container");
		} else if (vfsItem instanceof VFSLeaf vfsLeaf) {
			String filename = getFilename(vfsLeaf);
			if (StringHelper.containsNonWhitespace(filename)) {
				String fileSuffix = FileUtils.getFileSuffix(filename);
				if (StringHelper.containsNonWhitespace(fileSuffix)) {
					return translator.translate("type.leaf.suffix", fileSuffix.toLowerCase());
				}
				return translator.translate("type.leaf");
			}
		}
		return null;
	}

	public static Long getSize(VFSItem vfsItem) {
		if (vfsItem instanceof VFSContainer vfsContainer) {
			return Long.valueOf(vfsContainer.getItems(new VFSSystemItemFilter()).size());
		} else if (vfsItem instanceof VFSLeaf vfsLeaf) {
			VFSMetadata vfsMetadata = vfsLeaf.getMetaInfo();
			if (vfsMetadata != null) {
				return vfsMetadata.getFileSize();
			}
		}
		return null;
	}

	public static String getTranslatedSize(Translator translator, VFSItem vfsItem, Long size) {
		if (vfsItem instanceof VFSContainer vfsContainer) {
			int numFiles = vfsContainer.getItems(new VFSSystemItemFilter()).size();
			return translator.translate("elements", String.valueOf(numFiles));
		} else if (vfsItem instanceof VFSLeaf vfsLeaf) {
			if (size != null) {
				return Formatter.formatBytes(size);
			}
		}
		return null;
	}

	public static Long getVersions(VFSItem vfsItem) {
		if (vfsItem instanceof VFSLeaf vfsLeaf) {
			VFSMetadata vfsMetadata = vfsLeaf.getMetaInfo();
			if (vfsMetadata != null) {
				return vfsMetadata.getRevisionNr();
			}
		}
		return null;
	}

}
