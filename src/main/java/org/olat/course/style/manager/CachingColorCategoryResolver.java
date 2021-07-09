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
import org.olat.course.style.ColorCategory;
import org.olat.course.style.ColorCategoryResolver;
import org.olat.course.style.ColorCategorySearchParams;
import org.olat.course.tree.CourseEditorTreeNode;

import com.google.common.base.Functions;

/**
 * 
 * Initial date: 1 Jul 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CachingColorCategoryResolver implements ColorCategoryResolver {
	
	private final ColorCategoryDAO colorCategoryDao;
	private Map<String, ColorCategory> idenitiferToCategory;
	
	public CachingColorCategoryResolver(ColorCategoryDAO colorCategoryDao) {
		this(colorCategoryDao, null);
	}
	
	public CachingColorCategoryResolver(ColorCategoryDAO colorCategoryDao, ColorCategorySearchParams preloadParams) {
		this.colorCategoryDao = colorCategoryDao;
		if (preloadParams != null) {
			idenitiferToCategory = colorCategoryDao.load(preloadParams).stream()
					.collect(Collectors.toMap(ColorCategory::getIdentifier, Functions.identity()));
		} else {
			idenitiferToCategory = new HashMap<>();
		}
	}
	
	@Override
	public String getColorCategoryCss(INode iNode, String courseColorCategoryIdentifier) {
		ColorCategory colorCategory = getColorCategory(iNode, courseColorCategoryIdentifier);
		return colorCategory.isEnabled()
				? colorCategory.getCssClass()
				: getCachedOrLoad(ColorCategory.IDENTIFIER_NO_COLOR).getCssClass();
	}

	@Override
	public ColorCategory getInheritedColorCategory(INode iNode, String courseColorCategoryIdentifier) {
		INode parent = iNode.getParent();
		if (parent != null) {
			return getColorCategory(parent, courseColorCategoryIdentifier);
		}
		return getCachedOrLoad(ColorCategory.IDENTIFIER_NO_COLOR);
	}
	
	private ColorCategory getColorCategory(INode iNode, String courseColorCategoryIdentifier) {
		ColorCategory colorCategory = getColorCategory(iNode);
		if (ColorCategory.IDENTIFIER_INHERITED.equals(colorCategory.getIdentifier())) {
			colorCategory = getColorCategory(courseColorCategoryIdentifier, ColorCategory.IDENTIFIER_FALLBACK_COURSE);
		}
		return colorCategory;
	}

	private ColorCategory getColorCategory(INode iNode) {
		String colorCategoryIdentifier = getCourseNode(iNode).getColorCategoryIdentifier();
		ColorCategory colorCategory = getColorCategory(colorCategoryIdentifier, ColorCategory.IDENTIFIER_FALLBACK_COURSE_NODE);
		if (ColorCategory.IDENTIFIER_INHERITED.equals(colorCategory.getIdentifier())) {
			INode parent = iNode.getParent();
			if (parent != null) {
				colorCategory = getColorCategory(parent);
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
	
	private CourseNode getCourseNode(INode iNode) {
		if (iNode instanceof CourseNode) {
			return (CourseNode)iNode;
		} else if (iNode instanceof CourseEditorTreeNode) {
			return ((CourseEditorTreeNode)iNode).getCourseNode();
		}
		return null;
	}

}
