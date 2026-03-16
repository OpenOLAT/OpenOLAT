/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.components;

import java.util.Date;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

import org.olat.modules.selectus.ui.AcademicalDateFormat;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 9 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AcademicalDateCellRenderer implements CustomCellRenderer, FlexiCellRenderer {
	
	private final Locale locale;
	private final AcademicalDateFormat[] formats;
	
	public AcademicalDateCellRenderer(AcademicalDateFormat[] formats, Locale locale) {
		this.formats = formats;
		this.locale = locale;
	}
	
	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		render(sb, val);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		render(target, cellValue);
	}
	
	private void render(StringOutput sb, Object val) {
		if(val instanceof Date) {
			String date = RecruitingHelper.formatAcademicalTableDate((Date)val, formats, locale);
			if(date != null) {
				sb.append(date);
			}
		}
	}
}
