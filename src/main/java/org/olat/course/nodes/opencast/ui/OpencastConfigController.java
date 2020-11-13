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
package org.olat.course.nodes.opencast.ui;

import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.AutoCompleter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.AutoCompleteFormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.OpencastCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.opencast.AuthDelegate;
import org.olat.modules.opencast.OpencastEvent;
import org.olat.modules.opencast.OpencastEventProvider;
import org.olat.modules.opencast.OpencastSeries;
import org.olat.modules.opencast.OpencastSeriesProvider;
import org.olat.modules.opencast.OpencastService;
import org.olat.modules.opencast.ui.EventListController;
import org.olat.modules.opencast.ui.EventListController.OpencastEventSelectionEvent;
import org.olat.modules.opencast.ui.SeriesListController;
import org.olat.modules.opencast.ui.SeriesListController.OpencastSeriesSelectionEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 Aug 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OpencastConfigController extends FormBasicController {
	
	private static final String DISPLAY_KEY_SERIES = "config.display.series";
	private static final String DISPLAY_KEY_EVENT = "config.display.event";
	private static final String[] DISPLAY_KEYS = new String[] {
			DISPLAY_KEY_SERIES,
			DISPLAY_KEY_EVENT
	};
	private static final String MORE_KEY = ".....";
	
	private SingleSelection displayEl;
	private FormLayoutContainer seriesCont;
	private AutoCompleter seriesEl;
	private FormLink seriesSearchLink;
	private FormLayoutContainer eventCont;
	private AutoCompleter eventEl;
	private FormLink eventSearchLink;
	private StaticTextElement identifierEl;
	
	private CloseableModalController cmc;
	private SeriesListController seriesSearchCtrl;
	private EventListController eventSearchCtrl;
	
	private final ModuleConfiguration config;
	
	@Autowired
	private OpencastService opncastService;
	
	public OpencastConfigController(UserRequest ureq, WindowControl wControl, OpencastCourseNode courseNode) {
		super(ureq, wControl);
		config = courseNode.getModuleConfiguration();
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("pane.tab.config");
		setFormContextHelp("Knowledge Transfer#_opencast");
		setFormTranslatedDescription(getFormDescription());
		
		displayEl = uifactory.addRadiosVertical("config.display", formLayout, DISPLAY_KEYS, translateAll(getTranslator(), DISPLAY_KEYS));
		displayEl.addActionListener(FormEvent.ONCHANGE);
		String selectedKey = config.has(OpencastCourseNode.CONFIG_EVENT_IDENTIFIER)? DISPLAY_KEY_EVENT: DISPLAY_KEY_SERIES;
		displayEl.select(selectedKey, true);
		
		// Series
		seriesCont = FormLayoutContainer.createCustomFormLayout("series", getTranslator(), velocity_root + "/search_item.html");
		seriesCont.setLabel("config.series", null);
		seriesCont.setRootForm(mainForm);
		formLayout.add(seriesCont);
		
		String seriesIdentifier = config.getStringValue(OpencastCourseNode.CONFIG_SERIES_IDENTIFIER, null);
		String seriesTitle = null;
		if (seriesIdentifier != null) {
			seriesTitle = config.getStringValue(OpencastCourseNode.CONFIG_TITLE);
		}
		seriesEl = uifactory.addTextElementWithAutoCompleter("config.series.auto", "config.series.auto", 128, seriesTitle, seriesCont);
		seriesEl.setListProvider(new OpencastSeriesProvider(getIdentity(), MORE_KEY), ureq.getUserSession());
		seriesEl.setKey(seriesIdentifier);
		seriesEl.setMinLength(1);
		
		seriesSearchLink = uifactory.addFormLink("config.series.search", seriesCont, Link.BUTTON);
		
		// Event
		eventCont = FormLayoutContainer.createCustomFormLayout("event", getTranslator(), velocity_root + "/search_item.html");
		eventCont.setLabel("config.event", null);
		eventCont.setRootForm(mainForm);
		formLayout.add(eventCont);
		
		String eventIdentifier = config.getStringValue(OpencastCourseNode.CONFIG_EVENT_IDENTIFIER, null);
		String eventTitle = null;
		if (eventIdentifier != null) {
			eventTitle = config.getStringValue(OpencastCourseNode.CONFIG_TITLE);
		}
		eventEl = uifactory.addTextElementWithAutoCompleter("config.event.auto", "config.event.auto", 128, eventTitle, eventCont);
		eventEl.setListProvider(new OpencastEventProvider(getIdentity(), MORE_KEY), ureq.getUserSession());
		eventEl.setKey(eventIdentifier);
		eventEl.setMinLength(1);
		
		eventSearchLink = uifactory.addFormLink("config.event.search", eventCont, Link.BUTTON);
		
		// General
		String identifier = null;
		if (StringHelper.containsNonWhitespace(seriesIdentifier)) {
			identifier = seriesIdentifier;
		} else if (StringHelper.containsNonWhitespace(eventIdentifier)) {
			identifier = eventIdentifier;
		}
		identifierEl = uifactory.addStaticTextElement("config.identifier", identifier, formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	private String getFormDescription() {
		StringBuilder sb = new StringBuilder();
		sb.append(translate("config.desc.select"));
		AuthDelegate authDelegate = opncastService.getAuthDelegate(getIdentity());
		if (AuthDelegate.Type.User == authDelegate.getType()) {
			sb.append(" ").append(translate("config.desc.user", new String[] {authDelegate.getValue()}));
		} else if (AuthDelegate.Type.Roles == authDelegate.getType()) {
			sb.append(" ").append(translate("config.desc.roles", new String[] {authDelegate.getValue()}));
		}
		return sb.toString();
	}
	
	private void updateUI() {
		boolean seriesSelected = displayEl.isOneSelected() && displayEl.getSelectedKey().equals(DISPLAY_KEY_SERIES);
		seriesCont.setVisible(seriesSelected);
		eventCont.setVisible(!seriesSelected);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == displayEl) {
			identifierEl.setValue("");
			updateUI();
		} else if (source == seriesEl || source == eventEl) {
			if (event instanceof AutoCompleteFormEvent) {
				String key = ((AutoCompleteFormEvent)event).getKey();
				if (!MORE_KEY.equals(key)) {
					identifierEl.setValue(key);
				}
			}
		} else if (source == seriesSearchLink) {
			doOpenSeriesSearch(ureq);
		} else if (source == eventSearchLink) {
			doOpenEventSearch(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (seriesSearchCtrl == source) {
			if (event instanceof OpencastSeriesSelectionEvent) {
				OpencastSeries opencastSeries = ((OpencastSeriesSelectionEvent)event).getSeries();
				seriesEl.setKey(opencastSeries.getIdentifier());
				seriesEl.setValue(opencastSeries.getTitle());
				identifierEl.setValue(opencastSeries.getIdentifier());
			}
			cmc.deactivate();
			cleanUp();
		} else if (eventSearchCtrl == source) {
			if (event instanceof OpencastEventSelectionEvent) {
				OpencastEvent opencastEvent = ((OpencastEventSelectionEvent)event).getEvent();
				eventEl.setKey(opencastEvent.getIdentifier());
				eventEl.setValue(opencastEvent.getTitle());
				identifierEl.setValue(opencastEvent.getIdentifier());
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(seriesSearchCtrl);
		removeAsListenerAndDispose(eventSearchCtrl);
		removeAsListenerAndDispose(cmc);
		seriesSearchCtrl = null;
		eventSearchCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		seriesEl.clearError();
		eventCont.clearError();
		boolean seriesSelected = displayEl.isOneSelected() && displayEl.getSelectedKey().equals(DISPLAY_KEY_SERIES);
		if (seriesSelected) {
			if (!StringHelper.containsNonWhitespace(seriesEl.getValue())) {
				seriesEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
				identifierEl.setValue("");
			}
		} else {
			if (!StringHelper.containsNonWhitespace(eventEl.getValue())) {
				eventCont.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
				identifierEl.setValue("");
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean seriesSelected = displayEl.isOneSelected() && displayEl.getSelectedKey().equals(DISPLAY_KEY_SERIES);
		if (seriesSelected) {
			String seriesIdentifier = seriesEl.getKey();
			config.setStringValue(OpencastCourseNode.CONFIG_SERIES_IDENTIFIER, seriesIdentifier);
			String title = seriesEl.getValue();
			config.setStringValue(OpencastCourseNode.CONFIG_TITLE, title);
			config.remove(OpencastCourseNode.CONFIG_EVENT_IDENTIFIER);
		} else {
			String eventIdentifier = eventEl.getKey();
			config.setStringValue(OpencastCourseNode.CONFIG_EVENT_IDENTIFIER, eventIdentifier);
			String title = eventEl.getValue();
			config.setStringValue(OpencastCourseNode.CONFIG_TITLE, title);
			config.remove(OpencastCourseNode.CONFIG_SERIES_IDENTIFIER);
		}
		
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	private void doOpenSeriesSearch(UserRequest ureq) {
		seriesSearchCtrl = new SeriesListController(ureq, getWindowControl());
		listenTo(seriesSearchCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", seriesSearchCtrl.getInitialComponent(), true,
				translate("config.series.search"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpenEventSearch(UserRequest ureq) {
		eventSearchCtrl = new EventListController(ureq, getWindowControl());
		listenTo(eventSearchCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", eventSearchCtrl.getInitialComponent(), true,
				translate("config.event.search"));
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	protected void doDispose() {
		//
	}

}
