package org.olat.modules.creditpoint.ui.component;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.creditpoint.model.CreditPointExpiration;

/**
 * 
 * Initial date: 25 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointExpirationCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public CreditPointExpirationCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if(cellValue instanceof CreditPointExpiration expiration && expiration.value() != null) {
			target.append(expiration.value())
			      .append(" ").append(translator.translate(expiration.unit().i18n(expiration.value())));
		}
	}
}
