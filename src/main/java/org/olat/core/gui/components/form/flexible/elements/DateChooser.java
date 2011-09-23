/**
 * 
 */
package org.olat.core.gui.components.form.flexible.elements;

import java.util.Date;

/**
 * @author patrickb
 *
 */
public interface DateChooser extends TextElement{

	/**
	 * @return the date or null if the value is not valid (parsed with
	 *         DateFormat.getDateInstance(DateFormat.SHORT, locale), or with new
	 *         SimpleDateFormat(customDataFormat))
	 */
	public Date getDate();

	/**
	 * @param date
	 */
	public void setDate(Date date);

	/**
	 * @return
	 */
	public boolean isDateChooserTimeEnabled();

	/**
	 * @param dateChooserTimeEnabled
	 */
	public void setDateChooserTimeEnabled(boolean dateChooserTimeEnabled);

	/**
	 * @return
	 */
	public String getDateChooserDateFormat();

	/**
	 * Set an optional date chooser format if the default format for the current
	 * locale is not good. It is recommended to use the locale dependent format
	 * when possible. (e.g. 22.02.99 for DE and 99/22/92 for EN)
	 * <br>
	 * Use a pattern that corresponds to the SimpleDateFormat patterns.
	 * 
	 * @param dateChooserDateFormat
	 */
	public void setDateChooserDateFormat(String dateChooserDateFormat);

	/**
	 * Set an optional date format if the default format for the current
	 * locale is not good. It is recommended to use the locale dependent format
	 * when possible. (e.g. 22.02.99 for DE and 99/22/92 for EN)
	 * <br>
	 * Together with setDateChooserDateFormat!! One has to set the format of 
	 * the js datechoose which is differently specified, then the date formatting
	 * string of java.
	 * <br>
	 * Use a pattern that corresponds to the SimpleDateFormat patterns.
	 * 
	 * @param customDateFormat
	 */
	public void setCustomDateFormat(String customDateFormat);

	/**
	 * @param errorKey
	 * @return
	 */
	public void setValidDateCheck(String errorKey);

	public String getExampleDateString();

}