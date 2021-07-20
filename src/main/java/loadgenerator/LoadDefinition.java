package loadgenerator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadDefinition {
	private List<String> urls;
	private Map<String, String> headers;
	private Map<String, String> systemProperties;
	private Integer parallel = 10;
	private Integer warmup = 20;
	private Integer reportingInterval = 5000;
}
