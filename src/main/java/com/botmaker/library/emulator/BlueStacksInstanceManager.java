package com.botmaker.library.emulator;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BlueStacksInstanceManager {

    public static List<BlueStacksInstance> getInstances(Path mimMetaDataPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<BlueStacksInstance> instances = new ArrayList<>();

        JsonNode rootNode = mapper.readTree(mimMetaDataPath.toFile());
        JsonNode organizationNode = rootNode.get("Organization");

        if (organizationNode != null && organizationNode.isArray()) {
            for (JsonNode node : organizationNode) {
                instances.add(mapper.treeToValue(node, BlueStacksInstance.class));
            }
        }
        return instances;
    }
}
