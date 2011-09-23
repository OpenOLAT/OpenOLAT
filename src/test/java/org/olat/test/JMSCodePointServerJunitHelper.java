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
* <p>
*/
package org.olat.test;

import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.olat.testutils.codepoints.server.CodepointInstaller;
import org.olat.testutils.codepoints.server.impl.JMSCodepointServer;

/**
 * Description:<br>
 * helper class for junit testing
 * 
 * <P>
 * Initial Date:  04.03.2010 <br>
 * @author guido
 */
public class JMSCodePointServerJunitHelper {
	
	private static JMSCodepointServer codepointServer_;

	public static void startServer(String codePointServerId) {
		if (codepointServer_ == null) {
	 		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
			ActiveMQQueue queue = new ActiveMQQueue("olat/codepoints");
			try {
				codepointServer_ = new JMSCodepointServer(connectionFactory, queue, codePointServerId);
			} catch (JMSException e) {
				System.out.println("Error while creating codepoint server: "+e.getMessage());
			}
			CodepointInstaller.installCodepointServer(codepointServer_);
			System.out.println("JMSCodePointServerJunitHelper: Codepoint-Server started");
		} 
	}
	
	public static void stopServer() {
		CodepointInstaller.installCodepointServer(null);
		if (codepointServer_!=null) {
			codepointServer_.close();
			codepointServer_ = null;
		}
	}

}
