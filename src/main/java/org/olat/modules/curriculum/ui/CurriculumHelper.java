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
package org.olat.modules.curriculum.ui;

import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;

/**
 * 
 * Initial date: 3 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumHelper {
	
	public static final int AVATAR_MAX_LENGTH = 9;
	
	private CurriculumHelper() {
		//
	}
	
	public static String truncateAvatar(String ref) {
		if(ref != null && ref.length() > AVATAR_MAX_LENGTH) {
			ref = ref.substring(0, AVATAR_MAX_LENGTH);
		}
		return ref;
	}
	
	public static String getCurriculumBusinessPath(Long curriculumKey) {
		return "[CurriculumAdmin:0][Curriculum:" + curriculumKey + "]";
	}
	
	public static String getLabel(CurriculumElement element, Translator translator) {
		Curriculum curriculum = element.getCurriculum();
		CurriculumElement parentElement = element.getParent();
		
		String[] args = new String[] {
			element.getDisplayName(),										// 0
			element.getIdentifier(),										// 1
			parentElement == null ? null : parentElement.getDisplayName(),	// 2
			parentElement == null ? null : parentElement.getIdentifier(),	// 3
			curriculum.getDisplayName(),									// 4
			curriculum.getIdentifier()										// 5
		};

		String i18nKey = parentElement == null ? "select.value.element" : "select.value.element.parent";
		return translator.translate(i18nKey, args);
	}
	
	public static String getParticipantRange(Translator translator, CurriculumElement element, boolean appendIcon) {
		return getParticipantRange(translator, element.getMinParticipants(), element.getMaxParticipants(), appendIcon);
	}
	
	public static String getParticipantRange(Translator translator, Long minParticipants, Long maxParticipants, boolean appendIcon) {
		if (minParticipants == null && maxParticipants == null) {
			return null;
		}
		
		String participants = "";
		if (appendIcon) {
			participants += "<i class=\"o_icon o_icon_num_participants\"></i> ";
		}
		if (minParticipants != null && minParticipants != 0 && maxParticipants != null && maxParticipants != 0) {
			participants += translator.translate("curriculum.element.participants.min.max",
					String.valueOf(minParticipants),
					String.valueOf(maxParticipants));
		} else if (minParticipants != null && minParticipants != 0) {
			participants += translator.translate("curriculum.element.participants.min",
					String.valueOf(minParticipants));
		} else if (maxParticipants != null && maxParticipants != 0) {
			participants += translator.translate("curriculum.element.participants.max",
					String.valueOf(maxParticipants));
		} else {
			participants = null;
		}
		return participants;
	}
	
	public static boolean validateTextElement(TextElement el, boolean mandatory, int maxLength) {
		if (el != null) {
			el.clearError();
			if(el.isVisible() && el.isEnabled()) {
				String val = el.getValue();
				if (mandatory && !StringHelper.containsNonWhitespace(val)) {
					el.setErrorKey("form.legende.mandatory");
					return false;
				} else if (StringHelper.containsNonWhitespace(val) && val.length() > maxLength) {
					el.setErrorKey("input.toolong", Integer.toString(maxLength));
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean validateIntegerElement(TextElement el, boolean mandatory) {
		if (el != null) {
			el.clearError();
			if(el.isVisible() && el.isEnabled()) {
				String val = el.getValue();
				if (mandatory && !StringHelper.containsNonWhitespace(val)) {
					el.setErrorKey("form.legende.mandatory");
					return false;
				} else if (StringHelper.containsNonWhitespace(val) && !StringHelper.isLong(val)) {
					el.setErrorKey("form.error.positive.integer");
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean validateElement(SingleSelection el) {
		if (el != null) {
			el.clearError();
			if(el.isVisible() && el.isEnabled() &&!el.isOneSelected()) {
				el.setErrorKey("form.legende.mandatory");
				return false;
			}
		}
		return true;
	}

	public static boolean isMoreThanCurriculumManager(Roles roles) {
		return roles.isAdministrator() || roles.isSystemAdmin() || roles.isLearnResourceManager();
	}
}
