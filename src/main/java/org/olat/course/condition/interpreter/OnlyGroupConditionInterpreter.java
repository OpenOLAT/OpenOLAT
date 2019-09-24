/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.condition.interpreter;

import org.olat.course.condition.interpreter.score.GetAverageScoreFunction;
import org.olat.course.condition.interpreter.score.GetPassedFunction;
import org.olat.course.condition.interpreter.score.GetPassedWithCourseIdFunction;
import org.olat.course.condition.interpreter.score.GetScoreFunction;
import org.olat.course.condition.interpreter.score.GetScoreWithCourseIdFunction;
import org.olat.course.db.interpreter.GetUserCourseDBFunction;
import org.olat.course.run.userview.UserCourseEnvironment;

import com.neemsoft.jmep.Environment;

/**
 * Special condition-interpreter for assessment tool group- / course-structure-selection.
 * This condition-interpreter evalute only group conditions and insert dummy function for all 
 * other conditions.
 * 
 * @author Christian Guretzki
 */
public class OnlyGroupConditionInterpreter extends ConditionInterpreter{

	/**
	 * ConditionInterpreter interpretes course conditions.
	 * 
	 * @param userCourseEnv
	 */
	public OnlyGroupConditionInterpreter(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);

		env = new Environment();

		// constants: add for user convenience
		env.addConstant("true", 1);
		env.addConstant("false", 0);

		// variables
		env.addVariable(NowVariable.name, new DummyVariable(userCourseEnv));
		env.addVariable(NeverVariable.name, new NeverVariable(userCourseEnv));

		// functions
		env.addFunction(DateFunction.name, new DummyDateFunction(userCourseEnv));
		env.addFunction("inGroup", new InLearningGroupFunction(userCourseEnv, "inGroup")); // legacy
		env.addFunction("inLearningGroup", new InLearningGroupFunction(userCourseEnv, "inLearningGroup"));
		env.addFunction(InRightGroupFunction.name, new InRightGroupFunction(userCourseEnv));
		env.addFunction(InLearningAreaFunction.name, new InLearningAreaFunction(userCourseEnv));
		env.addFunction(IsUserFunction.name, new DummyBooleanFunction(userCourseEnv));
		env.addFunction(IsGuestFunction.name, new DummyBooleanFunction(userCourseEnv));
		env.addFunction(IsGlobalAuthorFunction.name, new DummyBooleanFunction(userCourseEnv));
		env.addFunction(Sleep.name, new Sleep(userCourseEnv));
		env.addFunction("hasAttribute", new DummyBooleanFunction(userCourseEnv));
		env.addFunction("isInAttribute", new DummyBooleanFunction(userCourseEnv));
		env.addFunction(GetUserPropertyFunction.name, new DummyStringFunction(userCourseEnv));
		env.addFunction(GetUserCourseDBFunction.name, new DummyStringFunction(userCourseEnv));
		env.addFunction(HasLanguageFunction.name, new DummyBooleanFunction(userCourseEnv));
		env.addFunction(InInstitutionFunction.name, new DummyBooleanFunction(userCourseEnv));
		env.addFunction(IsCourseCoachFunction.name, new DummyBooleanFunction(userCourseEnv));
		env.addFunction(IsCourseParticipantFunction.name, new DummyBooleanFunction(userCourseEnv));
		env.addFunction(IsCourseAdministratorFunction.name, new DummyBooleanFunction(userCourseEnv));
		env.addFunction(IsInOrganisationFunction.name, new IsInOrganisationFunction(userCourseEnv));
		env.addFunction(IsAssessmentModeFunction.name, new DummyBooleanFunction(userCourseEnv));

		env.addFunction(GetAttemptsFunction.name, new DummyIntegerFunction(userCourseEnv));

		env.addFunction(GetCourseBeginDateFunction.name, new GetCourseBeginDateFunction(userCourseEnv));
		env.addFunction(GetCourseEndDateFunction.name, new GetCourseEndDateFunction(userCourseEnv));
		env.addFunction(GetInitialCourseLaunchDateFunction.name, new GetInitialCourseLaunchDateFunction(userCourseEnv));
		env.addFunction(GetRecentCourseLaunchDateFunction.name, new GetRecentCourseLaunchDateFunction(userCourseEnv));

