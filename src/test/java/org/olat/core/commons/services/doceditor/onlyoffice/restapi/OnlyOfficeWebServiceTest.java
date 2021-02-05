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
package org.olat.core.commons.services.doceditor.onlyoffice.restapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.AccessSearchParams;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.doceditor.onlyoffice.ApiConfig;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeEditor;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeModule;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeSecurityService;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeService;
import org.olat.core.commons.services.doceditor.onlyoffice.model.ActionImpl;
import org.olat.core.commons.services.doceditor.onlyoffice.model.CallbackImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockApplicationType;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.lock.LockInfo;
import org.olat.restapi.RestConnection;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatRestTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OnlyOfficeWebServiceTest extends OlatRestTestCase {
	
	private RestConnection conn;
	private VFSContainer vfsContainer;
	private VFSLeaf vfsLeaf;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private OnlyOfficeModule onlyOfficeModule;
	@Autowired
	private OnlyOfficeService onlyOfficeService;
	@Autowired
	private OnlyOfficeSecurityService onlyOfficeSecurityService;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private VFSLockManager vfsLockManager;

	@Before
	public void setUp() {
		onlyOfficeModule.setEnabled(true);
		onlyOfficeModule.setEditorEnabled(true);
		onlyOfficeModule.setJwtSecret(random() + random() + random());
		
		conn = new RestConnection();
		vfsContainer = VFSManager.olatRootContainer("/" + "onlyoffice_" + random(), null);
		vfsLeaf = vfsContainer.createChildLeaf(random() + ".docx");
	}

	@Test
	public void testEditSession() throws Exception {
		SoftAssertions softly = new SoftAssertions();
		
		Identity user1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity user2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		
		DocEditorConfigs docEditorConfigs = DocEditorConfigs.builder().build(vfsLeaf);
		Access access1 = docEditorService.createAccess(user1, Roles.authorRoles(), docEditorConfigs);
		docEditorService.createAccess(user2, Roles.authorRoles(), docEditorConfigs);
		ApiConfig apiConfig1 = onlyOfficeService.getApiConfig(vfsLeaf.getMetaInfo(), user1, Mode.EDIT, false, true, null);
		ApiConfig apiConfig2 = onlyOfficeService.getApiConfig(vfsLeaf.getMetaInfo(), user2, Mode.EDIT, false, true, null);
		String user1Id = apiConfig1.getEditorConfig().getUser().getId();
		String user2Id = apiConfig2.getEditorConfig().getUser().getId();
		String documentKey = apiConfig1.getDocument().getKey();
		dbInstance.commitAndCloseSession();
		
		
		// Check access
		AccessSearchParams accessSearchParams = new AccessSearchParams();
		accessSearchParams.setMetadataKey(vfsLeaf.getMetaInfo().getKey());
		accessSearchParams.setEditorType(OnlyOfficeEditor.TYPE);
		softly.assertThat(docEditorService.getAccesses(accessSearchParams))
				.as("Start with two accesses")
				.hasSize(2);
		
		
		// The user opens the file
		// OnlyOffices posts the command to edit
		// Callback: CallbackImpl [actions=[ActionImpl [type=1, userid=openolat.limmat.administrator]], forcesavetype=null, key=9228803b-cfb6-423b-973a-f90228a6bda5-20210128133145, status=1, url=null, users=[openolat.limmat.administrator]]
		CallbackImpl callback = new CallbackImpl();
		ActionImpl action = new ActionImpl();
		action.setType(1);
		action.setUserid(user1Id);
		callback.setActions(Collections.singletonList(action));
		callback.setKey(documentKey);
		callback.setStatus(1); // Editing
		callback.setUsers(new String[] {user1Id} );
		HttpResponse response = postCallback(callback);
		softly.assertThat(getCallbackError(response)).as("Response error code").isEqualTo(0);
		
		softly.assertThat(vfsLockManager.getLock(vfsLeaf).getTokens())
				.as("User 1 started editing: the lock should have one tokens")
				.hasSize(1);
		softly.assertThat(vfsLockManager.isLockedForMe(vfsLeaf, user1, VFSLockApplicationType.vfs, null))
				.as("User 1 started editing: the file should be locked for other lock types")
				.isTrue();
		softly.assertThat(onlyOfficeService.isLockedForMe(vfsLeaf, user2))
				.as("User 1 started editing: the file should be loackable for other users in OnlyOffice")
				.isFalse();
		softly.assertThat(docEditorService.getAccesses(accessSearchParams))
				.as("User 1 started editing: still two accesses should exist.")
				.hasSize(2);
		
		
		// Second user opens same file in OnlyOffice for editing
		// Callback: CallbackImpl [actions=[ActionImpl [type=1, userid=openolat.limmat.user1]], forcesavetype=null, key=9228803b-cfb6-423b-973a-f90228a6bda5-20210128133145, status=1, url=null, users=[openolat.limmat.administrator, openolat.limmat.user1]]
		callback = new CallbackImpl();
		action = new ActionImpl();
		action.setType(1);
		action.setUserid(user2Id);
		callback.setActions(Collections.singletonList(action));
		callback.setKey(documentKey);
		callback.setStatus(1); // Editing
		callback.setUsers(new String[] {user1Id, user2Id} );
		response = postCallback(callback);
		softly.assertThat(getCallbackError(response)).as("Response error code").isEqualTo(0);
		
		softly.assertThat(vfsLockManager.getLock(vfsLeaf).getTokens())
				.as("User 2 started editing in 1st window: the lock should have 2 tokens")
				.hasSize(2);
		softly.assertThat(docEditorService.getAccesses(accessSearchParams))
				.as("User 2 started editing in 1st window: still two accesses should exist.")
				.hasSize(2);
		
		// Second user opens same file in OnlyOffice for editing in a second browser window
		// Callback: CallbackImpl [actions=[ActionImpl [type=1, userid=openolat.limmat.user1]], forcesavetype=null, key=9228803b-cfb6-423b-973a-f90228a6bda5-20210128133145, status=1, url=null, users=[openolat.limmat.administrator, openolat.limmat.user1]]
		callback = new CallbackImpl();
		action = new ActionImpl();
		action.setType(1);
		action.setUserid(user2Id);
		callback.setActions(Collections.singletonList(action));
		callback.setKey(documentKey);
		callback.setStatus(1); // Editing
		callback.setUsers(new String[] {user1Id, user2Id} );
		response = postCallback(callback);
		softly.assertThat(getCallbackError(response)).as("Response error code").isEqualTo(0);
		
		softly.assertThat(vfsLockManager.getLock(vfsLeaf).getTokens())
				.as("User 2 started editing in 2nd window: the lock should have 3 tokens")
				.hasSize(3);
		softly.assertThat(docEditorService.getAccesses(accessSearchParams))
				.as("User 2 started editing in 2nd window: still two accesses should exist.")
				.hasSize(2);
		
		
		// Second user closes second window
		//Callback: CallbackImpl [actions=[ActionImpl [type=0, userid=openolat.limmat.user1]], forcesavetype=null, key=9228803b-cfb6-423b-973a-f90228a6bda5-20210128134334, status=1, url=null, users=[openolat.limmat.administrator, openolat.limmat.user1]]
		callback = new CallbackImpl();
		action = new ActionImpl();
		action.setType(0); // closed
		action.setUserid(user2Id);
		callback.setActions(Collections.singletonList(action));
		callback.setKey(documentKey);
		callback.setStatus(1); // Editing
		callback.setUsers(new String[] {user1Id, user2Id} );
		response = postCallback(callback);
		softly.assertThat(getCallbackError(response)).as("Response error code").isEqualTo(0);
		
		// We do not know which token to remove...
		softly.assertThat(vfsLockManager.getLock(vfsLeaf).getTokens())
				.as("User 2 finished editing in 1st window: the lock should still have 3 tokens")
				.hasSize(3);
		softly.assertThat(docEditorService.getAccesses(accessSearchParams))
				.as("User 2 finished editing in 1st window: access of user 2 should still exist")
				.hasSize(2);
		
		
		// Second user closes OnlyOffice (with edits)
		// Callback: CallbackImpl [actions=[ActionImpl [type=0, userid=openolat.limmat.user1]], forcesavetype=null, key=9228803b-cfb6-423b-973a-f90228a6bda5-20210128133145, status=1, url=null, users=[openolat.limmat.administrator]]
		callback = new CallbackImpl();
		action = new ActionImpl();
		action.setType(0); // closed
		action.setUserid(user2Id);
		callback.setActions(Collections.singletonList(action));
		callback.setKey(documentKey);
		callback.setStatus(1); // Editing
		callback.setUsers(new String[] {user1Id} );
		response = postCallback(callback);
		softly.assertThat(getCallbackError(response)).as("Response error code").isEqualTo(0);
		
		// We do not know which token to remove...
		softly.assertThat(vfsLockManager.getLock(vfsLeaf).getTokens())
				.as("User 2 finished editing in 2nd window: the lock has still 3 tokens")
				.hasSize(3);
		softly.assertThat(docEditorService.getAccesses(accessSearchParams))
				.as("User 2 finished editing in 2nd window: Access of user 2 should be deleted")
				.hasSize(1)
				.containsExactly(access1);
		
		
		// First user closes OnlyOffice as well (with edits)
		// Callback: CallbackImpl [actions=[ActionImpl [type=0, userid=openolat.limmat.administrator]], forcesavetype=null, key=9228803b-cfb6-423b-973a-f90228a6bda5-20210128133145, status=2, url=https://onlyoffice.openolat.org/cache/files/9228803b-cfb6-423b-973a-f90228a6bda5-20210128133145_5054/output.docx/output.docx?md5=gfAQjiz1RPMlc2bMS6mE6g&expires=1611838714&disposition=attachment&filename=output.docx, users=[openolat.limmat.administrator]]
		callback = new CallbackImpl();
		action = new ActionImpl();
		action.setType(0); // closed
		action.setUserid(user1Id);
		callback.setActions(Collections.singletonList(action));
		callback.setKey(documentKey);
		callback.setStatus(2); // Must save
		callback.setUrl(conn.getContextURI().path("dmz/index.html").build().toString()); // Get the OpenOlat main URL as a fake
		callback.setUsers(new String[] {user1Id} );
		response = postCallback(callback);
		
		softly.assertThat(vfsLockManager.isLockedForMe(vfsLeaf, user2, VFSLockApplicationType.vfs, null))
				.as("User 1 finished editing: No locks anymore")
				.isFalse();
		softly.assertThat(docEditorService.getAccesses(accessSearchParams))
				.as("User 1 finished editing: All access deleted")
				.isEmpty();
		
		softly.assertAll();
	}
	
	@Test
	public void testEditSessionNoChanges() throws Exception {
		SoftAssertions softly = new SoftAssertions();
		
		Identity user1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity user2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		
		DocEditorConfigs docEditorConfigs = DocEditorConfigs.builder().build(vfsLeaf);
		// 3 access to test the deletion of all access
		docEditorService.createAccess(user1, Roles.authorRoles(), docEditorConfigs);
		docEditorService.createAccess(user1, Roles.authorRoles(), docEditorConfigs);
		docEditorService.createAccess(user2, Roles.authorRoles(), docEditorConfigs);
		ApiConfig apiConfig1 = onlyOfficeService.getApiConfig(vfsLeaf.getMetaInfo(), user1, Mode.EDIT, false, true, null);
		String user1Id = apiConfig1.getEditorConfig().getUser().getId();
		String documentKey = apiConfig1.getDocument().getKey();
		dbInstance.commitAndCloseSession();
		
		AccessSearchParams accessSearchParams = new AccessSearchParams();
		accessSearchParams.setMetadataKey(vfsLeaf.getMetaInfo().getKey());
		accessSearchParams.setEditorType(OnlyOfficeEditor.TYPE);
		
		
		// User 1 opens the document
		// Callback: CallbackImpl [actions=[ActionImpl [type=1, userid=openolat.limmat.administrator]], forcesavetype=null, key=9228803b-cfb6-423b-973a-f90228a6bda5-20210128133145, status=1, url=null, users=[openolat.limmat.administrator]]
		CallbackImpl callback = new CallbackImpl();
		ActionImpl action = new ActionImpl();
		action.setType(1);
		action.setUserid(user1Id);
		callback.setActions(Collections.singletonList(action));
		callback.setKey(documentKey);
		callback.setStatus(1); // Editing
		callback.setUsers(new String[] {user1Id} );
		HttpResponse response = postCallback(callback);
		softly.assertThat(getCallbackError(response)).as("Response error code").isEqualTo(0);
		
		LockInfo lock = vfsLockManager.getLock(vfsLeaf);
		softly.assertThat(lock)
				.as("User 1 started editing: The file has to be locked")
				.isNotNull();
		softly.assertThat(vfsLockManager.isLockedForMe(vfsLeaf, user1, VFSLockApplicationType.vfs, null))
				.as("User 1 started editing: The file should be locked for other lock types")
				.isTrue();
		softly.assertThat(docEditorService.getAccesses(accessSearchParams))
				.as("User 1 started editing: The access should still exist")
				.hasSize(3);
		
		
		// First user closes OnlyOffice as well (without edits)
		//Callback: CallbackImpl [actions=[ActionImpl [type=0, userid=openolat.limmat.administrator]], forcesavetype=null, key=8747fc44-fe74-4ead-8052-278f2be2a858-20210129135917, status=4, url=null, users=null]
		callback = new CallbackImpl();
		action = new ActionImpl();
		action.setType(0); // closed
		action.setUserid(user1Id);
		callback.setActions(Collections.singletonList(action));
		callback.setKey(documentKey);
		callback.setStatus(4); // ClosedWithoutChanges
		callback.setUsers(null);
		response = postCallback(callback);
		softly.assertThat(getCallbackError(response)).as("Response error code").isEqualTo(0);
		
		softly.assertThat(vfsLockManager.isLockedForMe(vfsLeaf, user1, VFSLockApplicationType.vfs, null))
				.as("User 1 finished editing: No locks anymore")
				.isFalse();
		softly.assertThat(docEditorService.getAccesses(accessSearchParams))
				.as("User 1 finished editing: All access deleted")
				.isEmpty();
		
		softly.assertAll();
	}
	
	@Test
	public void testViewSession() throws Exception {
		// USE CASE:
		// User 1 opens document
		// User 2 opens document in 1st window
		// User 2 opens document in 2st window
		// User 2 closes 1st window
		// User 2 closes 2st window
		// User 1 closes document
		
		// No request to the callback end point, so we do have nothing to test.
		
		// If someone has the document open in view mode, current edits are not
		// displayed?! Apparently the automatic refresh of the other window only works
		// with two edit sessions.
	}
	
	@Test
	public void shouldReturnSuccessWhenGettingCallbackIfAutorisationIsOk() throws Exception {
		CallbackImpl callback = new CallbackImpl();
		callback.setStatus(CallbackStatus.ErrorCorrupted.getValue());
		HttpResponse response = postCallback(callback);
		
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.OK.getStatusCode());
	}

	@Test
	public void shouldReturnBadRequestWhenGettingCallbackIfNoAutorisation() throws Exception {
		HttpPost request = conn.createPost(createCallbackUri(), MediaType.APPLICATION_JSON);
		HttpResponse response = conn.execute(request);
		
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
	}

	@Test
	public void shouldReturnBadRequestWhenGettingCallbackIfAutorisationfails() throws Exception {
		HttpPost request = conn.createPost(createCallbackUri(), MediaType.APPLICATION_JSON);
		request.addHeader("Authorization", "not a valid jwt token");
		HttpResponse response = conn.execute(request);
		
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
	}
	
	@Test
	public void shouldReturnSuccessWhenGettingContentIfAutorisationIsOk() throws Exception {
		HttpUriRequest request = new HttpGet(createContentUri());
		decorateAuthorisation(request, "something");
		HttpResponse response = conn.execute(request);

		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.OK.getStatusCode());
	}
	
	@Test
	public void shouldReturnBadRequestWhenGettingContentIfNoAutorisation() throws Exception {
		HttpUriRequest request = new HttpGet(createContentUri());
		HttpResponse response = conn.execute(request);
		
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
	}
	
	@Test
	public void shouldReturnBadRequestWhenGettingContentIfAutorisationFails() throws Exception {
		HttpUriRequest request = new HttpGet(createContentUri());
		request.addHeader("Authorization", "not a valid jwt token");
		HttpResponse response = conn.execute(request);
		
		assertThat(response.getStatusLine().getStatusCode()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
	}
	
	private HttpResponse postCallback(CallbackImpl callback) throws URISyntaxException, IOException {
		HttpPost request = conn.createPost(createCallbackUri(), MediaType.APPLICATION_JSON);
		decorateAuthorisation(request, callback);
		return conn.execute(request);
	}
	
	private URI createCallbackUri() throws URISyntaxException {
		return conn.getContextURI()
				.path("onlyoffice")
				.path("files")
				.path(getFileId())
				.path("callback")
				.build();
	}
	
	private URI createContentUri() throws URISyntaxException {
		return conn.getContextURI()
				.path("onlyoffice")
				.path("files")
				.path(getFileId())
				.path("contents")
				.build();
	}

	private String getFileId() {
		return vfsLeaf.getMetaInfo().getUuid();
	}

	
	private void decorateAuthorisation(HttpRequest request, Object object) {
		String autorisation = createAuthorisation(object);
		request.addHeader("Authorization", autorisation);
	}
	
	private String createAuthorisation(Object object) {
		Map<String, Object> payloadMap = new HashMap<>();
		payloadMap.put("payload", object);
		String payloadToken = onlyOfficeSecurityService.getToken(payloadMap);
		
		return "Bearer " + payloadToken;
	}
	
	private int getCallbackError(HttpResponse response) {
		CallbackResponseVO callbackResonse = conn.parse(response, CallbackResponseVO.class);
		return callbackResonse.getError();
	}

}
