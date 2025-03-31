
/**
 * A hash table to store Package objects
 * uses a java HashMap to look up items and quickly update.
 *
 * @author Taylor Ketterling 3/19/25
 */
import java.util.HashMap;

public class PackageHashTable {

    /** Internal hash map storing packages by their unique package ID. */
    private HashMap<Integer, Package> packages;

    /**
     * Constructs an empty PackageHashTable.
     */
    public PackageHashTable() {
        packages = new HashMap<>();
    }

    /**
     * Adds a package to the hash table.
     *
     * @param pkg the Package object to be added
     */
    public void addPackage(Package pkg) {
        packages.put(pkg.getPackageId(), pkg);
    }

    /**
     * Retrieves a package by its package ID.
     *
     * @param packageId the unique identifier of the package
     * @return the Package object if found; otherwise, null
     */
    public Package getPackage(int packageId) {
        return packages.get(packageId);
    }

    /**
     * Updates the status and deliveryTime of a package.
     *
     * @param packageId    unique identifier of the package to update
     * @param status       new status of the package (example: "Delivered")
     * @param deliveryTime time when the package was delivered (HH:MM format)
     */
    public void updatePackageStatus(int packageId, String status, String deliveryTime) {
        Package pkg = packages.get(packageId);
        if (pkg != null) {
            pkg.setStatus(status);
            pkg.setDeliveryTime(deliveryTime);
        }
    }

    /**
     * Displays details of all packages stored in the hash table.
     * Each package is printed using its ovridden toString representation.
     */
    public void displayAllPackages() {
        System.out.println("----- Package Status -----");
        for (Package pkg : packages.values()) {
            System.out.println(pkg);
        }
    }

    /**
     * Returns the size
     * 
     * @return the size of the PackageHashTable
     */
    public int size() {
        return packages.size();
    }

}
