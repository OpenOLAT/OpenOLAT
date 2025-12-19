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
package org.olat.modules.forms.model.xml;

import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;

/**
 * 
 * Initial date: Dec 17, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class DateInput extends AbstractElement {

	private static final long serialVersionUID = 2420712254825004290L;
	
	public static final String TYPE = "formdateinput";

	private boolean mandatory;
	private boolean date;
	private boolean time;
	private String nowButtonLabel;
	private BlockLayoutSettings layoutSettings;
	private AlertBoxSettings alertBoxSettings;

	@Override
	public String getType() {
		return TYPE;
	}
	
	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public boolean isDate() {
		return date;
	}

	public void setDate(boolean date) {
		this.date = date;
	}

	public boolean isTime() {
		return time;
	}

	public void setTime(boolean time) {
		this.time = time;
	}

	public String getNowButtonLabel() {
		return nowButtonLabel;
	}

	public void setNowButtonLabel(String nowButtonLabel) {
		this.nowButtonLabel = nowButtonLabel;
	}

	public BlockLayoutSettings getLayoutSettings() {
		return layoutSettings;
	}

	public void setLayoutSettings(BlockLayoutSettings layoutSettings) {
		this.layoutSettings = layoutSettings;
	}

	public AlertBoxSettings getAlertBoxSettings() {
		return alertBoxSettings;
	}

	public void setAlertBoxSettings(AlertBoxSettings alertBoxSettings) {
		this.alertBoxSettings = alertBoxSettings;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof DateInput) {
			DateInput input = (DateInput)obj;
			return getId() != null && getId().equals(input.getId());
		}
		return super.equals(obj);
	}
}
