
/**
 * Represents a delivery truck in the WGUPS routing system.
 * Handles package loading, delivering packages, tracking mileage,
 * current location, and current time.
 * 
 * Assumptions (from assignment givens):
 * <ul>
 *   <li>Truck capacity: max 16 packages</li>
 *   <li>Average speed: 18 miles/hour</li>
 *   <li>Infinite fuel capacity, instantaneous loading/unloading</li>
 * </ul>
 * @author Taylor Kettering 3/19/25
 */

import java.util.ArrayList;

public class Truck {

    /** Maximum number of packages a truck can hold at a time. */
    public static final int MAX_CAPACITY = 16;

    /** Average speed in miles per hour(mph, freedom units) */
    public static final double SPEED_MPH = 18.0;

    /** Packages currently loaded on the truck. */
    private ArrayList<Package> loadedPackages;

    /** Total mileage driven by the truck. */
    private double mileage;

    /** Current location/address of the truck. */
    private String currentLocation;

    /** Current timestamp of truck operations in HH:MM format. */
    private String currentTime;

    /**
     * Constructor:
     * Constructs a Truck, initializes starting location at HUB and time at 08:00
     * AM.
     */
    public Truck() {
        loadedPackages = new ArrayList<>();
        mileage = 0.0;
        currentLocation = "HUB";
        currentTime = "08:00";
    }

    /**
     * Loads a package onto the truck if capacity allows.
     *
     * @param pkg Package to load.
     */
    public void loadPackage(Package pkg) {
        if (loadedPackages.size() < MAX_CAPACITY) {
            loadedPackages.add(pkg);
            pkg.setStatus("En route");
        } else {
            System.out.println("Truck capacity reached, cannot load package #" + pkg.getPackageId());
        }
    }

    /**
     * Delivers a package, updates mileage, location, and delivery timestamp.
     *
     * @param pkg               Package being delivered.
     * @param distanceToPackage Distance to the package's delivery location.
     */
    public void deliverPackage(Package pkg, double distanceToPackage) {
        mileage += distanceToPackage;
        pkg.setStatus("Delivered");
        pkg.setDeliveryTime(currentTime);
        loadedPackages.remove(pkg);
        currentLocation = pkg.getAddress();

        double timeTraveled = distanceToPackage / SPEED_MPH;
        updateCurrentTime(timeTraveled);
    }

    /**
     * Sends the truck home.
     *
     * @param distanceToHome Distance to the package's delivery Hub.
     */
    public void goHome(double distanceToHome) {
        mileage += distanceToHome;
        currentLocation = "HUB";

        double timeTraveled = distanceToHome / SPEED_MPH;
        updateCurrentTime(timeTraveled);
    }

    /**
     * Updates the truck's current time based on hours traveled.
     *
     * @param hours Hours traveled to next destination.
     */
    private void updateCurrentTime(double hours) {
        String[] timeParts = currentTime.split(":");
        int hr = Integer.parseInt(timeParts[0]);
        int min = Integer.parseInt(timeParts[1]);

        int totalMinutes = (int) (hours * 60) + (hr * 60) + min;
        hr = totalMinutes / 60;
        min = totalMinutes % 60;

        currentTime = String.format("%02d:%02d", hr, min);
    }

    // Getters for Truck's internal state
    /** @return mileage of truck */
    public double getMileage() {
        return mileage;
    }

    /** @return current location */
    public String getCurrentLocation() {
        return currentLocation;
    }

    /** @return current time */
    public String getCurrentTime() {
        return currentTime;
    }

    /** @return packages on truck */
    public ArrayList<Package> getLoadedPackages() {
        return loadedPackages;
    }

    // No setters, There is a delivery method that 'Sets' things

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

}
