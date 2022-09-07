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
import com.vmware.safekeeping.cxf.rest.model.FcoLocation;
import com.vmware.safekeeping.cxf.rest.model.ManagedEntityInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * IvdLocation
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")public class IvdLocation extends FcoLocation  {
  @JsonProperty("vmdkFileName")
  private String vmdkFileName = null;

  @JsonProperty("datastorePath")
  private String datastorePath = null;

  @JsonProperty("datastoreInfo")
  private ManagedEntityInfo datastoreInfo = null;

  @JsonProperty("vmdkFullPath")
  private String vmdkFullPath = null;

  public IvdLocation vmdkFileName(String vmdkFileName) {
    this.vmdkFileName = vmdkFileName;
    return this;
  }

  /**
   * Get vmdkFileName
   * @return vmdkFileName
   **/
  @JsonProperty("vmdkFileName")
  @Schema(description = "")
  public String getVmdkFileName() {
    return vmdkFileName;
  }

  public void setVmdkFileName(String vmdkFileName) {
    this.vmdkFileName = vmdkFileName;
  }

  public IvdLocation datastorePath(String datastorePath) {
    this.datastorePath = datastorePath;
    return this;
  }

  /**
   * Get datastorePath
   * @return datastorePath
   **/
  @JsonProperty("datastorePath")
  @Schema(description = "")
  public String getDatastorePath() {
    return datastorePath;
  }

  public void setDatastorePath(String datastorePath) {
    this.datastorePath = datastorePath;
  }

  public IvdLocation datastoreInfo(ManagedEntityInfo datastoreInfo) {
    this.datastoreInfo = datastoreInfo;
    return this;
  }

  /**
   * Get datastoreInfo
   * @return datastoreInfo
   **/
  @JsonProperty("datastoreInfo")
  @Schema(description = "")
  @Valid
  public ManagedEntityInfo getDatastoreInfo() {
    return datastoreInfo;
  }

  public void setDatastoreInfo(ManagedEntityInfo datastoreInfo) {
    this.datastoreInfo = datastoreInfo;
  }

  public IvdLocation vmdkFullPath(String vmdkFullPath) {
    this.vmdkFullPath = vmdkFullPath;
    return this;
  }

  /**
   * Get vmdkFullPath
   * @return vmdkFullPath
   **/
  @JsonProperty("vmdkFullPath")
  @Schema(description = "")
  public String getVmdkFullPath() {
    return vmdkFullPath;
  }

  public void setVmdkFullPath(String vmdkFullPath) {
    this.vmdkFullPath = vmdkFullPath;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IvdLocation ivdLocation = (IvdLocation) o;
    return Objects.equals(this.vmdkFileName, ivdLocation.vmdkFileName) &&
        Objects.equals(this.datastorePath, ivdLocation.datastorePath) &&
        Objects.equals(this.datastoreInfo, ivdLocation.datastoreInfo) &&
        Objects.equals(this.vmdkFullPath, ivdLocation.vmdkFullPath) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(vmdkFileName, datastorePath, datastoreInfo, vmdkFullPath, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class IvdLocation {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    vmdkFileName: ").append(toIndentedString(vmdkFileName)).append("\n");
    sb.append("    datastorePath: ").append(toIndentedString(datastorePath)).append("\n");
    sb.append("    datastoreInfo: ").append(toIndentedString(datastoreInfo)).append("\n");
    sb.append("    vmdkFullPath: ").append(toIndentedString(vmdkFullPath)).append("\n");
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
