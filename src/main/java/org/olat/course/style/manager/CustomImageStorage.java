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
package org.olat.course.style.manager;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.style.CourseStyleService;
import org.olat.course.style.ImageSource;
import org.olat.course.style.ImageSourceType;
import org.olat.course.style.model.ImageSourceImpl;
import org.springframework.stereotype.Component;


/**
 * 
 * Initial date: 3 Jul 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class CustomImageStorage {
	
	private static final Logger log = Tracing.createLoggerFor(CustomImageStorage.class);
	
	private static final String BASE_IMAGE_DIR = CourseStyleService.FOLDER_ROOT + "/courseheader";
	private static final String COURSE_IMAGE_DIR = "course";

	public ImageSource store(VFSContainer baseContainer, Identity savedBy, File file, String filename) {
		if (file == null || !file.exists() || !file.isFile()) {
			return null;
		}
		
		String cleanedFilename;
		try {
			VFSContainer imageContainer = getCourseContainer(baseContainer);
			cleanedFilename = tryToStore(imageContainer, savedBy, file, filename);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
		
		ImageSourceImpl imageSource = new ImageSourceImpl();
		imageSource.setType(ImageSourceType.custom);
		imageSource.setFilename(cleanedFilename);
		imageSource.setPath(COURSE_IMAGE_DIR);
		return imageSource;
	}
	
	public ImageSource store(VFSContainer baseContainer, CourseNode courseNode, Identity savedBy, File file, String filename) {
		if (file == null || !file.exists() || !file.isFile()) {
			return null;
		}
		
		String cleanedFilename;
		try {
			VFSContainer imageContainer = getCourseNodeContainer(baseContainer, courseNode);
			cleanedFilename = tryToStore(imageContainer, savedBy, file, filename);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
		
		ImageSourceImpl imageSource = new ImageSourceImpl();
		imageSource.setType(ImageSourceType.custom);
		imageSource.setFilename(cleanedFilename);
		imageSource.setPath(courseNode.getIdent());
		return imageSource;
	}

	public VFSLeaf load(VFSContainer baseContainer) {
		return loadLeaf(getCourseContainer(baseContainer));
	}
	
	public VFSLeaf load(VFSContainer baseContainer, CourseNode courseNode) {
		return loadLeaf(getCourseNodeContainer(baseContainer, courseNode));
	}

	public void delete(VFSContainer baseContainer) {
		getCourseContainer(baseContainer).delete();
	}

	public void delete(VFSContainer baseContainer, CourseNode courseNode) {
		getCourseNodeContainer(baseContainer, courseNode).delete();
	}

	VFSContainer getCourseContainer(VFSContainer baseContainer) {
		return VFSManager.resolveOrCreateContainerFromPath(baseContainer, BASE_IMAGE_DIR + "/" + COURSE_IMAGE_DIR);
	}
	
	VFSContainer getCourseNodeContainer(VFSContainer baseContainer, CourseNode courseNode) {
		return VFSManager.resolveOrCreateContainerFromPath(baseContainer, BASE_IMAGE_DIR + "/" + courseNode.getIdent());
	}

	private String tryToStore(VFSContainer imageContainer, Identity savedBy, File file, String filename) {
		imageContainer.delete();
		
		String cleandFilename = FileUtils.cleanFilename(filename);
		VFSLeaf vfsLeaf = VFSManager.resolveOrCreateLeafFromPath(imageContainer, cleandFilename);
		VFSManager.copyContent(file, vfsLeaf, savedBy);
		return cleandFilename;
	}
	
	private VFSLeaf loadLeaf(VFSContainer container) {
		List<VFSItem> items = container.getItems();
		if (!items.isEmpty()) {
			VFSItem vfsItem = items.get(0);
			if (vfsItem instanceof VFSLeaf) {
				return (VFSLeaf)vfsItem;
			}
		}
		return null;
	}

}
