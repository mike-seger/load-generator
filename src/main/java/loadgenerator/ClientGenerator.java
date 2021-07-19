package loadgenerator;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientGenerator implements Runnable {
	private Thread clientGenerator;
	private final AsyncHttpClient asyncHttpClient;
	private final AtomicInteger activeClients;
	private final AtomicInteger numberOfRequests;
	private final Map<Integer, AtomicInteger> requestStats = new TreeMap<>();
	private final int numberOfClients;
	private final String url;
	private static final int reportingInterval = 500;

	public ClientGenerator(String url, int numberOfClients) {
		this.asyncHttpClient = new DefaultAsyncHttpClient();
		this.activeClients = new AtomicInteger(0);
		this.numberOfRequests = new AtomicInteger(0);
		this.numberOfClients = numberOfClients;
		this.url = url;
	}

	@Override
	public void run() {
		long lastReported = System.currentTimeMillis();

		clientGenerator = Thread.currentThread();
		System.out.println("URL: " + url);
		System.out.println("Number of clients: " + numberOfClients);

		while (true) {
			while (activeClients.get() < numberOfClients) {
				request();
				numberOfRequests.incrementAndGet();
				activeClients.incrementAndGet();
			}

			try {
				if(System.currentTimeMillis()>lastReported) {
					report(requestStats.toString());
					lastReported += reportingInterval;
				}
				Thread.sleep(reportingInterval);
			} catch (InterruptedException e) {}
		}
	}

	private void report(String message) {
		DateTimeFormatter formatter
				= DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
		OffsetDateTime now = OffsetDateTime.now(ZoneId.of("Europe/Zurich"));
		System.out.printf("%s: %s\n", now.format(formatter), message);
	}

	private void request() {
		asyncHttpClient.prepareGet(url).execute(new AsyncCompletionHandler<Response>() {
			@Override
			public Response onCompleted(Response response) throws Exception {
				requestStats.computeIfAbsent(response.getStatusCode(),
					status -> new AtomicInteger(0)).incrementAndGet();
				activeClients.decrementAndGet();
				clientGenerator.interrupt();
				return response;
			}

			@Override
			public void onThrowable(Throwable t) {
				requestStats.computeIfAbsent(-1,
					status -> new AtomicInteger(0)).incrementAndGet();
				activeClients.decrementAndGet();
				clientGenerator.interrupt();
			}
		});
	}
}
