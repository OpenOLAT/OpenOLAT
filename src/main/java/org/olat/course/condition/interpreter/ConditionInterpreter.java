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
* <p>
*/

package org.olat.course.condition.interpreter;

import java.text.ParseException;

import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.condition.Condition;
import org.olat.course.condition.interpreter.score.GetPassedFunction;
import org.olat.course.condition.interpreter.score.GetPassedWithCourseIdFunction;
import org.olat.course.condition.interpreter.score.GetScoreFunction;
import org.olat.course.condition.interpreter.score.GetScoreWithCourseIdFunction;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;

import com.neemsoft.jmep.Environment;
import com.neemsoft.jmep.Expression;
import com.neemsoft.jmep.XExpression;
import com.neemsoft.jmep.XIllegalOperation;
import com.neemsoft.jmep.XIllegalStatus;
import com.neemsoft.jmep.XUndefinedFunction;
import com.neemsoft.jmep.XUndefinedUnit;
import com.neemsoft.jmep.XUndefinedVariable;

import de.bps.course.condition.interpreter.score.GetOnyxTestOutcomeAnumFunction;
import de.bps.course.condition.interpreter.score.GetOnyxTestOutcomeNumFunction;

/**
 * Initial Date:  Jan 27, 2004
 * @author gnaegi
 * Comment:
 */
public class ConditionInterpreter {
	private OLog log = Tracing.createLoggerFor(this.getClass());

	protected static final String PACKAGE = Util.getPackageName(ConditionInterpreter.class);
	/** static Integer(1) object */
	public static final Integer INT_TRUE = new Integer(1);
	/** static Integer(0) object */
	public static final Integer INT_FALSE = new Integer(0);
	protected Environment env;
	protected PackageTranslator translator = null;
	protected UserCourseEnvironment uce;

	protected ConditionInterpreter() {

	}

	/**
	 * ConditionInterpreter interpretes course conditions.
	 *
	 * @param userCourseEnv
	 */
	public ConditionInterpreter(UserCourseEnvironment userCourseEnv) {
		uce = userCourseEnv;
		//
		CourseEditorEnv cev = uce.getCourseEditorEnv();
		if (cev != null) {
			translator = new PackageTranslator(PACKAGE, cev.getEditorEnvLocale());
		}

		env = new Environment();

		// constants: add for user convenience
		env.addConstant("true", 1);
		env.addConstant("false", 0);

		// variables
		env.addVariable(NowVariable.name, new NowVariable(userCourseEnv));
		env.addVariable(NeverVariable.name, new NeverVariable(userCourseEnv));

		// functions
		env.addFunction(DateFunction.name, new DateFunction(userCourseEnv));
		env.addFunction("inGroup", new InLearningGroupFunction(userCourseEnv, "inGroup")); // legacy
		env.addFunction("inLearningGroup", new InLearningGroupFunction(userCourseEnv, "inLearningGroup"));
		env.addFunction("isLearningGroupFull", new IsLearningGroupFullFunction(userCourseEnv, "isLearningGroupFull"));
		env.addFunction(InRightGroupFunction.name, new InRightGroupFunction(userCourseEnv));
		env.addFunction(InLearningAreaFunction.name, new InLearningAreaFunction(userCourseEnv));
		env.addFunction(IsUserFunction.name, new IsUserFunction(userCourseEnv));
		env.addFunction(IsGuestFunction.name, new IsGuestFunction(userCourseEnv));
		env.addFunction(IsGlobalAuthorFunction.name, new IsGlobalAuthorFunction(userCourseEnv));
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
		env.addFunction(GetUserPropertyFunction.name, new GetUserPropertyFunction(userCourseEnv));
		env.addFunction(HasLanguageFunction.name, new HasLanguageFunction(userCourseEnv));
		env.addFunction(InInstitutionFunction.name, new InInstitutionFunction(userCourseEnv));
		env.addFunction(IsCourseCoachFunction.name, new IsCourseCoachFunction(userCourseEnv));
		env.addFunction(IsCourseParticipantFunction.name, new IsCourseParticipantFunction(userCourseEnv));
		env.addFunction(IsCourseAdministratorFunction.name, new IsCourseAdministratorFunction(userCourseEnv));

		env.addFunction(GetInitialCourseLaunchDateFunction.name, new GetInitialCourseLaunchDateFunction(userCourseEnv));
		env.addFunction(GetRecentCourseLaunchDateFunction.name, new GetRecentCourseLaunchDateFunction(userCourseEnv));

		env.addFunction(GetAttemptsFunction.name, new GetAttemptsFunction(userCourseEnv));

		// enrollment building block specific functions
		env.addFunction(GetInitialEnrollmentDateFunction.name, new GetInitialEnrollmentDateFunction(userCourseEnv));
		env.addFunction(GetRecentEnrollmentDateFunction.name, new GetRecentEnrollmentDateFunction(userCourseEnv));

		// functions to calculate score
		env.addFunction(GetPassedFunction.name, new GetPassedFunction(userCourseEnv));
		env.addFunction(GetScoreFunction.name, new GetScoreFunction(userCourseEnv));
		env.addFunction(GetPassedWithCourseIdFunction.name, new GetPassedWithCourseIdFunction(userCourseEnv));
		env.addFunction(GetScoreWithCourseIdFunction.name, new GetScoreWithCourseIdFunction(userCourseEnv));

	  
		env.addFunction(GetOnyxTestOutcomeNumFunction.name, new GetOnyxTestOutcomeNumFunction(userCourseEnv));
		env.addFunction(GetOnyxTestOutcomeAnumFunction.name, new GetOnyxTestOutcomeAnumFunction(userCourseEnv));
		

		// units
		env.addUnit("min", new MinuteUnit());
		env.addUnit("h", new HourUnit());
		env.addUnit("d", new DayUnit());
		env.addUnit("w", new WeekUnit());
		env.addUnit("m", new MonthUnit());
	}

