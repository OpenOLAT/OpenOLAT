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
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.ui.model.ApplicationRow;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class NotesCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof String && StringHelper.containsNonWhitespace((String)cellValue)) {
			ApplicationRow appRow = (ApplicationRow)source.getFormItem().getTableDataModel().getObject(row);
			Long appKey = appRow.getApplication().getKey();
			target.append("<span id='notes_").append(appKey).append("'><i class='o_icon o_icon-lg o_icon_notes'> </i></span>");
			
			// Attach bootstrap tooltip handler to help icon
			target.append("<script>jQuery(function () {jQuery('#notes_").append(appKey).append("').tooltip({placement:\"top\",container: \"body\",html:true,title:\"");
			String notes = (String)cellValue;
			target.append(StringHelper.escapeJavaScript(notes))
			      .append("\"});})</script>");
		} else {
			target.append("<i class='o_icon o_icon-lg o_icon_notes_empty'> </i>");
		}
	}
}
