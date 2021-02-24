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
package org.olat.course.condition.interpreter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.UserImpl;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.GenericSelectionPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EvalUserPropertyFunctionTest extends OlatTestCase {
	
	private static UserCourseEnvironment userCourseEnv;
	
	@Autowired
	private UserManager userManager;
	
	@Before
	public void initTest() {
		if(userCourseEnv != null) return;
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("has-user-prop-1");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("has-user-prop-2");
		
		UserImpl user = (UserImpl)id.getUser();
		user.setUserProperty("street", "Guterstrasse 46");
		user.setUserProperty("genericSelectionProperty", "insert");
		user.setUserProperty("genericSelectionProperty2", "School 1,frentix,");
		
		GenericSelectionPropertyHandler selectionHandler = (GenericSelectionPropertyHandler)userManager.getUserPropertiesConfig()
				.getPropertyHandler("genericSelectionProperty2");
		selectionHandler.setMultiSelect(true);
		selectionHandler.setSelectionKeys(new String[] { "frentix", "OpenOlat", "School 1"} );
		selectionHandler.saveConfig();

		Roles roles = Roles.userRoles();
		RepositoryEntry re = JunitTestHelper.deployDemoCourse(author);
		ICourse course = CourseFactory.loadCourse(re);
		IdentityEnvironment identityEnv = new IdentityEnvironment(id, roles);
		userCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment());
	}
	
	@Test
	public void hasUserProperty() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "hasUserProperty(\"street\",\"Guterstrasse 46\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(result);
	}
	
	@Test
	public void hasUserPropertyFalse() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "hasUserProperty(\"street\",\"Guterstrasse\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(result);
	}
	
	@Test
	public void hasUserPropertySingleSelection() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "hasUserProperty(\"genericSelectionProperty\",\"insert\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(result);
	}
	
	@Test
	public void hasUserPropertySingleSelectionFalse() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "hasUserProperty(\"genericSelectionProperty\",\"update\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(result);
	}
	
	@Test
	public void hasUserPropertyMultipleSelection() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "hasUserProperty(\"genericSelectionProperty2\",\"School 1\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(result);
	}

	@Test
	public void hasUserPropertyMultipleSelectionFalse() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "hasUserProperty(\"genericSelectionProperty2\",\"OpenOlat\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(result);
	}
	
	@Test
	public void hasNotUserProperty() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "hasNotUserProperty(\"street\",\"Mettstrasse\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(result);
	}
	
	@Test
	public void hasNotUserPropertyFalse() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "hasNotUserProperty(\"street\",\"Guterstrasse 46\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(result);
	}

	@Test
	public void hasNotUserPropertySingleSelection() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "hasNotUserProperty(\"genericSelectionProperty\",\"update\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(result);
	}
	
	@Test
	public void hasNotUserPropertySingleSelectionFalse() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "hasNotUserProperty(\"genericSelectionProperty\",\"insert\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(result);
	}
	
	@Test
	public void hasNotUserPropertyMultipleSelection() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "hasNotUserProperty(\"genericSelectionProperty2\",\"OpenOlat\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(result);
	}

	@Test
	public void hasNotUserPropertyMultipleSelectionFalse() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "hasNotUserProperty(\"genericSelectionProperty2\",\"frentix\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(result);
	}
	
	@Test
	public void userPropertyStartswith() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "userPropertyStartswith(\"street\",\"Guterstrasse\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(result);
	}
	
	@Test
	public void userPropertyStartswithFalse() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "userPropertyStartswith(\"street\",\"46\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(result);
	}
	
	@Test
	public void userPropertyEndswith() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "userPropertyEndswith(\"street\",\"46\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(result);
	}
	
	@Test
	public void userPropertyEndswithFalse() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "userPropertyEndswith(\"street\",\"Guterstrasse\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(result);
	}
	
	@Test
	public void isInUserProperty() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "isInUserProperty(\"street\",\"strasse\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(result);
	}
	
	@Test
	public void isInUserPropertyFalse() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "isInUserProperty(\"street\",\"Mett\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(result);
	}
	
	@Test
	public void isNotInUserProperty() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "isNotInUserProperty(\"street\",\"Mett\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertTrue(result);
	}
	
	@Test
	public void isNotInUserPropertyFalse() {
		ConditionInterpreter interpreter = new ConditionInterpreter(userCourseEnv);

		String condition = "isNotInUserProperty(\"street\",\"strasse\")";
		boolean result = interpreter.evaluateCondition(condition);
		Assert.assertFalse(result);
	}

}
