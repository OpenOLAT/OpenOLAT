/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.form.flexible.elements;

import org.olat.core.gui.components.form.flexible.FormItem;

/**
 * 
 * Initial date: 9 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface SliderElement extends FormItem {
	
	public double getMinValue();
	
	public void setMinValue(double min);
	
	public double getMaxValue();
	
	public void setMaxValue(double max);
	
	public int getStep();

	public void setStep(int step);
	
	public void setValue(double value);
	
	public double getValue();
	
	/**
	 * {@link #getValue()} returns always a value even if it was never set by
	 * {@link #setValue(String)} or by the user in the GUI. The default value is
	 * 0.0. So if {@link #getValue()} returns 0.0 is is not clear if it is the
	 * default value or if it was set by the user. Use this method to determine if
	 * the value was at least set once by the user.
	 *
	 * @return
	 */
	public boolean hasValue();

	public void setDomReplacementWrapperRequired(boolean required);

}
