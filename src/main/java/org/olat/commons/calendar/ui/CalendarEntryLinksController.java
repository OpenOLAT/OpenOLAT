/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.commons.calendar.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.linkchooser.CustomMediaChooserController;
import org.olat.core.commons.controllers.linkchooser.CustomMediaChooserFactory;
import org.olat.core.commons.controllers.linkchooser.URLChoosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.helpers.Settings;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.run.calendar.CourseLinkProviderController;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Aug 29, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CalendarEntryLinksController extends FormBasicController {

	private static final String COURSE = "course";
	private static final String GROUP = "group";
	private static final String CLOSE = "close";

	private DropdownItem addDropdown;
	private FormLink createLinkToCourse;
	private FormLink createLinkToCourseEl;
	private FormLink createLinkToGroup;
	private FormLink createLinkToLibrary;
	private FormLink createExternalLink;

	private final Collection<KalendarRenderWrapper> availableCalendars;
	private final String caller;
	private final Boolean isReadOnly;
	private final Boolean isNew;
	private final List<CalendarEntryLinkRow> calendarEntryLinkRows;
	private final List<CalendarEntryLinkRow> calendarRssLinkRows;
	private final List<CalendarEntryLinkRow> calendarCourseElLinkRows;
	private final List<CalendarEntryLinkRow> calendarLibraryDocLinkRows;
	private final List<CalendarEntryLinkRow> calendarExternalLinkRows;
	private LinkProvider activeLinkProvider;
	private final KalendarEvent kalendarEvent;
	private final CalendarEntryForm eventForm;
	private KalendarRenderWrapper chosenCalendarWrapper;
	private final CustomMediaChooserFactory customMediaChooserFactory;

	private CloseableModalController cmc;
	private DialogBoxController deleteDialogCtrl;
	private ExternalLinksController externalLinksController;
	private final CustomMediaChooserController customMediaChooserCtr;

	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDAO;

	protected CalendarEntryLinksController(UserRequest ureq, WindowControl wControl, KalendarEvent kalendarEvent,
										   KalendarRenderWrapper chosenCalendarWrapper, Collection<KalendarRenderWrapper> availableCalendars,
										   Form mainForm, String caller, CalendarEntryForm eventForm, boolean isNew) {
		super(ureq, wControl, LAYOUT_CUSTOM, "cal_links", mainForm);
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));

		this.kalendarEvent = kalendarEvent;
		this.availableCalendars = availableCalendars;
		this.caller = caller;
		this.eventForm = eventForm;
		this.isNew = isNew;
		if (chosenCalendarWrapper == null) {
			updateChosenCalendarWrapper();
		} else {
			this.chosenCalendarWrapper = chosenCalendarWrapper;
		}

		this.isReadOnly = chosenCalendarWrapper == null || chosenCalendarWrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_ONLY;
		this.customMediaChooserFactory = (CustomMediaChooserFactory) CoreSpringFactory.getBean(CustomMediaChooserFactory.class.getName());
		customMediaChooserCtr = customMediaChooserFactory.getInstance(ureq, getWindowControl());

		calendarEntryLinkRows = new ArrayList<>();
		calendarRssLinkRows = new ArrayList<>();
		calendarCourseElLinkRows = new ArrayList<>();
		calendarLibraryDocLinkRows = new ArrayList<>();
		calendarExternalLinkRows = new ArrayList<>();

		initForm(ureq);
		if (!isNew) {
			doForgeExistingLinks();
		} else {
			doCreateLinkToResource();
		}
		updateVisibility();
		loadLinkItems();
	}

	private void updateChosenCalendarWrapper() {
		chosenCalendarWrapper = availableCalendars.stream().filter(ac -> ac.getKalendar().getCalendarID().equals(eventForm.getChoosenKalendarID())).findFirst().orElse(null);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer topButtons = FormLayoutContainer.createButtonLayout("topButtons", getTranslator());
		topButtons.setRootForm(mainForm);
		formLayout.add("topButtons", topButtons);
		topButtons.setElementCssClass("o_button_group o_button_group_right");

		addDropdown = uifactory.addDropdownMenu("links.dropdown", null, topButtons, getTranslator());
		addDropdown.setOrientation(DropdownOrientation.right);
		addDropdown.setEmbbeded(true);

		createLinkToCourse = uifactory.addFormLink("cal.entry.link.title.course", formLayout, Link.LINK);
		addDropdown.addElement(createLinkToCourse);
		createLinkToGroup = uifactory.addFormLink("cal.entry.link.title.group", formLayout, Link.LINK);
		addDropdown.addElement(createLinkToGroup);

		createLinkToCourseEl = uifactory.addFormLink("cal.entry.link.title.course.el", formLayout, Link.LINK);
		addDropdown.addElement(createLinkToCourseEl);
		createLinkToCourseEl.setVisible(!isNew);
		createLinkToLibrary = uifactory.addFormLink("cal.entry.link.title.library", formLayout, Link.LINK);
		addDropdown.addElement(createLinkToLibrary);
		createLinkToLibrary.setVisible(!isNew);
		createExternalLink = uifactory.addFormLink("cal.entry.link.title.external", formLayout, Link.LINK);
		addDropdown.addElement(createExternalLink);
		createExternalLink.setVisible(!isNew);
	}

	private void updateVisibility() {
		boolean isAllowed = !isReadOnly && !CalendarManagedFlag.isManaged(kalendarEvent, CalendarManagedFlag.links);

		createLinkToCourseEl.setVisible(isAllowed);
		createLinkToLibrary.setVisible(isAllowed);
		createExternalLink.setVisible(isAllowed);
	}

	private void loadLinkItems() {
		calendarEntryLinkRows.clear();
		calendarEntryLinkRows.addAll(calendarRssLinkRows);
		calendarEntryLinkRows.addAll(calendarCourseElLinkRows);
		calendarEntryLinkRows.addAll(calendarLibraryDocLinkRows);
		calendarEntryLinkRows.addAll(calendarExternalLinkRows);
		flc.contextPut("rows", calendarEntryLinkRows);

		boolean isNewCourseItemAllowed = calendarEntryLinkRows.stream().noneMatch(row -> row.getCalendarEntryLinkType().equals(CalendarEntryLinkType.LINK_TO_COURSE));
		boolean isNewGroupItemAllowed = calendarEntryLinkRows.stream().noneMatch(row -> row.getCalendarEntryLinkType().equals(CalendarEntryLinkType.LINK_TO_GROUP));
		createLinkToCourse.setVisible(getChosenCalendarType().equals(COURSE) && isNewCourseItemAllowed);
		createLinkToCourseEl.setVisible(getChosenCalendarType().equals(COURSE) && !isNew);
		createLinkToGroup.setVisible(getChosenCalendarType().equals(GROUP) && isNewGroupItemAllowed);

		addDropdown.setVisible(((List<FormItem>) addDropdown.getFormItems()).stream().anyMatch(FormItem::isVisible));
	}

	private CalendarEntryLinkRow forgeRow(CalendarEntryLinkRow calEntryLinkRow, String title, String url) {
		TextElement linkTitleEl = uifactory.addTextElement("linkTitle_" + calEntryLinkRow.getKey(), calEntryLinkRow.getCalendarEntryLinkType().i18nKey(), 100, title, flc);
		String linkUrl = "<span class='o_copy_code o_nowrap'><input type='text' value='" + url + "' onclick='this.select()'/></span>";
		StaticTextElement linkUrlEl = uifactory.addStaticTextElement("linkUrl_" + calEntryLinkRow.getKey(), "cal.entry.link.url", linkUrl, flc);
		linkUrlEl.setEnabled(false);
		calEntryLinkRow.setLinkTitleEl(linkTitleEl);
		calEntryLinkRow.setLinkUrlEl(linkUrlEl);
		forgeDeleteLink(calEntryLinkRow);
		return calEntryLinkRow;
	}

	private void forgeDeleteLink(CalendarEntryLinkRow row) {
		if (Boolean.TRUE.equals(isReadOnly)) return;

		FormLink link = uifactory.addFormLink("del_" + row.getKey(), "deleteLink", "", null, flc, Link.BUTTON + Link.NONTRANSLATED);
		link.setIconLeftCSS("o_icon o_icon-lg o_icon_deleted");
		link.setGhost(true);
		row.setDeleteLinkEl(link);
		link.setUserObject(row);
	}

	private String getChosenCalendarType() {
		return eventForm.getCalendarIdToCalendarType().get(eventForm.getChoosenKalendarID());
	}

	private void doCreateLinkToResource() {
		calendarRssLinkRows.clear();
		updateChosenCalendarWrapper();
		if (chosenCalendarWrapper != null) {
			String calendarType = getChosenCalendarType();
			List<KalendarEventLink> links = kalendarEvent.getKalendarEventLinks();
			String title = chosenCalendarWrapper.getDisplayName();
			String url = "";
			CalendarEntryLinkRow calendarEntryLinkRow = null;

			if (calendarType.equals(COURSE)) {
				Long courseId = repositoryEntryDAO.loadByResourceId("CourseModule", Long.valueOf(chosenCalendarWrapper.getKalendar().getCalendarID())).getKey();
				url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString("[RepositoryEntry:" + courseId + "]");
				calendarEntryLinkRow = new CalendarEntryLinkRow(url, CalendarEntryLinkType.LINK_TO_COURSE);
				KalendarEventLink courseCalEventLink = new KalendarEventLink(RepositoryEntry.class.getSimpleName(), url, title, url, "o_CourseModule_icon");
				if (links.stream().noneMatch(l -> l.getProvider().equals(RepositoryEntry.class.getSimpleName()))) {
					links.add(courseCalEventLink);
				}
			} else if (calendarType.equals(GROUP)) {
				url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString("[BusinessGroup:" + chosenCalendarWrapper.getKalendar().getCalendarID() + "]");
				calendarEntryLinkRow = new CalendarEntryLinkRow(chosenCalendarWrapper.getIdentifier(), CalendarEntryLinkType.LINK_TO_GROUP);
				KalendarEventLink groupCalEventLink = new KalendarEventLink(BusinessGroup.class.getSimpleName(), url, title, url, "o_icon_group");
				if (links.stream().noneMatch(l -> l.getProvider().equals(BusinessGroup.class.getSimpleName()))) {
					links.add(groupCalEventLink);
				}
			}
			if (calendarEntryLinkRow != null && StringHelper.containsNonWhitespace(url)) {
				calendarRssLinkRows.add(forgeRow(calendarEntryLinkRow, title, url));
			}
		}
	}

	private void loadActiveLinkProvider(UserRequest ureq) {
		String calendarID = eventForm.getChoosenKalendarID();
		KalendarRenderWrapper calWrapper = null;
		for (KalendarRenderWrapper availableCalendar : availableCalendars) {
			calWrapper = availableCalendar;
			if (calWrapper.getKalendar().getCalendarID().equals(calendarID)) {
				break;
			}
		}

		if (activeLinkProvider == null && calWrapper != null) {
			activeLinkProvider = calWrapper.createLinkProvider(ureq, getWindowControl());
			if (activeLinkProvider != null) {
				listenTo(activeLinkProvider);
				activeLinkProvider.setKalendarEvent(kalendarEvent);
				flc.put("linkprovider", activeLinkProvider.getInitialComponent());
				flc.contextPut("hasLinkProvider", Boolean.TRUE);
			} else {
				flc.contextPut("hasLinkProvider", Boolean.FALSE);
			}
		}
	}

	private void doStartCreateLinkToCourseEl(UserRequest ureq) {
		VelocityContainer linkVC = createVelocityContainer("calEditLinks");
		linkVC.contextPut("caller", caller);

		loadActiveLinkProvider(ureq);
		cmc = new CloseableModalController(getWindowControl(), translate(CLOSE), activeLinkProvider.getInitialComponent(), translate("tab.links"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doCreateLinkToCourseEl(UserRequest ureq) {
		loadActiveLinkProvider(ureq);
		calendarCourseElLinkRows.clear();
		Set<String> selectedKeys = activeLinkProvider.getMultiSelectTree().getSelectedKeys();

		for (String selectedKey : selectedKeys) {
			String subIdent = selectedKey.replaceAll(".*_", "");

			TreeNode selectedNode = activeLinkProvider.getMultiSelectTree().getTreeModel().getNodeById(selectedKey);
			RepositoryEntry entry = ((CourseLinkProviderController.LinkTreeNode) selectedNode).getEntry();
			if (entry == null) {
				continue;
			}
			String businessPath = "[RepositoryEntry:" + entry.getKey() + "]" + "[CourseNode:" + subIdent + "]";
			String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
			String title = selectedNode.getTitle();
			CalendarEntryLinkRow calendarEntryLinkRow = new CalendarEntryLinkRow(subIdent, CalendarEntryLinkType.LINK_TO_COURSE_ELEMENT);

			calendarCourseElLinkRows.add(forgeRow(calendarEntryLinkRow, title, url));
		}
		loadLinkItems();
	}

	private void doStartCreateLinkToLibraryDoc(UserRequest ureq) {
		if (CoreSpringFactory.containsBean(CustomMediaChooserFactory.class.getName())
				&& (customMediaChooserCtr != null)) {
			listenTo(customMediaChooserCtr);
			MediaLinksController mediaLinksController = new MediaLinksController(ureq, getWindowControl(), kalendarEvent, customMediaChooserFactory);
			listenTo(mediaLinksController);

			cmc = new CloseableModalController(getWindowControl(), translate(CLOSE), customMediaChooserCtr.getInitialComponent(), customMediaChooserCtr.getTabbedPaneTitle());
			listenTo(cmc);
			cmc.activate();
		}
	}

	private void doCreateLinkToLibraryDoc(String id, String title, String url) {
		if (StringHelper.containsNonWhitespace(url)) {
			CalendarEntryLinkRow calendarEntryLinkRow = new CalendarEntryLinkRow(id, CalendarEntryLinkType.LINK_TO_LIBRARY);
			calendarLibraryDocLinkRows.add(forgeRow(calendarEntryLinkRow, title, url));
		}
		loadLinkItems();
	}

	private void doStartCreateLinkToExternalRss(UserRequest ureq) {
		externalLinksController = new ExternalLinksController(ureq, getWindowControl(), kalendarEvent);
		listenTo(externalLinksController);

		cmc = new CloseableModalController(getWindowControl(), translate(CLOSE), externalLinksController.getInitialComponent(), translate("tab.links.extern"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doCreateLinkToExternalRss(UserRequest ureq) {
		//save externals links
		Kalendar cal = kalendarEvent.getCalendar();
		if (kalendarEvent.getCalendar() != null) {
			boolean doneSuccessfully = calendarManager.updateEventFrom(cal, kalendarEvent);
			if (doneSuccessfully) {
				List<KalendarEventLink> externalLinkList = kalendarEvent.getKalendarEventLinks()
						.stream()
						.filter(c -> c.getProvider().equals(ExternalLinksController.EXTERNAL_LINKS_PROVIDER))
						.toList();
				if (!externalLinkList.isEmpty()) {
					for (KalendarEventLink externalLink : externalLinkList) {
						CalendarEntryLinkRow row = new CalendarEntryLinkRow(externalLink.getId(), CalendarEntryLinkType.LINK_EXTERNAL);
						calendarExternalLinkRows.add(forgeRow(row, externalLink.getDisplayName(), externalLink.getURI()));
					}
				}
				loadLinkItems();
				fireEvent(ureq, Event.DONE_EVENT);
			} else {
				showError("cal.error.save");
				fireEvent(ureq, Event.FAILED_EVENT);
			}
		}
	}

	private void doForgeExistingLinks() {
		List<KalendarEventLink> kalendarEventLinks = kalendarEvent.getKalendarEventLinks();

		for (KalendarEventLink kalendarEventLink : kalendarEventLinks) {
			CalendarEntryLinkRow row;
			if (kalendarEventLink.getProvider().equals(RepositoryEntry.class.getSimpleName())) {
				row = new CalendarEntryLinkRow(kalendarEventLink.getId(), CalendarEntryLinkType.LINK_TO_COURSE);
				calendarRssLinkRows.add(forgeRow(row, kalendarEventLink.getDisplayName(), kalendarEventLink.getURI()));
			} else if (kalendarEventLink.getProvider().equals(BusinessGroup.class.getSimpleName())) {
				row = new CalendarEntryLinkRow(kalendarEventLink.getId(), CalendarEntryLinkType.LINK_TO_GROUP);
				calendarRssLinkRows.add(forgeRow(row, kalendarEventLink.getDisplayName(), kalendarEventLink.getURI()));
			} else if (kalendarEventLink.getProvider().equals("COURSE")) {
				row = new CalendarEntryLinkRow(kalendarEventLink.getId(), CalendarEntryLinkType.LINK_TO_COURSE_ELEMENT);
				calendarCourseElLinkRows.add(forgeRow(row, kalendarEventLink.getDisplayName(), kalendarEventLink.getURI()));
			} else if (kalendarEventLink.getProvider().equals(customMediaChooserCtr.getClass().getSimpleName())) {
				row = new CalendarEntryLinkRow(kalendarEventLink.getId(), CalendarEntryLinkType.LINK_TO_LIBRARY);
				calendarLibraryDocLinkRows.add(forgeRow(row, kalendarEventLink.getDisplayName(), kalendarEventLink.getURI()));
			} else if (kalendarEventLink.getProvider().equals(ExternalLinksController.EXTERNAL_LINKS_PROVIDER)) {
				row = new CalendarEntryLinkRow(kalendarEventLink.getId(), CalendarEntryLinkType.LINK_EXTERNAL);
				calendarExternalLinkRows.add(forgeRow(row, kalendarEventLink.getDisplayName(), kalendarEventLink.getURI()));
			}
		}
	}

	private void doConfirmDelete(UserRequest ureq, CalendarEntryLinkRow row) {
		List<String> buttons = new ArrayList<>();
		buttons.add(translate("delete"));
		buttons.add(translate("cancel"));

		deleteDialogCtrl = activateGenericDialog(ureq, translate("confirmation.delete"), translate("confirmation.delete.row.link", row.getLinkTitleEl().getValue()), buttons, deleteDialogCtrl);
		deleteDialogCtrl.setUserObject(row);
	}

	private void doDelete(CalendarEntryLinkRow row) {
		if (row.getCalendarEntryLinkType().equals(CalendarEntryLinkType.LINK_TO_COURSE)
				|| row.getCalendarEntryLinkType().equals(CalendarEntryLinkType.LINK_TO_GROUP)) {
			calendarRssLinkRows.remove(row);
		} else if (row.getCalendarEntryLinkType().equals(CalendarEntryLinkType.LINK_TO_COURSE_ELEMENT)) {
			calendarCourseElLinkRows.remove(row);
		} else if (row.getCalendarEntryLinkType().equals(CalendarEntryLinkType.LINK_TO_LIBRARY)) {
			calendarLibraryDocLinkRows.remove(row);
		} else if (row.getCalendarEntryLinkType().equals(CalendarEntryLinkType.LINK_EXTERNAL)) {
			calendarExternalLinkRows.remove(row);
		}
		kalendarEvent.getKalendarEventLinks().removeIf(k -> k.getId().contains(row.getKey()));
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == activeLinkProvider) {
			doCreateLinkToCourseEl(ureq);
			cmc.deactivate();
		} else if (source == customMediaChooserCtr) {
			boolean doneSuccessfully = true;
			if (event instanceof URLChoosenEvent urlEvent) {
				String url = urlEvent.getURL();
				List<KalendarEventLink> links = kalendarEvent.getKalendarEventLinks();

				String provider = customMediaChooserCtr.getClass().getSimpleName();
				String displayName = StringHelper.containsNonWhitespace(urlEvent.getDisplayName()) ? urlEvent.getDisplayName() : url;
				String uri = url.contains("://") ? url : (Settings.getServerContextPathURI() + url);
				String iconCssClass = urlEvent.getIconCssClass();
				if (!StringHelper.containsNonWhitespace(iconCssClass)) {
					iconCssClass = CSSHelper.createFiletypeIconCssClassFor(url);
				}

				links.add(new KalendarEventLink(provider, url, displayName, uri, iconCssClass));

				Kalendar cal = kalendarEvent.getCalendar();
				doneSuccessfully = calendarManager.updateEventFrom(cal, kalendarEvent);

				if (doneSuccessfully) {
					doCreateLinkToLibraryDoc(url, displayName, url);
					fireEvent(ureq, event);
				} else {
					showError("cal.error.save");
					fireEvent(ureq, Event.FAILED_EVENT);
				}
			}
			cmc.deactivate();
		} else if (source == externalLinksController) {
			doCreateLinkToExternalRss(ureq);
			cmc.deactivate();
		} else if (source == deleteDialogCtrl && (DialogBoxUIFactory.isOkEvent(event))) {
			doDelete((CalendarEntryLinkRow) deleteDialogCtrl.getUserObject());
			fireEvent(ureq, Event.CHANGED_EVENT);
			loadLinkItems();
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createLinkToCourse
				|| source == createLinkToGroup) {
			doCreateLinkToResource();
		} else if (source == createLinkToCourseEl) {
			doStartCreateLinkToCourseEl(ureq);
		} else if (source == createLinkToLibrary) {
			doStartCreateLinkToLibraryDoc(ureq);
		} else if (source == createExternalLink) {
			doStartCreateLinkToExternalRss(ureq);
		} else if (source instanceof FormLink link) {
			if ("deleteLink".equals(link.getCmd())) {
				doConfirmDelete(ureq, (CalendarEntryLinkRow) link.getUserObject());
			} else if (event == Event.CANCELLED_EVENT) {
				eventForm.setEntry(kalendarEvent);
				// user canceled, finish workflow
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if (source instanceof SingleSelection) {
			doCreateLinkToResource();
		}
		loadLinkItems();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// no need
	}

	/**
	 *
	 * @return all link rows
	 */
	public List<CalendarEntryLinkRow> getCalendarEntryLinkRows() {
		return calendarEntryLinkRows;
	}
}
