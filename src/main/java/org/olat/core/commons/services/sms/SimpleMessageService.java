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
package org.olat.core.commons.services.sms;

import java.util.List;

import org.olat.core.commons.services.sms.model.MessageStatistics;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 3 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface SimpleMessageService {
	
	/**
	 * @return A six digits token random generated.
	 */
	public String generateToken();
	
	/**
	 * A method to validate the phone number.
	 * 
	 * @param number The phone number
	 * @return true if the service can send a message with the specified number.
	 */
	public boolean validate(String number);
	
	/**
	 * The list of service providers registered in the system.
	 * 
	 * @return A list of service providers.
	 */
	public List<MessagesSPI> getMessagesSpiList();
	
	/**
	 * Return the active messages service provider.
	 * 
	 * @return A message service provider
	 */
	public MessagesSPI getMessagesSpi();
	
	/**
	 * Return the message service provider identified by its id.
	 * 
	 * @param serviceId The id of the desired service provider.
	 * @return A message service provider or null
	 */
	public MessagesSPI getMessagesSpi(String serviceId);

	public void sendMessage(String text, Identity recipient) throws SimpleMessageException;
	
	public void sendMessage(String text, String phone, Identity recipient) throws SimpleMessageException;
	
	public List<MessageStatistics> getStatisticsPerMonth();

}
