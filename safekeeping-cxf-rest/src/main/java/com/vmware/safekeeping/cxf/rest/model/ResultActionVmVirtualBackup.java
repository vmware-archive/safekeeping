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
import com.vmware.safekeeping.cxf.rest.model.AbstractResultActionVirtualBackupForEntityWithDisks;
import com.vmware.safekeeping.cxf.rest.model.GuestInfoFlags;
import com.vmware.safekeeping.cxf.rest.model.SapiTask;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * ResultActionVmVirtualBackup
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class ResultActionVmVirtualBackup extends AbstractResultActionVirtualBackupForEntityWithDisks implements OneOfTaskResultResult  {
  @JsonProperty("GuestFlags")
  private GuestInfoFlags guestFlags = null;

  @JsonProperty("ResultActionsOnDisk")
  private List<SapiTask> resultActionsOnDisk = null;

  @JsonProperty("Template")
  private Boolean template = null;

  public ResultActionVmVirtualBackup guestFlags(GuestInfoFlags guestFlags) {
    this.guestFlags = guestFlags;
    return this;
  }

  /**
   * Get guestFlags
   * @return guestFlags
   **/
  @JsonProperty("GuestFlags")
  @Schema(description = "")
  @Valid
  public GuestInfoFlags getGuestFlags() {
    return guestFlags;
  }

  public void setGuestFlags(GuestInfoFlags guestFlags) {
    this.guestFlags = guestFlags;
  }

  public ResultActionVmVirtualBackup resultActionsOnDisk(List<SapiTask> resultActionsOnDisk) {
    this.resultActionsOnDisk = resultActionsOnDisk;
    return this;
  }

  public ResultActionVmVirtualBackup addResultActionsOnDiskItem(SapiTask resultActionsOnDiskItem) {
    if (this.resultActionsOnDisk == null) {
      this.resultActionsOnDisk = new ArrayList<SapiTask>();
    }
    this.resultActionsOnDisk.add(resultActionsOnDiskItem);
    return this;
  }

  /**
   * Get resultActionsOnDisk
   * @return resultActionsOnDisk
   **/
  @JsonProperty("ResultActionsOnDisk")
  @Schema(description = "")
  @Valid
  public List<SapiTask> getResultActionsOnDisk() {
    return resultActionsOnDisk;
  }

  public void setResultActionsOnDisk(List<SapiTask> resultActionsOnDisk) {
    this.resultActionsOnDisk = resultActionsOnDisk;
  }

  public ResultActionVmVirtualBackup template(Boolean template) {
    this.template = template;
    return this;
  }

  /**
   * Get template
   * @return template
   **/
  @JsonProperty("Template")
  @Schema(description = "")
  public Boolean isTemplate() {
    return template;
  }

  public void setTemplate(Boolean template) {
    this.template = template;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResultActionVmVirtualBackup resultActionVmVirtualBackup = (ResultActionVmVirtualBackup) o;
    return Objects.equals(this.guestFlags, resultActionVmVirtualBackup.guestFlags) &&
        Objects.equals(this.resultActionsOnDisk, resultActionVmVirtualBackup.resultActionsOnDisk) &&
        Objects.equals(this.template, resultActionVmVirtualBackup.template) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(guestFlags, resultActionsOnDisk, template, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResultActionVmVirtualBackup {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    guestFlags: ").append(toIndentedString(guestFlags)).append("\n");
    sb.append("    resultActionsOnDisk: ").append(toIndentedString(resultActionsOnDisk)).append("\n");
    sb.append("    template: ").append(toIndentedString(template)).append("\n");
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