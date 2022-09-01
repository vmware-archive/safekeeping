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
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * InfoData
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class InfoData   {
  @JsonProperty("EntityType")
  private EntityType entityType = null;

  @JsonProperty("LatestGenerationId")
  private Integer latestGenerationId = null;

  @JsonProperty("LatestSucceededGenerationId")
  private Integer latestSucceededGenerationId = null;

  @JsonProperty("Moref")
  private String moref = null;

  @JsonProperty("Name")
  private String name = null;

  @JsonProperty("TimestampMsOfLatestGenerationId")
  private Long timestampMsOfLatestGenerationId = null;

  @JsonProperty("TimestampMsOfLatestSucceededGenerationId")
  private Long timestampMsOfLatestSucceededGenerationId = null;

  @JsonProperty("TimestampOfLatestGenerationId")
  private String timestampOfLatestGenerationId = null;

  @JsonProperty("TimestampOfLatestSucceededGenerationId")
  private String timestampOfLatestSucceededGenerationId = null;

  @JsonProperty("Uuid")
  private String uuid = null;

  @JsonProperty("Full")
  private Boolean full = null;

  public InfoData entityType(EntityType entityType) {
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

  public InfoData latestGenerationId(Integer latestGenerationId) {
    this.latestGenerationId = latestGenerationId;
    return this;
  }

  /**
   * Get latestGenerationId
   * @return latestGenerationId
   **/
  @JsonProperty("LatestGenerationId")
  @Schema(description = "")
  public Integer getLatestGenerationId() {
    return latestGenerationId;
  }

  public void setLatestGenerationId(Integer latestGenerationId) {
    this.latestGenerationId = latestGenerationId;
  }

  public InfoData latestSucceededGenerationId(Integer latestSucceededGenerationId) {
    this.latestSucceededGenerationId = latestSucceededGenerationId;
    return this;
  }

  /**
   * Get latestSucceededGenerationId
   * @return latestSucceededGenerationId
   **/
  @JsonProperty("LatestSucceededGenerationId")
  @Schema(description = "")
  public Integer getLatestSucceededGenerationId() {
    return latestSucceededGenerationId;
  }

  public void setLatestSucceededGenerationId(Integer latestSucceededGenerationId) {
    this.latestSucceededGenerationId = latestSucceededGenerationId;
  }

  public InfoData moref(String moref) {
    this.moref = moref;
    return this;
  }

  /**
   * Get moref
   * @return moref
   **/
  @JsonProperty("Moref")
  @Schema(description = "")
  public String getMoref() {
    return moref;
  }

  public void setMoref(String moref) {
    this.moref = moref;
  }

  public InfoData name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
   **/
  @JsonProperty("Name")
  @Schema(description = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public InfoData timestampMsOfLatestGenerationId(Long timestampMsOfLatestGenerationId) {
    this.timestampMsOfLatestGenerationId = timestampMsOfLatestGenerationId;
    return this;
  }

  /**
   * Get timestampMsOfLatestGenerationId
   * @return timestampMsOfLatestGenerationId
   **/
  @JsonProperty("TimestampMsOfLatestGenerationId")
  @Schema(description = "")
  public Long getTimestampMsOfLatestGenerationId() {
    return timestampMsOfLatestGenerationId;
  }

  public void setTimestampMsOfLatestGenerationId(Long timestampMsOfLatestGenerationId) {
    this.timestampMsOfLatestGenerationId = timestampMsOfLatestGenerationId;
  }

  public InfoData timestampMsOfLatestSucceededGenerationId(Long timestampMsOfLatestSucceededGenerationId) {
    this.timestampMsOfLatestSucceededGenerationId = timestampMsOfLatestSucceededGenerationId;
    return this;
  }

  /**
   * Get timestampMsOfLatestSucceededGenerationId
   * @return timestampMsOfLatestSucceededGenerationId
   **/
  @JsonProperty("TimestampMsOfLatestSucceededGenerationId")
  @Schema(description = "")
  public Long getTimestampMsOfLatestSucceededGenerationId() {
    return timestampMsOfLatestSucceededGenerationId;
  }

  public void setTimestampMsOfLatestSucceededGenerationId(Long timestampMsOfLatestSucceededGenerationId) {
    this.timestampMsOfLatestSucceededGenerationId = timestampMsOfLatestSucceededGenerationId;
  }

  public InfoData timestampOfLatestGenerationId(String timestampOfLatestGenerationId) {
    this.timestampOfLatestGenerationId = timestampOfLatestGenerationId;
    return this;
  }

  /**
   * Get timestampOfLatestGenerationId
   * @return timestampOfLatestGenerationId
   **/
  @JsonProperty("TimestampOfLatestGenerationId")
  @Schema(description = "")
  public String getTimestampOfLatestGenerationId() {
    return timestampOfLatestGenerationId;
  }

  public void setTimestampOfLatestGenerationId(String timestampOfLatestGenerationId) {
    this.timestampOfLatestGenerationId = timestampOfLatestGenerationId;
  }

  public InfoData timestampOfLatestSucceededGenerationId(String timestampOfLatestSucceededGenerationId) {
    this.timestampOfLatestSucceededGenerationId = timestampOfLatestSucceededGenerationId;
    return this;
  }

  /**
   * Get timestampOfLatestSucceededGenerationId
   * @return timestampOfLatestSucceededGenerationId
   **/
  @JsonProperty("TimestampOfLatestSucceededGenerationId")
  @Schema(description = "")
  public String getTimestampOfLatestSucceededGenerationId() {
    return timestampOfLatestSucceededGenerationId;
  }

  public void setTimestampOfLatestSucceededGenerationId(String timestampOfLatestSucceededGenerationId) {
    this.timestampOfLatestSucceededGenerationId = timestampOfLatestSucceededGenerationId;
  }

  public InfoData uuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /**
   * Get uuid
   * @return uuid
   **/
  @JsonProperty("Uuid")
  @Schema(description = "")
  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public InfoData full(Boolean full) {
    this.full = full;
    return this;
  }

  /**
   * Get full
   * @return full
   **/
  @JsonProperty("Full")
  @Schema(description = "")
  public Boolean isFull() {
    return full;
  }

  public void setFull(Boolean full) {
    this.full = full;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InfoData infoData = (InfoData) o;
    return Objects.equals(this.entityType, infoData.entityType) &&
        Objects.equals(this.latestGenerationId, infoData.latestGenerationId) &&
        Objects.equals(this.latestSucceededGenerationId, infoData.latestSucceededGenerationId) &&
        Objects.equals(this.moref, infoData.moref) &&
        Objects.equals(this.name, infoData.name) &&
        Objects.equals(this.timestampMsOfLatestGenerationId, infoData.timestampMsOfLatestGenerationId) &&
        Objects.equals(this.timestampMsOfLatestSucceededGenerationId, infoData.timestampMsOfLatestSucceededGenerationId) &&
        Objects.equals(this.timestampOfLatestGenerationId, infoData.timestampOfLatestGenerationId) &&
        Objects.equals(this.timestampOfLatestSucceededGenerationId, infoData.timestampOfLatestSucceededGenerationId) &&
        Objects.equals(this.uuid, infoData.uuid) &&
        Objects.equals(this.full, infoData.full);
  }

  @Override
  public int hashCode() {
    return Objects.hash(entityType, latestGenerationId, latestSucceededGenerationId, moref, name, timestampMsOfLatestGenerationId, timestampMsOfLatestSucceededGenerationId, timestampOfLatestGenerationId, timestampOfLatestSucceededGenerationId, uuid, full);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InfoData {\n");
    
    sb.append("    entityType: ").append(toIndentedString(entityType)).append("\n");
    sb.append("    latestGenerationId: ").append(toIndentedString(latestGenerationId)).append("\n");
    sb.append("    latestSucceededGenerationId: ").append(toIndentedString(latestSucceededGenerationId)).append("\n");
    sb.append("    moref: ").append(toIndentedString(moref)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    timestampMsOfLatestGenerationId: ").append(toIndentedString(timestampMsOfLatestGenerationId)).append("\n");
    sb.append("    timestampMsOfLatestSucceededGenerationId: ").append(toIndentedString(timestampMsOfLatestSucceededGenerationId)).append("\n");
    sb.append("    timestampOfLatestGenerationId: ").append(toIndentedString(timestampOfLatestGenerationId)).append("\n");
    sb.append("    timestampOfLatestSucceededGenerationId: ").append(toIndentedString(timestampOfLatestSucceededGenerationId)).append("\n");
    sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
    sb.append("    full: ").append(toIndentedString(full)).append("\n");
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