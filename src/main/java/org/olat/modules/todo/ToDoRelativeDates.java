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
package org.olat.modules.todo;

import java.util.Objects;

/**
 *
 * Initial date: 7 May 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class ToDoRelativeDates {

	private Integer startValue;
	private ToDoDateUnit startUnit;
	private String startRef;
	private Integer dueValue;
	private ToDoDateUnit dueUnit;
	private String dueRef;

	public Integer getStartValue() {
		return startValue;
	}

	public void setStartValue(Integer startValue) {
		this.startValue = startValue;
	}

	public ToDoDateUnit getStartUnit() {
		return startUnit;
	}

	public void setStartUnit(ToDoDateUnit startUnit) {
		this.startUnit = startUnit;
	}

	public String getStartRef() {
		return startRef;
	}

	public void setStartRef(String startRef) {
		this.startRef = startRef;
	}

	public Integer getDueValue() {
		return dueValue;
	}

	public void setDueValue(Integer dueValue) {
		this.dueValue = dueValue;
	}

	public ToDoDateUnit getDueUnit() {
		return dueUnit;
	}

	public void setDueUnit(ToDoDateUnit dueUnit) {
		this.dueUnit = dueUnit;
	}

	public String getDueRef() {
		return dueRef;
	}

	public void setDueRef(String dueRef) {
		this.dueRef = dueRef;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ToDoRelativeDates other)) return false;
		return Objects.equals(startValue, other.startValue)
				&& startUnit == other.startUnit
				&& Objects.equals(startRef, other.startRef)
				&& Objects.equals(dueValue, other.dueValue)
				&& dueUnit == other.dueUnit
				&& Objects.equals(dueRef, other.dueRef);
	}

	@Override
	public int hashCode() {
		return Objects.hash(startValue, startUnit, startRef, dueValue, dueUnit, dueRef);
	}
	
	public static ToDoRelativeDates copy(ToDoRelativeDates source) {
		if (source == null) return null;
		ToDoRelativeDates copy = new ToDoRelativeDates();
		copy.setStartValue(source.getStartValue());
		copy.setStartUnit(source.getStartUnit());
		copy.setStartRef(source.getStartRef());
		copy.setDueValue(source.getDueValue());
		copy.setDueUnit(source.getDueUnit());
		copy.setDueRef(source.getDueRef());
		return copy;
	}

}
