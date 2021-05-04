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
package com.vmware.safekeeping.common;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Auxiliary class which provides atomic operations on enumerations. Internally,
 * the class uses an {@link AtomicReference} object to guarantee the atomicity.
 * <p>
 * This class is thread-safe.
 *
 */
public final class AtomicEnum<T extends Enum<T>> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3830497177517242975L;
    /**
     * The atomic reference which provides the atomicity internally.
     */
    private final AtomicReference<T> ref;

    /**
     * Constructs a new atomic enumeration object.
     *
     * @param initialValue the initial value of the enumeration
     */
    public AtomicEnum(final T initialValue) {

        this.ref = new AtomicReference<>(initialValue);
    }

    /**
     * Atomically set the value to the given updated value if the current value ==
     * the expected value.
     *
     * @param expect the expected value
     * @param update the new value
     * @return <code>true</code> if successful, <code>false</code> otherwise
     */
    public boolean compareAndSet(final T expect, final T update) {

        return this.ref.compareAndSet(expect, update);
    }

    /**
     * Gets the current value.
     *
     * @return the current value.
     */
    public T get() {

        return this.ref.get();
    }

    /**
     * Sets the given value and returns the old value.
     *
     * @param newValue the new value
     * @return the previous value
     */
    public T getAndSet(final T newValue) {

        return this.ref.getAndSet(newValue);
    }

    /**
     * Sets to the given value.
     *
     * @param newValue the new value
     */
    public void set(final T newValue) {

        this.ref.set(newValue);
    }
}
