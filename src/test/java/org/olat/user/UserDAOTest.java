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
package org.olat.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27.10.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UserDAOTest extends OlatTestCase {
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserLifecycleManager userLifecycleManager;
	
	@Autowired
	private UserDAO sut;
	
	@Test
	public void shouldReturnUniqueUserIfFound() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao");
		String email = identity.getUser().getEmail();
		
		Identity foundIdentity = sut.findUniqueIdentityByEmail(email);
		
		assertThat(foundIdentity).isNotNull().isEqualTo(identity);
	}
	
	@Test
	public void shouldReturnUniqueUserIfFoundLowerUpperCase() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("userlow");
		String email = identity.getUser().getEmail().toUpperCase();
		
		Identity foundIdentity = sut.findUniqueIdentityByEmail(email);
		
		assertThat(foundIdentity).isNotNull().isEqualTo(identity);
	}
	
	@Test
	public void shouldReturnUniqueUserIfFoundInInstitutionalEmail() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao");
		String institutionalEmail = UUID.randomUUID() + "@trashmail.com";
		User user = identity.getUser();
		user.setProperty(UserConstants.INSTITUTIONALEMAIL, institutionalEmail);
		userManager.updateUser(identity, user);
		
		Identity foundIdentity = sut.findUniqueIdentityByEmail(institutionalEmail);
		
		assertThat(foundIdentity).isNotNull().isEqualTo(identity);
	}
	
	@Test
	public void shouldNotReturnUniqueUserIfNotFound() {
		Identity identity = sut.findUniqueIdentityByEmail("notPresent");
		
		assertThat(identity).isNull();
	}
	
	@Test
	public void shouldNotReturnUniqueUserIfManyFound() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao");
		String email = identity.getUser().getEmail();
		User userWithSameEmail = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao").getUser();
		userWithSameEmail.setProperty(UserConstants.EMAIL, email);
		userManager.updateUser(identity, userWithSameEmail);
		
		Identity foundIdentity = sut.findUniqueIdentityByEmail(email);
		
		assertThat(foundIdentity).isNull();
	}
	
	@Test
	public void shouldNotReturnUniqueUserIfManyFoundInInstitutionalEmail() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao");
		String email = identity.getUser().getEmail();
		User userWithInstitutionalEmail = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao").getUser();
		userWithInstitutionalEmail.setProperty(UserConstants.INSTITUTIONALEMAIL, email);
		userManager.updateUser(identity, userWithInstitutionalEmail);
		
		Identity foundIdentity = sut.findUniqueIdentityByEmail(email);
		
		assertThat(foundIdentity).isNull();		
	}
	
	@Test
	public void shouldReturnUniqueUserIfFoundExactMatch() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao");
		String email = identity.getUser().getEmail();
		User userWithSameEmail = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao").getUser();
		userWithSameEmail.setProperty(UserConstants.EMAIL, email + "suffix");
		userManager.updateUser(identity, userWithSameEmail);
		
		Identity foundIdentity = sut.findUniqueIdentityByEmail(email);
		
		assertThat(foundIdentity).isNotNull().isEqualTo(identity);
	}
	
	@Test
	public void shouldReturnIfEmailIsInUse() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao");
		String email = identity.getUser().getEmail();
		
		boolean isInUse = sut.isEmailInUse(email);
		
		assertThat(isInUse).isTrue();
	}
	
	@Test
	public void shouldReturnIfEmailIsNotInUse() {
		boolean isInUse = sut.isEmailInUse("notInUse");
		
		assertThat(isInUse).isFalse();
	}
			
	@Test
	public void shouldReturnIfEmailIsInInstitutionalEmailInUse() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao");
		User user = identity.getUser();
		String institutionalEmail = "INSTITUTION@openolat.org";
		user.setProperty(UserConstants.INSTITUTIONALEMAIL, institutionalEmail);
		userManager.updateUser(identity, user);
		
		boolean isInUse = sut.isEmailInUse(institutionalEmail);
		
		assertThat(isInUse).isTrue();
	}
	
	@Test
	public void shouldReturnIfEmailIsInUseCaseInsensitive() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao");
		String email = identity.getUser().getEmail();
		
		boolean isInUse = sut.isEmailInUse(email.toUpperCase());
		
		assertThat(isInUse).isTrue();
	}
	
	@Test
	public void shouldReturnIdentitiesWithoutEmail() {
		Identity identityWithoutEmail1 = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao");
		User userWithoutEmail1 = identityWithoutEmail1.getUser();
		userWithoutEmail1.setProperty(UserConstants.EMAIL, null);
		userManager.updateUser(identityWithoutEmail1, userWithoutEmail1);
		
		Identity identityWithoutEmailDeleted = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao");
		User userWithoutEmailDeleted= identityWithoutEmailDeleted.getUser();
		userWithoutEmailDeleted.setProperty(UserConstants.EMAIL, null);
		userManager.updateUser(identityWithoutEmailDeleted, userWithoutEmailDeleted);
		userLifecycleManager.deleteIdentity(identityWithoutEmailDeleted, null);
		
		Identity identityWithoutEmail2 = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao");
		User userWithoutEmail2 = identityWithoutEmail2.getUser();
		userWithoutEmail2.setProperty(UserConstants.EMAIL, null);
		userManager.updateUser(identityWithoutEmail2, userWithoutEmail2);
		
		Identity identityWithEmail = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao");
		
		List<Identity> identtitiesWithoutEmail = sut.findVisibleIdentitiesWithoutEmail();
		
		assertThat(identtitiesWithoutEmail).contains(identityWithoutEmail1, identityWithoutEmail2)
				.doesNotContain(identityWithoutEmailDeleted, identityWithEmail);
	}
	
	@Test
	public void shouldReturnIdentitiWithDuplicateEmail() {
		String emailDuplicate = "duplicate@trashmail.com";
		Identity identityEmailDuplicate1 = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao");
		User userEmailDuplicate1 = identityEmailDuplicate1.getUser();
		userEmailDuplicate1.setProperty(UserConstants.EMAIL, emailDuplicate);
		userManager.updateUser(identityEmailDuplicate1, userEmailDuplicate1);
		
		Identity identityWithUniqueEmail = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao");
		
		Identity identityEmailDuplicate2 = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao");
		User userEmailDuplicate2 = identityEmailDuplicate2.getUser();
		userEmailDuplicate2.setProperty(UserConstants.EMAIL, emailDuplicate);
		userManager.updateUser(identityEmailDuplicate2, userEmailDuplicate2);
		
		Identity identityEmailDuplicateDeleted = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao");
		User userEmailDuplicateDeleted = identityEmailDuplicateDeleted.getUser();
		userEmailDuplicateDeleted.setProperty(UserConstants.EMAIL, emailDuplicate);
		userManager.updateUser(identityEmailDuplicateDeleted, userEmailDuplicateDeleted);
		userLifecycleManager.deleteIdentity(identityEmailDuplicateDeleted, null);

		Identity identityEmailDuplicate3 = JunitTestHelper.createAndPersistIdentityAsRndUser("userdao");
		User userEmailDuplicate3 = identityEmailDuplicate3.getUser();
		userEmailDuplicate3.setProperty(UserConstants.EMAIL, emailDuplicate);
		userManager.updateUser(identityEmailDuplicate3, userEmailDuplicate3);
		
		List<Identity> identitiesWithDuplicateEmail = sut.findVisibleIdentitiesWithEmailDuplicates();
		
		assertThat(identitiesWithDuplicateEmail)
				.contains(identityEmailDuplicate1, identityEmailDuplicate2, identityEmailDuplicate3)
				.doesNotContain(identityEmailDuplicateDeleted, identityWithUniqueEmail);
	}
}
