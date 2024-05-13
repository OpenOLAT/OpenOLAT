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
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.AbstractVirtualContainer;
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
	
	public static String getIconCssClass(VFSMetadata vfsMetadata, VFSItem vfsItem) {
		if (vfsItem instanceof VFSContainer vfsContainer) {
			return vfsContainer.getIconCSS();
		} else if (vfsMetadata != null && vfsMetadata.isDirectory()) {
			return "o_filetype_folder";
		}
		return CSSHelper.createFiletypeIconCssClassFor(getFilename(vfsMetadata, (VFSLeaf)vfsItem));
	}
	
	public static String getDisplayName(VFSMetadata vfsMetadata, VFSItem vfsItem) {
		if (vfsItem instanceof AbstractVirtualContainer virtualContainer) {
			return virtualContainer.getName();
		}
		
		if (vfsMetadata != null) {
			if (StringHelper.containsNonWhitespace(vfsMetadata.getTitle())) {
				return vfsMetadata.getTitle();
			}
			if (StringHelper.containsNonWhitespace(vfsMetadata.getFilename())) {
				return vfsMetadata.getFilename();
			}
		}
		
		if (vfsItem != null) {
			return vfsItem.getName();
		}
		
		return null;
	}

	public static String getCreatedBy(UserManager userManager, VFSMetadata vfsMetadata) {
		if (vfsMetadata != null && !vfsMetadata.isDirectory()) {
			Identity fileInitializedBy = vfsMetadata.getFileInitializedBy();
			if (fileInitializedBy != null) {
				return userManager.getUserDisplayName(fileInitializedBy.getKey());
			}
		}
		return null;
	}

	public static Date getLastModifiedDate(VFSMetadata vfsMetadata, VFSItem vfsItem) {
		if (vfsMetadata != null) {
			Date fileLastModified = vfsMetadata.getFileLastModified();
			if (fileLastModified != null) {
				return fileLastModified;
			}
		}
		if (vfsItem != null && vfsItem.getLastModified() > 0) {
			return new Date(vfsItem.getLastModified());
		}
		
		return null;
	}

	public static String getLastModifiedBy(UserManager userManager, VFSMetadata vfsMetadata) {
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

	public static Date getDeletedDate(VFSMetadata vfsMetadata, VFSRevision vfsRevision) {
		if (vfsMetadata != null && vfsMetadata.getDeletedDate() != null) {
			return vfsMetadata.getDeletedDate();
		}
		if (vfsRevision != null && vfsRevision.getCreationDate() != null) {
			return vfsRevision.getCreationDate();
		}
		return null;
	}

	public static String getDeletedBy(UserManager userManager, VFSMetadata vfsMetadata, VFSRevision vfsRevision) {
		if (vfsMetadata != null && vfsMetadata.getDeletedBy() != null) {
			return userManager.getUserDisplayName(vfsMetadata.getDeletedBy().getKey());
		}
		if (vfsRevision != null && vfsRevision.getFileInitializedBy() != null) {
			return userManager.getUserDisplayName(vfsRevision.getFileInitializedBy().getKey());
		}
		return null;
	}

	private static String getFilename(VFSMetadata vfsMetadata, VFSLeaf vfsLeaf) {
		String filename = null;
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

	public static String getFileSuffix(VFSMetadata vfsMetadata, VFSItem vfsItem) {
		if (vfsItem instanceof VFSLeaf vfsLeaf) {
			String filename = getFilename(vfsMetadata, vfsLeaf);
			if (StringHelper.containsNonWhitespace(filename)) {
				String fileSuffix = FileUtils.getFileSuffix(filename);
				if (StringHelper.containsNonWhitespace(fileSuffix)) {
					return fileSuffix.toLowerCase();
				}
			}
		}
		return null;
	}

	public static String getTranslatedType(Translator translator, VFSMetadata vfsMetadata, VFSItem vfsItem) {
		if (vfsMetadata != null && vfsMetadata.isDirectory()) {
			return translator.translate("type.container");
		} else if (vfsItem instanceof VFSContainer){
			return translator.translate("type.container");
		}
		
		String filename = getFilename(vfsMetadata, (VFSLeaf)vfsItem);
		if (StringHelper.containsNonWhitespace(filename)) {
			String fileSuffix = FileUtils.getFileSuffix(filename);
			if (StringHelper.containsNonWhitespace(fileSuffix)) {
				return fileSuffix.toLowerCase();
			}
		}
		
		return null;
	}

	public static Long getSize(VFSMetadata vfsMetadata, VFSItem vfsItem) {
		if (vfsItem instanceof VFSContainer vfsContainer) {
			return Long.valueOf(vfsContainer.getItems(new VFSSystemItemFilter()).size());
		} else if (vfsItem instanceof VFSLeaf vfsLeaf) {
			if (vfsMetadata != null) {
				return vfsMetadata.getFileSize();
			}
		}
		return null;
	}

	public static String getTranslatedSize(Translator translator, VFSItem vfsItem, Long size) {
		if (size != null) {
			if (vfsItem instanceof VFSContainer vfsContainer) {
				return translator.translate("elements", String.valueOf(size));
			} else if (vfsItem instanceof VFSLeaf vfsLeaf) {
				return Formatter.formatBytes(size);
				}
			}
		return null;
	}

	public static Long getVersions(VFSMetadata vfsMetadata) {
		if (vfsMetadata != null && !vfsMetadata.isDirectory()) {
			return vfsMetadata.getRevisionNr();
		}
		return null;
	}

	public static boolean isNew(Date refDate, VFSMetadata vfsMetadata, VFSItem vfsItem) {
		if (vfsMetadata != null && vfsMetadata.getFileLastModified() != null && refDate.before(vfsMetadata.getFileLastModified())) {
			return true;
		}
		if (vfsItem != null && vfsItem.getLastModified() > 0 && refDate.before(new Date(vfsItem.getLastModified()))) {
			return true;
		}
		return false;
	}

}
