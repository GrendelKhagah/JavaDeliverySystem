/**
 * Represents distances between delivery locations using a graph structure.
 * Distances are symmetric; distance(A, B) is always equal to distance(B, A).
 * 
 * @author Taylor Ketterling 3/19/25
 */
import java.util.HashMap;

public class DistanceGraph {

    /** Nested HashMap storing distances between two addresses. */
    private final HashMap<String, HashMap<String, Double>> distances;

    /**
     * Constructs an empty DistanceGraph.
     */
    public DistanceGraph() {
        distances = new HashMap<>();
    }

    /**
     * Adds a distance between two locations. Automatically handles symmetry.
     *
     * @param address1 First location's address.
     * @param address2 Second location's address.
     * @param distance Distance between the two addresses in miles.
     */
    public void addDistance(String address1, String address2, double distance) {
        distances.computeIfAbsent(address1, k -> new HashMap<>()).put(address2, distance);
        distances.computeIfAbsent(address2, k -> new HashMap<>()).put(address1, distance);
    }

    /**
     * Retrieves the distance between two addresses.
     *
     * @param address1 First location's address.
     * @param address2 Second location's address.
     * @return distance between the locations, or -1 if not found.
     */
    public double getDistance(String address1, String address2) {
        if (distances.containsKey(address1) && distances.get(address1).containsKey(address2)) {
            return distances.get(address1).get(address2);
        }
        // Address not found or distance undefined
        System.out.println("Distance not found between " + address1 + " and " + address2);
        return -1;
    }

    /**
     * Prints all the stored distances
     * needed for debugging 
     * >> Distance not found between HUB and 195 W Oakland Ave
     * >> Distance not found between HUB and 2530 S 500 E
     */
    public void printAllDistances() {
        System.out.println("\n--- All Stored Distances ---");
        for (String from : distances.keySet()) {
            HashMap<String, Double> map = distances.get(from);
            for (String to : map.keySet()) {
                System.out.printf("%s -> %s : %.2f miles\n", from, to, map.get(to));
            }
        }
        System.out.println("----------------------------\n");
    }
    
}
