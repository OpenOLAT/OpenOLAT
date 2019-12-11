package org.olat.core.commons.services.vfs.ui.management;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.adobeconnect.model.AdobeConnectSco;

public class VFSOverviewFooterNameCellRenderer implements FlexiCellRenderer{
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		target.append(CSSHelper.getIcon(CSSHelper.CSS_CLASS_GLOBE));
		target.append(translator.translate(cellValue.toString()));
	}
}
