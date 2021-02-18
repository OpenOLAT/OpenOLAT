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
package org.olat.admin.sysinfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.NewControllerFactory;
import org.olat.admin.sysinfo.gui.LargeFilesAgeCellRenderer;
import org.olat.admin.sysinfo.gui.LargeFilesLockedCellRenderer;
import org.olat.admin.sysinfo.gui.LargeFilesNameCellRenderer;
import org.olat.admin.sysinfo.gui.LargeFilesRevisionCellRenderer;
import org.olat.admin.sysinfo.gui.LargeFilesSendMailCellRenderer;
import org.olat.admin.sysinfo.gui.LargeFilesSizeCellRenderer;
import org.olat.admin.sysinfo.gui.LargeFilesTrashedCellRenderer;
import org.olat.admin.sysinfo.model.LargeFilesTableContentRow;
import org.olat.admin.sysinfo.model.LargeFilesTableModel;
import org.olat.admin.sysinfo.model.LargeFilesTableModel.LargeFilesTableColumns;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.vfs.VFSContextInfo;
import org.olat.core.commons.services.vfs.VFSFilterKeys;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryModule;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.commons.services.vfs.impl.VFSContextInfoUnknownPathResolver;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExtendedFlexiTableSearchController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.modules.co.ContactFormController;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 23 Dec 2019<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
public class LargeFilesController extends FormBasicController implements ExtendedFlexiTableSearchController {

	private final AtomicInteger counter = new AtomicInteger();

	private FlexiTableElement largeFilesTableElement;
	private LargeFilesTableModel largeFilesTableModel;

	private SingleSelection trashedSelection;
	private SingleSelection revisionSelection; 
	private SingleSelection lockedSelection;
	private DateChooser createdAtNewerChooser;
	private DateChooser createdAtOlderChooser;
	private DateChooser editedAtNewerChooser;
	private DateChooser editedAtOlderChooser;
	private DateChooser lockedAtNewerChooser;
	private DateChooser lockedAtOlderChooser;
	private TextElement downloadCountMinEl;
	private TextElement revisionCountMinEl;
	private TextElement maxResultEl;
	private TextElement minSizeEl;
	private FormLink resetButton;
	private FormLink cleanupMetadataButton;

	private List<LargeFilesTableContentRow> rows;

	private CloseableModalController cmc;
	private ContactFormController contactCtrl;
	private DialogBoxController confirmMetadataCleanupBox;
	private CloseableCalloutWindowController pathInfoCalloutCtrl;

	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private VFSRepositoryModule vfsRepositoryModule;
	@Autowired
	private TaskExecutorManager taskExecutorManager;

