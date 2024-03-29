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
import com.vmware.safekeeping.cxf.rest.model.AbstractArchiveOptions;
import com.vmware.safekeeping.cxf.rest.model.GenerationsFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * ArchiveStatusOptions
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")public class ArchiveStatusOptions extends AbstractArchiveOptions  {
  @JsonProperty("Filter")
  private GenerationsFilter filter = null;

  @JsonProperty("GenerationId")
  private List<Integer> generationId = null;

  public ArchiveStatusOptions filter(GenerationsFilter filter) {
    this.filter = filter;
    return this;
  }

  /**
   * Get filter
   * @return filter
   **/
  @JsonProperty("Filter")
  @Schema(description = "")
  @Valid
  public GenerationsFilter getFilter() {
    return filter;
  }

  public void setFilter(GenerationsFilter filter) {
    this.filter = filter;
  }

  public ArchiveStatusOptions generationId(List<Integer> generationId) {
    this.generationId = generationId;
    return this;
  }

  public ArchiveStatusOptions addGenerationIdItem(Integer generationIdItem) {
    if (this.generationId == null) {
      this.generationId = new ArrayList<Integer>();
    }
    this.generationId.add(generationIdItem);
    return this;
  }

  /**
   * Get generationId
   * @return generationId
   **/
  @JsonProperty("GenerationId")
  @Schema(description = "")
  public List<Integer> getGenerationId() {
    return generationId;
  }

  public void setGenerationId(List<Integer> generationId) {
    this.generationId = generationId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ArchiveStatusOptions archiveStatusOptions = (ArchiveStatusOptions) o;
    return Objects.equals(this.filter, archiveStatusOptions.filter) &&
        Objects.equals(this.generationId, archiveStatusOptions.generationId) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(filter, generationId, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ArchiveStatusOptions {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    filter: ").append(toIndentedString(filter)).append("\n");
    sb.append("    generationId: ").append(toIndentedString(generationId)).append("\n");
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
