/*******************************************************************************
 * Copyright (C) 2019, VMware Inc
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
package com.vmware.vmbk.type;

import org.apache.commons.lang.StringUtils;

/*
 * ******************************************************
 * Copyright VMware, Inc. 2010-2012.  All Rights Reserved.
 * ******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS # OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY # DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY # QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
public class FirstClassObjectFilterType {
    /**
     * all 4
     */
    public static int all = 0b0000000000000100;

    /**
     * ivd 256
     */
    public static int ivd = 0b0000000100000000;
    /**
     * noTag 2
     */
    public static int noTag = 0b0000000000000010;
    /**
     * tag 4096
     */
    public final static int tag = 0b0001000000000000;
    /**
     * tagElement 1
     */
    public final static int tagElement = 0b0000000000000001;
    /**
     * vapp 64
     */
    public final static int vapp = 0b0000000001000000;
    /**
     * vm 1024
     */
    public final static int vm = 0b0000010000000000;
    /**
     * k8s 8192
     */
    public final static int k8s = 0b0010000000000000;

    /**
     * any 9536
     */
    public final static int any = k8s | vm | vapp | ivd;
    // 0b0010010101000000;

    public static Integer parse(final Object filterStr, final int defaulValue) {
	if (filterStr == null) {
	    return defaulValue;
	}
	return parse(filterStr.toString(), defaulValue);
    }

    private static Integer parse(final String filterStr, final int defaulValue) {
	if (StringUtils.isEmpty(filterStr)) {
	    return defaulValue;
	} else if (filterStr.equalsIgnoreCase("vm")) {
	    return (FirstClassObjectFilterType.vm | FirstClassObjectFilterType.all);
	} else if (filterStr.equalsIgnoreCase("ivd")) {
	    return (FirstClassObjectFilterType.ivd | FirstClassObjectFilterType.all);
	} else if (filterStr.equalsIgnoreCase("vapp")) {
	    return (FirstClassObjectFilterType.vapp | FirstClassObjectFilterType.all);
	} else if (filterStr.equalsIgnoreCase("all")) {
	    return (FirstClassObjectFilterType.any | FirstClassObjectFilterType.all);
	} else if (filterStr.equalsIgnoreCase("k8s")) {
	    return (FirstClassObjectFilterType.k8s | FirstClassObjectFilterType.all);
	}
	return null;
    }

    private int filter;

    FirstClassObjectFilterType(final int filter) {
	this.filter = filter;
    }

    public int getFilter() {
	return this.filter;
    }

    public void setFilter(final int filter) {
	this.filter = filter;
    }
}
