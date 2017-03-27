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

package org.olat.commons.info.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.commons.info.model.InfoMessage;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.group.BusinessGroupRef;

public abstract class InfoMessageManager {

	protected static InfoMessageManager INSTANCE;
	
	
	public static InfoMessageManager getInstance() {
		return INSTANCE;
	}
	
	
	public abstract InfoMessage createInfoMessage(OLATResourceable ores, String subPath, String businessPath, Identity author);
	
	public abstract void saveInfoMessage(InfoMessage infoMessage);
	
	public abstract void deleteInfoMessage(InfoMessage infoMessage);
	
	public abstract List<InfoMessage> loadInfoMessagesOfIdentity(BusinessGroupRef businessGroup, IdentityRef identity);
	
	public abstract InfoMessage loadInfoMessageByKey(Long key);
	
	public abstract List<InfoMessage> loadInfoMessageByResource(OLATResourceable ores, String subPath, String businessPath,
			Date after, Date before, int firstResult, int maxReturn);
	
	public abstract int countInfoMessageByResource(OLATResourceable ores, String subPath, String businessPath,
			Date after, Date before);
}
