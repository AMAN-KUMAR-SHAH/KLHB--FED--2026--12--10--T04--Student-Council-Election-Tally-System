import java.util.*;

class Candidate {
    String id, name;

    Candidate(String id, String name) {
        this.id = id;
        this.name = name;
    }
}

class Voter {
    String id, name;
    boolean hasVoted = false;

    Voter(String id, String name) {
        this.id = id;
        this.name = name;
    }
}

class Ballot {
    List<String> preferences;

    Ballot(List<String> preferences) {
        this.preferences = new ArrayList<>(preferences);
    }
}

public class Main {
    static Scanner sc = new Scanner(System.in);
    static ArrayList<Candidate> candidates = new ArrayList<>();
    static ArrayList<Voter> voters = new ArrayList<>();
    static ArrayList<Ballot> ballots = new ArrayList<>();

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== STUDENT COUNCIL ELECTION ===");
            System.out.println("1. Register Candidate");
            System.out.println("2. Register Voter");
            System.out.println("3. Cast Ballot");
            System.out.println("4. FPTP Result");
            System.out.println("5. Instant Runoff");
            System.out.println("6. Audit and Turnout");
            System.out.println("7. Exit");
            System.out.print("Enter choice: ");

            String input = sc.nextLine();

            switch (input) {
                case "1": registerCandidate(); break;
                case "2": registerVoter(); break;
                case "3": castBallot(); break;
                case "4": fptp(); break;
                case "5": instantRunoff(); break;
                case "6": audit(); break;
                case "7":
                    System.out.println("Election system closed.");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    static void registerCandidate() {
        System.out.print("Candidate ID: ");
        String id = sc.nextLine().trim();

        if (id.isEmpty() || findCandidate(id) != null) {
            System.out.println("Invalid or duplicate candidate ID.");
            return;
        }

        System.out.print("Candidate name: ");
        String name = sc.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }

        candidates.add(new Candidate(id, name));
        System.out.println("Candidate registered.");
    }

    static void registerVoter() {
        System.out.print("Voter ID: ");
        String id = sc.nextLine().trim();

        if (id.isEmpty() || findVoter(id) != null) {
            System.out.println("Invalid or duplicate voter ID.");
            return;
        }

        System.out.print("Voter name: ");
        String name = sc.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }

