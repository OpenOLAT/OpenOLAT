/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.commons.info.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageToCurriculumElement;
import org.olat.core.id.Persistable;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.model.CurriculumElementImpl;

/**
 * Initial date: Mai 09, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Entity(name = "infomessagetocurriculumelement")
@Table(name = "o_info_message_to_cur_el")
public class InfoMessageToCurriculumElementImpl implements Persistable, InfoMessageToCurriculumElement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false, unique = true, updatable = false)
	private Long key;
	@ManyToOne(targetEntity = InfoMessageImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_info_message_id", nullable = false, updatable = false)
	private InfoMessage infoMessage;
	@ManyToOne(targetEntity = CurriculumElementImpl.class, fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "fk_cur_element_id", nullable = false, updatable = false)
	private CurriculumElement curriculumElement;

	@Override
	public InfoMessage getInfoMessage() {
		return infoMessage;
	}

	public void setInfoMessage(InfoMessage infoMessage) {
		this.infoMessage = infoMessage;
	}

	@Override
	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}

	public void setCurriculumElement(CurriculumElement curriculumElement) {
		this.curriculumElement = curriculumElement;
	}

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
