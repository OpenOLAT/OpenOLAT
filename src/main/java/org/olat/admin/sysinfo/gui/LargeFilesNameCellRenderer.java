package org.olat.admin.sysinfo.gui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.adobeconnect.model.AdobeConnectSco;

public class LargeFilesNameCellRenderer implements FlexiCellRenderer{
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
		URLBuilder ubu, Translator translator) {
		String icon = CSSHelper.createFiletypeIconCssClassFor((String)cellValue);
		if(StringHelper.containsNonWhitespace(icon)) {
			target.append("<i class='o_icon o_icon-fw ").append(icon).append("'> </i> ").append((String) cellValue);
		}
	}
}
