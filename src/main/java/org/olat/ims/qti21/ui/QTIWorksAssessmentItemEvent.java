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
package org.olat.ims.qti21.ui;

import java.util.Map;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;

import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 26.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTIWorksAssessmentItemEvent extends FormEvent {

	private static final long serialVersionUID = 7767258131971848645L;
	
	public enum Event {
		source("source", "source"),
		state("state", "state"),
		result("result", "result"),
		validation("validation", "validation"),
		authorview("author-view", "author-view"),
		response("response", "response"),//impl
		tmpResponse("response-tmp", "response-tmp"),
		fullTmpResponse("response-full-tmp", "response-full-tmp"),
		deleteResponse("delete-response", "delete-response"),
		close("close", "close"),//ok
		solution("solution", "solution"),
		resetsoft("reset-soft","reset-soft"),
		resethard("reset-hard","reset-hard"),
		exit("exit", "exit"),
		back("back", "back"),
		skip("skip", "skip"),
		next("next", "next"),
		timesUp("times-up", "times-up");
		
		private final String path;
		private final String event;
		
		private Event(String event, String path) {
			this.event = event;
			this.path = path;
		}

		public String getPath() {
			return path;
		}

		public String event() {
			return event;
		}
	}
	
	private final Event event;
	private final String comment;
	private final String subCommand;
	private final Map<Identifier, ResponseInput> stringResponseMap;
	private final Map<Identifier, ResponseInput> fileResponseMap;
	
	public QTIWorksAssessmentItemEvent(Event event,  FormItem source) {
		this(event, null, null, null, source);
	}

	public QTIWorksAssessmentItemEvent(Event event, String subCommand, FormItem source) {
		this(event, subCommand, null, null, null, source);
	}
	
	public QTIWorksAssessmentItemEvent(Event event, Map<Identifier, ResponseInput> stringResponseMap,
			Map<Identifier, ResponseInput> fileResponseMap, String comment, FormItem source) {
		this(event, null, stringResponseMap, fileResponseMap, comment, source);
	}
	
	private QTIWorksAssessmentItemEvent(Event event, String subCommand,
			Map<Identifier, ResponseInput> stringResponseMap, Map<Identifier, ResponseInput> fileResponseMap,
			String comment, FormItem source) {
		super(event.name(), source);
		this.subCommand = subCommand;
		this.event = event;
		this.comment = comment;
		this.fileResponseMap = fileResponseMap;
		this.stringResponseMap = stringResponseMap;
	}

	public String getSubCommand() {
		return subCommand;
	}

	public Event getEvent() {
		return event;
	}
	
	public String getComment() {
		return comment;
	}

	public Map<Identifier, ResponseInput> getStringResponseMap() {
		return stringResponseMap;
	}

	public Map<Identifier, ResponseInput> getFileResponseMap() {
		return fileResponseMap;
	}
}
