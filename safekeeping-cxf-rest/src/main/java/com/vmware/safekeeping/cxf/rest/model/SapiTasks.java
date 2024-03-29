/*
 * Safekeeping Server
 * Safekeeping OpenAPI
 *
 * OpenAPI spec version: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package com.vmware.safekeeping.cxf.rest.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.vmware.safekeeping.cxf.rest.model.OperationState;
import com.vmware.safekeeping.cxf.rest.model.SapiTask;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * SapiTasks
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")
public class SapiTasks   {
  @JsonProperty("Reason")
  private String reason = null;

  @JsonProperty("State")
  private OperationState state = null;

  @JsonProperty("TaskList")
  private List<SapiTask> taskList = null;

  public SapiTasks reason(String reason) {
    this.reason = reason;
    return this;
  }

  /**
   * Get reason
   * @return reason
   **/
  @JsonProperty("Reason")
  @Schema(description = "")
  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public SapiTasks state(OperationState state) {
    this.state = state;
    return this;
  }

  /**
   * Get state
   * @return state
   **/
  @JsonProperty("State")
  @Schema(description = "")
  @Valid
  public OperationState getState() {
    return state;
  }

  public void setState(OperationState state) {
    this.state = state;
  }

  public SapiTasks taskList(List<SapiTask> taskList) {
    this.taskList = taskList;
    return this;
  }

  public SapiTasks addTaskListItem(SapiTask taskListItem) {
    if (this.taskList == null) {
      this.taskList = new ArrayList<SapiTask>();
    }
    this.taskList.add(taskListItem);
    return this;
  }

  /**
   * Get taskList
   * @return taskList
   **/
  @JsonProperty("TaskList")
  @Schema(description = "")
  @Valid
  public List<SapiTask> getTaskList() {
    return taskList;
  }

  public void setTaskList(List<SapiTask> taskList) {
    this.taskList = taskList;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SapiTasks sapiTasks = (SapiTasks) o;
    return Objects.equals(this.reason, sapiTasks.reason) &&
        Objects.equals(this.state, sapiTasks.state) &&
        Objects.equals(this.taskList, sapiTasks.taskList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(reason, state, taskList);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SapiTasks {\n");
    
    sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    taskList: ").append(toIndentedString(taskList)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
