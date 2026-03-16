/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.components;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.mail.MailerResult;

/**
 * 
 * Initial date: 23.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LogStatusCellRenderer implements FlexiCellRenderer {
	
	private final Translator trans;
	
	public LogStatusCellRenderer(Translator translator) {
		this.trans = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof Integer) {
			target.append(format((Integer)cellValue, trans));
		}
	}

	public static final String format(Integer status, Translator translator) {
		String formattedStatus;
		if(status == null || status.intValue() < 0) {
			formattedStatus = "";
		} else {
			int s = status.intValue();
			if(s == MailerResult.OK) {
				formattedStatus = translator.translate("mail.status.ok");
			} else if(s == MailerResult.MAILHOST_UNDEFINED) {
				formattedStatus = translator.translate("mail.status.error.host");
			} else if(s == MailerResult.SEND_GENERAL_ERROR) {
				formattedStatus = translator.translate("mail.status.error.send");
			} else if(s == MailerResult.SENDER_ADDRESS_ERROR) {
				formattedStatus = translator.translate("mail.status.error.sender");
			} else if(s == MailerResult.RECIPIENT_ADDRESS_ERROR) {
				formattedStatus = translator.translate("mail.status.error.recipient");
			} else if(s == MailerResult.TEMPLATE_PARSE_ERROR || s == MailerResult.TEMPLATE_GENERAL_ERROR) {
				formattedStatus = translator.translate("mail.status.error.template");
			} else if(s == MailerResult.ATTACHMENT_INVALID) {
				formattedStatus = translator.translate("mail.status.error.attachment");
			} else {
				formattedStatus = translator.translate("mail.status.error.unkown");
			}
		}
		return formattedStatus;
	}
}
