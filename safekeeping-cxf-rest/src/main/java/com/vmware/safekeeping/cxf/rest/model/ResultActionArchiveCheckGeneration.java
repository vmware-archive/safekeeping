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
 * ResultActionArchiveCheckGeneration
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class ResultActionArchiveCheckGeneration extends AbstractResultActionArchive implements OneOfTaskResultResult  {
  @JsonProperty("DependenciesInfo")
  private GeneretionDependenciesInfo dependenciesInfo = null;

  @JsonProperty("Dependents")
  private List<GeneretionDependenciesInfo> dependents = null;

  @JsonProperty("GenId")
  private Integer genId = null;

  @JsonProperty("Md5fileCheck")
  private List<Boolean> md5fileCheck = null;

  @JsonProperty("NumOfFiles")
  private Integer numOfFiles = null;

  @JsonProperty("TimestampMs")
  private Long timestampMs = null;

  public ResultActionArchiveCheckGeneration dependenciesInfo(GeneretionDependenciesInfo dependenciesInfo) {
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

  public ResultActionArchiveCheckGeneration dependents(List<GeneretionDependenciesInfo> dependents) {
    this.dependents = dependents;
    return this;
  }

  public ResultActionArchiveCheckGeneration addDependentsItem(GeneretionDependenciesInfo dependentsItem) {
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

  public ResultActionArchiveCheckGeneration genId(Integer genId) {
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

  public ResultActionArchiveCheckGeneration md5fileCheck(List<Boolean> md5fileCheck) {
    this.md5fileCheck = md5fileCheck;
    return this;
  }

  public ResultActionArchiveCheckGeneration addMd5fileCheckItem(Boolean md5fileCheckItem) {
    if (this.md5fileCheck == null) {
      this.md5fileCheck = new ArrayList<Boolean>();
    }
    this.md5fileCheck.add(md5fileCheckItem);
    return this;
  }

  /**
   * Get md5fileCheck
   * @return md5fileCheck
   **/
  @JsonProperty("Md5fileCheck")
  @Schema(description = "")
  public List<Boolean> getMd5fileCheck() {
    return md5fileCheck;
  }

  public void setMd5fileCheck(List<Boolean> md5fileCheck) {
    this.md5fileCheck = md5fileCheck;
  }

  public ResultActionArchiveCheckGeneration numOfFiles(Integer numOfFiles) {
    this.numOfFiles = numOfFiles;
    return this;
  }

  /**
   * Get numOfFiles
   * @return numOfFiles
   **/
  @JsonProperty("NumOfFiles")
  @Schema(description = "")
  public Integer getNumOfFiles() {
    return numOfFiles;
  }

  public void setNumOfFiles(Integer numOfFiles) {
    this.numOfFiles = numOfFiles;
  }

  public ResultActionArchiveCheckGeneration timestampMs(Long timestampMs) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResultActionArchiveCheckGeneration resultActionArchiveCheckGeneration = (ResultActionArchiveCheckGeneration) o;
    return Objects.equals(this.dependenciesInfo, resultActionArchiveCheckGeneration.dependenciesInfo) &&
        Objects.equals(this.dependents, resultActionArchiveCheckGeneration.dependents) &&
        Objects.equals(this.genId, resultActionArchiveCheckGeneration.genId) &&
        Objects.equals(this.md5fileCheck, resultActionArchiveCheckGeneration.md5fileCheck) &&
        Objects.equals(this.numOfFiles, resultActionArchiveCheckGeneration.numOfFiles) &&
        Objects.equals(this.timestampMs, resultActionArchiveCheckGeneration.timestampMs) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dependenciesInfo, dependents, genId, md5fileCheck, numOfFiles, timestampMs, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResultActionArchiveCheckGeneration {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    dependenciesInfo: ").append(toIndentedString(dependenciesInfo)).append("\n");
    sb.append("    dependents: ").append(toIndentedString(dependents)).append("\n");
    sb.append("    genId: ").append(toIndentedString(genId)).append("\n");
    sb.append("    md5fileCheck: ").append(toIndentedString(md5fileCheck)).append("\n");
    sb.append("    numOfFiles: ").append(toIndentedString(numOfFiles)).append("\n");
    sb.append("    timestampMs: ").append(toIndentedString(timestampMs)).append("\n");
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