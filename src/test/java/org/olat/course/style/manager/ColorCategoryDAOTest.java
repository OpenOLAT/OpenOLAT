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
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.course.style.ColorCategory;
import org.olat.course.style.ColorCategory.Type;
import org.olat.course.style.ColorCategorySearchParams;
import org.olat.course.style.model.ColorCategoryImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ColorCategoryDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private ColorCategoryDAO sut;
	
	@Test
	public void shouldCreateEducationalType() {
		String identifier = random();
		
		ColorCategory colorCategory = sut.create(identifier);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(colorCategory.getKey()).isNotNull();
		softly.assertThat(((ColorCategoryImpl)colorCategory).getCreationDate()).isNotNull();
		softly.assertThat(((ColorCategoryImpl)colorCategory).getLastModified()).isNotNull();
		softly.assertThat(colorCategory.getIdentifier()).isEqualTo(identifier);
		softly.assertThat(colorCategory.getType()).isEqualTo(Type.custom);
		softly.assertThat(colorCategory.getSortOrder()).isNotNull();
		softly.assertThat(colorCategory.isEnabled()).isTrue();
		softly.assertAll();
	}

	@Test
	public void shouldCreatePredefinedEducationalType() {
		String identifier = random();
		String cssClass = random();
		ColorCategory colorCategory = sut.createPredefined(identifier, Type.predefined, sut.getNextSortOrder(), cssClass);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(colorCategory.getKey()).isNotNull();
		softly.assertThat(((ColorCategoryImpl)colorCategory).getCreationDate()).isNotNull();
		softly.assertThat(((ColorCategoryImpl)colorCategory).getLastModified()).isNotNull();
		softly.assertThat(colorCategory.getIdentifier()).isEqualTo(identifier);
		softly.assertThat(colorCategory.getType()).isEqualTo(Type.predefined);
		softly.assertThat(colorCategory.getSortOrder()).isNotNull();
		softly.assertThat(colorCategory.isEnabled()).isTrue();
		softly.assertThat(colorCategory.getCssClass()).isEqualTo(cssClass);
		softly.assertAll();
	}
	
	@Test
	public void shouldSave() {
		ColorCategory colorCategory = sut.create(random());
		dbInstance.commitAndCloseSession();
		
		String cssClass = random();
		colorCategory.setCssClass(cssClass);
		colorCategory = sut.save(colorCategory);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(colorCategory.getCssClass()).isEqualTo(cssClass);
		softly.assertAll();
	}

	@Test
	public void shouldLoadByKey() {
		ColorCategory colorCategory = sut.create(random());
		dbInstance.commitAndCloseSession();
		
		ColorCategory reloaded = sut.loadByKey(() -> colorCategory.getKey());
		
		assertThat(reloaded).isEqualTo(colorCategory);
	}

	@Test
	public void shouldLoadByIdentifier() {
		String identifier = random();
		ColorCategory colorCategory = sut.create(identifier);
		dbInstance.commitAndCloseSession();
		
		ColorCategory reloaded = sut.loadByIdentifier(identifier);
		
		assertThat(reloaded).isEqualTo(colorCategory);
	}

	@Test
	public void shouldLoadBySortOrder() {
		ColorCategory colorCategory = sut.create(random());
		dbInstance.commitAndCloseSession();
		
		ColorCategory reloaded = sut.loadBySortOrder(colorCategory.getSortOrder());
		
		assertThat(reloaded).isEqualTo(colorCategory);
	}
	
	@Test
	public void shouldFilterByEnabled() {
		ColorCategory enabled1 = sut.create(random());
		ColorCategory enabled2 = sut.create(random());
		ColorCategory disabled = sut.create(random());
		disabled.setEnabled(false);
		sut.save(disabled);
		dbInstance.commitAndCloseSession();
		
		ColorCategorySearchParams searchParams = ColorCategorySearchParams.builder()
				.withEnabled(Boolean.TRUE)
				.build();
		List<ColorCategory> colorCategories = sut.load(searchParams);
		
		assertThat(colorCategories)
				.contains(enabled1)
				.contains(enabled2)
				.doesNotContain(disabled);
	}
	
	@Test
	public void shouldFilterByDisabled() {
		ColorCategory disabled1 = sut.create(random());
		disabled1.setEnabled(false);
		sut.save(disabled1);
		ColorCategory disabled2 = sut.create(random());
		disabled2.setEnabled(false);
		sut.save(disabled2);
		ColorCategory enabled = sut.create(random());
		dbInstance.commitAndCloseSession();
		
		ColorCategorySearchParams searchParams = ColorCategorySearchParams.builder()
				.withEnabled(Boolean.FALSE)
				.build();
		List<ColorCategory> colorCategories = sut.load(searchParams);
		
		assertThat(colorCategories)
				.contains(disabled1)
				.contains(disabled2)
				.doesNotContain(enabled);
	}
	
	@Test
	public void shouldFilterByType() {
		sut.create(random());
		dbInstance.commitAndCloseSession();
		
		ColorCategorySearchParams searchParams = ColorCategorySearchParams.builder()
				.addType(ColorCategory.Type.technical)
				.build();
		List<ColorCategory> colorCategories = sut.load(searchParams);
		
		assertThat(colorCategories).hasSize(2);
	}
	
	@Test
	public void shouldFilterByExcludedIdentifier() {
		ColorCategory included = sut.create(random());
		ColorCategory excluded1 = sut.create(random());
		ColorCategory excluded2 = sut.create(random());
		dbInstance.commitAndCloseSession();
		
		ColorCategorySearchParams searchParams = ColorCategorySearchParams.builder()
				.addExcludedIdentifier(excluded1.getIdentifier())
				.addExcludedIdentifier(excluded2.getIdentifier())
				.build();
		List<ColorCategory> colorCategories = sut.load(searchParams);
		
		assertThat(colorCategories)
				.contains(included)
				.doesNotContain(excluded1)
				.doesNotContain(excluded2);
	}

	
	@Test
	public void shouldGetNextSortOrder() {
		dbInstance.commitAndCloseSession();
		ColorCategory colorCategory = sut.create(random());
		
		int nextSortOrder = sut.getNextSortOrder();
		
		assertThat(nextSortOrder).isEqualTo(colorCategory.getSortOrder() + 1);
	}
	
	@Test
	public void shouldDelete() {
		String identifier = random();
		ColorCategory colorCategory = sut.create(identifier);
		dbInstance.commitAndCloseSession();
		
		sut.delete(colorCategory);
		dbInstance.commitAndCloseSession();
		
		ColorCategory reloaded = sut.loadByIdentifier(identifier);
		assertThat(reloaded).isNull();
	}

}
