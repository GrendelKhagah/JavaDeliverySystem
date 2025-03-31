import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main entry point for the WGUPS Delivery System.
 * Initializes packages, trucks, and routing logic for package delivery.
 *
 * Note: Replace manual data entries with parsed Excel data for final
 * implementation.
 */
public class App {
    public static void main(String[] args) {
        System.out.print("Booting WGUPS Delivery System..");

        // Initialize PackageHashTable and DistanceGraph
        PackageHashTable pkgTable = new PackageHashTable();
        System.out.print("..");
        DistanceGraph graph = new DistanceGraph();
        System.out.println("Done");

        // Initialization (completes with data from csv file WGUPS_Package_File.csv [csv
        // copy of WGUPS Package File.xlsx])
        System.out.print("Loading Packages..");
        loadPackagesFromCSV("WGUPS_Package_File.csv", pkgTable);
        System.out.println("..Done");

        // Initialization of distances
        System.out.print("Loading Distance Matrix..");
        loadDistancesFromCSV("WGUPS_Distance_Table.csv", graph);
        System.out.println("..Done");

        // Debug test
        // prints all the distances
        // graph.printAllDistances();

        // Initialize trucks
        Truck truck1 = new Truck();
        Truck truck2 = new Truck();
        Truck truck3 = new Truck();

        // Load packages into trucks
        loadTrucks(pkgTable, truck1, truck2, truck3);

        // Deliver packages using Nearest Neighbor routing algo
        deliverAllPackages(truck1, pkgTable, graph, true);
        deliverAllPackages(truck2, pkgTable, graph);

        // Set truck3's start time to the earlier finish time of truck1 or truck2
        if (timeToMinutes(truck1.getCurrentTime()) < timeToMinutes(truck2.getCurrentTime())) {
            truck3.setCurrentTime(truck1.getCurrentTime());
        } else {
            truck3.setCurrentTime(truck2.getCurrentTime());
        }

        System.out.println("Truck3 departs at " + truck3.getCurrentTime());
        deliverAllPackages(truck3, pkgTable, graph);

        // improvement note: make method for managing trucks, current does not check to
        // see if truck1 needs to be filled up and cued to be sent back out

        // Display final delivery statuses of all packages
        pkgTable.displayAllPackages();

        // Display truck mileages
        System.out.println("----- Truck Summary -----");
        System.out.printf("Truck 1 mileage: %.2f miles, Returned to HUB at: %s%n",
                truck1.getMileage(), truck1.getCurrentTime());
        System.out.printf("Truck 2 mileage: %.2f miles, Returned to HUB at: %s%n",
                truck2.getMileage(), truck2.getCurrentTime());
        System.out.printf("Truck 3 mileage: %.2f miles, Returned to HUB at: %s%n",
                truck3.getMileage(), truck3.getCurrentTime());
        System.out.printf("Total Miles driven by Trucks: %.2f miles%n",
                truck1.getMileage() + truck2.getMileage() + truck3.getMileage());
    }

