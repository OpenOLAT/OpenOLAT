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

package org.olat.resource.accesscontrol.model;

import org.olat.resource.accesscontrol.provider.free.FreeAccessHandler;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;


/**
 *
 * Description:<br>
 * This a "static" payment method. There is only one instance.
 *
 * <P>
 * Initial Date:  27 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity(name="acfreemethod")
@DiscriminatorValue(value="free.method")
public class FreeAccessMethod extends AbstractAccessMethod {

	private static final long serialVersionUID = -6028245920419886453L;

	@Override
	public String getType() {
		return FreeAccessHandler.METHOD_TYPE;
	}

	@Override
	public String getMethodCssClass() {
		return FreeAccessHandler.METHOD_CSS_CLASS;
	}

	@Override
	public boolean isNeedUserInteraction() {
		return false;
	}

	@Override
	public boolean isPaymentMethod() {
		return false;
	}

	@Override
	public boolean isVisibleInGui() {
		return true;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 2489579 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof FreeAccessMethod method) {
			return getKey() != null && getKey().equals(method.getKey());
		}
		return false;
	}
}
