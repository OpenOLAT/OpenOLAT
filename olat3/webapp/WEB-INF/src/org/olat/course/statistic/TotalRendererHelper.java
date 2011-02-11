package org.olat.course.statistic;

import org.olat.core.gui.render.StringOutput;

/**
 * Helper class for having all rendering related constants
 * in one place with regards to rendering the 'Total' 
 * title and values in the statistics table.
 * <p>
 * Initial Date:  16.02.2010 <br>
 * @author Stefan
 */
public class TotalRendererHelper {

	/**
	 * Render the given value with any applicable pre/postfix into
	 * the given StringOutput
	 * @param sb where the resulting rendering should be appended
	 * @param value the value which should be rendered with pre/postfix if applicable
	 */
	public static void renderTotalValue(StringOutput sb, Integer value) {
		renderTotalValuePrefix(sb);
		sb.append(value);
		renderTotalValuePostfix(sb);
	}

	/**
	 * Render just the prefix to the total value if applicable
	 * @param sb where the resulting rendering should be appended
	 */
	public static void renderTotalValuePrefix(StringOutput sb) {
		sb.append("<i>");
	}

	/**
	 * Render just the postfix to the total value if applicable
	 * @param sb where the resulting rendering should be appended
	 */
	public static void renderTotalValuePostfix(StringOutput sb) {
		sb.append("</i>");
	}

	/**
	 * Render the given totalTitle with any applicable pre/postfix into
	 * the given StringOutput
	 * @param sb where the resulting rendering should be appended
	 * @param totalTitle the title which should be rendered into the StringOutput
	 */
	public static void renderTotalTitle(StringOutput sb, String totalTitle) {
		sb.append("<b><i>");
		sb.append(totalTitle);
		sb.append("</i></b>");
	}

}
