/**
* OLAT - Online Learning and Training<br />
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br />
* you may not use this file except in compliance with the License.<br />
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br />
* software distributed under the License is distributed on an "AS IS" BASIS, <br />
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
* See the License for the specific language governing permissions and <br />
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.instantMessaging;

/**
 * Allows sending log messages to instant messaging recipients over a jabber server.
 * See log4j.properties for configuration issues.
 * 
 * <P>
 * Initial Date:  13.04.2005 <br />
 *
 * @author guido
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.CyclicBuffer;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * IMAppender appends logging requests through instant messaging network.
 * 
 * @author Rafael Luque & Ruth Zamorano
 */

public class IMAppender extends AppenderSkeleton {

	// ----------------------------------------------- Variables

	private String host;
	private int port = 5222;
	private String username;
	private String password;
	private String recipients;
	private List recipientsList = new ArrayList();
	private final static String DELIMITER =",";
	private boolean chatroom = false;
	private String nickname;
	private int bufferSize = 16;
	protected TriggeringEventEvaluator evaluator;
	protected CyclicBuffer cb;
	protected XMPPConnection con;
	protected List chats = new ArrayList();
	protected MultiUserChat groupchat;

	// -------------------------------------------- Constructors

	/**
	 * The default constructor will instantiate the appender with a default
	 * TriggeringEventEvaluator that will trigger on events with level ERROR or
	 * higher.
	 */
	public IMAppender() {
		this(new IMEvaluator());
	}

	public IMAppender(TriggeringEventEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	// ------------------------------- Setter and getter methods

	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRecipients() {
		return this.recipients;
	}

	public void setRecipients(String recipients) {
		this.recipients = recipients;
		StringTokenizer stockenizer = new StringTokenizer(recipients, DELIMITER);
		while(stockenizer.hasMoreElements()){
			this.recipientsList.add(stockenizer.nextElement());
		}
	}

	public boolean isChatroom() {
		return this.chatroom;
	}

	public void setChatroom(boolean chatroom) {
		this.chatroom = chatroom;
	}

	public String getNickname() {
		return this.nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getBufferSize() {
		return this.bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * The <b>EvaluatorClass </b> option takes a string value representing the
	 * name of the class implementing the {@linkTriggeringEventEvaluator}
	 * interface. A corresponding object will be instantiated and assigned as
	 * the triggering event evaluator for the SMTPAppender.
	 */
	public void setEvaluatorClass(String value) {
		evaluator = (TriggeringEventEvaluator) OptionConverter
				.instantiateByClassName(value, TriggeringEventEvaluator.class,
						evaluator);
	}

	public String getEvaluatorClass() {
		return evaluator == null ? null : evaluator.getClass().getName();
	}

	// ---------------------------------- Log4j callback methods

	/**
	 * Options are activated and become effective only after calling this
	 * method.
	 */
	public void activateOptions() {
		try {
			cb = new CyclicBuffer(bufferSize);

			// Create a connection to the XMPP server
			LogLog.debug("Stablishing connection with XMPP server");
			con = new XMPPConnection(InstantMessagingModule.getConnectionConfiguration());
			// Most servers require you to login before performing other tasks
			LogLog.debug("About to login as [" + username + "/" + password
					+ "]");
			con.connect();
			con.login(username, password);

			// Start a conversation with IMAddress
			if (chatroom) {
				LogLog.debug("About to create ChatGroup");
				groupchat = new MultiUserChat(con, (String)recipientsList.get(0));
				LogLog.debug("About to join room");
				groupchat.join(nickname != null ? nickname : username);
			} else {
				Iterator iter = recipientsList.iterator();
				while (iter.hasNext()) {
					chats.add(con.getChatManager().createChat((String)iter.next(), null));
				}
				//chat = con.createChat(recipients);
			}

		} catch (XMPPException xe) {
			errorHandler
					.error(
							"Error while activating options for appender named ["
									+ name
									+ "] Could not connect to instant messaging server with user: "
									+ getUsername(), xe,
							ErrorCode.GENERIC_FAILURE);
		} catch (Exception e) {
			errorHandler.error(
					"Error while activating options for appender named ["
							+ name + "]", e, ErrorCode.GENERIC_FAILURE);
		}
	}

	/**
	 * Close this IMAppender. Closing all resources used by the appender. A
	 * closed appender cannot be re-opened.
	 */
	public synchronized void close() {//o_clusterOK by:fj
		if (this.closed)
			return;

		LogLog.debug("Closing appender [" + name + "]");
		this.closed = true;

		// Closes the connection by setting presence to unavailable
		// then closing the stream to the XMPP server.
		if (con != null)
			con.disconnect();

		// Help GC
		con = null;
		chats.clear();
		chats = null;
		groupchat = null;
	}

	/**
	 * This method called by {@link AppenderSkeleton#doAppend}method does most
	 * of the real appending work. Adds the event to a buffer and checks if the
	 * event triggers a message to be sent.
	 */
	public void append(LoggingEvent event) {

		// check pre-conditions
		if (!checkEntryConditions()) {
			return;
		}

		cb.add(event);
		if (evaluator.isTriggeringEvent(event)) {
			sendBuffer();
		}
	}

	/**
	 * Send the contents of the cyclic buffer as an IM message.
	 */
	protected void sendBuffer() {
		try {
			StringBuilder buf = new StringBuilder();

			int len = cb.length();
			for (int i = 0; i < len; i++) {
				LoggingEvent event = cb.get();
				buf.append(layout.format(event));
				// if layout doesn't handle exception, the appender has to do it
				if (layout.ignoresThrowable()) {
					String[] s = event.getThrowableStrRep();
					if (s != null) {
						for (int j = 0; j < s.length; j++) {
							buf.append(Layout.LINE_SEP);
							buf.append(s[j]);
						}
					}
				}
			}

			if (chatroom) {
				groupchat.sendMessage(buf.toString());
			} else {
				Iterator iter = chats.iterator();
				while (iter.hasNext()) {
					Chat chat = (Chat) iter.next();
					chat.sendMessage(buf.toString());
				}
			}

		} catch (Exception e) {
			errorHandler.error("Could not send message in IMAppender [" + name
					+ "]", e, ErrorCode.GENERIC_FAILURE);
		}
	}

	/**
	 * This method determines if there is a sense in attempting to append.
	 * 
	 * <p>
	 * It checks whether there is an output chat available and also if there is
	 * a set layout. If these checks fail, then the boolean value
	 * <code>false</code> is returned.
	 */
	protected boolean checkEntryConditions() {
		if ((this.chats.size() == 0) && (this.groupchat == null)) {
			errorHandler.error("Chat object not configured");
			return false;
		}

		if (this.layout == null) {
			errorHandler.error("No layout set for appender named [" + name
					+ "]");
			return false;
		}
		return true;
	}

	/**
	 * The IMAppender requires a layout. Hence, this method returns
	 * <code>true</code>.
	 */
	public boolean requiresLayout() {
		return true;
	}
}