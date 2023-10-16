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
package org.olat.group.ui.lifecycle;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;


/**
 * 
 * Initial date: 20 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TranslationBundle {
	
	private final String labelI18nKey;
	private final String subjectI18nKey;
	private final String bodyI18nKey;
	private final String[] subjectParams;
	private final String[] bodyParams;
	private final StaticTextElement viewEl;
	private final FormLink translationLink;
	
	public TranslationBundle(String labelI18nKey, String subjectI18nKey, String[] subjectParams,
							 String bodyI18nKey, String[] bodyParams, StaticTextElement viewEl, FormLink translationLink) {
		this.viewEl = viewEl;
		this.bodyI18nKey = bodyI18nKey;
		this.bodyParams = bodyParams;
		this.labelI18nKey = labelI18nKey;
		this.subjectI18nKey = subjectI18nKey;
		this.subjectParams = subjectParams;
		this.translationLink = translationLink;
	}

	public StaticTextElement getViewEl() {
		return viewEl;
	}
	
	public String getSubjectI18nKey() {
		return subjectI18nKey;
	}

	public String getBodyI18nKey() {
		return bodyI18nKey;
	}

	public String getLabelI18nKey() {
		return labelI18nKey;
	}
	
	public void setVisible(boolean visible) {
		viewEl.setVisible(visible);
		translationLink.setVisible(visible);
	}
	
	public void update(Translator translator) {
		StringBuilder val = new StringBuilder(256);
		if(StringHelper.containsNonWhitespace(getSubjectI18nKey())) {
			String translatedSubject;
			if (subjectParams != null) {
				translatedSubject = translator.translate(getSubjectI18nKey(), subjectParams);
			} else {
				translatedSubject = translator.translate(getSubjectI18nKey());
			}
			val.append("<strong>").append(translatedSubject).append("</strong><br>");
		}
		if(StringHelper.containsNonWhitespace(getBodyI18nKey())) {
			String translatedBody;
			if (bodyParams != null) {
				translatedBody = translator.translate(getBodyI18nKey(), bodyParams);
			} else {
				translatedBody = translator.translate(getBodyI18nKey());
			}
			val.append(translatedBody);
		}
		getViewEl().setValue(val.toString());
	}
}
