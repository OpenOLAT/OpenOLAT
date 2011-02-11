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
 * Copyright (c) since 2004 at frentix GmbH, www.frentix.com
 * <p>
 */
package org.olat.instantMessaging;

import org.olat.core.logging.OLog;
import org.olat.core.logging.StartupException;
import org.olat.core.logging.Tracing;


/**
 * Description:<br>
 * Helps to get the right user-/groupname for chat
 * Used to have multiple OLAT instances running on one single jabber server.
 * Can be turned off in olat.properties with instantMessaging.multipleInstances=false
 * <P>
 * Initial Date: 31.03.2008 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com
 * @author guido
 */
public class IMNameHelper {
	private static OLog log = Tracing.createLoggerFor(IMNameHelper.class);

	private String instanceID;
	private String USERNAME_INSTANCE_DELIMITER = "_";
	private String GROUPNAME_INSTANCE_DELIMITER = "-";
	private IMConfig config;
	
	/**
	 * [used by spring]
	 * @param config
	 */
	public IMNameHelper(IMConfig config, String instanceID) {
		if (config == null) throw new StartupException("IMConfig cannot be null!");
		this.config = config;
		this.instanceID = instanceID;
	}

	/**
	 * convert olatUsername to imUsername the instanceID is attached to the
	 * olatUsername to distinguish between multiple OLAT instances using all the
	 * same IM-server
	 * 
	 * @param olatUsername (i.e: administrator, author) may contain @
	 * @return im-Username/JID (i.e. administrator_OLATinstanceID )
	 */
	public String getIMUsernameByOlatUsername(String olatUsername) {
		// replace "@" by "_at_" --> this allows "@" also for olat-usernames
		String imUsername = olatUsername.replace("@", config.getReplaceStringForEmailAt());
		if (config.isMultipleInstances()) {
			return (imUsername + USERNAME_INSTANCE_DELIMITER + instanceID).toLowerCase();
		}
		else {
			return imUsername.toLowerCase();
		}
	}
	
	/**
	 * Get olatUsername from imUsername/JID 
	 * 
	 * @param imUsername (i.e. administrator_OLATinstanceID@jabber.olat.org )
	 * 
	 * @return olatUsername (i.e: administrator, author) may contain @
	 */
	public String extractOlatUsername(String imUsername) {
		// info@localhost -> olat itself (chatbot)
		// see ChatController.colorize()
		if (imUsername != null && imUsername.indexOf("@")!=-1 ) {
			if (imUsername.startsWith("info@")) return imUsername;
			imUsername = imUsername.substring(0, imUsername.lastIndexOf("@"));
			if (config.isMultipleInstances()){
				try {
					imUsername = imUsername.substring(0, imUsername.lastIndexOf(USERNAME_INSTANCE_DELIMITER));										
				} catch (StringIndexOutOfBoundsException e) {
					log.error("Can not extract OLAT username from Jabber username::" + imUsername, e);					
					return "?";
				}
			}
			// if olatUsername contains @ this is backconverted from the "_at_"
			// form, which was used for jabber-server
			int atPos = imUsername.lastIndexOf(config.getReplaceStringForEmailAt());
			if (atPos != -1) {
				String tmpUsername = imUsername.substring(0, atPos);
				tmpUsername += "@";
				tmpUsername += imUsername.substring(atPos+config.getReplaceStringForEmailAt().length());
				return tmpUsername;
			}
			return imUsername;
		}
		// in case this is not a valid imUsername
		return "?";
	}

	/**
	 * convert groupId (name) to be usable with multiple OLAT-instances
	 * 
	 * @param groupId (i.e: BusinessGroup-12345)
	 * @return groupId with attached instance-id (i.e:
	 *         BusinessGroup-12345-OLATinstanceID )
	 */
	public String getGroupnameForOlatInstance(String groupId) {
		if (config.isMultipleInstances()) {
			return groupId + GROUPNAME_INSTANCE_DELIMITER + instanceID;
		}
		else return groupId;
	}
	
}
