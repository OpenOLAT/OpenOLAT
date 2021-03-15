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
package org.olat.course.condition;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ConditionTest extends OlatTestCase {
	
	@Autowired
	private BaseSecurity securityManager;
	
	@Test
	public void complexExpression() throws Exception {
		UserCourseEnvironment uce = getUserDemoCourseEnvironment();
		ConditionInterpreter interpreter = new ConditionInterpreter(uce);

		String condition = "(((inLearningGroup(\"Rule1Group1\") | inLearningGroup(\"Rule1Group2\"))|inLearningArea(\"Rule1Area1\")))";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(result);
	}
	
	@Test
	public void testTrueFalse(){
		UserCourseEnvironment uce = getUserDemoCourseEnvironment();
		ConditionInterpreter interpreter = new ConditionInterpreter(uce);
		
		String condition = "true";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(result);
		
		condition = "TRUE";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(result);
		
		condition = "false";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(result);
		
		condition = "FALSE";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(result);
	}

	@Test
	public void testHasPropertyFunction() throws Exception {
		UserCourseEnvironment uce = getUserDemoCourseEnvironment();
		ConditionInterpreter interpreter = new ConditionInterpreter(uce);

		String condition = "hasUserProperty(\"firstName\", \"firstcondition\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(condition, result);

		condition = "hasUserProperty(\"firstName\", \"\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);
		
		condition = "hasUserProperty(\"firstName\", \"firstcondition \")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);
		
		condition = "hasUserProperty(\"firstName\", \" firstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "hasUserProperty(\"firstName\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "hasUserProperty(\"firstName\", \"firstcondition\", \"firstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "hasUserProperty(\"firstName\", \"someThing\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "hasUserProperty(\"firstName\", \"firstconditiongugus\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "hasUserProperty(\"firstName\", \"gugusfirstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "hasUserProperty(\"lastName\", \"firstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);
	}

	@Test
	public void testHasNotPropertyFunction() throws Exception {
		UserCourseEnvironment uce = getUserDemoCourseEnvironment();
		ConditionInterpreter interpreter = new ConditionInterpreter(uce);

		String condition = "hasNotUserProperty(\"firstName\", \"firstcondition\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "hasNotUserProperty(\"firstName\", \"\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "hasNotUserProperty(\"firstName\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "hasNotUserProperty(\"firstName\", \"firstcondition\", \"firstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "hasNotUserProperty(\"firstName\", \"someThing\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(condition, result);

		condition = "hasNotUserProperty(\"firstName\", \"firstconditiongugus\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(condition, result);

		condition = "hasNotUserProperty(\"firstName\", \"gugusfirstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(condition, result);

		condition = "hasNotUserProperty(\"lastName\", \"firstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(condition, result);
	}

	@Test
	public void testUserPropertyStartswithFunction() throws Exception {
		UserCourseEnvironment uce = getUserDemoCourseEnvironment();
		ConditionInterpreter interpreter = new ConditionInterpreter(uce);

		String condition = "userPropertyStartswith(\"firstName\", \"firs\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(condition, result);

		condition = "userPropertyStartswith(\"firstName\", \"firstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(condition, result);
		
		condition = "userPropertyStartswith(\"firstName\", \"\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "userPropertyStartswith(\"firstName\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "userPropertyStartswith(\"firstName\", \"firstcondition\", \"firstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "userPropertyStartswith(\"firstName\", \"someThing\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "userPropertyStartswith(\"firstName\", \"firstconditiongugus\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "userPropertyStartswith(\"firstName\", \"gugusfirstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "userPropertyStartswith(\"lastName\", \"firstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);
	}

	@Test
	public void testUserPropertyEndswithFunction() throws Exception {
		UserCourseEnvironment uce = getUserDemoCourseEnvironment();
		ConditionInterpreter interpreter = new ConditionInterpreter(uce);
		
		String condition = "userPropertyEndswith(\"firstName\", \"ondition\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(condition, result);
		
		condition = "userPropertyEndswith(\"firstName\", \"\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "userPropertyEndswith(\"firstName\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "userPropertyEndswith(\"firstName\", \"firstcondition\", \"firstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "userPropertyEndswith(\"firstName\", \"someThing\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "userPropertyEndswith(\"firstName\", \"firstconditiongugus\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "userPropertyEndswith(\"firstName\", \"gugusfirstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "userPropertyEndswith(\"lastName\", \"firstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);
	}

	@Test
	public void testIsInUserPropertyFunction() throws Exception {
		UserCourseEnvironment uce = getUserDemoCourseEnvironment();
		ConditionInterpreter interpreter = new ConditionInterpreter(uce);

		String condition = "isInUserProperty(\"firstName\", \"firstcondition\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(condition, result);
		
		condition = "isInUserProperty(\"firstName\", \"stcondit\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(condition, result);

		condition = "isInUserProperty(\"firstName\", \"\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "isInUserProperty(\"firstName\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "isInUserProperty(\"firstName\", \"firstcondition\", \"firstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "isInUserProperty(\"firstName\", \"someThing\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "isInUserProperty(\"firstName\", \"firstconditiongugus\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "isInUserProperty(\"firstName\", \"gugusfirstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "isInUserProperty(\"lastName\", \"firstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);
	}

	@Test
	public void testIsNotInUserPropertyFunction() throws Exception {
		UserCourseEnvironment uce = getUserDemoCourseEnvironment();
		ConditionInterpreter interpreter = new ConditionInterpreter(uce);

		String condition = "isNotInUserProperty(\"firstName\", \"stcondit\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "isNotInUserProperty(\"firstName\", \"\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "isNotInUserProperty(\"firstName\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "isNotInUserProperty(\"firstName\", \"firstcondition\", \"firstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);

		condition = "isNotInUserProperty(\"firstName\", \"someThing\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(condition, result);

		condition = "isNotInUserProperty(\"firstName\", \"firstconditiongugus\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(condition, result);

		condition = "isNotInUserProperty(\"firstName\", \"gugusfirstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(condition, result);

		condition = "isNotInUserProperty(\"lastName\", \"firstcondition\")";
		result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(condition, result);
	}
	
	@Test
	public void testIsUser() throws Exception {
		UserCourseEnvironment uce = getUserDemoCourseEnvironment();
		Identity identity = uce.getIdentityEnvironment().getIdentity();
		Authentication olatAuthentication = securityManager.findAuthentication(identity, "OLAT");
		Assert.assertNotNull(olatAuthentication);

		ConditionInterpreter interpreter = new ConditionInterpreter(uce);
		String condition = "isUser(\"" + olatAuthentication.getAuthusername() + "\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(condition, result);
	}
	
	@Test
	public void testIsNotUser() throws Exception {
		UserCourseEnvironment uce = getUserDemoCourseEnvironment();
		
		ConditionInterpreter interpreter = new ConditionInterpreter(uce);
		String condition = "isUser(\"administrator\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);
	}
	
	@Test
	public void testArithmeticException() throws Exception {
		UserCourseEnvironment uce = getUserDemoCourseEnvironment();
		ConditionInterpreter interpreter = new ConditionInterpreter(uce);

		String condition = "2 / 0";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(condition, result);
	}

	
	private UserCourseEnvironment getUserDemoCourseEnvironment() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("junit_auth");
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("condition");
		Roles roles = Roles.userRoles();
		RepositoryEntry re = JunitTestHelper.deployDemoCourse(author);
		ICourse course = CourseFactory.loadCourse(re);
		IdentityEnvironment identityEnv = new IdentityEnvironment(id, roles);
		return new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment());
	}
}
