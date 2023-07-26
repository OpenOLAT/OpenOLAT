/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.quota.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.Quota;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.DialogCourseNode;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.nodes.PageCourseNode;

/**
 * Initial date: Jul 04, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CourseQuotaUsageRow implements FlexiTreeTableNode {

	private final String subIdent;
	private final String resource;
	private final String type;
	private final FormLink editQuota;
	private final FormLink displayRss;
	private String relPath;
	private FormLink external;
	private Integer numOfFiles;
	private Long totalUsedSize;
	private int numOfChildren = 0;
	private ProgressBar curUsed;
	private Quota elementQuota;
	private CourseQuotaUsageRow parent;

	public CourseQuotaUsageRow(String subIdent, String resource, String type, FormLink editQuota, FormLink displayRss) {
		this.subIdent = subIdent;
		this.resource = resource;
		this.type = type;
		this.editQuota = editQuota;
		this.displayRss = displayRss;
	}

	public String getRelPath() {
		return relPath;
	}

	public void setRelPath(String relPath) {
		this.relPath = relPath;
	}

	public Quota getElementQuota() {
		return elementQuota;
	}

	public void setElementQuota(Quota elementQuota) {
		this.elementQuota = elementQuota;
	}

	public String getSubIdent() {
		return subIdent;
	}

	public String getResource() {
		return resource;
	}

	public String getType() {
		return type;
	}

	public String getTypeCssIcon() {
		CourseNodeConfiguration cnc = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(getType());
		return CSSHelper.getIcon(cnc.getIconCSSClass());
	}

	public FormLink getExternal() {
		return external;
	}

	public void setExternal(FormLink external) {
		this.external = external;
	}

	public Integer getNumOfFiles() {
		return getType().equals(BCCourseNode.TYPE)
				|| getType().equals(PFCourseNode.TYPE)
				|| getType().equals(GTACourseNode.TYPE_INDIVIDUAL)
				|| getType().equals(FOCourseNode.TYPE)
				|| getType().equals(DialogCourseNode.TYPE)
				|| getType().equals(PageCourseNode.TYPE)
				? numOfFiles : null;
	}

	public void setNumOfFiles(Integer numOfFiles) {
		this.numOfFiles = numOfFiles;
	}

	public String getSize() {
		return getElementQuota() != null ? Formatter.formatKBytes(getElementQuota().getQuotaKB() - getElementQuota().getRemainingSpace()) : "";
	}

	public Long getTotalUsedSize() {
		return totalUsedSize;
	}

	public void setTotalUsedSize(Long totalUsedSize) {
		this.totalUsedSize = totalUsedSize;
	}

	public String getQuota() {
		return getElementQuota() != null ? Formatter.formatKBytes(getElementQuota().getQuotaKB()) : "";
	}

	public ProgressBar getCurUsed() {
		return curUsed;
	}

	public void setCurUsed(ProgressBar curUsed) {
		this.curUsed = curUsed;
	}

	public FormLink getEditQuota() {
		return editQuota;
	}

	public FormLink getDisplayRss() {
		return displayRss;
	}

	public int getNumOfChildren() {
		return numOfChildren;
	}

	public void incrementNumberOfChildren() {
		numOfChildren++;
	}

	public void decrementNumberOfChildren() {
		numOfChildren--;
	}

	@Override
	public FlexiTreeTableNode getParent() {
		return parent;
	}

	public void setParent(CourseQuotaUsageRow parent) {
		this.parent = parent;
	}

	@Override
	public String getCrump() {
		return getResource();
	}
}
