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

import org.olat.modules.selectus.model.Project;
import org.olat.modules.selectus.ui.model.ApplicationRow;

/**
 * 
 * Initial date: 10 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ProjectCellRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof Boolean && Boolean.TRUE.equals(cellValue)) {
			ApplicationRow appRow = (ApplicationRow)source.getFormItem().getTableDataModel().getObject(row);
			if(appRow.getApplication().getProject() != null) {
				Long appKey = appRow.getApplication().getKey();
				Project project = appRow.getApplication().getProject();
				target.append("<span id='project_").append(appKey).append("'><i class='o_icon o_icon-lg o_icon_project'>  </i></span>");
				
				// Attach bootstrap tooltip handler to help icon
				target.append("<script>jQuery(function () {jQuery('#project_").append(appKey).append("').tooltip({placement:\"top\",container: \"body\",html:true,title:\"");
				
				if(StringHelper.containsNonWhitespace(project.getTitle())) {
					target.append("<h5>").append(StringHelper.escapeJavaScript(project.getTitle())).append("</h5>");
				}
				if(StringHelper.containsNonWhitespace(project.getFinancialImpact1())) {
					target.append("<span class='o_project_financial_impact'><i class='o_icon o_icon_finance'> </i> ").append(StringHelper.escapeJavaScript(project.getFinancialImpact1())).append("</span>");
				}
				if(StringHelper.containsNonWhitespace(project.getDescription())) {
					StringBuilder desc = Formatter.escWithBR(project.getDescription());
					target.append("<p>").append(StringHelper.escapeJavaScript(desc.toString())).append("</p>");
				}
				target.append("\"});})</script>");
			}
		}
	}
}
