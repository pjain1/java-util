/*
 * Copyright 2011,2012 Metamarkets Group Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metamx.common.guava;

/**
 * A Sequence represents an iterable sequence of elements.  Unlike normal Iterators however, it doesn't expose
 * a way for you to extract values from it, instead you provide it with a worker (an Accumulator) and that defines
 * what happens with the data.
 *
 * This inversion of control is in place to allow the Sequence to do resource management.  It can enforce that close()
 * methods get called and other resources get cleaned up whenever processing is complete.  Without this inversion
 * it is very easy to unintentionally leak resources when iterating over something that is backed by a resource.
 *
 * Sequences also expose {#see com.metamx.common.guava.Yielder} Yielder objects which allow you to implement a
 * continuation over the Sequence.  Yielder do not offer the same guarantees of automagic resource management
 * as the accumulate method, but they are Closeable and will do the proper cleanup when close() is called on them.
 */
public interface Sequence<T>
{
  public <OutType> OutType accumulate(OutType initValue, Accumulator<OutType, T> accumulator);
  public <OutType> Yielder<OutType> toYielder(OutType initValue, YieldingAccumulator<OutType, T> accumulator);
}
