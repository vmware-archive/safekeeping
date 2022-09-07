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
import com.vmware.safekeeping.cxf.rest.model.ResultAction;
import com.vmware.safekeeping.cxf.rest.model.SapiTask;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * ResultActionConnect
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")public class ResultActionConnect extends ResultAction implements OneOfTaskResultResult  {
  @JsonProperty("Connect")
  private Boolean connect = null;

  @JsonProperty("ssoEndPointUrl")
  private String ssoEndPointUrl = null;

  @JsonProperty("subTasksActionConnectVCenters")
  private List<SapiTask> subTasksActionConnectVCenters = null;

  public ResultActionConnect connect(Boolean connect) {
    this.connect = connect;
    return this;
  }

  /**
   * Get connect
   * @return connect
   **/
  @JsonProperty("Connect")
  @Schema(description = "")
  public Boolean isConnect() {
    return connect;
  }

  public void setConnect(Boolean connect) {
    this.connect = connect;
  }

  public ResultActionConnect ssoEndPointUrl(String ssoEndPointUrl) {
    this.ssoEndPointUrl = ssoEndPointUrl;
    return this;
  }

  /**
   * Get ssoEndPointUrl
   * @return ssoEndPointUrl
   **/
  @JsonProperty("ssoEndPointUrl")
  @Schema(description = "")
  public String getSsoEndPointUrl() {
    return ssoEndPointUrl;
  }

  public void setSsoEndPointUrl(String ssoEndPointUrl) {
    this.ssoEndPointUrl = ssoEndPointUrl;
  }

  public ResultActionConnect subTasksActionConnectVCenters(List<SapiTask> subTasksActionConnectVCenters) {
    this.subTasksActionConnectVCenters = subTasksActionConnectVCenters;
    return this;
  }

  public ResultActionConnect addSubTasksActionConnectVCentersItem(SapiTask subTasksActionConnectVCentersItem) {
    if (this.subTasksActionConnectVCenters == null) {
      this.subTasksActionConnectVCenters = new ArrayList<SapiTask>();
    }
    this.subTasksActionConnectVCenters.add(subTasksActionConnectVCentersItem);
    return this;
  }

  /**
   * Get subTasksActionConnectVCenters
   * @return subTasksActionConnectVCenters
   **/
  @JsonProperty("subTasksActionConnectVCenters")
  @Schema(description = "")
  @Valid
  public List<SapiTask> getSubTasksActionConnectVCenters() {
    return subTasksActionConnectVCenters;
  }

  public void setSubTasksActionConnectVCenters(List<SapiTask> subTasksActionConnectVCenters) {
    this.subTasksActionConnectVCenters = subTasksActionConnectVCenters;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResultActionConnect resultActionConnect = (ResultActionConnect) o;
    return Objects.equals(this.connect, resultActionConnect.connect) &&
        Objects.equals(this.ssoEndPointUrl, resultActionConnect.ssoEndPointUrl) &&
        Objects.equals(this.subTasksActionConnectVCenters, resultActionConnect.subTasksActionConnectVCenters) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(connect, ssoEndPointUrl, subTasksActionConnectVCenters, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResultActionConnect {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    connect: ").append(toIndentedString(connect)).append("\n");
    sb.append("    ssoEndPointUrl: ").append(toIndentedString(ssoEndPointUrl)).append("\n");
    sb.append("    subTasksActionConnectVCenters: ").append(toIndentedString(subTasksActionConnectVCenters)).append("\n");
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