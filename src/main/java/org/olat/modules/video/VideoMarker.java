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
package org.olat.modules.video;

import java.util.Date;

/**
 * 
 * Initial date: 27 nov. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface VideoMarker {
	
	public String getId();
	
	public String getText();
	
	public void setText(String text);
	
	public Date getBegin();
	
	public void setBegin(Date date);
	
	/**
	 * 
	 * @return Duration in seconds
	 */
	public long getDuration();
	
	public void setDuration(long duration);
	
	public double getTop();
	
	public void setTop(double top);
	
	public double getLeft();
	
	public void setLeft(double left);
	
	public double getWidth();
	
	public void setWidth(double width);
	
	public double getHeight();
	
	public void setHeight(double height);
	
	public String getColor();
	
	public void setColor(String color);
	
	public long toSeconds();

}
