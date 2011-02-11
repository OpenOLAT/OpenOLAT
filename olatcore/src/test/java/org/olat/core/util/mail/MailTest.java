/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 * Description:<br>
 * jUnit tests for the mail package
 * <P>
 * Initial Date: 21.11.2006 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH<br>
 *         http://www.frentix.com
 */

package org.olat.core.util.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.test.OlatcoreTestCaseWithMocking;
	
public class MailTest extends OlatcoreTestCaseWithMocking {
	private static Logger log = Logger.getLogger(MailTest.class);
	private Identity id1, id2, id3, id4, id5, id6;

	// for local debugging you can set a systemproperty to a maildomain where
	// you immediately get the mails. If the property is not set the
	// mytrashmail domain is used. You can get the mails there with your
	// bowser
	static String maildomain = System.getProperty("junit.maildomain");
	static {
		if (maildomain == null) {
			maildomain = "thankyou2010.com";
		}
	}

	/**
	 * SetUp is called before each test.
	 */
	@Before
	public void setup() {
		id1 = createIdentity("one");
		id2 = createIdentity("two");
		id3 = createIdentity("three");
		id4 = createIdentity("four");
		id5 = createIdentity("five");
		id6 = createIdentity("six");
	}

	private Identity createIdentity(String login) {
		User testUser = new TestUser(login + "olattest@" + maildomain, login + "first", login + "last");
		Identity id = new TestIdentity(login, testUser);
		return id;
	}


	/**
	 * Simple helper to test valid email addresses
	 * @param mailAddress
	 */
	private void isValid(String mailAddress) {
		assertTrue(MailHelper.isValidEmailAddress(mailAddress));
	}
	/**
	 * Simple helper to test invalid email addresses
	 * @param mailAddress
	 */
	private void isInvalid(String mailAddress) {
		assertFalse(MailHelper.isValidEmailAddress(mailAddress));
	}

	/**
	 * Test the email addres validator
	 */
	@Test public void testValidEmailAddresses() {
		// valid addresses
		isValid("gnaegi@frentix.com");
		isValid("login@w.pl");
		// invalid addresses
		isInvalid(null);
		isInvalid("");
		isInvalid("gnägi@frentix.com");
		isInvalid("gnaegi @ frentix.com");
		isInvalid("gnaegi@frentix_com");
		isInvalid("gnaegi");
		isInvalid("g@g");
		// valid addresses but disable in OLAT because this is not what we want users to enter as mail addresses
		isInvalid("\"Florian Gnaegi\" <gnaegi@frentix.com>"); 
		isInvalid("someone@[192.168.1.100]"); 
	}

	
	/**
	 * this is more a playground method to understand the evaluate method than a
	 * rela testcase
	 */
	@Test public void testVelocityTemplate() {
		//
		VelocityContext context = new VelocityContext();
		// good case
		StringWriter writer = new StringWriter();
		context.put("foo", "bar");
		String template = "foo $foo";
		MailerResult result = new MailerResult();
		MailerWithTemplate.getInstance().evaluate(context, template, writer, result);
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo bar", writer.toString());

		writer = new StringWriter();
		template = "foo foo";
		MailerWithTemplate.getInstance().evaluate(context, template, writer, result);
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo foo", writer.toString());

		writer = new StringWriter();
		template = "foo $$foo";
		MailerWithTemplate.getInstance().evaluate(context, template, writer, result);
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo $bar", writer.toString());

		writer = new StringWriter();
		template = "foo $ foo";
		MailerWithTemplate.getInstance().evaluate(context, template, writer, result);
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo $ foo", writer.toString());

		writer = new StringWriter();
		template = "foo $ foo";
		MailerWithTemplate.getInstance().evaluate(context, template, writer, result);
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo $ foo", writer.toString());

		writer = new StringWriter();
		template = "foo #foo \n##sdf jubla";
		MailerWithTemplate.getInstance().evaluate(context, template, writer, result);
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo #foo \n", writer.toString());

		writer = new StringWriter();
		template = "foo #if(true)\n#end";
		MailerWithTemplate.getInstance().evaluate(context, template, writer, result);
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo ", writer.toString());

		// illegal templates: unclosed if-else statement
		writer = new StringWriter();
		template = "foo #if";
		MailerWithTemplate.getInstance().evaluate(context, template, writer, result);
		assertEquals(MailerResult.TEMPLATE_PARSE_ERROR, result.getReturnCode());
		assertEquals("", writer.toString());
	}

