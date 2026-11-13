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
package org.olat.restapi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.course.nodes.gta.rule.AssignTaskRuleSPI;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.reminder.manager.ReminderDAO;
import org.olat.modules.reminder.manager.ReminderRuleEngine;
import org.olat.modules.reminder.manager.ReminderRulesXStream;
import org.olat.modules.reminder.model.ReminderImpl;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.model.ReminderRules;
import org.olat.modules.reminder.restapi.ReminderRuleVO;
import org.olat.modules.reminder.restapi.ReminderVO;
import org.olat.modules.reminder.rule.DateRuleSPI;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.JunitTestHelper.IdentityWithLogin;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * Initial date: 25 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RemindersWebServiceTest extends OlatRestTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(RemindersWebServiceTest.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private ReminderDAO reminderDao;
	@Autowired
	private DateRuleSPI dateRuleSpi;
	@Autowired
	private ReminderService reminderService;
	
	@Test
	public void getReminders_repo()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-rem-1");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Reminder transientReminder = reminderService.createReminder(entry, creator);
		transientReminder.setDescription("Hello");
		Reminder reminder = reminderService.save(transientReminder);
		
		dbInstance.commitAndCloseSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString()).path("reminders").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		List<ReminderVO> reminderVoes = parseReminderArray(response.getEntity());
		Assert.assertNotNull(reminderVoes);
		Assert.assertEquals(1, reminderVoes.size());
		ReminderVO reminderVo = reminderVoes.get(0);
		Assert.assertNotNull(reminderVo);
		Assert.assertEquals(reminder.getKey(), reminderVo.getKey());
	}
	
	@Test
	public void getReminders_course()
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-rem-1");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(creator);
		Reminder transientReminder = reminderService.createReminder(courseEntry, creator);
		transientReminder.setDescription("Hello");
		Reminder reminder = reminderService.save(transientReminder);
		
		dbInstance.commitAndCloseSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(courseEntry.getOlatResource().getResourceableId().toString()).path("reminders").build();
		HttpGet method = conn.createGet(request, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		List<ReminderVO> reminderVoes = parseReminderArray(response.getEntity());
		Assert.assertNotNull(reminderVoes);
		Assert.assertEquals(1, reminderVoes.size());
		ReminderVO reminderVo = reminderVoes.get(0);
		Assert.assertNotNull(reminderVo);
		Assert.assertEquals(reminder.getKey(), reminderVo.getKey());
	}
	
	@Test
	public void putNewReminder()
	throws IOException, URISyntaxException {
		IdentityWithLogin creator = JunitTestHelper.createAndPersistRndAdmin("rest-rem-1");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(creator);
		
		ReminderVO reminderVo = new ReminderVO();
		reminderVo.setDescription("Hello, I'm a reminder");
		reminderVo.setEmailSubject("Remind me");
		reminderVo.setEmailBody("<p>To remind you</p>");
		
		List<ReminderRuleVO> rulesVo = new ArrayList<>();
		reminderVo.setRules(rulesVo);
		ReminderRuleVO ruleVo = new ReminderRuleVO();
		ruleVo.setType("DateRuleSPI");
		ruleVo.setOperator(">");
		ruleVo.setRightOperand("2015-05-13T00:00:00");
		rulesVo.add(ruleVo);
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString()).path("reminders").build();
		HttpPut method = conn.createPut(request, MediaType.APPLICATION_JSON, true);
		conn.addJsonEntity(method, reminderVo);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		// check the return value
		ReminderVO savedReminderVo = conn.parse(response, ReminderVO.class);
		Assert.assertNotNull(savedReminderVo);
		Assert.assertNotNull(savedReminderVo.getKey());
		Assert.assertEquals(entry.getKey(), savedReminderVo.getRepoEntryKey());
		Assert.assertEquals("Hello, I'm a reminder", savedReminderVo.getDescription());
		Assert.assertEquals("Remind me", savedReminderVo.getEmailSubject());
		Assert.assertEquals("<p>To remind you</p>", savedReminderVo.getEmailBody());
		Assert.assertEquals(1, savedReminderVo.getRules().size());
		
		// check the reminder on the database
		List<Reminder> reminders = reminderService.getReminders(entry);
		Assert.assertNotNull(reminders);
		Assert.assertEquals(1, reminders.size());
		Reminder reminder = reminders.get(0);
		Assert.assertEquals(savedReminderVo.getKey(), reminder.getKey());
		Assert.assertEquals("Hello, I'm a reminder", reminder.getDescription());
		Assert.assertEquals("Remind me", reminder.getEmailSubject());
		Assert.assertEquals("<p>To remind you</p>", reminder.getEmailBody());
		Assert.assertEquals(creator.getIdentity(), ((ReminderImpl)reminder).getCreator());
		
		// check rule configuration
		String configuration = reminder.getConfiguration();
		ReminderRules rules = reminderService.toRules(configuration);
		List<ReminderRule> ruleList = rules.getRules();
		Assert.assertEquals(1, ruleList.size());
		ReminderRule rule = ruleList.get(0);
		Assert.assertEquals(ReminderRuleEngine.DATE_RULE_TYPE, rule.getType());
		
		// check if the rule works
		boolean ok = dateRuleSpi.evaluate(rule);
		Assert.assertTrue(ok);
	}
	
	@Test
	public void postReminder()
	throws IOException, URISyntaxException {
		IdentityWithLogin creator = JunitTestHelper.createAndPersistRndAdmin("rest-rem-3");
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		
		Reminder reminder = reminderService.createReminder(entry, creator.getIdentity());
		reminder.setDescription("Hello, I'm a reminder");
		reminder.setEmailSubject("Remind me");
		reminder.setEmailBody("<p>To remind you</p>");
		
		ReminderRules rules = new ReminderRules();
		ReminderRuleImpl rule = new ReminderRuleImpl();
		rule.setType("DateRuleSPI");
		rule.setOperator("<");
		rule.setRightOperand("2015-05-13T00:00:00");
		rules.getRules().add(rule);
		String configuration = ReminderRulesXStream.toXML(rules);
		reminder.setConfiguration(configuration);
		reminder = reminderService.save(reminder);
		
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection(creator);
		
		ReminderVO reminderVo = new ReminderVO();
		reminderVo.setKey(reminder.getKey());
		reminderVo.setRepoEntryKey(entry.getKey());
		reminderVo.setDescription("I forgot");
		reminderVo.setEmailSubject("I forgot the subject");
		reminderVo.setEmailBody("<p>I forgot the body</p>");
		
		List<ReminderRuleVO> rulesVo = new ArrayList<>();
		reminderVo.setRules(rulesVo);
		ReminderRuleVO rule1Vo = new ReminderRuleVO();
		rule1Vo.setType("AssignTaskRuleSPI");
		rule1Vo.setLeftOperand("937539759");
		rule1Vo.setOperator("<");
		rule1Vo.setRightOperand("1");
		rule1Vo.setRightUnit("day");
		rulesVo.add(rule1Vo);
		
		ReminderRuleVO rule2Vo = new ReminderRuleVO();
		rule2Vo.setType("DateRuleSPI");
		rule2Vo.setOperator(">");
		rule2Vo.setRightOperand("2019-05-15T00:00:00");
		rulesVo.add(rule2Vo);
		
		reminderVo.setRules(rulesVo);
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(entry.getKey().toString()).path("reminders").build();
		HttpPost method = conn.createPost(request, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, reminderVo);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		// check the return value
		ReminderVO savedReminderVo = conn.parse(response, ReminderVO.class);
		Assert.assertNotNull(savedReminderVo);
		Assert.assertNotNull(savedReminderVo.getKey());
		Assert.assertEquals(entry.getKey(), savedReminderVo.getRepoEntryKey());
		Assert.assertEquals("I forgot", savedReminderVo.getDescription());
		Assert.assertEquals("I forgot the subject", savedReminderVo.getEmailSubject());
		Assert.assertEquals("<p>I forgot the body</p>", savedReminderVo.getEmailBody());
		Assert.assertEquals(2, savedReminderVo.getRules().size());
		
		// check the reminder on the database
		List<Reminder> reminders = reminderService.getReminders(entry);
		Assert.assertNotNull(reminders);
		Assert.assertEquals(1, reminders.size());
		Reminder updatedReminder = reminders.get(0);
		Assert.assertEquals(savedReminderVo.getKey(), updatedReminder.getKey());
		Assert.assertEquals("I forgot", updatedReminder.getDescription());
		Assert.assertEquals("I forgot the subject", updatedReminder.getEmailSubject());
		Assert.assertEquals("<p>I forgot the body</p>", updatedReminder.getEmailBody());
		
		// check rule configuration
		String updatedConfiguration = updatedReminder.getConfiguration();
		ReminderRules updatedRules = reminderService.toRules(updatedConfiguration);
		List<ReminderRule> updatedRuleList = updatedRules.getRules();
		Assert.assertEquals(2, updatedRuleList.size());
		ReminderRuleImpl updatedRule1 = (ReminderRuleImpl)updatedRuleList.get(0);
		Assert.assertEquals(AssignTaskRuleSPI.class.getSimpleName(), updatedRule1.getType());
		Assert.assertEquals("937539759", updatedRule1.getLeftOperand());
		Assert.assertEquals("<", updatedRule1.getOperator());
		Assert.assertEquals("1", updatedRule1.getRightOperand());
		Assert.assertEquals("day", updatedRule1.getRightUnit());
		
		ReminderRuleImpl updatedRule2 = (ReminderRuleImpl)updatedRuleList.get(1);
		Assert.assertEquals(DateRuleSPI.class.getSimpleName(), updatedRule2.getType());
		Assert.assertEquals(">", updatedRule2.getOperator());
		Assert.assertEquals("2019-05-15T00:00:00", updatedRule2.getRightOperand());	
	}
	
	@Test
	public void deleteReminder() 
	throws IOException, URISyntaxException {
		RestConnection conn = new RestConnection("administrator", "openolat");
		
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-rem-1");
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("rest-rem-1");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(creator);
		Reminder transientReminder = reminderService.createReminder(courseEntry, creator);
		transientReminder.setDescription("Hello");
		Reminder reminder = reminderService.save(transientReminder);

		//mark as sent
		reminderDao.markAsSend(reminder, id, "ok", 1);
		dbInstance.commitAndCloseSession();
		
		URI request = UriBuilder.fromUri(getContextURI()).path("repo").path("entries")
				.path(courseEntry.getKey().toString()).path("reminders")
				.path(reminder.getKey().toString()).build();
		HttpDelete method = conn.createDelete(request, MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());

		Reminder deletedReminder = reminderService.loadByKey(reminder.getKey());
		Assert.assertNull(deletedReminder);
	}
	
	
	/**
	 * An owner of course B cannot update a reminder of course A through
	 * course B.
	 */
	@Test
	public void postReminderOfOtherCourse()
	throws IOException, URISyntaxException {
		IdentityWithLogin ownerA = JunitTestHelper.createAndPersistRndAuthor("rest-rem-10");
		IdentityWithLogin ownerB = JunitTestHelper.createAndPersistRndAuthor("rest-rem-11");
		RepositoryEntry courseEntryA = JunitTestHelper.deployBasicCourse(ownerA.getIdentity());
		RepositoryEntry courseEntryB = JunitTestHelper.deployBasicCourse(ownerB.getIdentity());
		Reminder reminderA = createReminder(courseEntryA, ownerA.getIdentity(), "Reminder of A");
		dbInstance.commitAndCloseSession();
		
		ReminderVO reminderVo = new ReminderVO();
		reminderVo.setKey(reminderA.getKey());
		reminderVo.setDescription("Hacked");
		reminderVo.setEmailSubject("Hacked subject");
		reminderVo.setEmailBody("<p>Hacked body</p>");
		
		RestConnection conn = new RestConnection(ownerB);
		HttpPost method = conn.createPost(remindersUri(courseEntryB), MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, reminderVo);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(404, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		Reminder unchangedReminder = reminderService.loadByKey(reminderA.getKey());
		Assert.assertEquals(courseEntryA, unchangedReminder.getEntry());
		Assert.assertEquals("Reminder of A", unchangedReminder.getDescription());
		Assert.assertEquals("Subject of A", unchangedReminder.getEmailSubject());
	}
	
	/**
	 * Update a reminder with a key which doesn't exist.
	 */
	@Test
	public void postReminderUnknownKey()
	throws IOException, URISyntaxException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-rem-12");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		dbInstance.commitAndCloseSession();
		
		ReminderVO reminderVo = new ReminderVO();
		reminderVo.setKey(Long.valueOf(-25l));
		reminderVo.setDescription("Unknown");
		
		RestConnection conn = new RestConnection(owner);
		HttpPost method = conn.createPost(remindersUri(courseEntry), MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, reminderVo);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(404, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	}
	
	/**
	 * An owner can update a reminder of its own course.
	 */
	@Test
	public void postOwnReminderAsOwner()
	throws IOException, URISyntaxException {
		IdentityWithLogin owner = JunitTestHelper.createAndPersistRndAuthor("rest-rem-13");
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(owner.getIdentity());
		Reminder reminder = createReminder(courseEntry, owner.getIdentity(), "My reminder");
		dbInstance.commitAndCloseSession();
		
		ReminderVO reminderVo = new ReminderVO();
		reminderVo.setKey(reminder.getKey());
		reminderVo.setDescription("My updated reminder");
		reminderVo.setEmailSubject("My updated subject");
		reminderVo.setEmailBody("<p>My updated body</p>");
		
		RestConnection conn = new RestConnection(owner);
		HttpPost method = conn.createPost(remindersUri(courseEntry), MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, reminderVo);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		ReminderVO savedReminderVo = conn.parse(response, ReminderVO.class);
		Assert.assertEquals(reminder.getKey(), savedReminderVo.getKey());
		
		Reminder updatedReminder = reminderService.loadByKey(reminder.getKey());
		Assert.assertEquals("My updated reminder", updatedReminder.getDescription());
		Assert.assertEquals("My updated subject", updatedReminder.getEmailSubject());
	}
	
	/**
	 * An owner of course B cannot delete a reminder of course A through
	 * course B.
	 */
	@Test
	public void deleteReminderOfOtherCourse()
	throws IOException, URISyntaxException {
		IdentityWithLogin ownerA = JunitTestHelper.createAndPersistRndAuthor("rest-rem-14");
		IdentityWithLogin ownerB = JunitTestHelper.createAndPersistRndAuthor("rest-rem-15");
		RepositoryEntry courseEntryA = JunitTestHelper.deployBasicCourse(ownerA.getIdentity());
		RepositoryEntry courseEntryB = JunitTestHelper.deployBasicCourse(ownerB.getIdentity());
		Reminder reminderA = createReminder(courseEntryA, ownerA.getIdentity(), "Reminder of A");
		dbInstance.commitAndCloseSession();
		
		URI request = UriBuilder.fromUri(remindersUri(courseEntryB))
				.path(reminderA.getKey().toString()).build();
		RestConnection conn = new RestConnection(ownerB);
		HttpResponse response = conn.execute(conn.createDelete(request, MediaType.APPLICATION_JSON));
		Assert.assertEquals(404, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
		
		Reminder stillThereReminder = reminderService.loadByKey(reminderA.getKey());
		Assert.assertNotNull(stillThereReminder);
	}
	
	private URI remindersUri(RepositoryEntry courseEntry) {
		return UriBuilder.fromUri(getContextURI()).path("repo").path("courses")
				.path(courseEntry.getOlatResource().getResourceableId().toString()).path("reminders").build();
	}
	
	private Reminder createReminder(RepositoryEntry entry, Identity creator, String description) {
		Reminder reminder = reminderService.createReminder(entry, creator);
		reminder.setDescription(description);
		reminder.setEmailSubject("Subject of A");
		reminder.setEmailBody("<p>Body</p>");
		return reminderService.save(reminder);
	}
	
	private List<ReminderVO> parseReminderArray(HttpEntity entity) {
		try(InputStream content=entity.getContent()) {
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			return mapper.readValue(content, new TypeReference<List<ReminderVO>>(){/* */});
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
}
