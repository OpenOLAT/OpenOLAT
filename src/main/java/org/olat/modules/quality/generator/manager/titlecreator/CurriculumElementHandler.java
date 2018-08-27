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

import static org.olat.core.util.StringHelper.blankIfNull;

import java.util.Arrays;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.quality.generator.TitleCreatorHandler;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 27.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CurriculumElementHandler implements TitleCreatorHandler {

	static final String CURRICULUM_ELEMENT_DISPLAY_NAME = "curEleDisplayName";
	static final String CURRICULUM_ELEMENT_IDENTIFIER = "curEleIdentifier";
	static final String CURRICULUM_ELEMENT_TYPE_DISPLAY_NAME = "curEleTypeDisplayName";
	static final String CURRICULUM_DISPLAY_NAME = "curDisplayName";
	private static final List<String> identifiers = Arrays.asList(
			CURRICULUM_ELEMENT_DISPLAY_NAME,
			CURRICULUM_ELEMENT_IDENTIFIER,
			CURRICULUM_ELEMENT_TYPE_DISPLAY_NAME,
			CURRICULUM_DISPLAY_NAME
	);

	@Override
	public boolean canHandle(Class<?> clazz) {
		return CurriculumElement.class.isAssignableFrom(clazz);
	}

	@Override
	public void mergeContext(VelocityContext context, Object object) {
		if (object instanceof CurriculumElement) {
			CurriculumElement element = (CurriculumElement) object;
			context.put(CURRICULUM_ELEMENT_DISPLAY_NAME, blankIfNull(element.getDisplayName()));
			context.put(CURRICULUM_ELEMENT_IDENTIFIER, blankIfNull(element.getIdentifier()));
			
			Curriculum curriculum = element.getCurriculum();
			if (curriculum != null) {
				context.put(CURRICULUM_DISPLAY_NAME, blankIfNull(curriculum.getDisplayName()));
			}
			
			CurriculumElementType type = element.getType();
			if (type != null) {
				context.put(CURRICULUM_ELEMENT_TYPE_DISPLAY_NAME, blankIfNull(type.getDisplayName()));
			}
		}
	}

	@Override
	public List<String> getIdentifiers() {
		return identifiers;
	}

}
