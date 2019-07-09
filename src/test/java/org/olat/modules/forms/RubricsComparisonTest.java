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
package org.olat.modules.forms;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.olat.modules.forms.RubricsComparison.Attribute;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Rubric.SliderType;
import org.olat.modules.forms.model.xml.ScaleType;
import org.olat.modules.forms.model.xml.Slider;

/**
 * 
 * Initial date: 9 Jul 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricsComparisonTest {
	
	@Test
	public void shouldReturnIdentical() {
		Rubric rubric1 = createRubric();
		Rubric rubric2 = createRubric();
		Rubric rubric3 = createRubric();
		
		Attribute[] all = RubricsComparison.Attribute.values();
		boolean areIdentical = RubricsComparison.areIdentical(asList(rubric1, rubric2, rubric3), all);

		assertThat(areIdentical).isTrue();
	}
	
	@Test
	public void shouldReturnIdenticalIfNoCheck() {
		Rubric rubric1 = createRubric();
		Rubric rubric2 = createRubric();
		Rubric rubric3 = createRubric();
		
		boolean areIdentical = RubricsComparison.areIdentical(asList(rubric1, rubric2, rubric3));

		assertThat(areIdentical).isTrue();
	}
	
	@Test
	public void shouldFindDifferentSliderType() {
		Rubric rubric1 = createRubric();
		Rubric rubric2 = createRubric();
		rubric2.setSliderType(SliderType.discrete);
		
		findDifference(rubric1, rubric2, Attribute.sliderType);
	}
	
	@Test
	public void shouldFindDifferentScaleType() {
		Rubric rubric1 = createRubric();
		Rubric rubric2 = createRubric();
		rubric2.setScaleType(ScaleType.maxToZero);
		
		findDifference(rubric1, rubric2, Attribute.scaleType);
	}
	
	@Test
	public void shouldFindDifferentName() {
		Rubric rubric1 = createRubric();
		Rubric rubric2 = createRubric();
		rubric2.setName("new name");
		
		findDifference(rubric1, rubric2, Attribute.name);
	}
	
	@Test
	public void shouldFindDifferentStart() {
		Rubric rubric1 = createRubric();
		Rubric rubric2 = createRubric();
		rubric2.setStart(3);
		
		findDifference(rubric1, rubric2, Attribute.start);
	}
	
	@Test
	public void shouldFindDifferentEnd() {
		Rubric rubric1 = createRubric();
		Rubric rubric2 = createRubric();
		rubric2.setEnd(11);
		
		findDifference(rubric1, rubric2, Attribute.end);
	}
	
	@Test
	public void shouldFindDifferentSteps() {
		Rubric rubric1 = createRubric();
		Rubric rubric2 = createRubric();
		rubric2.setSteps(101);
		
		findDifference(rubric1, rubric2, Attribute.steps);
	}
	
	@Test
	public void shouldFindDifferentNoResponseEnabled() {
		Rubric rubric1 = createRubric();
		Rubric rubric2 = createRubric();
		rubric2.setNoResponseEnabled(true);
		
		findDifference(rubric1, rubric2, Attribute.noResponseEnabled);
	}
	
	@Test
	public void shouldFindDifferentLowerBoundInsufficient() {
		Rubric rubric1 = createRubric();
		Rubric rubric2 = createRubric();
		rubric2.setLowerBoundInsufficient(null);
		
		findDifference(rubric1, rubric2, Attribute.lowerBoundInsufficient);
	}
	
	@Test
	public void shouldFindDifferentUpperBoundInsufficient() {
		Rubric rubric1 = createRubric();
		Rubric rubric2 = createRubric();
		rubric2.setUpperBoundInsufficient(null);
		
		findDifference(rubric1, rubric2, Attribute.upperBoundInsufficient);
	}
	
	@Test
	public void shouldFindDifferentLowerBoundNeutral() {
		Rubric rubric1 = createRubric();
		Rubric rubric2 = createRubric();
		rubric2.setLowerBoundNeutral(null);
		
		findDifference(rubric1, rubric2, Attribute.lowerBoundNeutral);
	}
	
	@Test
	public void shouldFindDifferentUpperBoundNeutral() {
		Rubric rubric1 = createRubric();
		Rubric rubric2 = createRubric();
		rubric2.setUpperBoundNeutral(null);
		
		findDifference(rubric1, rubric2, Attribute.upperBoundNeutral);
	}
	
	@Test
	public void shouldFindDifferentLowerBoundSufficient() {
		Rubric rubric1 = createRubric();
		Rubric rubric2 = createRubric();
		rubric2.setLowerBoundSufficient(null);
		
		findDifference(rubric1, rubric2, Attribute.lowerBoundSufficient);
	}
	
	@Test
	public void shouldFindDifferentUpperBoundSufficient() {
		Rubric rubric1 = createRubric();
		Rubric rubric2 = createRubric();
		rubric2.setUpperBoundSufficient(null);
		
		findDifference(rubric1, rubric2, Attribute.upperBoundSufficient);
	}
	
	@Test
	public void shouldFindDifferentStartGoodRating() {
		Rubric rubric1 = createRubric();
		Rubric rubric2 = createRubric();
		rubric2.setStartGoodRating(true);
		
		findDifference(rubric1, rubric2, Attribute.startGoodRating);
	}
	
	@Test
	public void shouldFindDifferentSliderWeighted() {
		Rubric rubric1 = createRubric();
		Slider slider1 = new Slider();
		rubric1.setSliders(asList(slider1));
		Rubric rubric2 = createRubric();
		Slider slider2 = new Slider();
		slider2.setWeight(9);
		rubric2.setSliders(asList(slider2));
		
		findDifference(rubric1, rubric2, Attribute.slidersWeighted);
	}
	
	@Test
	public void shouldFindDifferentSlidersLabel() {
		Rubric rubric1 = createRubric();
		Slider slider1 = new Slider();
		slider1.setStartLabel("start");
		rubric1.setSliders(asList(slider1));
		Rubric rubric2 = createRubric();
		Slider slider2 = new Slider();
		slider2.setStartLabel("start");;
		slider2.setEndLabel("end");
		rubric2.setSliders(asList(slider2));
		
		findDifference(rubric1, rubric2, Attribute.slidersLabel);
	}
	
	private void findDifference(Rubric rubric1, Rubric rubric2, Attribute... attribute) {
		boolean areIdentical = RubricsComparison.areIdentical(asList(rubric1, rubric2), attribute);

		assertThat(areIdentical).isFalse();
	}

	private Rubric createRubric() {
		Rubric rubric = new Rubric();
		rubric.setSliderType(SliderType.continuous);
		rubric.setScaleType(ScaleType.maxToOne);
		rubric.setName("rubric");
		rubric.setStart(1);
		rubric.setEnd(10);
		rubric.setSteps(10);
		rubric.setNoResponseEnabled(false);
		rubric.setLowerBoundInsufficient(1.0);
		rubric.setUpperBoundInsufficient(4.0);
		rubric.setLowerBoundNeutral(4.0);
		rubric.setUpperBoundNeutral(8.0);
		rubric.setLowerBoundSufficient(8.0);
		rubric.setUpperBoundSufficient(10.0);
		rubric.setStartGoodRating(false);
		return rubric;
	}


}
