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
package org.olat.course.nodes.edusharing.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.TranslatorHelper;
import org.olat.core.util.StringHelper;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.EdusharingCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.edusharing.EdusharingHtmlElement;
import org.olat.modules.edusharing.EdusharingUsage;
import org.olat.modules.edusharing.model.SearchResult;
import org.olat.modules.edusharing.ui.EdusharingSearchController;
import org.olat.modules.edusharing.ui.EdusharingSearchController.SearchEvent;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 20 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdusharingConfigController extends FormBasicController {
	
	private final String[] SHOW_KEYS = new String [] { "edit.show" };

	private StaticTextElement noItemEl;
	private FormLink previewLink;
	private FormLink selectLink;
	private FormLink replaceLink;
	private SingleSelection versionEl;
	private MultipleSelectionElement licenseEl;
	private MultipleSelectionElement metadataEl;
	private String[] showValues;
	
	private final BreadcrumbPanel stackPanel;
	private CloseableModalController cmc;
	private EdusharingSearchController searchCtrl;
	private EdusharingRunController previewCtrl;
	
	private final EdusharingCourseNode courseNode;
	private final ModuleConfiguration config;
	private final RepositoryEntry courseEntry;

	public EdusharingConfigController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			EdusharingCourseNode courseNode, RepositoryEntry courseEntry) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.courseNode = courseNode;
		this.config = courseNode.getModuleConfiguration();
		this.courseEntry = courseEntry;
		this.showValues = TranslatorHelper.translateAll(getTranslator(), SHOW_KEYS);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("edit.title");
		setFormContextHelp("Knowledge Transfer#_edusharing");
		
		noItemEl = uifactory.addStaticTextElement("edit.no.item", "edit.no.item",
				translate("edit.no.item.text"), formLayout);
		previewLink = uifactory.addFormLink("edit.preview", "", translate("edit.preview"), formLayout,
				Link.NONTRANSLATED);
		previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		selectLink = uifactory.addFormLink("edit.select", buttonsCont, "btn btn-default o_xsmall");
		replaceLink = uifactory.addFormLink("edit.replace", buttonsCont, "btn btn-default o_xsmall");
		
		SelectionValues versionKV = new SelectionValues();
		versionKV.add(entry(EdusharingCourseNode.CONFIG_VERSION_VALUE_CURRENT, translate("edit.version.current")));
		versionKV.add(entry(EdusharingCourseNode.CONFIG_VERSION_VALUE_LATEST, translate("edit.version.latest")));
		versionEl = uifactory.addRadiosHorizontal("edit.version", formLayout, versionKV.keys(), versionKV.values());
		versionEl.addActionListener(FormEvent.ONCHANGE);
		String version = config.getStringValue(EdusharingCourseNode.CONFIG_VERSION, EdusharingCourseNode.CONFIG_VERSION_VALUE_CURRENT);
		versionEl.select(version, true);
		
		licenseEl = uifactory.addCheckboxesHorizontal("edit.show.license", formLayout, SHOW_KEYS, showValues);
		licenseEl.addActionListener(FormEvent.ONCHANGE);
		licenseEl.select(licenseEl.getKey(0), config.getBooleanSafe(EdusharingCourseNode.CONFIG_SHOW_LICENSE));
		
		metadataEl = uifactory.addCheckboxesHorizontal("edit.show.metadata", formLayout, SHOW_KEYS, showValues);
		metadataEl.addActionListener(FormEvent.ONCHANGE);
		metadataEl.select(metadataEl.getKey(0), config.getBooleanSafe(EdusharingCourseNode.CONFIG_SHOW_METADATA));
		
		updateUI();
	}
	
	private void updateUI() {
		boolean hasItem = config.has(EdusharingCourseNode.CONFIG_IDENTIFIER);
		noItemEl.setVisible(!hasItem);
		
		String title = config.getStringValue(EdusharingCourseNode.CONFIG_ES_TITLE, "no title");
		previewLink.setI18nKey(title);
		previewLink.setVisible(hasItem);
		
		selectLink.setVisible(!hasItem);
		replaceLink.setVisible(hasItem);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == selectLink || source == replaceLink) {
			doSearch(ureq);
		} else if (source == versionEl || source == licenseEl || source == metadataEl) {
			doSetConfig(ureq);
		} else if (source == previewLink) {
			doPreview(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (searchCtrl == source) {
			if (event instanceof SearchEvent) {
				SearchEvent se = (SearchEvent)event;
				doSetSearchResult(ureq, se.getSearchResult());
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(searchCtrl);
		removeAsListenerAndDispose(cmc);
		searchCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doSearch(UserRequest ureq) {
		searchCtrl = new EdusharingSearchController(ureq, getWindowControl());
		listenTo(searchCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", searchCtrl.getInitialComponent(), true, null);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSetSearchResult(UserRequest ureq, SearchResult searchResult) {
		if (searchResult != null) {
			String identifier = courseNode.createIdentifier();
			
			/**
			 * The usage is already here created. It would be more right on the publish process,
			 * but because there is no identity we create it here and delete the unnecessary
			 * usages on the publish process.
			 */
			EdusharingHtmlElement edusharingHtmlElement = createEdusharingHtmlElement(identifier, searchResult);
			EdusharingUsage usage = courseNode.getOrCreateUsage(courseEntry, courseNode.getIdent(), edusharingHtmlElement, getIdentity());
			if (usage != null) {
				doSetSearchResultConfig(identifier, searchResult);
				updateUI();
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			} else {
				showError("error.selection.failed");
			}
		} else {
			showError("error.selection.failed");
		}
	}
	
	private EdusharingHtmlElement createEdusharingHtmlElement(String identifier,
			SearchResult searchResult) {
		String objectUrl = searchResult.getObjectUrl();
		EdusharingHtmlElement element = new EdusharingHtmlElement(identifier, objectUrl);
		
		element.setVersion(searchResult.getWindowVersion());
		element.setMimeType(searchResult.getMimeType());
		element.setMediaType(searchResult.getMediaType());
		if (searchResult.getWindowWidth() != null) {
			element.setWidth(searchResult.getWindowWidth().toString());
		}
		if (searchResult.getWindowHight() != null) {
			element.setHight(searchResult.getWindowHight().toString());
		}
		
		return element;
	}

	private void doSetSearchResultConfig(String identifier, SearchResult searchResult) {
		config.set(EdusharingCourseNode.CONFIG_IDENTIFIER, identifier);
		config.setStringValue(EdusharingCourseNode.CONFIG_ES_OBJECT_URL, searchResult.getObjectUrl());
		config.setStringValue(EdusharingCourseNode.CONFIG_ES_TITLE, searchResult.getTitle());
		if (StringHelper.containsNonWhitespace(searchResult.getMimeType())) {
			config.setStringValue(EdusharingCourseNode.CONFIG_ES_MIME_TYPE, searchResult.getMimeType());
		} else {
			config.remove(EdusharingCourseNode.CONFIG_ES_MIME_TYPE);
		}
		if (StringHelper.containsNonWhitespace(searchResult.getMediaType())) {
			config.setStringValue(EdusharingCourseNode.CONFIG_ES_MEDIA_TYPE, searchResult.getMediaType());
		} else {
			config.remove(EdusharingCourseNode.CONFIG_ES_MEDIA_TYPE);
		}
		if (StringHelper.containsNonWhitespace(searchResult.getResourceType())) {
			config.setStringValue(EdusharingCourseNode.CONFIG_ES_RESOURCE_TYPE, searchResult.getResourceType());
		} else {
			config.remove(EdusharingCourseNode.CONFIG_ES_RESOURCE_TYPE);
		}
		if (StringHelper.containsNonWhitespace(searchResult.getRepoType())) {
			config.setStringValue(EdusharingCourseNode.CONFIG_ES_REPO_TYPE, searchResult.getRepoType());
		} else {
			config.remove(EdusharingCourseNode.CONFIG_ES_REPO_TYPE);
		}
		if (StringHelper.containsNonWhitespace(searchResult.getResourceVersion())) {
			config.setStringValue(EdusharingCourseNode.CONFIG_ES_RESOURCE_VERSION, searchResult.getResourceVersion());
		} else {
			config.remove(EdusharingCourseNode.CONFIG_ES_RESOURCE_VERSION);
		}
		if (searchResult.getWindowHight() != null) {
			config.setStringValue(EdusharingCourseNode.CONFIG_ES_WINDOW_HEIGHT, searchResult.getWindowHight().toString());
		} else {
			config.remove(EdusharingCourseNode.CONFIG_ES_WINDOW_HEIGHT);
		}
		if (searchResult.getWindowWidth() != null) {
			config.setStringValue(EdusharingCourseNode.CONFIG_ES_WINDOW_WIDTH, searchResult.getWindowWidth().toString());
		} else {
			config.remove(EdusharingCourseNode.CONFIG_ES_WINDOW_WIDTH);
		}
		if (searchResult.getRatio() != null) {
			config.setStringValue(EdusharingCourseNode.CONFIG_ES_RATIO, String.valueOf(searchResult.getRatio().doubleValue()));
		} else {
			config.remove(EdusharingCourseNode.CONFIG_ES_RATIO);
		}
		if (StringHelper.containsNonWhitespace(searchResult.getWindowVersion())) {
			config.setStringValue(EdusharingCourseNode.CONFIG_ES_WINDOW_VERISON, searchResult.getWindowVersion());
		} else {
			config.remove(EdusharingCourseNode.CONFIG_ES_WINDOW_VERISON);
		}
	}

	private void doSetConfig(UserRequest ureq) {
		String version = versionEl.isOneSelected()
				? versionEl.getSelectedKey()
				: EdusharingCourseNode.CONFIG_VERSION_VALUE_CURRENT;
		config.setStringValue(EdusharingCourseNode.CONFIG_VERSION, version);
		config.setBooleanEntry(EdusharingCourseNode.CONFIG_SHOW_LICENSE, licenseEl.isAtLeastSelected(1));
		config.setBooleanEntry(EdusharingCourseNode.CONFIG_SHOW_METADATA, metadataEl.isAtLeastSelected(1));
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}
	
	private void doPreview(UserRequest ureq) {
		removeAsListenerAndDispose(previewCtrl);
		previewCtrl = new EdusharingRunController(ureq, getWindowControl(), courseNode);
		listenTo(previewCtrl);
		stackPanel.pushController(translate("preview"), previewCtrl);
	}

}
