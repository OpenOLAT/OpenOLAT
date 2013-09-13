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
package de.bps.course.nodes.ll;

import java.io.Serializable;

/**
 * Description:<br>
 * Link list model to be used in course module configuration.
 *
 * <P>
 * Initial Date: 17.11.2008 <br>
 *
 * @author Marcel Karras (toka@freebits.de)
 * @see org.olat.modules.ModuleConfiguration
 */
public class LLModel implements Serializable {

	private String target = "";
	private String description = "";
	private String comment = "";
	private String htmlTarget = "_blank";
	private boolean intern = false;

	public LLModel() {
		// nothing to do
	}

	public LLModel(final String target, final String description, final String comment, final boolean intern) {
		this.target = target;
		this.description = description;
		this.comment = comment;
		this.intern = intern;
	}

	/**
	 * @return Returns the url of the link.
	 */
	public final String getTarget() {
		return target;
	}

	/**
	 * @param target The url of the link to set.
	 */
	public final void setTarget(final String target) {
		this.target = target;
	}

	/**
	 * @return the target to set in the html code
	 */
	public String getHtmlTarget() {
		return htmlTarget;
	}

	/**
	 * @param htmlTarget set the html target
	 */
	public void setHtmlTarget(String htmlTarget) {
		this.htmlTarget = htmlTarget;
	}

	/**
	 * @return Returns the description.
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public final void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * @return Returns the comment.
	 */
	public final String getComment() {
		return comment;
	}

	/**
	 * @param comment The comment to set.
	 */
	public final void setComment(final String comment) {
		this.comment = comment;
	}

	/**
	 * @return True if it's an intern link
	 */
	public boolean isIntern() {
		return intern;
	}

	/**
	 * @param intern
	 */
	public void setIntern(boolean intern) {
		this.intern = intern;
	}
}
