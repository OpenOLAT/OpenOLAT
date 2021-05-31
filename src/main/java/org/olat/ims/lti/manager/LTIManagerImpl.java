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
package org.olat.ims.lti.manager;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.Logger;
import org.imsglobal.basiclti.BasicLTIUtil;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.nodes.BasicLTICourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.ims.lti.LTIContext;
import org.olat.ims.lti.LTIManager;
import org.olat.ims.lti.LTIOutcome;
import org.olat.ims.lti.model.LTIOutcomeImpl;
import org.olat.ldap.ui.LDAPAuthenticationController;
import org.olat.modules.assessment.Role;
import org.olat.resource.OLATResource;
import org.olat.shibboleth.ShibbolethDispatcher;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Initial date: 13.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LTIManagerImpl implements LTIManager {

	private static final Logger log = Tracing.createLoggerFor(LTIManagerImpl.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	private HttpClientService httpClientService;

	@Override
	public LTIOutcome createOutcome(Identity identity, OLATResource resource,
			String resSubPath, String action, String outcomeKey, String outcomeValue) {
		LTIOutcomeImpl outcome = new LTIOutcomeImpl();
		outcome.setAssessedIdentity(identity);
		outcome.setResource(resource);
		if(StringHelper.containsNonWhitespace(resSubPath)) {
			outcome.setResSubPath(resSubPath);
		}
		outcome.setCreationDate(new Date());
		outcome.setLastModified(new Date());
		outcome.setAction(action);
		outcome.setOutcomeKey(outcomeKey);
		outcome.setOutcomeValue(outcomeValue);
		dbInstance.getCurrentEntityManager().persist(outcome);
		return outcome;
	}

	@Override
	public LTIOutcome loadOutcomeByKey(Long key) {
		List<LTIOutcome> outcomes = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadLTIOutcomeByKey", LTIOutcome.class).
				setParameter("outcomeKey", key)
				.getResultList();

		if(outcomes.isEmpty()) {
			return null;
		}
		return outcomes.get(0);
	}

	@Override
	public List<LTIOutcome> loadOutcomes(Identity identity, OLATResource resource, String resSubPath) {

		StringBuilder sb = new StringBuilder();
		sb.append("select outcome from ltioutcome outcome where outcome.assessedIdentity.key=:identityKey and outcome.resource=:resource");
		if(StringHelper.containsNonWhitespace(resSubPath)) {
			sb.append(" and outcome.resSubPath=:resSubPath");
		} else {
			sb.append(" and outcome.resSubPath is null");
		}

		TypedQuery<LTIOutcome> outcomes = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), LTIOutcome.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("resource", resource);

		if(StringHelper.containsNonWhitespace(resSubPath)) {
			outcomes.setParameter("resSubPath", resSubPath);
		}
		return outcomes.getResultList();
	}

	@Override
	public void deleteOutcomes(OLATResource resource) {
		String q = "delete from ltioutcome as outcome where outcome.resource=:resource";
		dbInstance.getCurrentEntityManager().createQuery(q)
			.setParameter("resource", resource)
			.executeUpdate();
	}

	@Override
	public Map<String,String> sign(Map<String,String> props, String url, String oauthKey, String oauthSecret) {
		String oauth_consumer_key = oauthKey;
		String oauth_consumer_secret = oauthSecret;
		String tool_consumer_instance_guid = Settings.getServerDomainName();
		String tool_consumer_instance_description = null;
		String tool_consumer_instance_url = null;
		String tool_consumer_instance_name = WebappHelper.getInstanceId();
		String tool_consumer_instance_contact_email = WebappHelper.getMailConfig("mailSupport");

		if (props == null) {
			props = new HashMap<>();
		}

		return BasicLTIUtil.signProperties(props, url, "POST",
				oauth_consumer_key,
				oauth_consumer_secret,
				tool_consumer_instance_guid,
				tool_consumer_instance_description,
				tool_consumer_instance_url,
				tool_consumer_instance_name,
				tool_consumer_instance_contact_email);
	}

	@Override
	public Map<String,String> forgeLTIProperties(Identity identity, Locale locale, LTIContext context,
			boolean sendName, boolean sendEmail, boolean ensureEmail) {
		final Locale loc = locale;
		final Identity ident = identity;
		final User u = ident.getUser();
		final String lastName = u.getProperty(UserConstants.LASTNAME, loc);
		final String firstName = u.getProperty(UserConstants.FIRSTNAME, loc);
		String email;
		if (ensureEmail) {
			email = userManager.getEnsuredEmail(u);
		} else {
			email = u.getProperty(UserConstants.EMAIL, loc);
		}

		Map<String,String> props = new HashMap<>();
		setProperty(props, "resource_link_id", context.getResourceId());
		setProperty(props, "resource_link_title", context.getResourceTitle());
		setProperty(props, "resource_link_description", context.getResourceDescription());
		//launch
		setProperty(props, "launch_presentation_locale", loc.toString());
		setProperty(props, "launch_presentation_document_target", context.getTarget());
		setProperty(props, "launch_presentation_return_url", context.getTalkBackMapperUri());
		if(StringHelper.containsNonWhitespace(context.getPreferredWidth())) {
			setProperty(props, "launch_presentation_width", context.getPreferredWidth());
		}
		if(StringHelper.containsNonWhitespace(context.getPreferredHeight())) {
			setProperty(props, "launch_presentation_height", context.getPreferredHeight());
		}
		//consumer infos
		setProperty(props, "tool_consumer_info_product_family_code", "openolat");
		setProperty(props, "tool_consumer_info_version", Settings.getVersion());
		//outcome
		if(StringHelper.containsNonWhitespace(context.getOutcomeMapperUri())) {
			//setProperty(props, "ext_ims_lis_basic_outcome_url", context.getOutcomeMapperUri());
			//setProperty(props, "ext_ims_lis_resultvalue_sourcedids", "decimal");
			setProperty(props, "lis_result_sourcedid", context.getSourcedId());
			setProperty(props, "lis_outcome_service_url", context.getOutcomeMapperUri());
		}
		//user data
		setProperty(props, "user_id", context.getUserId(identity));
		setProperty(props, "lis_person_sourcedid", createPersonSourceId(identity));
		if (sendName) {
			setProperty(props, "lis_person_name_given", firstName);
			setProperty(props, "lis_person_name_family", lastName);
			setProperty(props, "lis_person_name_full", firstName+" "+lastName);
		}
		if (sendEmail) {
			setProperty(props, "lis_person_contact_email_primary", email);
		}

		setProperty(props, "roles", context.getRoles(identity));
		setProperty(props, "context_id", context.getContextId());
		setProperty(props, "context_label", context.getContextTitle());
		setProperty(props, "context_title", context.getContextTitle());
		setProperty(props, "context_type", "CourseSection");

		setCustomProperties(context.getCustomProperties(), identity, props);

		return props;
	}

	private void setCustomProperties(String custom, Identity identity, Map<String,String> props) {
		if (!StringHelper.containsNonWhitespace(custom)) return;

		String[] params = custom.split("[\n;]");
		for (int i = 0; i < params.length; i++) {
			String param = params[i];
			if (!StringHelper.containsNonWhitespace(param)) {
				continue;
			}
			int pos = param.indexOf('=');
			if (pos < 1 || pos + 1 > param.length()) {
				continue;
			}

			String key = BasicLTIUtil.mapKeyName(param.substring(0, pos));
			if(!StringHelper.containsNonWhitespace(key)) {
				continue;
			}

			String value = param.substring(pos + 1).trim();
			if(value.length() < 1) {
				continue;
			}

			if(value.startsWith(LTIManager.USER_PROPS_PREFIX)) {
				String userProp = value.substring(LTIManager.USER_PROPS_PREFIX.length(), value.length());
				if(LTIManager.USER_NAME_PROP.equals(userProp)) {
					value = getUsername(identity);
				} else {
					value = identity.getUser().getProperty(userProp, null);
				}
			}
			setProperty(props, "custom_" + key, value);
		}
	}

	public void setProperty(Map<String,String> props, String key, String value) {
		if (value == null) return;
		if (value.trim().length() < 1) return;
		props.put(key, value);
	}

	/**
	 * A comma-separated list of URN values for roles. If this list is non-empty,
	 * it should contain at least one role from the LIS System Role, LIS
	 * Institution Role, or LIS Context Role vocabularies (See Appendix A of
	 * LTI_BasicLTI_Implementation_Guide_rev1.pdf).
	 *
	 * @param roles
	 * @return
	 */
	/*private String setRoles(Identity identity, Roles roles, LTIContext context) {
		StringBuilder rolesStr;
		if (roles.isGuestOnly()) {
			rolesStr = new StringBuilder("Guest");
		} else {
			rolesStr = new StringBuilder("Learner");
			boolean coach = context.isCoach(identity);
			if (coach) {
				rolesStr.append(",").append("Instructor");
			}
			boolean admin = context.isAdmin(identity);
			if (roles.isOLATAdmin() || admin) {
				rolesStr.append(",").append("Administrator");
			}
		}

		return rolesStr.toString();
	}*/

	private String createPersonSourceId(Identity identity) {
		// The person source ID is used as user identifier. The rule is as follows:
		// 1) if a shibboleth authentication token is availble, use the ShibbolethModule.getDefaultUIDAttribute()
		// 2) if a LDAP authentication token is available, use the LDAPConstants.LDAP_USER_IDENTIFYER
		// 3) as fallback use the system URL together with the identity username
		String personSourceId = null;
		// Use the shibboleth ID as person source identificator
		List<Authentication> authMethods = BaseSecurityManager.getInstance().getAuthentications(identity);
		for (Authentication method : authMethods) {
			String provider = method.getProvider();
			if (ShibbolethDispatcher.PROVIDER_SHIB.equals(provider)) {
				personSourceId = method.getAuthusername();
				// done, case 1)
				break;
			} else if (LDAPAuthenticationController.PROVIDER_LDAP.equals(provider)) {
				personSourceId = method.getAuthusername();
				// normally done, case 2). however, lets continue because we might still find a case 1)
			}
			// ignore all other authentication providers
		}
		if (!StringHelper.containsNonWhitespace(personSourceId)) {
			// fallback to the serverDomainName:identityId as case 3)
			personSourceId = Settings.getServerDomainName() + ":" + identity.getKey();
		}
		return personSourceId;
	}

	@Override
	public String post(Map<String,String> signedProps, String url) {
		String content = null;

		// Map the LTI properties to HttpClient parameters
		List<NameValuePair> urlParameters = signedProps.keySet().stream()
				.map(k -> new BasicNameValuePair(k, signedProps.get(k)))
				.collect(Collectors.toList());

		// make the http request and evaluate the result
		try (CloseableHttpClient httpclient = httpClientService.createHttpClient()) {
			HttpPost request = new HttpPost(url);
			HttpEntity postParams = new UrlEncodedFormEntity(urlParameters);
			request.setEntity(postParams);
			HttpResponse httpResponse = httpclient.execute(request);
			content = IOUtils.toString(httpResponse.getEntity().getContent());
		} catch (Exception e) {
			log.error("", e);
		}

		return content;
	}

	@Override
	public String joinCustomProps(Map<String, String> customProps) {
		if (customProps == null) return null;

		return customProps.entrySet().stream()
				.map(p -> p.getKey() + "=" + p.getValue())
				.collect(Collectors.joining(";"));
	}

	@Override
	public String getUsername(IdentityRef identity) {
		return securityManager.findAuthenticationName(identity);
	}

	@Override
	public void updateScore(Identity assessedId, Float score, ICourse course, String courseNodeId) {
		CourseNode node = course.getRunStructure().getNode(courseNodeId);
		if(node instanceof BasicLTICourseNode) {
			BasicLTICourseNode ltiNode = (BasicLTICourseNode)node;
			AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(node);

			Float cutValue = ltiNode.getCutValue(assessmentConfig);
			
			Float scaledScore = null;
			Boolean passed = null;
			if(score != null) {
				float scale = ltiNode.getScalingFactor();
				scaledScore = score * scale;
				if(cutValue != null) {
					passed = scaledScore >= cutValue;
				}
			}
			
			ScoreEvaluation eval = new ScoreEvaluation(scaledScore, passed);
			UserCourseEnvironment userCourseEnv = getUserCourseEnvironment(assessedId, course);
			courseAssessmentService.updateScoreEvaluation(node, eval, userCourseEnv, assessedId, false, Role.user);
		}
	}
	
	@Override
	public Float getScore(Identity assessedId, ICourse course, String courseNodeId) {
		Float score = null;
		CourseNode node = course.getRunStructure().getNode(courseNodeId);
		if(node instanceof BasicLTICourseNode) {
			BasicLTICourseNode ltiNode = (BasicLTICourseNode)node;
			UserCourseEnvironment userCourseEnv = getUserCourseEnvironment(assessedId, course);
			ScoreEvaluation eval = courseAssessmentService.getAssessmentEvaluation(ltiNode, userCourseEnv);
			if(eval != null && eval.getScore() != null) {
				float scaledScore = eval.getScore();
				if(scaledScore > 0.0f) {
					float scale = ltiNode.getScalingFactor();
					scaledScore = scaledScore / scale;
				}
				score = Float.valueOf(scaledScore);
			}
		}
		return score;
	}

	private UserCourseEnvironment getUserCourseEnvironment(Identity identity, ICourse course) {
		IdentityEnvironment identityEnvironment = new IdentityEnvironment();
		identityEnvironment.setIdentity(identity);
		return new UserCourseEnvironmentImpl(identityEnvironment, course.getCourseEnvironment());
	}
}
