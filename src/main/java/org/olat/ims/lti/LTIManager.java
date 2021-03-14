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
package org.olat.ims.lti;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.course.ICourse;
import org.olat.resource.OLATResource;

/**
 *
 * Initial date: 13.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface LTIManager {

	public static final String USER_NAME_PROP = "username";
	public static final String USER_PROPS_PREFIX = "$userprops_";
	public static final int EXPIRATION_TIME = 3600 * 24 * 30 * 6;//6 months

	public Map<String,String> forgeLTIProperties(Identity identity, Locale locale,
			LTIContext context, boolean sendName, boolean sendEmail, boolean ensureEmail);

	public Map<String,String> sign(Map<String,String> props, String url, String oauthKey, String oauthSecret);


	public LTIOutcome createOutcome(Identity identity, OLATResource resource, String resSubPath,
			String action, String outcomeKey, String outcomeValue);

	public LTIOutcome loadOutcomeByKey(Long key);

	public List<LTIOutcome> loadOutcomes(Identity identity, OLATResource resource, String resSubPath);
	
	/**
	 * 
	 * @param assessedId The assessed identity
	 * @param score The score return by the LTI tool
	 * @param course The course
	 * @param courseNodeId The course identifier
	 */
	public void updateScore(Identity assessedId, Float score, ICourse course, String courseNodeId);
	
	public Float getScore(Identity assessedId, ICourse course, String courseNodeId);

	/**
	 * Remove the outcomes of a resource, typically before deleting a course.
	 * @param resource
	 */
	public void deleteOutcomes(OLATResource resource);

	/**
	 * Make a LTI request with a HTTP Post request.
	 * @param signedProps the signed LTI properties
	 * @param url the url to send the request
	 * @return the http response content as string or null if the request was not successful
	 */
	public String post(Map<String,String> signedProps, String url);

	/**
	 * Join the custom properties to a String, so that it can be returned by a
	 * LtiContext.
	 *
	 * @param customProps
	 * @return
	 */
	public String joinCustomProps(Map<String, String> customProps);
	
	public String getUsername(IdentityRef identity);

}
