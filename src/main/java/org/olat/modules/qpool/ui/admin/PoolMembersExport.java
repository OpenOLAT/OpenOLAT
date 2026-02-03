/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.qpool.ui.admin;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.admin.securitygroup.gui.IdentitiesOfGroupTableDataModel;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QPoolService;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class PoolMembersExport extends OpenXMLWorkbookResource {

	private static final Logger log = Tracing.createLoggerFor(PoolMembersExport.class);

	private static final String usageIdentifyer = IdentitiesOfGroupTableDataModel.class.getCanonicalName();
	
	private final List<Pool> pools;
	private final Translator translator;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private QPoolService qPoolService;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public PoolMembersExport(String filename, List<Pool> pools, Roles roles, Translator translator) {
		super(filename);
		this.pools = pools;
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		this.userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser).stream()
				.filter(handler -> userManager.isMandatoryUserProperty(usageIdentifyer , handler))
				.toList();
		this.translator = userManager.getPropertyHandlerTranslator(translator);
	}

	@Override
	protected void generate(OutputStream out) {
		List<String> sheetsNames = List.of(translator.translate("export.members"), translator.translate("export.metadata"));
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 2, sheetsNames)) {
			OpenXMLWorksheet membersSheet = workbook.nextWorksheet();
			membersSheet.setHeaderRows(1);
			addMembersHeaders(membersSheet, workbook);
			for(Pool pool:pools) {
				addMembers(pool, membersSheet);
			}

			OpenXMLWorksheet metadataSheet = workbook.nextWorksheet();
			metadataSheet.setHeaderRows(1);
			addMetadataHeaders(metadataSheet, workbook);
			for(Pool pool:pools) {
				addMetadata(pool, metadataSheet, workbook);
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	private void addMembersHeaders(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		int col = 0;
		Row headerRow = exportSheet.newRow();
		headerRow.addCell(col++, translator.translate("export.pool"), workbook.getStyles().getHeaderStyle());
		for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
			String header = translator.translate(userPropertyHandler.i18nColumnDescriptorLabelKey());
			headerRow.addCell(col++, header, workbook.getStyles().getHeaderStyle());
		}
	}
	
	private void addMembers(Pool pool, OpenXMLWorksheet exportSheet) {
		List<Identity> owners = qPoolService.getOwners(pool);
		for(Identity owner:owners) {
			addMember(owner, pool, exportSheet);
		}
	}
	
	private void addMember(Identity owner, Pool pool, OpenXMLWorksheet exportSheet) {
		int col = 0;
		Row row = exportSheet.newRow();
		row.addCell(col++, pool.getName());
		for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
			String val = userPropertyHandler.getUserProperty(owner.getUser(), translator.getLocale());
			row.addCell(col++, val);
		}
	}
	
	private void addMetadataHeaders(OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		Row headerRow = exportSheet.newRow();
		headerRow.addCell(0, translator.translate("pool.public"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(1, translator.translate("export.name"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(2, translator.translate("export.creation.date"), workbook.getStyles().getHeaderStyle());
	}
	
	private void addMetadata(Pool pool, OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		Row row = exportSheet.newRow();
		row.addCell(0, pool.isPublicPool() ? translator.translate("yes") : translator.translate("no"));
		row.addCell(1, pool.getName());
		row.addCell(2, pool.getCreationDate(), workbook.getStyles().getDateTimeStyle());
	}
}
