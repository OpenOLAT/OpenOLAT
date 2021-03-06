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

import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.style.ColorCategory;
import org.olat.course.style.ColorCategoryRef;
import org.olat.course.style.ColorCategoryResolver;
import org.olat.course.style.ColorCategorySearchParams;
import org.olat.course.style.CourseStyleService;
import org.olat.course.style.ImageSource;
import org.olat.course.style.ImageSourceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CourseStyleServiceImpl implements CourseStyleService {
	
	@Autowired
	private SystemImageStorage systemImageStorage;
	@Autowired
	private CourseImageStorage courseImageStorage;
	@Autowired
	private ColorCategoryDAO colorCategoryDao;
	
	@Override
	public void initProvidedSystemImages() throws Exception {
		systemImageStorage.initProvidedSystemImages();
	}
	
	@Override
	public void storeSystemImage(File file) {
		systemImageStorage.store(file);
	}
	
	@Override
	public List<ImageSource> getSystemTeaserImageSources() {
		return systemImageStorage.loadAll();
	}
	
	@Override
	public File getSystemTeaserImageFile(String filename) {
		return systemImageStorage.load(filename);
	}

	@Override
	public ImageSource getSystemTeaserImageSource(String filename) {
		return systemImageStorage.createImageSource(filename);
	}

	@Override
	public ImageSource storeImage(ICourse course, Identity savedBy, File file, String filename) {
		return courseImageStorage.store(course.getCourseBaseContainer(), savedBy, file, filename);
	}
	
	@Override
	public VFSLeaf getImage(ICourse course) {
		return courseImageStorage.load(course.getCourseBaseContainer());
	}
	
	@Override
	public void deleteImage(ICourse course) {
		courseImageStorage.delete(course.getCourseBaseContainer());
	}

	@Override
	public ImageSource storeImage(ICourse course, CourseNode courseNode, Identity savedBy, File file, String filename) {
		return courseImageStorage.store(course.getCourseBaseContainer(), courseNode, savedBy, file, filename);
	}

	@Override
	public VFSLeaf getImage(ICourse course, CourseNode courseNode) {
		return courseImageStorage.load(course.getCourseBaseContainer(), courseNode);
	}

	@Override
	public void deleteImage(ICourse course, CourseNode courseNode) {
		courseImageStorage.delete(course.getCourseBaseContainer(), courseNode);
	}

	@Override
	public VFSMediaMapper getTeaserImageMapper(CourseEnvironment courseEnv, CourseNode courseNode) {
		ImageSource teaserImageSource = courseNode.getTeaserImageSource() != null
				? courseNode.getTeaserImageSource()
				: courseEnv.getCourseConfig().getTeaserImageSource();
		if (teaserImageSource == null) return null;
		
		VFSMediaMapper mapper = null;
		if (ImageSourceType.course == teaserImageSource.getType()) {
			VFSLeaf vfsLeaf = courseImageStorage.load(courseEnv.getCourseBaseContainer());
			if (vfsLeaf != null) {
				mapper = new VFSMediaMapper(vfsLeaf);
			}
		} else if (ImageSourceType.courseNode == teaserImageSource.getType()) {
			VFSLeaf vfsLeaf = courseImageStorage.load(courseEnv.getCourseBaseContainer(), courseNode);
			if (vfsLeaf != null) {
				mapper = new VFSMediaMapper(vfsLeaf);
			}
		} else if (ImageSourceType.system == teaserImageSource.getType()) {
			File file = systemImageStorage.load(teaserImageSource.getFilename());
			if (file != null) {
				mapper = new VFSMediaMapper(file);
			}
		}
		return mapper;
	}

	@Override
	public ColorCategory createColorCategory(String identifier) {
		ColorCategory colorCategory = colorCategoryDao.loadByIdentifier(identifier);
		return colorCategory != null? colorCategory: colorCategoryDao.create(identifier);
	}
	
	@Override
	public ColorCategory getColorCategory(ColorCategoryRef colorCategoryRef) {
		return colorCategoryDao.loadByKey(colorCategoryRef);
	}

	@Override
	public ColorCategory getColorCategory(String identifier, String fallbackIdentifier) {
		ColorCategory colorCategory = colorCategoryDao.loadByIdentifier(identifier);
		if (colorCategory == null) {
			colorCategory = colorCategoryDao.loadByIdentifier(fallbackIdentifier);
		}
		return colorCategory;
	}

	@Override
	public List<ColorCategory> getColorCategories(ColorCategorySearchParams searchParams) {
		return colorCategoryDao.load(searchParams);
	}

	@Override
	public boolean isColorCategoryIdentifierAvailable(String identifier) {
		return colorCategoryDao.loadByIdentifier(identifier) == null;
	}

	@Override
	public ColorCategory updateColorCategory(ColorCategory colorCategory) {
		return colorCategoryDao.save(colorCategory);
	}
	
	@Override
	public void doMove(ColorCategoryRef colorCategoryRef, boolean up) {
		ColorCategory colorCategory = colorCategoryDao.loadByKey(colorCategoryRef);
		if (colorCategory == null) return;
		
		int sortOrder = colorCategory.getSortOrder();
		// Up in gui is lower in sort order.
		int swapSortOrder = up? sortOrder - 1: sortOrder + 1;
		// Only technical color categories must have negative sort order.
		if (swapSortOrder <= 0) return;
		
		ColorCategory swapColorCategory = colorCategoryDao.loadBySortOrder(swapSortOrder);
		if (swapColorCategory == null) return;
		
		colorCategory.setSortOrder(swapSortOrder);
		swapColorCategory.setSortOrder(sortOrder);
		colorCategoryDao.save(colorCategory);
		colorCategoryDao.save(swapColorCategory);
	}

	@Override
	public void deleteColorCategory(ColorCategory colorCategory) {
		colorCategoryDao.delete(colorCategory);
	}

	@Override
	public ColorCategoryResolver getColorCategoryResolver(ColorCategorySearchParams preloadParams, String courseColorCategoryIdentifier) {
		return new CachingColorCategoryResolver(colorCategoryDao, preloadParams, courseColorCategoryIdentifier);
	}

}
