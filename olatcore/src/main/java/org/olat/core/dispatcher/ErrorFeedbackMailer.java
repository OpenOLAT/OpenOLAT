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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.dispatcher;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityManager;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.LogFileParser;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.Emailer;

/**
 * Description:<br>
 * Send an Email to the support address
 * <P>
 * Initial Date: Jan 31, 2006 <br>
 * 
 * @author guido
 */
public class ErrorFeedbackMailer implements Dispatcher {

	private static final ErrorFeedbackMailer INSTANCE = new ErrorFeedbackMailer();

	private ErrorFeedbackMailer() {
	// private since singleton
	}

	protected static ErrorFeedbackMailer getInstance() {
		return INSTANCE;
	}

	/**
	 * send email to olat support with user submitted error informaition
	 * 
	 * @param request
	 */
	public void sendMail(HttpServletRequest request) {
		String feedback = request.getParameter("textarea");
		String errorNr = feedback.substring(0, feedback.indexOf("\n") - 1);
		String username = request.getParameter("username");
		try {
            IdentityManager im = (IdentityManager)CoreSpringFactory.getBean("core.id.IdentityManager");
			Identity ident = im.findIdentityByName(username);
			//if null, user may crashed befor getting a valid session, try with guest user instead
			if (ident == null) ident = im.findIdentityByName("guest");
			Emailer emailer = new Emailer(ident, false);
			String errorNum = parseErrorNumber(errorNr);
			Collection logFileEntries = LogFileParser.getErrorToday(errorNum, false);
			StringBuilder out = new StringBuilder();
			if (logFileEntries != null) {
				for (Iterator iter = logFileEntries.iterator(); iter.hasNext();) {
					out.append((String) iter.next());
				}
			}
			emailer.sendEmail(WebappHelper.getMailConfig("mailSupport"), "Feedback from Error Nr.: " + errorNr, request.getParameter("textarea")
					+ "\n------------------------------------------\n\n --- from user: "+username+" ---" + out.toString());
		} catch (AddressException e) {
			// error in recipient email address(es)
			handleException(request, e);
			return;
		} catch (SendFailedException e) {
			// error in sending message
			// CAUSE: sender email address invalid
			handleException(request, e);
			return;
		} catch (MessagingException e) {
			// error in message-subject || .-body
			handleException(request, e);
			return;
		} catch (AssertException e) {
			handleException(request, e);
			return;
		} catch (NullPointerException e) {
			handleException(request, e);
			return;
		}

	}

	private String parseErrorNumber(String errorNr) {
		// try with format N<nodeId>-E<errorCode> first
		Pattern r1 = Pattern.compile("N[0-9]+-E[0-9]+");
		Matcher m1 = r1.matcher(errorNr);
		if (m1.find()) return m1.group();
		
		Pattern r2 = Pattern.compile("E[0-9]+");
		Matcher m2 = r2.matcher(errorNr);
		if (m2.find()) return m2.group();
		return "";
		
	}

	private void handleException(HttpServletRequest request, Exception e) {
		String feedback = request.getParameter("textarea");
		String username = request.getParameter("username");
		Tracing.logError("Error sending error feedback mail to olat support (" + WebappHelper.getMailConfig("mailSupport")
				+ ") from: " + username + " with content: " + feedback, e, this.getClass());

	}

	/**
	 * @see org.olat.core.dispatcher.Dispatcher#execute(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
	 */
	public void execute(HttpServletRequest request, HttpServletResponse response, String uriPrefix) {
		sendMail(request);
		DispatcherAction.redirectToDefaultDispatcher(response);
	}

}
