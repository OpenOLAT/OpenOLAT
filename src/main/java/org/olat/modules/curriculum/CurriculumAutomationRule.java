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

import java.util.Set;

import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Persistable;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * Initial date: 2026-07-10<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public interface CurriculumAutomationRule extends Persistable, CreateInfo {

	public static final String REFERENCE_BEGIN = "BEGIN";
	public static final String REFERENCE_END = "END";

	public AutomationContext getContext();

	public void setContext(AutomationContext context);

	public AutomationType getAutomationType();

	public void setAutomationType(AutomationType automationType);

	public String getTargetStatus();

	public void setTargetStatus(String targetStatus);

	public default void setTargetStatus(CurriculumElementStatus status) {
		setTargetStatus(status.name());
	}

	public default void setTargetStatus(RepositoryEntryStatusEnum status) {
		setTargetStatus(status.name());
	}

	public AutomationDependingOn getDependingOn();

	public void setDependingOn(AutomationDependingOn dependingOn);

	public String getReference();

	public void setReference(String reference);

	public Integer getValue();

	public void setValue(Integer value);

	public AutomationUnit getUnit();

	public void setUnit(AutomationUnit unit);

	public OffsetDirection getDirection();

	public void setDirection(OffsetDirection direction);

	public Set<String> getDependingOnStatus();

	public void setDependingOnStatus(Set<String> dependingOnStatus);

	public Set<String> getOnlyWhenStatus();

	public void setOnlyWhenStatus(Set<String> onlyWhenStatus);

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
