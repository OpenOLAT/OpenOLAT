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
package org.olat.core.commons.fullWebApp;

import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 9 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NotificationEvent extends MultiUserEvent {

	private static final long serialVersionUID = -7671244140814250725L;

	public static final String NOTIFICATION = "base-full-notification";
	
	private final Class<?> i18nPackage;
	private final String i18nKey;
	private final String[] arguments;
	
	public NotificationEvent(Class<?> i18nPackage, String i18nKey, String[] arguments) {
		super(NOTIFICATION);
		this.i18nKey = i18nKey;
		this.i18nPackage = i18nPackage;
		this.arguments = arguments;
	}

	public Class<?> getI18nPackage() {
		return i18nPackage;
	}

	public String getI18nKey() {
		return i18nKey;
	}

	public String[] getArguments() {
		return arguments;
	}
}
