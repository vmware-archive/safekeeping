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
import com.vmware.safekeeping.cxf.rest.model.GeneretionDependenciesInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * ResultActionArchiveRemoveGeneration
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")public class ResultActionArchiveRemoveGeneration extends AbstractResultActionArchive implements OneOfTaskResultResult  {
  @JsonProperty("DependenciesInfo")
  private GeneretionDependenciesInfo dependenciesInfo = null;

  @JsonProperty("Dependents")
  private List<GeneretionDependenciesInfo> dependents = null;

  @JsonProperty("GenId")
  private Integer genId = null;

  @JsonProperty("TimestampMs")
  private Long timestampMs = null;

  @JsonProperty("GenerationDataRemoved")
  private Boolean generationDataRemoved = null;

  @JsonProperty("GenerationProfileMetadataRemoved")
  private Boolean generationProfileMetadataRemoved = null;

  public ResultActionArchiveRemoveGeneration dependenciesInfo(GeneretionDependenciesInfo dependenciesInfo) {
    this.dependenciesInfo = dependenciesInfo;
    return this;
  }

  /**
   * Get dependenciesInfo
   * @return dependenciesInfo
   **/
  @JsonProperty("DependenciesInfo")
  @Schema(description = "")
  @Valid
  public GeneretionDependenciesInfo getDependenciesInfo() {
    return dependenciesInfo;
  }

  public void setDependenciesInfo(GeneretionDependenciesInfo dependenciesInfo) {
    this.dependenciesInfo = dependenciesInfo;
  }

  public ResultActionArchiveRemoveGeneration dependents(List<GeneretionDependenciesInfo> dependents) {
    this.dependents = dependents;
    return this;
  }

  public ResultActionArchiveRemoveGeneration addDependentsItem(GeneretionDependenciesInfo dependentsItem) {
    if (this.dependents == null) {
      this.dependents = new ArrayList<GeneretionDependenciesInfo>();
    }
    this.dependents.add(dependentsItem);
    return this;
  }

  /**
   * Get dependents
   * @return dependents
   **/
  @JsonProperty("Dependents")
  @Schema(description = "")
  @Valid
  public List<GeneretionDependenciesInfo> getDependents() {
    return dependents;
  }

  public void setDependents(List<GeneretionDependenciesInfo> dependents) {
    this.dependents = dependents;
  }

  public ResultActionArchiveRemoveGeneration genId(Integer genId) {
    this.genId = genId;
    return this;
  }

  /**
   * Get genId
   * @return genId
   **/
  @JsonProperty("GenId")
  @Schema(description = "")
  public Integer getGenId() {
    return genId;
  }

  public void setGenId(Integer genId) {
    this.genId = genId;
  }

  public ResultActionArchiveRemoveGeneration timestampMs(Long timestampMs) {
    this.timestampMs = timestampMs;
    return this;
  }

  /**
   * Get timestampMs
   * @return timestampMs
   **/
  @JsonProperty("TimestampMs")
  @Schema(description = "")
  public Long getTimestampMs() {
    return timestampMs;
  }

  public void setTimestampMs(Long timestampMs) {
    this.timestampMs = timestampMs;
  }

  public ResultActionArchiveRemoveGeneration generationDataRemoved(Boolean generationDataRemoved) {
    this.generationDataRemoved = generationDataRemoved;
    return this;
  }

  /**
   * Get generationDataRemoved
   * @return generationDataRemoved
   **/
  @JsonProperty("GenerationDataRemoved")
  @Schema(description = "")
  public Boolean isGenerationDataRemoved() {
    return generationDataRemoved;
  }

  public void setGenerationDataRemoved(Boolean generationDataRemoved) {
    this.generationDataRemoved = generationDataRemoved;
  }

  public ResultActionArchiveRemoveGeneration generationProfileMetadataRemoved(Boolean generationProfileMetadataRemoved) {
    this.generationProfileMetadataRemoved = generationProfileMetadataRemoved;
    return this;
  }

  /**
   * Get generationProfileMetadataRemoved
   * @return generationProfileMetadataRemoved
   **/
  @JsonProperty("GenerationProfileMetadataRemoved")
  @Schema(description = "")
  public Boolean isGenerationProfileMetadataRemoved() {
    return generationProfileMetadataRemoved;
  }

  public void setGenerationProfileMetadataRemoved(Boolean generationProfileMetadataRemoved) {
    this.generationProfileMetadataRemoved = generationProfileMetadataRemoved;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResultActionArchiveRemoveGeneration resultActionArchiveRemoveGeneration = (ResultActionArchiveRemoveGeneration) o;
    return Objects.equals(this.dependenciesInfo, resultActionArchiveRemoveGeneration.dependenciesInfo) &&
        Objects.equals(this.dependents, resultActionArchiveRemoveGeneration.dependents) &&
        Objects.equals(this.genId, resultActionArchiveRemoveGeneration.genId) &&
        Objects.equals(this.timestampMs, resultActionArchiveRemoveGeneration.timestampMs) &&
        Objects.equals(this.generationDataRemoved, resultActionArchiveRemoveGeneration.generationDataRemoved) &&
        Objects.equals(this.generationProfileMetadataRemoved, resultActionArchiveRemoveGeneration.generationProfileMetadataRemoved) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dependenciesInfo, dependents, genId, timestampMs, generationDataRemoved, generationProfileMetadataRemoved, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResultActionArchiveRemoveGeneration {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    dependenciesInfo: ").append(toIndentedString(dependenciesInfo)).append("\n");
    sb.append("    dependents: ").append(toIndentedString(dependents)).append("\n");
    sb.append("    genId: ").append(toIndentedString(genId)).append("\n");
    sb.append("    timestampMs: ").append(toIndentedString(timestampMs)).append("\n");
    sb.append("    generationDataRemoved: ").append(toIndentedString(generationDataRemoved)).append("\n");
    sb.append("    generationProfileMetadataRemoved: ").append(toIndentedString(generationProfileMetadataRemoved)).append("\n");
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