	public LargeFilesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "large_files");
		initForm(ureq);
		updateModel();
	}

	public void updateModel() {
		rows = new ArrayList<>();
		
		int maxResults = 100;
		int downloadCountMin = 0;
		int minSize = 0;
		
		Long revisionsCountMin = Long.valueOf(0);

		if(StringHelper.containsNonWhitespace(maxResultEl.getValue())) {
			maxResults = Integer.parseInt(maxResultEl.getValue());
		}
		if(StringHelper.containsNonWhitespace(minSizeEl.getValue())) {
			minSize = (int) (Double.parseDouble(minSizeEl.getValue()) * Formatter.BYTE_UNIT * Formatter.BYTE_UNIT);
		}
		if(StringHelper.containsNonWhitespace(downloadCountMinEl.getValue())) {
			downloadCountMin = Integer.parseInt(downloadCountMinEl.getValue());
		}
		if(StringHelper.containsNonWhitespace(revisionCountMinEl.getValue())) {
			revisionsCountMin = Long.parseLong(revisionCountMinEl.getValue());
		}

		if(!revisionSelection.getSelectedKey().equals(VFSFilterKeys.REVISIONS.name())) {
			List<VFSMetadata> files = vfsRepositoryService.getLargestFiles(maxResults, 
					createdAtNewerChooser.getDate(), createdAtOlderChooser.getDate(),
					editedAtNewerChooser.getDate(), editedAtOlderChooser.getDate(),
					lockedAtNewerChooser.getDate(), lockedAtOlderChooser.getDate(),
					trashedSelection.getSelectedKey(), lockedSelection.getSelectedKey(),
					downloadCountMin, revisionsCountMin, minSize);

			for(VFSMetadata file:files) {
				LargeFilesTableContentRow contentRow = new LargeFilesTableContentRow(file, getLocale());

				String[] path = contentRow.getPath().split("/");

				StringBuilder sb = new StringBuilder(path[0]);
				if(path.length > 1) {
					sb.append("/").append(path[1]);
					if(path.length > 2) {
						sb.append("/...");
					}
				}

				FormLink contextInfo = uifactory.addFormLink("contextinfo_" + counter.incrementAndGet(), "contextInfo", contentRow.getContext(), null, null, Link.NONTRANSLATED);
				contextInfo.setUserObject(contentRow);
				contentRow.setContextInfo(contextInfo);
				
				contentRow.setShowPath(sb.toString());
				
				rows.add(contentRow);
			}
		}

		if(!revisionSelection.getSelectedKey().equals(VFSFilterKeys.FILES.name())) {
			List<VFSRevision> revisions = vfsRepositoryService.getLargestRevisions(maxResults, 
					createdAtNewerChooser.getDate(), createdAtOlderChooser.getDate(),
					editedAtNewerChooser.getDate(), editedAtOlderChooser.getDate(),
					lockedAtNewerChooser.getDate(), lockedAtOlderChooser.getDate(),
					trashedSelection.getSelectedKey(), lockedSelection.getSelectedKey(),
					downloadCountMin, revisionsCountMin, minSize);

			for(VFSRevision revision:revisions) {
				LargeFilesTableContentRow contentRow = new LargeFilesTableContentRow(revision, getLocale());

				String[] path = contentRow.getPath().split("/");

				StringBuilder sb = new StringBuilder(path[0]);
				if(path.length > 1) {
					sb.append("/").append(path[1]);
					if(path.length > 2) {
						sb.append("/...");
					}
				}
				
				FormLink contextInfo = uifactory.addFormLink("contextinfo_" + counter.incrementAndGet(), "contextInfo", contentRow.getContext(), null, null, Link.NONTRANSLATED);
				contextInfo.setUserObject(contentRow);
				contentRow.setContextInfo(contextInfo);
				
				contentRow.setShowPath(sb.toString());
				
				rows.add(contentRow);
			}
		}

		Collections.sort(rows, (row1, row2) -> (row2.getSize().intValue() - row1.getSize().intValue()));

		if(maxResults != 0 && maxResults < rows.size()) {
			rows = rows.subList(0, maxResults);
		}

		largeFilesTableModel.setObjects(rows);
		largeFilesTableElement.reset(true, true, true);
	}

	private void resetForm() {
		createdAtNewerChooser.reset();
		createdAtOlderChooser.reset();
		lockedAtNewerChooser.reset();
		lockedAtOlderChooser.reset();
		editedAtNewerChooser.reset();
		editedAtOlderChooser.reset();	
		revisionCountMinEl.reset();
		downloadCountMinEl.reset();
		trashedSelection.reset();
		lockedSelection.reset();
		revisionSelection.reset();
		maxResultEl.reset();
		minSizeEl.reset();

		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		cleanupMetadataButton = uifactory.addFormLink("metadata.cleanup", formLayout, Link.BUTTON);
		cleanupMetadataButton.getComponent().isSuppressDirtyFormWarning();

		FormLayoutContainer leftContainer = FormLayoutContainer.createDefaultFormLayout_6_6("filter_left", getTranslator());
		leftContainer.setRootForm(mainForm);
		formLayout.add(leftContainer);

		FormLayoutContainer rightContainer = FormLayoutContainer.createDefaultFormLayout_6_6("filter_right", getTranslator());
		leftContainer.setRootForm(mainForm);
		formLayout.add(rightContainer);

		FormLayoutContainer filterButtonLayout = FormLayoutContainer.createButtonLayout("filter_buttons", getTranslator());
		leftContainer.setRootForm(mainForm);
		formLayout.add(filterButtonLayout);

		// Left part of the filter
		createdAtNewerChooser = uifactory.addDateChooser("largefiles.filter.created.newer", "largefiles.filter.created.newer", null, leftContainer);
		editedAtNewerChooser = uifactory.addDateChooser("largefiles.filter.edited.newer", "largefiles.filter.edited.newer", null, leftContainer);
		lockedAtNewerChooser = uifactory.addDateChooser("largefiles.filter.locked.newer", null, leftContainer);
		revisionCountMinEl = uifactory.addTextElement("largefiles.filter.revision.count.min", 4, "", leftContainer);
		downloadCountMinEl = uifactory.addTextElement("largefiles.filter.download.count.min", 8, "", leftContainer);
		maxResultEl = uifactory.addTextElement("largefiles.filter.results.max", 5, "100", leftContainer);
		minSizeEl = uifactory.addTextElement("largefiles.filter.size.min", 18, "", leftContainer);


		// Right part of the filter
		createdAtOlderChooser = uifactory.addDateChooser("largefiles.filter.created.older", "largefiles.filter.created.older", null, rightContainer);
		editedAtOlderChooser = uifactory.addDateChooser("largefiles.filter.edited.older", "largefiles.filter.edited.older", null, rightContainer);
		lockedAtOlderChooser = uifactory.addDateChooser("largefiles.filter.locked.older", null, rightContainer);

		
		KeyValues deletedKeys = new KeyValues();
		deletedKeys.add(KeyValues.entry(VFSFilterKeys.DELETED.name(), translate("largefiles.filter.trashed.only")));
		deletedKeys.add(KeyValues.entry(VFSFilterKeys.NOT_DELETED.name(), translate("largefiles.filter.trashed.not")));
		deletedKeys.add(KeyValues.entry(VFSFilterKeys.BOTH_DELETED.name(), translate("largefiles.filter.trashed.both")));
		
		trashedSelection = uifactory.addRadiosHorizontal("largefiles.filter.trashed", "largefiles.filter.trashed", rightContainer, deletedKeys.keys(), deletedKeys.values());
		trashedSelection.select(VFSFilterKeys.BOTH_DELETED.name(), true);

		KeyValues revisionKeys = new KeyValues();
		revisionKeys.add(KeyValues.entry(VFSFilterKeys.REVISIONS.name(), translate("largefiles.filter.revision.only")));
		revisionKeys.add(KeyValues.entry(VFSFilterKeys.FILES.name(), translate("largefiles.filter.revision.not")));
		revisionKeys.add(KeyValues.entry(VFSFilterKeys.BOTH_REVISIONS_FILES.name(), translate("largefiles.filter.revision.both")));
		
		revisionSelection = uifactory.addRadiosHorizontal("largefiles.filter.revision", "largefiles.filter.revision", rightContainer, revisionKeys.keys(), revisionKeys.values());
		revisionSelection.select(VFSFilterKeys.BOTH_REVISIONS_FILES.name(), true);

		KeyValues lockedValues = new KeyValues();
		lockedValues.add(KeyValues.entry(VFSFilterKeys.LOCKED.name(), translate("largefiles.filter.locked.only")));
		lockedValues.add(KeyValues.entry(VFSFilterKeys.NOT_LOCKED.name(), translate("largefiles.filter.locked.not")));
		lockedValues.add(KeyValues.entry(VFSFilterKeys.BOTH_LOCKED.name(), translate("largefiles.filter.locked.both")));
		
		lockedSelection = uifactory.addRadiosHorizontal("largefiles.filter.locked", rightContainer, lockedValues.keys(), lockedValues.values());
		lockedSelection.select(VFSFilterKeys.BOTH_LOCKED.name(), true);

		// Filter buttons
		uifactory.addFormSubmitButton("largefiles.filter.button.search", filterButtonLayout);
		resetButton = uifactory.addFormLink("largefiles.filter.button.reset", filterButtonLayout, Link.BUTTON);

		// Tabled
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		DefaultFlexiColumnModel column;

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.uuid));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, LargeFilesTableColumns.name, new LargeFilesNameCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, LargeFilesTableColumns.size, new LargeFilesSizeCellRenderer(vfsRepositoryModule)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, LargeFilesTableColumns.contextInfo));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.showPath));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.age, new LargeFilesAgeCellRenderer()));

		column = new DefaultFlexiColumnModel(false, LargeFilesTableColumns.trashed, new LargeFilesTrashedCellRenderer());
		column.setIconHeader(CSSHelper.getIconCssClassFor(CSSHelper.CSS_CLASS_TRASHED));
		columnsModel.addFlexiColumnModel(column);

		column = new DefaultFlexiColumnModel(false, LargeFilesTableColumns.revision, new LargeFilesRevisionCellRenderer());
		column.setIconHeader(CSSHelper.getIconCssClassFor(CSSHelper.CSS_CLASS_REVISION));
		columnsModel.addFlexiColumnModel(column);

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.revisionNr));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.revisionComment));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.fileCategory));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.fileType));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.downloadCount));

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.author, "selectAuthor"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.license));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.language));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.source));

		column = new DefaultFlexiColumnModel(false, LargeFilesTableColumns.locked, new LargeFilesLockedCellRenderer());
		column.setIconHeader(CSSHelper.getIconCssClassFor(CSSHelper.CSS_CLASS_LOCKED));
		columnsModel.addFlexiColumnModel(column);

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.lockedAt));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.lockedBy, "selectLockedBy"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.creator));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.publisher));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.pubDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.createdAt));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, LargeFilesTableColumns.lastModifiedAt));
		
		DefaultFlexiColumnModel sendMail = new DefaultFlexiColumnModel(true, LargeFilesTableColumns.sendMail, "sendMail", new LargeFilesSendMailCellRenderer());
		sendMail.setAlwaysVisible(true);
		sendMail.setIconHeader(CSSHelper.getIconCssClassFor(CSSHelper.CSS_CLASS_MAIL));
		columnsModel.addFlexiColumnModel(sendMail);

		largeFilesTableModel = new LargeFilesTableModel(columnsModel, getLocale());
		largeFilesTableElement = uifactory.addTableElement(getWindowControl(), "large_files", largeFilesTableModel, getTranslator(), formLayout);

		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(LargeFilesTableColumns.size.name(), false));
		sortOptions.setFromColumnModel(true);
		largeFilesTableElement.setSortSettings(sortOptions);
		largeFilesTableElement.setAndLoadPersistedPreferences(ureq, "admin-large-files-list");	
		largeFilesTableElement.setSearchEnabled(false);
		largeFilesTableElement.setExportEnabled(true);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		updateModel();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == largeFilesTableElement) {
			if(event instanceof SelectionEvent) {
				SelectionEvent te = (SelectionEvent) event;
				String cmd = te.getCommand();
				LargeFilesTableContentRow contentRow = largeFilesTableModel.getObject(te.getIndex());
				if("selectAuthor".equals(cmd)) {
					if (contentRow.getFileInitializedBy() != null) {
						openUser(ureq, contentRow.getFileInitializedBy().getKey());
					}
				} else if("selectLockedBy".equals(cmd)) {
					if (contentRow.getLockedBy() != null) {
						openUser(ureq, contentRow.getLockedBy().getKey());
					}
				} else if("sendMail".equals(cmd)) {
					if (contentRow.getFileInitializedBy() != null) {
						contactUser(ureq, contentRow.getFileInitializedBy());
					}
				}
			}
		} else if(source == resetButton) {
			resetForm();
		} else if(cleanupMetadataButton == source) {
			doConfirmMetadataCleanup(ureq);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink) source;

			if("contextInfo".equals(link.getCmd())) {
				removeAsListenerAndDispose(pathInfoCalloutCtrl);

				LargeFilesTableContentRow row = (LargeFilesTableContentRow) link.getUserObject();

				CalloutSettings settings = new CalloutSettings(false);
				VFSContextInfo contextInfo = vfsRepositoryService.getContextInfoFor(row.getPath(), getLocale());			
				VelocityContainer contextInfoContainer = createVelocityContainer("large_files_context_info");
				contextInfoContainer.contextPut("contextInfo", contextInfo);
				contextInfoContainer.contextPut("row", row);

				pathInfoCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),contextInfoContainer, link.getFormDispatchId(), "", true, "", settings);
				listenTo(pathInfoCalloutCtrl);
				pathInfoCalloutCtrl.activate();
			}

		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest  ureq, Controller source, Event event) {
		if (source == cmc) {
			cleanUp();
		} else if (source == contactCtrl) {
			cmc.deactivate();
			cleanUp();
		} else if(source == confirmMetadataCleanupBox) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				doCleanupMetadata();
			}
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(pathInfoCalloutCtrl);
		cmc = null;
		contactCtrl = null;
		pathInfoCalloutCtrl = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOK = super.validateFormLogic(ureq);

		if(StringHelper.containsNonWhitespace(maxResultEl.getValue())) {
			try {
				if(Integer.parseInt(maxResultEl.getValue()) <= 0) {
					maxResultEl.setErrorKey("largefiles.filter.error.small", null);
					allOK &= false;
				}
			} catch (Exception e) {
				maxResultEl.setErrorKey("largefiles.filter.error.letter", null);
				allOK &= false;
			}
		}

		if(StringHelper.containsNonWhitespace(minSizeEl.getValue())) {
			try {
				if(Double.parseDouble(minSizeEl.getValue()) <= 0) {
					minSizeEl.setErrorKey("largefiles.filter.error.small", null);
					allOK &= false;
				}
			} catch (Exception e) {
				minSizeEl.setErrorKey("largefiles.filter.error.letter", null);
				allOK &= false;
			}
		}

		if(StringHelper.containsNonWhitespace(downloadCountMinEl.getValue())) {
			try {
				if(Integer.parseInt(downloadCountMinEl.getValue()) <= 0) {
					downloadCountMinEl.setErrorKey("largefiles.filter.error.small", null);
					allOK &= false;
				}
			} catch (Exception e) {
				downloadCountMinEl.setErrorKey("largefiles.filter.error.letter", null);
				allOK &= false;
			}
		}

		if(StringHelper.containsNonWhitespace(revisionCountMinEl.getValue())) {
			try {
				if(Integer.parseInt(revisionCountMinEl.getValue()) <= 0) {
					revisionCountMinEl.setErrorKey("largefiles.filter.error.small", null);
					allOK &= false;
				}
			} catch (Exception e) {
				revisionCountMinEl.setErrorKey("largefiles.filter.error.letter", null);
				allOK &= false;
			}
		}

		return allOK;
	}

	private void contactUser(UserRequest ureq, Identity user) {
		removeAsListenerAndDispose(cmc);

		ContactMessage cmsg = new ContactMessage(getIdentity());
		String fullName = user.getUser().getFirstName() + " " + user.getUser().getLastName();
		ContactList contactList = new ContactList(fullName);
		contactList.add(user);
		cmsg.addEmailTo(contactList);
		cmsg.setSubject(translate("largefiles.mail.subject"));

		String bodyStart = translate("largefiles.mail.start", new String[] {user.getUser().getFirstName() + " " + user.getUser().getLastName()});
		StringBuilder bodyFiles = new StringBuilder(5000);
		String bodyEnd = translate("largefiles.mail.end");

		bodyFiles.append("<ul>");
		for(LargeFilesTableContentRow row:rows) {
			if (row.getFileInitializedBy() != null && row.getFileInitializedBy().equals(user)) {
				bodyFiles.append("<li><strong>" + row.getName() + "</strong>")
					.append("<ul>")
						.append("<li>")
							.append("Typ: " + row.getContext())
						.append("</li>")
						.append("<li>")
							.append("Size: " + "<strong>" + Formatter.formatBytes(row.getSize()) + "</strong>")
						.append("</li>")
						.append("<li>")
							.append("Path: " + row.getPath())
						.append("</li>");
						if (!row.getContext().equals(VFSContextInfoUnknownPathResolver.UNKNOWN_TYPE)) {
							bodyFiles.append("<li>")
								.append("<span>URL: " + "<a href='" + vfsRepositoryService.getContextInfoFor(row.getPath(), getLocale()).getContextUrl() + "'>" + vfsRepositoryService.getContextInfoFor(row.getPath(), getLocale()).getContextUrl() + "</a></span>")
							.append("</li>");
						}
					bodyFiles.append("<ul>")
				.append("</li>");
			}
		}
		bodyFiles.append("</ul>");

		cmsg.setBodyText(bodyStart + bodyFiles.toString() + bodyEnd);
		contactCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, cmsg);
		listenTo(contactCtrl);
		cmc = new CloseableModalController(getWindowControl(), "close", contactCtrl.getInitialComponent());
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmMetadataCleanup(UserRequest ureq) {
		String msg = translate("confirm.cleanup.metadata");
		confirmMetadataCleanupBox = activateYesNoDialog(ureq, translate("confirm.cleanup.metadata.title"), msg, confirmMetadataCleanupBox);
	}
	
	private void doCleanupMetadata() {
		cleanupMetadataButton.setIconLeftCSS("o_icon o_icon_busy o_icon-spin");
		cleanupMetadataButton.setEnabled(false);
		
		taskExecutorManager.execute(() -> {
			vfsRepositoryService.cleanMetadatas();
			if(cleanupMetadataButton != null) {
				cleanupMetadataButton.setIconLeftCSS("");
				cleanupMetadataButton.setEnabled(true);
			}
		});
	}

	@Override
	public void setEnabled(boolean enable) {
		// Nothing do to here
	}

	@Override
	public List<String> getConditionalQueries() {
		return Collections.emptyList();
	}

	private void openUser(UserRequest ureq, Long userKey) {
		NewControllerFactory.getInstance().launch("[UserAdminSite:0][usearch:0][table:0][Identity:" + userKey.toString() + "]", ureq, getWindowControl());
	}
}
