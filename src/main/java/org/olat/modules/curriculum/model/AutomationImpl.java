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
package org.olat.modules.curriculum.model;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import org.olat.modules.curriculum.Automation;
import org.olat.modules.curriculum.AutomationUnit;

/**
 * 
 * Initial date: 4 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Embeddable
public class AutomationImpl implements Automation, Serializable {
	
	private static final long serialVersionUID = 815811605737490273L;
	
	private Integer value;
	@Enumerated(EnumType.STRING)
	private AutomationUnit unit;
	
	public AutomationImpl() {
		//
	}
	
	public static final AutomationImpl valueOf(Integer value, AutomationUnit unit) {
		AutomationImpl auto = new AutomationImpl();
		auto.setValue(value);
		auto.setUnit(unit);
		return auto;
	}
	
	@Override
	public Integer getValue() {
		return value;
	}

	@Override
	public void setValue(Integer value) {
		this.value = value;
	}

	@Override
	public AutomationUnit getUnit() {
		return unit;
	}

	@Override
	public void setUnit(AutomationUnit unit) {
		this.unit = unit;
	}

	@Override
	public Date getDateBefore(Date referenceDate) {
		return date(referenceDate, true);
	}

	@Override
	public Date getDateAfter(Date referenceDate) {
		return date(referenceDate, false);
	}
	
	private final Date date(Date referenceDate, boolean before) {
		if(referenceDate == null || getUnit() == null) return null;
		
		AutomationUnit u = getUnit();
		if(u == AutomationUnit.SAME_DAY) {
			return referenceDate;
		}
		
		Integer val = getValue();
		if(val == null) {
			return null;
		}
		return before ? u.before(referenceDate, val.intValue()) : u.after(referenceDate, val.intValue());
	}
}
