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
package org.olat.modules.portfolio.ui.component;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;

/**
 * 
 * Initial date: 13.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TimelineElement extends FormItemImpl {
	
	private final TimelineComponent component;
	
	public TimelineElement(String name) {
		super(name);
		component = new TimelineComponent(name.concat("_CMP"), this);
	}
	
	public String getContainerId() {
		return component.getContainerId();
	}

	public void setContainerId(String containerId) {
		component.setContainerId(containerId);
	}

	public List<TimelinePoint> getPoints() {
		return component.getPoints();
	}

	public void setPoints(List<TimelinePoint> points) {
		component.setPoints(points);
	}
	
	public Date getStartTime() {
		return component.getStartTime();
	}

	public void setStartTime(Date startTime) {
		component.setStartTime(startTime);
	}

	public Date getEndTime() {
		return component.getEndTime();
	}

	public void setEndTime(Date endTime) {
		component.setEndTime(endTime);
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		//
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}

	@Override
	public void reset() {
		//
	}
}
