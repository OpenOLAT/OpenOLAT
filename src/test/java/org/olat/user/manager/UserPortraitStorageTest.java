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
package org.olat.user.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.restapi.CourseTest;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.olat.user.PortraitSize;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Apr 10, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class UserPortraitStorageTest extends OlatTestCase {
	
	@Autowired
	private UserPortraitStorage sut;

	@Test
	public void shoudStoreImage() throws URISyntaxException {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsUser(JunitTestHelper.random());
		File file = new File(CourseTest.class.getResource("portrait.jpg").toURI());
		
		String path = sut.store(doer, file, "portrait.jpg");
		assertThat(path).isNotNull();
		
		VFSLeaf image = sut.getImage(path, null);
		assertThat(image).isNotNull();
		assertThat(image.exists()).isTrue();
	}
	
	@Test
	public void shouldCreateSizedImages() throws URISyntaxException {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsUser(JunitTestHelper.random());
		File file = new File(CourseTest.class.getResource("portrait.jpg").toURI());
		
		String path = sut.store(doer, file, "portrait.jpg");
		
		VFSLeaf image = sut.getImage(path, PortraitSize.small);
		assertThat(image).isNotNull();
		assertThat(image.exists()).isTrue();
		
		image = sut.getImage(path, PortraitSize.xsmall);
		assertThat(image).isNotNull();
		assertThat(image.exists()).isTrue();
		
		image = sut.getImage(path, PortraitSize.medium);
		assertThat(image).isNotNull();
		assertThat(image.exists()).isTrue();
		
		 image = sut.getImage(path, PortraitSize.large);
		assertThat(image).isNotNull();
		assertThat(image.exists()).isTrue();
	}

	@Test
	public void shoudStoreImageInIndexDir() throws URISyntaxException {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsUser(JunitTestHelper.random());
		File file = new File(CourseTest.class.getResource("portrait.jpg").toURI());
		
		String path = sut.store(doer, file, "portrait.jpg");
		
		VFSLeaf image = sut.getImage(path, null);
		assertThat(image.getRelPath()).contains(path.substring(0, 2) + "/" + path);
	}

	@Test
	public void shoudDeleteImage() throws URISyntaxException {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsUser(JunitTestHelper.random());
		File file = new File(CourseTest.class.getResource("portrait.jpg").toURI());
		
		String path = sut.store(doer, file, "portrait.jpg");
		assertThat(path).isNotNull();
		
		sut.delete(path);
		
		VFSLeaf image = sut.getImage(path, null);
		assertThat(image).isNull();
	}

	@Test
	public void shoudDeleteEmptyIndexDirs() throws URISyntaxException {
		Identity doer = JunitTestHelper.createAndPersistIdentityAsUser(JunitTestHelper.random());
		File file = new File(CourseTest.class.getResource("portrait.jpg").toURI());
		
		sut.tryToStore(doer, "su123", file, "portrait.jpg");
		sut.tryToStore(doer, "su124", file, "portrait.jpg");
		
		// Two dirs in index dir
		VFSContainer imageContainer = sut.getImageContainer("su123");
		assertThat(imageContainer.exists()).isTrue();
		assertThat(imageContainer.getParentContainer().exists()).isTrue();
		
		// One dirs in index dir
		sut.delete("su124");
		assertThat(imageContainer.exists()).isTrue();
		assertThat(imageContainer.getParentContainer().exists()).isTrue();
		
		// Zero dirs in index dir
		sut.delete("su123");
		assertThat(imageContainer.exists()).isFalse();
		assertThat(imageContainer.getParentContainer().exists()).isFalse();
	}

}
