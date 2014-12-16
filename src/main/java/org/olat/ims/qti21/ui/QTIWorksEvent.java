package org.olat.ims.qti21.ui;

import java.util.Map;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;

import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.StringResponseData;

/**
 * 
 * Initial date: 11.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTIWorksEvent extends FormEvent {

	private static final long serialVersionUID = 7767258131971848645L;
	
	private final Event event;
	private final String subCommand;
	private final Map<Identifier, StringResponseData> stringResponseMap;
	
	public QTIWorksEvent(Event event,  FormItem source) {
		this(event, null, null, source);
	}

	public QTIWorksEvent(Event event, String subCommand, FormItem source) {
		this(event, subCommand, null, source);
	}
	
	public QTIWorksEvent(Event event, Map<Identifier, StringResponseData> stringResponseMap, FormItem source) {
		this(event, null, stringResponseMap, source);
	}
	
	private QTIWorksEvent(Event event, String subCommand, Map<Identifier, StringResponseData> stringResponseMap, FormItem source) {
		super(event.name(), source);
		this.subCommand = subCommand;
		this.event = event;
		this.stringResponseMap = stringResponseMap;
	}

	public String getSubCommand() {
		return subCommand;
	}

	public Event getEvent() {
		return event;
	}

	public Map<Identifier, StringResponseData> getStringResponseMap() {
		return stringResponseMap;
	}

	public enum Event {
		source("source", "source"),
		state("state", "state"),
		result("result", "result"),
		validation("validation", "validation"),
		authorview("author-view", "author-view"),
		response("response", "response"),//OK
		testPartNavigation("test-part-navigation", "test-part-navigation"),//OK
		selectItem("select-item", "select-item/"),//OK
		finishItem("finish-item", "finish-item"),
		reviewTestPart("review-test-part", "review-test-part"),
		reviewItem("review-item", "review-item"),
		itemSolution("item-solution", "item-solution"),
		endTestPart("end-test-part", "end-test-part"),//OK
		advanceTestPart("advance-test-part", "advance-test-part"),
		exitTest("exit-test", "exit-test")
		;
		
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
}
