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

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.commons.info.model.InfoMessageToCurriculumElementImpl;
import org.olat.commons.info.model.InfoMessageToGroupImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.modules.curriculum.CurriculumElement;

public interface InfoMessageManager {
	
	public InfoMessage createInfoMessage(OLATResourceable ores, String subPath, String businessPath, Identity author);

	/**
	 * retrieve all infoMessages, which are not published yet and have a publishDate in the past
	 * for publishing those infoMessages. Made for scheduler job
	 *
	 * @param firstResult The first result
	 * @param maxResults The maximum number of returned entries
	 * @return list of unpublished infoMessages, which have to be published
	 */
	List<InfoMessage> loadUnpublishedInfoMessages(int firstResult, int maxResults);
	
	public void saveInfoMessage(InfoMessage infoMessage);
	
	public void deleteInfoMessage(InfoMessage infoMessage);
	
	public List<InfoMessage> loadInfoMessagesOfIdentity(BusinessGroupRef businessGroup, IdentityRef identity);
	
	public InfoMessage loadInfoMessageByKey(Long key);
	
	public List<InfoMessage> loadInfoMessageByResource(OLATResourceable ores, String subPath, String businessPath,
			Date after, Date before, int firstResult, int maxReturn);
	
	public int countInfoMessageByResource(OLATResourceable ores, String subPath, String businessPath,
			Date after, Date before);

	/**
	 * create a new entry for an infoMessageToGroup
	 *
	 * @param infoMessage new entry belongs to this specific infoMessage
	 * @param businessGroup new entry is linked to this specific group
	 * @return InfoMessageToGroup object
	 */
	InfoMessageToGroup createInfoMessageToGroup(InfoMessage infoMessage, BusinessGroup businessGroup);

	/**
	 * retrieve all infoMessageToGroup objects with given group
	 *
	 * @param group query object
	 * @return list with infoMessageToGroupImpl objects containing param
	 */
	List<InfoMessageToGroupImpl> loadInfoMessageToGroupByGroup(BusinessGroup group);

	/**
	 * remove specific infoMessageToGroup object from database
	 *
	 * @param infoMessageToGroup object to remove
	 */
	void deleteInfoMessageToGroup(InfoMessageToGroup infoMessageToGroup);

	/**
	 * create a new entry for an infoMessageToCurriculumElement
	 *
	 * @param infoMessage new entry belongs to this specific infoMessage
	 * @param curriculumElement new entry is linked to this specific curriculumElement
	 * @return infoMessageToCurriculumElement object
	 */
	InfoMessageToCurriculumElement createInfoMessageToCurriculumElement(InfoMessage infoMessage, CurriculumElement curriculumElement);

	/**
	 * retrieve all infoMessageToCurriculumElement objects with given group
	 *
	 * @param curriculumElement query object
	 * @return list with infoMessageToCurriculumElementImpl objects containing param
	 */
	List<InfoMessageToCurriculumElementImpl> loadInfoMessageToCurriculumElementByCurEl(CurriculumElement curriculumElement);

	/**
	 * remove specific infoMessageToCurriculumElement object from database
	 *
	 * @param infoMessageToCurriculumElement object to remove
	 */
	void deleteInfoMessageToCurriculumElement(InfoMessageToCurriculumElement infoMessageToCurriculumElement);
}
