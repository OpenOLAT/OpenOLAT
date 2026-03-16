/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.rating;

import java.util.List;

import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * 
 * Initial date: 26 sept. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RatingsOverviewFormItem extends FormItemImpl {
	
	private RatingsOverviewComponent component;
	
	public RatingsOverviewFormItem(String name) {
		super(name);
		component = new RatingsOverviewComponent(name);
	}
	
	public List<UserRating> getRatings() {
		return component.getRatings();
	}

	public void setRatings(List<UserRating> ratings) {
		component.setRatings(ratings);
	}
	
	public void setDomReplacementWrapperRequired(boolean domReplacementWrapperRequired) {
		component.setDomReplacementWrapperRequired(domReplacementWrapperRequired);
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
		//
	}
}
