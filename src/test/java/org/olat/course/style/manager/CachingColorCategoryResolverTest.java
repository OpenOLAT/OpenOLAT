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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.olat.test.JunitTestHelper.random;

import org.junit.Before;
import org.junit.Test;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.style.ColorCategory;
import org.olat.course.style.model.ColorCategoryImpl;

/**
 * 
 * Initial date: 2 Jul 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CachingColorCategoryResolverTest {
	
	private ColorCategoryDAO colorCategoryDao;
	private String cssNoColor = "css.no.color";
	private String cssInherited = "css.inherited";
	private String cssCourse = "css.course";
	private String identifierCourse = "identifierCourse";
	private String identifierDisabled = "identifierD";
	private String cssDisabled = "css.disabled";
	private String identifier1 = "identifier1";
	private String css1 = "css1";
	private String identifier2 = "identifier2";
	private String css2 = "css2";
	

	@Before
	public void setup() {
		colorCategoryDao = mock(ColorCategoryDAO.class);
		
		ColorCategory colorCategory = createColorCategory(ColorCategory.IDENTIFIER_NO_COLOR, cssNoColor, true);
		when(colorCategoryDao.loadByIdentifier(ColorCategory.IDENTIFIER_NO_COLOR)).thenReturn(colorCategory);
		
		colorCategory = createColorCategory(ColorCategory.IDENTIFIER_INHERITED, cssInherited, true);
		when(colorCategoryDao.loadByIdentifier(ColorCategory.IDENTIFIER_INHERITED)).thenReturn(colorCategory);
		
		colorCategory = createColorCategory(identifierCourse, cssCourse, true);
		when(colorCategoryDao.loadByIdentifier(identifierCourse)).thenReturn(colorCategory);
		
		colorCategory = createColorCategory(identifierDisabled, cssDisabled, false);
		when(colorCategoryDao.loadByIdentifier(identifierDisabled)).thenReturn(colorCategory);
		
		colorCategory = createColorCategory(identifier1, css1, true);
		when(colorCategoryDao.loadByIdentifier(identifier1)).thenReturn(colorCategory);

		colorCategory = createColorCategory(identifier2, css2, true);
		when(colorCategoryDao.loadByIdentifier(identifier2)).thenReturn(colorCategory);
	}
	
	private ColorCategory createColorCategory(String identifier, String css, boolean enabled) {
		ColorCategoryImpl colorCategory = new ColorCategoryImpl();
		colorCategory .setIdentifier(identifier);
		colorCategory.setCssClass(css);
		colorCategory.setEnabled(enabled);
		return colorCategory;
	}
	
	@Test
	public void shouldGetCssOfCourseNode() {
		CourseNode courseNode = new STCourseNode();
		courseNode.setColorCategoryIdentifier(identifier1);
		
		CachingColorCategoryResolver sut = new CachingColorCategoryResolver(colorCategoryDao, identifierCourse);
		String colorCategoryCss = sut.getColorCategoryCss(courseNode);
		
		assertThat(colorCategoryCss).isEqualTo(css1);
	}
	
	@Test
	public void shouldGetFallbackCssOfCourseNodeIfCategoryNotExists() {
		CourseNode courseNode1 = new STCourseNode();
		courseNode1.setColorCategoryIdentifier(identifier1);
		CourseNode courseNode2 = new STCourseNode();
		courseNode2.setParent(courseNode1);
		courseNode2.setColorCategoryIdentifier(random()); // ColorCategory does not exists => inherit
		
		CachingColorCategoryResolver sut = new CachingColorCategoryResolver(colorCategoryDao, identifierCourse);
		String colorCategoryCss = sut.getColorCategoryCss(courseNode2);
		
		assertThat(colorCategoryCss).isEqualTo(css1);
	}
	
	@Test
	public void shouldGetCssOfInheritedCourseNode() {
		CourseNode courseNode1 = new STCourseNode();
		courseNode1.setColorCategoryIdentifier(identifier1);
		CourseNode courseNode2 = new STCourseNode();
		courseNode2.setParent(courseNode1);
		courseNode2.setColorCategoryIdentifier(ColorCategory.IDENTIFIER_INHERITED);
		
		CachingColorCategoryResolver sut = new CachingColorCategoryResolver(colorCategoryDao, identifierCourse);
		String colorCategoryCss = sut.getColorCategoryCss(courseNode2);
		
		assertThat(colorCategoryCss).isEqualTo(css1);
	}
	
	@Test
	public void shouldGetCssOfInheritedCourse() {
		CourseNode courseNode = new STCourseNode();
		courseNode.setColorCategoryIdentifier(ColorCategory.IDENTIFIER_INHERITED);
		
		CachingColorCategoryResolver sut = new CachingColorCategoryResolver(colorCategoryDao, identifierCourse);
		String colorCategoryCss = sut.getColorCategoryCss(courseNode);
		
		assertThat(colorCategoryCss).isEqualTo(cssCourse);
	}

	@Test
	public void shouldGetFallbackCssOfCourse() {
		CourseNode courseNode = new STCourseNode();
		courseNode.setColorCategoryIdentifier(ColorCategory.IDENTIFIER_INHERITED);
		
		CachingColorCategoryResolver sut = new CachingColorCategoryResolver(colorCategoryDao, random());
		String colorCategoryCss = sut.getColorCategoryCss(courseNode);
		
		assertThat(colorCategoryCss).isEqualTo(cssNoColor);
	}
	
	@Test
	public void shouldGetNoColorCssIfCategoryOfCourseNodeDisabled() {
		CourseNode courseNode = new STCourseNode();
		courseNode.setColorCategoryIdentifier(identifierDisabled);
		
		CachingColorCategoryResolver sut = new CachingColorCategoryResolver(colorCategoryDao, identifierCourse);
		String colorCategoryCss = sut.getColorCategoryCss(courseNode);
		
		assertThat(colorCategoryCss).isEqualTo(cssNoColor);
	}
	
	@Test
	public void shouldGetNoColorCssIfCategoryOfCourseDisabled() {
		CourseNode courseNode = new STCourseNode();
		courseNode.setColorCategoryIdentifier(ColorCategory.IDENTIFIER_INHERITED);
		
		CachingColorCategoryResolver sut = new CachingColorCategoryResolver(colorCategoryDao, identifierDisabled);
		String colorCategoryCss = sut.getColorCategoryCss(courseNode);
		
		assertThat(colorCategoryCss).isEqualTo(cssNoColor);
	}
	
	@Test
	public void shouldGetInheritedColorCategory() {
		CourseNode courseNode1 = new STCourseNode();
		courseNode1.setColorCategoryIdentifier(identifier1);
		CourseNode courseNode2 = new STCourseNode();
		courseNode2.setParent(courseNode1);
		courseNode2.setColorCategoryIdentifier(ColorCategory.IDENTIFIER_INHERITED);
		CourseNode courseNode3 = new STCourseNode();
		courseNode3.setParent(courseNode2);
		courseNode3.setColorCategoryIdentifier(ColorCategory.IDENTIFIER_INHERITED);
		
		CachingColorCategoryResolver sut = new CachingColorCategoryResolver(colorCategoryDao, identifierCourse);
		ColorCategory inheritedColorCategory = sut.getInheritedColorCategory(courseNode3);
		
		assertThat(inheritedColorCategory.getCssClass()).isEqualTo(css1);
	}
	
	@Test
	public void shouldGetInheritedColorCategoryUpToCourse() {
		CourseNode courseNode1 = new STCourseNode();
		courseNode1.setColorCategoryIdentifier(ColorCategory.IDENTIFIER_INHERITED);
		CourseNode courseNode2 = new STCourseNode();
		courseNode2.setParent(courseNode1);
		courseNode2.setColorCategoryIdentifier(ColorCategory.IDENTIFIER_INHERITED);
		CourseNode courseNode3 = new STCourseNode();
		courseNode3.setParent(courseNode2);
		courseNode3.setColorCategoryIdentifier(ColorCategory.IDENTIFIER_INHERITED);
		
		CachingColorCategoryResolver sut = new CachingColorCategoryResolver(colorCategoryDao, identifierCourse);
		ColorCategory inheritedColorCategory = sut.getInheritedColorCategory(courseNode3);
		
		assertThat(inheritedColorCategory.getCssClass()).isEqualTo(cssCourse);
	}

}
