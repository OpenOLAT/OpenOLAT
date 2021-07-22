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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.util.nodes.INode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeHelper;
import org.olat.course.style.ColorCategory;
import org.olat.course.style.ColorCategoryResolver;
import org.olat.course.style.ColorCategorySearchParams;

import com.google.common.base.Functions;

/**
 * 
 * Initial date: 1 Jul 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CachingColorCategoryResolver implements ColorCategoryResolver {
	
	private final ColorCategoryDAO colorCategoryDao;
	private final String courseColorCategoryIdentifier;
	private Map<String, ColorCategory> idenitiferToCategory;
	
	public CachingColorCategoryResolver(ColorCategoryDAO colorCategoryDao, String courseColorCategoryIdentifier) {
		this(colorCategoryDao, null, courseColorCategoryIdentifier);
	}
	
	public CachingColorCategoryResolver(ColorCategoryDAO colorCategoryDao, ColorCategorySearchParams preloadParams, String courseColorCategoryIdentifier) {
		this.colorCategoryDao = colorCategoryDao;
		this.courseColorCategoryIdentifier = courseColorCategoryIdentifier;
		if (preloadParams != null) {
			idenitiferToCategory = colorCategoryDao.load(preloadParams).stream()
					.collect(Collectors.toMap(ColorCategory::getIdentifier, Functions.identity()));
		} else {
			idenitiferToCategory = new HashMap<>();
		}
	}
	
	@Override
	public String getCss(ColorCategory colorCategory) {
		return colorCategory != null && colorCategory.isEnabled()
				? colorCategory.getCssClass()
				: getCachedOrLoad(ColorCategory.IDENTIFIER_NO_COLOR).getCssClass();
	}
	
	@Override
	public String getColorCategoryCss(INode iNode) {
		ColorCategory colorCategory = getNodeColorCategory(iNode);
		return getCss(colorCategory);
	}
	
	@Override
	public ColorCategory getColorCategory(INode iNode) {
		return getNodeColorCategory(iNode);
	}

	private ColorCategory getNodeColorCategory(INode iNode) {
		CourseNode courseNode = CourseNodeHelper.getCourseNode(iNode);
		String colorCategoryIdentifier = courseNode != null? courseNode.getColorCategoryIdentifier(): null;
		return getColorCategory(colorCategoryIdentifier, iNode);
	}

	@Override
	public ColorCategory getColorCategory(String colorCategoryIdentifier, INode iNode) {
		if (ColorCategory.IDENTIFIER_COURSE.equals(colorCategoryIdentifier)) {
			return getCachedOrLoad(courseColorCategoryIdentifier);
		}
		
		String fallbackIdentifier = iNode != null? ColorCategory.IDENTIFIER_FALLBACK_COURSE_NODE: ColorCategory.IDENTIFIER_FALLBACK_COURSE;
		ColorCategory colorCategory = getColorCategory(colorCategoryIdentifier, fallbackIdentifier);
		if (ColorCategory.IDENTIFIER_INHERITED.equals(colorCategory.getIdentifier())) {
			INode parent = iNode != null? iNode.getParent(): null;
			if (parent != null) {
				colorCategory = getNodeColorCategory(parent);
			} else {
				colorCategory = getCachedOrLoad(courseColorCategoryIdentifier);
			}
		}
		return colorCategory;
	}
	
	private ColorCategory getColorCategory(String identifier, String fallbackIdentifier) {
		ColorCategory colorCategory = getCachedOrLoad(identifier);
		if (colorCategory == null) {
			colorCategory = getCachedOrLoad(fallbackIdentifier);
		}
		return colorCategory;
	}
	
	private ColorCategory getCachedOrLoad(String identifier) {
		return idenitiferToCategory.computeIfAbsent(identifier, colorCategoryDao::loadByIdentifier);
	}
	


}
