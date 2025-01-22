package org.olat.resource.accesscontrol.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 22 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OrderModificationCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public OrderModificationCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {

		if(cellValue instanceof OrderModificationSummary summary) {
			if(summary.modification()) {
				renderModification(target, "has.modification", "o_activity_modify", "o_icon_retry");
				target.append(" ");
			}
		}
	}
	
	private void renderModification(StringOutput target, String i18nKey, String cssClass, String iconCssClass) {
		target.append("<span class='").append(cssClass).append("'><i class='o_icon o_icon-fw ").append(iconCssClass).append("' title='")
	      .append(translator.translate(i18nKey))
	      .append("'> </i></span>");
	}
}
