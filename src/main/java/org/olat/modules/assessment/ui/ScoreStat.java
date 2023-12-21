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
package org.olat.modules.assessment.ui;

/**
 * 
 * Initial date: 16 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ScoreStat {
	
	private static final ScoreStat NO_SCORE = new ScoreStat(false, 0.0d, 0.0d, 0.0d, 0.0d, false, false);
	
	private final boolean enabled;
	private final Double min;
	private final Double max;
	private final Double weightedMin;
	private final Double weightedMax;
	private final boolean gradeEnabled;
	private final boolean scoreScaleEnabled;
	
	public static ScoreStat noScore() {
		return NO_SCORE;
	}
	
	public static ScoreStat of(Double min, Double max, Double weightedMin, Double weightedMax, boolean gradeEnabled, boolean scoreScaleEnabled) {
		return new ScoreStat(true, min, max, weightedMin, weightedMax, gradeEnabled, scoreScaleEnabled);
	}
	
	private ScoreStat(boolean enabled, Double min, Double max, Double weightedMin, Double weightedMax, boolean gradeEnabled, boolean scoreScaleEnabled) {
		this.enabled = enabled;
		this.min = min;
		this.max = max;
		this.weightedMin = weightedMin;
		this.weightedMax = weightedMax;
		this.gradeEnabled = gradeEnabled;
		this.scoreScaleEnabled = scoreScaleEnabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Double getMin() {
		return min;
	}

	public Double getMax() {
		return max;
	}

	public Double getWeightedMin() {
		return weightedMin;
	}

	public Double getWeightedMax() {
		return weightedMax;
	}

	public boolean isGradeEnabled() {
		return gradeEnabled;
	}
	
	public boolean isScoreScaleEnabled() {
		return scoreScaleEnabled;
	}
	
}
