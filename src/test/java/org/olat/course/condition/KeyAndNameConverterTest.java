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
package org.olat.course.condition;

import static org.olat.course.condition.KeyAndNameConverter.convertExpressionKeyToKey;
import static org.olat.course.condition.KeyAndNameConverter.convertExpressionKeyToName;
import static org.olat.course.condition.KeyAndNameConverter.convertExpressionNameToKey;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.id.Persistable;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.noderight.manager.NodeRightServiceImplTest.BusinessGroupMock;
import org.olat.group.area.BGArea;
import org.olat.group.model.BGAreaReference;
import org.olat.group.model.BusinessGroupReference;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 16.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class KeyAndNameConverterTest {
	
	@Test
	public void convertBusinessGroupNameToKey() {
		CourseEnvironmentMapper envMapper = new CourseEnvironmentMapper();
		BusinessGroupMock newGroup = new BusinessGroupMock(567l, "Group 1");
		BusinessGroupReference bgRef = new BusinessGroupReference(newGroup, 345l, "Group 1");
		envMapper.getGroups().add(bgRef);

		String convertedExp = convertExpressionNameToKey("inLearningGroup(\"Group 1\")", envMapper);
		Assert.assertEquals("inLearningGroup(\"567\")", convertedExp);
	}
	
	@Test
	public void convertBusinessGroupKeyToKey() {
		CourseEnvironmentMapper envMapper = new CourseEnvironmentMapper();
		BusinessGroupMock newGroup = new BusinessGroupMock(567l, "Group 1");
		BusinessGroupReference bgRef = new BusinessGroupReference(newGroup, 345l, "Group 1");
		envMapper.getGroups().add(bgRef);

		String convertedExp = convertExpressionKeyToKey("inRightGroup(\"345\")", envMapper);
		Assert.assertEquals("inRightGroup(\"567\")", convertedExp);
	}
	
	@Test
	public void convertBusinessGroupKeyToName() {
		CourseEnvironmentMapper envMapper = new CourseEnvironmentMapper();
		BusinessGroupMock newGroup = new BusinessGroupMock(567l, "Group 1");
		BusinessGroupReference bgRef = new BusinessGroupReference(newGroup, null, null);
		envMapper.getGroups().add(bgRef);

		String convertedExp = convertExpressionKeyToName("isLearningGroupFull(\"567\")", envMapper);
		Assert.assertEquals("isLearningGroupFull(\"Group 1\")", convertedExp);
	}
	
	@Test
	public void convertAreaNameToKey() {
		CourseEnvironmentMapper envMapper = new CourseEnvironmentMapper();
		BGArea newArea = new MockArea(567l, "Area 1");
		BGAreaReference areaRef = new BGAreaReference(newArea, 345l, "Area 1");
		envMapper.getAreas().add(areaRef);

		String convertedExp = convertExpressionNameToKey("inLearningArea(\"Area 1\")", envMapper);
		Assert.assertEquals("inLearningArea(\"567\")", convertedExp);
	}
	
	@Test
	public void convertAreaKeyToKey() {
		CourseEnvironmentMapper envMapper = new CourseEnvironmentMapper();
		BGArea newArea = new MockArea(567l, "Area 1");
		BGAreaReference areaRef = new BGAreaReference(newArea, 345l, "Area 1");
		envMapper.getAreas().add(areaRef);

		String convertedExp = convertExpressionKeyToKey("inLearningArea(\"345\")", envMapper);
		Assert.assertEquals("inLearningArea(\"567\")", convertedExp);
	}
	
	@Test
	public void convertAreaKeyToName() {
		CourseEnvironmentMapper envMapper = new CourseEnvironmentMapper();
		BGArea newArea = new MockArea(567l, "Area 1");
		BGAreaReference areaRef = new BGAreaReference(newArea, null, null);
		envMapper.getAreas().add(areaRef);

		String convertedExp = convertExpressionKeyToName("inLearningArea(\"567\")", envMapper);
		Assert.assertEquals("inLearningArea(\"Area 1\")", convertedExp);
	}
	
	/**
	 * Test with same name for area and group
	 */
	@Test
	public void convertGroupAndAreaNameToKey_sameName() {
		CourseEnvironmentMapper envMapper = new CourseEnvironmentMapper();
		BGArea newArea = new MockArea(567l, "Test 1");
		BGAreaReference areaRef = new BGAreaReference(newArea, 345l, "Test 1");
		envMapper.getAreas().add(areaRef);
		
		BusinessGroupMock newGroup = new BusinessGroupMock(568l, "Test 1");
		BusinessGroupReference bgRef = new BusinessGroupReference(newGroup, 346l, "Test 1");
		envMapper.getGroups().add(bgRef);

		String convertedExp = convertExpressionNameToKey("inLearningArea(\"Test 1\") & inLearningGroup(\"Test 1\")", envMapper);
		Assert.assertEquals("inLearningArea(\"567\") & inLearningGroup(\"568\")", convertedExp);
	}
	
	@Test
	public void convertGroupAndAreaKeyToKey_sameKey() {
		CourseEnvironmentMapper envMapper = new CourseEnvironmentMapper();
		BGArea newArea = new MockArea(567l, "Area 1");
		BGAreaReference areaRef = new BGAreaReference(newArea, 345l, "Area 1");
		envMapper.getAreas().add(areaRef);
		
		BusinessGroupMock newGroup = new BusinessGroupMock(568l, "Group 1");
		BusinessGroupReference bgRef = new BusinessGroupReference(newGroup, 345l, "Group 1");
		envMapper.getGroups().add(bgRef);

		String convertedExp = convertExpressionKeyToKey("inLearningArea(\"345\") & inRightGroup(\"345\")", envMapper);
		Assert.assertEquals("inLearningArea(\"567\") & inRightGroup(\"568\")", convertedExp);
	}
	
	@Test
	public void convertGroupAndAreaKeyToName_sameKey() {
		CourseEnvironmentMapper envMapper = new CourseEnvironmentMapper();
		BGArea newArea = new MockArea(567l, "Area 1");
		BGAreaReference areaRef = new BGAreaReference(newArea, null, null);
		envMapper.getAreas().add(areaRef);
		
		BusinessGroupMock newGroup = new BusinessGroupMock(567l, "Group 1");
		BusinessGroupReference bgRef = new BusinessGroupReference(newGroup, null, null);
		envMapper.getGroups().add(bgRef);

		String convertedExp = convertExpressionKeyToName("inLearningArea(\"567\") & isLearningGroupFull(\"567\")", envMapper);
		Assert.assertEquals("inLearningArea(\"Area 1\") & isLearningGroupFull(\"Group 1\")", convertedExp);
	}
	

	private static class MockArea implements BGArea {

		private static final long serialVersionUID = 4486855369163795150L;
		private final Long key;
		private final String name;
		
		public MockArea(Long key, String name) {
			this.key = key;
			this.name = name;
		}

		@Override
		public Long getKey() {
			return key;
		}

		@Override
		public boolean equalsByPersistableKey(Persistable persistable) {
			return false;
		}

		@Override
		public Date getCreationDate() {
			return null;
		}

		@Override
		public String getShortName() {
			return name;
		}

		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public void setDescription(String description) {
			//
		}

		@Override
		public OLATResource getResource() {
			return null;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void setName(String name) {
			//
		}
	}
}
