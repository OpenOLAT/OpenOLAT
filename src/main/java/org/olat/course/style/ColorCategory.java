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
package org.olat.course.style;

/**
 * 
 * Initial date: 23 Jun 2021<br>>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ColorCategory extends ColorCategoryRef, Comparable<ColorCategory> {
	
	public final static String IDENTIFIER_NO_COLOR = "no.color";
	public final static String CSS_NO_COLOR = "o_colcat_nocolor";
	public final static String IDENTIFIER_INHERITED = "inherited";
	public final static String IDENTIFIER_COURSE = "course";
	public final static String IDENTIFIER_FALLBACK_COURSE = IDENTIFIER_NO_COLOR;
	public final static String IDENTIFIER_FALLBACK_COURSE_NODE = IDENTIFIER_INHERITED;
	
	public enum Type { technical, predefined, custom }
	
	public String getIdentifier();
	
	public Type getType();
	
	public int getSortOrder();
	
	public void setSortOrder(int sortOrder);
	
	public boolean isEnabled();
	
	public void setEnabled(boolean enabled);
	
	public String getCssClass();
	
	public void setCssClass(String cssClass);

}
