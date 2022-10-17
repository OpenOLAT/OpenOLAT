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
package org.olat.ims.lti.ui;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import jakarta.servlet.http.HttpServletRequest;

import org.imsglobal.basiclti.XMLMap;
import org.imsglobal.pox.IMSPOXRequest;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.UserSession;
import org.olat.core.util.session.UserSessionManager;
import org.olat.ims.lti.LTIManager;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 13.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OutcomeMapper implements Mapper, Serializable {
	private static final long serialVersionUID = 7954337449619783210L;
	private static final Logger log = Tracing.createLoggerFor(OutcomeMapper.class);

	private static final String READ_RESULT_REQUEST = "readResultRequest";
	private static final String DELETE_RESULT_REQUEST = "deleteResultRequest";
	private static final String REPLACE_RESULT_REQUEST = "replaceResultRequest";
	
	private String sourcedId;
	private Identity identity;
	private Long identityKey;
	private OLATResource resource;
	private String resSubPath;
	private String oauth_consumer_key;
	private String oauth_secret;
	
	public OutcomeMapper() {
		//
	}
	
	public OutcomeMapper(Identity identity, OLATResource resource, String resSubPath,
			String oauth_consumer_key, String oauth_secret,  String sourcedId) {
		this.sourcedId = sourcedId;
		this.oauth_consumer_key = oauth_consumer_key;
		this.oauth_secret = oauth_secret;
		this.identity = identity;
		this.identityKey = identity.getKey();
		this.resource = resource;
		this.resSubPath = resSubPath;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		reconnectUserSession(request);

		String contentType = request.getContentType();
		log.info(Tracing.M_AUDIT, "LTI outcome for: " + identityKey);
		// test on equals of content type, done the same way later on in IMSPOXRequest code 
		if (contentType != null && contentType.equals("application/xml") ) {
			String xmlResponse = doPostXml(request);
			return createMediaResource(xmlResponse, "application/xml");
		}

		return createMediaResource("Outcome service error: wrong content type. Must match 'application/xml' but was '" + (contentType == null ? "NULL" : contentType) + "'", "text/plain");
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public String getSourcedId() {
		return sourcedId;
	}
	
	protected void reconnectUserSession(HttpServletRequest request) {
		if(identity == null) {
			identity = CoreSpringFactory.getImpl(BaseSecurity.class).loadIdentityByKey(identityKey);
		}
		
		UserSession usess = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(request);
		if(usess == null) {
			usess = new UserSession();
		} 
		if(usess.getSessionInfo() == null) {
			usess.setSessionInfo(new SessionInfo(identityKey, request.getSession(true)));
		}
		if(usess.getIdentityEnvironment() == null || usess.getIdentity() == null) {
			usess.setIdentity(identity);
		}
	}
	
	private MediaResource createMediaResource(String body, String mimeType) {
		StringMediaResource mediares = new StringMediaResource();
		mediares.setData(body);
		mediares.setContentType(mimeType);
		mediares.setEncoding("UTF-8");
		return mediares;
	}
	
	private String doPostXml(HttpServletRequest request) {
		IMSPOXRequest pox = new IMSPOXRequest(oauth_consumer_key, oauth_secret, request);
		if(!pox.valid) {
			log.error("LTI outcome, OAuth verification failed: " + pox.errorMessage);
			return pox.getResponseFailure("OAuth verification failed", null);
		}

		String lti_message_type = pox.getOperation();
		Map<String,String> body = pox.getBodyMap();
		String reqSourceId =  body.get("/resultRecord/sourcedGUID/sourcedId");
		if(!sourcedId.equals(reqSourceId)) {
			log.error("LTI outcome sourcedId doesn't match: " + reqSourceId);
		}
		if(REPLACE_RESULT_REQUEST.equals(lti_message_type)) {
			String scoreString = body.get("/resultRecord/result/resultScore/textString");
			if(doUpdateResult(Float.parseFloat(scoreString))) {
				Map<String,Object> theMap = new TreeMap<>();
				theMap.put("/replaceResultRequest", "");
				String theXml = XMLMap.getXMLFragment(theMap, true);
				if (log.isDebugEnabled()) {
					log.debug("replace-result message successfull with score::" + scoreString);
				}
				return pox.getResponseSuccess("Update result",theXml);
			} else {
				if (log.isDebugEnabled()) {
					log.debug("replace-result message failed with score::" + scoreString);
				}
				return pox.getResponseFailure("Update result failed", null);
			}
		} else if(DELETE_RESULT_REQUEST.equals(lti_message_type)) {
			if(doDeleteResult()) {
				Map<String,Object> theMap = new TreeMap<>();
				theMap.put("/deleteResultRequest", "");
				String theXml = XMLMap.getXMLFragment(theMap, true);
				if (log.isDebugEnabled()) {
					log.debug("delete-result message successfull");
				}
				return pox.getResponseSuccess("Result deleted",theXml);
			} else {
				if (log.isDebugEnabled()) {
					log.debug("delete-result message failed");
				}
				return pox.getResponseFailure("Delete result failed", null);
			}
		} else if (READ_RESULT_REQUEST.equals(lti_message_type)) {
			return doReadResult(pox);
		}
		
		return pox.getResponseFailure("Not implemented", null);
	}
	
	protected String doReadResult(IMSPOXRequest pox) {
		return pox.getResponseFailure("Not implemented", null);
	}
	
	protected boolean doUpdateResult(Float score) {
		String outcomeValue = score == null ? null : score.toString();
		saveOutcome(REPLACE_RESULT_REQUEST, "/resultRecord/result/resultScore/textString", outcomeValue);
		return true;
	}
	
	protected boolean doDeleteResult() {
		saveOutcome(DELETE_RESULT_REQUEST, "/resultRecord/result/resultScore/textString", null);
		return true;
	}
	
	private void saveOutcome(String action, String outcomeKey, String outcomeValue) {

		CoreSpringFactory.getImpl(LTIManager.class).createOutcome(identity, resource, resSubPath, action, outcomeKey, outcomeValue);
	}
}
