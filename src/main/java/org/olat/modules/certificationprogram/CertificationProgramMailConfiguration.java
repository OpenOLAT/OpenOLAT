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
package org.olat.modules.certificationprogram;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.modules.certificationprogram.ui.component.Duration;
import org.olat.modules.certificationprogram.ui.component.DurationType;

/**
 * 
 * Initial date: 11 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public interface CertificationProgramMailConfiguration extends CreateInfo, ModifiedInfo {
	
	Long getKey();
	
	String getTitle();
	
	void setTitle(String title);
	
	CertificationProgramMailType getType();
	
	CertificationProgramMailConfigurationStatus getStatus();
	
	void setStatus(CertificationProgramMailConfigurationStatus status);
	
	String getI18nSuffix();

	boolean isCustomized();

	void setCustomized(boolean customized);
	
	int getTime();

	void setTime(int time);

	DurationType getTimeUnit();

	void setTimeUnit(DurationType unit);
	
	Duration getTimeDuration();
	
	boolean isCreditBalanceTooLow();
	
	void setCreditBalanceTooLow(boolean enable);
	
	
	CertificationProgram getCertificationProgram();

}
