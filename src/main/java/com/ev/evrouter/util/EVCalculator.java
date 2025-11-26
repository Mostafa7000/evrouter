package com.ev.evrouter.util;

public class EVCalculator {

    private static final double GRAVITY = 9.81;
    private static final double JOULES_TO_KWH = 1.0 / 3_600_000.0;

    // Efficiency Constants (Adjust based on specific vehicle data if available)
    private static final double EFFICIENCY_MOTOR = 0.90; // 90% efficient climbing
    private static final double EFFICIENCY_REGEN = 0.65; // 65% energy recovered

    /**
     * Calculates total EV energy consumption in kWh.
     *
     * @param distanceKm Total route distance in Kilometers
     * @param ascentMeters Total ascent in Meters (from ORS)
     * @param descentMeters Total descent in Meters (from ORS)
     * @param baseConsKwhPerKm Car's rated consumption (e.g., 0.15 for 150Wh/km)
     * @param vehicleMassKg Total weight of car + passengers (e.g., 2000)
     * @return Total kWh required
     */
    public static double calculateConsumption(double distanceKm,
                                              double ascentMeters,
                                              double descentMeters,
                                              double baseConsKwhPerKm,
                                              double vehicleMassKg) {

        // 1. Base energy for rolling/drag on flat ground
        double energyFlat = distanceKm * baseConsKwhPerKm;

        // 2. Extra energy to lift the car (Potential Energy / Motor Efficiency)
        double energyAscent = (vehicleMassKg * GRAVITY * ascentMeters * JOULES_TO_KWH) / EFFICIENCY_MOTOR;

        // 3. Energy recovered going down (Potential Energy * Regen Efficiency)
        double energyDescent = (vehicleMassKg * GRAVITY * descentMeters * JOULES_TO_KWH) * EFFICIENCY_REGEN;

        // Total = Base + Cost to Climb - Recovered from Descent
        // Math.max ensures we don't return negative consumption on extreme downhills
        return Math.max(0, energyFlat + energyAscent - energyDescent);
    }
}
