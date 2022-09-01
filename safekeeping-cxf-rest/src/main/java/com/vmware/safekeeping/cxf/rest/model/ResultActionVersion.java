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
import com.vmware.safekeeping.cxf.rest.model.ManagedFcoEntityInfo;
import com.vmware.safekeeping.cxf.rest.model.OperationState;
import com.vmware.safekeeping.cxf.rest.model.ResultAction;
import com.vmware.safekeeping.cxf.rest.model.SapiTask;
import com.vmware.safekeeping.cxf.rest.model.ServerInfo;
import com.vmware.safekeeping.cxf.rest.model.VddkVersion;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * ResultActionVersion
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class ResultActionVersion extends ResultAction implements OneOfTaskResultResult  {
  @JsonProperty("ExtendedVersion")
  private String extendedVersion = null;

  @JsonProperty("Identity")
  private String identity = null;

  @JsonProperty("JavaRuntime")
  private String javaRuntime = null;

  @JsonProperty("Major")
  private Integer major = null;

  @JsonProperty("Minor")
  private Integer minor = null;

  @JsonProperty("PatchLevel")
  private Integer patchLevel = null;

  @JsonProperty("ProductName")
  private String productName = null;

  @JsonProperty("ServerInfo")
  private ServerInfo serverInfo = null;

  @JsonProperty("Vddk")
  private VddkVersion vddk = null;

  @JsonProperty("Version")
  private String version = null;

  public ResultActionVersion extendedVersion(String extendedVersion) {
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

  public ResultActionVersion identity(String identity) {
    this.identity = identity;
    return this;
  }

  /**
   * Get identity
   * @return identity
   **/
  @JsonProperty("Identity")
  @Schema(description = "")
  public String getIdentity() {
    return identity;
  }

  public void setIdentity(String identity) {
    this.identity = identity;
  }

  public ResultActionVersion javaRuntime(String javaRuntime) {
    this.javaRuntime = javaRuntime;
    return this;
  }

  /**
   * Get javaRuntime
   * @return javaRuntime
   **/
  @JsonProperty("JavaRuntime")
  @Schema(description = "")
  public String getJavaRuntime() {
    return javaRuntime;
  }

  public void setJavaRuntime(String javaRuntime) {
    this.javaRuntime = javaRuntime;
  }

  public ResultActionVersion major(Integer major) {
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

  public ResultActionVersion minor(Integer minor) {
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

  public ResultActionVersion patchLevel(Integer patchLevel) {
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

  public ResultActionVersion productName(String productName) {
    this.productName = productName;
    return this;
  }

  /**
   * Get productName
   * @return productName
   **/
  @JsonProperty("ProductName")
  @Schema(description = "")
  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public ResultActionVersion serverInfo(ServerInfo serverInfo) {
    this.serverInfo = serverInfo;
    return this;
  }

  /**
   * Get serverInfo
   * @return serverInfo
   **/
  @JsonProperty("ServerInfo")
  @Schema(description = "")
  @Valid
  public ServerInfo getServerInfo() {
    return serverInfo;
  }

  public void setServerInfo(ServerInfo serverInfo) {
    this.serverInfo = serverInfo;
  }

  public ResultActionVersion vddk(VddkVersion vddk) {
    this.vddk = vddk;
    return this;
  }

  /**
   * Get vddk
   * @return vddk
   **/
  @JsonProperty("Vddk")
  @Schema(description = "")
  @Valid
  public VddkVersion getVddk() {
    return vddk;
  }

  public void setVddk(VddkVersion vddk) {
    this.vddk = vddk;
  }

  public ResultActionVersion version(String version) {
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
    ResultActionVersion resultActionVersion = (ResultActionVersion) o;
    return Objects.equals(this.extendedVersion, resultActionVersion.extendedVersion) &&
        Objects.equals(this.identity, resultActionVersion.identity) &&
        Objects.equals(this.javaRuntime, resultActionVersion.javaRuntime) &&
        Objects.equals(this.major, resultActionVersion.major) &&
        Objects.equals(this.minor, resultActionVersion.minor) &&
        Objects.equals(this.patchLevel, resultActionVersion.patchLevel) &&
        Objects.equals(this.productName, resultActionVersion.productName) &&
        Objects.equals(this.serverInfo, resultActionVersion.serverInfo) &&
        Objects.equals(this.vddk, resultActionVersion.vddk) &&
        Objects.equals(this.version, resultActionVersion.version) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(extendedVersion, identity, javaRuntime, major, minor, patchLevel, productName, serverInfo, vddk, version, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResultActionVersion {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    extendedVersion: ").append(toIndentedString(extendedVersion)).append("\n");
    sb.append("    identity: ").append(toIndentedString(identity)).append("\n");
    sb.append("    javaRuntime: ").append(toIndentedString(javaRuntime)).append("\n");
    sb.append("    major: ").append(toIndentedString(major)).append("\n");
    sb.append("    minor: ").append(toIndentedString(minor)).append("\n");
    sb.append("    patchLevel: ").append(toIndentedString(patchLevel)).append("\n");
    sb.append("    productName: ").append(toIndentedString(productName)).append("\n");
    sb.append("    serverInfo: ").append(toIndentedString(serverInfo)).append("\n");
    sb.append("    vddk: ").append(toIndentedString(vddk)).append("\n");
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