	/**
	 * Test for the mail template and the context variable methods
	 */
	@Test public void testMailToCcBccForEach() {
		String subject = "For Each Subject: Hello $firstname $lastname";
		String body = "For Each Body: \n\n You ($login) should go to	 \n\n'$coursename' @ $courseURL$login";

		final String coursename = "my course";
		final String courseURL = "http://www.mytrashmail.com/myTrashMail_inbox.aspx?email=";

		MailTemplate template = new MailTemplate(subject, body, null) {
			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity identity) {
				// Put user variables
				User user = identity.getUser();
				context.put("firstname", user.getProperty(UserConstants.FIRSTNAME, null));
				context.put("lastname", user.getProperty(UserConstants.LASTNAME, null));
				context.put("login", identity.getName());
				// Put variables from greater context, eg. course id, group name etc.
				context.put("coursename", coursename);
				context.put("courseURL", courseURL);

			}
		};

		// some recipients data
		List<Identity> recipients = new ArrayList<Identity>();
		recipients.add(id1);
		recipients.add(id2);
		recipients.add(id3);
		List<Identity> recipientsCC = new ArrayList<Identity>();
		recipientsCC.add(id4);
		recipientsCC.add(id5);
		List<Identity> recipientsBCC = new ArrayList<Identity>();
		recipientsBCC.add(id6);

		// tests with / witthout CC and BCC

