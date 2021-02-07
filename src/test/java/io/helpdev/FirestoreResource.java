package io.helpdev;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.MountableFile;

import java.util.HashMap;
import java.util.Map;

public class FirestoreResource implements QuarkusTestResourceLifecycleManager {

    private final Integer EXPOSED_PORT = 8080;
    private final String IMAGE_NAME = "gcr.io/google.com/cloudsdktool/cloud-sdk:317.0.0-emulators";
    private GenericContainer firestoreEmulator;

    @Override
    public Map<String, String> start() {
        firestoreEmulator = new GenericContainer(IMAGE_NAME);
        firestoreEmulator.withExposedPorts(new Integer[]{EXPOSED_PORT});
        firestoreEmulator.withCopyFileToContainer(MountableFile.forClasspathResource("firestore.rules"), "/tmp/firestore.rules");
        firestoreEmulator.withCommand(new String[]{"/bin/sh", "-c", "gcloud beta emulators firestore start --host-port 0.0.0.0:" + EXPOSED_PORT + " --rules=/tmp/firestore.rules"});
        firestoreEmulator.setWaitStrategy((new LogMessageWaitStrategy()).withRegEx("(?s).*running.*$"));
        firestoreEmulator.start();

        Map<String, String> map = new HashMap<>();
        map.put("firestore.emulator.endpoint", firestoreEmulator.getContainerIpAddress() + ":" + firestoreEmulator.getMappedPort(EXPOSED_PORT));
        return map;
    }


    @Override
    public void stop() {
        if(null != firestoreEmulator) {
            firestoreEmulator.stop();
        }
    }
}


