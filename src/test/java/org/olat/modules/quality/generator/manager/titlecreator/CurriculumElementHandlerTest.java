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
package org.olat.modules.quality.generator.manager.titlecreator;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.quality.generator.TitleCreator;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementHandlerTest extends OlatTestCase {
	
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CurriculumService curriculumService;

	@Autowired
	private TitleCreator titleCreator;
	
	@Test
	public void shouldMergeCurriculumElementDisplayName() {
		CurriculumElement element = createCurriculumElement();
		String value = random();
		element.setDisplayName(value);
		String template = "$" + CurriculumElementHandler.CURRICULUM_ELEMENT_DISPLAY_NAME;
		
		String merged = titleCreator.merge(template, asList(element));
		
		assertThat(merged).isEqualTo(value);
	}
	
	@Test
	public void shouldMergeCurriculumElemenIdentifier() {
		CurriculumElement element = createCurriculumElement();
		String value = random();
		element.setIdentifier(value);
		String template = "$" + CurriculumElementHandler.CURRICULUM_ELEMENT_IDENTIFIER;
		
		String merged = titleCreator.merge(template, asList(element));
		
		assertThat(merged).isEqualTo(value);
	}
	
	@Test
	public void shouldMergeCurriculumElemenTypeDisplayName() {
		String value = random();
		CurriculumElementType type = curriculumService.createCurriculumElementType(random(), value, null, null);
		CurriculumElement element = createCurriculumElement();
		element.setType(type);
		String template = "$" + CurriculumElementHandler.CURRICULUM_ELEMENT_TYPE_DISPLAY_NAME;
		
		String merged = titleCreator.merge(template, asList(element));
		
		assertThat(merged).isEqualTo(value);
	}
	
	
	@Test
	public void shouldMergeCurriculumElemenCurriculumDisplayName() {
		String value = random();
		Organisation organisation = organisationService.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum(random(), value, null, false, organisation);
		CurriculumElement element = curriculumService.createCurriculumElement(random(), random(),
				CurriculumElementStatus.active, null, null, null, null, CurriculumCalendars.disabled,
				CurriculumLectures.disabled, CurriculumLearningProgress.disabled, curriculum);
		String template = "$" + CurriculumElementHandler.CURRICULUM_DISPLAY_NAME;
		
		String merged = titleCreator.merge(template, asList(element));
		
		assertThat(merged).isEqualTo(value);
	}
	
	@Test
	public void shouldMergeCurriculumElemenSupressNulls() {
		CurriculumElement element = createCurriculumElement();
		element.setDisplayName(null);
		String template = "$" + CurriculumElementHandler.CURRICULUM_ELEMENT_DISPLAY_NAME;
		
		String merged = titleCreator.merge(template, asList(element));
		
		assertThat(merged).isEqualTo("");
	}
	
	@Test
	public void shouldMergeCurriculumElement() {
		CurriculumElement element = createCurriculumElement();
		String displaName = random();
		element.setDisplayName(displaName);
		String identifier = random();
		element.setIdentifier(identifier);
		String template = "$" + CurriculumElementHandler.CURRICULUM_ELEMENT_DISPLAY_NAME + " $"
				+ CurriculumElementHandler.CURRICULUM_ELEMENT_IDENTIFIER;

		String merged = titleCreator.merge(template, asList(element));

		assertThat(merged).isEqualTo(displaName + " " + identifier);
	}
	
	private CurriculumElement createCurriculumElement() {
		Organisation organisation = organisationService.getDefaultOrganisation();
		Curriculum curriculum = curriculumService.createCurriculum(random(), random(), null, false, organisation);
		return curriculumService.createCurriculumElement(random(), random(), CurriculumElementStatus.active, null, null,
				null, null, CurriculumCalendars.disabled, CurriculumLectures.disabled,
				CurriculumLearningProgress.disabled, curriculum);
	}
	
	private String random() {
		return UUID.randomUUID().toString();
	}

}
