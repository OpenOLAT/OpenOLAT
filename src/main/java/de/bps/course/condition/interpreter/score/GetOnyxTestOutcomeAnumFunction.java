
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

import java.util.HashMap;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.OLog;
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

import de.bps.webservices.clients.onyxreporter.OnyxReporterConnector;
import de.bps.webservices.clients.onyxreporter.OnyxReporterException;

/**
 * @author Ingmar Kroll
 */
public class GetOnyxTestOutcomeAnumFunction extends AbstractFunction {
	public static final String name = "getOnyxTestOutcomeZK";
	//<ONYX-705>
	private final static OLog log = Tracing.createLoggerFor(GetOnyxTestOutcomeAnumFunction.class);
	//</ONYX-705>
	/**
	 * Default constructor to use the current date
	 *
	 * @param userCourseEnv
	 */
	public GetOnyxTestOutcomeAnumFunction(UserCourseEnvironment userCourseEnv) {
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
			// Remember the reference to the node id for this condition for cycle testing. 
			// Allow self-referencing but do not allow dependencies to parents as they create cycles.
			if (!nodeId.equals(cev.getCurrentCourseNodeId())) {
				cev.addSoftReference("courseNodeId", nodeId, true);				
			}
		}
		

		//<OLATCE-1088>
		/*
		 * the real function evaluation which is used during run time
		 */
		try {
			
			//if the parameter is not in the list of the Onyx-Test's outcome-parameters add an error
			//<ONYX-705>
			OnyxReporterConnector onyxReporter = new OnyxReporterConnector();
			//</ONYX-705>
			UserCourseEnvironment uce = getUserCourseEnv();
			AssessableCourseNode node = null;
			boolean editorMode = false;
			if (uce.getClass().equals(EditorUserCourseEnvironmentImpl.class)) {
				editorMode = true;
				CourseEditorEnv cee = ((EditorUserCourseEnvironmentImpl) uce).getCourseEditorEnv();
				CourseNode cnode = ((CourseEditorEnvImpl) cee).getNode(nodeId);
				node = (AssessableCourseNode) cnode;
			} else {
				long courseResourceableId = getUserCourseEnv().getCourseEnvironment().getCourseResourceableId();
				node = (AssessableCourseNode) CourseFactory.loadCourse(courseResourceableId).getEditorTreeModel().getCourseNode(nodeId);
			}
			
			//begin course-editor-mode
			Map<String, String> outcomes = new HashMap<String, String>();
			if (editorMode) {
				if(node == null){
					return handleException( new ArgumentParseException(ArgumentParseException.REFERENCE_NOT_FOUND, name, nodeId,
							"error.notfound.coursenodeid", "solution.copypastenodeid"));
				}
				
				try {
				//<ONYX-705>
					outcomes = onyxReporter.getPossibleOutcomeVariables(node);
				} catch (OnyxReporterException e) {
				//</ONYX-705>
					log.error("Unable to get possible test-outcomes!", e);
				}
				if (!(outcomes.keySet().contains(varId))) {
					return handleException(new ArgumentParseException(ArgumentParseException.REFERENCE_NOT_FOUND,
							name, "", "error.argtype.outcome.undefined", ""));
				} else {
					return defaultValue();
				}
			}
			// end course-editor-mode
			
			
			// node can be null e.g. when it has been deleted
			if(node == null){
				log.warn("Coursenode for : "+nodeId+" does not exist!");
				return defaultValue();
			}

			IdentityEnvironment ienv = getUserCourseEnv().getIdentityEnvironment();
			Identity identity = ienv.getIdentity();

			//<ONYX-705>
			Map<String, String> results = null;

			try {
				if(identity != null){
					results = onyxReporter.getResults(node, identity);
				}
			} catch (OnyxReporterException e) {
				log.error("Unable to get Results! Identity "+(identity!=null?identity.getName():"NULL")+" courseNode "+(node!=null?node.getShortName():"null"), e);
			} finally {
				if(results == null){
					return defaultValue();
				}
			}
			
			String retVal = results.get(varId);
			
			if(retVal == null){
				return defaultValue();
			}
			
			try{
				return Integer.parseInt(retVal);
			} catch (NumberFormatException nfeI){
				log.warn("retVal "+retVal+" is not a Integer!", nfeI);
				try{
					return Double.parseDouble(retVal);
				} catch (NumberFormatException nfeD){
					log.warn("retVal "+retVal+" is not a Double!", nfeD);
					
					return retVal;
				}	
			}

		} catch (OnyxReporterException e) {
			log.error(e.getMessage(), e);
		} catch (org.olat.core.logging.AssertException e) {
			log.error(e.getMessage(), e);
		}
		//</ONYX-705>

		// finally check existing value

		return defaultValue();
		//</OLATCE-1088>
	}

	/**
	 * @see org.olat.course.condition.interpreter.AbstractFunction#defaultValue()
	 */
	protected Object defaultValue() {
		return new String();
	}

}