	/**
	 * @param expression
	 * @return null if no error, else the error msg
	 */
	public ConditionErrorMessage[] syntaxTestExpression(ConditionExpression condExpr) {
		try {
			/*
			 * the functions, units, variables from the condition expression access
			 * the active condition expression through the
			 * CourseEditorEnv.getActiveConditionExpression(). Whereas the active
			 * condition expression is determined by calling
			 * CourseEditorEnv.validateConditionExpression(). Hence
			 * syntaxTestExpression should never be called directly or in a non editor
			 * environment.
			 */
			String conditionString = condExpr.getExptressionString();
			Expression exp = new Expression(conditionString, env);
			exp.evaluate();
			Exception[] condExceptions = condExpr.getExceptions();
			ConditionErrorMessage[] cems = null;
			if (condExceptions != null && condExceptions.length > 0) {
				// create an error message from the first error in the expression
				cems=new ConditionErrorMessage[condExceptions.length];
				for (int i = 0; i < condExceptions.length; i++) {
					cems[i]=handleExpressionExceptions(condExceptions[i]);
				}
				return cems;
			}// else return null, at the end of the method!
		} catch (Exception e) {
			// catches every non ArgumentParseException
			return new ConditionErrorMessage[] {handleExpressionExceptions(e)};
		}
		// if no exception was found, thus no condition error message to report.
		return null;
	}

	/**
	 * Check an expression on syntactical errors.
	 *
	 * @param expression
	 * @return Null if syntactically correct, error message otherwise.
	 * @deprecated TODO: remove as it is no longer referenced, except test?
	 */
	public ConditionErrorMessage syntaxTestCalculation(String expression) {
		try {
			Expression exp = new Expression(expression, env);
			exp.evaluate();
		} catch (Exception e) {
			return handleExpressionExceptions(e);
		}
		return null;
	}

	/**
	 * evaluation of expression may throw exceptions, the handling of these is
	 * done here.
	 *
	 * @param e
	 * @return
	 */
	private ConditionErrorMessage handleExpressionExceptions(Exception e) {
		String msg = "";
		String[] params = null;
		String solutionMsg = "";
		try {
			throw e;
			//TODO:pb:b do no rethrow, but test for instanceof
		} catch (XIllegalOperation xe) {
			msg = "error.illegal.operation.at";
			params = new String[] { Integer.toString(xe.getPosition()) };
		} catch (XUndefinedFunction xe) {
			msg = "error.undefined.function.at";
			params = new String[] { Integer.toString(xe.getPosition()) };
		} catch (XUndefinedUnit xe) {
			msg = "error.undefined.unit.at";
			params = new String[] { Integer.toString(xe.getPosition()) };
		} catch (XUndefinedVariable xe) {
			msg = "error.undefined.variable.at";
			params = new String[] { Integer.toString(xe.getPosition()) };
			solutionMsg = "solution.error.undefvariable";
		} catch (XIllegalStatus xe) {
			// illegal status in the condition interpreter, this must not happen!
			throw new OLATRuntimeException(xe.getMessage(), xe);
		} catch (XExpression xe) {
			msg = "error.inexpression.at";
			params = new String[] { Integer.toString(xe.getPosition()) };
			solutionMsg = "solution.error.inexpression";
		} catch (ArgumentParseException apex) {
			// the function, units etc, are responsible to provide reasonable error
			// messages (translation keys)
			msg = apex.getWhatsWrong();
			params = new String[] { apex.getFunctionName(), apex.getWrongArgs() };
			solutionMsg = apex.getSolutionProposal();
		} catch (Exception ex) {
			// this must not happen!
			throw new OLATRuntimeException(ex.getMessage(), ex);
		}
		return new ConditionErrorMessage(msg, solutionMsg, params);
	}

