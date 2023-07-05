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
package org.olat.restapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.quality.manager.GeneralToDoTaskProvider;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.restapi.ToDoStatusVO;
import org.olat.modules.todo.restapi.ToDoTaskVO;
import org.olat.modules.todo.restapi.ToDoTaskVOes;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 Jun 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoTaskWebServiceTest extends OlatRestTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ToDoService toDoService;
	
	@Test
	public void getMyToDoTasks() throws IOException, URISyntaxException {
		String assigneeName = random();
		String assigneePw = random();
		Identity assignee = JunitTestHelper.createAndPersistIdentityAsUser(assigneeName, assigneePw);
		dbInstance.commitAndCloseSession();
		
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		toDoService.createToDoTask(doer, random());
		ToDoTask toDoTask2 = toDoService.createToDoTask(doer, random());
		toDoService.updateMember(doer, toDoTask2, List.of(assignee), List.of());
		ToDoTask toDoTask3 = toDoService.createToDoTask(doer, random());
		toDoService.updateMember(doer, toDoTask3, List.of(), List.of(assignee));
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login(assigneeName, assigneePw));
		
		URI uri = UriBuilder.fromUri(getContextURI())
				.path("todotasks")
				.path("my")
				.build();
		HttpGet method = conn.createGet(uri, MediaType.APPLICATION_JSON, true);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		
		ToDoTaskVOes toDoTaskVoes = conn.parse(response, ToDoTaskVOes.class);
		assertThat(toDoTaskVoes.getToDoTasks())
			.extracting(ToDoTaskVO::getKey)
			.containsExactlyInAnyOrder(toDoTask2.getKey(), toDoTask3.getKey());
		
		conn.shutdown();
	}
	
	@Test
	public void postToDoStatus() throws IOException, URISyntaxException {
		String assigneeName = random();
		String assigneePw = random();
		Identity assignee = JunitTestHelper.createAndPersistIdentityAsUser(assigneeName, assigneePw);
		dbInstance.commitAndCloseSession();
		
		Identity doer = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ToDoTask toDoTask = toDoService.createToDoTask(doer, GeneralToDoTaskProvider.TYPE);
		toDoService.updateMember(doer, toDoTask, List.of(assignee), List.of());
		dbInstance.commitAndCloseSession();
		
		RestConnection conn = new RestConnection();
		Assert.assertTrue(conn.login(assigneeName, assigneePw));
		
		URI uri = UriBuilder.fromUri(getContextURI())
				.path("todotasks")
				.path(toDoTask.getKey().toString())
				.path("status")
				.build();
		ToDoStatus status = ToDoStatus.done;
		ToDoStatusVO statusVO = new ToDoStatusVO();
		statusVO.setStatus(status.name());
		HttpPost method = conn.createPost(uri, MediaType.APPLICATION_JSON);
		conn.addJsonEntity(method, statusVO);
		HttpResponse response = conn.execute(method);
		Assert.assertEquals(200, response.getStatusLine().getStatusCode());
		EntityUtils.consume(response.getEntity());
	
		ToDoTask reloadedToDoTask = toDoService.getToDoTask(toDoTask);
		assertThat(reloadedToDoTask.getStatus()).isEqualTo(status);
		
		conn.shutdown();
	}

}
