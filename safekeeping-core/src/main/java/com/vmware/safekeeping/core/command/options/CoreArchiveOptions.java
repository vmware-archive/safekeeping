/*******************************************************************************
 * Copyright (C) 2021, VMware Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package com.vmware.safekeeping.core.command.options;

import java.util.LinkedList;
import java.util.List;

import com.vmware.safekeeping.core.type.enums.ArchiveObjects;

public class CoreArchiveOptions extends CoreBackupRestoreCommonOptions {

    /**
     * 
     */
    private static final long serialVersionUID = 1534401789992416670L;

    private List<Integer> generationId;

    private boolean check;

    private boolean list;

    private Long dateTimeFilter;

    private boolean profile;

    private boolean remove;

    private boolean status;

    private boolean prettyJason;

    private ArchiveObjects show;

    /**
     *
     */
    public CoreArchiveOptions() {
        setGenerationId(new LinkedList<>());
    }

    /**
     * @return the mtime
     */
    public Long getDateTimeFilter() {
        return this.dateTimeFilter;
    }

    /**
     * @return the generationId
     */
    public List<Integer> getGenerationId() {
        return this.generationId;
    }

    /**
     * @return the show
     */
    public ArchiveObjects getShow() {
        return this.show;
    }

    /**
     * @return the check
     */
    public boolean isCheck() {
        return this.check;
    }

    /**
     * @return the list
     */
    public boolean isList() {
        return this.list;
    }

    /**
     * @return the prettyJason
     */
    public boolean isPrettyJason() {
        return this.prettyJason;
    }

    /**
     * @return the profile
     */
    public boolean isProfile() {
        return this.profile;
    }

    /**
     * @return the remove
     */
    public boolean isRemove() {
        return this.remove;
    }

    /**
     * @return the status
     */
    public boolean isStatus() {
        return this.status;
    }

    /**
     * @param check the check to set
     */
    public void setCheck(final boolean check) {
        this.check = check;
    }

    /**
     * @param mtime the mtime to setq
     */
    public void setDateTimeFilter(final Long mtime) {
        this.dateTimeFilter = mtime;
    }

    /**
     * @param generationId the generationId to set
     */
    public void setGenerationId(final List<Integer> generationId) {
        this.generationId = generationId;
    }

    /**
     * @param list the list to set
     */
    public void setList(final boolean list) {
        this.list = list;
    }

    /**
     * @param prettyJason the prettyJason to set
     */
    public void setPrettyJason(final boolean prettyJason) {
        this.prettyJason = prettyJason;
    }

    /**
     * @param profile the profile to set
     */
    public void setProfile(final boolean profile) {
        this.profile = profile;
    }

    /**
     * @param remove the remove to set
     */
    public void setRemove(final boolean remove) {
        this.remove = remove;
    }

    /**
     * @param show the show to set
     */
    public void setShow(final ArchiveObjects show) {
        this.show = show;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(final boolean status) {
        this.status = status;
    }

}
