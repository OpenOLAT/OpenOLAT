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
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 22 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LongTextRenderer implements FlexiCellRenderer {
	
	private static int count = 0;
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof String && StringHelper.containsNonWhitespace((String)cellValue)) {
			String id = "text_" + (++count);
			String text = (String)cellValue;
			if(text.length() > 48) {
				String truncatedText = Formatter.truncate(text, 32, "");
				target
					.append(truncatedText)
					.append("<span id='").append(id).append("'>\u2026</span>");
				
				text = escapeReturns(text);
				target.append("<script>\"use strict\";\njQuery(function () {jQuery('#").append(id).append("').tooltip({placement:\"top\",container: \"body\",html:true,title:\"");
				target.append(StringHelper.escapeJavaScript(text))
				      .append("\"});})</script>");
			} else {
				target.append(text);
			}
		}
	}

	public static String escapeReturns(String source) {
		return source.replace("\r\n", "<br>").replace("\n\r", "<br>")
				.replace("\n", "<br>").replace("\u2028", "<br>");
	}
}
