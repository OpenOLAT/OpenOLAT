/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.learningpath.obligation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: Mar 21, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CourseRunExceptionalObligationHandlerTest {

	@Test
	public void should() {
		CourseRunExceptionalObligationHandler sut = new CourseRunExceptionalObligationHandler();
		Identity identity = new TransientIdentity();
		TestingObligationContext obligationContext = new TestingObligationContext();
		
		CourseRunExceptionalObligation exceptionalObligation = new CourseRunExceptionalObligation();
		exceptionalObligation.setOperand(3);

		exceptionalObligation.setOperator("<");
		obligationContext.addCourseRun(identity, 2l);
		assertThat(sut.matchesIdentity(exceptionalObligation, identity, obligationContext, null, null, null)).isTrue();
		obligationContext.addCourseRun(identity, 3l);
		assertThat(sut.matchesIdentity(exceptionalObligation, identity, obligationContext, null, null, null)).isFalse();
		obligationContext.addCourseRun(identity, 4l);
		assertThat(sut.matchesIdentity(exceptionalObligation, identity, obligationContext, null, null, null)).isFalse();
		
		exceptionalObligation.setOperator("<=");
		obligationContext.addCourseRun(identity, 2l);
		assertThat(sut.matchesIdentity(exceptionalObligation, identity, obligationContext, null, null, null)).isTrue();
		obligationContext.addCourseRun(identity, 3l);
		assertThat(sut.matchesIdentity(exceptionalObligation, identity, obligationContext, null, null, null)).isTrue();
		obligationContext.addCourseRun(identity, 4l);
		assertThat(sut.matchesIdentity(exceptionalObligation, identity, obligationContext, null, null, null)).isFalse();
		
		exceptionalObligation.setOperator("=");
		obligationContext.addCourseRun(identity, 2l);
		assertThat(sut.matchesIdentity(exceptionalObligation, identity, obligationContext, null, null, null)).isFalse();
		obligationContext.addCourseRun(identity, 3l);
		assertThat(sut.matchesIdentity(exceptionalObligation, identity, obligationContext, null, null, null)).isTrue();
		obligationContext.addCourseRun(identity, 4l);
		assertThat(sut.matchesIdentity(exceptionalObligation, identity, obligationContext, null, null, null)).isFalse();
		
		exceptionalObligation.setOperator("=>");
		obligationContext.addCourseRun(identity, 2l);
		assertThat(sut.matchesIdentity(exceptionalObligation, identity, obligationContext, null, null, null)).isFalse();
		obligationContext.addCourseRun(identity, 3l);
		assertThat(sut.matchesIdentity(exceptionalObligation, identity, obligationContext, null, null, null)).isTrue();
		obligationContext.addCourseRun(identity, 4l);
		assertThat(sut.matchesIdentity(exceptionalObligation, identity, obligationContext, null, null, null)).isTrue();
		
		exceptionalObligation.setOperator(">");
		obligationContext.addCourseRun(identity, 2l);
		assertThat(sut.matchesIdentity(exceptionalObligation, identity, obligationContext, null, null, null)).isFalse();
		obligationContext.addCourseRun(identity, 3l);
		assertThat(sut.matchesIdentity(exceptionalObligation, identity, obligationContext, null, null, null)).isFalse();
		obligationContext.addCourseRun(identity, 4l);
		assertThat(sut.matchesIdentity(exceptionalObligation, identity, obligationContext, null, null, null)).isTrue();
		
		exceptionalObligation.setOperator("-");
		obligationContext.addCourseRun(identity, 2l);
		assertThat(sut.matchesIdentity(exceptionalObligation, identity, obligationContext, null, null, null)).isFalse();
		obligationContext.addCourseRun(identity, 3l);
		assertThat(sut.matchesIdentity(exceptionalObligation, identity, obligationContext, null, null, null)).isFalse();
		obligationContext.addCourseRun(identity, 4l);
		assertThat(sut.matchesIdentity(exceptionalObligation, identity, obligationContext, null, null, null)).isFalse();
	}

}
