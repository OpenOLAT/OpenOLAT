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
* Copyright (c) 2008 frentix GmbH, Switzerland<br>
* <p>
*/

package org.olat.resource.accesscontrol.provider.paypal.model;

import org.olat.resource.accesscontrol.model.AbstractAccessMethod;
import org.olat.resource.accesscontrol.provider.paypal.PaypalAccessHandler;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;


/**
 *
 * Description:<br>
 * This a paypal payment method.
 *
 * <P>
 * Initial Date:  18 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity(name="acpaypalmethod")
@DiscriminatorValue(value="paypal.method")
public class PaypalAccessMethod extends AbstractAccessMethod {

	private static final long serialVersionUID = 7682228653442368290L;

	@Override
	public String getType() {
		return PaypalAccessHandler.METHOD_TYPE;
	}

	@Override
	public String getMethodCssClass() {
		return PaypalAccessHandler.METHOD_CSS_CLASS;
	}

	@Override
	public boolean isNeedUserInteraction() {
		return true;
	}

	@Override
	public boolean isPaymentMethod() {
		return true;
	}

	@Override
	public boolean isVisibleInGui() {
		return true;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? -130581 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PaypalAccessMethod method) {
			return getKey() != null && getKey().equals(method.getKey());
		}
		return false;
	}
}
