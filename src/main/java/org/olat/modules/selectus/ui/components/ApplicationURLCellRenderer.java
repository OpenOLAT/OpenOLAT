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
import org.olat.core.id.context.BusinessControlFactory;

import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.ui.model.ApplicationRow;

/**
 * 
 * Initial date: 4 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationURLCellRenderer implements FlexiCellRenderer {
	
	private final PositionRef position;
	private final Translator translator;
	
	public ApplicationURLCellRenderer(PositionRef position, Translator translator) {
		this.position = position;
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		Object rowObject = source.getFormItem().getTableDataModel().getObject(row);
		if(rowObject instanceof ApplicationRow) {
			ApplicationRow app = (ApplicationRow)rowObject;
			String businessPath = "[Positions:0][Position:" + position.getKey() + "][Applications:" + app.getKey() + "]";
			String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath);
			target.append("<a href='").append(url).append("' target='_blank' aria-label=\"")
			      .append(translator.translate("open.new.window")).append("\"><i class='o_icon o_icon_external_link'> </i></a>");
		}
	}
}
