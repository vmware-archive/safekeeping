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
import com.vmware.safekeeping.cxf.rest.model.ManagedFcoEntityInfo;
import com.vmware.safekeeping.cxf.rest.model.OperationState;
import com.vmware.safekeeping.cxf.rest.model.SapiTask;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * ResultAction
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class ResultAction   {
  @JsonProperty("CreationDate")
  private Date creationDate = null;

  @JsonProperty("EndDate")
  private Date endDate = null;

  @JsonProperty("EndTime")
  private Long endTime = null;

  @JsonProperty("FcoEntityInfo")
  private ManagedFcoEntityInfo fcoEntityInfo = null;

  @JsonProperty("Parent")
  private SapiTask parent = null;

  @JsonProperty("Progress")
  private Integer progress = null;

  @JsonProperty("Reason")
  private String reason = null;

  @JsonProperty("StartDate")
  private Date startDate = null;

  @JsonProperty("StartTime")
  private Long startTime = null;

  @JsonProperty("State")
  private OperationState state = null;

  @JsonProperty("Task")
  private SapiTask task = null;

  @JsonProperty("Done")
  private Boolean done = null;

  public ResultAction creationDate(Date creationDate) {
    this.creationDate = creationDate;
    return this;
  }

  /**
   * Get creationDate
   * @return creationDate
   **/
  @JsonProperty("CreationDate")
  @Schema(description = "")
  @Valid
  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public ResultAction endDate(Date endDate) {
    this.endDate = endDate;
    return this;
  }

  /**
   * Get endDate
   * @return endDate
   **/
  @JsonProperty("EndDate")
  @Schema(description = "")
  @Valid
  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public ResultAction endTime(Long endTime) {
    this.endTime = endTime;
    return this;
  }

  /**
   * Get endTime
   * @return endTime
   **/
  @JsonProperty("EndTime")
  @Schema(description = "")
  public Long getEndTime() {
    return endTime;
  }

  public void setEndTime(Long endTime) {
    this.endTime = endTime;
  }

  public ResultAction fcoEntityInfo(ManagedFcoEntityInfo fcoEntityInfo) {
    this.fcoEntityInfo = fcoEntityInfo;
    return this;
  }

  /**
   * Get fcoEntityInfo
   * @return fcoEntityInfo
   **/
  @JsonProperty("FcoEntityInfo")
  @Schema(description = "")
  @Valid
  public ManagedFcoEntityInfo getFcoEntityInfo() {
    return fcoEntityInfo;
  }

  public void setFcoEntityInfo(ManagedFcoEntityInfo fcoEntityInfo) {
    this.fcoEntityInfo = fcoEntityInfo;
  }

  public ResultAction parent(SapiTask parent) {
    this.parent = parent;
    return this;
  }

  /**
   * Get parent
   * @return parent
   **/
  @JsonProperty("Parent")
  @Schema(description = "")
  @Valid
  public SapiTask getParent() {
    return parent;
  }

  public void setParent(SapiTask parent) {
    this.parent = parent;
  }

  public ResultAction progress(Integer progress) {
    this.progress = progress;
    return this;
  }

  /**
   * Get progress
   * @return progress
   **/
  @JsonProperty("Progress")
  @Schema(description = "")
  public Integer getProgress() {
    return progress;
  }

  public void setProgress(Integer progress) {
    this.progress = progress;
  }

  public ResultAction reason(String reason) {
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

  public ResultAction startDate(Date startDate) {
    this.startDate = startDate;
    return this;
  }

  /**
   * Get startDate
   * @return startDate
   **/
  @JsonProperty("StartDate")
  @Schema(description = "")
  @Valid
  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public ResultAction startTime(Long startTime) {
    this.startTime = startTime;
    return this;
  }

  /**
   * Get startTime
   * @return startTime
   **/
  @JsonProperty("StartTime")
  @Schema(description = "")
  public Long getStartTime() {
    return startTime;
  }

  public void setStartTime(Long startTime) {
    this.startTime = startTime;
  }

  public ResultAction state(OperationState state) {
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

  public ResultAction task(SapiTask task) {
    this.task = task;
    return this;
  }

  /**
   * Get task
   * @return task
   **/
  @JsonProperty("Task")
  @Schema(description = "")
  @Valid
  public SapiTask getTask() {
    return task;
  }

  public void setTask(SapiTask task) {
    this.task = task;
  }

  public ResultAction done(Boolean done) {
    this.done = done;
    return this;
  }

  /**
   * Get done
   * @return done
   **/
  @JsonProperty("Done")
  @Schema(description = "")
  public Boolean isDone() {
    return done;
  }

  public void setDone(Boolean done) {
    this.done = done;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResultAction resultAction = (ResultAction) o;
    return Objects.equals(this.creationDate, resultAction.creationDate) &&
        Objects.equals(this.endDate, resultAction.endDate) &&
        Objects.equals(this.endTime, resultAction.endTime) &&
        Objects.equals(this.fcoEntityInfo, resultAction.fcoEntityInfo) &&
        Objects.equals(this.parent, resultAction.parent) &&
        Objects.equals(this.progress, resultAction.progress) &&
        Objects.equals(this.reason, resultAction.reason) &&
        Objects.equals(this.startDate, resultAction.startDate) &&
        Objects.equals(this.startTime, resultAction.startTime) &&
        Objects.equals(this.state, resultAction.state) &&
        Objects.equals(this.task, resultAction.task) &&
        Objects.equals(this.done, resultAction.done);
  }

  @Override
  public int hashCode() {
    return Objects.hash(creationDate, endDate, endTime, fcoEntityInfo, parent, progress, reason, startDate, startTime, state, task, done);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResultAction {\n");
    
    sb.append("    creationDate: ").append(toIndentedString(creationDate)).append("\n");
    sb.append("    endDate: ").append(toIndentedString(endDate)).append("\n");
    sb.append("    endTime: ").append(toIndentedString(endTime)).append("\n");
    sb.append("    fcoEntityInfo: ").append(toIndentedString(fcoEntityInfo)).append("\n");
    sb.append("    parent: ").append(toIndentedString(parent)).append("\n");
    sb.append("    progress: ").append(toIndentedString(progress)).append("\n");
    sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
    sb.append("    startDate: ").append(toIndentedString(startDate)).append("\n");
    sb.append("    startTime: ").append(toIndentedString(startTime)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    task: ").append(toIndentedString(task)).append("\n");
    sb.append("    done: ").append(toIndentedString(done)).append("\n");
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