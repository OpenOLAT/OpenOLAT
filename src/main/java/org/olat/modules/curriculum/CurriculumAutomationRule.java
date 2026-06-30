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
package org.olat.modules.curriculum;

import java.util.HashSet;
import java.util.Set;

import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * Initial date: 2026-06-26<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class CurriculumAutomationRule {

	public static final String REFERENCE_BEGIN = "BEGIN";
	public static final String REFERENCE_END = "END";

	private AutomationContext context;
	private AutomationType automationType;
	private String targetStatus;
	private boolean enabled;
	private AutomationDependingOn dependingOn;
	private String reference;
	private Integer value;
	private AutomationUnit unit;
	private OffsetDirection direction;
	private Set<String> dependingOnStatus;
	private Set<String> onlyWhenStatus;

	public CurriculumAutomationRule() {
	}

	public AutomationContext getContext() {
		return context;
	}

	public void setContext(AutomationContext context) {
		this.context = context;
	}

	public AutomationType getAutomationType() {
		return automationType;
	}

	public void setAutomationType(AutomationType automationType) {
		this.automationType = automationType;
	}

	public String getTargetStatus() {
		return targetStatus;
	}

	public void setTargetStatus(String targetStatus) {
		this.targetStatus = targetStatus;
	}

	public void setTargetStatus(CurriculumElementStatus status) {
		this.targetStatus = status.name();
	}

	public void setTargetStatus(RepositoryEntryStatusEnum status) {
		this.targetStatus = status.name();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public AutomationDependingOn getDependingOn() {
		return dependingOn;
	}

	public void setDependingOn(AutomationDependingOn dependingOn) {
		this.dependingOn = dependingOn;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public AutomationUnit getUnit() {
		return unit;
	}

	public void setUnit(AutomationUnit unit) {
		this.unit = unit;
	}

	public OffsetDirection getDirection() {
		return direction;
	}

	public void setDirection(OffsetDirection direction) {
		this.direction = direction;
	}

	public Set<String> getDependingOnStatus() {
		return dependingOnStatus;
	}

	public void setDependingOnStatus(Set<String> dependingOnStatus) {
		this.dependingOnStatus = dependingOnStatus != null ? new HashSet<>(dependingOnStatus) : null;
	}

	public Set<String> getOnlyWhenStatus() {
		return onlyWhenStatus;
	}

	public void setOnlyWhenStatus(Set<String> onlyWhenStatus) {
		this.onlyWhenStatus = onlyWhenStatus != null ? new HashSet<>(onlyWhenStatus) : null;
	}
	
	public static Object toStatusEnum(String statusString) {
		if (statusString == null) {
			return null;
		}
		if (CurriculumElementStatus.isValueOf(statusString)) {
			return CurriculumElementStatus.valueOf(statusString);
		}
		if (RepositoryEntryStatusEnum.isValid(statusString)) {
			return RepositoryEntryStatusEnum.valueOf(statusString);
		}
		return statusString;
	}
}
