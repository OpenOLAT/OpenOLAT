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
package org.olat.modules.fo.ui.events;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 10.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectMessageEvent extends Event {

	private static final long serialVersionUID = -478977517193199740L;
	public static final String  SELECT_THREAD = "select-thread";
	public static final String  SELECT_MARKED = "select-marked";
	public static final String  SELECT_NEW = "select-new";
	public static final String  SELECT_MESSAGE = "select-message";
	
	private Long messageKey;
	private Long scrollToMessageKey;
	
	public SelectMessageEvent(String cmd, Long messageKey) {
		this(cmd, messageKey, null);
	}
	
	public SelectMessageEvent(String cmd, Long messageKey, Long scrollToMessageKey) {
		super(cmd);
		this.messageKey = messageKey;
		this.scrollToMessageKey = scrollToMessageKey;
	}
	
	public Long getMessageKey() {
		return messageKey;
	}

	public Long getScrollToMessageKey() {
		return scrollToMessageKey;
	}
}
