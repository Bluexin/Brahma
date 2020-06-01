/*
 * Copyright (C) 2019-2020 Arnaud 'Bluexin' Solé
 *
 * This file is part of Brahma.
 *
 * Brahma is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Brahma is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Brahma.  If not, see <https://www.gnu.org/licenses/>.
 */

package be.bluexin.brahma.sync;

import be.bluexin.brahma.SerializedComponent;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class TestJavaComponent extends SerializedComponent {
    private String message = null;
    private int truth = 0;
    private double d = 0.0D;
    private float f = 0.0F;
    private byte b = 0;
    private boolean b2 = false;
    private long l = 0;

    @Override
    protected void reset() {
        this.setMessage(null);
        this.truth = 0;
        this.d = 0.0D;
        this.f = 0.0F;
        this.b = 0;
        this.l = 0;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTruth() {
        return truth;
    }

    public void setTruth(int truth) {
        this.truth = truth;
    }

    @Override
    public void serializeTo(@NotNull DataOutput outputStream) throws IOException {
        outputStream.writeUTF(this.getMessage());
        outputStream.writeInt(this.getTruth());
        outputStream.writeDouble(this.d);
        outputStream.writeFloat(this.f);
        outputStream.writeByte(this.b);
        outputStream.writeBoolean(this.b2);
        outputStream.writeLong(this.l);
    }

    @Override
    public void deserializeFrom(@NotNull DataInput inputStream) throws IOException {
        this.setMessage(inputStream.readUTF());
        this.setTruth(inputStream.readInt());
        this.d = inputStream.readDouble();
        this.f = inputStream.readFloat();
        this.b = inputStream.readByte();
        this.b2 = inputStream.readBoolean();
        this.l = inputStream.readLong();
    }
}
