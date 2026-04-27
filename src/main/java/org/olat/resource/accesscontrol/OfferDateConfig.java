/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.resource.accesscontrol;

/**
 * Stores the relative date configuration for an offer's From and Until boundaries.
 * Serialized as XML via {@link OfferDateConfigXStream} into the {@code valid_date_config} column.
 *
 * Initial date: 24.04.2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class OfferDateConfig {

	private Integer fromValue;
	private OfferDateUnit fromUnit;
	private OfferDateRef fromRef;
	private Integer toValue;
	private OfferDateUnit toUnit;
	private OfferDateRef toRef;

	public Integer getFromValue() {
		return fromValue;
	}

	public void setFromValue(Integer fromValue) {
		this.fromValue = fromValue;
	}

	public OfferDateUnit getFromUnit() {
		return fromUnit;
	}

	public void setFromUnit(OfferDateUnit fromUnit) {
		this.fromUnit = fromUnit;
	}

	public OfferDateRef getFromRef() {
		return fromRef;
	}

	public void setFromRef(OfferDateRef fromRef) {
		this.fromRef = fromRef;
	}

	public Integer getToValue() {
		return toValue;
	}

	public void setToValue(Integer toValue) {
		this.toValue = toValue;
	}

	public OfferDateUnit getToUnit() {
		return toUnit;
	}

	public void setToUnit(OfferDateUnit toUnit) {
		this.toUnit = toUnit;
	}

	public OfferDateRef getToRef() {
		return toRef;
	}

	public void setToRef(OfferDateRef toRef) {
		this.toRef = toRef;
	}

}
