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
package org.olat.repository.ui.author;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.id.Identity;

/**
 * Initial date: Jan 3, 2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ModifyOwnersContext {
	
	public static String CONTEXT_KEY = ModifyOwnersContext.class.getSimpleName();
	
	private List<AuthoringEntryRow> authoringEntryRows;
	private List<Identity> owners;
	private List<Identity> ownersToRemove;
	private List<Identity> ownersToAdd;
	private Map<Identity, List<AuthoringEntryRow>> ownersResourcesMap;
	private boolean sendMail;
	
	public List<AuthoringEntryRow> getAuthoringEntryRows() {
		return authoringEntryRows;
	}
	
	public void setAuthoringEntryRows(List<AuthoringEntryRow> authoringEntryRows) {
		this.authoringEntryRows = authoringEntryRows;
	}
	
	public List<Identity> getOwners() {
		return owners;
	}
	
	public void addOwnersAndResource(List<Identity> owners, AuthoringEntryRow resource) {
		if (this.owners == null) {
			this.owners = new ArrayList<>();
		}
		
		if (ownersResourcesMap == null) {
			ownersResourcesMap = new HashMap<>();
		}
		
		for (Identity owner : owners) {
			if (!this.owners.contains(owner)) {
				this.owners.add(owner);
			}
			
			if (ownersResourcesMap.containsKey(owner)) {
				if (!ownersResourcesMap.get(owner).contains(resource)) {
					ownersResourcesMap.get(owner).add(resource);
				}
			} else {
				List<AuthoringEntryRow> rows = new ArrayList<>();
				rows.add(resource);
				
				ownersResourcesMap.put(owner, rows);
			}
		}
	}
	
	public Map<Identity, List<AuthoringEntryRow>> getOwnersResourcesMap() {
		return ownersResourcesMap;
	}
	
	public List<Identity> getOwnersToRemove() {
		return ownersToRemove;
	}
	
	public void setOwnersToRemove(List<Identity> ownersToRemove) {
		this.ownersToRemove = ownersToRemove;
	}
	
	public List<Identity> getOwnersToAdd() {
		return ownersToAdd;
	}
	
	public void setOwnersToAdd(List<Identity> ownersToAdd) {
		this.ownersToAdd = ownersToAdd;
	}
	
	public void setSendMail(boolean sendMail) {
		this.sendMail = sendMail;
	}
	
	public boolean isSendMail() {
		return sendMail;
	}
	
	public List<Identity> getAllIdentities(Locale locale) {
		List<Identity> identities = new ArrayList<>();
		
		identities.addAll(owners);
		
		for (Identity newOwner : ownersToAdd) {
			if (!identities.contains(newOwner)) {
				identities.add(newOwner);
			}
		}
		
		Collator collator = Collator.getInstance(locale);
		if (!identities.isEmpty()) {
			Collections.sort(identities, new Comparator<Identity>() {
		    	@Override
		        public int compare(Identity i1, Identity i2) {
		        	String name1 = i1.getUser().getFirstName() + i1.getUser().getLastName();
		        	String name2 = i2.getUser().getFirstName() + i2.getUser().getLastName();
		            return collator.compare(name1, name2);
		        }
			});
		}
		
		return identities;
	}
}
