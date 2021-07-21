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
package org.olat.course.style;

import org.olat.core.dispatcher.mapper.Mapper;

/**
 * 
 * Initial date: 24 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class Header {
	
	private final String title;
	private final String description;
	private final String objectives;
	private final String instruction;
	private final String instructionalDesign;
	private final String iconCss;
	private final Mapper teaserImageMapper;
	private final TeaserImageStyle teaserImageStyle;
	private final String colorCategoryCss;

	private Header(Builder builder) {
		this.title = builder.title;
		this.description = builder.description;
		this.objectives = builder.objectives;
		this.instruction = builder.instruction;
		this.instructionalDesign = builder.instructionalDesign;
		this.iconCss = builder.iconCss;
		this.teaserImageMapper = builder.teaserImageMapper;
		this.teaserImageStyle = builder.teaserImageStyle;
		this.colorCategoryCss = builder.colorCategoryCss;
	}
	
	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getObjectives() {
		return objectives;
	}

	public String getInstruction() {
		return instruction;
	}

	public String getInstructionalDesign() {
		return instructionalDesign;
	}

	public String getIconCss() {
		return iconCss;
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
	
	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String title;
		private String description;
		private String objectives;
		private String instruction;
		private String instructionalDesign;
		private String iconCss;
		private Mapper teaserImageMapper;
		private TeaserImageStyle teaserImageStyle;
		private String colorCategoryCss;

		private Builder() {
		}

		public Builder withTitle(String title) {
			this.title = title;
			return this;
		}

		public Builder withDescription(String description) {
			this.description = description;
			return this;
		}

		public Builder withObjectives(String objectives) {
			this.objectives = objectives;
			return this;
		}
		
		public Builder withInstruction(String instruction) {
			this.instruction = instruction;
			return this;
		}
		
		public Builder withInstrucionalDesign(String instructionalDesign) {
			this.instructionalDesign = instructionalDesign;
			return this;
		}

		public Builder withIconCss(String iconCss) {
			this.iconCss = iconCss;
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

		public Header build() {
			return new Header(this);
		}
	}

}
