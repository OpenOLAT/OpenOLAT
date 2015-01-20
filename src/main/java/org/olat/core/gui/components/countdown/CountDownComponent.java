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
package org.olat.core.gui.components.countdown;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.translator.Translator;

/**
 * Count down in minutes
 * 
 * 
 * Initial date: 08.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CountDownComponent extends AbstractComponent {
	
	private static final CountDownComponentRenderer RENDERER = new CountDownComponentRenderer();

	private String i18nKey;
	private String currentRenderedTime;
	private Date date;
	
	public CountDownComponent(String name, Date date, Translator translator) {
		super(name);
		this.date = date;
		setDomReplacementWrapperRequired(false);
		setTranslator(translator);
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getCountDown() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long diffInMillies = date.getTime() - cal.getTimeInMillis();
		
		String countDown;
		if(diffInMillies < 0) {
			countDown = null;
		} else {
			TimeUnit timeUnit = TimeUnit.MINUTES;
			long diffInMinutes = timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
			countDown = Long.toString(diffInMinutes);
		}
	    return countDown;
	}

	public String getCurrentRenderedTime() {
		return currentRenderedTime;
	}

	public void setCurrentRenderedTime(String currentRenderedTime) {
		this.currentRenderedTime = currentRenderedTime;
	}
	
	public String getI18nKey() {
		return i18nKey;
	}

	public void setI18nKey(String key) {
		this.i18nKey = key;
	}

	@Override
	public boolean isDirty() {
		boolean dirty;
		String countDown = getCountDown();
		if(countDown == null && currentRenderedTime == null) {
			dirty = false;
		} else if(countDown != null && currentRenderedTime == null) {
			dirty = true;
		} else if(countDown == null && currentRenderedTime != null) {
			dirty = true;
		} else {
			dirty = !countDown.equals(currentRenderedTime);
		}
		return dirty;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
