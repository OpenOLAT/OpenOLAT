package org.olat.ims.qti21.ui.logviewer;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.XlsFlexiTableExporter;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.model.LogViewerEntry.Answer;
import org.olat.ims.qti21.model.LogViewerEntry.Answers;

/**
 * 
 * Initial date: 25 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LogAnswerCellRenderer implements FlexiCellRenderer {
	
	private final boolean showIds;
	
	public LogAnswerCellRenderer(boolean showIds) {
		this.showIds = showIds;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof Answers answers) {
			List<Answer> answerList = answers.answers();
			if(answerList != null) {
				boolean append = false;		
				for(Answer answer:answerList) {
					List<String> list = showIds ? answer.ids() : answer.values();
					if(list != null) {
						for(int i=0; i<list.size(); i++) {
							String text = list.get(i);
							if(StringHelper.containsNonWhitespace(text)) {
								if(append) {
									target.append("<br>");
								} else {
									append = true;
								}
								text = text.replace("\\r", "");
								if(renderer == null) {
									text = text.replace("\\n", XlsFlexiTableExporter.LINE_BREAK_MARKER);
								} else {
									text = text.replace("\\n", "<br>");
								}
								target.append(text);
							}
						}
					}
				}
			}
		}
	}
}
