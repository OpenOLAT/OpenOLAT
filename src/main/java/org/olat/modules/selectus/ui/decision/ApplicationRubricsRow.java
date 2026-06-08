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