	/**
	 * Evaluates a condition.
	 *
	 * @param c
	 * @return True if evaluation successfull.
	 */
	public boolean evaluateCondition(Condition c) {
		return evaluateCondition(c.getConditionExpression());
	}

	/**
	 * Evaluates a condition.
	 *
	 * @param condition
	 * @return True if evaluation successfull.
	 */
	public boolean evaluateCondition(String condition) {
		boolean ok = false;
		try {
			// TODO: lookup in Map: key = c -> cached Expression
			// if not null then: ok = evaluateCondition(Expression cachedExpression)
			ok = doEvaluateCondition(condition);
		} catch (ParseException e) {
			log.info("ParseException in evaluateCondition:" + e);
			throw new AssertException("parse error in:: " + condition + " exception=" + e.getMessage());
		}
		return ok;
	}

	/**
	 * Evaluates a calculation.
	 *
	 * @param calculation
	 * @return True if evaluation successfull.
	 */
	public float evaluateCalculation(String calculation) {
		float res = -100000000000000000000000000000000f;
		try {
			res = doEvaluateCalculation(calculation);
		} catch (ParseException e) {
			log.info("ParseException in evaluateCalculation:" + e);
			throw new AssertException("parse or execute error in calculation:" + calculation + " exception=" + e.getMessage());
		}
		return res;
	}

	private float doEvaluateCalculation(String calculation) throws ParseException {
		try {
			Expression exp = new Expression(calculation, env);
			Object result = exp.evaluate();
			if (result instanceof Double) {
				return ((Double) result).floatValue();
			} else if (result instanceof Integer) {
				return ((Integer) result).floatValue();
			} else throw new ArgumentParseException("Parse exception: expected Double or Integer, but got:"
					+ (result == null ? "no object(null)" : result.getClass().getName()));
		} catch (XExpression xe) {
			throw new ParseException("Parse exception for calculation: " + calculation + ". " + xe.getMessage(), xe.getPosition());
		}
	}

	/**
	 * Evaluate a condition using the jmep expression parser
	 *
	 * @param condition The condition as a java script text
	 * @return true if condition matches, false otherwhise
	 */
	private boolean doEvaluateCondition(String condition) throws ParseException {
		try {
			Expression exp = new Expression(condition, env);
			Object result = exp.evaluate();
			if (result instanceof Double) {
				return (((Double) result).doubleValue() == 1.0) ? true : false;
			} else if (result instanceof Integer) {
				return (((Integer) result).intValue() == 1) ? true : false;
			} else return false;
		} catch (XExpression xe) {
			throw new ParseException("Parse exception for condition: " + condition + ". " + xe.getMessage(), xe.getPosition());
		}
	}

	private boolean evaluateCondition(Expression exp) throws ParseException {
		try {
			Object result = exp.evaluate();
			if (result instanceof Double) {
				return (((Double) result).doubleValue() == 1.0) ? true : false;
			} else if (result instanceof Integer) {
				return (((Integer) result).intValue() == 1) ? true : false;
			} else return false;
		} catch (XExpression xe) {
			throw new ParseException("Parse exception" + xe.getMessage(), xe.getPosition());
		}
	}

	/**
	 * Test method for condition interpreter using the dummy lu callback
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * --- this method is not working, but left for the docu ---
		 */

		// CourseEnvironment ce = new CourseEnvironmentImpl(null);
		ConditionInterpreter interpreter = null; //new ConditionInterpreter(new TestUserCourseEnvironmentImpl());// ce);

