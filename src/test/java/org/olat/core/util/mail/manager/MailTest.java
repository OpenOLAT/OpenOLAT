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
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.core.util.mail.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

	
/**
 * Description:<br>
 * jUnit tests for the mail package
 * <P>
 * Initial Date: 21.11.2006 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH<br>
 *         http://www.frentix.com
 */
public class MailTest extends OlatTestCase {
	private Identity id1, id2, id3, id4, id6;
	
	@Autowired
	private MailManager mailManager;

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
		id1 = JunitTestHelper.createAndPersistIdentityAsUser("one");
		id2 = JunitTestHelper.createAndPersistIdentityAsUser("two");
		id3 = JunitTestHelper.createAndPersistIdentityAsUser("three");
		id4 = JunitTestHelper.createAndPersistIdentityAsUser("four");
		id6 = JunitTestHelper.createAndPersistIdentityAsUser("six");
	}
	
	@Test
	public void testValidEmailAddresses() {
		Assert.assertTrue(MailHelper.isValidEmailAddress("gnaegi@frentix.com"));
		Assert.assertTrue(MailHelper.isValidEmailAddress("login@w.pl"));
		Assert.assertTrue(MailHelper.isValidEmailAddress("christian.reichel@on-point.consulting"));
		Assert.assertTrue(MailHelper.isValidEmailAddress("gnägi@frentix.com"));
		Assert.assertTrue(MailHelper.isValidEmailAddress("someone@[192.168.1.100]"));
	}
	
	@Test
	public void testInvalidEmailAddresses() {
		Assert.assertFalse(MailHelper.isValidEmailAddress(null));
		Assert.assertFalse(MailHelper.isValidEmailAddress(""));
		Assert.assertFalse(MailHelper.isValidEmailAddress("gnaegi @ frentix.com"));
		Assert.assertFalse(MailHelper.isValidEmailAddress("gnaegi@frentix_com"));
		Assert.assertFalse(MailHelper.isValidEmailAddress("\"Florian Gnaegi\" <gnaegi@frentix.com>"));
		Assert.assertFalse(MailHelper.isValidEmailAddress("g@g"));
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
		((MailManagerImpl)mailManager).evaluate(context, template, writer, result);
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo bar", writer.toString());

		writer = new StringWriter();
		template = "foo foo";
		((MailManagerImpl)mailManager).evaluate(context, template, writer, result);
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo foo", writer.toString());

		writer = new StringWriter();
		template = "foo $$foo";
		((MailManagerImpl)mailManager).evaluate(context, template, writer, result);
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo $bar", writer.toString());

		writer = new StringWriter();
		template = "foo $ foo";
		((MailManagerImpl)mailManager).evaluate(context, template, writer, result);
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo $ foo", writer.toString());

		writer = new StringWriter();
		template = "foo $ foo";
		((MailManagerImpl)mailManager).evaluate(context, template, writer, result);
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo $ foo", writer.toString());

		writer = new StringWriter();
		template = "foo #foo \n##sdf jubla";
		((MailManagerImpl)mailManager).evaluate(context, template, writer, result);
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo #foo \n", writer.toString());

		writer = new StringWriter();
		template = "foo #if(true)\n#end";
		((MailManagerImpl)mailManager).evaluate(context, template, writer, result);
		assertEquals(MailerResult.OK, result.getReturnCode());
		assertEquals("foo ", writer.toString());

		// illegal templates: unclosed if-else statement
		writer = new StringWriter();
		template = "foo #if";
		((MailManagerImpl)mailManager).evaluate(context, template, writer, result);
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
		List<Identity> recipients = new ArrayList<>();
		recipients.add(id1);
		recipients.add(id2);
		recipients.add(id3);
		Identity recipientCC = id4;

		// tests with / witthout CC and BCC

		MailerResult result = new MailerResult();
		result = sendMailAsSeparateMails(null, recipients, null, template, id6, null);
		assertEquals(MailerResult.OK, result.getReturnCode());
		result = sendMailAsSeparateMails(null, recipients, recipientCC, template, id6, null);
		assertEquals(MailerResult.OK, result.getReturnCode());
		result = sendMailAsSeparateMails(null, recipients, null, template, id6, null);
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
		List<Identity> recipients = new ArrayList<>();
		recipients.add(id1);
		recipients.add(id2);
		recipients.add(id3);
		Identity recipientCC = id4;

		// tests with / witthout CC and BCC

		MailerResult result = new MailerResult();
		result = sendMailAsSeparateMails(null, recipients, null, template, id6, null);
		assertEquals(MailerResult.OK, result.getReturnCode());
		result = sendMailAsSeparateMails(null, recipients, recipientCC, template, id6, null);
		assertEquals(MailerResult.OK, result.getReturnCode());
		result = sendMailAsSeparateMails(null, recipients, null, template, id6, null);
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
		File file1;
		try {
			System.out.println("MailTest.testMailAttachments Url1=" + MailTest.class.getResource("MailTest.class") );
			file1 = new File(MailTest.class.getResource("MailTest.class").toURI());
			attachments[0] = file1;
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
		List<Identity> recipients = new ArrayList<>();
		recipients.add(id1);

		MailerResult result = new MailerResult();
		result = sendMailAsSeparateMails(null, recipients, null, template, id2, null);
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
		List<Identity> recipients = new ArrayList<>();
		recipients.add(id1);

		MailerResult result = new MailerResult();
		result = sendMailAsSeparateMails(null, recipients, null, template, id2, null);
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
		Identity illegal1 = JunitTestHelper.createAndPersistIdentityAsUser("illegal1");
		illegal1.getUser().setProperty(UserConstants.EMAIL, "doesnotexisteserlkmlkm@sdf.com");
		Identity illegal2 = JunitTestHelper.createAndPersistIdentityAsUser("illegal2");
		illegal2.getUser().setProperty(UserConstants.EMAIL, "sd@this.domain.does.not.exist.at.all");
		Identity illegal3 = JunitTestHelper.createAndPersistIdentityAsUser("illegal3");
		illegal3.getUser().setProperty(UserConstants.EMAIL, "@ sdf");
		
		DBFactory.getInstance().intermediateCommit();

		List<Identity> recipients = new ArrayList<>();


		recipients.add(illegal1);

		// if only one recipient: error must be indicated
		MailerResult result = new MailerResult();
		result = sendMailAsSeparateMails(null, recipients, null, template, id6, null);
		// mail will bounce back since address does not exist, but sent to local MTA
		// this test is not very good, depends on smtp settings!
		//assertEquals(MailerResult.OK, result.getReturnCode());

		recipients = new ArrayList<>();
		recipients.add(illegal2);
		result = sendMailAsSeparateMails(null, recipients, null, template, id6, null);
		// mail will bounce back since address does not exist, but sent to local MTA
		assertEquals(MailerResult.OK, result.getReturnCode());

		recipients = new ArrayList<>();
		recipients.add(illegal3);
		result = sendMailAsSeparateMails(null, recipients, null, template, id6, null);
		assertEquals(MailerResult.RECIPIENT_ADDRESS_ERROR, result.getReturnCode());

		// now with one valid and the invalid recipient: should return ok but have
		// one recipient in the failed list
		recipients.add(id1);
		result = sendMailAsSeparateMails(null, recipients, null, template, id6, null);
		assertEquals(MailerResult.RECIPIENT_ADDRESS_ERROR, result.getReturnCode());
		assertEquals(1, result.getFailedIdentites().size());

		// valid recipient but invalid sender
		recipients = new ArrayList<>();
		recipients.add(id1);
		result = sendMailAsSeparateMails(null, recipients, null, template, illegal3, null);
		assertEquals(MailerResult.SENDER_ADDRESS_ERROR, result.getReturnCode());

		// invalid cc and bcc but valid to, mus count up the invalid accounts
		recipients = new ArrayList<>();
		recipients.add(id1);
		recipients.add(illegal3); // first
		Identity recipientCC = illegal3; // second
		result =sendMailAsSeparateMails(null, recipients, recipientCC, template, id6, null);
		// mail will bounce back since address does not exist, but sent to local MTA
		assertEquals(MailerResult.RECIPIENT_ADDRESS_ERROR, result.getReturnCode());
		assertEquals(2, result.getFailedIdentites().size());

	}
	
	public MailerResult sendMailAsSeparateMails(MailContext mCtxt, List<Identity> recipientsTO,
			Identity recipientCC, MailTemplate template, Identity sender, String metaId) {

		MailerResult result = new MailerResult();
		MailBundle[] bundles = mailManager.makeMailBundles(mCtxt, recipientsTO, template, sender, metaId, result);
		result.append(mailManager.sendMessage(bundles));
		
		if(recipientCC != null) {
			MailBundle ccBundle = mailManager.makeMailBundle(mCtxt, recipientCC, template, sender, metaId, result);
			result.append(mailManager.sendMessage(ccBundle));
		}

		return result;
	}
}