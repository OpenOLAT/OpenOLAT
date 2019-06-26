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
package org.olat.modules.forms.model;

import org.olat.modules.forms.RubricStatistic;
import org.olat.modules.forms.SliderStatistic;
import org.olat.modules.forms.SlidersStatistic;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Slider;

/**
 * 
 * Initial date: 25 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricStatisticImpl implements RubricStatistic {
	
	private final Rubric rubric;
	private final SlidersStatistic slidersStatistic;
	private final SliderStatistic totalStatistic;

	public RubricStatisticImpl(Rubric rubric, SlidersStatistic slidersStatistic, SliderStatistic totalStatistic) {
		this.rubric = rubric;
		this.slidersStatistic = slidersStatistic;
		this.totalStatistic = totalStatistic;
	}

	@Override
	public Rubric getRubric() {
		return rubric;
	}

	@Override
	public SliderStatistic getSliderStatistic(Slider slider) {
		return slidersStatistic.getSliderStatistic(slider);
	}

	@Override
	public SliderStatistic getTotalStatistic() {
		return totalStatistic;
	}

}
