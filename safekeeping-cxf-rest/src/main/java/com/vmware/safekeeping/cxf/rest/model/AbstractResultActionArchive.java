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
import java.util.Date;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * AbstractResultActionArchive
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")public class AbstractResultActionArchive extends ResultAction  {
  @JsonProperty("TargetName")
  private String targetName = null;

  public AbstractResultActionArchive targetName(String targetName) {
    this.targetName = targetName;
    return this;
  }

  /**
   * Get targetName
   * @return targetName
   **/
  @JsonProperty("TargetName")
  @Schema(description = "")
  public String getTargetName() {
    return targetName;
  }

  public void setTargetName(String targetName) {
    this.targetName = targetName;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractResultActionArchive abstractResultActionArchive = (AbstractResultActionArchive) o;
    return Objects.equals(this.targetName, abstractResultActionArchive.targetName) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(targetName, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AbstractResultActionArchive {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    targetName: ").append(toIndentedString(targetName)).append("\n");
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
