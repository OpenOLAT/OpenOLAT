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
package org.olat.modules.curriculum.ui;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.grading.ui.component.RepositoryEntryComparator;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmCurriculumElementDeleteController extends FormBasicController {
	
	private FormLink deleteButton;
	private FormLink downloadLink;
	private FormLink additionalReferencesLink;
	
	private final int numOfChildren;
	private final CurriculumElementRow rowToDelete;
	private final List<RepositoryEntry> references;
	
	private CloseableCalloutWindowController calloutCtrl;
	private AdditionalReferencesController addReferencesCtrl;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public ConfirmCurriculumElementDeleteController(UserRequest ureq, WindowControl wControl, CurriculumElementRow rowToDelete) {
		super(ureq, wControl, "confirm_delete_curriculum_element", Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		this.rowToDelete = rowToDelete;
		
		List<CurriculumElement> children = curriculumService.getCurriculumElements(rowToDelete);
		numOfChildren = children.size();
		references = curriculumService.getRepositoryEntries(rowToDelete);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			
			String msgI18nKey;
			if(numOfChildren == 0) {
				msgI18nKey = "confirmation.delete.element";
			} else if(numOfChildren == 1) {
				msgI18nKey = "confirmation.delete.element.child";
			} else {
				msgI18nKey = "confirmation.delete.element.children";
			}
			String msg = translate(msgI18nKey, rowToDelete.getDisplayName(), Integer.toString(numOfChildren));
			layoutCont.contextPut("msg", StringHelper.xssScan(msg));
			
			List<String> resources = new ArrayList<>(6);
			int maxRefs = references.size() <= 5 ? references.size() : 4;
			for(int i=0; i<maxRefs; i++) {
				resources.add(references.get(i).getDisplayname());
			}
			layoutCont.contextPut("resources", resources);
			
			String referencesI18nMsg = (maxRefs <= 1) ? "confirmation.delete.reference" : "confirmation.delete.references";
			String referenceMsg = translate(referencesI18nMsg);
			layoutCont.contextPut("referenceMsg", referenceMsg);

			if(references.size() > maxRefs) {
				String linkTitle = translate("additional.references", Integer.toString(references.size()));
				additionalReferencesLink = uifactory.addFormLink("additional.references", "refs", linkTitle,
						null, layoutCont, Link.NONTRANSLATED | Link.LINK);
			}
			if(!references.isEmpty()) {
				downloadLink = uifactory.addFormLink("additional.references.download", "download", "additional.references.download",
						null, layoutCont, Link.BUTTON);
				downloadLink.setIconLeftCSS("o_icon o_icon_download");
				downloadLink.setElementCssClass("o_button_ghost");
			}
		}

		deleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
		deleteButton.setElementCssClass("btn-danger");
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == addReferencesCtrl) {
			calloutCtrl.deactivate();
			cleanUp();
		} else if(source == calloutCtrl) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(addReferencesCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		addReferencesCtrl = null;
		calloutCtrl = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(deleteButton == source) {
			doDelete();
			fireEvent(ureq, Event.DONE_EVENT);
		} else if(additionalReferencesLink == source) {
			doOpenAdditionalReferences(ureq, additionalReferencesLink);
		} else if(downloadLink == source) {
			doDownload(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenAdditionalReferences(UserRequest ureq, FormLink link) {
		addReferencesCtrl = new AdditionalReferencesController(ureq, getWindowControl(), references);
		listenTo(addReferencesCtrl);

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				addReferencesCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doDelete() {
		curriculumService.deleteCurriculumElement(rowToDelete);
	}
	
	private void doDownload(UserRequest ureq) {
		String name = "resources_" + rowToDelete.getDisplayName();
		name = StringHelper.transformDisplayNameToFileSystemName(name);
		ReferencesMediaResources mediaResource = new ReferencesMediaResources(name, references, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(mediaResource);
	}
	
	private static class AdditionalReferencesController extends BasicController {
		
		public AdditionalReferencesController(UserRequest ureq, WindowControl wControl, List<RepositoryEntry> addReferences) {
			super(ureq, wControl);
			
			VelocityContainer mainVC = createVelocityContainer("additional_resources");
			List<String> resources = new ArrayList<>(addReferences.size());
			for(int i=4; i<addReferences.size(); i++) {
				resources.add(addReferences.get(i).getDisplayname());
			}
			mainVC.contextPut("resources", resources);
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			//
		}
	}
	
	private static class ReferencesMediaResources extends OpenXMLWorkbookResource {
		
		private static final Logger log = Tracing.createLoggerFor(ReferencesMediaResources.class);
		
		private final Translator translator;
		private final List<RepositoryEntry> entries;
		
		@Autowired
		private DB dbInstance;
		@Autowired
		private RepositoryService repositoryService;
		
		public ReferencesMediaResources(String name, List<RepositoryEntry> entries, Translator translator) {
			super(name);
			this.entries = entries;
			this.translator = translator;
		}

		@Override
		protected void generate(OutputStream out) {
			try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
				OpenXMLWorksheet sheet = workbook.nextWorksheet();
				sheet.setHeaderRows(1);
				generateHeaders(sheet);
				generateData(sheet);
			} catch (Exception e) {
				log.error("", e);
			}
		}
		
		private void generateHeaders(OpenXMLWorksheet sheet) {
			int col = 0;
			Row headerRow = sheet.newRow();
			headerRow.addCell(col++, translator.translate("table.header.id"));
			headerRow.addCell(col++, translator.translate("table.header.type"));
			headerRow.addCell(col++, translator.translate("table.header.displayname"));
			headerRow.addCell(col++, translator.translate("table.header.externalref"));
			headerRow.addCell(col++, translator.translate("table.header.status"));
			headerRow.addCell(col++, translator.translate("cif.organisations"));
			headerRow.addCell(col++, translator.translate("table.header.owners"));
			headerRow.addCell(col, translator.translate("table.header.owners.emails"));
		}
		
		private void generateData(OpenXMLWorksheet sheet) {
			Collections.sort(entries, new RepositoryEntryComparator(translator.getLocale()));
			for(RepositoryEntry entry:entries) {
				try {
					generateData(entry, sheet);
				} catch (Exception e) {
					log.error("", e);
				} finally {
					dbInstance.commitAndCloseSession();
				}
			}	
		}
		
		@Autowired
		private UserManager userManager;

		private void generateData(RepositoryEntry entry, OpenXMLWorksheet sheet) {
			int col = 0;
			Row row = sheet.newRow();
			row.addCell(col++, entry.getKey(), null);
			String type = entry.getOlatResource().getResourceableTypeName();
			type = NewControllerFactory.translateResourceableTypeName(type, translator.getLocale());
			row.addCell(col++, type);
			row.addCell(col++, entry.getDisplayname());
			row.addCell(col++, entry.getExternalRef());
			row.addCell(col++, translator.translate("table.status." + entry.getEntryStatus()));
			
			// Administrative access or organisations
			List<Organisation> reOrganisations = repositoryService.getOrganisations(entry);
			String orgs = reOrganisations.stream()
					.map(Organisation::getDisplayName)
					.filter(Objects::nonNull)
					.collect(Collectors.joining(", "));
			row.addCell(col++, orgs);
			
			// Owners
			List<Identity> ownersList = repositoryService.getMembers(entry, RepositoryEntryRelationType.all, GroupRoles.owner.name());
			String owners = ownersList.stream()
					.map(id -> userManager.getUserDisplayName(id))
					.filter(Objects::nonNull)
					.collect(Collectors.joining(", "));
			row.addCell(col++, owners);
			
			String emails = ownersList.stream()
					.map(Identity::getUser).map(User::getEmail)
					.filter(Objects::nonNull)
					.collect(Collectors.joining("; "));
			row.addCell(col, emails);
		}
	}
}
