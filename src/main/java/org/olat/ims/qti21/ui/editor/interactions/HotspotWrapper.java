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
package org.olat.ims.qti21.ui.editor.interactions;

import java.util.List;

import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.interactions.HotspotAssessmentItemBuilder;

import uk.ac.ed.ph.jqtiplus.node.expression.operator.Shape;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.graphic.HotspotChoice;

/**
 * 
 * Initial date: 08.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class HotspotWrapper {
	
	private final HotspotChoice choice;
	private final HotspotAssessmentItemBuilder itemBuilder;
	
	public HotspotWrapper(HotspotChoice choice, HotspotAssessmentItemBuilder itemBuilder) {
		this.choice = choice;
		this.itemBuilder = itemBuilder;
	}
	
	public HotspotChoice getChoice() {
		return choice;
	}
	
	public boolean isCorrect() {
		return itemBuilder.isCorrect(choice);
	}

	public String getIdentifier() {
		return choice.getIdentifier().toString();
	}

	public String getShape() {
		return choice.getShape().toQtiString();
	}
	
	public void setShape(String shape) {
		if("circle".equals(shape)) {
			choice.setShape(Shape.CIRCLE);
		} else if("rect".equals(shape)) {
			choice.setShape(Shape.RECT);
		} else if("poly".equals(shape)) {
			choice.setShape(Shape.POLY);
		}
	}

	public String getCoords() {
		return AssessmentItemFactory.coordsString(choice.getCoords());
	}

	public void setCoords(String coords) {
		List<Integer> coordList = AssessmentItemFactory.coordsList(coords);
		choice.setCoords(coordList);
	}

}
