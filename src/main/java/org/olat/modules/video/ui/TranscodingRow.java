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
	
	private final int resolution;
	private final int sumVideos;
	private final int external;
	private final int missingTranscodings;
	private final int failedTranscodings;
	private final int numberTranscodings;
	private final boolean startTranscodingAvailable;

	public TranscodingRow(int resolution, int numberTranscodings, int failedTranscodings, int extern, int sumVideos, boolean mayTranscode) {
		this.resolution = resolution;
		this.numberTranscodings = numberTranscodings;
		this.sumVideos = sumVideos;
		this.external = extern;
		this.missingTranscodings = sumVideos - extern - numberTranscodings - failedTranscodings;
		this.failedTranscodings = failedTranscodings;
		this.startTranscodingAvailable = mayTranscode && missingTranscodings > 0;
	}

	public int getResolution() {
		return resolution;
	}

	public int getSumVideos() {
		return sumVideos;
	}
	
	public int getExtern() {
		return external;
	}

	public int getNumberTranscodings() {
		return numberTranscodings;
	}

	public int getFailedTranscodings() {
		return failedTranscodings;
	}

	public int getMissingTranscodings() {
		return missingTranscodings >= 0 ? missingTranscodings : 0;
	}

	public boolean isStartTranscodingAvailable() {
		return startTranscodingAvailable;
	}
	
}
