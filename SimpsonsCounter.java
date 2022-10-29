import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashMap;

public class SimpsonsCounter {
    private static final int THREAD_COUNT = 3;
    private static final int CALLS = 100;
    private static final String API_URL = "http://metaphorpsum.com/paragraphs/10";
    private static String text;
    private static double moLength;
    private static double finalMoLength;
    private static double tempo;
    private static int totalCounter;

    public static void main(String[] args) {
        for (int co = 0; co < THREAD_COUNT + 1; co++) {
            // Create starting timestamp
            Date begin = new Date();
            finalMoLength = 0;
            tempo = 0;
            text = "";
            String threadNum = new DecimalFormat("#").format((int) Math.pow(2, co));
            String nOfThreads = "";
            String underLine = "";
            ProcessThread[] threads = new ProcessThread[(int) Math.pow(2, co)];

            // We could also use a Map for the results, but the following approach is simpler and faster!
            int[] mainCounter = new int['z' - 'a' + 1];

            for (int i = 0; i < threads.length; i++) {
                threads[i] = new ProcessThread();
                threads[i].start();
            }

            for (ProcessThread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            final HashMap<String, Double> moCalc = new HashMap<>();
            // Merge the results from the different threads
            for (ProcessThread thread : threads) {
                moLength = 0;
                totalCounter = 0;
                // Merge Variables
                thread.getMoCalc().forEach((word, count) -> {
                    double countSum = count;
                    if (moCalc.containsKey(word)) {
                        countSum += moCalc.get(word);
                    }
                    moCalc.put(word, countSum);
                });

                for (int j = 0; j < mainCounter.length; j++) {
                    mainCounter[j] += thread.getCounter()[j];
                    totalCounter = totalCounter + mainCounter[j];
                }
            }

            if (co == 0) {
                nOfThreads = "Thread";
                underLine = "------------------------";
            } else {
                nOfThreads = "Threads";
                underLine = "-------------------------";
            }
            System.out.println("\nExecution with " + threadNum + " " + nOfThreads + ".");
            System.out.println(underLine);
            moCalc.forEach((word, count) -> {
                tempo = tempo + count;
            });

            String num = new DecimalFormat("#.###").format((double) tempo / Math.pow(2, co));
            System.out.println("Average Words Length: " + num);
            System.out.println("Percentage of Appearance for Each Letter:");

            // Display the Results
            for (int i = 0; i < mainCounter.length; i++) {
                char c = (char) ('a' + i);
                float percent = (mainCounter[i] * 100) / (float) totalCounter;
                System.out.printf("Appearence of %c : %.3f %s\n", c, percent, " %");
            }
            // Create ending timestamp
            Date finish = new Date();
            System.out.println("Time taken in milli seconds: " + (finish.getTime() - begin.getTime()));
        }
    }

    // This thread processes the file lines.
    static class ProcessThread extends Thread {
        private final HashMap<String, Double> moCalc = new HashMap<>();
        private final int[] countUp = new int['z' - 'a' + 1];

        @Override
        public void run() {
            double temp = 0;
            for (int i = 0; i < CALLS; i++) {
                String data = loadDataFromUrl();
                // System.out.println(data);
                String[] words = data.split(" ");
                double lengthOfWord = 0;
                int counter = 0;
                for (String word : words) {
                    String low = word.toLowerCase();
                    String str = low.replaceAll("[^a-zA-Z0-9]", "");
                    counter++;
                    lengthOfWord += str.length();
                    text = text.concat(str);
                }
                if (counter != 0) {
                    moLength = lengthOfWord / counter;
                }
                temp = temp + moLength;
            }
            finalMoLength = temp / CALLS;
            moCalc.put(text, finalMoLength);
            for (char c : text.toCharArray()) {
                int index = c - 'a';
                countUp[index]++;
            }
        }

        // This method loads the data from a url
        private String loadDataFromUrl() {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(API_URL);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    result.append(inputLine);
                    result.append(" ");
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        public HashMap<String, Double> getMoCalc() {
            return moCalc;
        }

        public int[] getCounter() {
            return countUp;
        }
    }
}
