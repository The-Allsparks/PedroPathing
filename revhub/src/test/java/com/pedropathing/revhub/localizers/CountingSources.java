package com.pedropathing.revhub.localizers;

import com.pedropathing.localization.MotionState;
import com.pedropathing.localization.MotionStateSource;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

final class CountingIntSupplier implements IntSupplier {
    int value;
    int calls;

    CountingIntSupplier() {
        this(0);
    }

    CountingIntSupplier(int value) {
        this.value = value;
    }

    @Override
    public int getAsInt() {
        calls++;
        return value;
    }
}

final class CountingDoubleSupplier implements DoubleSupplier {
    double value;
    int calls;

    CountingDoubleSupplier() {
        this(0);
    }

    CountingDoubleSupplier(double value) {
        this.value = value;
    }

    @Override
    public double getAsDouble() {
        calls++;
        return value;
    }
}

final class CountingMotionStateSource implements MotionStateSource {
    MotionState value = MotionState.zero();
    int calls;

    CountingMotionStateSource() {}

    CountingMotionStateSource(MotionState value) {
        this.value = value;
    }

    @Override
    public MotionState state() {
        calls++;
        return value;
    }
}
