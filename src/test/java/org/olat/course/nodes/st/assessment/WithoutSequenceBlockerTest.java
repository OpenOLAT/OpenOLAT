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
package org.olat.course.nodes.st.assessment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.Test;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 22 Oct 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class WithoutSequenceBlockerTest {

	@Test
	public void shouldBock() {
		WithoutSequenceBlocker sut = new WithoutSequenceBlocker(null, AssessmentObligation.mandatory);
		assertThat(sut.isBlocked()).isFalse();
		assertThat(sut.getStartDate()).isNull();
		
		sut.block();
		assertThat(sut.isBlocked()).isTrue();
		assertThat(sut.getStartDate()).isNull();
		
		sut.nextCourseNode();
		assertThat(sut.isBlocked()).isFalse();
		assertThat(sut.getStartDate()).isNull();
	}
	
	@Test
	public void shouldBlockWithDate() {
		Date date = new Date();
		WithoutSequenceBlocker sut = new WithoutSequenceBlocker(null, AssessmentObligation.mandatory);
		assertThat(sut.isBlocked()).isFalse();
		assertThat(sut.getStartDate()).isNull();
		
		sut.block(date);
		assertThat(sut.isBlocked()).isTrue();
		assertThat(sut.getStartDate()).isEqualTo(date);
		
		sut.nextCourseNode();
		assertThat(sut.isBlocked()).isFalse();
		assertThat(sut.getStartDate()).isNull();
	}

	@Test
	public void shouldBlockPathThrough() {
		WithoutSequenceBlocker sut = new WithoutSequenceBlocker(null, AssessmentObligation.mandatory);
		assertThat(sut.isBlocked()).isFalse();
		assertThat(sut.getStartDate()).isNull();
		
		sut.blockNoPassThrough();
		assertThat(sut.isBlocked()).isTrue();
		assertThat(sut.getStartDate()).isNull();
		
		sut.nextCourseNode();
		assertThat(sut.isBlocked()).isFalse();
		assertThat(sut.getStartDate()).isNull();
	}

	@Test
	public void shouldBlockIfNoObligations() {
		WithoutSequenceBlocker sut = new WithoutSequenceBlocker(null, AssessmentObligation.mandatory);
		assertThat(sut.isBlocked()).isFalse();
		
		sut.block();
		assertThat(sut.isBlocked()).isTrue();
	}

	@Test
	public void shouldIgnoreBlockIfExcluded() {
		WithoutSequenceBlocker sut = new WithoutSequenceBlocker(null, AssessmentObligation.excluded);
		assertThat(sut.isBlocked()).isFalse();
		
		sut.block();
		assertThat(sut.isBlocked()).isFalse();
	}
	
	@Test
	public void shouldIgnoreBlockWithDateExcluded() {
		Date date = new Date();
		WithoutSequenceBlocker sut = new WithoutSequenceBlocker(null, AssessmentObligation.excluded);
		assertThat(sut.isBlocked()).isFalse();
		assertThat(sut.getStartDate()).isNull();
		
		sut.block(date);
		assertThat(sut.isBlocked()).isFalse();
		assertThat(sut.getStartDate()).isNull();
	}

	@Test
	public void shouldIgnoreBlockPathThroughExcluded() {
		WithoutSequenceBlocker sut = new WithoutSequenceBlocker(null, AssessmentObligation.excluded);
		assertThat(sut.isBlocked()).isFalse();
		
		sut.blockNoPassThrough();
		assertThat(sut.isBlocked()).isFalse();
	}

}
