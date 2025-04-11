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
package org.olat.user.manager;

import java.io.File;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.services.image.ImageService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.user.PortraitSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: Apr 9, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class UserPortraitStorage {
	
	private static final String PREFIX_MASTER = "master";
	
	private static final Logger log = Tracing.createLoggerFor(UserPortraitStorage.class);
	
	@Autowired
	private ImageService imageService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	@PostConstruct
	public void initFolders() {
		File bcrootDirectory = new File(FolderConfig.getCanonicalRoot());
		File portraitsDirectory = new File(bcrootDirectory, "userportraits");
		if (!portraitsDirectory.exists()) {
			portraitsDirectory.mkdirs();
		}
	}
	
	private VFSContainer getPortraitsContainer() {
		return VFSManager.olatRootContainer("/userportraits");
	}
	
	public String store(Identity doer, File file, String filename) {
		if (file == null || !file.exists() || !file.isFile()) {
			return null;
		}
		
		String imagePath = UUID.randomUUID().toString().replace("-", "");
		try {
			tryToStore(doer, imagePath, file, filename);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
		
		return imagePath;
	}

	void tryToStore(Identity doer, String imagePath, File file, String filename) {
		VFSContainer imageContainer = createImageContainer(imagePath);
		tryToStore(doer, imageContainer, file, filename);
	}

	private void tryToStore(Identity doer, VFSContainer imageContainer, File file, String filename) {
		String suffix = FileUtils.getFileSuffix(filename);
		if (!StringHelper.containsNonWhitespace(suffix)) {
			suffix = "png";
		}
		String cleandFilename = PREFIX_MASTER + "." + suffix;
		VFSLeaf vfsLeaf = VFSManager.resolveOrCreateLeafFromPath(imageContainer, cleandFilename);
		VFSManager.copyContent(file, vfsLeaf, doer);
	}
	
	private VFSContainer createImageContainer(String imagePath) {
		VFSContainer portraitsContainer = getPortraitsContainer();
		String firstToken = getIndexPath(imagePath);
		VFSContainer indexContainer = VFSManager.getOrCreateContainer(portraitsContainer, firstToken);
		return VFSManager.getOrCreateContainer(indexContainer, imagePath);
	}

	public void delete(String imagePath) {
		VFSContainer portraitsContainer = getPortraitsContainer();
		String firstToken = getIndexPath(imagePath);
		VFSItem indexItem = portraitsContainer.resolve(firstToken);
		if (indexItem instanceof VFSContainer indexConatiner && indexConatiner.exists()) {
			VFSItem imageItem = indexConatiner.resolve(imagePath);
			if (imageItem instanceof VFSContainer imageContainer) {
				imageContainer.deleteSilently();
				
				// Delete empty index containers as well
				if (indexConatiner.getItems(new VFSSystemItemFilter()).isEmpty()) {
					indexConatiner.deleteSilently();
				}
			}
		}
	}

	private String getIndexPath(String imagePath) {
		return imagePath.substring(0, 2).toLowerCase();
	}

	public VFSLeaf getImage(String imagePath, PortraitSize portraitSize) {
		if (!StringHelper.containsNonWhitespace(imagePath)) {
			return null;
		}
		
		VFSContainer imageContainer = getImageContainer(imagePath);
		if (imageContainer == null) {
			return null;
		}
		
		if (portraitSize == null) {
			return getImage(imageContainer, PREFIX_MASTER);
		}
		
		String prefix = getFilePrefix(portraitSize);
		VFSLeaf imageLeaf = getImage(imageContainer, prefix);
		if (imageLeaf == null) {
			imageLeaf = createImage(imageContainer, portraitSize);
		}
		if (imageLeaf == null) {
			imageLeaf = getImage(imageContainer, PREFIX_MASTER);
		}
		
		return imageLeaf;
	}

	private VFSLeaf createImage(VFSContainer imageContainer, PortraitSize portraitSize) {
		VFSLeaf masterLeaf = getImage(imageContainer, PREFIX_MASTER);
		if (masterLeaf == null || !masterLeaf.exists()) {
			return null;
		}
		
		String suffix = FileUtils.getFileSuffix(masterLeaf.getName());
		if (!StringHelper.containsNonWhitespace(suffix)) {
			suffix = "png";
		}
		String filename = getFilePrefix(portraitSize) + "." + suffix;
		VFSLeaf imageLeaf = imageContainer.createChildLeaf(filename);
		
		int imageSize = getImageSize(portraitSize);
		Size size = imageService.scaleImage(masterLeaf, imageLeaf, imageSize, imageSize, false);
		if (size != null) {
			//The resized image is saved with a delay, but still has the same creator as the master file.
			VFSMetadata masterMetadata = masterLeaf.getMetaInfo();
			if (masterMetadata != null && masterMetadata.getFileInitializedBy() != null) {
				vfsRepositoryService.itemSaved(imageLeaf, masterMetadata.getFileInitializedBy());
			}
		} else {
			imageLeaf.deleteSilently();
			return null;
		}
		
		return imageLeaf;
	}

	private VFSLeaf getImage(VFSContainer imageContainer, String prefix) {
		List<VFSItem> items = imageContainer.getItems(new PortraitFilter(prefix));
		if (!items.isEmpty() && items.get(0) instanceof VFSLeaf imageLeaf) {
			return imageLeaf;
		}
		return null;
	}
	
	VFSContainer getImageContainer(String imagePath) {
		VFSContainer portraitsContainer = getPortraitsContainer();
		String firstToken = getIndexPath(imagePath);
		VFSItem indexItem = portraitsContainer.resolve(firstToken);
		if (indexItem instanceof VFSContainer indexConatiner && indexConatiner.exists()) {
			VFSItem imageItem = indexConatiner.resolve(imagePath);
			if (imageItem instanceof VFSContainer imageContainer && imageContainer.exists()) {
				return imageContainer;
			}
		}
		return null;
	}
	
	private String getFilePrefix(PortraitSize portraitSize) {
		String imageSizeStr = String.valueOf(getImageSize(portraitSize));
		return imageSizeStr + "_" + imageSizeStr;
	}
	
	/*
	 * The size of the image is twice as large as the size of the display in the
	 * browser (see CSS), so that high definition screens are optimally supported.
	 */
	private int getImageSize(PortraitSize portraitSize) {
		return switch (portraitSize) {
		case large -> 200;
		case medium -> 100;
		case small -> 60;
		case xsmall -> 30;
		};
	}

	private static class PortraitFilter implements VFSItemFilter {

		private final String prefix;

		public PortraitFilter(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public boolean accept(VFSItem vfsItem) {
			String name = vfsItem.getName();
			return name.startsWith(prefix) && name.contains(".");
		}
		
	}

}
