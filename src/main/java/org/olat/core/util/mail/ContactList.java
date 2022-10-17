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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * The ContactList is used to group e-mail addresses and name such a group. It
 * is the frameworks implementation of a MailList. The e-mail adresses are given
 * either by providing them as <code>strings</code> or in the form of
 * identites (<code>org.olat.core.id.Identity</code>). Further it is
 * possible to add all contacts from another ContactList.
 * <P>
 * Moreover one can specify to use the users institional e-mail first and use
 * the other e-mail as fall-back.
 * 
 * Initial Date: Sep 23, 2004
 * @author patrick
 */
public class ContactList {
	
	private static final Logger log = Tracing.createLoggerFor(ContactList.class);
	
	private String name;
	private String description;
	//container for addresses contributed as strings
	private Map<String, String> stringEmails = new HashMap<>();
	//container for addresses contributed as identites
	private Map<Long, Identity> identiEmails = new HashMap<>();
	private boolean emailPrioInstitutional = false;
	
	/**
	 * A ContacList must have at least a name != null, matching ^[^;,:]*$
	 * 
	 * @param name
	 */
	public ContactList(String name) {
		setName(name);
	}

	/**
	 * check the priority of the institutional mail is set.
	 * 
	 * @return Returns the emailPrioInstitutional.
	 */
	public boolean isEmailPrioInstitutional() {
		return emailPrioInstitutional;
	}

	/**
	 * set the priority of the institutional mail.
	 * 
	 * @param emailPrioInstitutional The emailPrioInstitutional to set.
	 */
	public void setEmailPrioInstitutional(boolean emailPrioInstitutional) {
		this.emailPrioInstitutional = emailPrioInstitutional;
	}

	/**
	 * A ContactList must have at least a name != null, matching ^[a-zA-Z ]*$, and
	 * can have a description.
	 * 
	 * @param name
	 * @param description
	 */
	public ContactList(String name, String description) {
		setName(name);
		this.description = description;
	}

	/**
	 * contribute a contact as a string email address.
	 * 
	 * @param emailAddress
	 */
	public void add(String emailAddress) {
		stringEmails.put(keyFrom(emailAddress), emailAddress);
	}

	/**
	 * contribute a contact as an identity.
	 * 
	 * @param identity
	 */
	public void add(Identity identity) {
		identiEmails.put(identity.getKey(), identity);
	}
	
	public void remove(Identity identity) {
		identiEmails.remove(identity.getKey());
	}

	/**
	 * Name getter
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * ContactList-name as String encoded according to
	 * http://www.ietf.org/rfc/rfc2047.txt
	 * 
	 * @return String encoded according to RFC2047
	 */
	public String getRFC2047Name() {
		String rfc2047name;
		// try to MIME-encode the name, if this fails, return the name un-encoded  
		try {
			rfc2047name = jakarta.mail.internet.MimeUtility.encodeWord(name, "UTF-8", null);
		}
		catch (java.io.UnsupportedEncodingException e) {
			log.warn("Error MIME-encoding name: ", e);
			rfc2047name = name;
		}

		return rfc2047name;
	}


	/**
	 * ContactList-name as String formatted according to <a href =
	 * "http://www.ietf.org/rfc/rfc2822.txt"> RFC2822 </a>
	 * 
	 * @return
	 */
	public String getRFC2822Name() {
		return getRFC2047Name() + ":";
	}

	/**
	 * ContactList-name and e-mail adresses as String formatted according to
	 * http://www.ietf.org/rfc/rfc2822.txt
	 * 
	 * @return
	 */
	public String getRFC2822NameWithAddresses() {
		return getRFC2822Name() + toString() + ";";
	}

	/**
	 * Description getter
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * the returned ArrayList contains String Objects representing the e-mail
	 * addresses added. The address of user which login is denied are excluded.
	 * 
	 * @return
	 */
	public List<String> getEmailsAsStrings() {
		List<String> ret = new ArrayList<>(stringEmails.values());
		/*
		 * if priority is on institutional email get all the institutional emails
		 * first, if they are present, remove the identity from the hashtable. If
		 * they were not present, the user email is used in the next loop.
		 */
		List<Identity> copy = new ArrayList<>(identiEmails.values());
		if (emailPrioInstitutional) {
			for (Iterator<Identity> it=copy.iterator(); it.hasNext(); ) {
				Identity tmp = it.next();
				if(Identity.STATUS_LOGIN_DENIED.equals(tmp.getStatus())) {
					continue;
				}

				String addEmail = tmp.getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, null);
				if (addEmail != null) {
					ret.add(addEmail);
					it.remove();
				}
			}
		}
		/*
		 * loops over the (remaining) identities, fetches the user email.
		 */
		for (Identity tmp : copy){
			if(Identity.STATUS_LOGIN_DENIED.equals(tmp.getStatus())) {
				continue;
			}
			String email = tmp.getUser().getProperty(UserConstants.EMAIL, null);
			if (StringHelper.containsNonWhitespace(email)) {
				ret.add(email);
			}
		}
		return ret;
	}
	
	public boolean hasAddresses() {
		return (identiEmails != null && identiEmails.size() > 0)
			|| (stringEmails != null && stringEmails.size() > 0);
	}

	/**
	 * add members of another ContactList to this ContactList.
	 * 
	 * @param emailList
	 */
	public void add(ContactList emailList) {
		stringEmails.putAll(emailList.getStringEmails());
		identiEmails.putAll(emailList.getIdentiEmails());
	}

	/**
	 * A comma separated list of e-mail addresses. The ContactList name is
	 * ommitted, if the form
	 * ContactList.Name:member1@host.suffix,member2@host.suffix,...,memberN@host.suffix;
	 * is needed, please use the appropriate getRFC2822xxx getter method.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String retVal = "";
		String sep = "";
		for (String email:getEmailsAsStrings()) {
			retVal += sep + email;
			sep = ", ";
		}
		return retVal;
	}

	/**
	 * add all identity-Objects in the provided list.
	 * 
	 * @param listOfIdentity List containing Identites
	 */
	public void addAllIdentites(Collection<Identity> listOfIdentity) {
		for (Identity identity:listOfIdentity) {
			add(identity);
		}
	}

	/**
	 * The e-mail addresses are generated as InternetAddresses, the priority of
	 * the institutional email is taken in account.
	 * 
	 * @return the email addresses as InternetAddresses
	 * @throws AddressException
	 */
	public InternetAddress[] getEmailsAsAddresses() throws AddressException {
		return InternetAddress.parse(toString());
	}

	public Map<String,String> getStringEmails() {
		return stringEmails;
	}

	public Map<Long, Identity> getIdentiEmails() {
		return identiEmails;
	}

	private String keyFrom(String unformattedEmailAddr) {
		String key = unformattedEmailAddr.trim();
		return key.toUpperCase();
	}

	private void setName(String nameP) {
		if (!StringHelper.containsNoneOfCoDouSemi(nameP)
				|| nameP.contains("(") || nameP.contains(")")
				|| nameP.contains("[") || nameP.contains("]")) {
			log.warn("Contact list name \"{}\" doesn't match {}", nameP, StringHelper.ALL_WITHOUT_COMMA_2POINT_STRPNT);
			//replace bad chars with bad char in rfc compliant comments
			nameP = nameP.replace(":","¦")
					.replace(";","_")
					.replace(",","-")
					.replace("(","_")
					.replace(")","_")
					.replace("[","_")
					.replace("]","_");
		}		
		this.name = nameP;
	}
}