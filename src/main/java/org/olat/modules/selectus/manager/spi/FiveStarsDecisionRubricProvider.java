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
package org.olat.modules.selectus.manager.spi;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.rating.RatingFormItem;
import org.olat.core.gui.components.rating.RatingType;
import org.olat.core.util.CodeHelper;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.manager.DecisionRubricSPI;
import org.olat.modules.selectus.model.DecisionRubric;

/**
 * 
 * Initial date: 18 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class FiveStarsDecisionRubricProvider implements DecisionRubricSPI {

	@Override
	public String getKey() {
		return "abc";
	}

	@Override
	public String getName() {
		return "5 star";
	}

	@Override
	public String getValue(DecisionRubric rubric) {
		if(rubric != null && rubric.getIntegerValue() != null) {
			int value = rubric.getIntegerValue().intValue();
			switch(value) {
				case 5: return "*****";
				case 4: return "****";
				case 3: return "***";
				case 2: return "**";
				case 1: return "*";
				default: return null;
			}
		}
		return null;
	}

	@Override
	public double getNumericalNormalizedValue(DecisionRubric rubric) {
		if(rubric != null && rubric.getIntegerValue() != null) {
			int value = rubric.getIntegerValue().intValue();
			switch(value) {
				case 5: return 1.0d;
				case 4: return 0.8d;
				case 3: return 0.6d;
				case 2: return 0.4d;
				case 1: return 0.2d;
				case 0: return 0.0d;
				default: return 0.0d;
			}
		}
		return 0.0d;
	}
	
	@Override
	public void commitValue(DecisionRubric rubric, FormItem item) {
		if(item instanceof RatingFormItem rItem) {
			float rating = rItem.getCurrentRating();
			rubric.setIntegerValue(Math.round(rating));
		}
	}

	@Override
	public FormItem createElement(DecisionRubric rubric, FormItemContainer formLayout, FormUIFactory uifactory) {
		int initialRating = 0;
		if(rubric != null && rubric.getIntegerValue() != null) {
			initialRating = rubric.getIntegerValue().intValue();
		}
		if(initialRating < 0) {
			initialRating = 0;
		}
		if(initialRating > 5) {
			initialRating = 5;
		}
		return new RatingFormItem("rating5star-" + CodeHelper.getRAMUniqueID(), RatingType.stars, initialRating, 5, true);
	}
}
