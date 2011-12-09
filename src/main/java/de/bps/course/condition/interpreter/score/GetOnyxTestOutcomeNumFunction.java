
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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */

package de.bps.course.condition.interpreter.score;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.condition.interpreter.AbstractFunction;
import org.olat.course.condition.interpreter.ArgumentParseException;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.CourseEditorEnvImpl;
import org.olat.course.editor.EditorUserCourseEnvironmentImpl;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;

import de.bps.webservices.clients.onyxreporter.OnyxReporterWebserviceManager;
import de.bps.webservices.clients.onyxreporter.OnyxReporterWebserviceManagerFactory;

/**
 * @author Ingmar Kroll
 */
public class GetOnyxTestOutcomeNumFunction extends AbstractFunction {
	public static final String name = "getOnyxTestOutcome";

	/**
	 * Default constructor to use the current date
	 *
	 * @param userCourseEnv
	 */
	public GetOnyxTestOutcomeNumFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	/**
	 * @see com.neemsoft.jmep.FunctionCB#call(java.lang.Object[])
	 */
	public Object call(Object[] inStack) {
		/*
		 * argument check
		 */
		if (inStack.length > 2) {
			return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_FEWER_ARGUMENTS, name, "", "error.fewerargs",
					"solution.provideone.nodereference"));
		} else if (inStack.length < 2) { return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_MORE_ARGUMENTS, name,
				"", "error.moreargs", "solution.provideone.nodereference")); }
		/*
		 * argument type check
		 */
		if (!(inStack[0] instanceof String)) return handleException(new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT,
				name, "", "error.argtype.coursnodeidexpeted", "solution.example.node.infunction"));
		String nodeId = (String) inStack[0];

		if (!(inStack[1] instanceof String)) return handleException(new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT,
				name, "", "error.argtype.coursnodeidexpeted", "solution.example.node.infunction"));
		String varId = (String) inStack[1];

		
		/*
		 * check reference integrity
		 */
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			if (!cev.existsNode(nodeId)) { return handleException( new ArgumentParseException(ArgumentParseException.REFERENCE_NOT_FOUND, name, nodeId,
					"error.notfound.coursenodeid", "solution.copypastenodeid")); }
			if (!cev.isAssessable(nodeId)) { return handleException(new ArgumentParseException(ArgumentParseException.REFERENCE_NOT_FOUND, name,
					nodeId, "error.notassessable.coursenodid", "solution.takeassessablenode")); }
			// remember the reference to the node id for this condtion
			cev.addSoftReference("courseNodeId", nodeId);
		}
		

		/*
		 * the real function evaluation which is used during run time
		 */
		try {
			
			//if the parameter is not in the list of the Onyx-Test's outcome-parameters add an error
			OnyxReporterWebserviceManager onyxReporter = null;
			UserCourseEnvironment uce = getUserCourseEnv();
			AssessableCourseNode node = null;
			if (uce.getClass().equals(EditorUserCourseEnvironmentImpl.class)) {
				CourseEditorEnv cee = ((EditorUserCourseEnvironmentImpl) uce).getCourseEditorEnv();
				//TODO: anders holen siehe GetScoreWithCourseId L. 75
//				CourseNode cnode = ((CourseEditorEnvImpl) cee).getNode(nodeId);
//				node = (AssessableCourseNode) cnode;
			} else {
				long courseResourceableId = getUserCourseEnv().getCourseEnvironment().getCourseResourceableId();
				node = (AssessableCourseNode) CourseFactory.loadCourse(courseResourceableId).getEditorTreeModel().getCourseNode(nodeId);
			}
			Map<String, String> outcomes = new HashMap<String, String>();
			// node can be null e.g. when it has been deleted
			if (node != null && node.getUserAttempts(uce) > 0) {
				try {
					onyxReporter = OnyxReporterWebserviceManagerFactory.getInstance().fabricate("OnyxReporterWebserviceClient");
					if (onyxReporter!= null ) {
						outcomes = onyxReporter.getOutcomes(node);
					} else {
						throw new UnsupportedOperationException("could not connect to onyx reporter");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (!(outcomes.keySet().contains(varId))) {
				return handleException(new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT,
				name, "", "error.argtype.coursnodeidexpeted", "solution.example.node.infunction"));
			}
			

			if (getUserCourseEnv().getClass().equals(EditorUserCourseEnvironmentImpl.class)) {
				return defaultValue();
			}
			IdentityEnvironment ienv = getUserCourseEnv().getIdentityEnvironment();
			Identity identity = ienv.getIdentity();

			//long courseResourceableId = getUserCourseEnv().getCourseEnvironment().getCourseResourceableId();
			//AssessableCourseNode node = (AssessableCourseNode) CourseFactory.loadCourse(courseResourceableId).getEditorTreeModel().getCourseNode(nodeId);

			List<String[]> liste = new ArrayList<String[]>();

			try {
//				onyxReporter = OnyxReporterWebserviceManagerFactory.getInstance().fabricate("OnyxReporterWebserviceClient");
				if (onyxReporter != null) {
							liste = onyxReporter.getResults(node, identity);
				} else {
					throw new UnsupportedOperationException("could not connect to onyx reporter");
				}
			} catch (RemoteException e) {
				Tracing.logWarn("Unable to get results from OnyxReporter for user " + identity.getName(), this.getClass());
				//e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (liste.size() > 0) {
				for (String[] outcome : liste) {
					if (outcome[0].equals(varId)) {
						if (Integer.valueOf(outcome[1]) != null) {
							return Integer.valueOf(outcome[1]);
						} else if (Double.valueOf(outcome[1]) != null) {
							return Double.valueOf(outcome[1]);
						} else {
							return outcome[1];
						}
					}
				}
			}

		} catch (org.olat.core.logging.AssertException e) {
			Tracing.logDebug(e.getMessage(), this.getClass());
		}

		// finally check existing value

		return Double.MIN_VALUE;

	}

	/**
	 * @see org.olat.course.condition.interpreter.AbstractFunction#defaultValue()
	 */
	protected Object defaultValue() {
		return Double.MIN_VALUE;
	}

}