    /**
     * Loads packages into available trucks while remaining within capacity limits
     * and driver constraints. checks deliver restrains such as Delivery Deadline
     *
     * Only two trucks can be on the road at any given time due to driver
     * limitations.
     * This method loads the first 32 packages into truck1 and truck2 (16 each),
     * and the remaining packages are staged for truck3.
     *
     * @param pkgTable the hash table containing all packages
     * @param truck1   first truck (active)
     * @param truck2   second truck (active)
     * @param truck3   third truck (staged until a driver returns)
     */
    private static void loadTrucks(PackageHashTable pkgTable, Truck truck1, Truck truck2, Truck truck3) {
        List<Package> allPackages = new ArrayList<>();
        Set<Integer> loadedIds = new HashSet<>();

        for (int i = 1; i <= pkgTable.size(); i++) {
            Package pkg = pkgTable.getPackage(i);
            if (pkg != null)
                allPackages.add(pkg);
        }

        // Sort by deadline priority: earliest deadlines first, EOD last
        allPackages.sort((a, b) -> {
            String dA = a.getDeadline();
            String dB = b.getDeadline();

            if (dA.equals("EOD") && !dB.equals("EOD"))
                return 1;
            if (!dA.equals("EOD") && dB.equals("EOD"))
                return -1;
            if (!dA.equals("EOD") && !dB.equals("EOD"))
                return timeToMinutes(dA) - timeToMinutes(dB);
            return 0;
        });

        for (Package pkg : allPackages) {
            // verify package is not already loaded
            if (loadedIds.contains(pkg.getPackageId()))
                continue;

            // find which truck has space starting with 1
            Truck targetTruck = null;
            if (truck1.getLoadedPackages().size() < Truck.MAX_CAPACITY) {
                targetTruck = truck1;
            } else if (truck2.getLoadedPackages().size() < Truck.MAX_CAPACITY) {
                targetTruck = truck2;
            } else if (truck3.getLoadedPackages().size() < Truck.MAX_CAPACITY) {
                targetTruck = truck3;
            }

            if (targetTruck == null) {
                System.out.println("All trucks full, cannot load more at this time.");
                break; // All trucks full
            }

            // Handle grouped deliveries
            List<Integer> groupWith = pkg.getGroupWith();
            if (!groupWith.isEmpty()) {
                List<Package> group = new ArrayList<>();
                group.add(pkg);

                for (int groupId : groupWith) {
                    Package grouped = pkgTable.getPackage(groupId);
                    if (grouped != null && !loadedIds.contains(grouped.getPackageId())) {
                        group.add(grouped);
                    }
                }

                // Only load group if it fits entirely
                if (targetTruck.getLoadedPackages().size() + group.size() <= Truck.MAX_CAPACITY) {
                    for (Package p : group) {
                        targetTruck.loadPackage(p);
                        loadedIds.add(p.getPackageId());
                    }
                }
                continue; // go to next pkg
            }

            // Otherwise, load single package
            targetTruck.loadPackage(pkg);
            loadedIds.add(pkg.getPackageId());
        }
        System.out.println("Truck1 loaded with " + truck1.getLoadedPackages().size() + " packages.");
        System.out.println("Truck2 loaded with " + truck2.getLoadedPackages().size() + " packages.");
        System.out.println(
                "Truck3 loaded with " +
                        truck3.getLoadedPackages().size() +
                        " packages (waiting for driver).");
    }

