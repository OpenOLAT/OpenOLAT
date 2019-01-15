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
package org.olat.modules.gotomeeting.manager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.modules.gotomeeting.model.GoToErrorG2T;
import org.olat.modules.gotomeeting.model.GoToErrors;
import org.olat.modules.gotomeeting.model.GoToOrganizerG2T;
import org.olat.modules.gotomeeting.model.GoToRecordingsG2T;
import org.olat.modules.gotomeeting.model.GoToRegistrantG2T;
import org.olat.modules.gotomeeting.model.GoToTrainingG2T;

/**
 * Make the dirty job to understand the JSON return by the GoToMeeting servers.
 * 
 * 
 * Initial date: 23.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToJsonUtil {
	
	private static final OLog log = Tracing.createLoggerFor(GoToJsonUtil.class);
    private static SimpleDateFormat gotoReadFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static SimpleDateFormat gotoReadRecordingFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
    private static DateFormat gotoPostFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    static {
    	gotoReadFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    	gotoReadRecordingFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    	gotoPostFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
	
	/*
{
  "name": "string",
  "description": "string",
  "timeZone": "string",
  "times": [
    {
      "startDate": "2016-03-23T10:00:00Z",
      "endDate": "2016-03-23T11:00:00Z"
    }
  ],
  "registrationSettings": {
    "disableConfirmationEmail": true,
    "disableWebRegistration": true
  },
  "organizers": [
    0
  ]
}
	 */
	protected static final JSONObject training(long organizerKey, String name, String description,
			String timeZoneId, Date start, Date end) {
		try {
			JSONObject json = new JSONObject();
			json.put("name", name);
			json.put("description", description);
			json.put("timeZone", timeZoneId);
			
			JSONObject times = new JSONObject();
			String startTime = formatDateTimeForPost(start);
			times.put("startDate", startTime);
			String endTime = formatDateTimeForPost(end);
			times.put("endDate", endTime);
			json.append("times", times);

			JSONObject registrationSettings = new JSONObject();
			registrationSettings.put("disableConfirmationEmail", "true");
			registrationSettings.put("disableWebRegistration", "true");
			json.put("registrationSettings", registrationSettings);
			
			JSONArray organizers = new JSONArray();
			organizers.put(organizerKey);
			json.put("organizers", organizers);

			return json;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
/*
{
  "name": "string",
  "description": "string"
}
*/
	protected static final JSONObject trainingNameDescription(String name, String description) {
		try {
			JSONObject json = new JSONObject();
			json.put("name", name);
			json.put("description", description);
			return json;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
/*
{
  "timeZone": "string",
  "times": [
    {
      "startDate": "2016-03-29T10:00:00Z",
      "endDate": "2016-03-29T11:00:00Z"
    }
  ],
  "notifyRegistrants": false,
  "notifyTrainers": false
}
*/
	protected static final JSONObject trainingTimes(String timeZoneId, Date start, Date end) {
		try {
			JSONObject json = new JSONObject();
			json.put("timeZone", timeZoneId);
			
			JSONObject times = new JSONObject();
			String startTime = formatDateTimeForPost(start);
			times.put("startDate", startTime);
			String endTime = formatDateTimeForPost(end);
			times.put("endDate", endTime);
			json.append("times", times);
			
			json.put("notifyRegistrants", false);
			json.put("notifyTrainers", false);
			return json;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
/*
{
  "trainingId": "string",
  "name": "string",
  "description": "string",
  "timeZone": "string",
  "times": [
    {
      "startDate": "2016-03-24T10:00:00Z",
      "endDate": "2016-03-24T11:00:00Z"
    }
  ],
  "organizers": [
    {
      "organizerKey": "string",
      "email": "string",
      "givenName": "string",
      "surname": "string"
    }
  ],
  "registrationSettings": {
    "disableConfirmationEmail": true,
    "disableWebRegistration": true
  },
  "trainingKey": "string"
}
*/
	protected static final GoToTrainingG2T parseTraining(String content) {
		try {
			JSONObject json = new JSONObject(content);
			GoToTrainingG2T training = new GoToTrainingG2T();
			training.setName(json.optString("name"));
			training.setDescription(json.optString("description"));
			training.setTrainingKey(json.optString("trainingKey", null));
			training.setTimeZoneId(json.optString("timeZone", null));
			
			JSONArray times = json.getJSONArray("times");
			JSONObject time = times.getJSONObject(0);
			
			String startDate = time.optString("startDate", null);
			Date start = parseDateTime(startDate);
			training.setStart(start);
			String endDate = time.optString("endDate", null);
			Date end = parseDateTime(endDate);
			training.setEnd(end);

			return training;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
/*
	{"errorCode":"DuplicateRegistrant","description":"Registration failed, email address already in use.","incident":3437843380023983360,"registrantKey":3019527584166801154} ^%^ cause:n/a
*/
	protected static final GoToErrorG2T parseError(String content) {
		try {
			JSONObject json = new JSONObject(content);
			GoToErrorG2T error = new GoToErrorG2T();
			error.setErrorCode(GoToErrors.valueOfOrNull(json.optString("errorCode", null)));
			error.setDescription(json.optString("description", null));
			error.setRegistrantKey(json.optString("registrantKey", null));
			return error;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
/*
	{
	  "email": "string",
	  "givenName": "string",
	  "surname": "string"
	}
*/
	protected static final JSONObject registrant(Identity identity) {
		try {
			JSONObject json = new JSONObject();
			json.put("email", identity.getUser().getProperty(UserConstants.EMAIL, null));
			json.put("givenName", identity.getUser().getProperty(UserConstants.FIRSTNAME, null));
			json.put("surname", identity.getUser().getProperty(UserConstants.LASTNAME, null));
			return json;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
/*
{
	 "hostURL": "string"
}
*/
	protected static final String parseHostUrl(String content) {
		try {
			JSONObject json = new JSONObject(content);
			return json.optString("hostURL", null);
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
/*
{
  "joinUrl": "string",
  "confirmationUrl": "string",
  "registrantKey": "string"
}
 */
	protected static final GoToRegistrantG2T parseAddRegistrant(String content) {
		try {
			JSONObject json = new JSONObject(content);
			GoToRegistrantG2T registrant = new GoToRegistrantG2T();
			registrant.setRegistrantKey(json.optString("registrantKey", null));
			registrant.setJoinUrl(json.optString("joinUrl", null));
			registrant.setConfirmationUrl(json.optString("confirmationUrl", null));
			return registrant;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
/*
{
  "access_token":"RlUe11faKeyCWxZToK3nk0uTKAL",
  "expires_in":"30758399",
  "refresh_token":"d1cp20yB3hrFAKeTokenTr49EZ34kTvNK",
  "organizer_key":"8439885694023999999",
  "account_key":"9999982253621659654",
  "account_type":"",
  "firstName":"Mahar",
  "lastName":"Singh",
  "email":"mahar.singh@singhSong.com",
  "platform":"GLOBAL",
  "version":"2",
}	
 */
	protected static final GoToOrganizerG2T parseDirectLogin(String content) {
		try {
			JSONObject json = new JSONObject(content);
			GoToOrganizerG2T organizer = new GoToOrganizerG2T();
			organizer.setAccessToken(json.optString("access_token", null));
			organizer.setOrganizerKey(json.optString("organizer_key", null));
			organizer.setExpiresIn(Long.parseLong(json.optString("expires_in", "0")));
			organizer.setFirstName(json.optString("firstName", null));
			organizer.setLastName(json.optString("lastName", null));
			organizer.setEmail(json.optString("email", null));
			organizer.setAccountKey(json.optString("account_key", null));
			return organizer;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	/*
	{
	  "access_token":"RlUe11faKeyCWxZToK3nk0uTKAL",
	  "expires_in":"30758399",
	  "refresh_token":"d1cp20yB3hrFAKeTokenTr49EZ34kTvNK",
	  "organizer_key":"8439885694023999999",
	  "account_key":"9999982253621659654",
	  "account_type":"",
	  "firstName":"Mahar",
	  "lastName":"Singh",
	  "email":"mahar.singh@singhSong.com",
	  "platform":"GLOBAL",
	  "version":"2",
	}	
	 */
	public static final GoToOrganizerG2T parseToken(String content) {
		try {
			JSONObject json = new JSONObject(content);
			GoToOrganizerG2T organizer = new GoToOrganizerG2T();
			organizer.setAccessToken(json.optString("access_token", null));
			organizer.setRefreshToken(json.optString("refresh_token", null));
			organizer.setOrganizerKey(json.optString("organizer_key", null));
			organizer.setAccountKey(json.optString("account_key", null));
			organizer.setExpiresIn(Long.parseLong(json.optString("expires_in", "0")));
			organizer.setFirstName(json.optString("firstName", null));
			organizer.setLastName(json.optString("lastName", null));
			organizer.setEmail(json.optString("email", null));
			return organizer;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
/*
	[
	  {
	    "email": "string",
	    "givenName": "string",
	    "surname": "string",
	    "status": "WAITING",
	    "registrationDate": "2016-03-23T16:00:00Z",
	    "joinUrl": "string",
	    "confirmationUrl": "string",
	    "registrantKey": "string"
	  }
	]
*/
	protected static final List<GoToRegistrantG2T> parseRegistrants(String content) {
		try {
			List<GoToRegistrantG2T> registrants = new ArrayList<>();
			JSONArray jsonArr = new JSONArray(content);
			for(int i=jsonArr.length(); i-->0; ) {
				JSONObject registrantJson = jsonArr.getJSONObject(i);
				GoToRegistrantG2T registrant = new GoToRegistrantG2T();
				registrant.setEmail(registrantJson.optString("email", null));
				registrant.setGivenName(registrantJson.optString("givenName", null));
				registrant.setSurname(registrantJson.optString("surname", null));
				registrant.setStatus(registrantJson.optString("status", null));
				registrant.setJoinUrl(registrantJson.optString("joinUrl", null));
				registrant.setConfirmationUrl(registrantJson.optString("confirmationUrl", null));
				registrant.setRegistrantKey(registrantJson.optString("registrantKey", null));
				registrants.add(registrant);
			}
			return registrants;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}

/*
{
  "recordingList": [
    {
      "recordingId": 0,
      "name": "string",
      "description": "string",
      "registrationUrl": "string",
      "downloadUrl": "string",
      "startDate": "2016-03-31T14:00:00Z",
      "endDate": "2016-03-31T15:00:00Z"
    }
  ],
  "trainingKey": 0
}
*/
	protected static final List<GoToRecordingsG2T> parseRecordings(String content) {
		try {
			List<GoToRecordingsG2T> recordings = new ArrayList<>();
			JSONObject jsonObj = new JSONObject(content);
			JSONArray jsonArr = jsonObj.getJSONArray("recordingList");
			for(int i=jsonArr.length(); i-->0; ) {
				JSONObject recordingJson = jsonArr.getJSONObject(i);
				GoToRecordingsG2T recording = new GoToRecordingsG2T();
				recording.setRecordingId(recordingJson.optString("recordingId", null));
				recording.setName(recordingJson.optString("name", null));
				recording.setDescription(recordingJson.optString("description", null));
				recording.setRegistrationUrl(recordingJson.optString("registrationUrl", null));
				recording.setDownloadUrl(recordingJson.optString("downloadUrl", null));
				
				String startDate = recordingJson.optString("startDate", null);
				Date start = parseRecordingDateTime(startDate);
				recording.setStartDate(start);
				String endDate = recordingJson.optString("endDate", null);
				Date end = parseRecordingDateTime(endDate);
				recording.setEndDate(end);
				
				recordings.add(recording);
			}
			return recordings;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	protected static Date parseDateTime(String string) {
		Date date = null;
		if(string != null) {
			synchronized(gotoReadFormat) {
				try {
					date = gotoReadFormat.parse(string);
				} catch (ParseException e) {
					log.error("Cannot parse date: " + string);
				}
			}
		}
		return date;
	}
	
	protected static Date parseRecordingDateTime(String string) {
		Date date = null;
		if(string != null) {
			synchronized(gotoReadRecordingFormat) {
				try {
					date = gotoReadRecordingFormat.parse(string);
				} catch (ParseException e) {
					log.error("Cannot parse date: " + string);
				}
			}
		}
		return date;
	}
	
	
	
	/**
	 * Convert to UTC with trailing Z
	 * @param date
	 * @return
	 */
	protected static String formatDateTimeForPost(Date date) {
		String string = null;
		if(date != null) {
			synchronized(gotoPostFormat) {
				string = gotoPostFormat.format(date);
			}
		}
		return string;
	}
}