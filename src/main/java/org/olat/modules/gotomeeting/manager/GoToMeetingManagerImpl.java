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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.modules.gotomeeting.GoToMeeting;
import org.olat.modules.gotomeeting.GoToMeetingManager;
import org.olat.modules.gotomeeting.GoToMeetingModule;
import org.olat.modules.gotomeeting.GoToOrganizer;
import org.olat.modules.gotomeeting.GoToRegistrant;
import org.olat.modules.gotomeeting.model.GoToError;
import org.olat.modules.gotomeeting.model.GoToErrorG2T;
import org.olat.modules.gotomeeting.model.GoToErrors;
import org.olat.modules.gotomeeting.model.GoToMeetingImpl;
import org.olat.modules.gotomeeting.model.GoToOrganizerG2T;
import org.olat.modules.gotomeeting.model.GoToRecordingsG2T;
import org.olat.modules.gotomeeting.model.GoToRegistrantG2T;
import org.olat.modules.gotomeeting.model.GoToTrainingG2T;
import org.olat.modules.gotomeeting.model.GoToType;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class GoToMeetingManagerImpl implements GoToMeetingManager {
	
	private static final Logger log = Tracing.createLoggerFor(GoToMeetingManagerImpl.class);
	
	private String tokenUrl = "https://api.getgo.com/oauth/v2/token";
	private String gotoTrainingUrl = "https://api.getgo.com/G2T/rest";

	@Autowired
	private DB dbInstance;
	@Autowired
	private GoToMeetingDAO meetingDao;
	@Autowired
	private GoToOrganizerDAO organizerDao;
	@Autowired
	private GoToRegistrantDAO registrantDao;
	@Autowired
	private GoToMeetingModule goToMeetingModule;
	@Autowired
	private HttpClientService httpClientService;


	@Override
	public List<GoToOrganizer> getOrganizersFor(Identity identity) {
		return organizerDao.getOrganizersFor(identity);
	}

	@Override
	public boolean createOrUpdateOrganizer(GoToOrganizerG2T org) {
		List<GoToOrganizer> organizers = organizerDao.getOrganizers(org.getAccountKey(), org.getOrganizerKey());
		
		String name = org.getFirstName() + " " + org.getLastName();
		if(organizers.isEmpty()) {
			organizerDao.createOrganizer(name, org.getEmail(), org.getAccessToken(), org.getRefreshToken(), org.getOrganizerKey(), 
					org.getFirstName(), org.getLastName(), org.getEmail(), org.getAccountKey(), org.getExpiresIn(), null);
		} else {
			GoToOrganizer organizer = organizers.get(0);
			organizerDao.updateOrganizer(organizer, name, org.getAccessToken(), org.getRefreshToken(), org.getOrganizerKey(),
					org.getFirstName(), org.getLastName(), org.getEmail(), org.getAccountKey(), org.getExpiresIn());
		}
		return true;
	}	
	
	@Override
	public boolean createOrUpdateOrganizer(String name, String username, String password, Identity owner,
			GoToError error) {
		GoToOrganizerG2T org = login(username, password, error);
		if(org != null) {
			List<GoToOrganizer> organizers = organizerDao.getOrganizers(org.getAccountKey(), org.getOrganizerKey());
			if(organizers.isEmpty()) {
				organizerDao.createOrganizer(name, username, org.getAccessToken(), org.getRefreshToken(), org.getOrganizerKey(),
						org.getFirstName(), org.getLastName(), org.getEmail(),
						org.getAccountKey(), org.getExpiresIn(), owner);
			} else {
				GoToOrganizer organizer = organizers.get(0);
				organizerDao.updateOrganizer(organizer, name, org.getAccessToken(), org.getRefreshToken(), org.getOrganizerKey(),
						org.getFirstName(), org.getLastName(), org.getEmail(), org.getAccountKey(), org.getExpiresIn());
			}
			return true;
		}
		return false;
	}

	@Override
	public void updateOrganizer(GoToOrganizer organizer, String name) {
		GoToOrganizer reloadedOrganizer = organizerDao.loadOrganizerByKey(organizer.getKey());
		organizerDao.updateOrganizer(reloadedOrganizer, name);
	}

	@Override
	public boolean removeOrganizer(GoToOrganizer organizer) {
		if(meetingDao.countMeetingsOrganizedBy(organizer) == 0) {
			organizerDao.deleteOrganizer(organizer);
			return true;
		}
		return false;
	}

	@Override
	public boolean checkOrganizerAvailability(GoToOrganizer organizer, Date start, Date end) {
		List<GoToMeeting> meetings = meetingDao.getMeetingsOverlap(GoToType.training, organizer, start, end);
		return meetings.isEmpty();
	}

	@Override
	public GoToMeeting scheduleTraining(GoToOrganizer organizer, String name, String externalId, String description, Date start, Date end,
			RepositoryEntry resourceOwner, String subIdentifier, BusinessGroup businessGroup, GoToError error) {
		
		GoToMeeting scheduledMeeting = null;
		try {
			String url = gotoTrainingUrl + "/organizers/" + organizer.getOrganizerKey() + "/trainings";
			HttpPost post = new HttpPost(url);
			decorateWithAccessToken(post, organizer);
			
			String timeZoneId = goToMeetingModule.getGoToTimeZoneId();
			JSONObject trainingJson = GoToJsonUtil
					.training(Long.parseLong(organizer.getOrganizerKey()), name, description, timeZoneId, start, end);
			String objectStr = trainingJson.toString();
			post.setEntity(new StringEntity(objectStr, ContentType.APPLICATION_JSON));
			
			GoToResponse response = execute(post);
			int status = response.status();
			if(status == 201) {//created
				String trainingKey = response.content();
				trainingKey = trainingKey.replace("\"", "");
				scheduledMeeting = meetingDao.createTraining(name, externalId, description, trainingKey, start, end, organizer, resourceOwner, subIdentifier, businessGroup);
				dbInstance.commit();
			} else {
				logGoToError("scheduleTraining", response, error);
			}
		} catch(Exception e) {
			log.error("", e);
		}
		return scheduledMeeting;
	}
	
	@Override
	public GoToMeeting updateTraining(GoToMeeting meeting, String name, String description, Date start, Date end, GoToError error) {
		//reload the training from GoTo
		GoToTrainingG2T training = getTraining(meeting, error);
		if(training != null && !error.hasError()) {
			GoToMeetingImpl meetingImpl = (GoToMeetingImpl)meeting;
			if(!training.getName().equals(name) ||
					(StringHelper.containsNonWhitespace(description) && !"-".equals(description) && !description.equals(training.getDescription()))) {
				//update name and description
				updateNameDescription(meetingImpl, training, name, description, error);
			}

			if(!error.hasError()) {
				if((start != null && start.compareTo(training.getStart()) != 0)
						|| (end != null && end.compareTo(training.getEnd()) != 0)) {
					updateStartEnd(meetingImpl, start, end, error);
				}
			}

			if(!error.hasError()) {
				meeting = meetingDao.update(meetingImpl);
			}
		}
		return meeting;
	}
	
	private void updateStartEnd(GoToMeetingImpl meeting, Date start, Date end, GoToError error) {
		try {
			GoToOrganizer organizer = meeting.getOrganizer();
			String url = gotoTrainingUrl + "/organizers/" + organizer.getOrganizerKey() + "/trainings/" + meeting.getMeetingKey() + "/times";

			HttpPut put = new HttpPut(url);
			decorateWithAccessToken(put, organizer);

			String payload = GoToJsonUtil.trainingTimes(goToMeetingModule.getGoToTimeZoneId(), start, end).toString();
			put.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
			
			GoToResponse response = execute(put);
			int status = response.status();
			if(status == 200) {//created
				meeting.setStartDate(start);
				meeting.setEndDate(end);
			} else {
				logGoToError("updateStartEnd", response, error);
			}
		} catch(Exception e) {
			log.error("", e);
		}
	}
	
	private void updateNameDescription(GoToMeetingImpl meeting, GoToTrainingG2T training, String name, String description, GoToError error) {
		try {
			GoToOrganizer organizer = meeting.getOrganizer();
			String url = gotoTrainingUrl + "/organizers/" + organizer.getOrganizerKey() + "/trainings/" + meeting.getMeetingKey() + "/nameDescription";

			HttpPut put = new HttpPut(url);
			this.decorateWithAccessToken(put, organizer);
			
			if(!StringHelper.containsNonWhitespace(description) || "-".equals(description)) {
				description = training.getDescription();
			}
			
			String payload = GoToJsonUtil.trainingNameDescription(name, description).toString();
			put.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
			
			GoToResponse response = execute(put);
			int status = response.status();
			if(status == 204) {//created
				meeting.setName(name);
				meeting.setDescription(description);
			} else {
				logGoToError("updateNameDescription", response, error);
			}
		} catch(Exception e) {
			log.error("", e);
		}
	}

	/**
	 * Error code: 400 (Bad Request), 403 (Forbidden), 404 (Not Found), 409 (Conflict)
	 */
	@Override
	public GoToRegistrant registerTraining(GoToMeeting meeting, Identity trainee, GoToError error) {
		GoToRegistrant registrant = registrantDao.getRegistrant(meeting, trainee);
		if(registrant == null) {
			try {
				GoToOrganizer organizer = meeting.getOrganizer();
				String url = gotoTrainingUrl + "/organizers/" + organizer.getOrganizerKey() + "/trainings/" + meeting.getMeetingKey() + "/registrants";

				HttpPost post = new HttpPost(url);
				decorateWithAccessToken(post, organizer);
				
				String traineeJson = GoToJsonUtil.registrant(trainee).toString();
				post.setEntity(new StringEntity(traineeJson, ContentType.APPLICATION_JSON));
				
				GoToResponse response = execute(post);
				int status = response.status();
				if(status == 201) {//created
					String content = response.content();
					GoToRegistrantG2T registrantVo = GoToJsonUtil.parseAddRegistrant(content);
					registrant = registrantDao.createRegistrant(meeting, trainee, registrantVo.getRegistrantKey(), registrantVo.getJoinUrl(), registrantVo.getConfirmationUrl());
				} else if(status == 409) {
					String content = response.content();
					GoToErrorG2T errorVo = GoToJsonUtil.parseError(content);
					if(errorVo.getErrorCode() == GoToErrors.DuplicateRegistrant && StringHelper.containsNonWhitespace(errorVo.getRegistrantKey())) {
						//already registrate but not in OpenOLAT
						GoToRegistrantG2T registrantVo = getRegistrant(errorVo.getRegistrantKey(), meeting, error);
						registrant = registrantDao.createRegistrant(meeting, trainee, registrantVo.getRegistrantKey(), registrantVo.getJoinUrl(), registrantVo.getConfirmationUrl());
					} else {
						logGoToError("registerTraining", status, content, error);
					}
				} else {
					logGoToError("registerTraining", response, error);
				}
			} catch(Exception e) {
				log.error("", e);
			}
		}
		return registrant;
	}
	
	private GoToRegistrantG2T getRegistrant(String registrantKey, GoToMeeting meeting, GoToError error) {
		try {
			GoToOrganizer organizer = meeting.getOrganizer();
			String url = gotoTrainingUrl + "/organizers/" + organizer.getOrganizerKey() + "/trainings/" + meeting.getMeetingKey() + "/registrants/" + registrantKey;

			HttpGet get = new HttpGet(url);
			decorateWithAccessToken(get, organizer);

			GoToResponse response = execute(get);
			if(response.status() == 200) {
				String content = response.content();
				return GoToJsonUtil.parseAddRegistrant(content);
			} else {
				logGoToError("getRegistrant", response, error);
				return null;
			}
		} catch(Exception e) {
			log.error("", e);
			return null;
		}
	}

	
	private void logGoToError(String method, GoToResponse response, GoToError error) {
		int status = response.status();
		error.setErrorCode(status);
		String responseString = response.content();
		try {
			GoToErrorG2T errorVo = GoToJsonUtil.parseError(responseString);
			if(errorVo != null) {
				error.setError(errorVo.getErrorCode());
				error.setDescription(errorVo.getDescription());
			}
		} catch (Exception e) {
			log.error("", e);
		} finally {
			log.error("{} return {}: {}", method, status, responseString);
		}
	}
	
	private void logGoToError(String method, int status, String responseString, GoToError error) {
		error.setErrorCode(status);
		log.error("{} return {}: {}", method, status, responseString);
	}
	
	@Override
	public List<GoToMeeting> getAllMeetings() {
		return meetingDao.getMeetings();
	}

	@Override
	public List<GoToMeeting> getMeetings(GoToType type, RepositoryEntryRef entry, String subIdent, BusinessGroupRef businessGroup) {
		return meetingDao.getMeetings(type, entry, subIdent, businessGroup);
	}

	@Override
	public GoToMeeting getMeetingByKey(Long meetingKey) {
		return meetingDao.loadMeetingByKey(meetingKey);
	}
	
	@Override
	public GoToMeeting getMeetingByExternalId(String externalId) {
		return meetingDao.loadMeetingByExternalId(externalId);
	}

	@Override
	public GoToMeeting getMeeting(GoToMeeting meeting, GoToError error) {
		GoToMeeting reloadMeeting = meetingDao.loadMeetingByKey(meeting.getKey());
		if(reloadMeeting != null) {
			GoToTrainingG2T trainingVo = getTraining(meeting, error);
			if(trainingVo == null) {
				log.error("Training not found");
			}
		}
		return reloadMeeting;
	}
	
	private GoToTrainingG2T getTraining(GoToMeeting meeting, GoToError error) {
		try {
			GoToOrganizer organizer = meeting.getOrganizer();
			String url = gotoTrainingUrl + "/organizers/" + organizer.getOrganizerKey() + "/trainings/" + meeting.getMeetingKey();

			HttpGet get = new HttpGet(url);
			decorateWithAccessToken(get, organizer);

			GoToResponse response = execute(get);
			int status = response.status();
			if(status == 200) {//deleted
				String content = response.content();
				return GoToJsonUtil.parseTraining(content);
			} else {
				logGoToError("getTraining", response, error);
				return null;
			}
		} catch(Exception e) {
			log.error("", e);
			return null;
		}
	}

	@Override
	public String startTraining(GoToMeeting meeting, GoToError error) {
		try {
			GoToOrganizer organizer = meeting.getOrganizer();
			String url = gotoTrainingUrl + "/trainings/" + meeting.getMeetingKey() + "/start";

			HttpGet get = new HttpGet(url);
			decorateWithAccessToken(get, organizer);

			GoToResponse response = execute(get);
			int status = response.status();
			if(status == 200) {//deleted
				String content = response.content();
				return GoToJsonUtil.parseHostUrl(content);
			} else {
				logGoToError("startTraining", response, error);
				return null;
			}
		} catch(Exception e) {
			log.error("", e);
			return null;
		}
	}

	@Override
	public List<GoToRegistrant> getRegistrants(IdentityRef identity, RepositoryEntryRef entry, String subIdent, BusinessGroupRef businessGroup) {
		return registrantDao.getRegistrants(identity, entry, subIdent, businessGroup);
	}

	@Override
	public GoToRegistrant getRegistrant(GoToMeeting meeting, IdentityRef trainee) {
		return registrantDao.getRegistrant(meeting, trainee);
	}

	@Override
	public List<GoToRecordingsG2T> getRecordings(GoToMeeting meeting, GoToError error) {
		try {
			GoToOrganizer organizer = meeting.getOrganizer();
			String url = gotoTrainingUrl + "/trainings/" + meeting.getMeetingKey() + "/recordings";

			HttpGet get = new HttpGet(url);
			decorateWithAccessToken(get, organizer);

			GoToResponse response = execute(get);
			int status = response.status();
			if(status == 200) {//deleted
				String content = response.content();
				return GoToJsonUtil.parseRecordings(content);
			} else {
				logGoToError("getRecordings", response, error);
				return null;
			}
		} catch(Exception e) {
			log.error("", e);
			return null;
		}
	}

	@Override
	public boolean delete(GoToMeeting meeting) {
		GoToError error = new GoToError();
		if(deleteTraining(meeting, error)
				|| error.getError() == GoToErrors.NoSuchTraining
				|| error.getError() == GoToErrors.InvalidRequest) {
			meetingDao.delete(meeting);
			return true;
		}
		//do nothing
		return false;
	}
	
	private boolean deleteTraining(GoToMeeting meeting, GoToError error) {
		try {
			GoToOrganizer organizer = meeting.getOrganizer();
			String url = gotoTrainingUrl + "/organizers/" + organizer.getOrganizerKey() + "/trainings/" + meeting.getMeetingKey();

			HttpDelete delete = new HttpDelete(url);
			decorateWithAccessToken(delete, organizer);

			GoToResponse response = execute(delete);
			int status = response.status();
			if(status == 204) {//deleted
				return true;
			} else if (status == 404 || status == 400) {
				String content = response.content();
				GoToErrorG2T errorVo = GoToJsonUtil.parseError(content);
				if(errorVo.getErrorCode() == GoToErrors.NoSuchTraining
						|| errorVo.getErrorCode() == GoToErrors.InvalidRequest) {
					error.setError(errorVo.getErrorCode());
					error.setDescription(errorVo.getDescription());
				} else {
					log.error("deleteTraining return {}: {}", status, content);
				}
			} else {
				logGoToError("deleteTraining", response, error);
			}
			return false;
		} catch(Exception e) {
			log.error("", e);
			return false;
		}
	}

	@Override
	public void deleteAll(RepositoryEntryRef entry, String subIdent, BusinessGroupRef businessGroup) {
		List<GoToMeeting> trainings = meetingDao.getMeetings(GoToType.training, entry, subIdent, businessGroup);
		for(GoToMeeting training:trainings) {
			delete(training);
		}
	}
	
	/**
	 * curl -X POST "https://api.getgo.com/oauth/v2/token" \
	 *  -H "Authorization: Basic {Base64 Encoded consumerKey and consumerSecret}" \
	 *  -H "Accept:application/json" \
	 *  -H "Content-Type: application/x-www-form-urlencoded" \
	 *  -d "grant_type=refresh_token&refresh_token={refresh_token}"
	 * @param organizer
	 */
	@Override
	public boolean refreshToken(GoToOrganizer organizer) {
		GoToOrganizer reloadedOrganizer = organizerDao.loadOrganizerForUpdate(organizer);
		boolean success = false;
		try {
			HttpPost post = new HttpPost(tokenUrl);
			post.addHeader("Accept", "application/json");
			
			String authVal = goToMeetingModule.getTrainingConsumerKey() + ":" + goToMeetingModule.getTrainingConsumerSecret();
        	post.addHeader("Authorization", "Basic " + StringHelper.encodeBase64(authVal)); //NOSONAR no other choice
			post.addHeader("Content-Type", "application/x-www-form-urlencoded");

			List<NameValuePair> urlParameters = new ArrayList<>(4);
			urlParameters.add(new BasicNameValuePair("grant_type", "refresh_token"));
			urlParameters.add(new BasicNameValuePair("refresh_token", reloadedOrganizer.getRefreshToken()));
			post.setEntity(new UrlEncodedFormEntity(urlParameters));
			
			GoToResponse response = execute(post);
			if(response.status() < 400) {
				GoToOrganizerG2T org = GoToJsonUtil.parseToken(response.content());
				organizerDao.updateOrganizer(reloadedOrganizer, org.getAccessToken(), org.getRefreshToken(), org.getExpiresIn());
				success = true;
			} else {
				GoToError error = new GoToError();
				logGoToError(tokenUrl, response, error);
			}
		} catch(Exception e) {
			log.error("", e);
		} finally {
			dbInstance.commit();
		}
		return success;
	}
	
	/**
	 * curl -X POST \
	 *  'https://api.getgo.com/oauth/v2/token' \
	 *  -H 'Authorization: Basic {Base64 Encoded client_id and client_secret}' \
	 *  -H 'Content-Type: application/x-www-form-urlencoded' \
	 *  -d 'grant_type=password&username={username}&password={password}'
	 * @param username
	 * @param password
	 * @return
	 */
	public GoToOrganizerG2T login(String username, String password, GoToError error) {
		GoToOrganizerG2T organizer = null;
		try {
			HttpPost post = new HttpPost(tokenUrl);
			post.addHeader("Accept", "application/json");
			
			String authVal = goToMeetingModule.getTrainingConsumerKey() + ":" + goToMeetingModule.getTrainingConsumerSecret();
        	post.addHeader("Authorization", "Basic " + StringHelper.encodeBase64(authVal)); //NOSONAR no other choice
			post.addHeader("Content-Type", "application/x-www-form-urlencoded");

			List<NameValuePair> urlParameters = new ArrayList<>(4);
			urlParameters.add(new BasicNameValuePair("grant_type", "password"));
			urlParameters.add(new BasicNameValuePair("username", username));
			urlParameters.add(new BasicNameValuePair("password", password));
			post.setEntity(new UrlEncodedFormEntity(urlParameters));
			
			GoToResponse response = execute(post);
			if(response.status() < 400) {
				organizer = GoToJsonUtil.parseToken(response.content());
			} else {
				logGoToError(tokenUrl, response, error);
			}
		} catch(Exception e) {
			log.error("", e);
		} finally {
			dbInstance.commit();
		}
		return organizer;
	}

	@Override
	public List<GoToOrganizer> getOrganizers() {
		return organizerDao.getOrganizers();
	}
	
	@Override
	public List<GoToOrganizer> getOrganizers(String accountKey, String organizerKey) {
		return organizerDao.getOrganizers(accountKey, organizerKey);
	}

	@Override
	public List<GoToOrganizer> getSystemOrganizers() {
		return organizerDao.getOrganizers();
	}
	
	private void decorateWithAccessToken(HttpUriRequest request, GoToOrganizer organizer) {
		String accessToken = getAccessToken(organizer);
		request.addHeader("Accept", "application/json");
		request.addHeader("Authorization", "OAuth oauth_token=" + accessToken);
		request.addHeader("Content-type", "application/json");
	}
	
	private String getAccessToken(GoToOrganizer organizer) {
		if(needRefresh(organizer)) {
			refreshToken(organizer);
			organizer = organizerDao.loadOrganizerByKey(organizer.getKey());
		}
		return organizer.getAccessToken();
	}
	
	private boolean needRefresh(GoToOrganizer organizer) {
		Date renewDate = organizer.getRenewDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(renewDate);
		cal.add(Calendar.MINUTE, -5);
		return new Date().after(cal.getTime());
	}
	
	private GoToResponse execute(HttpUriRequest request) {
		try(CloseableHttpClient httpClient = httpClientService.createHttpClient();
				CloseableHttpResponse response = httpClient.execute(request)) {
			int status = response.getStatusLine().getStatusCode();
			HttpEntity entity = response.getEntity();
			String content;
			if(entity == null) {
				content = "";
			} else {
				content = EntityUtils.toString(entity);
			}
			return new GoToResponse(status, content);
		} catch(IOException e) {
			log.error("", e);
			return new GoToResponse(500, null);
		}
	}
	
	private static class GoToResponse {
		
		private final int status;
		private final String content;
		
		public GoToResponse(int status, String content) {
			this.status = status;
			this.content = content;
		}
		
		public int status() {
			return status;
		}
		
		public String content() {
			return content;
		}
		
	}
}
