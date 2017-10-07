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
package org.olat.resource.accesscontrol.provider.auto.manager;

import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.provider.auto.AdvanceOrderInput;
import org.springframework.stereotype.Component;

/**
 * Helper to validate the values of the AdvanceOrderInput.
 *
 * Initial date: 17.08.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
class InputValidator {

	boolean isValid(AdvanceOrderInput input) {
		boolean isValid = true;

		if (input == null) {
			isValid &= false;
		} else {
			isValid &= notNull(input.getIdentity());
			isValid &= notNull(input.getMethodClass());
			isValid &= StringHelper.containsNonWhitespace(input.getRawValues());

			if (input.getKeys() == null) {
				isValid &= false;
			} else {
				isValid &= input.getKeys().isEmpty() ? false: true;
			}
		}

		return isValid;
	}

	private boolean notNull(Object input) {
		return input == null? false: true;
	}
}
