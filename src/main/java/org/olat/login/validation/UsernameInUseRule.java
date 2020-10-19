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
package org.olat.login.validation;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 19 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class UsernameInUseRule extends DescriptionRule {

	UsernameInUseRule(ValidationDescription description) {
		super(description);
	}

	@Override
	public boolean validate(String value, Identity identity) {
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		Identity byLogin = securityManager.findIdentityByLogin(value);
		if(byLogin != null && !byLogin.equals(identity)) {
			return false;
		}
		Identity byName = securityManager.findIdentityByNameCaseInsensitive(value);
		if(byName != null && !byName.equals(identity)) {
			return false;
		}
		Identity byNickname = securityManager.findIdentityByNickName(value);
		return byNickname == null || byNickname.equals(identity);
	}

	@Override
	public boolean isIdentityRule() {
		return false;
	}

}
