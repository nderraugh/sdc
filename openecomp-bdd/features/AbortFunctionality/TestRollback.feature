# Copyright © 2016-2018 European Support Limited
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

Feature: Abort Flow - Test rollback

  Background: Init
    Given I want to create a VLM

  Scenario: Test rollback. In case abort it should successfully rollback to previously processed service model
    When I want to create a VSP with onboarding type "NetworkPackage"
    Then I want to make sure this Item has status "Draft"

    When I want to upload a NetworkPackage for this VSP from path "resources/uploads/BASE_MUX.zip"
    Then I want to process the NetworkPackage file for this VSP

    When I want to get path "/vendor-software-products/{item.id}/versions/{item.versionId}"
    Then I want to check property "onboardingOrigin" for value "zip"

    When I want to upload a NetworkPackage for this VSP from path "resources/uploads/errorHeat.zip"
    Then I want to process the NetworkPackage file for this VSP
    Then I want to check property "status" for value "Failure"

    When I want to get path "/vendor-software-products/{item.id}/versions/{item.versionId}"
    Then I want to check property "candidateOnboardingOrigin" for value "zip"

    When I want to delete for path "/vendor-software-products/{item.id}/versions/{item.versionId}/orchestration-template-candidate"
    # rollback - check whether it rolled back to previous processed file
    Then I want to get path "/vendor-software-products/{item.id}/versions/{item.versionId}"
    Then I want to check property "onboardingOrigin" for value "zip"
    Then I want to check property "candidateOnboardingOrigin" does not exist
