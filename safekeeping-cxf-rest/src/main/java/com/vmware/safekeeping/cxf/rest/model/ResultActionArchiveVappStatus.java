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
import com.vmware.safekeeping.cxf.rest.model.GenerationVirtualMachinesInfoList;
import com.vmware.safekeeping.cxf.rest.model.ResultActionArchiveStatus;
import com.vmware.safekeeping.cxf.rest.model.StatusProfilePhases;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * ResultActionArchiveVappStatus
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")public class ResultActionArchiveVappStatus extends ResultActionArchiveStatus implements OneOfTaskResultResult  {
  @JsonProperty("GenerationVmInfoList")
  private List<GenerationVirtualMachinesInfoList> generationVmInfoList = null;

  public ResultActionArchiveVappStatus generationVmInfoList(List<GenerationVirtualMachinesInfoList> generationVmInfoList) {
    this.generationVmInfoList = generationVmInfoList;
    return this;
  }

  public ResultActionArchiveVappStatus addGenerationVmInfoListItem(GenerationVirtualMachinesInfoList generationVmInfoListItem) {
    if (this.generationVmInfoList == null) {
      this.generationVmInfoList = new ArrayList<GenerationVirtualMachinesInfoList>();
    }
    this.generationVmInfoList.add(generationVmInfoListItem);
    return this;
  }

  /**
   * Get generationVmInfoList
   * @return generationVmInfoList
   **/
  @JsonProperty("GenerationVmInfoList")
  @Schema(description = "")
  @Valid
  public List<GenerationVirtualMachinesInfoList> getGenerationVmInfoList() {
    return generationVmInfoList;
  }

  public void setGenerationVmInfoList(List<GenerationVirtualMachinesInfoList> generationVmInfoList) {
    this.generationVmInfoList = generationVmInfoList;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResultActionArchiveVappStatus resultActionArchiveVappStatus = (ResultActionArchiveVappStatus) o;
    return Objects.equals(this.generationVmInfoList, resultActionArchiveVappStatus.generationVmInfoList) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(generationVmInfoList, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResultActionArchiveVappStatus {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    generationVmInfoList: ").append(toIndentedString(generationVmInfoList)).append("\n");
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
