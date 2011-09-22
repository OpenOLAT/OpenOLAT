
/**
 *
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 *
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
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

