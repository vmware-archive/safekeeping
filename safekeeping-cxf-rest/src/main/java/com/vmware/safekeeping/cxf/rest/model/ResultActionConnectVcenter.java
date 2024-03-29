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
import com.vmware.safekeeping.cxf.rest.model.VMwareCloudPlatforms;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * ResultActionConnectVcenter
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")public class ResultActionConnectVcenter extends ResultAction implements OneOfTaskResultResult  {
  @JsonProperty("ResourcePoolFilter")
  private String resourcePoolFilter = null;

  @JsonProperty("VmFolderFilter")
  private String vmFolderFilter = null;

  @JsonProperty("Api")
  private String api = null;

  @JsonProperty("InstanceUuid")
  private String instanceUuid = null;

  @JsonProperty("Name")
  private String name = null;

  @JsonProperty("PbmUrl")
  private String pbmUrl = null;

  @JsonProperty("PbmVersion")
  private String pbmVersion = null;

  @JsonProperty("Url")
  private String url = null;

  @JsonProperty("VapiUrl")
  private String vapiUrl = null;

  @JsonProperty("VslmName")
  private String vslmName = null;

  @JsonProperty("VslmUrl")
  private String vslmUrl = null;

  @JsonProperty("PbmConnected")
  private Boolean pbmConnected = null;

  @JsonProperty("VapiConnected")
  private Boolean vapiConnected = null;

  @JsonProperty("VslmConnected")
  private Boolean vslmConnected = null;

  @JsonProperty("cloudPlatform")
  private VMwareCloudPlatforms cloudPlatform = null;

  public ResultActionConnectVcenter resourcePoolFilter(String resourcePoolFilter) {
    this.resourcePoolFilter = resourcePoolFilter;
    return this;
  }

  /**
   * Get resourcePoolFilter
   * @return resourcePoolFilter
   **/
  @JsonProperty("ResourcePoolFilter")
  @Schema(description = "")
  public String getResourcePoolFilter() {
    return resourcePoolFilter;
  }

  public void setResourcePoolFilter(String resourcePoolFilter) {
    this.resourcePoolFilter = resourcePoolFilter;
  }

  public ResultActionConnectVcenter vmFolderFilter(String vmFolderFilter) {
    this.vmFolderFilter = vmFolderFilter;
    return this;
  }

  /**
   * Get vmFolderFilter
   * @return vmFolderFilter
   **/
  @JsonProperty("VmFolderFilter")
  @Schema(description = "")
  public String getVmFolderFilter() {
    return vmFolderFilter;
  }

  public void setVmFolderFilter(String vmFolderFilter) {
    this.vmFolderFilter = vmFolderFilter;
  }

  public ResultActionConnectVcenter api(String api) {
    this.api = api;
    return this;
  }

  /**
   * Get api
   * @return api
   **/
  @JsonProperty("Api")
  @Schema(description = "")
  public String getApi() {
    return api;
  }

  public void setApi(String api) {
    this.api = api;
  }

  public ResultActionConnectVcenter instanceUuid(String instanceUuid) {
    this.instanceUuid = instanceUuid;
    return this;
  }

  /**
   * Get instanceUuid
   * @return instanceUuid
   **/
  @JsonProperty("InstanceUuid")
  @Schema(description = "")
  public String getInstanceUuid() {
    return instanceUuid;
  }

  public void setInstanceUuid(String instanceUuid) {
    this.instanceUuid = instanceUuid;
  }

  public ResultActionConnectVcenter name(String name) {
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

  public ResultActionConnectVcenter pbmUrl(String pbmUrl) {
    this.pbmUrl = pbmUrl;
    return this;
  }

  /**
   * Get pbmUrl
   * @return pbmUrl
   **/
  @JsonProperty("PbmUrl")
  @Schema(description = "")
  public String getPbmUrl() {
    return pbmUrl;
  }

  public void setPbmUrl(String pbmUrl) {
    this.pbmUrl = pbmUrl;
  }

  public ResultActionConnectVcenter pbmVersion(String pbmVersion) {
    this.pbmVersion = pbmVersion;
    return this;
  }

  /**
   * Get pbmVersion
   * @return pbmVersion
   **/
  @JsonProperty("PbmVersion")
  @Schema(description = "")
  public String getPbmVersion() {
    return pbmVersion;
  }

  public void setPbmVersion(String pbmVersion) {
    this.pbmVersion = pbmVersion;
  }

  public ResultActionConnectVcenter url(String url) {
    this.url = url;
    return this;
  }

  /**
   * Get url
   * @return url
   **/
  @JsonProperty("Url")
  @Schema(description = "")
  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public ResultActionConnectVcenter vapiUrl(String vapiUrl) {
    this.vapiUrl = vapiUrl;
    return this;
  }

  /**
   * Get vapiUrl
   * @return vapiUrl
   **/
  @JsonProperty("VapiUrl")
  @Schema(description = "")
  public String getVapiUrl() {
    return vapiUrl;
  }

  public void setVapiUrl(String vapiUrl) {
    this.vapiUrl = vapiUrl;
  }

  public ResultActionConnectVcenter vslmName(String vslmName) {
    this.vslmName = vslmName;
    return this;
  }

  /**
   * Get vslmName
   * @return vslmName
   **/
  @JsonProperty("VslmName")
  @Schema(description = "")
  public String getVslmName() {
    return vslmName;
  }

  public void setVslmName(String vslmName) {
    this.vslmName = vslmName;
  }

  public ResultActionConnectVcenter vslmUrl(String vslmUrl) {
    this.vslmUrl = vslmUrl;
    return this;
  }

  /**
   * Get vslmUrl
   * @return vslmUrl
   **/
  @JsonProperty("VslmUrl")
  @Schema(description = "")
  public String getVslmUrl() {
    return vslmUrl;
  }

  public void setVslmUrl(String vslmUrl) {
    this.vslmUrl = vslmUrl;
  }

  public ResultActionConnectVcenter pbmConnected(Boolean pbmConnected) {
    this.pbmConnected = pbmConnected;
    return this;
  }

  /**
   * Get pbmConnected
   * @return pbmConnected
   **/
  @JsonProperty("PbmConnected")
  @Schema(description = "")
  public Boolean isPbmConnected() {
    return pbmConnected;
  }

  public void setPbmConnected(Boolean pbmConnected) {
    this.pbmConnected = pbmConnected;
  }

  public ResultActionConnectVcenter vapiConnected(Boolean vapiConnected) {
    this.vapiConnected = vapiConnected;
    return this;
  }

  /**
   * Get vapiConnected
   * @return vapiConnected
   **/
  @JsonProperty("VapiConnected")
  @Schema(description = "")
  public Boolean isVapiConnected() {
    return vapiConnected;
  }

  public void setVapiConnected(Boolean vapiConnected) {
    this.vapiConnected = vapiConnected;
  }

  public ResultActionConnectVcenter vslmConnected(Boolean vslmConnected) {
    this.vslmConnected = vslmConnected;
    return this;
  }

  /**
   * Get vslmConnected
   * @return vslmConnected
   **/
  @JsonProperty("VslmConnected")
  @Schema(description = "")
  public Boolean isVslmConnected() {
    return vslmConnected;
  }

  public void setVslmConnected(Boolean vslmConnected) {
    this.vslmConnected = vslmConnected;
  }

  public ResultActionConnectVcenter cloudPlatform(VMwareCloudPlatforms cloudPlatform) {
    this.cloudPlatform = cloudPlatform;
    return this;
  }

  /**
   * Get cloudPlatform
   * @return cloudPlatform
   **/
  @JsonProperty("cloudPlatform")
  @Schema(description = "")
  @Valid
  public VMwareCloudPlatforms getCloudPlatform() {
    return cloudPlatform;
  }

  public void setCloudPlatform(VMwareCloudPlatforms cloudPlatform) {
    this.cloudPlatform = cloudPlatform;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResultActionConnectVcenter resultActionConnectVcenter = (ResultActionConnectVcenter) o;
    return Objects.equals(this.resourcePoolFilter, resultActionConnectVcenter.resourcePoolFilter) &&
        Objects.equals(this.vmFolderFilter, resultActionConnectVcenter.vmFolderFilter) &&
        Objects.equals(this.api, resultActionConnectVcenter.api) &&
        Objects.equals(this.instanceUuid, resultActionConnectVcenter.instanceUuid) &&
        Objects.equals(this.name, resultActionConnectVcenter.name) &&
        Objects.equals(this.pbmUrl, resultActionConnectVcenter.pbmUrl) &&
        Objects.equals(this.pbmVersion, resultActionConnectVcenter.pbmVersion) &&
        Objects.equals(this.url, resultActionConnectVcenter.url) &&
        Objects.equals(this.vapiUrl, resultActionConnectVcenter.vapiUrl) &&
        Objects.equals(this.vslmName, resultActionConnectVcenter.vslmName) &&
        Objects.equals(this.vslmUrl, resultActionConnectVcenter.vslmUrl) &&
        Objects.equals(this.pbmConnected, resultActionConnectVcenter.pbmConnected) &&
        Objects.equals(this.vapiConnected, resultActionConnectVcenter.vapiConnected) &&
        Objects.equals(this.vslmConnected, resultActionConnectVcenter.vslmConnected) &&
        Objects.equals(this.cloudPlatform, resultActionConnectVcenter.cloudPlatform) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(resourcePoolFilter, vmFolderFilter, api, instanceUuid, name, pbmUrl, pbmVersion, url, vapiUrl, vslmName, vslmUrl, pbmConnected, vapiConnected, vslmConnected, cloudPlatform, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResultActionConnectVcenter {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    resourcePoolFilter: ").append(toIndentedString(resourcePoolFilter)).append("\n");
    sb.append("    vmFolderFilter: ").append(toIndentedString(vmFolderFilter)).append("\n");
    sb.append("    api: ").append(toIndentedString(api)).append("\n");
    sb.append("    instanceUuid: ").append(toIndentedString(instanceUuid)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    pbmUrl: ").append(toIndentedString(pbmUrl)).append("\n");
    sb.append("    pbmVersion: ").append(toIndentedString(pbmVersion)).append("\n");
    sb.append("    url: ").append(toIndentedString(url)).append("\n");
    sb.append("    vapiUrl: ").append(toIndentedString(vapiUrl)).append("\n");
    sb.append("    vslmName: ").append(toIndentedString(vslmName)).append("\n");
    sb.append("    vslmUrl: ").append(toIndentedString(vslmUrl)).append("\n");
    sb.append("    pbmConnected: ").append(toIndentedString(pbmConnected)).append("\n");
    sb.append("    vapiConnected: ").append(toIndentedString(vapiConnected)).append("\n");
    sb.append("    vslmConnected: ").append(toIndentedString(vslmConnected)).append("\n");
    sb.append("    cloudPlatform: ").append(toIndentedString(cloudPlatform)).append("\n");
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
