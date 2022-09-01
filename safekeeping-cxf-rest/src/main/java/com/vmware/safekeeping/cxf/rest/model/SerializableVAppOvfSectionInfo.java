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
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * SerializableVAppOvfSectionInfo
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class SerializableVAppOvfSectionInfo   {
  @JsonProperty("AtEnvelopeLevel")
  private Boolean atEnvelopeLevel = null;

  @JsonProperty("Contents")
  private String contents = null;

  @JsonProperty("Key")
  private Integer key = null;

  @JsonProperty("Namespace")
  private String namespace = null;

  @JsonProperty("Type")
  private String type = null;

  public SerializableVAppOvfSectionInfo atEnvelopeLevel(Boolean atEnvelopeLevel) {
    this.atEnvelopeLevel = atEnvelopeLevel;
    return this;
  }

  /**
   * Get atEnvelopeLevel
   * @return atEnvelopeLevel
   **/
  @JsonProperty("AtEnvelopeLevel")
  @Schema(description = "")
  public Boolean isAtEnvelopeLevel() {
    return atEnvelopeLevel;
  }

  public void setAtEnvelopeLevel(Boolean atEnvelopeLevel) {
    this.atEnvelopeLevel = atEnvelopeLevel;
  }

  public SerializableVAppOvfSectionInfo contents(String contents) {
    this.contents = contents;
    return this;
  }

  /**
   * Get contents
   * @return contents
   **/
  @JsonProperty("Contents")
  @Schema(description = "")
  public String getContents() {
    return contents;
  }

  public void setContents(String contents) {
    this.contents = contents;
  }

  public SerializableVAppOvfSectionInfo key(Integer key) {
    this.key = key;
    return this;
  }

  /**
   * Get key
   * @return key
   **/
  @JsonProperty("Key")
  @Schema(description = "")
  public Integer getKey() {
    return key;
  }

  public void setKey(Integer key) {
    this.key = key;
  }

  public SerializableVAppOvfSectionInfo namespace(String namespace) {
    this.namespace = namespace;
    return this;
  }

  /**
   * Get namespace
   * @return namespace
   **/
  @JsonProperty("Namespace")
  @Schema(description = "")
  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public SerializableVAppOvfSectionInfo type(String type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
   **/
  @JsonProperty("Type")
  @Schema(description = "")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SerializableVAppOvfSectionInfo serializableVAppOvfSectionInfo = (SerializableVAppOvfSectionInfo) o;
    return Objects.equals(this.atEnvelopeLevel, serializableVAppOvfSectionInfo.atEnvelopeLevel) &&
        Objects.equals(this.contents, serializableVAppOvfSectionInfo.contents) &&
        Objects.equals(this.key, serializableVAppOvfSectionInfo.key) &&
        Objects.equals(this.namespace, serializableVAppOvfSectionInfo.namespace) &&
        Objects.equals(this.type, serializableVAppOvfSectionInfo.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(atEnvelopeLevel, contents, key, namespace, type);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SerializableVAppOvfSectionInfo {\n");
    
    sb.append("    atEnvelopeLevel: ").append(toIndentedString(atEnvelopeLevel)).append("\n");
    sb.append("    contents: ").append(toIndentedString(contents)).append("\n");
    sb.append("    key: ").append(toIndentedString(key)).append("\n");
    sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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