/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.decision;

import java.util.ArrayList;
import java.util.List;

import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.ui.UserRatingMapper;
import org.olat.modules.selectus.ui.model.ApplicationLightRow;
import org.olat.modules.selectus.ui.rating.RatingsOverviewFormItem;

/**
 * 
 * Initial date: 18 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationRubricsRow implements ApplicationLightRow {

	private double sum;
	private UserRatingMapper userRatingMapper;
	private final ApplicationLight application;
	private RatingsOverviewFormItem ratingOverviewItem;
	
	private final List<ApplicationRubric> rubrics = new ArrayList<>();
	
	public ApplicationRubricsRow(ApplicationLight application) {
		this.application = application;
	}
	
	@Override
	public ApplicationLight getApplication() {
		return application;
	}
	
	public UserRatingMapper getUserRatingMapper() {
		return userRatingMapper;
	}

	public void setUserRatingMapper(UserRatingMapper userRatingMapper) {
		this.userRatingMapper = userRatingMapper;
	}

	public RatingsOverviewFormItem getRatingOverviewItem() {
		return ratingOverviewItem;
	}

	public void setRatingOverviewItem(RatingsOverviewFormItem ratingOverviewItem) {
		this.ratingOverviewItem = ratingOverviewItem;
	}
	
	public double getSum() {
		return sum;
	}

	public void setSum(double sum) {
		this.sum = sum;
	}

	public List<ApplicationRubric> getApplicationRubrics() {
		return new ArrayList<>(rubrics);
	}
	
	public ApplicationRubric getApplicationRubric(int index) {
		if(index >= 0 && index < rubrics.size()) {
			return rubrics.get(index);
		}
		return null;
	}
	
	public void addApplicationRubric(ApplicationRubric rubric) {
		rubrics.add(rubric);
	}
}
