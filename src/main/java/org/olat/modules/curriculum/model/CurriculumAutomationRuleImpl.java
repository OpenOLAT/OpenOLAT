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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.core.id.Persistable;
import org.olat.modules.curriculum.AutomationContext;
import org.olat.modules.curriculum.AutomationDependingOn;
import org.olat.modules.curriculum.AutomationType;
import org.olat.modules.curriculum.AutomationUnit;
import org.olat.modules.curriculum.CurriculumAutomationRule;

/**
 * Initial date: 2026-07-10<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
@Entity(name="curriculumautomationrule")
@Table(name="o_cur_automation_rule")
public class CurriculumAutomationRuleImpl implements Persistable, CurriculumAutomationRule {

	private static final long serialVersionUID = -8462671938264113987L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@Enumerated(EnumType.STRING)
	@Column(name="c_context", nullable=true, insertable=true, updatable=true)
	private AutomationContext context;

	@Enumerated(EnumType.STRING)
	@Column(name="c_automation_type", nullable=true, insertable=true, updatable=true)
	private AutomationType automationType;

	@Column(name="c_target_status", nullable=true, insertable=true, updatable=true)
	private String targetStatus;

	@Enumerated(EnumType.STRING)
	@Column(name="c_depending_on", nullable=true, insertable=true, updatable=true)
	private AutomationDependingOn dependingOn;

	@Column(name="c_reference", nullable=true, insertable=true, updatable=true)
	private String reference;

	@Column(name="c_value", nullable=true, insertable=true, updatable=true)
	private Integer value;

	@Enumerated(EnumType.STRING)
	@Column(name="c_unit", nullable=true, insertable=true, updatable=true)
	private AutomationUnit unit;

	@Enumerated(EnumType.STRING)
	@Column(name="c_direction", nullable=true, insertable=true, updatable=true)
	private OffsetDirection direction;

	@Column(name="c_depending_on_status", nullable=true, insertable=true, updatable=true)
	private String dependingOnStatus;

	@Column(name="c_only_when_status", nullable=true, insertable=true, updatable=true)
	private String onlyWhenStatus;

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public AutomationContext getContext() {
		return context;
	}

	@Override
	public void setContext(AutomationContext context) {
		this.context = context;
	}

	@Override
	public AutomationType getAutomationType() {
		return automationType;
	}

	@Override
	public void setAutomationType(AutomationType automationType) {
		this.automationType = automationType;
	}

	@Override
	public String getTargetStatus() {
		return targetStatus;
	}

	@Override
	public void setTargetStatus(String targetStatus) {
		this.targetStatus = targetStatus;
	}

	@Override
	public AutomationDependingOn getDependingOn() {
		return dependingOn;
	}

	@Override
	public void setDependingOn(AutomationDependingOn dependingOn) {
		this.dependingOn = dependingOn;
	}

	@Override
	public String getReference() {
		return reference;
	}

	@Override
	public void setReference(String reference) {
		this.reference = reference;
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
	public OffsetDirection getDirection() {
		return direction;
	}

	@Override
	public void setDirection(OffsetDirection direction) {
		this.direction = direction;
	}

	@Override
	public Set<String> getDependingOnStatus() {
		return toSet(dependingOnStatus);
	}

	@Override
	public void setDependingOnStatus(Set<String> dependingOnStatus) {
		this.dependingOnStatus = toCsv(dependingOnStatus);
	}

	@Override
	public Set<String> getOnlyWhenStatus() {
		return toSet(onlyWhenStatus);
	}

	@Override
	public void setOnlyWhenStatus(Set<String> onlyWhenStatus) {
		this.onlyWhenStatus = toCsv(onlyWhenStatus);
	}

	private static Set<String> toSet(String csv) {
		Set<String> values = new HashSet<>();
		if (csv != null && !csv.isBlank()) {
			for (String value : csv.split(",")) {
				if (!value.isBlank()) {
					values.add(value);
				}
			}
		}
		return values;
	}

	private static String toCsv(Set<String> values) {
		if (values == null || values.isEmpty()) {
			return null;
		}
		return String.join(",", new HashSet<>(values));
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 947231 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof CurriculumAutomationRuleImpl rule) {
			return getKey() != null && getKey().equals(rule.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	public static CurriculumAutomationRuleImpl copyOf(CurriculumAutomationRule sourceRule, Date creationDate) {
		CurriculumAutomationRuleImpl rule = new CurriculumAutomationRuleImpl();
		rule.setCreationDate(creationDate);
		rule.setContext(sourceRule.getContext());
		rule.setAutomationType(sourceRule.getAutomationType());
		rule.setTargetStatus(sourceRule.getTargetStatus());
		rule.setDependingOn(sourceRule.getDependingOn());
		rule.setReference(sourceRule.getReference());
		rule.setValue(sourceRule.getValue());
		rule.setUnit(sourceRule.getUnit());
		rule.setDirection(sourceRule.getDirection());
		rule.setDependingOnStatus(sourceRule.getDependingOnStatus());
		rule.setOnlyWhenStatus(sourceRule.getOnlyWhenStatus());
		return rule;
	}
}
