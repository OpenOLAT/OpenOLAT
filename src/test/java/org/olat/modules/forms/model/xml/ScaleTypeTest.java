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


import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

/**
 * 
 * Initial date: 04.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ScaleTypeTest {
	
	@Test
	public void shouldGetStepValueForOneToMax() {
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(ScaleType.oneToMax.getStepValue(6, 1)).isEqualTo(1);
		softly.assertThat(ScaleType.oneToMax.getStepValue(6, 2)).isEqualTo(2);
		softly.assertThat(ScaleType.oneToMax.getStepValue(6, 3)).isEqualTo(3);
		softly.assertThat(ScaleType.oneToMax.getStepValue(6, 4)).isEqualTo(4);
		softly.assertThat(ScaleType.oneToMax.getStepValue(6, 5)).isEqualTo(5);
		softly.assertThat(ScaleType.oneToMax.getStepValue(6, 6)).isEqualTo(6);
		softly.assertAll();
	}

	@Test
	public void shouldGetStepValueForMaxToOne() {
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(ScaleType.maxToOne.getStepValue(6, 1)).isEqualTo(6);
		softly.assertThat(ScaleType.maxToOne.getStepValue(6, 2)).isEqualTo(5);
		softly.assertThat(ScaleType.maxToOne.getStepValue(6, 3)).isEqualTo(4);
		softly.assertThat(ScaleType.maxToOne.getStepValue(6, 4)).isEqualTo(3);
		softly.assertThat(ScaleType.maxToOne.getStepValue(6, 5)).isEqualTo(2);
		softly.assertThat(ScaleType.maxToOne.getStepValue(6, 6)).isEqualTo(1);
		softly.assertAll();
	}
	
	@Test
	public void shouldGetStepValueForZeroToMax() {
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(ScaleType.zeroToMax.getStepValue(6, 1)).isEqualTo(0);
		softly.assertThat(ScaleType.zeroToMax.getStepValue(6, 2)).isEqualTo(1);
		softly.assertThat(ScaleType.zeroToMax.getStepValue(6, 3)).isEqualTo(2);
		softly.assertThat(ScaleType.zeroToMax.getStepValue(6, 4)).isEqualTo(3);
		softly.assertThat(ScaleType.zeroToMax.getStepValue(6, 5)).isEqualTo(4);
		softly.assertThat(ScaleType.zeroToMax.getStepValue(6, 6)).isEqualTo(5);
		softly.assertAll();
	}

	@Test
	public void shouldGetStepValueForMaxToZero() {
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(ScaleType.maxToZero.getStepValue(6, 1)).isEqualTo(5);
		softly.assertThat(ScaleType.maxToZero.getStepValue(6, 2)).isEqualTo(4);
		softly.assertThat(ScaleType.maxToZero.getStepValue(6, 3)).isEqualTo(3);
		softly.assertThat(ScaleType.maxToZero.getStepValue(6, 4)).isEqualTo(2);
		softly.assertThat(ScaleType.maxToZero.getStepValue(6, 5)).isEqualTo(1);
		softly.assertThat(ScaleType.maxToZero.getStepValue(6, 6)).isEqualTo(0);
		softly.assertAll();
	}
	
	@Test
	public void shouldGetValueForZeroBalanced() {
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(ScaleType.zeroBallanced.getStepValue(6, 1)).isEqualTo(-2.5);
		softly.assertThat(ScaleType.zeroBallanced.getStepValue(6, 2)).isEqualTo(-1.5);
		softly.assertThat(ScaleType.zeroBallanced.getStepValue(6, 3)).isEqualTo(-0.5);
		softly.assertThat(ScaleType.zeroBallanced.getStepValue(6, 4)).isEqualTo(0.5);
		softly.assertThat(ScaleType.zeroBallanced.getStepValue(6, 5)).isEqualTo(1.5);
		softly.assertThat(ScaleType.zeroBallanced.getStepValue(6, 6)).isEqualTo(2.5);

		softly.assertThat(ScaleType.zeroBallanced.getStepValue(7, 1)).isEqualTo(-3);
		softly.assertThat(ScaleType.zeroBallanced.getStepValue(7, 2)).isEqualTo(-2);
		softly.assertThat(ScaleType.zeroBallanced.getStepValue(7, 3)).isEqualTo(-1);
		softly.assertThat(ScaleType.zeroBallanced.getStepValue(7, 4)).isEqualTo(0);
		softly.assertThat(ScaleType.zeroBallanced.getStepValue(7, 5)).isEqualTo(1);
		softly.assertThat(ScaleType.zeroBallanced.getStepValue(7, 6)).isEqualTo(2);
		softly.assertThat(ScaleType.zeroBallanced.getStepValue(7, 7)).isEqualTo(3);
		softly.assertAll();
	}

}
