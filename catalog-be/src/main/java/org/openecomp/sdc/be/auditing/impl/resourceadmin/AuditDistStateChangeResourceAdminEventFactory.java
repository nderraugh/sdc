/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.auditing.impl.resourceadmin;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.common.api.Constants;

public class AuditDistStateChangeResourceAdminEventFactory extends AuditResourceAdminEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" RESOURCE_NAME = \"%s\" RESOURCE_TYPE = \"%s\" SERVICE_INSTANCE_ID = \"%s\"" +
            " CURR_VERSION = \"%s\" MODIFIER = \"%s\" CURR_STATE = \"%s\"" +
            " DPREV_STATUS = \"%s\" DCURR_STATUS = \"%s\" STATUS = \"%s\" DESC = \"%s\" COMMENT = \"%s\"";


    public AuditDistStateChangeResourceAdminEventFactory(AuditingActionEnum action, CommonAuditData commonFields, ResourceCommonInfo resourceCommonInfo,
                                                         ResourceVersionInfo prevParams, ResourceVersionInfo currParams, String invariantUuid,
                                                         User modifier, String artifactData, String comment, String did) {
        super(action, commonFields, resourceCommonInfo, prevParams, currParams, invariantUuid,
                modifier, artifactData, comment, did, Constants.EMPTY_STRING);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[] {event.getAction(), event.getResourceName(), event.getResourceType(),
                event.getServiceInstanceId(), event.getCurrVersion(), event.getModifier(),
                event.getCurrState(), event.getDprevStatus(), event.getDcurrStatus(),
                event.getStatus(), event.getDesc(), event.getComment()};
    }

}
