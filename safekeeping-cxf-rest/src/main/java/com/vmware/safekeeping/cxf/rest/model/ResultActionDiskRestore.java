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
import com.vmware.safekeeping.cxf.rest.model.AbstractResultActionDiskVirtualOperation;
import com.vmware.safekeeping.cxf.rest.model.RestoreDiskPhases;
import com.vmware.safekeeping.cxf.rest.model.SapiTask;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * ResultActionDiskRestore
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class ResultActionDiskRestore extends AbstractResultActionDiskVirtualOperation implements OneOfTaskResultResult  {
  @JsonProperty("Phase")
  private RestoreDiskPhases phase = null;

  public ResultActionDiskRestore phase(RestoreDiskPhases phase) {
    this.phase = phase;
    return this;
  }

  /**
   * Get phase
   * @return phase
   **/
  @JsonProperty("Phase")
  @Schema(description = "")
  @Valid
  public RestoreDiskPhases getPhase() {
    return phase;
  }

  public void setPhase(RestoreDiskPhases phase) {
    this.phase = phase;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResultActionDiskRestore resultActionDiskRestore = (ResultActionDiskRestore) o;
    return Objects.equals(this.phase, resultActionDiskRestore.phase) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(phase, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResultActionDiskRestore {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    phase: ").append(toIndentedString(phase)).append("\n");
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