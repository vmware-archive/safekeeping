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
package com.vmware.safekeeping.core.profile.dataclass;

import java.util.ArrayList;
import java.util.List;

public class FcoNetwork {
    private String name;
    private String type;
    private String moref;
    private String key;
    private int index;
    private List<Integer> vmNics;

    public FcoNetwork() {
        this.vmNics = new ArrayList<>();
    }

    /**
     * Clone constructor
     *
     * @param src
     */
    public FcoNetwork(final FcoNetwork src) {
        this.name = src.name;
        this.type = src.type;
        this.moref = src.moref;
        this.key = src.key;
        this.index = src.index;
        this.vmNics = new ArrayList<>(src.vmNics);
    }

    public int getIndex() {
        return this.index;
    }

    public String getKey() {
        return this.key;
    }

    /**
     * @return the moref
     */
    public String getMoref() {
        return this.moref;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the type
     */
    public String getType() {
        return this.type;
    }

    public List<Integer> getVmNics() {
        return this.vmNics;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    /**
     * @param moref the moref to set
     */
    public void setMoref(final String moref) {
        this.moref = moref;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @param type the type to set
     */
    public void setType(final String type) {
        this.type = type;
    }

    public void setVmNics(final List<Integer> vmNics) {
        this.vmNics = vmNics;
    }
}
