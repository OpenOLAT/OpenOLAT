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
package org.olat.course.style.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.style.ImageSource;
import org.olat.course.style.ImageSourceType;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Jul 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseImageStorageTest extends OlatTestCase {
	
	@Autowired
	private CourseImageStorage sut;

	@Test
	public void shouldStoreCourseImage() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(identity);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		File file = getExistingFile();
		String filename = random();
		
		ImageSource imageSource = sut.store(course.getCourseBaseContainer(), identity, file, filename);
		
		assertThat(imageSource.getType()).isEqualTo(ImageSourceType.course);
		assertThat(imageSource.getFilename()).isEqualTo(FileUtils.cleanFilename(filename));
		assertThat(imageSource.getPath()).isNotNull();
	}
	
	@Test
	public void shouldOverrideOldCourseImage() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(identity);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		File file = getExistingFile();
		String filename = random();
		sut.store(course.getCourseBaseContainer(), identity, file, filename);
		
		sut.store(course.getCourseBaseContainer(), identity, file, random());
		
		assertThat(sut.getCourseContainer(course.getCourseBaseContainer()).getItems()).hasSize(1);
	}
	
	@Test
	public void shouldLoadCourseImage() throws IOException {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(identity);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		File file = getExistingFile();
		String filename = random();
		sut.store(course.getCourseBaseContainer(), identity, file, filename);
		
		VFSLeaf vfsLeaf = sut.load(course.getCourseBaseContainer());
		
		assertThat(vfsLeaf.getName()).isEqualTo(FileUtils.cleanFilename(filename));
		assertThat(vfsLeaf.getSize()).isEqualTo(Files.size(file.toPath()));
	}
	
	@Test
	public void shouldDeleteCourseImage() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(identity);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		File file = getExistingFile();
		String filename = random();
		sut.store(course.getCourseBaseContainer(), identity, file, filename);
		VFSLeaf vfsLeaf = sut.load(course.getCourseBaseContainer());
		assertThat(vfsLeaf).isNotNull();
		
		sut.delete(course.getCourseBaseContainer());
		
		vfsLeaf = sut.load(course.getCourseBaseContainer());
		assertThat(vfsLeaf).isNull();
	}

	@Test
	public void shouldStoreCourseNodeImage() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(identity);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		CourseNode courseNode = new STCourseNode();
		courseNode.setIdent(random());
		File file = getExistingFile();
		String filename = random();
		
		ImageSource imageSource = sut.store(course.getCourseBaseContainer(), courseNode, identity, file, filename);
		
		assertThat(imageSource.getType()).isEqualTo(ImageSourceType.courseNode);
		assertThat(imageSource.getFilename()).isEqualTo(FileUtils.cleanFilename(filename));
		assertThat(imageSource.getPath()).isEqualTo(courseNode.getIdent());
	}
	
	@Test
	public void shouldOverrideOldCourseNodeImage() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(identity);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		CourseNode courseNode = new STCourseNode();
		File file = getExistingFile();
		String filename = random();
		sut.store(course.getCourseBaseContainer(), courseNode, identity, file, filename);
		
		sut.store(course.getCourseBaseContainer(), courseNode, identity, file, random());
		
		assertThat(sut.getCourseNodeContainer(course.getCourseBaseContainer(), courseNode).getItems()).hasSize(1);
	}
	
	@Test
	public void shouldLoadCourseNodeImage() throws IOException {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(identity);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		CourseNode courseNode = new STCourseNode();
		File file = getExistingFile();
		String filename = random();
		sut.store(course.getCourseBaseContainer(), courseNode, identity, file, filename);
		
		VFSLeaf vfsLeaf = sut.load(course.getCourseBaseContainer(), courseNode);
		
		assertThat(vfsLeaf.getName()).isEqualTo(FileUtils.cleanFilename(filename));
		assertThat(vfsLeaf.getSize()).isEqualTo(Files.size(file.toPath()));
	}
	
	@Test
	public void shouldDeleteCourseNodeImage() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		RepositoryEntry courseEntry = JunitTestHelper.deployBasicCourse(identity);
		ICourse course = CourseFactory.loadCourse(courseEntry);
		CourseNode courseNode = new STCourseNode();
		File file = getExistingFile();
		String filename = random();
		sut.store(course.getCourseBaseContainer(), courseNode, identity, file, filename);
		VFSLeaf vfsLeaf = sut.load(course.getCourseBaseContainer(), courseNode);
		assertThat(vfsLeaf).isNotNull();
		
		sut.delete(course.getCourseBaseContainer(), courseNode);
		
		vfsLeaf = sut.load(course.getCourseBaseContainer(), courseNode);
		assertThat(vfsLeaf).isNull();
	}
	
	private File getExistingFile() {
		try {
			URL url = JunitTestHelper.class.getResource("file_resources/house.jpg");
			return Paths.get(url.toURI()).toFile();
		} catch (URISyntaxException e) {
			throw(new RuntimeException(e));
		}
	}

}
