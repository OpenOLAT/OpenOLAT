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
*/

package org.olat.upgrade.model;

import java.util.Date;
import java.util.Set;

import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryRef;
import org.olat.resource.OLATResource;

/**
 *Represents a repository entry.
 */
public class RepositoryEntryUpgrade extends PersistentObject implements ModifiedInfo, OLATResourceable, RepositoryEntryRef {

	private static final long serialVersionUID = 5319576295875289054L;
	// IMPORTANT: Keep relation ACC_OWNERS < ACC_OWNERS_AUTHORS < ACC_USERS < ACC_USERS_GUESTS
	/**
	 * limit access to owners
	 */
	public static final int ACC_OWNERS = 1; // limit access to owners
	/**
	 * limit access to owners and authors
	 */
	public static final int ACC_OWNERS_AUTHORS = 2; // limit access to owners and authors
	/**
	 * limit access to owners, authors and users
	 */
	public static final int ACC_USERS = 3; // limit access to owners, authors and users
	/**
	 * no limits
	 */
	public static final int ACC_USERS_GUESTS = 4; // no limits
	
	//fxdiff VCRP-1,2: access control of resources
	public static final String MEMBERS_ONLY =  "membersonly";
	
	private String softkey; // mandatory
	private OLATResource olatResource; // mandatory
	private SecurityGroup ownerGroup; // mandatory
	//fxdiff VCRP-1,2: access control of resources
	private SecurityGroup tutorGroup;
	private SecurityGroup participantGroup;
	private Set<RepositoryEntryUpgradeToGroupRelation> groups;
	private String resourcename; // mandatory
	private String displayname; // mandatory
	private String description; // mandatory
	private String initialAuthor; // mandatory // login of the author of the first version
	private String authors;
	
	private String externalId;
	private String externalRef;
	private String managedFlagsString;
	
	private int access;
	private boolean canCopy;
	private boolean canReference;
	private boolean canLaunch;
	private boolean canDownload;
	private boolean membersOnly;//fxdiff VCRP-1,2: access control of resources
	private int statusCode;
	private int version;
	private Date lastModified;
	
	@Override
	public String toString() {
		return super.toString()+" [resourcename="+resourcename+", version="+version+", description="+description+"]";
	}
	
	/**
	 * Default constructor.
	 */
	public RepositoryEntryUpgrade() {
		softkey = CodeHelper.getGlobalForeverUniqueID();
		access = ACC_OWNERS;
	}

	/**
	 * @return The softkey associated with this repository entry.
	 */
	public String getSoftkey() {
		return softkey;
	}
	
	/**
	 * Set the softkey of this repository entry.
	 * @param softkey
	 */
	public void setSoftkey(String softkey) {
		if (softkey.length() > 30)
			throw new AssertException("Trying to set a softkey which is too long...");
		this.softkey = softkey;
	}
	
	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return description as HTML snippet
	 */
	public String getFormattedDescription() {
		String descr = Formatter.formatLatexFormulas(getDescription());
		return descr;		
	}
	
	/**
	 * @return Returns the initialAuthor.
	 */
	public String getInitialAuthor() {
		return initialAuthor;
	}
	/**
	 * @param initialAuthor The initialAuthor to set.
	 */
	public void setInitialAuthor(String initialAuthor) {
		if (initialAuthor == null) initialAuthor = "";
		if (initialAuthor.length() > IdentityImpl.NAME_MAXLENGTH)
			throw new AssertException("initialAuthor is limited to "+IdentityImpl.NAME_MAXLENGTH+" characters.");
		this.initialAuthor = initialAuthor;
	}

	public String getAuthors() {
		return authors;
	}

	public void setAuthors(String authors) {
		this.authors = authors;
	}

	/**
	 * @return Returns the statusCode.
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * @param statusCode The statusCode to set.
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	/**
	 * @return Returns the name.
	 */
	public String getResourcename() {
		return resourcename;
	}
	/**
	 * @param name The name to set.
	 */
	public void setResourcename(String name) {
		if (name.length() > 100)
			throw new AssertException("resourcename is limited to 100 characters.");
		this.resourcename = name;
	}

	/**
	 * @return Returns the olatResource.
	 */
	public OLATResource getOlatResource() {
		return olatResource;
	}
	/**
	 * @param olatResource The olatResource to set.
	 */
	public void setOlatResource(OLATResource olatResource) {
		this.olatResource = olatResource;
	}
	
	/**
	 * @return Grou of owners of this repo entry.
	 */
	public SecurityGroup getOwnerGroup() {
		return ownerGroup;
	}
	
