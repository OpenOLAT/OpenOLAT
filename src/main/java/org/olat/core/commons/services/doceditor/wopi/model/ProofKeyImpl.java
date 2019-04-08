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
package org.olat.core.commons.services.doceditor.wopi.model;

import org.olat.core.commons.services.doceditor.wopi.ProofKey;

/**
 * 
 * Initial date: 1 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProofKeyImpl implements ProofKey {
	
	private String oldValue;
	private String oldModulus;
	private String oldExponent;
	private String value;
	private String modulus;
	private String exponent;

	@Override
	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	@Override
	public String getOldModulus() {
		return oldModulus;
	}

	public void setOldModulus(String oldModulus) {
		this.oldModulus = oldModulus;
	}

	@Override
	public String getOldExponent() {
		return oldExponent;
	}

	public void setOldExponent(String oldExponent) {
		this.oldExponent = oldExponent;
	}

	@Override
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String getModulus() {
		return modulus;
	}

	public void setModulus(String modulus) {
		this.modulus = modulus;
	}
	
	@Override
	public String getExponent() {
		return exponent;
	}
	
	public void setExponent(String exponent) {
		this.exponent = exponent;
	}
	

}
