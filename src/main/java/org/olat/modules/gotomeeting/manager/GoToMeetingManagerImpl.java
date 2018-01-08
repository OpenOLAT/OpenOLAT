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
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
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
	
	private static final OLog log = Tracing.createLoggerFor(GoToMeetingManagerImpl.class);
	
	private String directLoginUrl = "https://api.getgo.com/oauth/access_token";
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


	@Override
	public List<GoToOrganizer> getOrganizersFor(Identity identity) {
		return organizerDao.getOrganizersFor(identity);
	}

	@Override
	public boolean addOrganizer(String name, String username, String password, Identity owner, GoToError error) {
		GoToOrganizerG2T organizerVo = directLogin(username, password, error);
		if(organizerVo != null) {
			GoToOrganizer organizer = organizerDao.loadOrganizerByUsername(username);
			if(organizer == null) {
				organizerDao.createOrganizer(name, username, organizerVo.getAccessToken(), organizerVo.getOrganizerKey(),
						organizerVo.getFirstName(), organizerVo.getLastName(), organizerVo.getEmail(),
						organizerVo.getAccountKey(), organizerVo.getExpiresIn(), owner);
			} else {
				organizerDao.updateOrganizer(organizer, name, organizerVo.getAccessToken(), organizerVo.getOrganizerKey(),
						organizerVo.getFirstName(), organizerVo.getLastName(), organizerVo.getEmail(),
						organizerVo.getAccountKey(), organizerVo.getExpiresIn());
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean removeOrganizer(GoToOrganizer organizer) {
		if(meetingDao.countMeetingsOrganizedBy(organizer) == 0) {
			organizerDao.deleteOrganizer(organizer);
			return true;
		}
		return false;
	}
	
	public boolean checkOrganizerAvailability(GoToOrganizer organizer, Date start, Date end) {
		List<GoToMeeting> meetings = meetingDao.getMeetingsOverlap(GoToType.training, organizer, start, end);
		return meetings.isEmpty();
	}

	@Override
	public GoToMeeting scheduleTraining(GoToOrganizer organizer, String name, String externalId, String description, Date start, Date end,
			RepositoryEntry resourceOwner, String subIdentifier, BusinessGroup businessGroup, GoToError error) {
		//GoToMeeting scheduledMeeting = meetingDao.createTraining(name, externalId, description, UUID.randomUUID().toString(), start, end, organizer, resourceOwner, subIdentifier, businessGroup);
		GoToMeeting scheduledMeeting = null;
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			String url = gotoTrainingUrl + "/organizers/" + organizer.getOrganizerKey() + "/trainings";
			HttpPost post = new HttpPost(url);
			post.addHeader("Accept", "application/json");
			post.addHeader("Authorization", "OAuth oauth_token=" + organizer.getAccessToken());
			post.addHeader("Content-type", "application/json");
			
			String timeZoneId = goToMeetingModule.getGoToTimeZoneId();
			JSONObject trainingJson = GoToJsonUtil
					.training(Long.parseLong(organizer.getOrganizerKey()), name, description, timeZoneId, start, end);
			String objectStr = trainingJson.toString();
			post.setEntity(new StringEntity(objectStr, ContentType.APPLICATION_JSON));
			
			HttpResponse response = httpClient.execute(post);
			int status = response.getStatusLine().getStatusCode();
			if(status == 201) {//created
				String trainingKey = EntityUtils.toString(response.getEntity());
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
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			GoToOrganizer organizer = meeting.getOrganizer();
			String url = gotoTrainingUrl + "/organizers/" + organizer.getOrganizerKey() + "/trainings/" + meeting.getMeetingKey() + "/times";

			HttpPut put = new HttpPut(url);
			put.addHeader("Accept", "application/json");
			put.addHeader("Authorization", "OAuth oauth_token=" + organizer.getAccessToken());
			put.addHeader("Content-type", "application/json");

			String payload = GoToJsonUtil.trainingTimes(goToMeetingModule.getGoToTimeZoneId(), start, end).toString();
			put.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
			
			HttpResponse response = httpClient.execute(put);
			int status = response.getStatusLine().getStatusCode();
			if(status == 200) {//created
				EntityUtils.consume(response.getEntity());
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
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			GoToOrganizer organizer = meeting.getOrganizer();
			String url = gotoTrainingUrl + "/organizers/" + organizer.getOrganizerKey() + "/trainings/" + meeting.getMeetingKey() + "/nameDescription";

			HttpPut put = new HttpPut(url);
			put.addHeader("Accept", "application/json");
			put.addHeader("Authorization", "OAuth oauth_token=" + organizer.getAccessToken());
			put.addHeader("Content-type", "application/json");
			
			if(!StringHelper.containsNonWhitespace(description) || "-".equals(description)) {
				description = training.getDescription();
			}
			
			String payload = GoToJsonUtil.trainingNameDescription(name, description).toString();
			put.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));
			
			HttpResponse response = httpClient.execute(put);
			int status = response.getStatusLine().getStatusCode();
			if(status == 204) {//created
				EntityUtils.consume(response.getEntity());
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
			try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
				GoToOrganizer organizer = meeting.getOrganizer();
				String url = gotoTrainingUrl + "/organizers/" + organizer.getOrganizerKey() + "/trainings/" + meeting.getMeetingKey() + "/registrants";

				HttpPost post = new HttpPost(url);
				post.addHeader("Accept", "application/json");
				post.addHeader("Authorization", "OAuth oauth_token=" + organizer.getAccessToken());
				post.addHeader("Content-type", "application/json");
				
				String traineeJson = GoToJsonUtil.registrant(trainee).toString();
				post.setEntity(new StringEntity(traineeJson, ContentType.APPLICATION_JSON));
				
				HttpResponse response = httpClient.execute(post);
				int status = response.getStatusLine().getStatusCode();
				if(status == 201) {//created
					String content = EntityUtils.toString(response.getEntity());
					GoToRegistrantG2T registrantVo = GoToJsonUtil.parseAddRegistrant(content);
					registrant = registrantDao.createRegistrant(meeting, trainee, registrantVo.getRegistrantKey(), registrantVo.getJoinUrl(), registrantVo.getConfirmationUrl());
				} else if(status == 409) {
					String content = EntityUtils.toString(response.getEntity());
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
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			GoToOrganizer organizer = meeting.getOrganizer();
			String url = gotoTrainingUrl + "/organizers/" + organizer.getOrganizerKey() + "/trainings/" + meeting.getMeetingKey() + "/registrants/" + registrantKey;

			HttpGet get = new HttpGet(url);
			get.addHeader("Accept", "application/json");
			get.addHeader("Authorization", "OAuth oauth_token=" + organizer.getAccessToken());
			get.addHeader("Content-type", "application/json");

			HttpResponse response = httpClient.execute(get);
			int status = response.getStatusLine().getStatusCode();
			if(status == 200) {
				String content = EntityUtils.toString(response.getEntity());
				GoToRegistrantG2T registrantVo = GoToJsonUtil.parseAddRegistrant(content);
				return registrantVo;
			} else {
				logGoToError("getRegistrant", response, error);
				return null;
			}
		} catch(Exception e) {
			log.error("", e);
			return null;
		}
	}

	
	private void logGoToError(String method, HttpResponse response, GoToError error)
	throws IOException {
		int status = response.getStatusLine().getStatusCode();
		error.setErrorCode(status);
		String responseString = EntityUtils.toString(response.getEntity());
		try {
			GoToErrorG2T errorVo = GoToJsonUtil.parseError(responseString);
			if(errorVo != null) {
				error.setError(errorVo.getErrorCode());
				error.setDescription(errorVo.getDescription());
			}
		} catch (Exception e) {
			log.error("", e);
		} finally {
			EntityUtils.consumeQuietly(response.getEntity());
			log.error(method + " return " + status + ": " + responseString);
		}
	}
	
	private void logGoToError(String method, int status, String responseString, GoToError error) {
		error.setErrorCode(status);
		log.error(method + " return " + status + ": " + responseString);
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
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			GoToOrganizer organizer = meeting.getOrganizer();
			String url = gotoTrainingUrl + "/organizers/" + organizer.getOrganizerKey() + "/trainings/" + meeting.getMeetingKey();

			HttpGet get = new HttpGet(url);
			get.addHeader("Accept", "application/json");
			get.addHeader("Authorization", "OAuth oauth_token=" + organizer.getAccessToken());
			get.addHeader("Content-type", "application/json");

			HttpResponse response = httpClient.execute(get);
			int status = response.getStatusLine().getStatusCode();
			if(status == 200) {//deleted
				String content = EntityUtils.toString(response.getEntity());
				GoToTrainingG2T trainingVo = GoToJsonUtil.parseTraining(content);
				return trainingVo;
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
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			GoToOrganizer organizer = meeting.getOrganizer();
			String url = gotoTrainingUrl + "/trainings/" + meeting.getMeetingKey() + "/start";

			HttpGet get = new HttpGet(url);
			get.addHeader("Accept", "application/json");
			get.addHeader("Authorization", "OAuth oauth_token=" + organizer.getAccessToken());
			get.addHeader("Content-type", "application/json");

			HttpResponse response = httpClient.execute(get);
			int status = response.getStatusLine().getStatusCode();
			if(status == 200) {//deleted
				String content = EntityUtils.toString(response.getEntity());
				String startUrl = GoToJsonUtil.parseHostUrl(content);
				return startUrl;
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
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			GoToOrganizer organizer = meeting.getOrganizer();
			String url = gotoTrainingUrl + "/trainings/" + meeting.getMeetingKey() + "/recordings";

			HttpGet get = new HttpGet(url);
			get.addHeader("Accept", "application/json");
			get.addHeader("Authorization", "OAuth oauth_token=" + organizer.getAccessToken());
			get.addHeader("Content-type", "application/json");

			HttpResponse response = httpClient.execute(get);
			int status = response.getStatusLine().getStatusCode();
			if(status == 200) {//deleted
				String content = EntityUtils.toString(response.getEntity());
				List<GoToRecordingsG2T> recordings = GoToJsonUtil.parseRecordings(content);
				return recordings;
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
		if(deleteTraining(meeting, error)) {
			meetingDao.delete(meeting);
			return true;
		} else if(error.getError() == GoToErrors.NoSuchTraining
				|| error.getError() == GoToErrors.InvalidRequest) {
			//clean up our database
			meetingDao.delete(meeting);
			return true;
		} else {
			//do nothing
			return false;
		}	
	}
	
	private boolean deleteTraining(GoToMeeting meeting, GoToError error) {
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			GoToOrganizer organizer = meeting.getOrganizer();
			String url = gotoTrainingUrl + "/organizers/" + organizer.getOrganizerKey() + "/trainings/" + meeting.getMeetingKey();

			HttpDelete delete = new HttpDelete(url);
			delete.addHeader("Accept", "application/json");
			delete.addHeader("Authorization", "OAuth oauth_token=" + organizer.getAccessToken());
			delete.addHeader("Content-type", "application/json");

			HttpResponse response = httpClient.execute(delete);
			int status = response.getStatusLine().getStatusCode();
			if(status == 204) {//deleted
				return true;
			} else if (status == 404 || status == 400) {
				String content = EntityUtils.toString(response.getEntity());
				GoToErrorG2T errorVo = GoToJsonUtil.parseError(content);
				if(errorVo.getErrorCode() == GoToErrors.NoSuchTraining
						|| errorVo.getErrorCode() == GoToErrors.InvalidRequest) {
					error.setError(errorVo.getErrorCode());
					error.setDescription(errorVo.getDescription());
				} else {
					log.error("deleteTraining return " + status + ": " + content);
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
	 * curl -X POST -H "Accept:application/json"
	 *   -H "Content-Type: application/x-www-form-urlencoded" "https://api.citrixonline.com/oauth/access_token"
	 *   -d 'grant_type=password&user_id=test@test.com&password=xyz&client_id={consumerKey}'
	 *   
	 *   
	 * {
	 *  "access_token":"RlUe11faKeyCWxZToK3nk0uTKAL",
	 *  "expires_in":"30758399",
	 *  "refresh_token":"d1cp20yB3hrFAKeTokenTr49EZ34kTvNK",
	 *  "organizer_key":"8439885694023999999",
	 *  "account_key":"9999982253621659654",
	 *  "account_type":"",
	 *  "firstName":"Mahar",
	 *  "lastName":"Singh",
	 *  "email":"mahar.singh@singhSong.com",
	 *  "platform":"GLOBAL",
	 *  "version":"2",
	 * }
	 */
	private GoToOrganizerG2T directLogin(String username, String password, GoToError error) {
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
			String consumerKey = goToMeetingModule.getTrainingConsumerKey();
			
			HttpPost post = new HttpPost(directLoginUrl);
			post.addHeader("Accept", "application/json");

			List<NameValuePair> urlParameters = new ArrayList<>(4);
			urlParameters.add(new BasicNameValuePair("grant_type", "password"));
			urlParameters.add(new BasicNameValuePair("user_id", username));
			urlParameters.add(new BasicNameValuePair("password", password));
			urlParameters.add(new BasicNameValuePair("client_id", consumerKey));
			post.setEntity(new UrlEncodedFormEntity(urlParameters));

			HttpResponse response = httpClient.execute(post);
			int status = response.getStatusLine().getStatusCode();
			if(status < 400) {
				String content = EntityUtils.toString(response.getEntity());
				GoToOrganizerG2T organizerVo = GoToJsonUtil.parseDirectLogin(content);
				return organizerVo;
			} else {
				error.setErrorCode(status);
				String responseString = EntityUtils.toString(response.getEntity());
				EntityUtils.consumeQuietly(response.getEntity());
				log.error("directLogin return " + status + ": " + responseString);
			}
			return null;
		} catch(Exception e) {
			log.error("", e);
			return null;
		}
	}

	@Override
	public List<GoToOrganizer> getOrganizers() {
		return organizerDao.getOrganizers();
	}

	@Override
	public List<GoToOrganizer> getSystemOrganizers() {
		return organizerDao.getOrganizers();
	}

}
