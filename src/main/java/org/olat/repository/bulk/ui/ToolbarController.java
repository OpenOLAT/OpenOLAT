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
package org.olat.repository.bulk.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.fileresource.types.WikiResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.bulk.SettingsBulkEditable;
import org.olat.repository.bulk.SettingsBulkEditables;
import org.olat.repository.bulk.model.SettingsContext;
import org.olat.repository.bulk.model.SettingsContext.Replacement;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToolbarController extends StepFormBasicController {
	
	private static final String KEY_ON = "on";
	private static final String KEY_OFF = "off";
	private static final String[] CHANGE_KEYS = new String[] {"change"};
	
	private final List<MultipleSelectionElement> checkboxSwitch = new ArrayList<>(3);
	private final Map<MultipleSelectionElement, FormLayoutContainer> checkboxContainer = new HashMap<>(3);
	private SingleSelection searchEl;
	private SingleSelection calendarEl;
	private SingleSelection participantListEl;
	private SingleSelection participantInfoEl;
	private SingleSelection emailEl;
	private SingleSelection teamsEl;
	private MultipleSelectionElement bigBlueButtonCheckboxEl;
	private SingleSelection bigBlueButtonEl;
	private SingleSelection bigBlueButtonModeratorStartsMeetingEl;
	private SingleSelection zoomEl;
	private SingleSelection blogEl;
	private FormLayoutContainer blogKeyCont;
	private StaticTextElement blogNameEl;
	private FormLink blogSelectLink;
	private SingleSelection wikiEl;
	private FormLayoutContainer wikiKeyCont;
	private StaticTextElement wikiNameEl;
	private FormLink wikiSelectLink;
	private SingleSelection forumEl;
	private SingleSelection documentsEl;
	private SingleSelection chatEl;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController blogSearchCtrl;
	private ReferencableEntriesSearchController wikiSearchCtrl;
	
	private final SettingsContext context;
	private final SettingsBulkEditables editables;
	private final String[] changeValues;
	private RepositoryEntry blogEntry;
	private RepositoryEntry wikiEntry;
	
	@Autowired
	private RepositoryManager repositoryManager;
	
	
	public ToolbarController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.context = (SettingsContext)runContext.get(SettingsContext.DEFAULT_KEY);
		this.editables = (SettingsBulkEditables)runContext.get(SettingsBulkEditables.DEFAULT_KEY);
		this.changeValues = new String[] {translate("settings.bulk.change")};
		if (StringHelper.containsNonWhitespace(context.getToolBlogKey())) {
			blogEntry = repositoryManager.lookupRepositoryEntryBySoftkey(context.getToolBlogKey(), false);
		}
		if (StringHelper.containsNonWhitespace(context.getToolWikiKey())) {
			wikiEntry = repositoryManager.lookupRepositoryEntryBySoftkey(context.getToolWikiKey(), false);
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer cont = FormLayoutContainer.createDefaultFormLayout("toolbarCont", getTranslator());
		cont.setFormTitle(translate("settings.bulk.toolbar.title"));
		cont.setFormInfo(RepositoryBulkUIFactory.getSettingsDescription(getTranslator(), context.getRepositoryEntries(), "settings.bulk.toolbar.desc"));
		cont.setRootForm(mainForm);
		formLayout.add(cont);
		
		String courseOnlyInfo = "<i class='o_icon o_icon_warn'> </i> " + translate("settings.bulk.course.only.multi");
		StaticTextElement courseOnlyEl = uifactory.addStaticTextElement("course.only.info", null, courseOnlyInfo, cont);
		courseOnlyEl.setElementCssClass("o_form_explanation");
		
		SelectionValues toolbarSV = new SelectionValues();
		toolbarSV.add(SelectionValues.entry(KEY_ON, translate("on")));
		toolbarSV.add(SelectionValues.entry(KEY_OFF, translate("off")));
		
		SelectionValues replacementSV = new SelectionValues();
		replacementSV.add(SelectionValues.entry(Replacement.add.name(), translate("settings.bulk.replacement.add")));
		replacementSV.add(SelectionValues.entry(Replacement.change.name(), translate("settings.bulk.replacement.change")));
		replacementSV.add(SelectionValues.entry(Replacement.addChange.name(), translate("settings.bulk.replacement.add.change")));
		replacementSV.add(SelectionValues.entry(Replacement.remove.name(), translate("settings.bulk.replacement.remove")));
		
		searchEl = uifactory.addRadiosHorizontal("settings.bulk.toolbar.search", cont, toolbarSV.keys(), toolbarSV.values());
		searchEl.select(context.isToolSearch()? KEY_ON: KEY_OFF, true);
		decorate(searchEl, cont, SettingsBulkEditable.toolSearch);
		
		if (editables.isToolCalendarEnabled()) {
			calendarEl = uifactory.addRadiosHorizontal("settings.bulk.toolbar.calendar", cont, toolbarSV.keys(), toolbarSV.values());
			calendarEl.select(context.isToolCalendar()? KEY_ON: KEY_OFF, true);
			decorate(calendarEl, cont, SettingsBulkEditable.toolCalendar);
		}
		
		participantListEl = uifactory.addRadiosHorizontal("settings.bulk.toolbar.participant.list", cont, toolbarSV.keys(), toolbarSV.values());
		participantListEl.select(context.isToolParticipantList()? KEY_ON: KEY_OFF, true);
		decorate(participantListEl, cont, SettingsBulkEditable.toolParticipantList);
		
		participantInfoEl = uifactory.addRadiosHorizontal("settings.bulk.toolbar.participant.info", cont, toolbarSV.keys(), toolbarSV.values());
		participantInfoEl.select(context.isToolParticipantInfo()? KEY_ON: KEY_OFF, true);
		decorate(participantInfoEl, cont, SettingsBulkEditable.toolParticipantInfo);
		
		emailEl = uifactory.addRadiosHorizontal("settings.bulk.toolbar.email", cont, toolbarSV.keys(), toolbarSV.values());
		emailEl.select(context.isToolEmail()? KEY_ON: KEY_OFF, true);
		decorate(emailEl, cont, SettingsBulkEditable.toolEmail);
		
		if (editables.isToolTeamsEnables()) {
			teamsEl = uifactory.addRadiosHorizontal("settings.bulk.toolbar.teams", cont, toolbarSV.keys(), toolbarSV.values());
			teamsEl.select(context.isToolTeams()? KEY_ON: KEY_OFF, true);
			decorate(teamsEl, cont, SettingsBulkEditable.toolTeams);
		}
		
		if (editables.isToolBigBlueButtonEnabled()) {
			bigBlueButtonEl = uifactory.addRadiosHorizontal("settings.bulk.toolbar.bigbluebutton", cont, toolbarSV.keys(), toolbarSV.values());
			bigBlueButtonEl.select(context.isToolBigBlueButton()? KEY_ON: KEY_OFF, true);
			bigBlueButtonEl.addActionListener(FormEvent.ONCHANGE);
			bigBlueButtonCheckboxEl = decorate(bigBlueButtonEl, cont, SettingsBulkEditable.toolBigBlueButton);
			
			bigBlueButtonModeratorStartsMeetingEl = uifactory.addRadiosHorizontal("settings.bulk.toolbar.bigbluebutton.moderator", cont, toolbarSV.keys(), toolbarSV.values());
			bigBlueButtonModeratorStartsMeetingEl.select(context.isToolBigBlueButtonModeratorStartsMeeting()? KEY_ON: KEY_OFF, true);
			
			updateBigBlueButtonUI();
		}
		
		if (editables.isToolZoomEnabled()) {
			zoomEl = uifactory.addRadiosHorizontal("settings.bulk.toolbar.zoom", cont, toolbarSV.keys(), toolbarSV.values());
			zoomEl.select(context.isToolZoom()? KEY_ON: KEY_OFF, true);
			decorate(zoomEl, cont, SettingsBulkEditable.toolZoom);
		}
		
		blogEl = uifactory.addDropdownSingleselect("settings.bulk.toolbar.blog", cont, replacementSV.keys(), replacementSV.values());
		blogEl.select(context.getToolBlog() != null? context.getToolBlog().name(): Replacement.add.name(), true);
		blogEl.addActionListener(FormEvent.ONCHANGE);
		decorate(blogEl, cont, SettingsBulkEditable.toolBlog);
		
		blogKeyCont = FormLayoutContainer.createButtonLayout("blogButtons", getTranslator());
		blogKeyCont.setElementCssClass("o_inline_cont");
		blogKeyCont.setRootForm(mainForm);
		cont.add(blogKeyCont);
		blogNameEl = uifactory.addStaticTextElement("blog.name", null, "", blogKeyCont);
		blogSelectLink = uifactory.addFormLink("settings.bulk.toolbar.blog.select", blogKeyCont, Link.BUTTON_XSMALL);
		updateBlogUI();
		
		wikiEl = uifactory.addDropdownSingleselect("settings.bulk.toolbar.wiki", cont, replacementSV.keys(), replacementSV.values());
		wikiEl.select(context.getToolWiki() != null? context.getToolWiki().name(): Replacement.add.name(), true);
		wikiEl.addActionListener(FormEvent.ONCHANGE);
		decorate(wikiEl, cont, SettingsBulkEditable.toolWiki);
		
		wikiKeyCont = FormLayoutContainer.createButtonLayout("wikiButtons", getTranslator());
		wikiKeyCont.setElementCssClass("o_inline_cont");
		wikiKeyCont.setRootForm(mainForm);
		cont.add(wikiKeyCont);
		wikiNameEl = uifactory.addStaticTextElement("wiki.name", null, "", wikiKeyCont);
		wikiSelectLink = uifactory.addFormLink("settings.bulk.toolbar.wiki.select", wikiKeyCont, Link.BUTTON_XSMALL);
		updateWikiUI();
		
		forumEl = uifactory.addRadiosHorizontal("settings.bulk.toolbar.forum", cont, toolbarSV.keys(), toolbarSV.values());
		forumEl.select(context.isToolForum()? KEY_ON: KEY_OFF, true);
		decorate(forumEl, cont, SettingsBulkEditable.toolForum);
		
		documentsEl = uifactory.addRadiosHorizontal("settings.bulk.toolbar.documents", cont, toolbarSV.keys(), toolbarSV.values());
		documentsEl.select(context.isToolDocuments()? KEY_ON: KEY_OFF, true);
		decorate(documentsEl, cont, SettingsBulkEditable.toolDocuments);
		
		chatEl = uifactory.addRadiosHorizontal("settings.bulk.toolbar.chat", cont, toolbarSV.keys(), toolbarSV.values());
		chatEl.select(context.isToolChat()? KEY_ON: KEY_OFF, true);
		decorate(chatEl, cont, SettingsBulkEditable.toolChat);
	}

	private MultipleSelectionElement decorate(FormItem item, FormLayoutContainer formLayout, SettingsBulkEditable editable) {
		boolean selected = context.isSelected(editable);
		String itemName = item.getName();
		MultipleSelectionElement checkbox = uifactory.addCheckboxesHorizontal("cbx_" + itemName, itemName, formLayout, CHANGE_KEYS, changeValues);
		checkbox.select(checkbox.getKey(0), selected);
		checkbox.setEnabled(editables.isEditable(editable));
		checkbox.addActionListener(FormEvent.ONCLICK);
		checkbox.setUserObject(item);
		checkboxSwitch.add(checkbox);
		
		item.setLabel(null, null);
		item.setVisible(selected);
		item.setUserObject(checkbox);
		
		checkboxContainer.put(checkbox, formLayout);
		formLayout.moveBefore(checkbox, item);
		return checkbox;
	}
	
	private void updateBigBlueButtonUI() {
		if (bigBlueButtonCheckboxEl == null) {
			return;
		}
		
		boolean bigBlueButtonEnabled = bigBlueButtonCheckboxEl.isAtLeastSelected(1) && bigBlueButtonEl.isKeySelected(KEY_ON);
		bigBlueButtonModeratorStartsMeetingEl.setVisible(bigBlueButtonEnabled);
	}
	
	private void updateBlogUI() {
		boolean blogMandatory = blogEl.isVisible() && blogEl.isOneSelected() && !blogEl.isKeySelected(Replacement.remove.name());
		blogKeyCont.setVisible(blogMandatory);
		blogNameEl.setValue(blogEntry != null? blogEntry.getDisplayname(): translate("settings.bulk.toolbar.blog.none"));
		blogNameEl.setVisible(blogMandatory);
		blogSelectLink.setVisible(blogMandatory);
	}
	
	private void updateWikiUI() {
		boolean wikiMandatory = wikiEl.isVisible() && wikiEl.isOneSelected() && !wikiEl.isKeySelected(Replacement.remove.name());
		wikiKeyCont.setVisible(wikiMandatory);
		wikiNameEl.setValue(wikiEntry != null? wikiEntry.getDisplayname(): translate("settings.bulk.toolbar.wiki.none"));
		wikiNameEl.setVisible(wikiMandatory);
		wikiSelectLink.setVisible(wikiMandatory);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == bigBlueButtonEl) {
			updateBigBlueButtonUI();
		} else if (source == blogEl) {
			updateBlogUI();
		} else if (source == blogSelectLink) {
			doSelectBlog(ureq);
		} else if (source == wikiEl) {
			updateWikiUI();
		} else if (source == wikiSelectLink) {
			doSelectWiki(ureq);
		} else if (checkboxSwitch.contains(source)) {
			MultipleSelectionElement checkbox = (MultipleSelectionElement)source;
			FormItem item = (FormItem)checkbox.getUserObject();
			item.setVisible(checkbox.isAtLeastSelected(1));
			checkboxContainer.get(checkbox).setDirty(true);
			
			if (item == bigBlueButtonEl) {
				updateBigBlueButtonUI();
			} else if (item == blogEl) {
				updateBlogUI();
			} else if (item == wikiEl) {
				updateWikiUI();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == blogSearchCtrl) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				RepositoryEntry re = blogSearchCtrl.getSelectedEntry();
				if (re != null) {
					blogEntry = re;
					updateBlogUI();
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == wikiSearchCtrl) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				RepositoryEntry re = wikiSearchCtrl.getSelectedEntry();
				if (re != null) {
					wikiEntry = re;
					updateWikiUI();
				}
			}
			cmc.deactivate();
			cleanUp();
		} 
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(blogSearchCtrl);
		removeAsListenerAndDispose(wikiSearchCtrl);
		removeAsListenerAndDispose(cmc);
		blogSearchCtrl = null;
		wikiSearchCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
	
		blogKeyCont.clearError();
		boolean blogKeyEnabled = blogKeyCont.isVisible();
		if (blogKeyEnabled && blogEntry == null) {
			blogKeyCont.setErrorKey("settings.bulk.toolbar.error.no.blog.selected");
			allOk &= false;
		}
		
		wikiKeyCont.clearError();
		boolean wikiKeyEnabled = wikiKeyCont.isVisible();
		if (wikiKeyEnabled && wikiEntry == null) {
			wikiKeyCont.setErrorKey("settings.bulk.toolbar.error.no.wiki.selected");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		context.select(SettingsBulkEditable.toolSearch, searchEl.isVisible());
		if (searchEl.isVisible()) {
			context.setToolSearch(searchEl.isKeySelected(KEY_ON));
		}
		
		context.select(SettingsBulkEditable.toolCalendar, calendarEl != null && calendarEl.isVisible());
		if (calendarEl != null && calendarEl.isVisible()) {
			context.setToolCalendar(calendarEl.isKeySelected(KEY_ON));
		}
		
		context.select(SettingsBulkEditable.toolParticipantList, participantListEl.isVisible());
		if (participantListEl.isVisible()) {
			context.setToolParticipantList(participantListEl.isKeySelected(KEY_ON));
		}
		
		context.select(SettingsBulkEditable.toolParticipantInfo, participantInfoEl.isVisible());
		if (participantInfoEl.isVisible()) {
			context.setToolParticipantInfo(participantInfoEl.isKeySelected(KEY_ON));
		}
		
		context.select(SettingsBulkEditable.toolEmail, emailEl.isVisible());
		if (emailEl.isVisible()) {
			context.setToolEmail(emailEl.isKeySelected(KEY_ON));
		}
		
		context.select(SettingsBulkEditable.toolTeams, teamsEl != null && teamsEl.isVisible());
		if (teamsEl != null && teamsEl.isVisible()) {
			context.setToolTeams(teamsEl.isKeySelected(KEY_ON));
		}
		
		context.select(SettingsBulkEditable.toolBigBlueButton, bigBlueButtonEl != null && bigBlueButtonEl.isVisible());
		if (bigBlueButtonEl != null && bigBlueButtonEl.isVisible()) {
			context.setToolBigBlueButton(bigBlueButtonEl.isKeySelected(KEY_ON));
		}
		
		if (bigBlueButtonModeratorStartsMeetingEl != null && bigBlueButtonModeratorStartsMeetingEl.isVisible()) {
			context.setToolBigBlueButtonModeratorStartsMeeting(bigBlueButtonModeratorStartsMeetingEl.isKeySelected(KEY_ON));
		} else {
			context.setToolBigBlueButtonModeratorStartsMeeting(false);
		}
		
		context.select(SettingsBulkEditable.toolZoom, zoomEl != null && zoomEl.isVisible());
		if (zoomEl != null && zoomEl.isVisible()) {
			context.setToolZoom(zoomEl.isKeySelected(KEY_ON));
		}
		
		context.select(SettingsBulkEditable.toolBlog, blogEl.isVisible() && blogEl.isOneSelected());
		if (blogEl.isVisible() && blogEl.isOneSelected()) {
			context.setToolBlog(Replacement.valueOf(blogEl.getSelectedKey()));
			if (blogKeyCont.isVisible()) {
				context.setToolBlogKey(blogEntry.getSoftkey());
			}
		}
		
		context.select(SettingsBulkEditable.toolWiki, wikiEl.isVisible() && wikiEl.isOneSelected());
		if (wikiEl.isVisible() && wikiEl.isOneSelected()) {
			context.setToolWiki(Replacement.valueOf(wikiEl.getSelectedKey()));
			if (wikiKeyCont.isVisible()) {
				context.setToolWikiKey(wikiEntry.getSoftkey());
			}
		}
		
		context.select(SettingsBulkEditable.toolForum, forumEl.isVisible());
		if (forumEl.isVisible()) {
			context.setToolForum(forumEl.isKeySelected(KEY_ON));
		}
		
		context.select(SettingsBulkEditable.toolDocuments, documentsEl.isVisible());
		if (documentsEl.isVisible()) {
			context.setToolDocuments(documentsEl.isKeySelected(KEY_ON));
		}
		
		context.select(SettingsBulkEditable.toolChat, chatEl.isVisible());
		if (chatEl.isVisible()) {
			context.setToolChat(chatEl.isKeySelected(KEY_ON));
		}
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	private void doSelectBlog(UserRequest ureq) {
		blogSearchCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq, BlogFileResource.TYPE_NAME,
				translate("settings.bulk.toolbar.blog.select.title"));
		listenTo(blogSearchCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				blogSearchCtrl.getInitialComponent(), true, translate("settings.bulk.toolbar.blog.select.title"));
		cmc.activate();
	}

	private void doSelectWiki(UserRequest ureq) {
		wikiSearchCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq, WikiResource.TYPE_NAME,
				translate("settings.bulk.toolbar.wiki.select.title"));
		listenTo(wikiSearchCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				wikiSearchCtrl.getInitialComponent(), true, translate("settings.bulk.toolbar.wiki.select.title"));
		cmc.activate();
	}

}
