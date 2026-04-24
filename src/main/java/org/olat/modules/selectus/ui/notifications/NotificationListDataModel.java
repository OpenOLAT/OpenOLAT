/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.notifications;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExportableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.RecruitingAuditLog;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.log.RecruitingAuditLogSearchParameters;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 22 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NotificationListDataModel extends DefaultFlexiTableDataModel<AuditLogRow>
implements SortableFlexiTableDataModel<AuditLogRow>, ExportableFlexiTableDataModel {
	
	private static final Logger log = Tracing.createLoggerFor(NotificationListDataModel.class);
	
	private static final AuditCols[] COLS = AuditCols.values();
	
	private final Roles roles;
	private final Identity identity;
	private final PositionRef position;
	private final Translator translator;
	
	private RecruitingAuditLogSearchParameters params;
	
	public NotificationListDataModel(FlexiTableColumnModel columnsModel, Identity identity, Roles roles, PositionRef position, Translator translator) {
		super(columnsModel);
		this.roles = roles;
		this.identity = identity;
		this.position = position;
		this.translator = translator;
	}

	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			List<AuditLogRow> views = new SortableFlexiTableModelDelegate<>(orderBy, this, null).sort();
			super.setObjects(views);
		}
	}

	
	@Override
	public MediaResource export(FlexiTableComponent ftC) {
		String label = "TableExport_"
				+ Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
				+ ".xlsx";
		
		ftC.getFormItem().getFilters();
		
		return new OpenXMLWorkbookResource(label) {
			@Override
			protected void generate(OutputStream out) {
				try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
					OpenXMLWorksheet sheet = workbook.nextWorksheet();
					createHeader(sheet, workbook);
					createData(sheet, workbook);
				} catch (IOException e) {
					log.error("", e);
				}
			}
		};
	}
	
	private void createHeader(OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
		Row headerRow = sheet.newRow();
		sheet.setHeaderRows(1);
		headerRow.addCell(0, translator.translate("table.header.time"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(1, translator.translate("table.header.identity"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(2, translator.translate("table.header.target"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(3, translator.translate("table.header.action"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(4, translator.translate("table.header.message"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(5, translator.translate("table.header.before"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(6, translator.translate("table.header.after"), workbook.getStyles().getHeaderStyle());
	}
	
	private void createData(OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
		List<RecruitingAuditLog> auditLogs = CoreSpringFactory.getImpl(AuditService.class)
				.getLogs(identity, roles, params);

		final Locale locale = translator.getLocale();
		final Map<Identity,String> identityToName = new HashMap<>();
		
		for(RecruitingAuditLog auditLog:auditLogs) {
			Identity logIdentity = auditLog.getIdentity();
			String fullName = null;
			if(logIdentity != null) {
				fullName = identityToName
					.computeIfAbsent(logIdentity, i -> RecruitingHelper.formatFullNameWithTitle(i, locale));
			}
			
			String message;
			if(auditLog.getMessageI18n() == null) {
				message = auditLog.getMessage();
			} else {
				message = translator.translate(auditLog.getMessageI18n(), auditLog.getMessageValues());
			}
			
			Row row = sheet.newRow();
			row.addCell(0, auditLog.getCreationDate(), workbook.getStyles().getDateTimeStyle());
			row.addCell(1, fullName);
			row.addCell(2, auditLog.getTargetEnum().name());
			row.addCell(3, auditLog.getActionEnum().name());
			row.addCell(4, message);
			row.addCell(5, toCellString(auditLog.getBefore()));
			row.addCell(6, toCellString(auditLog.getAfter()));
		}
	}
	
	private String toCellString(String string) {
		if(string != null && string.length() > 32000) {
			string = string.substring(0, 32000);
		}
		return string;
	}

	@Override
	public Object getValueAt(int row, int col) {
		AuditLogRow log = getObject(row);
		return getValueAt(log, col);
	}

	@Override
	public Object getValueAt(AuditLogRow row, int col) {
		return switch(COLS[col]) {
			case read -> row.isRead();
			case time -> row.getTime();
			case identity -> row.getIdentityFullName();
			case target -> row.getTarget();
			case action -> row.getAction();
			case message -> getTranslatedMessage(row);
			case gotoItem -> gotoItem(row);
			default -> "ERROR";
		};
	}
	
	private boolean gotoItem(AuditLogRow row) {
		if(position != null && row.getAction() == Action.delete && row.getTarget() == ActionTarget.application) {
			return false;
		}
		return true;
	}
	
	private String getTranslatedMessage(AuditLogRow row) {
		String i18nKey = row.getMessageI18n();
		if(i18nKey == null) {
			return row.getMessage();
		}
		return translator.translate(row.getMessageI18n(), row.getMessageValues());
	}
	
	public void setObjects(List<AuditLogRow> objects,  RecruitingAuditLogSearchParameters params) {
		this.params = params;
		super.setObjects(objects);
	}
	
	public enum AuditCols implements FlexiSortableColumnDef {
		read("table.header.read"),
		time("table.header.time"),
		identity("table.header.identity"),
		target("table.header.target"),
		message("table.header.message"),
		action("table.header.action"),
		gotoItem("goto");
		
		private final String key;
		
		private AuditCols(String key) {
			this.key = key;
		}
		
		public String key() {
			return key;
		}

		@Override
		public String i18nHeaderKey() {
			return key;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
