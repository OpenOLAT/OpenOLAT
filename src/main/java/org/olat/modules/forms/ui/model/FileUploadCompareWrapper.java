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
package org.olat.modules.forms.ui.model;

/**
 * 
 * Initial date: 06.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FileUploadCompareWrapper {
	
	private final String color;
	private final String evaluator;
	private final String filename;
	private final String filesize;
	private final String mapperUri;
	private final String iconCss;
	private final String thumbUri;

	public FileUploadCompareWrapper(String color, String evaluator, String filename, String filesize, String mapperUri,
			String iconCss, String thumbUri) {
		this.color = color;
		this.evaluator = evaluator;
		this.filename = filename;
		this.filesize = filesize;
		this.mapperUri = mapperUri;
		this.iconCss = iconCss;
		this.thumbUri = thumbUri;
	}

	public String getEvaluator() {
		return evaluator;
	}

	public String getColor() {
		return color;
	}

	public String getFilename() {
		return filename;
	}

	public String getFilesize() {
		return filesize;
	}

	public String getMapperUri() {
		return mapperUri;
	}

	public String getIconCss() {
		return iconCss;
	}

	public String getThumbUri() {
		return thumbUri;
	}

}
