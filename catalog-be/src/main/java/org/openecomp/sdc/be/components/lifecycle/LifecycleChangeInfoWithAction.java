/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.lifecycle;

import com.fasterxml.jackson.annotation.JsonInclude;

public class LifecycleChangeInfoWithAction extends LifecycleChangeInfoBase {

    public enum LifecycleChanceActionEnum {
        CREATE_FROM_CSAR, UPDATE_FROM_EXTERNAL_API, UPGRADE_MIGRATION
    };

    @JsonInclude
    private LifecycleChanceActionEnum action;

    public LifecycleChangeInfoWithAction() {
    }

    public LifecycleChangeInfoWithAction(String userRemarks) {
        super(userRemarks);
    }

    public LifecycleChangeInfoWithAction(String userRemarks, LifecycleChanceActionEnum action) {
        super(userRemarks);
        this.action = action;
    }

    public LifecycleChanceActionEnum getAction() {
        return action;
    }

    public void setAction(LifecycleChanceActionEnum action) {
        this.action = action;
    }
}
