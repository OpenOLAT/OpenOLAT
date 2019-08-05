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
package org.olat.modules.fo.ui;

import java.util.Date;

import org.olat.core.commons.services.mark.Mark;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.modules.fo.Status;

/**
 * 
 * Initial date: 31 Jul 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MessageLightViewRow implements FlexiTreeTableNode {

	private final MessageLightView view;
	private Mark mark;
	private final FormLink markLink;
	private MessageLightViewRow parent;
	private boolean hasChildren;
	
	public MessageLightViewRow(MessageLightView view, Mark mark, FormLink markLink) {
		this.view = view;
		this.mark = mark;
		this.markLink = markLink;
	}
	
	public MessageLightView getView() {
		return view;
	}

	public void setMark(Mark mark) {
		this.mark = mark;
	}

	public Mark getMark() {
		return mark;
	}

	public boolean isMarked() {
		return mark != null;
	}

	public FormLink getMarkLink() {
		return markLink;
	}
	
	public boolean isSticky() {
		return Status.getStatus(view.getStatusCode()).isSticky();
	}
	
	public Date getLastModified() {
		return view.getLastModified();
	}

	public void setParent(MessageLightViewRow parent) {
		this.parent = parent;
		if (parent != null) {
			parent.hasChildren = true;
		}
	}

	@Override
	public FlexiTreeTableNode getParent() {
		return parent;
	}

	@Override
	public String getCrump() {
		return null;
	}

	public boolean hasChildren() {
		return hasChildren;
	}

}
