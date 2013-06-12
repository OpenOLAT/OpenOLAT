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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.webservices.clients.onyxreporter;

import java.io.File;
import java.io.FilenameFilter;

public class OnyxReporterConnectorFileNameFilter implements FilenameFilter {

	private final String nodeId;
	private final String assessmentId;

	public OnyxReporterConnectorFileNameFilter(String nodeId, String assessmentId) {
		this.nodeId = nodeId;
		this.assessmentId = assessmentId;
	}

	public OnyxReporterConnectorFileNameFilter(String nodeId) {
		this(nodeId, null);
	}

	/**
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	@Override
	public boolean accept(File diretory, String name) {
		boolean accept = false;
		if (name != null && name.startsWith(nodeId)) {
			if (assessmentId == null || assessmentId.isEmpty()) {
				accept = true;
			} else {
				accept = name.contains(assessmentId);
			}

		}
		return accept;
	}
}

/*
history:

$Log: OnyxReporterConnectorFileNameFilter.java,v $
Revision 1.1  2012-05-09 16:03:49  blaw
OLATCE-2007
* allow suspend and resume of tests


*/