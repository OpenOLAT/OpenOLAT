package org.olat.course.archiver.wizard;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 26 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ArchiveRepositoryEntryStatusRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public ArchiveRepositoryEntryStatusRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(cellValue instanceof ArchiveRepositoryEntryRow entry) {
			target.append("<span>");
			if(entry.isRunningArchive()) {
				target.append("<i class='o_icon o_icon-fw o_icon_warn'> </i> ").append(translator.translate("warning.running.archive"));
			} else if(entry.isCompleteArchive()) {
				target.append(translator.translate("warning.complete.archive"));
			}
			target.append("</span>");
		}
	}
}
