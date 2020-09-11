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
package org.olat.core.commons.services.doceditor.onlyoffice.model;

/**
 * 
 * Initial date: 4 Sep 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class Thumbnail {
	
	private Integer aspect;
	private Boolean first;
	private Integer height;
	private Integer width;
	
	public Integer getAspect() {
		return aspect;
	}
	
	public void setAspect(Integer aspect) {
		this.aspect = aspect;
	}
	
	public Boolean getFirst() {
		return first;
	}
	
	public void setFirst(Boolean first) {
		this.first = first;
	}
	
	public Integer getHeight() {
		return height;
	}
	
	public void setHeight(Integer height) {
		this.height = height;
	}
	
	public Integer getWidth() {
		return width;
	}
	
	public void setWidth(Integer width) {
		this.width = width;
	}

}
