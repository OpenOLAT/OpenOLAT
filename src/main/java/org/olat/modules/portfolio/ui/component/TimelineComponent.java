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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.helpers.Settings;
import org.olat.core.util.Formatter;

/**
 * 
 * Initial date: 05.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TimelineComponent extends FormBaseComponentImpl {
	
	private static final TimelineComponentRenderer RENDERER = new TimelineComponentRenderer();

	private Date startTime, endTime;
	private String containerId;
	private List<TimelinePoint> points;
	private final TimelineElement element;
	
	public TimelineComponent(String name, TimelineElement element) {
		super(name);
		this.element = element;
	}
	
	@Override
	public TimelineElement getFormItem() {
		return element;
	}

	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public List<TimelinePoint> getPoints() {
		return points;
	}

	public void setPoints(List<TimelinePoint> points) {
		this.points = points;
		setDirty(true);
	}
	
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	public String getD3DateFormat(Locale locale) {
		Calendar cal = Calendar.getInstance();
		cal.set( 1999, Calendar.MARCH, 1, 0, 0, 0 );
		String formattedDate = Formatter.getInstance(locale).formatDate(cal.getTime());
		formattedDate = formattedDate.replace("1999", "%y");
		formattedDate = formattedDate.replace("99", "%y");
		formattedDate = formattedDate.replace("03", "%m");
		formattedDate = formattedDate.replace("3", "%m");
		formattedDate = formattedDate.replace("01", "%d");
		formattedDate = formattedDate.replace("1", "%d");
		return formattedDate;
	}

	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		
		if(Settings.isDebuging()) {
			vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/d3/d3.js");
			vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/jquery/openolat/jquery.timeline.js");
		} else {
			vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/d3/d3.min.js");
			vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/jquery/openolat/jquery.timeline.min.js");
		}
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
