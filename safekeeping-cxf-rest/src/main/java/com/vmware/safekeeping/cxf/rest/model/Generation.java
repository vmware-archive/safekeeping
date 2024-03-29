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
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * Generation
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")public class Generation   {
  @JsonProperty("BackupMode")
  private BackupMode backupMode = null;

  @JsonProperty("DependingOnGenerationId")
  private Integer dependingOnGenerationId = null;

  @JsonProperty("GenId")
  private Integer genId = null;

  @JsonProperty("Timestamp")
  private Date timestamp = null;

  @JsonProperty("Succeeded")
  private Boolean succeeded = null;

  public Generation backupMode(BackupMode backupMode) {
    this.backupMode = backupMode;
    return this;
  }

  /**
   * Get backupMode
   * @return backupMode
   **/
  @JsonProperty("BackupMode")
  @Schema(description = "")
  @Valid
  public BackupMode getBackupMode() {
    return backupMode;
  }

  public void setBackupMode(BackupMode backupMode) {
    this.backupMode = backupMode;
  }

  public Generation dependingOnGenerationId(Integer dependingOnGenerationId) {
    this.dependingOnGenerationId = dependingOnGenerationId;
    return this;
  }

  /**
   * Get dependingOnGenerationId
   * @return dependingOnGenerationId
   **/
  @JsonProperty("DependingOnGenerationId")
  @Schema(description = "")
  public Integer getDependingOnGenerationId() {
    return dependingOnGenerationId;
  }

  public void setDependingOnGenerationId(Integer dependingOnGenerationId) {
    this.dependingOnGenerationId = dependingOnGenerationId;
  }

  public Generation genId(Integer genId) {
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

  public Generation timestamp(Date timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  /**
   * Get timestamp
   * @return timestamp
   **/
  @JsonProperty("Timestamp")
  @Schema(description = "")
  @Valid
  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public Generation succeeded(Boolean succeeded) {
    this.succeeded = succeeded;
    return this;
  }

  /**
   * Get succeeded
   * @return succeeded
   **/
  @JsonProperty("Succeeded")
  @Schema(description = "")
  public Boolean isSucceeded() {
    return succeeded;
  }

  public void setSucceeded(Boolean succeeded) {
    this.succeeded = succeeded;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Generation generation = (Generation) o;
    return Objects.equals(this.backupMode, generation.backupMode) &&
        Objects.equals(this.dependingOnGenerationId, generation.dependingOnGenerationId) &&
        Objects.equals(this.genId, generation.genId) &&
        Objects.equals(this.timestamp, generation.timestamp) &&
        Objects.equals(this.succeeded, generation.succeeded);
  }

  @Override
  public int hashCode() {
    return Objects.hash(backupMode, dependingOnGenerationId, genId, timestamp, succeeded);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Generation {\n");
    
    sb.append("    backupMode: ").append(toIndentedString(backupMode)).append("\n");
    sb.append("    dependingOnGenerationId: ").append(toIndentedString(dependingOnGenerationId)).append("\n");
    sb.append("    genId: ").append(toIndentedString(genId)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    succeeded: ").append(toIndentedString(succeeded)).append("\n");
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
