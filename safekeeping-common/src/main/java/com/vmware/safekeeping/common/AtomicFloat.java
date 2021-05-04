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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;

public class AtomicFloat {

    private final AtomicReference<Float> af;

    private static final BinaryOperator<Float> ADD_OPERATION = ((Float arg0, Float arg1) -> {
        return arg0 + arg1;
    });

    /**
     * Constructs an atomic float with initial value zero.
     */
    public AtomicFloat() {
        this.af = new AtomicReference<>(0F);
    }

    /**
     * Constructs an atomic float with specified initial value.
     *
     * @param value the initial value.
     */
    public AtomicFloat(final Float value) {
        this.af = new AtomicReference<>(value);
    }

    /**
     * Atomically adds a specified value to the value of this float.
     *
     * @param delta the value to add.
     * @return the updated value of this float.
     */
    public final float addAndGet(final Float delta) {
        return af.accumulateAndGet(delta, ADD_OPERATION);
    }

    /**
     * Atomically sets this float to the specified updated value if the current
     * value equals the specified expected value.
     *
     * @param expect the expected value.
     * @param update the updated value.
     * @return true, if successfully set; false, if the current value was not equal
     *         to the expected value.
     */
    public final boolean compareAndSet(final float expect, final float update) {
        return af.compareAndSet(expect, update);
    }

    /**
     * Atomically decrements by one the value of this float.
     *
     * @return the updated value of this float.
     */
    public final float decrementAndGet() {
        return addAndGet(-1.0F);
    }

    public double doubleValue() {
        return get();
    }

    public float floatValue() {
        return get();
    }

    /**
     * Gets the current value of this float.
     *
     * @return the current value.
     */
    public final Float get() {
        return this.af.get();
    }

    /**
     * Atomically adds a specified value to the value of this float.
     *
     * @param delta the value to add.
     * @return the previous value of this float.
     */
    private final float getAndAdd(final Float delta) {
        return af.getAndAccumulate(delta, ADD_OPERATION);
    }

    /**
     * Atomically decrements by one the value of this float.
     *
     * @return the previous value of this float.
     */
    public final float getAndDecrement() {
        return getAndAdd(-1.0F);
    }

    /**
     * Atomically increments by one the value of this float.
     *
     * @return the previous value of this float.
     */
    public final float getAndIncrement() {
        return getAndAdd(1.0F);
    }

    /**
     * Atomically sets the value of this float and returns its old value.
     *
     * @param value the new value.
     * @return the old value.
     */
    public final float getAndSet(final Float value) {
        return af.getAndSet(value);
    }

    /**
     * Atomically increments by one the value of this float.
     *
     * @return the updated value of this float.
     */
    public final Float incrementAndGet() {
        return addAndGet(1.0F);
    }

    /**
     * Eventually sets to the given value.
     * 
     * @param newValue
     */
    public void lazySet(Float newValue) {
        af.lazySet(newValue);
    }

    public int intValue() {
        return get().intValue();
    }

    public long longValue() {
        return get().longValue();
    }

    /**
     * Sets the value of this float.
     *
     * @param value the new value.
     */
    public final void set(final float value) {
        this.af.set(value);
    }

    @Override
    public String toString() {
        return Float.toString(get());
    }

    /**
     * Atomically sets this float to the specified updated value if the current
     * value equals the specified expected value.
     *
     * @param expect the expected value.
     * @param update the updated value.
     * @return true, if successfully set; false, if the current value was not equal
     *         to the expected value.
     */
    public final boolean weakCompareAndSet(final float expect, final float update) {
        return this.af.weakCompareAndSet(expect, update);
    }
}
