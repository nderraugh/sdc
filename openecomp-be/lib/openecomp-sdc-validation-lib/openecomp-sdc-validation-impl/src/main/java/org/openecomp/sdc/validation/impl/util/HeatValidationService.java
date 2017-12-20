/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */

package org.openecomp.sdc.validation.impl.util;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.DefinedHeatParameterTypes;
import org.openecomp.sdc.heat.datatypes.model.Environment;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Parameter;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.validation.impl.validators.HeatValidator;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;


public class HeatValidationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeatValidator.class);
  private static final String NESTED_FILE = "nested file";
  private static final String NO_CONTENT_IN_FILE_MSG = "The file ' %s ' has no content";
  private static final MdcDataDebugMessage MDC_DATA_DEBUG_MESSAGE = new MdcDataDebugMessage();
  private HeatValidationService(){

  }
  /**
   * Check artifacts existence.
   *
   * @param fileName the file name
   * @param artifactsNames the artifacts names
   * @param globalContext the global context
   */
  public static void checkArtifactsExistence(String fileName, Set<String> artifactsNames,
                                             GlobalValidationContext globalContext) {


    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", fileName);
    artifactsNames
            .stream()
            .filter(artifactName -> !globalContext.getFileContextMap().containsKey(artifactName))
            .forEach(artifactName ->
              globalContext.addMessage(fileName,
                      ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                              .getErrorWithParameters(
                                      globalContext.getMessageCode(),
                                      Messages.MISSING_ARTIFACT.getErrorMessage(), artifactName),
                      LoggerTragetServiceName.VALIDATE_ARTIFACTS_EXISTENCE,
                      LoggerErrorDescription.MISSING_FILE));

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", fileName);
  }

  /**
   * Draw files loop string.
   *
   * @param filesInPath the files in path
   * @return the string
   */
  public static String drawFilesLoop(List<String> filesInPath) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("[");
    int pathSize = filesInPath.size();

    for (int i = 0; i < pathSize; i++) {
      stringBuilder.append(filesInPath.get(i));
      if (i != pathSize - 1) {
        stringBuilder.append(" -- ");
      }
    }
    if (!filesInPath.get(0).equals(filesInPath.get(pathSize - 1))) {
      stringBuilder.append(" -- ");
      stringBuilder.append(filesInPath.get(0));
    }
    stringBuilder.append("]");

    return stringBuilder.toString();
  }

  /**
   * Check nested parameters.
   *
   * @param parentFileName the calling nested file name
   * @param nestedFileName the nested file name
   * @param globalContext the global context
   * @param parentParameters parent parameters.
   * @param nestedParameters nested parameters.
   * @param nestedParametersNames nested parameter names.
   */
  public static void checkNestedParameters(String parentFileName, String nestedFileName,
                                           GlobalValidationContext globalContext,
                                           Map<String, Parameter> parentParameters,
                                           Map<String, Parameter> nestedParameters,
                                           Set<String> nestedParametersNames) {

    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", parentFileName);

    HeatOrchestrationTemplate parentHeatOrchestrationTemplate;
    HeatOrchestrationTemplate nestedHeatOrchestrationTemplate;

    try {
      nestedHeatOrchestrationTemplate = getHeatOrchestrationTemplate(nestedFileName, globalContext);
      parentHeatOrchestrationTemplate = getHeatOrchestrationTemplate(parentFileName, globalContext);
    } catch (Exception exception) {
      MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", parentFileName);
      return;
    }

    parentParameters.putAll(parentHeatOrchestrationTemplate.getParameters());
    nestedParameters.putAll(nestedHeatOrchestrationTemplate.getParameters());
    if (!nestedParameters.isEmpty()) {
      nestedParametersNames.addAll(nestedHeatOrchestrationTemplate.getParameters().keySet());
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", parentFileName);
  }

  private static HeatOrchestrationTemplate getHeatOrchestrationTemplate(String fileName,
                                                                        GlobalValidationContext globalContext)
          throws Exception {

    Optional<InputStream> fileContent = globalContext.getFileContent(fileName);
    if (fileContent.isPresent()) {
      return new YamlUtil().yamlToObject(fileContent.get(), HeatOrchestrationTemplate.class);
    } else {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
              LoggerTragetServiceName.VALIDATE_PROPERTIES_MATCH_NESTED_PARAMETERS,
              ErrorLevel.ERROR.name(), LoggerErrorCode.DATA_ERROR.getErrorCode(),
              LoggerErrorDescription.EMPTY_FILE);
      Exception exception = new Exception(String.format(NO_CONTENT_IN_FILE_MSG, fileName));
      LOGGER.error("Error while reading file : " + fileName , exception);
      throw exception;
    }
  }

  public static void checkNestedParametersNoMissingParameterInNested(String parentFileName,
                                                                     String nestedFileName,
                                                                     String resourceName,
                                                                     Set<String> resourceFileProperties,
                                                                     GlobalValidationContext globalContext) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", parentFileName);

    Map<String, Parameter> parentParameters = new HashMap<>();
    Map<String, Parameter> nestedParameters = new HashMap<>();
    Set<String> nestedParametersNames = new HashSet<>();
    checkNestedParameters(parentFileName, nestedFileName, globalContext, parentParameters,
            nestedParameters, nestedParametersNames);

    checkNoMissingParameterInNested(parentFileName, nestedFileName, resourceName,
            resourceFileProperties, nestedParametersNames, globalContext);

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", parentFileName);
  }

  public static void checkNestedInputValuesAlignWithType(String parentFileName,
                                                         String nestedFileName,
                                                         String resourceName, Resource resource,
                                                         Optional<String> indexVarValue,
                                                         GlobalValidationContext globalContext) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", parentFileName);

    Map<String, Parameter> parentParameters = new HashMap<>();
    Map<String, Parameter> nestedParameters = new HashMap<>();
    Set<String> nestedParametersNames = new HashSet<>();
    checkNestedParameters(parentFileName, nestedFileName, globalContext, parentParameters,
            nestedParameters, nestedParametersNames);

    checkNestedInputValuesAlignWithType(parentFileName, nestedFileName,
            nestedParameters, resourceName, resource, indexVarValue, globalContext);

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", parentFileName);
  }

  private static void checkNoMissingParameterInNested(String parentFileName, String nestedFileName,
                                                      String resourceName,
                                                      Set<String> resourceFileProperties,
                                                      Set<String> nestedParametersNames,
                                                      GlobalValidationContext globalContext) {

    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("nested file", nestedFileName);

    if (CollectionUtils.isNotEmpty(nestedParametersNames)) {
      resourceFileProperties
              .stream()
              .filter(propertyName -> !nestedParametersNames.contains(propertyName))
              .forEach(propertyName -> globalContext
                      .addMessage(parentFileName, ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                                      .getErrorWithParameters(
                                              globalContext.getMessageCode(),
                                              Messages.MISSING_PARAMETER_IN_NESTED.getErrorMessage(),
                                              nestedFileName, resourceName, propertyName),
                              LoggerTragetServiceName.VALIDATE_PROPERTIES_MATCH_NESTED_PARAMETERS,
                              LoggerErrorDescription.MISSING_PARAMETER_IN_NESTED));
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage(NESTED_FILE, nestedFileName);
  }

  private static void checkNestedInputValuesAlignWithType(String parentFileName,
                                                          String nestedFileName,
                                                          Map<String, Parameter> nestedParameters,
                                                          String resourceName, Resource resource,
                                                          Optional<String> indexVarValue,
                                                          GlobalValidationContext globalContext) {

    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage(NESTED_FILE, nestedFileName);

    Map<String, Object> properties = resource.getProperties();
    for (Map.Entry<String, Object> propertyEntry : properties.entrySet()) {
      String parameterName = propertyEntry.getKey();
      Object parameterInputValue = propertyEntry.getValue();
      if (parameterInputValue instanceof String) {
        if (indexVarValue.isPresent() && indexVarValue.get().equals(parameterInputValue)) {
          parameterInputValue = 3; //indexVarValue is actually number value in runtime
        }
        validateStaticValueForNestedInputParameter(parentFileName, nestedFileName, resourceName,
                parameterName, parameterInputValue, nestedParameters.get(parameterName),
                globalContext);
      }
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage(NESTED_FILE, nestedFileName);
  }

  private static void validateStaticValueForNestedInputParameter(String parentFileName,
                                                                 String nestedFileName,
                                                                 String resourceName,
                                                                 String parameterName,
                                                                 Object staticValue,
                                                                 Parameter parameterInNested,
                                                                 GlobalValidationContext
                                                                         globalContext) {

    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage(NESTED_FILE, nestedFileName);

    if (parameterInNested == null) {
      return;
    }
    if (!DefinedHeatParameterTypes
            .isValueIsFromGivenType(staticValue, parameterInNested.getType())) {
      globalContext.addMessage(parentFileName, ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                      .getErrorWithParameters(globalContext.getMessageCode(),
                              Messages.WRONG_VALUE_TYPE_ASSIGNED_NESTED_INPUT.getErrorMessage(),
                              resourceName, parameterName, nestedFileName),
              LoggerTragetServiceName.VALIDATE_PROPERTIES_MATCH_NESTED_PARAMETERS,
              LoggerErrorDescription.WRONG_VALUE_ASSIGNED_NESTED_PARAMETER);
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage(NESTED_FILE, nestedFileName);
  }


  /**
   * Is nested loop exist in file boolean.
   *
   * @param callingFileName the calling file name
   * @param nestedFileName the nested file name
   * @param filesInLoop the files in loop
   * @param globalContext the global context
   * @return the boolean
   */
  public static boolean isNestedLoopExistInFile(String callingFileName, String nestedFileName,
                                                List<String> filesInLoop,
                                                GlobalValidationContext globalContext) {

    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("file", callingFileName);

    HeatOrchestrationTemplate nestedHeatOrchestrationTemplate;
    try {
      nestedHeatOrchestrationTemplate = getNestedHeatOrchestrationTemplate(nestedFileName,
                                          globalContext);
    } catch (Exception exception) {
      LOGGER.error("Error while reading file :  " + nestedFileName, exception);
      LOGGER.warn("HEAT Validator will not be executed on file " + nestedFileName
              + " due to illegal HEAT format");

      MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", callingFileName);
      return false;
    }
    filesInLoop.add(nestedFileName);
    Collection<Resource> nestedResources =
            nestedHeatOrchestrationTemplate.getResources() == null ? null
                    : nestedHeatOrchestrationTemplate.getResources().values();
    boolean isNestedLoopExist = addNestedFilesInLoopAndCheckIfNestedLoopExist(nestedResources,
                    callingFileName, filesInLoop, globalContext);

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", callingFileName);
    return isNestedLoopExist;
  }
  private static boolean addNestedFilesInLoopAndCheckIfNestedLoopExist(
                Collection<Resource> nestedResources,String callingFileName,
                List<String> filesInLoop,
                GlobalValidationContext globalContext){
    if (CollectionUtils.isNotEmpty(nestedResources)) {
      for (Resource resource : nestedResources) {
        String resourceType = resource.getType();

        if (Objects.nonNull(resourceType) && isNestedResource(resourceType)) {
          MDC_DATA_DEBUG_MESSAGE.debugExitMessage("file", callingFileName);
          return resourceType.equals(callingFileName) || !filesInLoop.contains(resourceType)
                  && isNestedLoopExistInFile(callingFileName, resourceType, filesInLoop, globalContext);
        }
      }
    }
    return false;
  }
  private static HeatOrchestrationTemplate getNestedHeatOrchestrationTemplate( String nestedFileName,
                                          GlobalValidationContext globalContext) throws Exception {
    Optional<InputStream> fileContent = globalContext.getFileContent(nestedFileName);
    HeatOrchestrationTemplate nestedHeatOrchestrationTemplate;
    if (fileContent.isPresent()) {
      nestedHeatOrchestrationTemplate =
              new YamlUtil().yamlToObject(fileContent.get(), HeatOrchestrationTemplate.class);
    } else {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
              LoggerTragetServiceName.VALIDATE_NESTING_LOOPS, ErrorLevel.ERROR.name(),
              LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.EMPTY_FILE);
      throw new Exception(String.format(NO_CONTENT_IN_FILE_MSG, nestedFileName));
    }

    return nestedHeatOrchestrationTemplate;
  }

  public static boolean isNestedResource(String resourceType) {
    return resourceType.contains(".yaml") || resourceType.contains(".yml");
  }

  /**
   * Validate env content environment.
   *
   * @param fileName the file name
   * @param envFileName the env file name
   * @param globalContext the global context
   * @return the environment
   */
  public static Environment validateEnvContent(String fileName, String envFileName,
                                               GlobalValidationContext globalContext) {

    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage("env file", envFileName);

    Environment envContent;
    try {
      Optional<InputStream> fileContent = globalContext.getFileContent(envFileName);
      if (fileContent.isPresent()) {
        envContent = new YamlUtil().yamlToObject(fileContent.get(), Environment.class);
      } else {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
                LoggerTragetServiceName.VALIDATE_ENV_FILE, ErrorLevel.ERROR.name(),
                LoggerErrorCode.DATA_ERROR.getErrorCode(), LoggerErrorDescription.EMPTY_FILE);
        throw new Exception(String.format(NO_CONTENT_IN_FILE_MSG, envFileName));
      }
    } catch (Exception exception) {
      LOGGER.error("Error while reading env file : " + envFileName, exception);
      MDC_DATA_DEBUG_MESSAGE.debugExitMessage("env file", envFileName);
      return null;
    }
    return envContent;
  }


  public static String getResourceGroupResourceName(String resourceCallingToResourceGroup) {
    return "OS::Heat::ResourceGroup in " + resourceCallingToResourceGroup;
  }

}
