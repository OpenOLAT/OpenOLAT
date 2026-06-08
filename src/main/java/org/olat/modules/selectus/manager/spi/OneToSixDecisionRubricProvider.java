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
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.model.DecisionRubric;

/**
 * 
 * Max is 10
 * 
 * Initial date: 18 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OneToSixDecisionRubricProvider extends AbstractSelectionDecisionRubricProvider {

	@Override
	public String getKey() {
		return "1-6";
	}

	@Override
	public String getName() {
		return "1 (min) - 6 (max)";
	}

	@Override
	public double getNumericalNormalizedValue(DecisionRubric rubric) {
		if(rubric != null && rubric.getIntegerValue() != null) {
			switch(rubric.getIntegerValue().intValue()) {
				case 6: return 1.0d;
				case 5: return 0.8d;
				case 4: return 0.6d;
				case 3: return 0.4d;
				case 2: return 0.2d;
				case 1: return 0.0d;
			}
		}
		return 0;
	}

	@Override
	public FormItem createElement(DecisionRubric rubric, FormItemContainer formLayout, FormUIFactory uifactory) {
		String[] theKeys = new String[]{ "-", "6", "5", "4", "3", "2", "1"};
		return super.createElement(rubric, theKeys, theKeys, formLayout, uifactory);
	}
}
