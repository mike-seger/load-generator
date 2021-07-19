package loadgenerator;

public class LoadGenerator {

    public static void main(String[] args) {
        if (args.length == 2) {
            String url = args[0];
            int numberOfClients = 100;
            try {
                numberOfClients = Integer.parseInt(args[1]);
            } catch (Exception ex) {
                System.out.println("Invalid number of clients.");
                System.out.println("Starting with default 100");
            }

            new Thread(new ClientGenerator(url, numberOfClients)).start();
        } else {
            System.out.printf("Usage: %s <url> <number of clients>\n", LoadGenerator.class.getName());
        }
    }
}
