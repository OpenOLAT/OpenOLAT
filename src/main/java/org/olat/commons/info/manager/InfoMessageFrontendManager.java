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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.commons.info.manager;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.commons.info.model.InfoMessage;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.resource.OresHelper;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  28 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public abstract class InfoMessageFrontendManager extends BasicManager {
	
	protected static InfoMessageFrontendManager INSTANCE;
	
	public static  final OLATResourceable oresFrontend = OresHelper.lookupType(InfoMessageFrontendManager.class);
	
	public static InfoMessageFrontendManager getInstance() {
		return INSTANCE;
	}
	
	public abstract InfoMessage loadInfoMessage(Long key);
	
	public abstract InfoMessage createInfoMessage(OLATResourceable ores, String subPath, String businessPath, Identity author);
	
	/**
	 * Publish the info message. Save it and send the email if there are some tos
	 * @param msgs The info message
	 * @param mailFormatter Generate body and subject of the mail (optional)
	 * @param locale The locale of the mail
	 * @param tos The list of recipients of the mail
	 * @return
	 */
	public abstract boolean sendInfoMessage(InfoMessage msgs, MailFormatter mailFormatter, Locale locale, List<Identity> tos);
	
	public abstract void deleteInfoMessage(InfoMessage infoMessage);
	
	public abstract List<InfoMessage> loadInfoMessageByResource(OLATResourceable ores, String subPath, String businessPath,
			Date after, Date before, int firstResult, int maxReturn);
	
	public abstract int countInfoMessageByResource(OLATResourceable ores, String subPath, String businessPath,
			Date after, Date before);
	
	public abstract List<Identity> getInfoSubscribers(OLATResourceable resource, String subPath);

}
