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
package org.olat.modules.forms.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SliderStatistic;
import org.olat.modules.forms.model.xml.Rubric;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricTotalBarChartsController extends RubricBarChartsController {
	
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	public RubricTotalBarChartsController(UserRequest ureq, WindowControl wControl, Rubric rubric,
			SessionFilter filter) {
		super(ureq, wControl, rubric, filter);
		initForm(ureq);
	}
	
	@Override
	protected RubricWrapper createRubricWrapper() {
		String name = translate("rubric.report.total", new String[] {getRubric().getName()});
		RubricStatistic rubricStatistic = evaluationFormManager.getRubricStatistic(getRubric(), getFilter());
		SliderStatistic totalStatistic = rubricStatistic.getTotalStatistic();
		List<SliderWrapper> sliderWrappers = new ArrayList<>();
		SliderWrapper sliderWrapper = createSliderWrapper(name, null, totalStatistic);
		sliderWrappers.add(sliderWrapper);
		RubricWrapper rubricWrapper = new RubricWrapper(getRubric());
		rubricWrapper.setSliders(sliderWrappers);
		return rubricWrapper;
	}

}
