package net.runelite.client.plugins;

import java.awt.Polygon;
import javax.annotation.Nonnull;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;

public class BLPerspective {
    public static Polygon getCanvasTileAreaPoly(@Nonnull Client client, @Nonnull LocalPoint localLocation, int size) {
        return getCanvasTileAreaPoly(client, localLocation, size, 0, true);
    }

    public static Polygon getCanvasTileAreaPoly(@Nonnull Client client, @Nonnull LocalPoint localLocation, int size, int borderOffset) {
        return getCanvasTileAreaPoly(client, localLocation, size, borderOffset, true);
    }

    public static Polygon getCanvasTileAreaPoly(@Nonnull Client client, @Nonnull LocalPoint localLocation, int size, boolean centered) {
        return getCanvasTileAreaPoly(client, localLocation, size, 0, centered);
    }

    public static Polygon getCanvasTileAreaPoly(@Nonnull Client client, @Nonnull LocalPoint localLocation, int size, int borderOffset, boolean centered) {
        int swX, swY, neX, neY, plane = client.getPlane();
        if (centered) {
            swX = localLocation.getX() - size * (128 + borderOffset) / 2;
            swY = localLocation.getY() - size * (128 + borderOffset) / 2;
            neX = localLocation.getX() + size * (128 + borderOffset) / 2;
            neY = localLocation.getY() + size * (128 + borderOffset) / 2;
        } else {
            swX = localLocation.getX() - (128 + borderOffset) / 2;
            swY = localLocation.getY() - (128 + borderOffset) / 2;
            neX = localLocation.getX() - (128 + borderOffset) / 2 + size * (128 + borderOffset);
            neY = localLocation.getY() - (128 + borderOffset) / 2 + size * (128 + borderOffset);
        }
        int seX = swX;
        int seY = neY;
        int nwX = neX;
        int nwY = swY;
        byte[][][] tileSettings = client.getTileSettings();
        int sceneX = localLocation.getSceneX();
        int sceneY = localLocation.getSceneY();
        if (sceneX < 0 || sceneY < 0 || sceneX >= 104 || sceneY >= 104)
            return null;
        int tilePlane = plane;
        if (plane < 3 && (tileSettings[1][sceneX][sceneY] & 0x2) == 2)
            tilePlane = plane + 1;
        int swHeight = getHeight(client, swX, swY, tilePlane);
        int nwHeight = getHeight(client, nwX, nwY, tilePlane);
        int neHeight = getHeight(client, neX, neY, tilePlane);
        int seHeight = getHeight(client, seX, seY, tilePlane);
        Point p1 = Perspective.localToCanvas(client, swX, swY, swHeight);
        Point p2 = Perspective.localToCanvas(client, nwX, nwY, nwHeight);
        Point p3 = Perspective.localToCanvas(client, neX, neY, neHeight);
        Point p4 = Perspective.localToCanvas(client, seX, seY, seHeight);
        if (p1 == null || p2 == null || p3 == null || p4 == null)
            return null;
        Polygon poly = new Polygon();
        poly.addPoint(p1.getX(), p1.getY());
        poly.addPoint(p2.getX(), p2.getY());
        poly.addPoint(p3.getX(), p3.getY());
        poly.addPoint(p4.getX(), p4.getY());
        return poly;
    }

    private static int getHeight(@Nonnull Client client, int localX, int localY, int plane) {
        int sceneX = localX >> 7;
        int sceneY = localY >> 7;
        if (sceneX >= 0 && sceneY >= 0 && sceneX < 104 && sceneY < 104) {
            int[][][] tileHeights = client.getTileHeights();
            int x = localX & 0x7F;
            int y = localY & 0x7F;
            int var8 = x * tileHeights[plane][sceneX + 1][sceneY] + (128 - x) * tileHeights[plane][sceneX][sceneY] >> 7;
            int var9 = tileHeights[plane][sceneX][sceneY + 1] * (128 - x) + x * tileHeights[plane][sceneX + 1][sceneY + 1] >> 7;
            return (128 - y) * var8 + y * var9 >> 7;
        }
        return 0;
    }
}
