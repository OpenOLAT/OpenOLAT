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
package org.olat.core.commons.fullWebApp;

/**
 * <h3>Description:</h3> Config object to store users column settings You can
 * use the spring setters setCol1WidthEmDefault() and setCol2WidthEmDefault() to
 * set the width system wide to another value (e.g. when your theme needs it)
 * <p>
 * Initial Date: 14.01.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class LayoutMain3ColsConfig {
	// default values for column width in EM
	private static final int col1WidthEmDefault = 14, col2WidthEmDefault = 12;
	// specific configured width
	private int col1WidthEM;
	private int col2WidthEM;

	/**
	 * Default constructor that uses the default width configuration
	 */
	public LayoutMain3ColsConfig() {
		this(col1WidthEmDefault, col2WidthEmDefault);
	}

	/**
	 * Constructor with specific width values
	 * 
	 * @param col1WidthEM
	 * @param col2WidthEM
	 */
	public LayoutMain3ColsConfig(int col1WidthEM, int col2WidthEM) {
		super();
		this.col1WidthEM = col1WidthEM;
		this.col2WidthEM = col2WidthEM;
	}
 
	/**
	 * @return Witdh of col1 measured in EM values
	 */
	public int getCol1WidthEM() {
		return col1WidthEM;
	}

	/**
	 * @param col1WidthEM Width of col1 measured in EM values
	 */
	public void setCol1WidthEM(int col1WidthEM) {
		this.col1WidthEM = col1WidthEM;
	}

	/**
	 * @return Width of col2 measured in EM values
	 */
	public int getCol2WidthEM() {
		return col2WidthEM;
	}

	/**
	 * @param col2WidthEM Width of col2 measured in EM values
	 */
	public void setCol2WidthEM(int col2WidthEM) {
		this.col2WidthEM = col2WidthEM;
	}
}
