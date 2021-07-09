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
package org.olat.basesecurity.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.AuthenticationImpl;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.FindNamedIdentity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25 oct. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class IdentityDAO {
	
	@Autowired
	private DB dbInstance;
	
	public Identity findIdentityByName(String identityName) {
		if (identityName == null) throw new AssertException("findIdentitybyName: name was null");

		StringBuilder sb = new StringBuilder(128);
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident")
		  .append(" inner join fetch ident.user user")
		  .append(" where ident.name=:username");
		
		List<Identity> identities = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("username", identityName)
				.getResultList();
		
		if(identities.isEmpty()) {
			return null;
		}
		return identities.get(0);
	}
	
	public List<Identity> findIdentitiesByNickName(String name) {
		if(!StringHelper.containsNonWhitespace(name)) return new ArrayList<>();

		StringBuilder sb = new StringBuilder(128);
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident")
		  .append(" inner join fetch ident.user user")
		  .append(" where user.nickName=:username");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("username", name)
				.getResultList();
	}
	
	public List<Identity> findByUsernames(String username) {
		QueryBuilder sb = new QueryBuilder(512);
		sb.append("select ident from ").append(IdentityImpl.class.getName()).append(" as ident")
		  .append(" left join ").append(AuthenticationImpl.class.getName()).append(" as auth on (auth.identity.key=ident.key)")
		  .append(" inner join fetch ident.user user")
		  .append(" where ").lowerEqual("ident.name").append(":username")
		  .append(" or ").lowerEqual("auth.authusername").append(":username")
		  .append(" or ").lowerEqual("user.nickName").append(":username");
		
		List<Identity> identities = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Identity.class)
				.setParameter("username", username.toLowerCase())
				.getResultList();
		// deduplicate in Java, quicker than a distinct
		return new ArrayList<>(new HashSet<>(identities));
	}
	
	public List<FindNamedIdentity> findByNames(Collection<String> names, List<Organisation> organisations) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ident, auth from ").append(IdentityImpl.class.getName()).append(" as ident")
		  .append(" left join ").append(AuthenticationImpl.class.getName()).append(" as auth on (auth.identity.key=ident.key)")
		  .append(" inner join fetch ident.user user")
		  .append(" where (lower(ident.name) in (:names)")
		  .append(" or lower(auth.authusername) in (:names)")
		  .append(" or lower(concat(user.firstName,' ', user.lastName)) in (:names)")
		  .append(" or lower(user.nickName) in (:names)")
		  .append(" or lower(user.email) in (:names)")
		  .append(" or lower(user.institutionalEmail) in (:names)")
		  .append(" or lower(user.institutionalUserIdentifier) in (:names))");
		
		List<Long> organisationKeys = null;
		if(organisations != null && !organisations.isEmpty()) {
			sb.append(" and exists (select orgtomember.key from bgroupmember as orgtomember ")
			  .append("  inner join organisation as org on (org.group.key=orgtomember.group.key)")
			  .append("  where orgtomember.identity.key=ident.key and org.key in (:organisationKey))");
			
			organisationKeys = organisations.stream()
					.map(Organisation::getKey)
					.collect(Collectors.toList());
		}

		List<String> loweredIdentityNames = names.stream()
				.map(String::toLowerCase).collect(Collectors.toList());
		
		Set<String> loweredIdentityNamesSet = new HashSet<>(loweredIdentityNames);
		Map<Identity, FindNamedIdentity> namedIdentities = new HashMap<>();
		for (List<String> chunkOfIdentityNames : PersistenceHelper.collectionOfChunks(new ArrayList<>(loweredIdentityNames), 7)) {
			TypedQuery<Object[]> rawQuery = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Object[].class)
					.setParameter("names", chunkOfIdentityNames);
			if(organisations != null && !organisations.isEmpty()) {
				rawQuery.setParameter("organisationKey", organisationKeys);
			}

			List<Object[]> rawObjects =	rawQuery.getResultList();
			for(Object[] rawObject:rawObjects) {
				Identity identity = (Identity)rawObject[0];
				Authentication authentication = (Authentication)rawObject[1];
				FindNamedIdentity namedIdentity = namedIdentities
						.computeIfAbsent(identity, FindNamedIdentity::new);
				appendName(namedIdentity, authentication, loweredIdentityNamesSet);
			}
		}
		
		return new ArrayList<>(namedIdentities.values());
	}
	
	private void appendName(FindNamedIdentity namedIdentity, Authentication authentication, Set<String> names) {
		if(authentication != null) {
			String authUsername = authentication.getAuthusername().toLowerCase();
			if(names.contains(authUsername)) {
				namedIdentity.addName(authentication.getAuthusername());
			}
		}
		
		Identity identity = namedIdentity.getIdentity();
		if(names.contains(identity.getName().toLowerCase())) {
			namedIdentity.addName(identity.getName());
		}
		
		User user =  identity.getUser();
		StringBuilder sb = new StringBuilder();
		if(StringHelper.containsNonWhitespace(user.getFirstName())) {
			sb.append(user.getFirstName());
		}
		sb.append(" ");
		if(StringHelper.containsNonWhitespace(user.getLastName())) {
			sb.append(user.getLastName());
		}
		
		String fullName = sb.toString();
		if(names.contains(fullName.toLowerCase())) {
			namedIdentity.addName(fullName);
		}
		
		if(StringHelper.containsNonWhitespace(user.getEmail())
				&& names.contains(user.getEmail().toLowerCase())) {
			namedIdentity.addName(user.getEmail());
		}
		
		if(StringHelper.containsNonWhitespace(user.getInstitutionalEmail())
				&& names.contains(user.getInstitutionalEmail().toLowerCase())) {
			namedIdentity.addName(user.getInstitutionalEmail());
		}
		
		if(StringHelper.containsNonWhitespace(user.getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null))
				&& names.contains(user.getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null).toLowerCase())) {
			namedIdentity.addName(user.getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, null));
		}
		
		if(StringHelper.containsNonWhitespace(user.getProperty(UserConstants.NICKNAME, null))
				&& names.contains(user.getProperty(UserConstants.NICKNAME, null).toLowerCase())) {
			namedIdentity.addName(user.getProperty(UserConstants.NICKNAME, null));
		}
	}
	
	public void setIdentityLastLogin(IdentityRef identity, Date lastLogin) {
		dbInstance.getCurrentEntityManager()
				.createNamedQuery("updateIdentityLastLogin")
				.setParameter("identityKey", identity.getKey())
				.setParameter("now", lastLogin)
				.executeUpdate();
	}
	
	public Identity saveIdentity(Identity identity) {
		return dbInstance.getCurrentEntityManager().merge(identity);
	}
	
	

}
