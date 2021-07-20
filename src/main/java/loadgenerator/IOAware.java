package loadgenerator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public interface IOAware {
	default String loadResource(String location) throws IOException, URISyntaxException {
		return new String(Files.readAllBytes(
				Paths.get(Objects.requireNonNull(getClass().getResource(location)).toURI())));
	}

	default String loadFile(String path) throws IOException {
		return Files.readString(Paths.get(path), StandardCharsets.UTF_8);
	}
}
