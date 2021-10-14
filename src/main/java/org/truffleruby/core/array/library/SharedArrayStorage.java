/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array.library;

import java.util.Set;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.library.ArrayStoreLibrary.ArrayAllocator;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;
import org.truffleruby.language.objects.shared.WriteBarrierNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(ArrayStoreLibrary.class)
@GenerateUncached
@ImportStatic(ArrayGuards.class)
public class SharedArrayStorage implements ObjectGraphNode {

    public final Object storage;

    public SharedArrayStorage(Object backingStore) {
        this.storage = backingStore;
    }

    @ExportMessage
    protected static boolean accepts(SharedArrayStorage store,
            @CachedLibrary(limit = "1") ArrayStoreLibrary backingStores) {
        return backingStores.accepts(store.storage);
    }

    @ExportMessage
    protected Object read(int index,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.read(storage, index);
    }

    @ExportMessage
    protected void write(int index, Object value,
            @Shared("barrier") @Cached WriteBarrierNode writeBarrierNode,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        writeBarrierNode.executeWriteBarrier(value);
        stores.write(storage, index, value);
    }

    @ExportMessage
    protected void fill(int start, int length, Object value,
                        @Shared("barrier") @Cached WriteBarrierNode writeBarrierNode,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        writeBarrierNode.executeWriteBarrier(value);
        stores.fill(storage, start, length, value);
    }

    @ExportMessage
    protected boolean acceptsValue(Object value,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.acceptsValue(storage, value);
    }

    @ExportMessage
    protected boolean acceptsAllValues(Object otherStore,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.acceptsAllValues(storage, otherStore);
    }

    @ExportMessage
    protected boolean isMutable(
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.isMutable(storage);
    }

    @ExportMessage
    protected boolean isPrimitive(
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.isPrimitive(storage);
    }

    @ExportMessage
    public boolean isShared() {
        return true;
    }

    @ExportMessage
    @TruffleBoundary
    protected String toString(
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return String.format("Shared storage of (%s)", stores.toString(storage));
    }

    @ExportMessage
    protected int capacity(
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.capacity(storage);
    }

    @ExportMessage
    protected Object expand(int capacity,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return new SharedArrayStorage(stores.expand(storage, capacity));
    }

    @ExportMessage
    protected Object extractRange(int start, int end,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.extractRange(storage, start, end);
    }

    @ExportMessage
    protected Object[] boxedCopyOfRange(int start, int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.boxedCopyOfRange(storage, start, length);
    }

    @ExportMessage
    protected void copyContents(int srcStart, Object destStore, int destStart, int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary srcStores) {
        srcStores.copyContents(storage, srcStart, destStore, destStart, length);
    }

    @ExportMessage
    protected Object toJavaArrayCopy(int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.toJavaArrayCopy(storage, length);
    }

    @ExportMessage
    protected void sort(int size,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        stores.sort(storage, size);
    }

    @ExportMessage
    protected Iterable<Object> getIterable(int from, int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.getIterable(storage, from, length);
    }

    @ExportMessage
    protected ArrayAllocator generalizeForValue(Object newValue,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return new SharedArrayAllocator(stores.generalizeForValue(storage, newValue));
    }

    @ExportMessage
    protected ArrayAllocator generalizeForStore(Object newStore,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return new SharedArrayAllocator(stores.generalizeForStore(newStore, storage));
    }

    @ExportMessage
    public ArrayAllocator generalizeForSharing(@CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.generalizeForSharing(storage);
    }

    @ExportMessage
    protected Object allocateForNewValue(Object newValue, int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return new SharedArrayStorage(stores.allocateForNewValue(storage, newValue, length));
    }

    @ExportMessage
    protected Object allocateForNewStore(Object newStore, int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return new SharedArrayStorage(stores.allocateForNewStore(storage, newStore, length));
    }

    @ExportMessage
    protected boolean isDefaultValue(Object value,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.isDefaultValue(storage, value);
    }

    @ExportMessage
    protected ArrayAllocator allocator(
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return new SharedArrayAllocator(stores.allocator(storage));
    }

    public boolean hasObjectArrayStorage() {
        return storage != null && storage.getClass() == Object[].class;
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        if (hasObjectArrayStorage()) {
            final Object[] objectArray = (Object[]) storage;

            for (int i = 0; i < objectArray.length; i++) {
                final Object value = objectArray[i];
                if (ObjectGraph.isRubyObject(value)) {
                    reachable.add(value);
                }
            }
        } else if (storage instanceof ObjectGraphNode) {
            ((ObjectGraphNode) storage).getAdjacentObjects(reachable);
        }
    }

    static final ArrayAllocator SHARED_ZERO_LENGTH_ARRAY_ALLOCATOR = new SharedArrayAllocator(
            ZeroLengthArrayStore.ZERO_LENGTH_ALLOCATOR);
    static final ArrayAllocator SHARED_INTEGER_ARRAY_ALLOCATOR = new SharedArrayAllocator(IntegerArrayStore.INTEGER_ARRAY_ALLOCATOR);
    static final ArrayAllocator SHARED_LONG_ARRAY_ALLOCATOR = new SharedArrayAllocator(LongArrayStore.LONG_ARRAY_ALLOCATOR);
    static final ArrayAllocator SHARED_DOUBLE_ARRAY_ALLOCATOR = new SharedArrayAllocator(DoubleArrayStore.DOUBLE_ARRAY_ALLOCATOR);
    static final ArrayAllocator SHARED_OBJECT_ARRAY_ALLOCATOR = new SharedArrayAllocator(ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR);

    private static class SharedArrayAllocator extends ArrayAllocator {

        private final ArrayAllocator storageAllocator;

        SharedArrayAllocator(ArrayAllocator storageAllocator) {
            this.storageAllocator = storageAllocator;
        }

        @Override
        public SharedArrayStorage allocate(int capacity) {
            return new SharedArrayStorage(storageAllocator.allocate(capacity));
        }

        @Override
        public boolean accepts(Object value) {
            return storageAllocator.accepts(value);
        }

        @Override
        public boolean specializesFor(Object value) {
            return storageAllocator.specializesFor(value);
        }

        @Override
        public boolean isDefaultValue(Object value) {
            return storageAllocator.isDefaultValue(value);
        }
    }
}
