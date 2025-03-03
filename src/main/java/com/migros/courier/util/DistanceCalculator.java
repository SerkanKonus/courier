package com.migros.courier.util;

/**
 * İki coğrafi koordinat noktası arasındaki mesafeyi hesaplayan yardımcı sınıf.
 * Haversine formülünü kullanarak dünya üzerindeki iki nokta arasındaki
 * en kısa mesafeyi (ortodrom) metre cinsinden hesaplar.
 * <p>
 * Haversine formülü, dünyanın küresel şeklini dikkate alarak
 * iki nokta arasındaki kuş uçuşu mesafeyi hesaplamak için kullanılır.
 */
public class DistanceCalculator {
    private static final double EARTH_RADIUS = 6371; // Dünya'nın yarıçapı (km)

    /**
     * İki koordinat noktası arasındaki mesafeyi metre cinsinden hesaplar.
     *
     * @param lat1 Başlangıç noktasının enlemi
     * @param lon1 Başlangıç noktasının boylamı
     * @param lat2 Bitiş noktasının enlemi
     * @param lon2 Bitiş noktasının boylamı
     * @return İki nokta arasındaki mesafe (metre cinsinden)
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c * 1000;
    }
}
