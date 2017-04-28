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
package org.olat.ims.qti.export;

import org.olat.course.archiver.ExportFormat;

/**
 * 
 * Initial date: 27 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTIExportItemFormatDelegate implements QTIExportItemFormatConfig {
	
	private final ExportFormat delegate;
	
	public QTIExportItemFormatDelegate(ExportFormat delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean hasResponseCols() {
		return delegate.isResponseCols();
	}

	@Override
	public boolean hasPositionsOfResponsesCol() {
		return delegate.isPositionsOfResponsesCol();
	}

	@Override
	public boolean hasPointCol() {
		return delegate.isPointCol();
	}

	@Override
	public boolean hasTimeCols() {
		return delegate.isTimeCols();
	}

	@Override
	public void setPointCol(boolean pointColConfigured) {
		delegate.setPointCol(pointColConfigured);
	}

	@Override
	public void setPositionsOfResponsesCol(boolean positionsOfResponsesColConfigured) {
		delegate.setPositionsOfResponsesCol(positionsOfResponsesColConfigured);
	}

	@Override
	public void setResponseCols(boolean responseColsConfigured) {
		delegate.setResponseCols(responseColsConfigured);
	}

	@Override
	public void setTimeCols(boolean timeColsConfigured) {
		delegate.setTimeCols(timeColsConfigured);
	}
}
