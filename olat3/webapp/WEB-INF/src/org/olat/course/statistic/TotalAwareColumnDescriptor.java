package org.olat.course.statistic;

import java.util.Locale;

import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;

/**
 * An extension of DefaultColumnDescriptor with the sole purpose of rendering
 * the TOTAL_ROW_TITLE_CELL differently than the rest of the crowd.
 * <p>
 * The StatisticResult uses a special token, the StatisticResult.TOTAL_ROW_TITLE_CELL
 * which it returns for the last row, column 0 (the 'Total' title).
 * This ColumnDescriptor extends renderValue to catch the rendering of that title
 * and apply any boldness or the like (using TotalRendererHelper)
 * <P>
 *@GODO
 * Note that this ColumnDescriptor also uses the 'trick' of checking for
 * renderer==null to distinguish between normal rendering (on screen) where
 * we do have a renderer, and rendering for export where the renderer is null.
 * This is a bit hacky though and we should probably come up with a nicer generic
 * solution for the ColumnDescriptor/CellRenderer etc.
 * Initial Date:  16.02.2010 <br>
 * @author Stefan
 */
public class TotalAwareColumnDescriptor extends DefaultColumnDescriptor {

	public TotalAwareColumnDescriptor(String headerKey, int dataColumn, String action, Locale locale, int alignment) {
		super(headerKey, dataColumn, action, locale, alignment);
	}
	
	@Override
	public String getAction(int row) {
		if (row==table.getTableDataModel().getRowCount()-1) {
			return super.getAction(row);
		} else {
			return null;
		}
	}
	
	@Override
	public void renderValue(StringOutput sb, int row, Renderer renderer) {
		Object col0 = table.getTableDataModel().getValueAt(table.getSortedRow(row), 0);
		if (col0!=StatisticResult.TOTAL_ROW_TITLE_CELL) {
			super.renderValue(sb, row, renderer);
		} else {
			if (renderer!=null) {
				TotalRendererHelper.renderTotalValuePrefix(sb);
			}
			super.renderValue(sb, row, renderer);
			if (renderer!=null) {
				TotalRendererHelper.renderTotalValuePostfix(sb);
			}
		}
	}

}
