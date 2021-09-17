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
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeHelper;
import org.olat.course.style.ColorCategory;
import org.olat.course.style.ColorCategoryRef;
import org.olat.course.style.ColorCategoryResolver;
import org.olat.course.style.ColorCategorySearchParams;
import org.olat.course.style.CourseStyleService;
import org.olat.course.style.ImageSource;
import org.olat.course.style.ImageSourceType;
import org.olat.course.style.TeaserImageStyle;
import org.olat.course.style.model.ImageSourceImpl;
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

	private static final Logger log = Tracing.createLoggerFor(CourseStyleServiceImpl.class);
	
	@Autowired
	private SystemImageStorage systemImageStorage;
	@Autowired
	private CustomImageStorage customImageStorage;
	@Autowired
	private ColorCategoryDAO colorCategoryDao;
	@Autowired
	private CourseModule courseModule;
	
	@PostConstruct
	public void initProvidedSystemImages() throws Exception {
		Set<String> systemImagesProvided = courseModule.getSystemImagesProvided();
		if (!systemImagesProvided.contains("oo_16_0_1")) {
			try {
				systemImageStorage.initProvidedSystemImages("oo_16_0_1");
				systemImagesProvided.add("oo_16_0_1");
				courseModule.setSystemImages(systemImagesProvided);
				log.info("System images 16.0.1 initialized");
			} catch (Exception e) {
				log.error("System images 16.0.1 not initialized", e);
			}
		}
	}
	
	@Override
	public void storeSystemImage(File file, String filename) {
		systemImageStorage.store(file, filename);
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
	public boolean existsSystemImage(String filename) {
		return systemImageStorage.exists(filename);
	}
	
	@Override
	public void deleteSystemImage(String filename) {
		systemImageStorage.delete(filename);
	}

	@Override
	public ImageSource createEmptyImageSource(ImageSourceType type) {
		ImageSourceImpl imageSource = new ImageSourceImpl();
		imageSource.setType(type);
		return imageSource;
	}

	@Override
	public ImageSource storeImage(ICourse course, Identity savedBy, File file, String filename) {
		return customImageStorage.store(course.getCourseBaseContainer(), savedBy, file, filename);
	}
	
	@Override
	public VFSLeaf getImage(ICourse course) {
		return customImageStorage.load(course.getCourseBaseContainer());
	}
	
	@Override
	public void deleteImage(ICourse course) {
		customImageStorage.delete(course.getCourseBaseContainer());
	}

	@Override
	public ImageSource storeImage(ICourse course, CourseNode courseNode, Identity savedBy, File file, String filename) {
		return customImageStorage.store(course.getCourseBaseContainer(), courseNode, savedBy, file, filename);
	}

	@Override
	public VFSLeaf getImage(ICourse course, CourseNode courseNode) {
		return customImageStorage.load(course.getCourseBaseContainer(), courseNode);
	}

	@Override
	public void deleteImage(ICourse course, CourseNode courseNode) {
		customImageStorage.delete(course.getCourseBaseContainer(), courseNode);
	}
	
	@Override
	public VFSMediaMapper getTeaserImageMapper(ICourse course) {
		ImageSourceType type = course.getCourseConfig().getTeaserImageSource() != null
				? course.getCourseConfig().getTeaserImageSource().getType()
				: ImageSourceType.DEFAULT_COURSE;
		
		VFSMediaMapper mapper = null;
		if (ImageSourceType.custom == type) {
			VFSLeaf vfsLeaf = customImageStorage.load(course.getCourseBaseContainer());
			if (vfsLeaf != null) {
				mapper = new VFSMediaMapper(vfsLeaf);
			}
		} else if (ImageSourceType.system == type) {
			File file = systemImageStorage.load(course.getCourseConfig().getTeaserImageSource().getFilename());
			if (file != null) {
				mapper = new VFSMediaMapper(file);
			}
		}
		return mapper;
	}

	@Override
	public VFSMediaMapper getTeaserImageMapper(ICourse course, INode node) {
		CourseNode courseNode = CourseNodeHelper.getCourseNode(node);
		ImageSourceType type = courseNode.getTeaserImageSource() != null
				? courseNode.getTeaserImageSource().getType()
				: ImageSourceType.DEFAULT_COURSE_NODE;
		
		VFSMediaMapper mapper = null;
		if (ImageSourceType.course == type) {
			mapper = getTeaserImageMapper(course);
		} else if (ImageSourceType.custom == type) {
			VFSLeaf vfsLeaf = customImageStorage.load(course.getCourseBaseContainer(), courseNode);
			if (vfsLeaf != null) {
				mapper = new VFSMediaMapper(vfsLeaf);
			}
		} else if (ImageSourceType.system == type) {
			File file = systemImageStorage.load(courseNode.getTeaserImageSource().getFilename());
			if (file != null) {
				mapper = new VFSMediaMapper(file);
			}
		} else if (ImageSourceType.inherited == type) {
			if (node.getParent() != null) {
				mapper = getTeaserImageMapper(course, node.getParent());
			} else {
				//No parent = root. On top of the root is the course.
				mapper = getTeaserImageMapper(course);
			}
		}
		return mapper;
	}
	
	@Override
	public TeaserImageStyle getTeaserImageStyle(ICourse course, INode node) {
		CourseNode courseNode = CourseNodeHelper.getCourseNode(node);
		TeaserImageStyle teaserImageStyle = courseNode.getTeaserImageStyle();
		
		if (TeaserImageStyle.course == teaserImageStyle) {
			teaserImageStyle = course.getCourseConfig().getTeaserImageStyle();
		} else if (TeaserImageStyle.inherited == teaserImageStyle) {
			if (node.getParent() != null) {
				teaserImageStyle = getTeaserImageStyle(course, node.getParent());
			} else {
				teaserImageStyle = course.getCourseConfig().getTeaserImageStyle();
			}
		}
		
		return teaserImageStyle;
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