    /**
     * Loads distance matrix from a CSV file into a provided DistanceGraph.
     * Use the WGUPS_Distance_Table.csv File, simple csv copy of WGUPS Distantance
     * table.xslx
     * 
     * @param filepath Path to the CSV file.
     * @param graph    DistanceGraph to store loaded distances.
     */
    public static void loadDistancesFromCSV(String fileName, DistanceGraph graph) {
        // BufferedReader
        // https://docs.oracle.com/javase/8/docs/api/java/io/BufferedReader.html
        // FileReader https://docs.oracle.com/javase/8/docs/api/java/io/FileReader.html
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            List<String> addresses = new ArrayList<>();
            int lineNum = 0;

            // Read header row
            if ((line = br.readLine()) != null) {
                List<String> header = parseComplexLine(line);
                for (int i = 1; i < header.size(); i++) {
                    addresses.add(cleanAddress(header.get(i)));
                }
            }

            // read distance rows
            while ((line = br.readLine()) != null) {
                lineNum++;
                List<String> values = parseComplexLine(line);
                if (values.size() < 2)
                    continue;

                String fromLocation = cleanAddress(values.get(0));

                for (int i = 1; i < values.size(); i++) {
                    String distanceStr = values.get(i).trim();
                    if (!distanceStr.isEmpty()) {
                        try {
                            double distance = Double.parseDouble(distanceStr);
                            String toLocation = cleanAddress(addresses.get(i - 1)); // Match index with header
                            graph.addDistance(fromLocation, toLocation, distance);
                        } catch (NumberFormatException e) {
                            System.err.printf("Invalid number at line %d, column %d: %s%n", lineNum, i, distanceStr);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // #dontcrash if given bad file name
            System.err.println("Error reading CSV distances file: " + e.getMessage());
        }
    }

    /**
     * Loads package data from a CSV file into the provided hash table.
     *
     * @param filepath Path to the CSV file.
     * @param pkgTable PackageHashTable to store loaded packages.
     */
    public static void loadPackagesFromCSV(String filepath, PackageHashTable pkgTable) {
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            int lineNum = 0;

            while ((line = br.readLine()) != null) {

                lineNum++;
                // Debug, see how the line looks when attempting to read
                // System.out.println("LINE " + lineNum + ": " + line);

                // Skip headers or non-data rows
                if (lineNum <= 7)
                    continue;

                String[] values = line.split(",");

                // Check if the line is valid
                if (values.length < 7)
                    continue;
                // Debug, see whats being digested for insertion into a package, verify package
                // test shows package 1 - 40 passing print check
                // System.out.println("Print Check: " + line);

                int packageId = Integer.parseInt(values[0].trim());
                String address = cleanAddress(values[1].trim());
                String city = values[2].trim();
                String state = values[3].trim();
                String zip = values[4].trim();
                String deadline = values[5].trim();
                double weight = Double.parseDouble(values[6].trim());
                // fancy ternary conditional statement, trims result of specialNote if values
                // has length over 7, if less than 7 no special note so avoid out an of bounds
                String specialNote = (values.length > 7) ? values[7].trim() : "";

                Package pkg = new Package(packageId, address, city, state, zip, deadline, weight, specialNote);
                pkgTable.addPackage(pkg);
            }

        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing numeric data: " + e.getMessage());
        }
    }

    /**
     * Delivers all packages loaded onto a truck using nearest Neighbor logic.
     * Handles sending the truck home
     * defaults to trace off
     *
     * @param truck    the truck performing deliveries
     * @param pkgTable reference to package hash table for status updates
     * @param graph    distance graph used for calculating delivery distances
     */
    private static void deliverAllPackages(Truck truck, PackageHashTable pkgTable, DistanceGraph graph) {
        deliverAllPackages(truck, pkgTable, graph, false); // defaults to no tracing
    }

    /**
     * Delivers all packages loaded onto a truck using Nearest neighbor logic.
     * Handles sending the truck home
     * Polymorphic copy, handles turning trace off or on
     *
     * @param truck    the truck performing deliveries
     * @param pkgTable reference to package hash table for status updates
     * @param graph    distance graph used for calculating delivery distances
     * @param trace    True turns trace on, False turns trace off
     */
    private static void deliverAllPackages(Truck truck, PackageHashTable pkgTable, DistanceGraph graph, boolean trace) {
        if (trace) {
            System.out.println("----- Delivery Trace -----");
            System.out.println("Truck starting at HUB\n");
        }

        // While not empty
        while (!truck.getLoadedPackages().isEmpty()) {
            Package nextPkg = findNearestPackage(truck, graph);

            // shouldn't happen but just incase to handle no packages
            if (nextPkg == null) {
                if (trace)
                    System.out.println("No valid package found from current location: " + truck.getCurrentLocation());
                break;
            }

            String from = cleanAddress(truck.getCurrentLocation());
            String to = cleanAddress(nextPkg.getAddress());
            double distance = graph.getDistance(from, to);

            if (trace)
                System.out.printf("Driving from '%s' to '%s' [%.2f miles]%n", from, to, distance);

            truck.deliverPackage(nextPkg, distance);
            pkgTable.updatePackageStatus(nextPkg.getPackageId(), "Delivered", truck.getCurrentTime());

            if (trace) {
                System.out.printf("Delivered Package #%d at %s%n", nextPkg.getPackageId(), truck.getCurrentTime());
                System.out.printf("Truck mileage: %.2f miles%n%n", truck.getMileage());
            }
        }

        // Return to HUB, done delivering packages
        String returnFrom = cleanAddress(truck.getCurrentLocation());
        double returnDistance = graph.getDistance(returnFrom, "HUB");

        if (trace)
            System.out.printf("Returning from '%s' to HUB [%.2f miles]%n", returnFrom, returnDistance);

        truck.goHome(returnDistance);

        if (trace) {
            System.out.printf("Truck returned to HUB at %s, Total mileage: %.2f miles%n",
                    truck.getCurrentTime(), truck.getMileage());
            System.out.println("--------------------------\n");
        }
    }

    /**
     * Finds the nearest package to the truck's current location.
     * Uses cleanAddress() to address consistent errors attempting to read csv file
     *
     * @param truck the delivery truck
     * @param graph distance graph
     * @return nearest package to deliver next
     */
    private static Package findNearestPackage(Truck truck, DistanceGraph graph) {
        Package nearestPackage = null;
        double minDistance = Double.MAX_VALUE;

        for (Package pkg : truck.getLoadedPackages()) {

            /*
             * // Debug finding why 2 distances arent loading:
             * // >>Distance not found between 5383 South 900 East #104 and 5383 South 900
             * // >>East
             * // >>#104
             * // >>Comparing cleaned addresses: '5383 South 900 East #104' to '1060 Dalton
             * // >>Ave
             * // >>S'
             * // >>Distance not found between 5383 South 900 East #104 and 1060 Dalton Ave
             * // >>S
             * System.out.printf("Comparing cleaned addresses: '%s' to '%s'%n",
             * // NOTE: was a csv file issue, i put dalton ave in wrong
             * cleanAddress(truck.getCurrentLocation()),
             * cleanAddress(pkg.getAddress()));
             */
            double distance = graph.getDistance(
                    cleanAddress(truck.getCurrentLocation()),
                    cleanAddress(pkg.getAddress()));
            if (distance < minDistance && distance >= 0) {
                minDistance = distance;
                nearestPackage = pkg;
            }
        }
        return nearestPackage;
    }

    /**
     * Cleans a raw address string extracted from the CSV file.
     * 
     * This method performs the following operations:
     * <ul>
     * <li>Removes any surrounding double quotes</li>
     * <li>Extracts the last line of a potentially multi-line address (assumed to be
     * the actual street address)</li>
     * <li>Removes any ZIP code or extra details enclosed in parentheses</li>
     * <li>Trims leading and trailing whitespace</li>
     * </ul>
     * 
     *
     * @param raw The raw address string as read from the CSV file.
     * @return A cleaned, standardized version of the address for consistent
     *         matching.
     */
    private static String cleanAddress(String raw) {
        if (raw == null || raw.isEmpty())
            return "";

        raw = raw.replaceAll("\"", "").trim();

        // Handle multiline
        String[] lines = raw.split("\n");
        String lastLine = lines[lines.length - 1].trim();

        // Remove anything inside parentheses (zip, notes)
        lastLine = lastLine.replaceAll("\\(.*?\\)", "").trim();

        // Normalize whitespace
        lastLine = lastLine.replaceAll("\\s+", " ");

        return lastLine;
    }

    // Helper method to handle complex CSV rows with quotes and commas.
    private static List<String> parseComplexLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (char ch : line.toCharArray()) {
            if (ch == '"') {
                inQuotes = !inQuotes; // Toggle quote state
            } else if (ch == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0); // reset builder
            } else {
                current.append(ch);
            }
        }
        result.add(current.toString().trim());
        return result;
    }

    /**
     * Converts HH:MM formatted string to total minutes.
     *
     * @param timeStr time string in HH:MM format
     * @return total minutes
     */
    private static int timeToMinutes(String timeStr) {
        if (timeStr.equalsIgnoreCase("EOD"))
            return Integer.MAX_VALUE;

        timeStr = timeStr.trim().toUpperCase();

        // Handle format: HH:MM
        if (!timeStr.contains("AM") && !timeStr.contains("PM")) {
            String[] parts = timeStr.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            return hour * 60 + minute;
        }

        // Handle format: HH:MM AM/PM
        String[] timeAndPeriod = timeStr.split(" ");
        if (timeAndPeriod.length != 2) {
            throw new IllegalArgumentException("Invalid time format: " + timeStr);
        }

        String[] hourMin = timeAndPeriod[0].split(":");
        int hour = Integer.parseInt(hourMin[0]);
        int minute = Integer.parseInt(hourMin[1]);
        String period = timeAndPeriod[1];

        if (period.equals("PM") && hour != 12)
            hour += 12;
        if (period.equals("AM") && hour == 12)
            hour = 0;

        return hour * 60 + minute;
    }

}
