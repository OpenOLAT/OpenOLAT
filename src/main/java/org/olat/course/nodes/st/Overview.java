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
package org.olat.course.nodes.st;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.learningpath.LearningPathStatus;
import org.olat.course.nodeaccess.NoAccessResolver.NoAccess;
import org.olat.course.style.TeaserImageStyle;

/**
 * 
 * Initial date: 15 July 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class Overview {
	
	private final String nodeIdent;
	private final String title;
	private final String subTitle;
	private final String description;
	private final String iconCss;
	private final String teaserImageID;
	private final Mapper teaserImageMapper;
	private final TeaserImageStyle teaserImageStyle;
	private final String colorCategoryCss;
	private final NoAccess noAccessMessage;
	private final LearningPathStatus learningPathStatus;
	private final DueDateConfig startDateConfig;
	private final DueDateConfig endDateConfig;
	private final Integer duration;

	private Overview(Builder builder) {
		this.nodeIdent = builder.nodeIdent;
		this.title = builder.title;
		this.subTitle = builder.subTitle;
		this.description = builder.description;
		this.iconCss = builder.iconCss;
		this.teaserImageID = builder.teaserImageID;
		this.teaserImageMapper = builder.teaserImageMapper;
		this.teaserImageStyle = builder.teaserImageStyle;
		this.colorCategoryCss = builder.colorCategoryCss;
		this.noAccessMessage = builder.noAccessMessage;
		this.learningPathStatus = builder.learningPathStatus;
		this.startDateConfig = builder.startDateConfig;
		this.endDateConfig = builder.endDateConfig;
		this.duration = builder.duration;
	}
	
	public String getNodeIdent() {
		return nodeIdent;
	}

	public String getTitle() {
		return title;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public String getDescription() {
		return description;
	}

	public String getIconCss() {
		return iconCss;
	}
	
	public String getTeaserImageID() {
		return teaserImageID;
	}

	public Mapper getTeaserImageMapper() {
		return teaserImageMapper;
	}

	public TeaserImageStyle getTeaserImageStyle() {
		return teaserImageStyle;
	}

	public String getColorCategoryCss() {
		return colorCategoryCss;
	}
	
	public NoAccess getNoAccessMessage() {
		return noAccessMessage;
	}

	public LearningPathStatus getLearningPathStatus() {
		return learningPathStatus;
	}

	public DueDateConfig getStartDateConfig() {
		return startDateConfig;
	}

	public DueDateConfig getEndDateConfig() {
		return endDateConfig;
	}

	public Integer getDuration() {
		return duration;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String nodeIdent;
		private String title;
		private String subTitle;
		private String description;
		private String iconCss;
		private String teaserImageID;
		private Mapper teaserImageMapper;
		private TeaserImageStyle teaserImageStyle;
		private String colorCategoryCss;
		private NoAccess noAccessMessage;
		private LearningPathStatus learningPathStatus;
		private DueDateConfig startDateConfig;
		private DueDateConfig endDateConfig;
		private Integer duration;

		private Builder() {
		}

		public Builder withNodeIdent(String nodeIdent) {
			this.nodeIdent = nodeIdent;
			return this;
		}

		public Builder withTitle(String title) {
			this.title = title;
			return this;
		}

		public Builder withSubTitle(String subTitle) {
			this.subTitle = subTitle;
			return this;
		}
		
		public Builder withDescription(String description) {
			this.description = description;
			return this;
		}

		public Builder withIconCss(String iconCss) {
			this.iconCss = iconCss;
			return this;
		}

		public Builder withTeaserImageID(String teaserImageID) {
			this.teaserImageID = teaserImageID;
			return this;
		}

		public Builder withTeaserImage(Mapper mapper, TeaserImageStyle style) {
			this.teaserImageMapper = mapper;
			this.teaserImageStyle = style;
			return this;
		}

		public Builder withColorCategoryCss(String colorCategoryCss) {
			this.colorCategoryCss = colorCategoryCss;
			return this;
		}

		public Builder withNoAccessMessage(NoAccess noAccessMessage) {
			this.noAccessMessage = noAccessMessage;
			return this;
		}

		public Builder withLearningPathStatus(LearningPathStatus learningPathStatus) {
			this.learningPathStatus = learningPathStatus;
			return this;
		}

		public Builder withStartDateConfig(DueDateConfig startDateConfig) {
			this.startDateConfig = startDateConfig;
			return this;
		}

		public Builder withEndDateConfig(DueDateConfig endDateConfig) {
			this.endDateConfig = endDateConfig;
			return this;
		}

		public Builder withDuration(Integer duration) {
			this.duration = duration;
			return this;
		}

		public Overview build() {
			return new Overview(this);
		}
	}

}
