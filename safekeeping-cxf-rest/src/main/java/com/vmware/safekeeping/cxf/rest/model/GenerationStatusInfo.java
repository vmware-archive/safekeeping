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
import com.vmware.safekeeping.cxf.rest.model.EntityType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * GenerationStatusInfo
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")public class GenerationStatusInfo   {
  @JsonProperty("EntityType")
  private EntityType entityType = null;

  @JsonProperty("GenId")
  private Integer genId = null;

  @JsonProperty("TimeStamp")
  private Date timeStamp = null;

  @JsonProperty("GenerationSucceeded")
  private Boolean generationSucceeded = null;

  public GenerationStatusInfo entityType(EntityType entityType) {
    this.entityType = entityType;
    return this;
  }

  /**
   * Get entityType
   * @return entityType
   **/
  @JsonProperty("EntityType")
  @Schema(description = "")
  @Valid
  public EntityType getEntityType() {
    return entityType;
  }

  public void setEntityType(EntityType entityType) {
    this.entityType = entityType;
  }

  public GenerationStatusInfo genId(Integer genId) {
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

  public GenerationStatusInfo timeStamp(Date timeStamp) {
    this.timeStamp = timeStamp;
    return this;
  }

  /**
   * Get timeStamp
   * @return timeStamp
   **/
  @JsonProperty("TimeStamp")
  @Schema(description = "")
  @Valid
  public Date getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(Date timeStamp) {
    this.timeStamp = timeStamp;
  }

  public GenerationStatusInfo generationSucceeded(Boolean generationSucceeded) {
    this.generationSucceeded = generationSucceeded;
    return this;
  }

  /**
   * Get generationSucceeded
   * @return generationSucceeded
   **/
  @JsonProperty("GenerationSucceeded")
  @Schema(description = "")
  public Boolean isGenerationSucceeded() {
    return generationSucceeded;
  }

  public void setGenerationSucceeded(Boolean generationSucceeded) {
    this.generationSucceeded = generationSucceeded;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GenerationStatusInfo generationStatusInfo = (GenerationStatusInfo) o;
    return Objects.equals(this.entityType, generationStatusInfo.entityType) &&
        Objects.equals(this.genId, generationStatusInfo.genId) &&
        Objects.equals(this.timeStamp, generationStatusInfo.timeStamp) &&
        Objects.equals(this.generationSucceeded, generationStatusInfo.generationSucceeded);
  }

  @Override
  public int hashCode() {
    return Objects.hash(entityType, genId, timeStamp, generationSucceeded);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GenerationStatusInfo {\n");
    
    sb.append("    entityType: ").append(toIndentedString(entityType)).append("\n");
    sb.append("    genId: ").append(toIndentedString(genId)).append("\n");
    sb.append("    timeStamp: ").append(toIndentedString(timeStamp)).append("\n");
    sb.append("    generationSucceeded: ").append(toIndentedString(generationSucceeded)).append("\n");
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
