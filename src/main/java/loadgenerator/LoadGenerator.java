package loadgenerator;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LoadGenerator implements IOAware {
    public static void main(String[] args) throws IOException {
        new LoadGenerator().run(args);
    }
    public void run(String[] args) throws IOException {
        if (args.length == 1) {
            Yaml yaml = new Yaml(new Constructor(LoadDefinition.class));
            LoadDefinition loadDefinition = yaml.load(loadFile(args[0]));
            if(loadDefinition.getUrls()==null || loadDefinition.getUrls().size()==0) {
                System.err.println("You must provide at leas 1 url");
                System.exit(1);
            } else {
                List<String> newUrls = new ArrayList<>();
                for(String url : loadDefinition.getUrls()) {
                    if(url.startsWith("@")) {
                        newUrls.addAll(Arrays.stream(loadFile(url.substring(1)).split("[\r\n]"))
                            .map(String::trim).collect(Collectors.toList()));
                    } else newUrls.add(url);
                }
                loadDefinition.setUrls(newUrls);
            }
            new Thread(new ClientGenerator(loadDefinition)).start();
        } else {
            System.err.printf("Usage: %s <load definition yaml>\n", LoadGenerator.class.getName());
            System.exit(1);
        }
    }
}
