/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.components;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 3 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TooltipCellRenderer implements FlexiCellRenderer {
	
	private final String iconCssClass;
	
	public TooltipCellRenderer(String iconCssClass) {
		this.iconCssClass = iconCssClass;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof String && StringHelper.containsNonWhitespace((String)cellValue)) {
			render(renderer, target, (String)cellValue);
		}
	}

	public void render(Renderer renderer, StringOutput target, String value) {
		if(renderer == null) {
			target.append(value);
		} else if(StringHelper.containsNonWhitespace(value)) {
			String tooltip = Formatter.escWithBR(value).toString();
			tooltip = tooltip.replace("\r", "").replace("\n", "");
			tooltip = StringHelper.escapeJavaScript(tooltip);
			
			String appKey = CodeHelper.getUniqueID();
			target.append("<span id='row_tooltip_").append(appKey).append("'><i class='o_icon o_icon-lg ").append(iconCssClass).append("'>  </i></span>")
			// Attach bootstrap tooltip handler to help icon
			      .append("<script>jQuery(function () {\n")
			      .append(" jQuery('#row_tooltip_").append(appKey).append("').tooltip({placement:\"top\",container: \"body\",html:true,title:\"")
			      .append(tooltip)
			      .append("\"}).on('click', function() { jQuery(this).tooltip('hide')});\n")
			      .append("})</script>");
		}
	}
}
