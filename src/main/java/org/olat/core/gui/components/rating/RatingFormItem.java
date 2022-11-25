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
package org.olat.core.gui.components.rating;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * 
 * Initial date: 31.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RatingFormItem extends FormItemImpl {

	private final RatingComponent component;
	
	public RatingFormItem(String name, float intialRating, int maxRating, boolean allowUserInput) {
		super(name);
		component = new RatingComponent(name, name, intialRating, maxRating, allowUserInput, this);
	}

	public float getCurrentRating() {
		return component.getCurrentRating();
	}
	
	public void setCurrentRating(float currentRating) {
		component.setCurrentRating(currentRating);
	}
	
	public void setShowRatingAsText(boolean showRatingAsText) {
		component.setShowRatingAsText(showRatingAsText);
	}
	
	public void setCssClass(String cssClass) {
		component.setCssClass(cssClass);
	}

	@Override
	protected RatingComponent getFormItemComponent() {
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
	public void dispatchFormRequest(UserRequest ureq) {
		component.doDispatchRequest(ureq);
		float newRating = getCurrentRating();
		getRootForm().fireFormEvent(ureq, new RatingFormEvent(this, newRating));
	}

	@Override
	public void reset() {
		//
	}
}