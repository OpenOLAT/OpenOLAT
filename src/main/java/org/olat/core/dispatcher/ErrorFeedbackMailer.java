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

package org.olat.core.dispatcher;

import java.util.Collection;
import java.util.Iterator;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.id.Identity;
import org.olat.core.logging.LogFileParser;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;

/**
 * Description:<br>
 * Send an Email to the support address
 * <P>
 * Initial Date: Jan 31, 2006 <br>
 * 
 * @author guido
 */
public class ErrorFeedbackMailer implements Dispatcher {
	
	private static final Logger log = Tracing.createLoggerFor(ErrorFeedbackMailer.class);
	
	private MailManager mailManager;
	private BaseSecurity securityManager;

	/**
	 * [used by Spring]
	 * @param mailManager
	 */
	public void setMailManager(MailManager mailManager) {
		this.mailManager = mailManager;
	}

	/**
	 * [used by spring]
	 * @param securityManager
	 */
	public void setSecurityManager(BaseSecurity securityManager) {
		this.securityManager = securityManager;
	}

	/**
	 * send email to support with user submitted error information
	 * 
	 * @param request
	 */
	public void sendMail(HttpServletRequest request) {
		String feedback = request.getParameter("textarea");
		String errorNr = request.getParameter("fx_errnum");
		String identityKey = request.getParameter("username");
		try {
			if(StringHelper.isLong(identityKey)) {
				Identity ident = securityManager.loadIdentityByKey(Long.valueOf(identityKey));
				Collection<String> logFileEntries = LogFileParser.getErrorToday(errorNr, false);
				StringBuilder out = new StringBuilder(2048);
				out.append(feedback)
				   .append("\n------------------------------------------\n\n --- from user: ").append(identityKey).append(" ---");
				if (logFileEntries != null) {
					for (Iterator<String> iter = logFileEntries.iterator(); iter.hasNext();) {
						out.append(iter.next());
					}
				}
	
				MailBundle bundle = new MailBundle();
				bundle.setFromId(ident);
				bundle.setTo(WebappHelper.getMailConfig("mailError"));
				bundle.setContent("Feedback from Error Nr.: " + errorNr, out.toString());
				mailManager.sendExternMessage(bundle, null, false);
			} else {
				log.error("Try to send a feedback without identity");
			}
		} catch (Exception e) {
			handleException(request, e);
		}
	}

	private void handleException(HttpServletRequest request, Exception e) {
		String feedback = request.getParameter("textarea");
		String username = request.getParameter("username");
		log.error("Error sending error feedback mail to OpenOLAT error support ({}) from: {} with content: {}", WebappHelper.getMailConfig("mailError"), username, feedback, e);
	}

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		sendMail(request);
		DispatcherModule.redirectToDefaultDispatcher(response);
	}
}
