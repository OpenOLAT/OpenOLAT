package org.olat.course.statistic;

import java.util.Locale;

import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;

/**
 * This renderer is used by the StatisticDisplayController to render the 'Total' column 
 * (the last column). 
 * <p>
 * The idea is to render it bold and filter the boldiness out for the export, that's it.
 * <P>
 * Initial Date:  16.02.2010 <br>
 * @author Stefan
 */
public class TotalColumnRenderer implements CustomCellRenderer {

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		if (val==null) {
			// don't render nulls
			return;
		}
		
		if (renderer==null) {
			// if no renderer is set, then we assume it's a table export - in which case we don't want the htmls
			sb.append(String.valueOf(val));
			return;
		}
		
		// this is the normal case
		TotalRendererHelper.renderTotalValue(sb, (Integer) val);
	}

}
