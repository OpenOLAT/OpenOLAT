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

import java.util.HashMap;
import java.util.Map;

import org.olat.modules.forms.SlidersStepCounts;
import org.olat.modules.forms.StepCounts;
import org.olat.modules.forms.model.xml.Slider;

/**
 * 
 * Initial date: 2 Jul 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SlidersStepCountsImpl implements SlidersStepCounts {

	private final Map<Slider, StepCounts> sliderToStepCounts = new HashMap<>();
	
	public void put(Slider slider, StepCounts stepCounts) {
		sliderToStepCounts.put(slider, stepCounts);
	}

	@Override
	public StepCounts getStepCounts(Slider slider) {
		return sliderToStepCounts.get(slider);
	}

}
