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
import com.vmware.safekeeping.cxf.rest.model.ArchiveObjects;
import com.vmware.safekeeping.cxf.rest.model.GenerationsFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * ArchiveShowOptions
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")public class ArchiveShowOptions extends AbstractArchiveOptions  {
  @JsonProperty("ArchiveObject")
  private ArchiveObjects archiveObject = null;

  @JsonProperty("Filter")
  private GenerationsFilter filter = null;

  @JsonProperty("GenerationId")
  private Integer generationId = null;

  @JsonProperty("PrettyJason")
  private Boolean prettyJason = null;

  public ArchiveShowOptions archiveObject(ArchiveObjects archiveObject) {
    this.archiveObject = archiveObject;
    return this;
  }

  /**
   * Get archiveObject
   * @return archiveObject
   **/
  @JsonProperty("ArchiveObject")
  @Schema(description = "")
  @Valid
  public ArchiveObjects getArchiveObject() {
    return archiveObject;
  }

  public void setArchiveObject(ArchiveObjects archiveObject) {
    this.archiveObject = archiveObject;
  }

  public ArchiveShowOptions filter(GenerationsFilter filter) {
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

  public ArchiveShowOptions generationId(Integer generationId) {
    this.generationId = generationId;
    return this;
  }

  /**
   * Get generationId
   * @return generationId
   **/
  @JsonProperty("GenerationId")
  @Schema(description = "")
  public Integer getGenerationId() {
    return generationId;
  }

  public void setGenerationId(Integer generationId) {
    this.generationId = generationId;
  }

  public ArchiveShowOptions prettyJason(Boolean prettyJason) {
    this.prettyJason = prettyJason;
    return this;
  }

  /**
   * Get prettyJason
   * @return prettyJason
   **/
  @JsonProperty("PrettyJason")
  @Schema(description = "")
  public Boolean isPrettyJason() {
    return prettyJason;
  }

  public void setPrettyJason(Boolean prettyJason) {
    this.prettyJason = prettyJason;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ArchiveShowOptions archiveShowOptions = (ArchiveShowOptions) o;
    return Objects.equals(this.archiveObject, archiveShowOptions.archiveObject) &&
        Objects.equals(this.filter, archiveShowOptions.filter) &&
        Objects.equals(this.generationId, archiveShowOptions.generationId) &&
        Objects.equals(this.prettyJason, archiveShowOptions.prettyJason) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(archiveObject, filter, generationId, prettyJason, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ArchiveShowOptions {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    archiveObject: ").append(toIndentedString(archiveObject)).append("\n");
    sb.append("    filter: ").append(toIndentedString(filter)).append("\n");
    sb.append("    generationId: ").append(toIndentedString(generationId)).append("\n");
    sb.append("    prettyJason: ").append(toIndentedString(prettyJason)).append("\n");
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
