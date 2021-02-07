package io.helpdev;

import com.google.api.core.ApiFuture;
import com.google.cloud.NoCredentials;
import com.google.cloud.firestore.*;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@QuarkusTestResource(FirestoreResource.class)
public class FirestoreSecurityRulesTest {

    @ConfigProperty(name = "firestore.emulator.endpoint")
    String firestoreEmulatorEndpoint;

    private static Firestore firestore;

    @BeforeEach
    public void beforeAll() {
        FirestoreOptions options = FirestoreOptions.getDefaultInstance().toBuilder()
                .setHost(firestoreEmulatorEndpoint)
                .setCredentials(NoCredentials.getInstance())
                .setProjectId("my-test-project")
                .build();
        firestore = options.getService();
    }

    @Test
    public void testNotAllowedWrite() {
        CollectionReference pizzaStore = firestore.collection("notAllowedCollection");
        DocumentReference doc = pizzaStore.document("1234");
        Map<String, Object> data = new HashMap<>();
        data.put("testKey", "testValue");
        ApiFuture<WriteResult> result = doc.set(data);
        Exception exception = assertThrows(ExecutionException.class, () -> {
            result.get();
        });
        Assertions.assertTrue(exception.getMessage().contains("PERMISSION_DENIED"));
    }

    @Test
    public void testNotAllowedRead() {
        CollectionReference pizzaStore = firestore.collection("notAllowedCollection");
        ApiFuture<QuerySnapshot> result = pizzaStore.get();
        Exception exception = assertThrows(ExecutionException.class, () -> {
            result.get();
        });
        Assertions.assertTrue(exception.getMessage().contains("PERMISSION_DENIED"));
    }

    @Test
    public void testSuccessWrite() throws ExecutionException, InterruptedException {
        CollectionReference pizzaStore = firestore.collection("pizzaStore");
        DocumentReference docRef = pizzaStore.document("1234");
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Regina");
        data.put("status", "PIZZA_BAKED");
        ApiFuture<WriteResult> result = docRef.set(data);
        WriteResult writeResult = result.get();
        Assertions.assertNotNull(writeResult);
    }

    @Test
    public void testNotEnoughFieldsWrite() throws ExecutionException, InterruptedException {
        CollectionReference pizzaStore = firestore.collection("pizzaStore");
        DocumentReference docRef = pizzaStore.document("1234");
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Regina");
        ApiFuture<WriteResult> result = docRef.set(data);
        Exception exception = assertThrows(ExecutionException.class, () -> {
            result.get();
        });
        Assertions.assertTrue(exception.getMessage().contains("PERMISSION_DENIED"));
    }
}
