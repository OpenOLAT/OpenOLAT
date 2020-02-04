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
import org.olat.course.nodes.st.assessment.SequentialBlocker;
import org.olat.course.run.scoring.Blocker;

/**
 * 
 * Initial date: 5 Nov 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConfigStartDateEvaluatorTest {
	
	private ConfigStartDateEvaluator sut = new ConfigStartDateEvaluator();
	
	@Test
	public void shouldBlockIfStartIsInFuture() {
		Blocker blocker = new SequentialBlocker();
		Date inThreeDays = DateUtils.toDate(LocalDate.now().plusDays(3));
		
		sut.evaluateDate(inThreeDays, blocker);
		
		assertThat(blocker.isBlocked()).isTrue();
	}
	
	@Test
	public void shouldNotBlockIfStartIsInPast() {
		Blocker blocker = new SequentialBlocker();
		Date threeDaysBefore = DateUtils.toDate(LocalDate.now().minusDays(3));
		
		sut.evaluateDate(threeDaysBefore, blocker);
		
		assertThat(blocker.isBlocked()).isFalse();
	}
	
	@Test
	public void shouldNotBlockIfHasNoStart() {
		Blocker blocker = new SequentialBlocker();
		
		sut.evaluateDate(null, blocker);
		
		assertThat(blocker.isBlocked()).isFalse();
	}

	@Test
	public void shouldReturnStartDateIfBlocks() {
		Blocker blocker = new SequentialBlocker();
		Date inThreeDays = DateUtils.toDate(LocalDate.now().plusDays(3));
		
		sut.evaluateDate(inThreeDays, blocker);
		
		assertThat(blocker.getStartDate()).isEqualTo(inThreeDays);
	}

	@Test
	public void shouldReturnBlockerStartIfBlockerIsLater() {
		Date inFourDays = DateUtils.toDate(LocalDate.now().plusDays(4));
		Date inThreeDays = DateUtils.toDate(LocalDate.now().plusDays(3));
		Blocker blocker = new SequentialBlocker();
		blocker.block(inFourDays);
		
		sut.evaluateDate(inThreeDays, blocker);
		
		assertThat(blocker.getStartDate()).isEqualTo(inFourDays);
	}

	@Test
	public void shouldReturnNodeStartIfNodeIsLater() {
		Date inTwoDays = DateUtils.toDate(LocalDate.now().plusDays(2));
		Date inThreeDays = DateUtils.toDate(LocalDate.now().plusDays(3));
		Blocker blocker = new SequentialBlocker();
		blocker.block(inTwoDays);
		
		sut.evaluateDate(inThreeDays, blocker);
		
		assertThat(blocker.getStartDate()).isEqualTo(inThreeDays);
	}

	@Test
	public void shouldReturnBlockerStartIfNodeIsNotBlocking() {
		Date inFourDays = DateUtils.toDate(LocalDate.now().plusDays(4));
		Blocker blocker = new SequentialBlocker();
		blocker.block(inFourDays);
		
		sut.evaluateDate(null, blocker);
		
		assertThat(blocker.getStartDate()).isEqualTo(inFourDays);
	}

}
