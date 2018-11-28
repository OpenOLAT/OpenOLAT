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
package org.olat.modules.video.model;

import java.util.Date;

import org.olat.modules.video.VideoMarker;

/**
 * 
 * Initial date: 27 nov. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoMarkerImpl implements VideoMarker {
	
	private String id;
	private String text;
	private Date begin;
	private long duration;
	private String color;
	
	private double top;
	private double left;
	private double width;
	private double height;
	
	@Override
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public Date getBegin() {
		return begin;
	}

	@Override
	public void setBegin(Date begin) {
		this.begin = begin;
	}
	
	@Override
	public long getDuration() {
		return duration;
	}

	@Override
	public void setDuration(long duration) {
		this.duration = duration;
	}

	@Override
	public String getColor() {
		return color;
	}

	@Override
	public void setColor(String color) {
		this.color = color;
	}

	@Override
	public double getTop() {
		return top;
	}

	@Override
	public void setTop(double top) {
		this.top = top;
	}

	@Override
	public double getLeft() {
		return left;
	}

	@Override
	public void setLeft(double left) {
		this.left = left;
	}

	@Override
	public double getWidth() {
		return width;
	}

	@Override
	public void setWidth(double width) {
		this.width = width;
	}

	@Override
	public double getHeight() {
		return height;
	}

	@Override
	public void setHeight(double height) {
		this.height = height;
	}

	@Override
	public int hashCode() {
		return id == null ? 27649 : id.hashCode();
	}

	@Override
	public long toSeconds() {
		return begin == null ? 0 : begin.getTime() / 1000l;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof VideoMarkerImpl) {
			VideoMarkerImpl marker = (VideoMarkerImpl)obj;
			return id != null && id.equals(marker.getId());
		}
		return false;
	}
}
