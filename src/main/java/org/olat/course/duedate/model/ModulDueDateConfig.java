/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.duedate.model;

import java.util.Date;

import org.olat.core.util.StringHelper;
import org.olat.course.duedate.DueDateConfig;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 4 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ModulDueDateConfig implements DueDateConfig {
	
	private final ModuleConfiguration config;
	private final String relativeKey;
	private final String absoluteDateKey;
	private final String absoluteStartDateKey;
	private final String numOfDaysKey;
	private final String raltiveToTypeKey;
	private final String durationKey;
	
	public ModulDueDateConfig(ModuleConfiguration config, String relativeKey, String absoluteDateKey,
			String numOfDaysKey, String relativeToTypeKey) {
		this(config, relativeKey, null, absoluteDateKey, numOfDaysKey, relativeToTypeKey, null);
	}
	
	public ModulDueDateConfig(ModuleConfiguration config, String relativeKey,
			String absoluteStartDateKey, String absoluteDateKey,
			String numOfDaysKey, String relativeToTypeKey, String durationKey) {
		this.config = config;
		this.relativeKey = relativeKey;
		this.absoluteDateKey = absoluteDateKey;
		this.absoluteStartDateKey = absoluteStartDateKey;
		this.numOfDaysKey = numOfDaysKey;
		this.raltiveToTypeKey = relativeToTypeKey;
		this.durationKey = durationKey;
	}

	@Override
	public Date getAbsoluteDate() {
		return !isRelative()? config.getDateValue(absoluteDateKey): null;
	}
	
	@Override
	public Date getAbsoluteStartDate() {
		return !isRelative() && StringHelper.containsNonWhitespace(absoluteStartDateKey)
				? config.getDateValue(absoluteStartDateKey): null;
	}

	@Override
	public int getNumOfDays() {
		return isRelative()? config.getIntegerSafe(numOfDaysKey, -1): -1;
	}

	@Override
	public String getRelativeToType() {
		return isRelative()? config.getStringValue(raltiveToTypeKey): null;
	}
	
	@Override
	public int getDurationInDays() {
		return isRelative()? config.getIntegerSafe(durationKey, -1): -1;
	}

	private boolean isRelative() {
		return config.getBooleanSafe(relativeKey, false);
	}

}
