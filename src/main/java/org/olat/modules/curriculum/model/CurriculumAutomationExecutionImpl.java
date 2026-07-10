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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.id.Persistable;
import org.olat.modules.curriculum.AutomationExecutionResult;
import org.olat.modules.curriculum.CurriculumAutomationExecution;
import org.olat.modules.curriculum.CurriculumAutomationRule;

/**
 * Initial date: 2026-07-10<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
@Entity(name="curriculumautomationexecution")
@Table(name="o_cur_automation_execution")
public class CurriculumAutomationExecutionImpl implements Persistable, CurriculumAutomationExecution {

	private static final long serialVersionUID = 5892741063287451102L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@ManyToOne(targetEntity=CurriculumAutomationRuleImpl.class, cascade={CascadeType.PERSIST, CascadeType.REMOVE}, fetch=FetchType.LAZY)
	@JoinColumn(name="fk_rule", nullable=false, insertable=true, updatable=false)
	private CurriculumAutomationRule rule;

	@Column(name="fk_curriculum_element", nullable=false, insertable=true, updatable=false)
	private Long curriculumElementKey;

	@Column(name="fk_element_type", nullable=true, insertable=true, updatable=false)
	private Long elementTypeKey;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="c_execution_date", nullable=false, insertable=true, updatable=false)
	private Date executionDate;

	@Enumerated(EnumType.STRING)
	@Column(name="c_result", nullable=false, insertable=true, updatable=false)
	private AutomationExecutionResult result;

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
	public CurriculumAutomationRule getRule() {
		return rule;
	}

	public void setRule(CurriculumAutomationRule rule) {
		this.rule = rule;
	}

	@Override
	public Long getCurriculumElementKey() {
		return curriculumElementKey;
	}

	public void setCurriculumElementKey(Long curriculumElementKey) {
		this.curriculumElementKey = curriculumElementKey;
	}

	@Override
	public Long getElementTypeKey() {
		return elementTypeKey;
	}

	public void setElementTypeKey(Long elementTypeKey) {
		this.elementTypeKey = elementTypeKey;
	}

	@Override
	public Date getExecutionDate() {
		return executionDate;
	}

	public void setExecutionDate(Date executionDate) {
		this.executionDate = executionDate;
	}

	@Override
	public AutomationExecutionResult getResult() {
		return result;
	}

	public void setResult(AutomationExecutionResult result) {
		this.result = result;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 615398 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof CurriculumAutomationExecutionImpl execution) {
			return getKey() != null && getKey().equals(execution.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
