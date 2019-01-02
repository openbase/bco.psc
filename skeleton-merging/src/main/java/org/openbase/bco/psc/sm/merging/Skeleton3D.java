package org.openbase.bco.psc.sm.merging;

/*
 * -
 * #%L
 * BCO PSC Skeleton Merging
 * %%
 * Copyright (C) 2016 - 2019 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import javafx.geometry.Point3D;
import org.openbase.bco.psc.lib.pointing.Joints;
import org.openbase.bco.psc.lib.pointing.PostureFunctions;

/**
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class Skeleton3D extends ArrayList<Joint3D> {

    private Point3D weightedMean;
    private Point3D mean;
    private double totalConfidence = Double.NaN;

    public Skeleton3D() {
        super();
    }

    public Skeleton3D(final int size) {
        super();
        for (int i = 0; i < size; i++) {
            add(null);
        }
    }

    public Skeleton3D(final List<Joint3D> other) {
        super(other);
    }

    public void set(final Joints joint, final Joint3D element) {
        set(joint.getValue(), element);
    }

    public Joint3D get(final Joints joint) {
        return get(joint.getValue());
    }

    public Joint3D getOther(final Joints joint) {
        return get(PostureFunctions.otherJoint(joint));
    }

    public Joint3D get(final Joints joint, final boolean mirrored) {
        if (mirrored) {
            return getOther(joint);
        }
        return get(joint);
    }

    public Skeleton3D multiply(final double factor) {
        return new Skeleton3D(stream().map(j -> j.multiply(factor)).collect(Collectors.toList()));
    }

    public Skeleton3D addPositions(final Skeleton3D other) {
        ListIterator<Joint3D> otherIterator = other.listIterator();
        return new Skeleton3D(stream()
                .map(j -> j.addPosition(otherIterator.next()))
                .collect(Collectors.toList())
        );
    }

    public Skeleton3D mirrored() {
        final Skeleton3D mirrored = new Skeleton3D(Joints.values().length);
        for (final Joints joint : Joints.values()) {
            mirrored.set(joint, get(joint, true));
        }
        return mirrored;
    }

    public double getTotalConfidence() {
        if (totalConfidence == Double.NaN) {
            createTotalConfidence();
        }
        return totalConfidence;
    }

    public Point3D getWeightedMean() {
        if (weightedMean == null) {
            createWeightedMean();
        }
        return weightedMean;
    }

    public Point3D getMean() {
        if (mean == null) {
            createMean();
        }
        return mean;
    }

    private void createTotalConfidence() {
        totalConfidence = stream().mapToDouble(Joint3D::getConfidence).sum();
    }

    private void createWeightedMean() {
        assert this.size() == Joints.values().length;
        weightedMean = stream()
                .map(j -> j.getPosition().multiply(j.getConfidence()))
                .reduce(Point3D.ZERO, Point3D::add)
                .multiply(1.0 / getTotalConfidence());
    }

    private void createMean() {
        assert this.size() == Joints.values().length;
        mean = stream()
                .map(Joint3D::getPosition)
                .reduce(Point3D.ZERO, Point3D::add)
                .multiply(1.0 / this.size());
    }

    public double weightedMeanDistance(final Skeleton3D other) {
        return getWeightedMean().distance(other.getWeightedMean());
    }

    public double meanDistance(final Skeleton3D other) {
        return getMean().distance(other.getMean());
    }

    public double distance(final Skeleton3D other) {
        assert this.size() == other.size();
        double totalConf = 0.0;
        double totalDist = 0.0;
        // TODO: or sqrt of sum of squares like euclidean?! + weights stay...
        // TODO: other way with the distance between the means... if only calculated once would save a lot of calculations...
        final ListIterator<Joint3D> first = this.listIterator(), second = other.listIterator();
        while (first.hasNext() && second.hasNext()) {
            final Joint3D f = first.next();
            final Joint3D s = second.next();
            final double conf = f.getConfidence() * s.getConfidence();
            totalConf += conf;
            totalDist += f.getPosition().distance(s.getPosition()) * conf;
        }
        return totalDist / totalConf;
    }

    public double jointDistance(final Skeleton3D other, final boolean mirrored) {
        // TODO: or sqrt of sum of squares like euclidean?! + weights stay...
        // TODO: other way with the distance between the means... if only calculated once would save a lot of calculations...
        double totalConf = 0.0;
        double totalDist = 0.0;
        for (final Joints joint : Joints.values()) {
            final Joint3D thisJoint = this.get(joint);
            final Joint3D otherJoint = other.get(joint, mirrored);
            final double conf = thisJoint.getConfidence() * otherJoint.getConfidence();
            totalConf += conf;
            totalDist += thisJoint.getPosition().distance(otherJoint.getPosition()) * conf;
        }
        return totalDist / totalConf;
    }
}
