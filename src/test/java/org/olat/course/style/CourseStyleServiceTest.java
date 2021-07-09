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

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseStyleServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private CourseStyleService sut;
	
	@Test
	public void shouldReturnColorCategoryIfExistingIdentifier() {
		ColorCategory colorCategory = sut.createColorCategory(random());
		
		ColorCategory reloaded = sut.getColorCategory(colorCategory.getIdentifier(), ColorCategory.IDENTIFIER_NO_COLOR);
		
		assertThat(reloaded).isEqualTo(colorCategory);
	}
	
	@Test
	public void shouldReturnFallbackColorCategoryIfIdentifierNotExists() {
		ColorCategory fallbackCategory = sut.createColorCategory(ColorCategory.IDENTIFIER_NO_COLOR);
		
		ColorCategory reloaded = sut.getColorCategory(random(), ColorCategory.IDENTIFIER_NO_COLOR);
		
		assertThat(reloaded).isEqualTo(fallbackCategory);
	}
	
	@Test
	public void shouldMoveUp() {
		ColorCategory colorCategory1 = sut.createColorCategory(random());
		ColorCategory colorCategory2 = sut.createColorCategory(random());
		dbInstance.commitAndCloseSession();
		
		sut.doMove(colorCategory2, true);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getColorCategory(colorCategory1).getSortOrder()).isEqualTo(colorCategory2.getSortOrder());
		softly.assertThat(sut.getColorCategory(colorCategory2).getSortOrder()).isEqualTo(colorCategory1.getSortOrder());
		softly.assertAll();
	}
	
	@Test
	public void shouldMoveDown() {
		ColorCategory colorCategory1 = sut.createColorCategory(random());
		ColorCategory colorCategory2 = sut.createColorCategory(random());
		dbInstance.commitAndCloseSession();
		
		sut.doMove(colorCategory1, false);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getColorCategory(colorCategory1).getSortOrder()).isEqualTo(colorCategory2.getSortOrder());
		softly.assertThat(sut.getColorCategory(colorCategory2).getSortOrder()).isEqualTo(colorCategory1.getSortOrder());
		softly.assertAll();
	}
	
	@Test
	public void shouldNotMoveUpTopmost() {
		ColorCategorySearchParams searchParams = ColorCategorySearchParams.builder().addColorTypes().build();
		ColorCategory topmost = sut.getColorCategories(searchParams).stream()
				.sorted()
				.findFirst().get();
		
		sut.doMove(topmost, true);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.getColorCategory(topmost).getSortOrder()).isEqualTo(topmost.getSortOrder());
	}
	
	@Test
	public void shouldNotMoveDownLowermost() {
		ColorCategory lowermost = sut.createColorCategory(random());
		dbInstance.commitAndCloseSession();
		
		sut.doMove(lowermost, false);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.getColorCategory(lowermost).getSortOrder()).isEqualTo(lowermost.getSortOrder());
	}

}
