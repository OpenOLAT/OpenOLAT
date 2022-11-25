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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * 
 * Initial date: 31.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RatingWithAverageFormItem extends FormItemImpl implements FormItemCollection {

	private float initialUserRating;
	private float lastUserRating;
	private long numOfRatings;

	private final RatingFormItem userComponent;
	private final RatingFormItem averageComponent;
	private final RatingWithAverageComponent component;
	
	public RatingWithAverageFormItem(String name, float userRating, float averageRating, int maxRating, long numOfRatings) {
		super(name);

		this.initialUserRating = userRating;
		this.lastUserRating = userRating;
		this.numOfRatings = numOfRatings;
		component = new RatingWithAverageComponent(name, this);
		
		averageComponent = new RatingFormItem("ravg_".concat(getName()), averageRating, maxRating, false);
		averageComponent.setCssClass("o_rating_average");

		userComponent = new RatingFormItem("rusr_".concat(getName()), initialUserRating, maxRating, true);
		userComponent.getFormItemComponent().setTranslateExplanation(true);
		userComponent.getFormItemComponent().setTranslateRatingLabels(true);
		userComponent.getFormItemComponent().addListener(component);
		userComponent.setCssClass("o_rating_personal");
	}
	
	protected Component getUserComponent() {
		return userComponent.getComponent();
	}
	
	protected Component getAverageComponent() {
		return averageComponent.getComponent();
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		super.setEnabled(isEnabled);
		userComponent.setEnabled(isEnabled);
		averageComponent.setEnabled(isEnabled);
	}
	
	public void setShowRatingAsText(boolean showRatingAsText) {
		userComponent.setShowRatingAsText(showRatingAsText);
		averageComponent.setShowRatingAsText(showRatingAsText);
	}
	
	public void setUserRating(float userRating) {
		userComponent.setCurrentRating(userRating);
		component.setDirty(true);
	}
	
	public void setAverageRating(float averageRating) {
		averageComponent.setCurrentRating(averageRating);
		component.setDirty(true);
	}

	@Override
	public Iterable<FormItem> getFormItems() {
		List<FormItem> items = new ArrayList<>(3);
		items.add(userComponent);
		items.add(averageComponent);
		return items;
	}

	@Override
	public FormItem getFormComponent(String name) {
		if(userComponent.getName().equals(name)) {
			return userComponent;
		}
		if(averageComponent.getName().equals(name)) {
			return averageComponent;
		}
		return null;
	}

	@Override
	protected void rootFormAvailable() {
		if(userComponent.getRootForm() == null) {
			userComponent.setRootForm(getRootForm());
			userComponent.rootFormAvailable();
		}

		if(averageComponent.getRootForm() == null) {	
			averageComponent.setRootForm(getRootForm());
			averageComponent.rootFormAvailable();
			
			String explanation = translator.translate("rating.average.explanation", Long.toString(numOfRatings));
			averageComponent.getFormItemComponent().setExplanation(explanation);
			averageComponent.getFormItemComponent().setTranslateExplanation(false);
			averageComponent.getFormItemComponent().setTranslateRatingLabels(true);
		}
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}
	
	@Override
	public void dispatchFormRequest(UserRequest ureq) {
		float newUserRating = userComponent.getCurrentRating();
		getRootForm().fireFormEvent(ureq, new RatingFormEvent(this, newUserRating));

		long correctedNumOfRatings = numOfRatings;
		if(initialUserRating <= 0f) {
			String[] args = new String[]{ Long.toString(++correctedNumOfRatings)};
			String explanation = translator.translate("rating.average.explanation", args);
			averageComponent.getFormItemComponent().setExplanation(explanation);
		}
		
		float newAverageRating;
		if(lastUserRating > 0f) {
			float sumOfRatings = correctedNumOfRatings * averageComponent.getCurrentRating();
			float newSumOfRatings = (sumOfRatings - lastUserRating) + newUserRating;
			newAverageRating = newSumOfRatings / correctedNumOfRatings;
		} else {
			float sumOfRatings = numOfRatings * averageComponent.getCurrentRating();
			float newSumOfRatings = sumOfRatings + newUserRating;
			newAverageRating = newSumOfRatings / correctedNumOfRatings;
		}
		averageComponent.setCurrentRating(newAverageRating);
		
		lastUserRating = newUserRating;
		component.setDirty(true);
	}

	@Override
	public void reset() {
		//
	}
	
	
}
