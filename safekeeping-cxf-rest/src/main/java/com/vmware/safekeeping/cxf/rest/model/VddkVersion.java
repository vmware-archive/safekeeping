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
 * VddkVersion
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")public class VddkVersion   {
  @JsonProperty("Build")
  private Integer build = null;

  @JsonProperty("ExtendedVersion")
  private String extendedVersion = null;

  @JsonProperty("Major")
  private Integer major = null;

  @JsonProperty("Minor")
  private Integer minor = null;

  @JsonProperty("PatchLevel")
  private Integer patchLevel = null;

  @JsonProperty("Version")
  private String version = null;

  public VddkVersion build(Integer build) {
    this.build = build;
    return this;
  }

  /**
   * Get build
   * @return build
   **/
  @JsonProperty("Build")
  @Schema(description = "")
  public Integer getBuild() {
    return build;
  }

  public void setBuild(Integer build) {
    this.build = build;
  }

  public VddkVersion extendedVersion(String extendedVersion) {
    this.extendedVersion = extendedVersion;
    return this;
  }

  /**
   * Get extendedVersion
   * @return extendedVersion
   **/
  @JsonProperty("ExtendedVersion")
  @Schema(description = "")
  public String getExtendedVersion() {
    return extendedVersion;
  }

  public void setExtendedVersion(String extendedVersion) {
    this.extendedVersion = extendedVersion;
  }

  public VddkVersion major(Integer major) {
    this.major = major;
    return this;
  }

  /**
   * Get major
   * @return major
   **/
  @JsonProperty("Major")
  @Schema(description = "")
  public Integer getMajor() {
    return major;
  }

  public void setMajor(Integer major) {
    this.major = major;
  }

  public VddkVersion minor(Integer minor) {
    this.minor = minor;
    return this;
  }

  /**
   * Get minor
   * @return minor
   **/
  @JsonProperty("Minor")
  @Schema(description = "")
  public Integer getMinor() {
    return minor;
  }

  public void setMinor(Integer minor) {
    this.minor = minor;
  }

  public VddkVersion patchLevel(Integer patchLevel) {
    this.patchLevel = patchLevel;
    return this;
  }

  /**
   * Get patchLevel
   * @return patchLevel
   **/
  @JsonProperty("PatchLevel")
  @Schema(description = "")
  public Integer getPatchLevel() {
    return patchLevel;
  }

  public void setPatchLevel(Integer patchLevel) {
    this.patchLevel = patchLevel;
  }

  public VddkVersion version(String version) {
    this.version = version;
    return this;
  }

  /**
   * Get version
   * @return version
   **/
  @JsonProperty("Version")
  @Schema(description = "")
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VddkVersion vddkVersion = (VddkVersion) o;
    return Objects.equals(this.build, vddkVersion.build) &&
        Objects.equals(this.extendedVersion, vddkVersion.extendedVersion) &&
        Objects.equals(this.major, vddkVersion.major) &&
        Objects.equals(this.minor, vddkVersion.minor) &&
        Objects.equals(this.patchLevel, vddkVersion.patchLevel) &&
        Objects.equals(this.version, vddkVersion.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(build, extendedVersion, major, minor, patchLevel, version);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VddkVersion {\n");
    
    sb.append("    build: ").append(toIndentedString(build)).append("\n");
    sb.append("    extendedVersion: ").append(toIndentedString(extendedVersion)).append("\n");
    sb.append("    major: ").append(toIndentedString(major)).append("\n");
    sb.append("    minor: ").append(toIndentedString(minor)).append("\n");
    sb.append("    patchLevel: ").append(toIndentedString(patchLevel)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
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