	/**
	 * Set the group of owners of this repo entry.
	 * @param ownerGroup
	 */
	public void setOwnerGroup(SecurityGroup ownerGroup) {
		this.ownerGroup = ownerGroup;
	}
	
	/**
	 * @return The group for tutors
	 */
	public SecurityGroup getTutorGroup() {
		return tutorGroup;
	}

	/**
	 * Set the group for tutors
	 * @param tutorGroup
	 */
	public void setTutorGroup(SecurityGroup tutorGroup) {
		this.tutorGroup = tutorGroup;
	}

	/**
	 * @return The group of participants
	 */
	public SecurityGroup getParticipantGroup() {
		return participantGroup;
	}

	/**
	 * Set the group of participants
	 * @param participantGroup
	 */
	public void setParticipantGroup(SecurityGroup participantGroup) {
		this.participantGroup = participantGroup;
	}

	public Set<RepositoryEntryUpgradeToGroupRelation> getGroups() {
		return groups;
	}

	public void setGroups(Set<RepositoryEntryUpgradeToGroupRelation> groups) {
		this.groups = groups;
	}

	/**
	 * @return Wether this repo entry can be copied.
	 */
	public boolean getCanCopy() {
		return canCopy;
	}

	/**
	 * @return Wether this repo entry can be referenced by other people.
	 */
	public boolean getCanReference() {
		return canReference;
	}

	/**
	 * @return Wether this repo entry can be downloaded.
	 */
	public boolean getCanDownload() {
		return canDownload;
	}

	/**
	 * @return Wether this repo entry can be launched.
	 */
	public boolean getCanLaunch() {
		return canLaunch;
	}

	/**
	 * @return Access restrictions.
	 */
	public int getAccess() {
		return access;
	}
	
	/**
	 * Is the repository entry exclusive
	 * @return
	 */
	public boolean isMembersOnly() {
		return membersOnly;
	}

	/**
	 * @param b
	 */
	public void setCanCopy(boolean b) {
		canCopy = b;
	}

	/**
	 * @param b
	 */
	public void setCanReference(boolean b) {
		canReference = b;
	}

	/**
	 * @param b
	 */
	public void setCanDownload(boolean b) {
		canDownload = b;
	}

	/**
	 * @param b
	 */
	public void setCanLaunch(boolean b) {
		canLaunch = b;
	}

	/**
	 * Set access restrictions.
	 * @param i
	 */
	public void setAccess(int i) {
		access = i;
	}
	
	/**
	 * Set if the repository entry is exclusive 
	 * @param membersOnly
	 */
	public void setMembersOnly(boolean membersOnly) {
		this.membersOnly = membersOnly;
	}
	
	/**
	 * @return Returns the displayname.
	 */
	public String getDisplayname() {
		return displayname;
	}
	/**
	 * @param displayname The displayname to set.
	 */
	public void setDisplayname(String displayname) {
		if (displayname.length() > 255)
			throw new AssertException("DisplayName is limited to 255 characters.");
		this.displayname = displayname;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getExternalRef() {
		return externalRef;
	}

	public void setExternalRef(String externalRef) {
		this.externalRef = externalRef;
	}

	public RepositoryEntryManagedFlag[] getManagedFlags() {
		return RepositoryEntryManagedFlag.toEnum(managedFlagsString);
	}


	public String getManagedFlagsString() {
		return managedFlagsString;
	}

	public void setManagedFlagsString(String managedFlagsString) {
		this.managedFlagsString = managedFlagsString;
	}

	/**
	 * @see org.olat.core.id.OLATResourceablegetResourceableTypeName()
	 */
	public String getResourceableTypeName() { 
		return OresHelper.calculateTypeName(RepositoryEntry.class); 
	}

	/**
	 * @see org.olat.core.id.OLATResourceablegetResourceableId()
	 */
	public Long getResourceableId() {
		return getKey();
	}

	public int getVersion() {
		return version;
	}
	
	public void setVersion(int v) {
		version = v;
	}

	/**
	 * 
	 * @see org.olat.core.id.ModifiedInfo#getLastModified()
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * 
	 * @see org.olat.core.id.ModifiedInfo#setLastModified(java.util.Date)
	 */
	public void setLastModified(Date date) {
		this.lastModified = date;
	}
	
	
	@Override
	public int hashCode() {
		return getKey() == null ? 293485 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof RepositoryEntry) {
			RepositoryEntry re = (RepositoryEntry)obj;
			return getKey() != null && getKey().equals(re.getKey());
		}
		return false;
	}
}