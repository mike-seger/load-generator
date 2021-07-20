package loadgenerator;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientGenerator implements Runnable {
	private Thread clientGenerator;
	private final AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
	private final AtomicInteger activeClients = new AtomicInteger(0);
	private final AtomicInteger numberOfRequests = new AtomicInteger(0);
	private final Map<String, AtomicInteger> requestStats = new TreeMap<>();
	private final LoadDefinition loadDefinition;

	public ClientGenerator(LoadDefinition loadDefinition) {
		this.loadDefinition = loadDefinition;
	}

	@Override
	public void run() {
		long lastReported = System.currentTimeMillis();

		clientGenerator = Thread.currentThread();

		while (true) {
			while (activeClients.get() < loadDefinition.getParallel()) request();
			try {
				if(System.currentTimeMillis()>lastReported) {
					report(requestStats.toString());
					lastReported += loadDefinition.getReportingInterval();
				}
				Thread.sleep(loadDefinition.getReportingInterval());
			} catch (InterruptedException e) {}
		}
	}

	private void report(String message) {
		DateTimeFormatter formatter
				= DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
		OffsetDateTime now = OffsetDateTime.now(ZoneId.of("Europe/Zurich"));
		System.out.printf("%s: %s\n", now.format(formatter), message);
	}

	private String getNextUrl() {
		int n = numberOfRequests.incrementAndGet();
		return loadDefinition.getUrls().get(n%loadDefinition.getUrls().size());
	}

	private void request() {

		numberOfRequests.incrementAndGet();
		activeClients.incrementAndGet();
		asyncHttpClient.prepareGet(getNextUrl()).setSingleHeaders(loadDefinition.getHeaders()).execute(new AsyncCompletionHandler<Response>() {
			@Override
			public Response onCompleted(Response response) {
				if(numberOfRequests.get()>=loadDefinition.getWarmup())
					requestStats.computeIfAbsent(response.getStatusCode()+"",
						status -> new AtomicInteger(0)).incrementAndGet();
				activeClients.decrementAndGet();
				clientGenerator.interrupt();
				return response;
			}

			@Override
			public void onThrowable(Throwable t) {
				if(numberOfRequests.get()>=loadDefinition.getWarmup())
					requestStats.computeIfAbsent(
						(t.getMessage()+"")
							.replace("\r"," ")
							.replace("\n"," ")
							.replaceAll("  *"," ")
							.replaceAll(".*connection timed out.*","Connection timed out"),
					status -> {
						t.printStackTrace();
						return new AtomicInteger(0);
					}).incrementAndGet();
				activeClients.decrementAndGet();
				clientGenerator.interrupt();
			}
		});
	}
}
