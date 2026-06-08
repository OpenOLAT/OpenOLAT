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
package org.olat.modules.selectus.ui.rating;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.rating.RatingFormEvent;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 26 sept. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CustomRatingFormItem extends FormItemImpl {
	
	private final CustomRatingComponent component;
	
	public CustomRatingFormItem(String name, float intialRating, int maxRating, boolean allowUserInput, boolean allowAbstain, Translator translator) {
		super(name);
		component = new CustomRatingComponent(name, intialRating, maxRating, allowUserInput, allowAbstain, this);
		setTranslator(translator);
	}
	
	public float getCurrentRating() {
		return component.getCurrentRating();
	}
	
	public void setCurrentRating(float currentRating) {
		component.setCurrentRating(currentRating);
	}
	
	public void setDomReplacementWrapperRequired(boolean required ) {
		component.setDomReplacementWrapperRequired(required);
	}

	@Override
	public void setTranslator(Translator translator) {
		component.setTranslator(translator);
		super.setTranslator(translator);
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		// 
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}

	@Override
	public void reset() {
		component.setCurrentRating(0.0f);
	}
	
	@Override
	public void dispatchFormRequest(UserRequest ureq) {
		component.doDispatchRequest(ureq);
		float newRating = getCurrentRating();
		getRootForm().fireFormEvent(ureq, new RatingFormEvent(this, newRating));
	}

	public void setLevelLabel(int position, String ratingLabel) {
		component.setLevelLabel(position, ratingLabel);
	}
}
