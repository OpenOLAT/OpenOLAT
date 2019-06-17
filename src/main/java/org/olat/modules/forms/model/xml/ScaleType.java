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
package org.olat.modules.forms.model.xml;

import java.util.Arrays;

import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 04.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public enum ScaleType {
	zeroToMax {
		@Override
		public double getStepValue(int numberOfSteps, int step) {
			return step - 1;
		}
	},
	oneToMax {
		@Override
		public double getStepValue(int numberOfSteps, int step) {
			return step;
		}
	},
	maxToOne {
		@Override
		public double getStepValue(int numberOfSteps, int step) {
			return numberOfSteps + 1 - step;
		}
	},
	maxToZero {
		@Override
		public double getStepValue(int numberOfSteps, int step) {
			return numberOfSteps - step;
		}
	},
	zeroBallanced {
		@Override
		public double getStepValue(int numberOfSteps, int step) {
			double offset = (numberOfSteps - 1) / 2.0;
			return step - 1 - offset;
		}
	};
	
	/**
	 * Get the value of a step to use for calculations.
	 *
	 * @param numberOfSteps the total number of steps
	 * @param step the index of the step (index starts with 1)
	 * @return 
	 */
	public abstract double getStepValue(int numberOfSteps, int step);

	public String getKey() {
		return name();
	}
	
	public static ScaleType getEnum(String key) {
		return ScaleType.valueOf(key);
	}

	public static String[] getKeys() {
		return Arrays.stream(ScaleType.values()).map(ScaleType::name).toArray(String[]::new);
	}
	
	public static String[] getValues(Translator translator) {
		return Arrays.stream(ScaleType.values())
				.map(type -> "rubric.scale." + type.name())
				.map(i18n -> translator.translate(i18n))
				.toArray(String[]::new);
	}
}
