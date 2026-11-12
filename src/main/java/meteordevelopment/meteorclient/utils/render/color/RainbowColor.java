/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.render.color;

public class RainbowColor extends Color {
    private double speed;
    private double progress;
    private static final float[] hsb = new float[3];

    private static final Color[] gradient = new Color[] {
        new Color(91, 206, 250),
        new Color(245, 169, 184),
        new Color(255, 255, 255),
        new Color(245, 169, 184)
    };

    public RainbowColor() {
        super();
    }

    public double getSpeed() {
        return speed;
    }

    public RainbowColor setSpeed(double speed) {
        this.speed = speed;
        return this;
    }

    public RainbowColor getNext() {
        return getNext(1);
    }

    public RainbowColor getNext(double delta) {
        if (speed > 0) {
            progress += speed * delta;
            if (progress >= 1.0) {
                progress -= 1.0;
            }
            if (progress <= -1.0) {
                progress += 1.0;
            }

            int c = getInterpolation(progress);
            r = Color.toRGBAR(c);
            g = Color.toRGBAG(c);
            b = Color.toRGBAB(c);
        }
        return this;
    }

    public static int getInterpolation(double progress) {
        progress %= 1.0;
        if (progress < 0.0) {
            progress += 1.0;
        }
        progress *= gradient.length;

        Color startColor = gradient[(int)Math.floor(progress)];
        Color endColor = gradient[(int)Math.ceil(progress) % gradient.length];
        double fac = sigmoid(progress % 1.0);

        int r = (int)((1.0 - fac)*(double)startColor.r + fac*(double)endColor.r);
        int g = (int)((1.0 - fac)*(double)startColor.g + fac*(double)endColor.g);
        int b = (int)((1.0 - fac)*(double)startColor.b + fac*(double)endColor.b);
        return (r << 16) | (g << 8) | b;
    }

    private static double sigmoid(double x) {
        double K = 3.0;
        double a = Math.pow(x, K);
        double b = Math.pow(1.0 - x, K);
        return a / (a + b);
    }

    public RainbowColor set(RainbowColor color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
        this.speed = color.speed;
        this.progress = color.progress;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        return Double.compare(((RainbowColor) o).speed, speed) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(speed);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(progress);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
