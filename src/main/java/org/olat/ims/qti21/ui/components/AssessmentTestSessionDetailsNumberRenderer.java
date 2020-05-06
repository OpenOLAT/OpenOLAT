package org.olat.ims.qti21.ui.components;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.ims.qti21.ui.QTI21AssessmentTestSessionDetails;

/**
 * 
 * Initial date: 6 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestSessionDetailsNumberRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public AssessmentTestSessionDetailsNumberRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translate) {
		
		Object obj = source.getFlexiTableElement().getTableDataModel().getObject(row);
		if(obj instanceof QTI21AssessmentTestSessionDetails && ((QTI21AssessmentTestSessionDetails)obj).isError()) {
			target.append("<span title=\"").append(translator.translate("error.assessment.test.session")).append("\">")
			      .append("<i class='o_icon o_icon_error'> </i> ").append(translator.translate("error.assessment.test.session.short"))
			      .append("</span>");
			
		} else if(cellValue != null) {
			target.append(cellValue.toString());
		}
	}
}
