package ru.mihozhereb;

public class Calculator {
    public static boolean isHit(double x, double y, int r) {
        boolean inSector = (x <= 0.0) && (y >= 0.0)
                && (x * x + y * y <= (double) r * (double) r );

        boolean inRect = (x >= 0.0) && (x <= (double) r / 2.0)
                && (y <= 0.0) && (y >= -(double) r);

        boolean inTriangle = (y <= 0.0) && (y >= -(double) r)
                && (x <= 0.0)
                && (x >= (-(y + (double) r) / 2.0));

        return inSector || inRect || inTriangle;
    }
}
