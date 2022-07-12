/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import java.util.Objects;

import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.strings.TruffleString.CopyToByteArrayNode;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.TStringUtils;
import org.truffleruby.core.string.StringGuards;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.InternalByteArray;
import com.oracle.truffle.api.strings.TruffleStringIterator;

abstract class TStringWithEncodingBase {

    public final AbstractTruffleString tstring;
    public final RubyEncoding encoding;

    protected TStringWithEncodingBase(AbstractTruffleString tstring, RubyEncoding encoding) {
        assert tstring.isCompatibleTo(encoding.tencoding);
        this.tstring = tstring;
        this.encoding = encoding;
    }

    public abstract TStringWithEncoding asImmutable();

    public final RubyEncoding getEncoding() {
        return encoding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        TStringWithEncodingBase that = (TStringWithEncodingBase) o;
        return encoding == that.encoding && tstring.equals(that.tstring);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tstring, encoding);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + tstring.toStringDebug();
    }

    public int byteLength() {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return tstring.byteLength(encoding.tencoding);
    }

    public int characterLength() {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return tstring.codePointLengthUncached(encoding.tencoding);
    }

    public InternalByteArray getInternalByteArray() {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return tstring.getInternalByteArrayUncached(encoding.tencoding);
    }

    public TruffleString.CodeRange getCodeRange() {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return tstring.getByteCodeRangeUncached(encoding.tencoding);
    }

    public TStringWithEncoding forceEncoding(RubyEncoding newEncoding) {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return new TStringWithEncoding(tstring.forceEncodingUncached(encoding.tencoding, newEncoding.tencoding),
                newEncoding);
    }

    public boolean isAsciiOnly() {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return StringGuards.is7BitUncached(tstring, encoding);
    }

    public int get(int index) {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return tstring.readByteUncached(index, encoding.tencoding);
    }

    public int getByte(int index) {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return (byte) tstring.readByteUncached(index, encoding.tencoding);
    }

    public TStringWithEncoding substring(int byteOffset, int length) {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return new TStringWithEncoding(tstring.substringByteIndexUncached(byteOffset, length, encoding.tencoding, true),
                encoding);
    }

    public String toJavaString() {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return tstring.toJavaStringUncached();
    }

    public String toJavaStringOrThrow() {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return TStringUtils.toJavaStringOrThrow(tstring, encoding);
    }

    public TruffleStringIterator createCodePointIterator() {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return tstring.createCodePointIteratorUncached(encoding.tencoding);
    }

    public boolean isSingleByteOptimizable() {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return isAsciiOnly() || encoding.jcoding.isSingleByte();
    }

    public byte[] getBytesCopy() {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return CopyToByteArrayNode.getUncached().execute(tstring, encoding.tencoding);
    }

    /** TODO: should return InternalByteArray and use that instead */
    public byte[] getBytes() {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return TStringUtils.getBytesOrCopy(tstring, encoding);
    }
}
