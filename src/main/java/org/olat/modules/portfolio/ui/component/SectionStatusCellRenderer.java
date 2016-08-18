package org.olat.modules.portfolio.ui.component;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.portfolio.SectionStatus;

/**
 * 
 * Initial date: 18.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SectionStatusCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public SectionStatusCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if(cellValue instanceof SectionStatus) {
			SectionStatus status = (SectionStatus)cellValue;
			target.append("<i class='o_icon o_icon-fw ").append(status.cssClass()).append("'> </i> ")
			      .append(translator.translate(status.i18nKey()));
		}
	}
}
