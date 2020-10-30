import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PDFDownloader {
    static String[] urls = {
            "http://www.ubicomp.org/ubicomp2003/adjunct_proceedings/proceedings.pdf",
            "https://www.hq.nasa.gov/alsj/a17/A17_FlightPlan.pdf",
            "https://ars.els-cdn.com/content/image/1-s2.0-S0140673617321293-mmc1.pdf",
            "http://www.visitgreece.gr/deployedFiles/StaticFiles/maps/Peloponnese_map.pdf"
    };

    private final static int POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private final static int NUMBER_OF_TASKS = urls.length;

    public static void main(String[] args) {
        Integer mode = Integer.parseInt(args[0]);

        if (mode == 0) {
            downloadSequentially();
        } else if (mode == 1) {
            downloadWithMultithreading();
        }
    }

    static void downloadSequentially() {
        System.out.println("Mode: Single threaded");
        double timeInitiated = System.currentTimeMillis();

        System.out.print("Files: ");

        for (int i = 0; i < urls.length; i++) {
            Sequential sequential = new Sequential(urls[i], "file" + (i + 1));
            sequential.download();
            if (i < urls.length - 1) {
                System.out.print("file" + (i + 1) + " -> done, ");
            } else {
                System.out.print("file" + (i + 1) + " -> done");
            }
        }

        double timeFinished = System.currentTimeMillis();
        double timeElapsed = (timeFinished - timeInitiated) / 1000;

        System.out.printf("\nTime: %.2fsec.", timeElapsed);
    }

    static void downloadWithMultithreading() {
        System.out.println("Mode: Multi threaded");
        ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);
        
        double timeInitiated = System.currentTimeMillis();

        System.out.print("Files: ");
        try {
            for (int i = 0; i < NUMBER_OF_TASKS; i++) {
                executor.submit(new Parallel(urls[i], "file" + (i + 1)));
            }

            executor.shutdown();
            executor.awaitTermination(24L, TimeUnit.HOURS);
        } catch (InterruptedException exception) {
            exception.printStackTrace();
        }

        double timeFinished = System.currentTimeMillis();
        double timeElapsed = (timeFinished - timeInitiated) / 1000;

        System.out.printf("\nTime: %.2fsec.", timeElapsed);
    }
}

class Sequential {
    private final String URL;
    private final String FILE_NAME;
    private FileOutputStream os;

    Sequential(String URL, String FILE_NAME) {
        this.URL = URL;
        this.FILE_NAME = FILE_NAME;

        download();
    }

    void download() {
        try(BufferedInputStream in = new BufferedInputStream(new URL(URL).openStream())) {
            os = new FileOutputStream(FILE_NAME + ".pdf");
            byte[] data = in.readAllBytes();
            os.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class Parallel implements Runnable {

    private final String URL;
    private final String FILE_NAME;
    private FileOutputStream os;

    Parallel(String URL, String FILE_NAME) {
        this.URL = URL;
        this.FILE_NAME = FILE_NAME;
    }

    @Override
    public void run() {
        download();
    }

    void download() {
        try(BufferedInputStream in = new BufferedInputStream(new URL(URL).openStream())) {
            os = new FileOutputStream(FILE_NAME + ".pdf");
            byte[] data = in.readAllBytes();
            os.write(data);
            System.out.print(FILE_NAME + " -> done, ");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}