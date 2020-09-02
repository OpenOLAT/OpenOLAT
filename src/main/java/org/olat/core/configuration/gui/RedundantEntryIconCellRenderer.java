package org.olat.core.configuration.gui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 22.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RedundantEntryIconCellRenderer implements FlexiCellRenderer {
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof Boolean) {
			if((Boolean) cellValue) {
				if (renderer == null) {
					target.append("X");
				} else {
					String icon = CSSHelper.CSS_CLASS_WARN;
		
					if(StringHelper.containsNonWhitespace(icon)) {
						target.append("<i class='o_icon o_icon-fw ")
						.append(icon)
						.append("'></i>");
					}
				}
			}
		}
	}
}
