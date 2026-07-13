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

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Initial date: 2026-06-26<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public interface CurriculumAutomationService {

	public void execute();

	public void processStatusChange(Collection<CurriculumElement> elements);

	public List<CurriculumAutomationConfig> getDefaultConfig(boolean implOnly, int maxRepositoryEntryRelations);

	public List<CurriculumAutomationConfig> getConfigs(CurriculumElementType type);

	public List<CurriculumAutomationConfig> getConfigs(CurriculumElement element);

	public List<CurriculumAutomationConfig> updateConfigs(CurriculumElementType type, List<CurriculumAutomationConfig> configs);

	public List<CurriculumAutomationConfig> updateConfigs(CurriculumElement element, List<CurriculumAutomationConfig> configs);

	public Date computeTriggerDate(CurriculumElement element, CurriculumAutomationRule rule);

	public Date getNextAutomationExecution(CurriculumElement element, List<CurriculumAutomationConfig> configs);

	public Date getNextExecutionTime();

}
