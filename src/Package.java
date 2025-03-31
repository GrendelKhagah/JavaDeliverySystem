import java.util.ArrayList;
import java.util.List;

/**
 * Package object for managing package delivery within the WGUPS network
 *
 * @author Taylor Ketterling 3/19/25
 */
public class Package {
    // Variables
    /** Unique ID assigned to the package. */
    private int packageId;

    /** Street address for package delivery. */
    private String address;

    /** City for package delivery. */
    private String city;

    /** State abbreviation for delivery location. */
    private String state;

    /** Zip code for delivery location. */
    private String zip;

    /** Delivery deadline (e.g., "EOD", "10:30 AM"). */
    private String deadline;

    /** Weight of the package in kilograms. */
    private double weight;

    /** Current status of the package ("At hub", "En route", "Delivered"). */
    private String status;

    /** Recorded time when package was delivered (formatted as HH:MM). */
    private String deliveryTime;

    /** Special notes or constraints associated with this package. */
    private String specialNote;

    /** If the package is grouped with other packages */
    private List<Integer> groupWith = new ArrayList<>();

    // Constructor
    /**
     * Constructs a new Package with given delivery details.
     * packageId must be unique, system does not set unique ID, but receives a
     * unique ID
     *
     * @param packageId   unique identifier for the package
     * @param address     street address for delivery
     * @param city        city name
     * @param state       state abbreviation
     * @param zip         zip code
     * @param deadline    delivery deadline
     * @param weight      package weight
     * @param specialNote special instructions or constraints
     */
    public Package(int packageId, String address, String city, String state,
            String zip, String deadline, double weight, String specialNote) {
        this.packageId = packageId;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.deadline = deadline;
        this.weight = weight;
        this.status = "At hub";
        this.deliveryTime = "N/A";
        this.specialNote = specialNote;

        if (specialNote.contains("Must be delivered with")) {
            String[] parts = specialNote.split("with");
            if (parts.length > 1) {
                String[] ids = parts[1].replaceAll("[^0-9,]", "").split(",");
                for (String idStr : ids) {
                    try {
                        groupWith.add(Integer.parseInt(idStr.trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
    }

    // Getters
    /** @return unique package ID */
    public int getPackageId() {
        return packageId;
    }

    /** @return street address of the package */
    public String getAddress() {
        return address;
    }

    /** @return city for delivery */
    public String getCity() {
        return city;
    }

    /** @return state abbreviation for delivery */
    public String getState() {
        return state;
    }

    /** @return zip code for delivery */
    public String getZip() {
        return zip;
    }

    /** @return deeadline of delivery */
    public String getDeadline() {
        return deadline;
    }

    /** @return weight of package */
    public double getWeight() {
        return weight;
    }

    /** @return current package status */
    public String getStatus() {
        return status;
    }

    /** @return time when the package was delivered */
    public String getDeliveryTime() {
        return deliveryTime;
    }

    /** @return special notes or constraints for the package */
    public String getSpecialNote() {
        return specialNote;
    }

    /** @return package group */
    public List<Integer> getGroupWith() {
        return groupWith;
    }

    // Setters
    /**
     * Sets/updates the address.
     * 
     * @param address new address for delivery
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Sets/updates the package status.
     * 
     * @param status current status ("At hub", "En route", "Delivered")
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Sets/updates the delivery time.
     * 
     * @param deliveryTime the exact time the package was delivered
     */
    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    /**
     * Provides a readable representation of the Package Object for printing
     * 
     * @return formatted package details
     */
    @Override
    public String toString() {
        return "Package #" + packageId + " to " + address + ", Status: " + status + ", Delivered at: " + deliveryTime;
    }
}
