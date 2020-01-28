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
package org.olat.course.learningpath.evaluation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Date;

import org.junit.Test;
import org.olat.core.util.DateUtils;
import org.olat.course.run.scoring.Blocker;

/**
 * 
 * Initial date: 26 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConfigEndDateEvaluatorTest {
	
	private ConfigEndDateEvaluator sut = new ConfigEndDateEvaluator();

	@Test
	public void shouldNotBlockIfNotFullyAssessedAndConfigDateNotOver() {
		Blocker blocker = new Blocker();
		Date configDateNotOver = DateUtils.toDate(LocalDate.now().plusDays(3));
		
		sut.evaluateBlocker(Boolean.FALSE, configDateNotOver, blocker);
		
		assertThat(blocker.isBlocked()).isFalse();
	}

	@Test
	public void shouldNotBlockIfFullyAssessedAndConfigDateNotOver() {
		Blocker blocker = new Blocker();
		Date configDateNotOver = DateUtils.toDate(LocalDate.now().plusDays(3));
		
		sut.evaluateBlocker(Boolean.TRUE, configDateNotOver, blocker);
		
		assertThat(blocker.isBlocked()).isFalse();
	}

	@Test
	public void shouldNotBlockIfUnknownFullyAssessedAndConfigDateNotOver() {
		Blocker blocker = new Blocker();
		Date configDateNotOver = DateUtils.toDate(LocalDate.now().plusDays(3));
		
		sut.evaluateBlocker(null, configDateNotOver, blocker);
		
		assertThat(blocker.isBlocked()).isFalse();
	}

	@Test
	public void shouldBlockIfNotFullyAssessedAndConfigDateOver() {
		Blocker blocker = new Blocker();
		Date configDateOver = DateUtils.toDate(LocalDate.now().minusDays(3));
		
		sut.evaluateBlocker(Boolean.FALSE, configDateOver, blocker);
		
		assertThat(blocker.isBlocked()).isTrue();
	}

	@Test
	public void shouldNotBlockIfFullyAssessedAndConfigDateOver() {
		Blocker blocker = new Blocker();
		Date configDateOver = DateUtils.toDate(LocalDate.now().minusDays(3));
		
		sut.evaluateBlocker(Boolean.TRUE, configDateOver, blocker);
		
		assertThat(blocker.isBlocked()).isFalse();
	}

	@Test
	public void shouldBlockIfUnknownFullyAssessedAndConfigDateOver() {
		Blocker blocker = new Blocker();
		Date configDateOver = DateUtils.toDate(LocalDate.now().minusDays(3));
		
		sut.evaluateBlocker(null, configDateOver, blocker);
		
		assertThat(blocker.isBlocked()).isTrue();
	}
	

}
