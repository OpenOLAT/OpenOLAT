package org.olat.core.commons.services.vfs.ui.management;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;

public class VFSOverviewNameCellRenderer implements FlexiCellRenderer{
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		switch (cellValue.toString()) {
		case "vfs.overview.files":
			target.append(CSSHelper.getIcon(CSSHelper.CSS_CLASS_FILETYPE_FILE));
			break;
		case "vfs.overview.versions":
			target.append(CSSHelper.getIcon(CSSHelper.CSS_CLASS_VERSION));
			break;
		case "vfs.overview.thumbnails":
			target.append(CSSHelper.getIcon(CSSHelper.CSS_CLASS_THUMBNAIL));
			break;
		case "vfs.overview.trash":
			target.append(CSSHelper.getIcon(CSSHelper.CSS_CLASS_TRASHED));
			break;
		case "vfs.overview.total":
			target.append(CSSHelper.getIcon(CSSHelper.CSS_CLASS_GLOBE));
			break;
		default:
			break;
		}
		target.append(translator.translate(cellValue.toString()));
	}
}
