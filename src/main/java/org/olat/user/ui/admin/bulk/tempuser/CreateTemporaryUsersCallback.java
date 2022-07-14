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
package org.olat.user.ui.admin.bulk.tempuser;

import java.io.OutputStream;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.control.winmgr.ShowInfoCommand;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 janv. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CreateTemporaryUsersCallback implements StepRunnerCallback {
	
	private final Translator translator;
	private final CreateTemporaryUsers temporaryUsers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private OrganisationService organisationService;
	
	public CreateTemporaryUsersCallback(CreateTemporaryUsers temporaryUsers, Translator translator) {
		this.translator = Util.createPackageTranslator(CreateTemporaryUsersCallback.class, translator.getLocale(), translator);
		this.temporaryUsers = temporaryUsers;
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		List<TransientIdentity> identities = temporaryUsers.getValidatedIdentities();
		if(identities != null && !identities.isEmpty()) {
			Organisation organisation = temporaryUsers.getOrganisation();
			if(organisation == null) {
				organisation = organisationService.getDefaultOrganisation();
			}
			for(TransientIdentity identity:identities) {
				doCreateAndPersistIdentity(identity, organisation);
			}
			
			MediaResource report = new TemporaryUsersMediaResources("Temp_users.xlsx", temporaryUsers, translator);
			ureq.getDispatchResult().setResultingMediaResource(report);
			
			if(identities.size() == 1) {
				showInfo(wControl, "info.user.created", 1);
			} else if(identities.size() > 1) {
				showInfo(wControl, "info.users.created", identities.size());
			}
		}
		return StepsMainRunController.DONE_MODIFIED;
	}
	
	private void showInfo(WindowControl wControl, String i18nKey, int numOfUsers) {
		String title = translator.translate("info.header");
		String msg = translator.translate(i18nKey, Integer.toString(numOfUsers));
		wControl.getWindowBackOffice().sendCommandTo(new ShowInfoCommand(title, msg));
	}
	
	private Identity doCreateAndPersistIdentity(TransientIdentity identity, Organisation organisation) {
		User newUser = userManager.createUser(identity.getFirstName(), identity.getLastName(), null);
		newUser.getPreferences().setLanguage(identity.getLanguage());
		newUser.getPreferences().setInformSessionTimeout(true);
		return securityManager.createAndPersistIdentityAndUserWithOrganisation(identity.getName(), identity.getName(), null, newUser,
				BaseSecurityModule.getDefaultAuthProviderIdentifier(), BaseSecurity.DEFAULT_ISSUER, identity.getName(), identity.getPassword(),
				organisation, identity.getExpirationDate());
	}
	
	private static class TemporaryUsersMediaResources extends OpenXMLWorkbookResource {
		
		private static final Logger log = Tracing.createLoggerFor(TemporaryUsersMediaResources.class);
		
		private final CreateTemporaryUsers users;
		private final Translator translator;
		
		public TemporaryUsersMediaResources(String filename, CreateTemporaryUsers users, Translator translator) {
			super(filename);
			this.users = users;
			this.translator = translator;
		}

		@Override
		protected void generate(OutputStream out) {
			try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
				OpenXMLWorksheet sheet = workbook.nextWorksheet();
				sheet.setHeaderRows(1);
				generateHeaders(sheet);
				generateData(sheet, workbook);
			} catch (Exception e) {
				log.error("", e);
			}
		}
		
		protected void generateHeaders(OpenXMLWorksheet sheet) {
			Row headerRow = sheet.newRow();
			headerRow.addCell(0, translator.translate("table.header.username"));
			headerRow.addCell(1, translator.translate("table.header.firstname"));
			headerRow.addCell(2, translator.translate("table.header.lastname"));
			headerRow.addCell(3, translator.translate("table.header.password"));
			if(users.getExpirationDate() != null) {
				headerRow.addCell(4, translator.translate("table.header.expiration"));
			}
		}
		
		protected void generateData(OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
			List<TransientIdentity> identities = users.getValidatedIdentities();
			for(TransientIdentity identity:identities) {
				Row row = sheet.newRow();
				row.addCell(0, identity.getName());
				row.addCell(1, identity.getFirstName());
				row.addCell(2, identity.getLastName());
				row.addCell(3, identity.getPassword());
				if(users.getExpirationDate() != null) {
					row.addCell(4, identity.getExpirationDate(), workbook.getStyles().getDateStyle());
				}
			}
		}
	}
}
