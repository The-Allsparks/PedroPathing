package com.pedropathing.ftc;

import com.pedropathing.geometry.CoordinateSystem;
import com.pedropathing.geometry.PedroCoordinates;
import com.pedropathing.geometry.Pose;
import com.pedropathing.math.LengthUnit;

/**
 * Inverted FTC standard coordinate system (for DECODE). Field-center offsets follow the supplied
 * follower {@link LengthUnit}.
 *
 * <p>{@link #INSTANCE} is the inch-based system (field center 72) for upstream compatibility.
 *
 * @author BeepBot99
 * @author Baron Henderson
 */
public final class InvertedFTCCoordinates implements CoordinateSystem {
    private static final InvertedFTCCoordinates[] CACHE = createCache();
    public static final InvertedFTCCoordinates INSTANCE = CACHE[LengthUnit.INCHES.ordinal()];

    private final LengthUnit unit;
    private final double fieldCenter;

    private InvertedFTCCoordinates(LengthUnit unit) {
        this.unit = unit;
        this.fieldCenter = unit.fieldCenter();
    }

    private static InvertedFTCCoordinates[] createCache() {
        InvertedFTCCoordinates[] cache = new InvertedFTCCoordinates[LengthUnit.values().length];
        for (LengthUnit unit : LengthUnit.values()) {
            cache[unit.ordinal()] = new InvertedFTCCoordinates(unit);
        }
        return cache;
    }

    public static InvertedFTCCoordinates in(LengthUnit unit) {
        return CACHE[LengthUnit.requireNonNull(unit).ordinal()];
    }

    public LengthUnit getLengthUnit() {
        return unit;
    }

    /**
     * Converts a {@link Pose} to this coordinate system from Pedro coordinates
     *
     * @param pose The {@link Pose} to convert, in the Pedro coordinate system
     * @return The converted {@link Pose}, in Inverted FTC standard coordinates
     */
    @Override
    public Pose convertFromPedro(Pose pose) {
        Pose newPose = pose.minus(new Pose(fieldCenter, fieldCenter)).rotate(Math.PI / 2, true);
        return new Pose(newPose.getX(), newPose.getY(), newPose.getHeading(), this);
    }

    /**
     * Converts a {@link Pose} to Pedro coordinates from this coordinate system
     *
     * @param pose The {@link Pose} to convert, in Inverted FTC standard coordinates
     * @return The converted {@link Pose}, in Pedro coordinate system
     */
    @Override
    public Pose convertToPedro(Pose pose) {
        Pose newPose = new Pose(pose.getX(), pose.getY(), pose.getHeading(), PedroCoordinates.INSTANCE);
        return newPose.rotate(-Math.PI / 2, true).plus(new Pose(fieldCenter, fieldCenter));
    }
}
