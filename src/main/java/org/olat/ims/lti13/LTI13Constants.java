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
package org.olat.ims.lti13;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 8 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LTI13Constants {
	
	private LTI13Constants() {
		//
	}
	
	public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	
	
	public enum Claims {
		MESSAGE_TYPE("https://purl.imsglobal.org/spec/lti/claim/message_type"),
		VERSION("https://purl.imsglobal.org/spec/lti/claim/version"),
		RESOURCE_LINK("https://purl.imsglobal.org/spec/lti/claim/resource_link"),
		DEPLOYMENT_ID("https://purl.imsglobal.org/spec/lti/claim/deployment_id"),
		TARGET_LINK_URI("https://purl.imsglobal.org/spec/lti/claim/target_link_uri"),
	    TOOL_PLATFORM("https://purl.imsglobal.org/spec/lti/claim/tool_platform"),
		LAUNCH_PRESENTATION("https://purl.imsglobal.org/spec/lti/claim/launch_presentation"),
		ASSIGNMENT_AND_GRADING_SERVICE("https://purl.imsglobal.org/spec/lti-ags/claim/endpoint"),
		NAMES_AND_ROLES_SERVICE("https://purl.imsglobal.org/spec/lti-nrps/claim/namesroleservice"),
		BASIC_OUTCOMES("https://purl.imsglobal.org/spec/lti-bo/claim/basicoutcome"),
		CUSTOM("https://purl.imsglobal.org/spec/lti/claim/custom"),
		ROLES("https://purl.imsglobal.org/spec/lti/claim/roles"),
		CONTEXT("https://purl.imsglobal.org/spec/lti/claim/context"),
	    BASIC_OUTCOME("https://purl.imsglobal.org/spec/lti-bo/claim/basicoutcome")
		;
		
		private final String url;
		
		private Claims(String url) {
			this.url = url;
		}
		
		public String url() {
			return url;
		}
	}
	
	public enum Scopes {
		
		NRPS_CONTEXT_MEMBERSHIP("https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly"),
		AGS_LINE_ITEM_READ_ONLY("https://purl.imsglobal.org/spec/lti-ags/scope/lineitem.readonly"),
	    AGS_LINE_ITEM("https://purl.imsglobal.org/spec/lti-ags/scope/lineitem"),
	    AGS_RESULT_READ_ONLY("https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly"),
	    AGS_SCORE("https://purl.imsglobal.org/spec/lti-ags/scope/score"),
	    BASIC_OUTCOME("https://purl.imsglobal.org/spec/lti-bo/scope/basicoutcome")
		;
		
		private final String url;
		
		private Scopes(String url) {
			this.url = url;
		}
		
		public String url() {
			return url;
		}
	}
	
	public enum Roles {
		// Membership
		LEARNER("Learner",                      "http://purl.imsglobal.org/vocab/lis/v2/membership#Learner"),
		INSTRUCTOR("Instructor",                "http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor"),
	    MENTOR("Mentor",                        "http://purl.imsglobal.org/vocab/lis/v2/membership#Mentor"),
	    TEACHING_ASSISTANT("TeachingAssistant", "http://purl.imsglobal.org/vocab/lis/v2/membership/Instructor#TeachingAssistant"),
	    CONTENT_DEVELOPER("ContentDeveloper",   "http://purl.imsglobal.org/vocab/lis/v2/membership#ContentDeveloper"),
		// Institution
		INSTITUTION_INSTRUCTOR("Instructor", "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Instructor"),
		INSTITUTION_STUDENT(null,            "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Student"),
	    ADMINISTRATOR("Administrator",       "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Administrator")
	    ;
	    
	    private final String roleV2;
	    private final String roleEditor;
	    
	    private Roles(String roleEditor, String roleV2) {
	    	this.roleV2 = roleV2;
	    	this.roleEditor = roleEditor;
	    }
	    
	    public String roleV2() {
	    	return roleV2;
	    }
	    
	    public String editor() {
	    	return roleEditor;
	    }
	    
	    public static List<String> editorToRoleV2(List<String> editorValues) {
	    	List<String> roleV2List = new ArrayList<>();
	    	if(editorValues != null && !editorValues.isEmpty()) {
	    		for(String editorValue:editorValues) {
	    			Roles role = editorToRoles(editorValue);
	    			if(role != null) {
	    				roleV2List.add(role.roleV2());
	    			}
	    		}
	    	}
	    	return roleV2List;
	    }
	    
	    public static Roles editorToRoles(String editorValue) {
	    	if(StringHelper.containsNonWhitespace(editorValue)) {
	    		for(Roles role:values()) {
	    			if(editorValue.equals(role.editor())) {
	    				return role;
	    			}
	    		}
	    	}
	    	return null;
	    }
	}
	
	public enum UserAttributes {
		GIVEN_NAME("given_name", UserConstants.FIRSTNAME),
		FAMILY_NAME("family_name", UserConstants.LASTNAME),
		EMAIL("email", UserConstants.EMAIL),
		;
		
		private final String ltiAttribute;
		private final String openolatAttribute;
		
		private UserAttributes(String ltiAttribute, String openolatAttribute) {
			this.ltiAttribute = ltiAttribute;
			this.openolatAttribute = openolatAttribute;
		}
		
		public String ltiAttribute() {
			return ltiAttribute;
		}
		
		public String openolatAttribute() {
			return openolatAttribute;
		}
	}
	
	public enum ActivityProgress {
		/**
		 * The user has not started the activity, or the activity has been reset for that student.
		 */
		Initialized,
		/**
		 * The activity associated with the line item has been started by the user to which the result relates.
		 */
		Started,
		/**
		 * The activity is being drafted and is available for comment.
		 */
		InProgress,
		/**
		 * The activity has been submitted at least once by the user but the user is still able make further submissions.
		 */
		Submitted,
		/**
		 * The user has completed the activity associated with the line item.
		 */
		Completed;
		
		public static ActivityProgress valueOf(AssessmentEvaluation eval) {
			AssessmentEntryStatus status = eval.getAssessmentStatus();
			if(status == null || status == AssessmentEntryStatus.notStarted) {
				return Initialized;
			}
			if(status == AssessmentEntryStatus.inProgress) {
				return InProgress;
			}
			if(status == AssessmentEntryStatus.inReview) {
				return Submitted;
			}
			if(status == AssessmentEntryStatus.done) {
				return Completed;
			}
			return InProgress;
		}
	}
	
	public enum GradingProgress {
		/**
		 * The grading process is completed; the score value, if any, represents the current Final Grade;
		 */
		FullyGraded,
		/**
		 * Final Grade is pending, but does not require manual intervention; if a Score value is present, it indicates the current value is partial and may be updated.
		 */
		Pending,
		/**
		 * Final Grade is pending, and it does require human intervention; if a Score value is present, it indicates the current value is partial and may be updated during the manual grading.
		 */
		PendingManual,
		/**
		 * The grading could not complete.
		 */
		Failed,
		/**
		 * There is no grading process occurring; for example, the student has not yet made any submission.
		 */
		NotReady;
		
		public static GradingProgress valueOf(AssessmentEvaluation eval) {
			AssessmentEntryStatus status = eval.getAssessmentStatus();
			if(status == null || status == AssessmentEntryStatus.notStarted) {
				return NotReady;
			}
			if(status == AssessmentEntryStatus.inProgress) {
				return Pending;
			}
			if(status == AssessmentEntryStatus.inReview) {
				return PendingManual;
			}
			if(status == AssessmentEntryStatus.done) {
				return FullyGraded;
			}
			return NotReady;
		}
	}
	
	public static class Keys {
		
		private Keys() {
			//
		}
		
		public static final String KEY_IDENTIFIER = "kid";
		public static final String ALGORITHM = "alg";
		public static final String TYPE = "typ";
		public static final String JWT = "JWT";
	}
	
	public static class UserSub {
		
		private UserSub() {
			//
		}
		
		public static final String GIVEN_NAME = "given_name";
		public static final String FAMILY_NAME = "family_name";
		public static final String EMAIL = "email";
		public static final String LOCALE = "locale";
		
	}
	
	public static class OAuth {
		
		public static final String BEARER = "Bearer";
		public static final String GRANT_TYPE = "grant_type";
		public static final String CLIENT_CREDENTIALS = "client_credentials";
		public static final String CLIENT_ASSERTION = "client_assertion";
		public static final String CLIENT_ASSERTION_TYPE = "client_assertion_type";
		public static final String SCOPE = "scope";
		public static final String ID_TOKEN = "id_token";
		public static final String ERROR = "error";
		public static final String STATE = "state";


	    public static final String CLIENT_ASSERTION_TYPE_BEARER = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
	    
	    private OAuth() {
	    	//
	    }
	}
	
	public static class AGS {
		
		public static final String LINEITEMS_URL = "lineitems";
	    public static final String LINEITEM_URL = "lineitem";
	    
	    private AGS() {
	    	//
	    }
	}
	
	public static class NRPS {
		
		public static final String MEMBERSHIPS_URL = "context_memberships_url";
	    
		private NRPS() {
			//
		}
	}
	
	public static class Errors {

		public static final String INVALID_TARGET_LINK_URI = "invalid_target_link_uri";
		public static final String INVALID_REQUEST = "invalid_request";
		public static final String INVALID_STATE = "invalid_state";
		public static final String INVALID_CONTENT_TYPE = "invalid_content_type";
		
		private Errors() {
			//
		}
	}
	
	public static class ContentTypes {
		
		public static final String SCORE_CONTENT_TYPE = "application/vnd.ims.lis.v1.score+json";
	    public static final String LINE_ITEM_CONTENT_TYPE = "application/vnd.ims.lis.v2.lineitem+json";
	    public static final String LINE_ITEM_CONTAINER_CONTENT_TYPE = "application/vnd.ims.lis.v2.lineitemcontainer+json";
	    public static final String RESULT_CONTAINER_CONTENT_TYPE = "application/vnd.ims.lis.v2.resultcontainer+json";
	    
	    private ContentTypes() {
	    	//
	    }
	}
	
	public static class ContextTypes {
		
		public static final String COURSE_TEMPLATE = "http://purl.imsglobal.org/vocab/lis/v2/course#CourseTemplate";
	    public static final String COURSE_OFFERING = "http://purl.imsglobal.org/vocab/lis/v2/course#CourseOffering";
	    public static final String COURSE_SECTION = "http://purl.imsglobal.org/vocab/lis/v2/course#CourseSection";
	    public static final String GROUP = "http://purl.imsglobal.org/vocab/lis/v2/course#Group";
	    
	    private ContextTypes() {
	    	//
	    }
	}
}