		long start = System.currentTimeMillis();
		try {
			Expression exp = new Expression("(inGroup(\"blue\")|!inGroup(\"red\")) & now > date(\"09.03.2004 15:00\")", interpreter.env);
			Expression exp2 = new Expression("(9.99999 < 10) & (1000/2 > 200)", interpreter.env);

			for (int i = 0; i < 1000; i++) {
				exp = new Expression("(inGroup(\"blue\")|!inGroup(\"red\")) & now > date(\"09.03.2004 15:00\")", interpreter.env);
				interpreter.evaluateCondition(exp);
				exp2 = new Expression("(9.99999 < 10) & (1000/2 > 200)", interpreter.env);
				interpreter.evaluateCondition(exp2);
			}

		} catch (Exception e) {
			// just a timing test
		}
		long stop = System.currentTimeMillis();
		System.out.println("time:" + ((stop - start)));

		// TODO refine tests with new interpreter

		// number tests
		/*
		 * interpreter.printTestCase("is in group red", "inGroup(\"red\")");
		 * interpreter.printTestCase("is not in group red", "!inGroup(\"red\")");
		 * interpreter.printTestCase("is in group blue", "inGroup(\"blue\")");
		 * interpreter.printTestCase("is in group blue or red", "(inGroup(\"blue\")) |
		 * (inGroup(\"red\"))"); interpreter.printTestCase("is in group red or blue
		 * (no cond eval)", "(inGroup(\"red\")) | (inGroup(\"blue\"))");
		 * interpreter.printTestCase("is in group red and blue", "(inGroup(\"red\")) &
		 * (inGroup(\"blue\"))"); interpreter.printTestCase("is not in group blue
		 * and in group red", "(inGroup(\"blue\")) & (inGroup(\"red\"))");
		 */
		interpreter.printTestCase("(inGroup(\"blue\")|inGroup(\"red\")) & now > date(\"09.03.2004 15:00\")");

		interpreter.printTestCase("1 < 10");
		interpreter.printTestCase("(9.99999 < 10) & (1000/2 > 200)");
		interpreter.printTestCase("(90.99999 < 10) | (1000/2 > 200)");
		// interpreter.printTestCase("100/0");
		// interpreter.printTestCase("100/0 < 2"); // hm, interresting
		// interpreter.printTestCase("100/0 > 2");

		// property tests using exposed java object as property callback
		// PropertyManager pm = new PropertyManager();
		// pm.setCourseProperty(new IntProperty("integer-prop", 5));
		/*
		 * interpreter.printTestCase("courseProperty(\"integer-prop\") > 4");
		 * interpreter.printTestCase("courseProperty(\"integer-prop\") < 4");
		 * interpreter.printTestCase("courseProperty(\"integer-prop\") = 5"); //
		 * might be confusing to beginners that = must be written as == (will throw
		 * exception otherwhise) // property tests with non existing properties
		 * interpreter.printTestCase("courseProperty(\"gibts-nöd\") < 4"); // hm...
		 * user must understand that a property exists by default and has a default
		 * value interpreter.printTestCase("courseProperty(\"gibts-nöd\") > 4");
		 * interpreter.printTestCase("courseProperty(\"gibts-nöd\") = 4");
		 */
		// Property tests with date properties
		/*
		 * pm.setUserProperty(new DateProperty("date-prop", new Date())); // set
		 * property to a tesing value
		 * interpreter.printTestCase("userProperty(\"date-prop\") < now");
		 * interpreter.printTestCase("userProperty(\"date-prop\") = now");
		 * interpreter.printTestCase("userProperty(\"date-prop\") > now");
		 */
		// Now a test with a fake date (e.g. for runtime simulation-preview-mode)
		interpreter.printTestCase("date(\"01.01.2004 12:00\") < now");

		interpreter.printTestCase("date(\"01.01.2004 12:00\") > now");
		interpreter.printTestCase("date(\"01.01.2004 12:00\") + 3m > now");
		interpreter.printTestCase("now > date(\"09.03.2004 15:00\")");
		interpreter.printTestCase("1 & 1 & 0 | 1");
		//
		// System.out.println("testsyntax ok:" +
		// interpreter.syntaxTestExpression("now > date(\"28.02.2004 15:00\")"));
		// System.out.println("testsyntax nok:" +
		// interpreter.syntaxTestExpression("now > date(\"31.02.2004 15:00\")"));

	}

	/**
	 * helper for main method
	 *
	 * @param testCondition
	 */
	private void printTestCase(String testCondition) {
		try {
			System.out.println(testCondition + ":  " + doEvaluateCondition(testCondition) + "\n-----\n");
		} catch (ParseException e) {
			System.out.println(e.toString());
		}
	}
	
	private UserCourseEnvironment getUserCourseEnvironment() {
		return uce;
	}

}