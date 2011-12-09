
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
package de.bps.onyx.plugin.run;

import java.util.HashMap;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.iq.IQEditController;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * @author Ingmar Kroll
 */
public class OnyxSessionStore {

	private static HashMap<String,OnyxSession> idOnyxsession = new HashMap<String,OnyxSession>();

	public static String getUniqueId(Identity identity, CourseNode node,
			UserCourseEnvironment userCourseEnv) {
		String uId= String.valueOf(CodeHelper.getGlobalForeverUniqueID().hashCode());
		OnyxSession os = new OnyxSession();
		os.setNode(node);
		os.setUserCourseEnvironment(userCourseEnv);
		os.setAssessmenttype(node.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE).toString());
		os.setIdentity(identity);
		idOnyxsession.put(uId, os);
		return uId;
	}

	public static OnyxSession getAndRemoveOnyxsession(String getUniqueId){
		return idOnyxsession.get(getUniqueId);
	}

	/**
	 * Getting the key to the value.
	 * @param os The OnyxSession to get the id for.
	 * @return the unique id.
	 */
	public static String getIdForSession(OnyxSession os) {
		String id = "";
		for (Map.Entry entry : idOnyxsession.entrySet()) {
			if (entry.getValue().equals(os)) {
				id = entry.getKey().toString();
			}
		}
		return id;
	}
}

