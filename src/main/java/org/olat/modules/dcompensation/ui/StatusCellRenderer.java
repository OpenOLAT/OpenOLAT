package org.olat.modules.dcompensation.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.dcompensation.DisadvantageCompensationStatusEnum;

/**
 * 
 * Initial date: 24 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StatusCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public StatusCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(cellValue instanceof DisadvantageCompensationStatusEnum) {
			DisadvantageCompensationStatusEnum status = (DisadvantageCompensationStatusEnum)cellValue;
			target.append(translator.translate("status.".concat(status.name())));
		}
	}
}
