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
package org.olat.modules.video.ui;

/**
 * The Class TranscodingRow.
 * Initial date: 15.11.2016<br>
 * @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
 */
public class TranscodingRow {
	
	private int resolution;
	private int sumVideos;
	private int missingTranscodings;
	private int failedTranscodings;
	private int numberTranscodings;
	private boolean allTranscoded;

	public TranscodingRow(int resolution, int numberTranscodings, int failedTranscodings, int sumVideos, boolean mayTranscode) {
		super();
		this.resolution = resolution;
		this.numberTranscodings = numberTranscodings;
		this.sumVideos = sumVideos;
		this.missingTranscodings = sumVideos - numberTranscodings - failedTranscodings;
		this.failedTranscodings = failedTranscodings;
		this.allTranscoded = numberTranscodings + failedTranscodings < sumVideos && mayTranscode;		
	}

	
	
	public boolean isAllTranscoded() {
		return allTranscoded;
	}

	public void setAllTranscoded(boolean allTranscoded) {
		this.allTranscoded = allTranscoded;
	}

	public int getResolution() {
		return resolution;
	}

	public void setResolution(int resolution) {
		this.resolution = resolution;
	}

	public int getSumVideos() {
		return sumVideos;
	}

	public void setSumVideos(int sumVideos) {
		this.sumVideos = sumVideos;
	}

	public int getNumberTranscodings() {
		return numberTranscodings;
	}

	public void setNumberTranscodings(int numberTranscodings) {
		this.numberTranscodings = numberTranscodings;
	}
	
	public int getFailedTranscodings() {
		return failedTranscodings;
	}

	public void setFailedTranscodings(int failedTranscodings) {
		this.failedTranscodings = failedTranscodings;
	}

	public int getMissingTranscodings() {
		return missingTranscodings >= 0 ? missingTranscodings : 0;
	}

	public void setMissingTranscodings(int missingTranscodings) {
		this.missingTranscodings = missingTranscodings;
	}


	
	
}