		EvalAttributeFunction eaf;
		eaf = new EvalAttributeFunction(userCourseEnv, EvalAttributeFunction.FUNCTION_TYPE_HAS_ATTRIBUTE);
		env.addFunction(eaf.name, eaf);
		eaf = new EvalAttributeFunction(userCourseEnv, EvalAttributeFunction.FUNCTION_TYPE_IS_IN_ATTRIBUTE);
		env.addFunction(eaf.name, eaf);
		eaf = new EvalAttributeFunction(userCourseEnv, EvalAttributeFunction.FUNCTION_TYPE_HAS_NOT_ATTRIBUTE);
		env.addFunction(eaf.name, eaf);
		eaf = new EvalAttributeFunction(userCourseEnv, EvalAttributeFunction.FUNCTION_TYPE_IS_NOT_IN_ATTRIBUTE);
		env.addFunction(eaf.name, eaf);
		eaf = new EvalAttributeFunction(userCourseEnv, EvalAttributeFunction.FUNCTION_TYPE_ATTRIBUTE_ENDS_WITH);
		env.addFunction(eaf.name, eaf);
		eaf = new EvalAttributeFunction(userCourseEnv, EvalAttributeFunction.FUNCTION_TYPE_ATTRIBUTE_STARTS_WITH);
		env.addFunction(eaf.name, eaf);
		
		// enrollment building block specific functions
		env.addFunction(GetInitialEnrollmentDateFunction.name, new DummyDateFunction(userCourseEnv));
		env.addFunction(GetRecentEnrollmentDateFunction.name, new DummyDateFunction(userCourseEnv));

		// functions to calculate score
		env.addFunction(GetPassedFunction.name, new DummyBooleanFunction(userCourseEnv));
		env.addFunction(GetScoreFunction.name, new DummyDoubleFunction(userCourseEnv));
		env.addFunction(GetAverageScoreFunction.NAME, new DummyDoubleFunction(userCourseEnv));
		env.addFunction(GetPassedWithCourseIdFunction.name, new DummyBooleanFunction(userCourseEnv));
		env.addFunction(GetScoreWithCourseIdFunction.name, new DummyDoubleFunction(userCourseEnv));

		// units
		env.addUnit("min", new MinuteUnit());
		env.addUnit("h", new HourUnit());
		env.addUnit("d", new DayUnit());
		env.addUnit("w", new WeekUnit());
		env.addUnit("m", new MonthUnit());
	}

}

//////////////////////////////////
// Dummy function implementations
//////////////////////////////////

class DummyBooleanFunction extends AbstractFunction {


	public DummyBooleanFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	@Override
	public Object call(Object[] inStack) {		
		// return allways true, because it is a dummy implementation without condition
		return ConditionInterpreter.INT_TRUE;
	}

	@Override
	protected Object defaultValue() {
		return ConditionInterpreter.INT_TRUE;
	}

}

class DummyDateFunction extends AbstractFunction {

	public DummyDateFunction(UserCourseEnvironment userCourseEnv ) {
		super(userCourseEnv);
	}

	@Override
	public Object call(Object[] inStack) {		
		// return allways true, because it is a dummy implementation without condition
		return new Double(0);
	}

	@Override
	protected Object defaultValue() {
		return new Double(0);
	}
}

class DummyDoubleFunction extends AbstractFunction {

	public DummyDoubleFunction(UserCourseEnvironment userCourseEnv ) {
		super(userCourseEnv);
	}

	@Override
	public Object call(Object[] inStack) {		
		return Double.MIN_VALUE;
	}

	@Override
	protected Object defaultValue() {
		return Double.MIN_VALUE;
	}
}

class DummyIntegerFunction extends AbstractFunction {

	public DummyIntegerFunction(UserCourseEnvironment userCourseEnv ) {
		super(userCourseEnv);
	}

	@Override
	public Object call(Object[] inStack) {		
		return Integer.MIN_VALUE;
	}

	@Override
	protected Object defaultValue() {
		return Integer.MIN_VALUE;
	}
}

class DummyStringFunction extends AbstractFunction {

	public DummyStringFunction(UserCourseEnvironment userCourseEnv ) {
		super(userCourseEnv);
	}

	@Override
	public Object call(Object[] inStack) {		
		return "";
	}

	@Override
	protected Object defaultValue() {
		return "";
	}
}

class DummyVariable extends AbstractVariable {

	public static final String name = "now";

	/**
	 * Default constructor to use the current date
	 * @param userCourseEnv
	 */
	public DummyVariable(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}
	
	/**
	 * @see com.neemsoft.jmep.VariableCB#getValue()
	 */
	@Override
	public Object getValue() {
		return new Double(0);
	}

}