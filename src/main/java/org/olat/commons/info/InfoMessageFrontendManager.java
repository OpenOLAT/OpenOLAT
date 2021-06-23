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

package org.olat.commons.info;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.IdentityRef;
import org.olat.commons.info.manager.MailFormatter;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  28 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface InfoMessageFrontendManager {

	public static  final OLATResourceable oresFrontend = OresHelper.lookupType(InfoMessageFrontendManager.class);
	
	public static final String businessGroupResSubPath = "";// may not be null
	
	public InfoMessage loadInfoMessage(Long key);
	
	public InfoMessage createInfoMessage(OLATResourceable ores, String subPath, String businessPath, Identity author);
	
	/**
	 * Publish the info message. Save it and send the email if there are some tos
	 * @param msgs The info message
	 * @param mailFormatter Generate body and subject of the mail (optional)
	 * @param locale The locale of the mail
	 * @param tos The list of recipients of the mail
	 * @return
	 */
	public boolean sendInfoMessage(InfoMessage msgs, MailFormatter mailFormatter, Locale locale, Identity from, List<Identity> tos);
	
	public void saveInfoMessage(InfoMessage msg);
	
	//public VFSItem getAttachment(InfoMessage msg);
	
	public String storeAttachment(File file, String filename, OLATResourceable ores, Identity identity);
	
	public void deleteAttachments(Collection<String> paths);
	
	public void deleteStorage(OLATResourceable ores);
	
	public void deleteInfoMessage(InfoMessage infoMessage);
	
	public void updateInfoMessagesOfIdentity(BusinessGroupRef businessGroup, IdentityRef identity);
	
	public void removeInfoMessagesAndSubscriptionContext(BusinessGroup group);
	
	public List<InfoMessage> loadInfoMessageByResource(OLATResourceable ores, String subPath, String businessPath,
			Date after, Date before, int firstResult, int maxReturn);
	
	public int countInfoMessageByResource(OLATResourceable ores, String subPath, String businessPath,
			Date after, Date before);
	
	public List<Identity> getInfoSubscribers(OLATResourceable resource, String subPath);
	
	public List<VFSLeaf> getAttachments(InfoMessage msg);
	
	public List<File> getAttachmentFiles(InfoMessage msg);

}