		MailerResult result = new MailerResult();
		result = MailerWithTemplate.getInstance().sendMailAsSeparateMails(recipients, null, null, template, id6);
		assertEquals(MailerResult.OK, result.getReturnCode());
		result = MailerWithTemplate.getInstance().sendMailAsSeparateMails(recipients, recipientsCC, null, template, id6);
		assertEquals(MailerResult.OK, result.getReturnCode());
		result = MailerWithTemplate.getInstance().sendMailAsSeparateMails(recipients, null, recipientsBCC, template, id6);
		assertEquals(MailerResult.OK, result.getReturnCode());
	}

	/**
	 * Test for the mail template and the context variable methods
	 */
	@Test public void testMailToCcBccTogether() {
		String subject = "Together Subject: Hello everybody";
		String body = "Together Body: \n\n You should go to \n\n'$coursename' @ $courseURL";

		final String coursename = "my course";
		final String courseURL = "http://www.mytrashmail.com/";

		MailTemplate template = new MailTemplate(subject, body, null) {
			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity identity) {
				// identity is null in this mode - template parsed only once not for
				// everybody

				// Put variables from greater context, eg. course id, group name etc.
				context.put("coursename", coursename);
				context.put("courseURL", courseURL);

			}
		};

		// some recipients data
		List<Identity> recipients = new ArrayList<Identity>();
		recipients.add(id1);
		recipients.add(id2);
		recipients.add(id3);
		List<Identity> recipientsCC = new ArrayList<Identity>();
		recipientsCC.add(id4);
		recipientsCC.add(id5);
		List<Identity> recipientsBCC = new ArrayList<Identity>();
		recipientsBCC.add(id6);

		// tests with / witthout CC and BCC

		MailerResult result = new MailerResult();
		result = MailerWithTemplate.getInstance().sendMailAsSeparateMails(recipients, null, null, template, id6);
		assertEquals(MailerResult.OK, result.getReturnCode());
		result = MailerWithTemplate.getInstance().sendMailAsSeparateMails(recipients, recipientsCC, null, template, id6);
		assertEquals(MailerResult.OK, result.getReturnCode());
		result = MailerWithTemplate.getInstance().sendMailAsSeparateMails(recipients, null, recipientsBCC, template, id6);
		assertEquals(MailerResult.OK, result.getReturnCode());
	}

	/**
	 * Test for the mail template and the context variable methods
	 */
	@Test public void testMailAttachments() {
		String subject = "Subject: Hello $firstname with attachment";
		String body = "Body: \n\n Hey $login, here's a file for you: ";

		// some attachemnts
		File[] attachments = new File[1];
		File file1, file2;
		try {
			System.out.println("MailTest.testMailAttachments Url1=" + MailTest.class.getResource("MailTest.class") );
			file1 = new File(MailTest.class.getResource("MailTest.class").toURI());
			attachments[0] = file1;
// TODO: cg Properties file is in olat_core.jar and not be lookup as resource (jar:file:...)
//			System.out.println("MailTest.testMailAttachments Url2=" + MailTest.class.getResource("_i18n/LocalStrings_de.properties") );	
//			file2 = new File(MailTest.class.getResource("_i18n/LocalStrings_de.properties").toURI());
//			attachments[1] = file2;
		} catch (URISyntaxException e) {
			fail("ups, can't get testfiles from local path: MailTest.class and _i18n/LocalStrings_de.properties");
		}

		MailTemplate template = new MailTemplate(subject, body, attachments) {
			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity identity) {
				// Put user variables
				User user = identity.getUser();
				context.put("firstname", user.getProperty(UserConstants.FIRSTNAME, null));
				context.put("login", identity.getName());
			}
		};

		// some recipients data
		List<Identity> recipients = new ArrayList<Identity>();
		recipients.add(id1);

		MailerResult result = new MailerResult();
		result = MailerWithTemplate.getInstance().sendMailAsSeparateMails(recipients, null, null, template, id2);
		assertEquals(MailerResult.OK, result.getReturnCode());
	}

	/**
	 * Test for the mail template and the context variable methods
	 */
	@Test public void testMailAttachmentsInvalid() {
		String subject = "Subject: Hello $firstname with attachment";
		String body = "Body: \n\n Hey $login, here's a file for you: ";

		// some attachemnts - but no file
		File[] attachments = new File[1];

		MailTemplate template = new MailTemplate(subject, body, attachments) {
			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity identity) {
				// Put user variables
				User user = identity.getUser();
				context.put("firstname", user.getProperty(UserConstants.FIRSTNAME, null));
				context.put("login", identity.getName());
			}
		};

		// some recipients data
		List<Identity> recipients = new ArrayList<Identity>();
		recipients.add(id1);

		MailerResult result = new MailerResult();
		result = MailerWithTemplate.getInstance().sendMailAsSeparateMails(recipients, null, null, template, id2);
		assertEquals(MailerResult.ATTACHMENT_INVALID, result.getReturnCode());
	}

	/**
	 * Test for the mailer result codes
	 */
	@Test public void testMailerResult() {
		String subject = "MailerResult Subject: Hello everybody";
		String body = "MailerResult Body: \n\n This is just a test";

		MailTemplate template = new MailTemplate(subject, body, null) {
			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity identity) {
			// nothing to do
			}
		};

		// some recipients data
		Identity illegal1 = createIdentity("illegal1");
		illegal1.getUser().setProperty(UserConstants.EMAIL, "doesnotexisteserlkmlkm@sdf.com");
		Identity illegal2 = createIdentity("illegal2");
		illegal2.getUser().setProperty(UserConstants.EMAIL, "sd@this.domain.does.not.exist.at.all");
		Identity illegal3 = createIdentity("illegal3");
		illegal3.getUser().setProperty(UserConstants.EMAIL, "@ sdf");

		List<Identity> recipients = new ArrayList<Identity>();
		List<Identity> recipientsCC = new ArrayList<Identity>();
		List<Identity> recipientsBCC = new ArrayList<Identity>();

		recipients.add(illegal1);

		// if only one recipient: error must be indicated
		MailerResult result = new MailerResult();
		result = MailerWithTemplate.getInstance().sendMailAsSeparateMails(recipients, null, null, template, id6);
		// mail will bounce back since address does not exist, but sent to local MTA
		// this test is not very good, depends on smtp settings!
		//assertEquals(MailerResult.OK, result.getReturnCode());

		recipients = new ArrayList<Identity>();
		recipients.add(illegal2);
		result = MailerWithTemplate.getInstance().sendMailAsSeparateMails(recipients, null, null, template, id6);
		// mail will bounce back since address does not exist, but sent to local MTA
		assertEquals(MailerResult.OK, result.getReturnCode());

		recipients = new ArrayList<Identity>();
		recipients.add(illegal3);
		result = MailerWithTemplate.getInstance().sendMailAsSeparateMails(recipients, null, null, template, id6);
		assertEquals(MailerResult.RECIPIENT_ADDRESS_ERROR, result.getReturnCode());

		// now with one valid and the invalid recipient: should return ok but have
		// one recipient in the failed list
		recipients.add(id1);
		result = MailerWithTemplate.getInstance().sendMailAsSeparateMails(recipients, null, null, template, id6);
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals(1, result.getFailedIdentites().size());

		// valid recipient but invalid sender
		recipients = new ArrayList<Identity>();
		recipients.add(id1);
		result = MailerWithTemplate.getInstance().sendMailAsSeparateMails(recipients, null, null, template, illegal3);
		assertEquals(MailerResult.SENDER_ADDRESS_ERROR, result.getReturnCode());

		// invalid cc and bcc but valid to, mus count up the invalid accounts
		recipients = new ArrayList<Identity>();
		recipients.add(id1);
		recipients.add(illegal3); // first
		recipientsCC.add(illegal3); // second
		recipientsBCC.add(illegal3); // third
		result = MailerWithTemplate.getInstance().sendMailAsSeparateMails(recipients, recipientsCC, recipientsBCC, template, id6);
		// mail will bounce back since address does not exist, but sent to local MTA
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals(3, result.getFailedIdentites().size());

	}

}

