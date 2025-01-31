/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui;

import java.util.List;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;

/**
 * 
 * Initial date: 31 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public record ConfirmDelete(String title, String message, String confirmation, String confirmationButton) {
	
	
	public static final ConfirmDelete valueOf(CurriculumElement curriculumElement, List<CurriculumElement> descendants, Translator translator) {
		if(descendants.isEmpty()) {
			return deleteSingleElement(curriculumElement, translator);
		}
		return deleteElementAndDescendants(curriculumElement, translator);
	}
	
	public static final ConfirmDelete deleteElementAndDescendants(CurriculumElement curriculumElement, Translator translator) {
		String title = translator.translate("confirmation.delete.elements.title");
		String elementType = getType(curriculumElement);
		String message = translator.translate("curriculums.elements.bulk.delete.text",
				StringHelper.escapeHtml(curriculumElement.getDisplayName()),
				StringHelper.escapeHtml(elementType));
		
		String confirmation = translator.translate("curriculums.elements.bulk.delete.confirm");
		String button = translator.translate("curriculums.element.bulk.delete.button");
		return new ConfirmDelete(title, message, confirmation, button);
	}
	
	public static final ConfirmDelete deleteSingleElement(CurriculumElement curriculumElement, Translator translator) {
		String title = translator.translate("confirmation.delete.element.title");
		String elementType = getType(curriculumElement);
		String message = translator.translate("curriculums.element.bulk.delete.text",
				StringHelper.escapeHtml(curriculumElement.getDisplayName()),
				StringHelper.escapeHtml(elementType));

		String confirmation = translator.translate("curriculums.element.bulk.delete.confirm");
		String button = translator.translate("curriculums.element.bulk.delete.button");
		return new ConfirmDelete(title, message, confirmation, button);
	}
	
	private static String getType(CurriculumElement curriculumElement) {
		if(curriculumElement.getType() != null) {
			return curriculumElement.getType().getDisplayName();
		}
		return "";
	}
}
