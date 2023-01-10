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
package org.olat.shibboleth.manager;

import org.olat.resource.accesscontrol.provider.auto.model.AutoAccessMethod;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 *
 * Initial date: 17.08.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="acautomethod")
@DiscriminatorValue(value="auto.shib.method")
public class ShibbolethAutoAccessMethod extends AutoAccessMethod {

	private static final long serialVersionUID = -241494885573765862L;

	@Override
	public String getType() {
		return ShibbolethAutoAccessHandler.METHOD_TYPE;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 238490 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ShibbolethAutoAccessMethod method) {
			return getKey() != null && getKey().equals(method.getKey());
		}
		return false;
	}
}
