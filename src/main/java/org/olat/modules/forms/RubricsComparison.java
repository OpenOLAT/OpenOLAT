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

import java.util.Collection;
import java.util.Iterator;

import org.olat.core.util.StringHelper;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Slider;

/**
 * 
 * Initial date: 9 Jul 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RubricsComparison {
	
	public enum Attribute {
		sliderType,
		scaleType,
		name,
		start,
		end,
		steps,
		noResponseEnabled,
		lowerBoundInsufficient,
		upperBoundInsufficient,
		lowerBoundNeutral,
		upperBoundNeutral,
		lowerBoundSufficient,
		upperBoundSufficient,
		startGoodRating,
		slidersLabel,
		slidersWeighted
	}
	
	private enum LabelType {
		none,
		start,
		end,
		both
	}
	
	public static boolean areIdentical(Collection<Rubric> rubrics, Attribute... attributes) {
		if (rubrics == null || rubrics.size() <= 1) return true;
		
		Iterator<Rubric> iterator = rubrics.iterator();
		Rubric master = iterator.next();
		while (iterator.hasNext()) {
			Rubric rubric = iterator.next();
			if (!areIdentical(master, rubric, attributes)) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean areIdentical(Rubric master, Rubric rubric, Attribute[] attributes) {
		for (Attribute attribute : attributes) {
			if (!areIdentical(master, rubric, attribute)) {
				return false;
			}
		}
		return true;
	}

	private static boolean areIdentical(Rubric master, Rubric rubric, Attribute attribute) {
		switch(attribute) {
		case sliderType: return master.getSliderType() == rubric.getSliderType();
		case scaleType: return master.getScaleType() == rubric.getScaleType();
		//
		case name: return equalsNull(master.getName(), rubric.getName());
		case start: return master.getStart() == rubric.getStart();
		case end: return master.getEnd() == rubric.getEnd();
		case steps: return master.getSteps() == rubric.getSteps();
		//
		case noResponseEnabled: return master.isNoResponseEnabled() == rubric.isNoResponseEnabled();
		case lowerBoundInsufficient: return equalsNull(master.getLowerBoundInsufficient(), rubric.getLowerBoundInsufficient());
		case upperBoundInsufficient: return equalsNull(master.getUpperBoundInsufficient(), rubric.getUpperBoundInsufficient());
		case lowerBoundNeutral: return equalsNull(master.getLowerBoundNeutral(), rubric.getLowerBoundNeutral());
		case upperBoundNeutral: return equalsNull(master.getUpperBoundNeutral(), rubric.getUpperBoundNeutral());
		case lowerBoundSufficient: return equalsNull(master.getLowerBoundSufficient(), rubric.getLowerBoundSufficient());
		case upperBoundSufficient: return equalsNull(master.getUpperBoundSufficient(), rubric.getUpperBoundSufficient());
		case startGoodRating: return master.isStartGoodRating() == rubric.isStartGoodRating();
		case slidersLabel: return getSlidersLabelType(master) == getSlidersLabelType(rubric);
		case slidersWeighted: return hasWeightedSliders(master) == hasWeightedSliders(rubric);
		default: return false;
		}
	}

	private static LabelType getSlidersLabelType(Rubric rubric) {
		boolean start = false;
		boolean end = false;
		for (Slider slider: rubric.getSliders()) {
			if (StringHelper.containsNonWhitespace(slider.getStartLabel())) {
				start = true;
			}
			if (StringHelper.containsNonWhitespace(slider.getEndLabel())) {
				end = true;
			}
		}
		
		if (start && end) return LabelType.both;
		if (start)        return LabelType.start;
		if (end)          return LabelType.end;
		return LabelType.none;
	}

	private static boolean hasWeightedSliders(Rubric rubric) {
		return rubric.getSliders().stream()
				.filter(slider -> slider.getWeight().intValue() != 1)
				.findFirst()
				.isPresent();
	}

	@SuppressWarnings("null")
	private static boolean equalsNull(Object o1, Object o2) {
		if (o1 == null && o2 == null) return true;
		if (o1 == null && o2 != null) return false;
		if (o1 != null && o2 == null) return false;
		return o1.equals(o2);
	}

}
