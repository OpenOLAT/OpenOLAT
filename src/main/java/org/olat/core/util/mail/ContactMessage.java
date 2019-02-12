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
package org.olat.core.util.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.id.Identity;

/**
 * Initial Date: Jan 22, 2006 <br>
 * 
 * @author patrick
 */
public class ContactMessage {

	private Map<String,ContactList> contactLists = new HashMap<>();
	private List<Identity> disabledIdentities;
	private String bodyText;
	private String subject;
	private Identity from;

	/**
	 * 
	 * @param from
	 */
	public ContactMessage(Identity from) {
		this.from = from;
		disabledIdentities = new ArrayList<>();
	}

	public Identity getFrom(){
		return this.from;
	}
	
	public void setSubject(String subject){
		this.subject=subject;
	}
	
	public String getSubject(){
		return subject;
	}
	
	public void setBodyText(String bodyText){
		this.bodyText=bodyText;
	}
	
	public String getBodyText(){
		return bodyText;
	}
	
	/**
	 * add a ContactList as EmailTo:
	 * 
	 * @param emailList
	 */
	public void addEmailTo(ContactList emailList) {
		emailList = cleanEMailList(emailList);
		if (emailList != null) {
			if (contactLists.containsKey(emailList.getName())) {
				// there is already a ContactList with this name...
				ContactList existing = contactLists.get(emailList.getName());
				// , merge their values.
				existing.add(emailList);
			} else {
				// a new ContactList, put it into contactLists
				contactLists.put(emailList.getName(), emailList);
			}
		}
	}

	/**
	 * @return Returns the disabledIdentities.
	 */
	public List<Identity> getDisabledIdentities() {
		return disabledIdentities;
	}
	
	private ContactList cleanEMailList(ContactList emailList) {
		Identity[] identityMails = emailList.getIdentiEmails().values()
				.toArray(new Identity[emailList.getIdentiEmails().size()]);
		for (Identity identity:identityMails) {
			if(MailHelper.isDisabledMailAddress(identity, null)) {
				emailList.remove(identity);
				if(!disabledIdentities.contains(identity)) {
					disabledIdentities.add(identity);
				}
			}
		}
		if(emailList.getIdentiEmails().size() == 0 && emailList.getStringEmails().size() == 0) {
			emailList = null;
		}
		return emailList;
	}

	/**
	 * a List with ContactLists as elements is returned
	 * 
	 * @return
	 */
	public List<ContactList> getEmailToContactLists() {
		return new ArrayList<>(contactLists.values());
	}
}
