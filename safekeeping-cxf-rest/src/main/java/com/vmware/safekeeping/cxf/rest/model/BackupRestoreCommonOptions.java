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
import com.vmware.safekeeping.cxf.rest.model.AbstractBasicCommandOptions;
import com.vmware.safekeeping.cxf.rest.model.FcoTarget;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * BackupRestoreCommonOptions
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class BackupRestoreCommonOptions extends AbstractBasicCommandOptions  {
  @JsonProperty("NumberOfThreads")
  private Integer numberOfThreads = null;

  @JsonProperty("Force")
  private Boolean force = null;

  @JsonProperty("NoVmdk")
  private Boolean noVmdk = null;

  public BackupRestoreCommonOptions numberOfThreads(Integer numberOfThreads) {
    this.numberOfThreads = numberOfThreads;
    return this;
  }

  /**
   * Get numberOfThreads
   * @return numberOfThreads
   **/
  @JsonProperty("NumberOfThreads")
  @Schema(description = "")
  public Integer getNumberOfThreads() {
    return numberOfThreads;
  }

  public void setNumberOfThreads(Integer numberOfThreads) {
    this.numberOfThreads = numberOfThreads;
  }

  public BackupRestoreCommonOptions force(Boolean force) {
    this.force = force;
    return this;
  }

  /**
   * Get force
   * @return force
   **/
  @JsonProperty("Force")
  @Schema(description = "")
  public Boolean isForce() {
    return force;
  }

  public void setForce(Boolean force) {
    this.force = force;
  }

  public BackupRestoreCommonOptions noVmdk(Boolean noVmdk) {
    this.noVmdk = noVmdk;
    return this;
  }

  /**
   * Get noVmdk
   * @return noVmdk
   **/
  @JsonProperty("NoVmdk")
  @Schema(description = "")
  public Boolean isNoVmdk() {
    return noVmdk;
  }

  public void setNoVmdk(Boolean noVmdk) {
    this.noVmdk = noVmdk;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BackupRestoreCommonOptions backupRestoreCommonOptions = (BackupRestoreCommonOptions) o;
    return Objects.equals(this.numberOfThreads, backupRestoreCommonOptions.numberOfThreads) &&
        Objects.equals(this.force, backupRestoreCommonOptions.force) &&
        Objects.equals(this.noVmdk, backupRestoreCommonOptions.noVmdk) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(numberOfThreads, force, noVmdk, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BackupRestoreCommonOptions {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    numberOfThreads: ").append(toIndentedString(numberOfThreads)).append("\n");
    sb.append("    force: ").append(toIndentedString(force)).append("\n");
    sb.append("    noVmdk: ").append(toIndentedString(noVmdk)).append("\n");
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