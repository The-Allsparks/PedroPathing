/*
 * Copyright (c) 2026 Pedro Pathing
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.pedropathing.localization;

/**
 * Supplies a pose and velocity snapshot.
 *
 * <p>Implementations may read a Pinpoint, OTOS, OctoQuad, a simulator, a replay, or a
 * cycle-level cache. Values must already be in Pedro units and heading convention.
 */
@FunctionalInterface
public interface MotionStateSource {
    /**
     * Current pose and velocity.
     *
     * @return motion state in Pedro units
     */
    MotionState state();
}
