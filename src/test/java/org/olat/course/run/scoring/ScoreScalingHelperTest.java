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
package org.olat.course.run.scoring;

import java.math.BigDecimal;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 5 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ScoreScalingHelperTest {
	
	@Test
	public void parseFraction() {
		BigDecimal half = ScoreScalingHelper.getScoreScale("1/2");
		Assertions.assertThat(half).isEqualByComparingTo(BigDecimal.valueOf(0.5d));
	}
	
	@Test
	public void parseIndeterminedFraction() {
		BigDecimal third = ScoreScalingHelper.getScoreScale("1/3");
		Assertions.assertThat(0.3333333d).isCloseTo(third.doubleValue(), Assertions.within(0.000001d));
	}
	
	@Test
	public void parseInteger() {
		BigDecimal three = ScoreScalingHelper.getScoreScale("3");
		Assertions.assertThat(three).isEqualByComparingTo(BigDecimal.valueOf(3));
	}
	
	@Test
	public void parseDouble() {
		BigDecimal doubleValue = ScoreScalingHelper.getScoreScale("1.25");
		Assert.assertEquals(1.25d, doubleValue.doubleValue(), 0.000001);
	}
	
	@Test
	public void thirdSum() {
		BigDecimal third = ScoreScalingHelper.getScoreScale("1/3");
		
		float score = ScoreScalingHelper.getWeightedFloatScore(BigDecimal.TEN, third);
		score += ScoreScalingHelper.getWeightedFloatScore(BigDecimal.TEN, third);
		score += ScoreScalingHelper.getWeightedFloatScore(BigDecimal.TEN, third);
		Assert.assertEquals(10f, score, 0.0000001);
	}

}
