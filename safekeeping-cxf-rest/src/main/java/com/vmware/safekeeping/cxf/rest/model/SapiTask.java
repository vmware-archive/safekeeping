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
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * SapiTask
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")public class SapiTask   {
  @JsonProperty("FcoEntity")
  private ManagedFcoEntityInfo fcoEntity = null;

  @JsonProperty("Id")
  private String id = null;

  @JsonProperty("Reason")
  private String reason = null;

  @JsonProperty("State")
  private OperationState state = null;

  public SapiTask fcoEntity(ManagedFcoEntityInfo fcoEntity) {
    this.fcoEntity = fcoEntity;
    return this;
  }

  /**
   * Get fcoEntity
   * @return fcoEntity
   **/
  @JsonProperty("FcoEntity")
  @Schema(description = "")
  @Valid
  public ManagedFcoEntityInfo getFcoEntity() {
    return fcoEntity;
  }

  public void setFcoEntity(ManagedFcoEntityInfo fcoEntity) {
    this.fcoEntity = fcoEntity;
  }

  public SapiTask id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
   **/
  @JsonProperty("Id")
  @Schema(description = "")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public SapiTask reason(String reason) {
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

  public SapiTask state(OperationState state) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SapiTask sapiTask = (SapiTask) o;
    return Objects.equals(this.fcoEntity, sapiTask.fcoEntity) &&
        Objects.equals(this.id, sapiTask.id) &&
        Objects.equals(this.reason, sapiTask.reason) &&
        Objects.equals(this.state, sapiTask.state);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fcoEntity, id, reason, state);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SapiTask {\n");
    
    sb.append("    fcoEntity: ").append(toIndentedString(fcoEntity)).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
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