/**
 * Test classes: user and identity
 */
class TestUser implements User {
	private Map<String, String> userProperties = new HashMap<String, String>();
	private Preferences preferences;

	TestUser(String email, String firstname, String lastname) {
		
		setProperty(UserConstants.FIRSTNAME, firstname);
		setProperty(UserConstants.LASTNAME, lastname);
		setProperty(UserConstants.EMAIL, email);
		
		setPreferences(new Preferences() {
			public String getFontsize() {
				return null;
			}

			public String getNotificationInterval() {
				return null;
			}

			public boolean getInformSessionTimeout() {
				return false;
			}

			public String getLanguage() {
				return "en";
			}

			public boolean getPresenceMessagesPublic() {
				return false;
			}

			public void setFontsize(String l) {}

			public void setNotificationInterval(String ni) {}

			public void setInformSessionTimeout(boolean b) {}

			public void setLanguage(String l) {}

			public void setPresenceMessagesPublic(boolean b) {}
		});
	}

	public Long getKey() {
		return null;
	}

	public boolean equalsByPersistableKey(Persistable persistable) {
		return false;
	}

	public Date getLastModified() {
		return null;
	}

	public Date getCreationDate() {
		return null;
	}

	public void setPreferences(Preferences prefs) {
		preferences = prefs;
	}

	public Preferences getPreferences() {
		return preferences;
	}

	public String getProperty(String name, Locale locale) {
		return userProperties.get(name);
	}

	public void setProperty(String name, String value) {
		if (value == null) {
			userProperties.remove(name);
		} else {
			userProperties.put(name, value);
		}
	}
	

	public void setIdentityEnvironmentAttributes(Map<String, String> identEnvAttribs) {
		throw new AssertException("SETTER not yet implemented, not used in tests so far, must be used if IdentityEnvironmentAttributes should be tested");
	}	

	public String getPropertyOrIdentityEnvAttribute(String propertyName, Locale locale) {
		throw new AssertException("GETTER not yet implemented, not used in tests so far, must be used if IdentityEnvironmentAttributes should be tested");
	}

	public int getFieldCount() {
		// TODO Auto-generated method stub
		return 0;
	}

}

class TestIdentity implements Identity {
	String name;
	User user;

	public TestIdentity(String login, User testUser) {
		this.name = login;
		this.user = testUser;
	}

	public String getName() {
		return name;
	}

	public User getUser() {
		return user;
	}

	public Date getCreationDate() {
		return null;
	}

	public Date getLastModified() {
		return null;
	}

	public boolean equalsByPersistableKey(Persistable persistable) {
		return false;
	}

	public Long getKey() {
		return null;
	}

	public Date getLastLogin() {
		return null;
	}

	public void setLastLogin(Date loginDate) {
	}

	public Integer getStatus() {
		return Identity.STATUS_ACTIV;
	}

	public void setStatus(Integer newStatus) {
	}

	public Date getDeleteEmailDate() {
		return null;
	}

	public void setDeleteEmailDate(Date newDeleteEmail) {
	}

	public void setName(String loginName) {
		// TODO Auto-generated method stub
		
	}

}