        voters.add(new Voter(id, name));
        System.out.println("Voter registered.");
    }

    static void castBallot() {
        if (candidates.isEmpty()) {
            System.out.println("Register candidates first.");
            return;
        }

        System.out.print("Voter ID: ");
        Voter voter = findVoter(sc.nextLine().trim());

        if (voter == null) {
            System.out.println("Voter not registered.");
            return;
        }

        if (voter.hasVoted) {
            System.out.println("ERROR: Voter has already voted.");
            return;
        }

        System.out.println("Candidates:");
        for (Candidate c : candidates)
            System.out.println(c.id + " - " + c.name);

        System.out.print("Rank candidate IDs (e.g. C1 C2 C3): ");
        String line = sc.nextLine().trim();

        if (line.isEmpty()) {
            System.out.println("Empty ballot rejected.");
            return;
        }

        ArrayList<String> preferences = new ArrayList<>();

        for (String id : line.split("\\s+")) {
            Candidate c = findCandidate(id);

            if (c == null || preferences.contains(c.id)) {
                System.out.println("Invalid or repeated candidate ID.");
                return;
            }

            preferences.add(c.id);
        }

        ballots.add(new Ballot(preferences));
        voter.hasVoted = true;
        System.out.println("Ballot recorded successfully.");
    }

    // First-Past-The-Post
    static void fptp() {
        if (candidates.isEmpty() || ballots.isEmpty()) {
            System.out.println("Candidates and votes are required.");
            return;
        }

        HashMap<String, Integer> count = new HashMap<>();
        for (Candidate c : candidates)
            count.put(c.id, 0);

        for (Ballot b : ballots) {
            String first = b.preferences.get(0);
            count.put(first, count.get(first) + 1);
        }

        System.out.println("\n=== FPTP RESULTS ===");
        int max = -1;
        ArrayList<String> winners = new ArrayList<>();

        for (Candidate c : candidates) {
            int votes = count.get(c.id);
            System.out.println(c.name + ": " + votes);

            if (votes > max) {
                max = votes;
                winners.clear();
                winners.add(c.id);
            } else if (votes == max) {
                winners.add(c.id);
            }
        }

        if (winners.size() == 1)
            System.out.println("WINNER: " +
                    findCandidate(winners.get(0)).name);
        else
            System.out.println("TIE: " + names(winners));

        turnout();
    }

    // Instant Runoff Voting
    static void instantRunoff() {
        if (candidates.isEmpty() || ballots.isEmpty()) {
            System.out.println("Candidates and votes are required.");
            return;
        }

        ArrayList<String> active = new ArrayList<>();
        for (Candidate c : candidates)
            active.add(c.id);

        int round = 1;

        while (!active.isEmpty()) {
            HashMap<String, Integer> count = new HashMap<>();
            for (String id : active)
                count.put(id, 0);

            int validVotes = 0;

            for (Ballot b : ballots) {
                for (String id : b.preferences) {
                    if (active.contains(id)) {
                        count.put(id, count.get(id) + 1);
                        validVotes++;
                        break;
                    }
                }
            }

            System.out.println("\n--- Round " + round + " ---");
            for (String id : active)
                System.out.println(findCandidate(id).name +
                        ": " + count.get(id));

            if (validVotes == 0) {
                System.out.println("No valid votes remain.");
                return;
            }

            for (String id : active) {
                if (count.get(id) * 2 > validVotes) {
                    System.out.println("WINNER: " +
                            findCandidate(id).name);
                    turnout();
                    return;
                }
            }

            if (active.size() == 1) {
                System.out.println("WINNER: " +
                        findCandidate(active.get(0)).name);
                turnout();
                return;
            }

            int lowest = Integer.MAX_VALUE;
            ArrayList<String> tied = new ArrayList<>();

            for (String id : active) {
                int votes = count.get(id);

                if (votes < lowest) {
                    lowest = votes;
                    tied.clear();
                    tied.add(id);
                } else if (votes == lowest) {
                    tied.add(id);
                }
            }

            // Tie-break: eliminate alphabetically smallest ID
            Collections.sort(tied);
            String eliminated = tied.get(0);

            System.out.println("Eliminated: " +
                    findCandidate(eliminated).name);

            active.remove(eliminated);
            round++;
        }
    }

    // Anonymised ballot audit
    static void audit() {
        System.out.println("\n=== ANONYMISED AUDIT ===");

        if (ballots.isEmpty()) {
            System.out.println("No ballots recorded.");
        } else {
            for (int i = 0; i < ballots.size(); i++) {
                System.out.println("Ballot " + (i + 1) + ": " +
                        String.join(" > ", ballots.get(i).preferences));
            }
        }

        turnout();
    }

    static void turnout() {
        int total = voters.size();
        int voted = ballots.size();
        double percent = total == 0 ? 0 : voted * 100.0 / total;

        System.out.printf("Turnout: %d/%d (%.2f%%)%n",
                voted, total, percent);
    }

    static String names(List<String> ids) {
        ArrayList<String> result = new ArrayList<>();

        for (String id : ids)
            result.add(findCandidate(id).name);

        return String.join(", ", result);
    }

    static Candidate findCandidate(String id) {
        for (Candidate c : candidates)
            if (c.id.equalsIgnoreCase(id))
                return c;
        return null;
    }

    static Voter findVoter(String id) {
        for (Voter v : voters)
            if (v.id.equalsIgnoreCase(id))
                return v;
        return null;
    }
}