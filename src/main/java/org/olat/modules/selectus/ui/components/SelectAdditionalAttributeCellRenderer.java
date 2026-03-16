/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.components;

import java.io.IOException;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

import org.olat.modules.selectus.model.attributes.SelectConfiguration;
import org.olat.modules.selectus.ui.app_wizard.ApplicationAttributesDelegate;

/**
 * 
 * Initial date: 19 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectAdditionalAttributeCellRenderer implements FlexiCellRenderer {
	
	private final Locale locale;
	private final SelectConfiguration configuration;
	
	public SelectAdditionalAttributeCellRenderer(SelectConfiguration configuration, Locale locale) {
		this.locale = locale;
		this.configuration = configuration;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof String[]) {
			String[] selectedOptions = (String[])cellValue;
			appendArray(target, selectedOptions);
			
		} else if(cellValue instanceof String) {
			target.append(ApplicationAttributesDelegate.getSingleLocalizedValue(configuration, (String)cellValue, locale));
		}
	}
	
	public static String render(String[] selectedOptions) {
		try(StringOutput out = new StringOutput()) {
			appendArray(out, selectedOptions);
			return out.toString();
		} catch(IOException e) {
			return null;
		}
	}
	
	private static void appendArray(StringOutput target, String[] selectedOptions) {
		boolean started = false;
		for(String option:selectedOptions) {
			if(ApplicationAttributesDelegate.SELECT_CHOOSE.equals(option)
					|| ApplicationAttributesDelegate.SELECT_OTHER.equals(option)
					|| ApplicationAttributesDelegate.FILTER_OTHERS.equals(option)) {
				continue;
			}
			
			if(started) {
				target.append(", ");
			} else {
				started = true;
			}
			target.append(option);
		}
	}
}
