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
import com.vmware.safekeeping.cxf.rest.model.AbstractResultDiskBackupRestore;
import com.vmware.safekeeping.cxf.rest.model.AdapterType;
import com.vmware.safekeeping.cxf.rest.model.FileBackingInfoProvisioningType;
import com.vmware.safekeeping.cxf.rest.model.SapiTask;
import com.vmware.safekeeping.cxf.rest.model.VirtualDiskModeType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * AbstractResultActionDiskVirtualOperation
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")public class AbstractResultActionDiskVirtualOperation extends AbstractResultDiskBackupRestore  {
  @JsonProperty("DiskRestoreGenerationsProfileTasks")
  private List<SapiTask> diskRestoreGenerationsProfileTasks = null;

  @JsonProperty("GenerationList")
  private List<Integer> generationList = null;

  @JsonProperty("NumberOfGenerations")
  private Integer numberOfGenerations = null;

  @JsonProperty("NumberOfRelocatedBlocks")
  private Integer numberOfRelocatedBlocks = null;

  @JsonProperty("NumberOfReplacedBlock")
  private Integer numberOfReplacedBlock = null;

  @JsonProperty("TotalBlocks")
  private Integer totalBlocks = null;

  @JsonProperty("TotalDumpSize")
  private Long totalDumpSize = null;

  @JsonProperty("TotalNumberOfDisks")
  private Integer totalNumberOfDisks = null;

  public AbstractResultActionDiskVirtualOperation diskRestoreGenerationsProfileTasks(List<SapiTask> diskRestoreGenerationsProfileTasks) {
    this.diskRestoreGenerationsProfileTasks = diskRestoreGenerationsProfileTasks;
    return this;
  }

  public AbstractResultActionDiskVirtualOperation addDiskRestoreGenerationsProfileTasksItem(SapiTask diskRestoreGenerationsProfileTasksItem) {
    if (this.diskRestoreGenerationsProfileTasks == null) {
      this.diskRestoreGenerationsProfileTasks = new ArrayList<SapiTask>();
    }
    this.diskRestoreGenerationsProfileTasks.add(diskRestoreGenerationsProfileTasksItem);
    return this;
  }

  /**
   * Get diskRestoreGenerationsProfileTasks
   * @return diskRestoreGenerationsProfileTasks
   **/
  @JsonProperty("DiskRestoreGenerationsProfileTasks")
  @Schema(description = "")
  @Valid
  public List<SapiTask> getDiskRestoreGenerationsProfileTasks() {
    return diskRestoreGenerationsProfileTasks;
  }

  public void setDiskRestoreGenerationsProfileTasks(List<SapiTask> diskRestoreGenerationsProfileTasks) {
    this.diskRestoreGenerationsProfileTasks = diskRestoreGenerationsProfileTasks;
  }

  public AbstractResultActionDiskVirtualOperation generationList(List<Integer> generationList) {
    this.generationList = generationList;
    return this;
  }

  public AbstractResultActionDiskVirtualOperation addGenerationListItem(Integer generationListItem) {
    if (this.generationList == null) {
      this.generationList = new ArrayList<Integer>();
    }
    this.generationList.add(generationListItem);
    return this;
  }

  /**
   * Get generationList
   * @return generationList
   **/
  @JsonProperty("GenerationList")
  @Schema(description = "")
  public List<Integer> getGenerationList() {
    return generationList;
  }

  public void setGenerationList(List<Integer> generationList) {
    this.generationList = generationList;
  }

  public AbstractResultActionDiskVirtualOperation numberOfGenerations(Integer numberOfGenerations) {
    this.numberOfGenerations = numberOfGenerations;
    return this;
  }

  /**
   * Get numberOfGenerations
   * @return numberOfGenerations
   **/
  @JsonProperty("NumberOfGenerations")
  @Schema(description = "")
  public Integer getNumberOfGenerations() {
    return numberOfGenerations;
  }

  public void setNumberOfGenerations(Integer numberOfGenerations) {
    this.numberOfGenerations = numberOfGenerations;
  }

  public AbstractResultActionDiskVirtualOperation numberOfRelocatedBlocks(Integer numberOfRelocatedBlocks) {
    this.numberOfRelocatedBlocks = numberOfRelocatedBlocks;
    return this;
  }

  /**
   * Get numberOfRelocatedBlocks
   * @return numberOfRelocatedBlocks
   **/
  @JsonProperty("NumberOfRelocatedBlocks")
  @Schema(description = "")
  public Integer getNumberOfRelocatedBlocks() {
    return numberOfRelocatedBlocks;
  }

  public void setNumberOfRelocatedBlocks(Integer numberOfRelocatedBlocks) {
    this.numberOfRelocatedBlocks = numberOfRelocatedBlocks;
  }

  public AbstractResultActionDiskVirtualOperation numberOfReplacedBlock(Integer numberOfReplacedBlock) {
    this.numberOfReplacedBlock = numberOfReplacedBlock;
    return this;
  }

  /**
   * Get numberOfReplacedBlock
   * @return numberOfReplacedBlock
   **/
  @JsonProperty("NumberOfReplacedBlock")
  @Schema(description = "")
  public Integer getNumberOfReplacedBlock() {
    return numberOfReplacedBlock;
  }

  public void setNumberOfReplacedBlock(Integer numberOfReplacedBlock) {
    this.numberOfReplacedBlock = numberOfReplacedBlock;
  }

  public AbstractResultActionDiskVirtualOperation totalBlocks(Integer totalBlocks) {
    this.totalBlocks = totalBlocks;
    return this;
  }

  /**
   * Get totalBlocks
   * @return totalBlocks
   **/
  @JsonProperty("TotalBlocks")
  @Schema(description = "")
  public Integer getTotalBlocks() {
    return totalBlocks;
  }

  public void setTotalBlocks(Integer totalBlocks) {
    this.totalBlocks = totalBlocks;
  }

  public AbstractResultActionDiskVirtualOperation totalDumpSize(Long totalDumpSize) {
    this.totalDumpSize = totalDumpSize;
    return this;
  }

  /**
   * Get totalDumpSize
   * @return totalDumpSize
   **/
  @JsonProperty("TotalDumpSize")
  @Schema(description = "")
  public Long getTotalDumpSize() {
    return totalDumpSize;
  }

  public void setTotalDumpSize(Long totalDumpSize) {
    this.totalDumpSize = totalDumpSize;
  }

  public AbstractResultActionDiskVirtualOperation totalNumberOfDisks(Integer totalNumberOfDisks) {
    this.totalNumberOfDisks = totalNumberOfDisks;
    return this;
  }

  /**
   * Get totalNumberOfDisks
   * @return totalNumberOfDisks
   **/
  @JsonProperty("TotalNumberOfDisks")
  @Schema(description = "")
  public Integer getTotalNumberOfDisks() {
    return totalNumberOfDisks;
  }

  public void setTotalNumberOfDisks(Integer totalNumberOfDisks) {
    this.totalNumberOfDisks = totalNumberOfDisks;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractResultActionDiskVirtualOperation abstractResultActionDiskVirtualOperation = (AbstractResultActionDiskVirtualOperation) o;
    return Objects.equals(this.diskRestoreGenerationsProfileTasks, abstractResultActionDiskVirtualOperation.diskRestoreGenerationsProfileTasks) &&
        Objects.equals(this.generationList, abstractResultActionDiskVirtualOperation.generationList) &&
        Objects.equals(this.numberOfGenerations, abstractResultActionDiskVirtualOperation.numberOfGenerations) &&
        Objects.equals(this.numberOfRelocatedBlocks, abstractResultActionDiskVirtualOperation.numberOfRelocatedBlocks) &&
        Objects.equals(this.numberOfReplacedBlock, abstractResultActionDiskVirtualOperation.numberOfReplacedBlock) &&
        Objects.equals(this.totalBlocks, abstractResultActionDiskVirtualOperation.totalBlocks) &&
        Objects.equals(this.totalDumpSize, abstractResultActionDiskVirtualOperation.totalDumpSize) &&
        Objects.equals(this.totalNumberOfDisks, abstractResultActionDiskVirtualOperation.totalNumberOfDisks) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(diskRestoreGenerationsProfileTasks, generationList, numberOfGenerations, numberOfRelocatedBlocks, numberOfReplacedBlock, totalBlocks, totalDumpSize, totalNumberOfDisks, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AbstractResultActionDiskVirtualOperation {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    diskRestoreGenerationsProfileTasks: ").append(toIndentedString(diskRestoreGenerationsProfileTasks)).append("\n");
    sb.append("    generationList: ").append(toIndentedString(generationList)).append("\n");
    sb.append("    numberOfGenerations: ").append(toIndentedString(numberOfGenerations)).append("\n");
    sb.append("    numberOfRelocatedBlocks: ").append(toIndentedString(numberOfRelocatedBlocks)).append("\n");
    sb.append("    numberOfReplacedBlock: ").append(toIndentedString(numberOfReplacedBlock)).append("\n");
    sb.append("    totalBlocks: ").append(toIndentedString(totalBlocks)).append("\n");
    sb.append("    totalDumpSize: ").append(toIndentedString(totalDumpSize)).append("\n");
    sb.append("    totalNumberOfDisks: ").append(toIndentedString(totalNumberOfDisks)).append("\n");
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
