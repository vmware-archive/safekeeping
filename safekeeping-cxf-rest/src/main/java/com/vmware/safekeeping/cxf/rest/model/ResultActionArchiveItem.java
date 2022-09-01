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
import com.vmware.safekeeping.cxf.rest.model.AbstractResultActionArchive;
import com.vmware.safekeeping.cxf.rest.model.InfoData;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * ResultActionArchiveItem
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class ResultActionArchiveItem extends AbstractResultActionArchive implements OneOfTaskResultResult  {
  @JsonProperty("Info")
  private InfoData info = null;

  public ResultActionArchiveItem info(InfoData info) {
    this.info = info;
    return this;
  }

  /**
   * Get info
   * @return info
   **/
  @JsonProperty("Info")
  @Schema(description = "")
  @Valid
  public InfoData getInfo() {
    return info;
  }

  public void setInfo(InfoData info) {
    this.info = info;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResultActionArchiveItem resultActionArchiveItem = (ResultActionArchiveItem) o;
    return Objects.equals(this.info, resultActionArchiveItem.info) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(info, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResultActionArchiveItem {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    info: ").append(toIndentedString(info)).append("\n");
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