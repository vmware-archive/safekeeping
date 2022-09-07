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
import com.vmware.safekeeping.cxf.rest.model.FcoTarget;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * AbstractBasicCommandOptions
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")
public class AbstractBasicCommandOptions   {
  @JsonProperty("AnyFcoOfType")
  private Integer anyFcoOfType = null;

  @JsonProperty("TargetList")
  private List<FcoTarget> targetList = null;

  @JsonProperty("Vim")
  private String vim = null;

  @JsonProperty("DryRun")
  private Boolean dryRun = null;

  public AbstractBasicCommandOptions anyFcoOfType(Integer anyFcoOfType) {
    this.anyFcoOfType = anyFcoOfType;
    return this;
  }

  /**
   * Get anyFcoOfType
   * @return anyFcoOfType
   **/
  @JsonProperty("AnyFcoOfType")
  @Schema(description = "")
  public Integer getAnyFcoOfType() {
    return anyFcoOfType;
  }

  public void setAnyFcoOfType(Integer anyFcoOfType) {
    this.anyFcoOfType = anyFcoOfType;
  }

  public AbstractBasicCommandOptions targetList(List<FcoTarget> targetList) {
    this.targetList = targetList;
    return this;
  }

  public AbstractBasicCommandOptions addTargetListItem(FcoTarget targetListItem) {
    if (this.targetList == null) {
      this.targetList = new ArrayList<FcoTarget>();
    }
    this.targetList.add(targetListItem);
    return this;
  }

  /**
   * Get targetList
   * @return targetList
   **/
  @JsonProperty("TargetList")
  @Schema(description = "")
  @Valid
  public List<FcoTarget> getTargetList() {
    return targetList;
  }

  public void setTargetList(List<FcoTarget> targetList) {
    this.targetList = targetList;
  }

  public AbstractBasicCommandOptions vim(String vim) {
    this.vim = vim;
    return this;
  }

  /**
   * Get vim
   * @return vim
   **/
  @JsonProperty("Vim")
  @Schema(description = "")
  public String getVim() {
    return vim;
  }

  public void setVim(String vim) {
    this.vim = vim;
  }

  public AbstractBasicCommandOptions dryRun(Boolean dryRun) {
    this.dryRun = dryRun;
    return this;
  }

  /**
   * Get dryRun
   * @return dryRun
   **/
  @JsonProperty("DryRun")
  @Schema(description = "")
  public Boolean isDryRun() {
    return dryRun;
  }

  public void setDryRun(Boolean dryRun) {
    this.dryRun = dryRun;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractBasicCommandOptions abstractBasicCommandOptions = (AbstractBasicCommandOptions) o;
    return Objects.equals(this.anyFcoOfType, abstractBasicCommandOptions.anyFcoOfType) &&
        Objects.equals(this.targetList, abstractBasicCommandOptions.targetList) &&
        Objects.equals(this.vim, abstractBasicCommandOptions.vim) &&
        Objects.equals(this.dryRun, abstractBasicCommandOptions.dryRun);
  }

  @Override
  public int hashCode() {
    return Objects.hash(anyFcoOfType, targetList, vim, dryRun);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AbstractBasicCommandOptions {\n");
    
    sb.append("    anyFcoOfType: ").append(toIndentedString(anyFcoOfType)).append("\n");
    sb.append("    targetList: ").append(toIndentedString(targetList)).append("\n");
    sb.append("    vim: ").append(toIndentedString(vim)).append("\n");
    sb.append("    dryRun: ").append(toIndentedString(dryRun)).append("\n");
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
