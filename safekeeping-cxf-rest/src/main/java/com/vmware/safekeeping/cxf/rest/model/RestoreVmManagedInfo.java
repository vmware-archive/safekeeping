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
import com.vmware.safekeeping.cxf.rest.model.ManagedEntityInfo;
import com.vmware.safekeeping.cxf.rest.model.RestoreManagedInfo;
import com.vmware.safekeeping.cxf.rest.model.SerializableVAppConfigInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * RestoreVmManagedInfo
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")public class RestoreVmManagedInfo extends RestoreManagedInfo  {
  @JsonProperty("DsInfo")
  private ManagedEntityInfo dsInfo = null;

  @JsonProperty("FolderInfo")
  private ManagedEntityInfo folderInfo = null;

  @JsonProperty("HostInfo")
  private ManagedEntityInfo hostInfo = null;

  @JsonProperty("NetworkMapping")
  private List<ManagedEntityInfo> networkMapping = null;

  @JsonProperty("ResourcePoolInfo")
  private ManagedEntityInfo resourcePoolInfo = null;

  @JsonProperty("VAppConfig")
  private SerializableVAppConfigInfo vappConfig = null;

  public RestoreVmManagedInfo dsInfo(ManagedEntityInfo dsInfo) {
    this.dsInfo = dsInfo;
    return this;
  }

  /**
   * Get dsInfo
   * @return dsInfo
   **/
  @JsonProperty("DsInfo")
  @Schema(description = "")
  @Valid
  public ManagedEntityInfo getDsInfo() {
    return dsInfo;
  }

  public void setDsInfo(ManagedEntityInfo dsInfo) {
    this.dsInfo = dsInfo;
  }

  public RestoreVmManagedInfo folderInfo(ManagedEntityInfo folderInfo) {
    this.folderInfo = folderInfo;
    return this;
  }

  /**
   * Get folderInfo
   * @return folderInfo
   **/
  @JsonProperty("FolderInfo")
  @Schema(description = "")
  @Valid
  public ManagedEntityInfo getFolderInfo() {
    return folderInfo;
  }

  public void setFolderInfo(ManagedEntityInfo folderInfo) {
    this.folderInfo = folderInfo;
  }

  public RestoreVmManagedInfo hostInfo(ManagedEntityInfo hostInfo) {
    this.hostInfo = hostInfo;
    return this;
  }

  /**
   * Get hostInfo
   * @return hostInfo
   **/
  @JsonProperty("HostInfo")
  @Schema(description = "")
  @Valid
  public ManagedEntityInfo getHostInfo() {
    return hostInfo;
  }

  public void setHostInfo(ManagedEntityInfo hostInfo) {
    this.hostInfo = hostInfo;
  }

  public RestoreVmManagedInfo networkMapping(List<ManagedEntityInfo> networkMapping) {
    this.networkMapping = networkMapping;
    return this;
  }

  public RestoreVmManagedInfo addNetworkMappingItem(ManagedEntityInfo networkMappingItem) {
    if (this.networkMapping == null) {
      this.networkMapping = new ArrayList<ManagedEntityInfo>();
    }
    this.networkMapping.add(networkMappingItem);
    return this;
  }

  /**
   * Get networkMapping
   * @return networkMapping
   **/
  @JsonProperty("NetworkMapping")
  @Schema(description = "")
  @Valid
  public List<ManagedEntityInfo> getNetworkMapping() {
    return networkMapping;
  }

  public void setNetworkMapping(List<ManagedEntityInfo> networkMapping) {
    this.networkMapping = networkMapping;
  }

  public RestoreVmManagedInfo resourcePoolInfo(ManagedEntityInfo resourcePoolInfo) {
    this.resourcePoolInfo = resourcePoolInfo;
    return this;
  }

  /**
   * Get resourcePoolInfo
   * @return resourcePoolInfo
   **/
  @JsonProperty("ResourcePoolInfo")
  @Schema(description = "")
  @Valid
  public ManagedEntityInfo getResourcePoolInfo() {
    return resourcePoolInfo;
  }

  public void setResourcePoolInfo(ManagedEntityInfo resourcePoolInfo) {
    this.resourcePoolInfo = resourcePoolInfo;
  }

  public RestoreVmManagedInfo vappConfig(SerializableVAppConfigInfo vappConfig) {
    this.vappConfig = vappConfig;
    return this;
  }

  /**
   * Get vappConfig
   * @return vappConfig
   **/
  @JsonProperty("VAppConfig")
  @Schema(description = "")
  @Valid
  public SerializableVAppConfigInfo getVappConfig() {
    return vappConfig;
  }

  public void setVappConfig(SerializableVAppConfigInfo vappConfig) {
    this.vappConfig = vappConfig;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RestoreVmManagedInfo restoreVmManagedInfo = (RestoreVmManagedInfo) o;
    return Objects.equals(this.dsInfo, restoreVmManagedInfo.dsInfo) &&
        Objects.equals(this.folderInfo, restoreVmManagedInfo.folderInfo) &&
        Objects.equals(this.hostInfo, restoreVmManagedInfo.hostInfo) &&
        Objects.equals(this.networkMapping, restoreVmManagedInfo.networkMapping) &&
        Objects.equals(this.resourcePoolInfo, restoreVmManagedInfo.resourcePoolInfo) &&
        Objects.equals(this.vappConfig, restoreVmManagedInfo.vappConfig) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dsInfo, folderInfo, hostInfo, networkMapping, resourcePoolInfo, vappConfig, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RestoreVmManagedInfo {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    dsInfo: ").append(toIndentedString(dsInfo)).append("\n");
    sb.append("    folderInfo: ").append(toIndentedString(folderInfo)).append("\n");
    sb.append("    hostInfo: ").append(toIndentedString(hostInfo)).append("\n");
    sb.append("    networkMapping: ").append(toIndentedString(networkMapping)).append("\n");
    sb.append("    resourcePoolInfo: ").append(toIndentedString(resourcePoolInfo)).append("\n");
    sb.append("    vappConfig: ").append(toIndentedString(vappConfig)).append("\n");
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
