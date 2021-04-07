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
package org.olat.registration;

import java.util.Collections;
import java.util.Set;

import org.olat.core.id.Identity;
import org.olat.resource.accesscontrol.provider.auto.AdvanceOrderInput;
import org.olat.resource.accesscontrol.provider.auto.IdentifierKey;
import org.olat.resource.accesscontrol.provider.auto.manager.SemicolonSplitter;
import org.olat.resource.accesscontrol.provider.auto.model.AutoAccessMethod;

/**
 * Initial date: 05.04.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class SelfRegistrationAdvanceOrderInput implements AdvanceOrderInput {

	private Identity identity;
	private String rawValues;
	
	@Override
	public Class<? extends AutoAccessMethod> getMethodClass() {
		return SelfRegistrationAutoAccessMethod.class;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	@Override
	public Set<IdentifierKey> getKeys() {
		return Collections.singleton(IdentifierKey.internalId);
	}

	@Override
	public String getRawValues() {
		return rawValues;
	}
	
	public void setRawValues(String rawValues) {
		this.rawValues = rawValues;
	}

	@Override
	public String getSplitterType() {
		return SemicolonSplitter.TYPE;
	}

}
