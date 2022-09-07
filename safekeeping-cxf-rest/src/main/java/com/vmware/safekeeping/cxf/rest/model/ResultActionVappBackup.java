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
import com.vmware.safekeeping.cxf.rest.model.BackupMode;
import com.vmware.safekeeping.cxf.rest.model.ManagedFcoEntityInfo;
import com.vmware.safekeeping.cxf.rest.model.ResultActionBackup;
import com.vmware.safekeeping.cxf.rest.model.SapiTask;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * ResultActionVappBackup
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")
public class ResultActionVappBackup extends ResultActionBackup implements OneOfTaskResultResult  {
  @JsonProperty("FcoChildren")
  private List<ManagedFcoEntityInfo> fcoChildren = null;

  @JsonProperty("NumberOfChildVm")
  private Integer numberOfChildVm = null;

  @JsonProperty("ResultActionOnChildVms")
  private List<SapiTask> resultActionOnChildVms = null;

  public ResultActionVappBackup fcoChildren(List<ManagedFcoEntityInfo> fcoChildren) {
    this.fcoChildren = fcoChildren;
    return this;
  }

  public ResultActionVappBackup addFcoChildrenItem(ManagedFcoEntityInfo fcoChildrenItem) {
    if (this.fcoChildren == null) {
      this.fcoChildren = new ArrayList<ManagedFcoEntityInfo>();
    }
    this.fcoChildren.add(fcoChildrenItem);
    return this;
  }

  /**
   * Get fcoChildren
   * @return fcoChildren
   **/
  @JsonProperty("FcoChildren")
  @Schema(description = "")
  @Valid
  public List<ManagedFcoEntityInfo> getFcoChildren() {
    return fcoChildren;
  }

  public void setFcoChildren(List<ManagedFcoEntityInfo> fcoChildren) {
    this.fcoChildren = fcoChildren;
  }

  public ResultActionVappBackup numberOfChildVm(Integer numberOfChildVm) {
    this.numberOfChildVm = numberOfChildVm;
    return this;
  }

  /**
   * Get numberOfChildVm
   * @return numberOfChildVm
   **/
  @JsonProperty("NumberOfChildVm")
  @Schema(description = "")
  public Integer getNumberOfChildVm() {
    return numberOfChildVm;
  }

  public void setNumberOfChildVm(Integer numberOfChildVm) {
    this.numberOfChildVm = numberOfChildVm;
  }

  public ResultActionVappBackup resultActionOnChildVms(List<SapiTask> resultActionOnChildVms) {
    this.resultActionOnChildVms = resultActionOnChildVms;
    return this;
  }

  public ResultActionVappBackup addResultActionOnChildVmsItem(SapiTask resultActionOnChildVmsItem) {
    if (this.resultActionOnChildVms == null) {
      this.resultActionOnChildVms = new ArrayList<SapiTask>();
    }
    this.resultActionOnChildVms.add(resultActionOnChildVmsItem);
    return this;
  }

  /**
   * Get resultActionOnChildVms
   * @return resultActionOnChildVms
   **/
  @JsonProperty("ResultActionOnChildVms")
  @Schema(description = "")
  @Valid
  public List<SapiTask> getResultActionOnChildVms() {
    return resultActionOnChildVms;
  }

  public void setResultActionOnChildVms(List<SapiTask> resultActionOnChildVms) {
    this.resultActionOnChildVms = resultActionOnChildVms;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResultActionVappBackup resultActionVappBackup = (ResultActionVappBackup) o;
    return Objects.equals(this.fcoChildren, resultActionVappBackup.fcoChildren) &&
        Objects.equals(this.numberOfChildVm, resultActionVappBackup.numberOfChildVm) &&
        Objects.equals(this.resultActionOnChildVms, resultActionVappBackup.resultActionOnChildVms) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(fcoChildren, numberOfChildVm, resultActionOnChildVms, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResultActionVappBackup {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    fcoChildren: ").append(toIndentedString(fcoChildren)).append("\n");
    sb.append("    numberOfChildVm: ").append(toIndentedString(numberOfChildVm)).append("\n");
    sb.append("    resultActionOnChildVms: ").append(toIndentedString(resultActionOnChildVms)).append("\n");
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
