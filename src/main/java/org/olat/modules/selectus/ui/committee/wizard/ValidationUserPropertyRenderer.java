/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.wizard;

import java.util.Locale;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 27 oct. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ValidationUserPropertyRenderer implements FlexiCellRenderer {
	
	public static final String MISSING_MARKER = " <i class='o_icon o_icon-fw o_icon_not_available'> </i>";

	private final DB dbInstance;
	private final Locale locale;
	private final boolean mandatory;
	private final UserPropertyHandler propertyHandler;
	private final ValidationError error = new ValidationError();
	
	public ValidationUserPropertyRenderer(UserPropertyHandler propertyHandler, boolean mandatory, Locale locale, DB dbInstance) {
		this.propertyHandler = propertyHandler;
		this.dbInstance = dbInstance;
		this.mandatory = mandatory;
		this.locale = locale;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		
		if(cellValue instanceof String ) {
			String text = (String)cellValue;
			target.append(text);
			
			Object rawObj = source.getFormItem().getTableDataModel().getObject(row);
			
			User user = null;
			if(rawObj instanceof CommitteeMember) {
				CommitteeMember member = (CommitteeMember)rawObj;
				user = member.getIdentity().getUser();
			}
			
			String rawValue = null;
			if(user != null) {
				rawValue = user.getProperty(propertyHandler.getName());
				if("_".equals(rawValue) && dbInstance.isOracle()) {
					rawValue = null;
				}
			}
			
			if((mandatory && (!StringHelper.containsNonWhitespace(rawValue) || "-".equals(rawValue)))
					|| !propertyHandler.isValidValue(user, rawValue, error, locale)) {
				target.append(MISSING_MARKER);
			}
		} else if(cellValue == null && mandatory) {
			target.append(MISSING_MARKER);
		}
	}
}
