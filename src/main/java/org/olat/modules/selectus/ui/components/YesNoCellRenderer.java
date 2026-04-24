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

/**
 * 
 * Initial date: 23 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class YesNoCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public YesNoCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if(cellValue instanceof Boolean) {
			Boolean b = (Boolean)cellValue;
			if(b.booleanValue()) {
				target.append(translator.translate("yes"));
			} else {
				target.append(translator.translate("no"));
			}
		}
	}
}
