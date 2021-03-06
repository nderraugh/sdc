/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.itempermissions.dao.impl;

import org.mockito.*;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.itempermissions.PermissionsServices;
import org.openecomp.sdc.notification.dtos.Event;
import org.openecomp.sdc.notification.services.NotificationPropagationManager;
import org.openecomp.sdc.notification.services.SubscriptionService;
import org.openecomp.sdc.versioning.AsdcItemManager;
import org.openecomp.sdc.versioning.types.Item;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by ayalaben on 7/6/2017
 */
public class PermissionsManagerImplTest {

  private static final String ITEM1_ID = "1";
  private static final String PERMISSION = "Contributor";
  private static final String ACTION = "Change_Item_Permissions";
  private static final String USER = "user";
  private static final String AFFECTED_USER1 = "affected_user1";
  private static final String AFFECTED_USER2 = "affected_user2";
  private static final String AFFECTED_USER3 = "affected_user3";
  private static final String tenant = "dox";

  @Mock
  private PermissionsServices permissionsServicesMock;
  @Mock
  private AsdcItemManager asdcItemManagerMock;
  @Mock
  private SubscriptionService subscriptionServiceMock;
  @Mock
  private NotificationPropagationManager notifierMock;
  @Captor
  private ArgumentCaptor<Event> eventArgumentCaptor;

  @InjectMocks
  private PermissionsManagerImpl permissionsManager;


  @BeforeMethod
  public void setUp() throws Exception {
    SessionContextProviderFactory.getInstance().createInterface().create(USER,
        tenant);
    MockitoAnnotations.initMocks(this);
  }

  @Test(expectedExceptions = CoreException.class, expectedExceptionsMessageRegExp = "Permissions " +
          "Error. The user does not have permission to perform this action.")
  public void testUpdateItemPermissionsWhenNotAllowed() {
    doReturn(false).when(permissionsServicesMock).isAllowed(ITEM1_ID, USER, ACTION);

    permissionsManager
            .updateItemPermissions(ITEM1_ID, PERMISSION, Collections.singleton(AFFECTED_USER1),
                    new HashSet<>());
  }

  @Test
  public void testUpdateItemPermissions() {
    doReturn(true).when(permissionsServicesMock).isAllowed(ITEM1_ID, USER, ACTION);
    Item item = new Item();
    item.setName("Item 1 Name");
    doReturn(item).when(asdcItemManagerMock).get(ITEM1_ID);

    Set<String> addedUsersIds =
            Stream.of(AFFECTED_USER1, AFFECTED_USER2).collect(Collectors.toSet());
    Set<String> removedUsersIds = Collections.singleton(AFFECTED_USER3);
    permissionsManager
            .updateItemPermissions(ITEM1_ID, PERMISSION, addedUsersIds, removedUsersIds);

    verify(permissionsServicesMock)
            .updateItemPermissions(ITEM1_ID, PERMISSION, addedUsersIds, removedUsersIds);

    for (String addedUsersId : addedUsersIds) {
      verifyCallsToNotificationsFramework(addedUsersId, true);
    }
    for (String removedUsersId : removedUsersIds) {
      verifyCallsToNotificationsFramework(removedUsersId, false);
    }
  }

  @Test
  public void testListUserPermittedItems(){
    permissionsManager.listUserPermittedItems(AFFECTED_USER1,PERMISSION);

    verify(permissionsServicesMock)
            .listUserPermittedItems(AFFECTED_USER1, PERMISSION);

  }

  private void verifyCallsToNotificationsFramework(String affectedUser, boolean permissionGranted) {
    verifyCallToSubscriptionService(affectedUser, permissionGranted);
    verifyDirectNotificationCallParameters(affectedUser, permissionGranted);
  }

  private void verifyDirectNotificationCallParameters(String affectedUser, boolean permissionGranted) {
    verify(notifierMock).directNotification(eventArgumentCaptor.capture(), Matchers.eq(affectedUser));
    Event event = eventArgumentCaptor.getValue();
    assertTrue(event.getEventType().equals(PERMISSION_CHANGED));
    Map<String, Object> attributes = event.getAttributes();
    assertEquals(attributes.get(PERMISSION_GRANTED), permissionGranted);
    assertEquals(attributes.get(ITEM_ID_PROP), ITEM1_ID);
    assertEquals(attributes.get(PERMISSION_ITEM), PERMISSION);
  }

  private void verifyCallToSubscriptionService(String affectedUser, boolean permissionGranted) {
    if (permissionGranted) {
      verify(subscriptionServiceMock).subscribe(affectedUser, ITEM1_ID);
    } else {
      verify(subscriptionServiceMock).unsubscribe(affectedUser, ITEM1_ID);
    }
  }


}
