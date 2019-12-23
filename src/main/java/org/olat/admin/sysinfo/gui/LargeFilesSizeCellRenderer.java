package org.olat.admin.sysinfo.gui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;

public class LargeFilesSizeCellRenderer implements FlexiCellRenderer{
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if (cellValue instanceof Number) {
			String icon = CSSHelper.CSS_CLASS_CIRCLE_COLOR;
			String color;
			Long size = ((Number)cellValue).longValue();

			if(size < 5000) {
				color = "okay";
			} else if(size < 10000) {
				color = "warning";
			} else {
				color = "large";
			}

			if(StringHelper.containsNonWhitespace(icon)) {
				target.append("<i class='o_icon ")
				.append(icon)
				.append(" o_files_size_" + color + " ")
				.append("'> </i> ")
				.append(Formatter.formatBytes(size));
			}
		}
	}
}
