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
package org.olat.course.style;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;

/**
 * 
 * Initial date: 23 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface CourseStyleService {
	
	public final static String FOLDER_ROOT = "coursestyles";
	public static final Set<String> IMAGE_MIME_TYPES = Set.of("image/gif", "image/jpg", "image/jpeg", "image/png");
	public static final long IMAGE_LIMIT_KB = 4096;
	
	public List<ImageSource> getSystemTeaserImageSources();

	public File getSystemTeaserImageFile(String filename);
	
	public ImageSource getSystemTeaserImageSource(String filename);
	
	public ImageSource storeImage(ICourse course, Identity createdBy, File file, String filename);
	
	public VFSLeaf getImage(ICourse course);

	public void deleteImage(ICourse course);
	
	public ImageSource storeImage(ICourse course, CourseNode courseNode, Identity createdBy, File file, String filename);
	
	public VFSLeaf getImage(ICourse course, CourseNode courseNode);

	public void deleteImage(ICourse course, CourseNode courseNode);

	public ColorCategory createColorCategory(String identifier);

	public ColorCategory getColorCategory(ColorCategoryRef colorCategoryRef);

	public ColorCategory getColorCategory(String identifier, String fallbackIdentifier);

	public List<ColorCategory> getColorCategories(ColorCategorySearchParams searchParams);

	public boolean isColorCategoryIdentifierAvailable(String identifier);

	public ColorCategory updateColorCategory(ColorCategory colorCategory);

	public void doMove(ColorCategoryRef colorCategoryRef, boolean up);

	public void deleteColorCategory(ColorCategory colorCategory);
	
	public ColorCategoryResolver getColorCategoryResolver(ColorCategorySearchParams preloadParams);

	public Header getHeader(CourseEnvironment courseEnv, CourseNode courseNode, String iconCssClass);

}
