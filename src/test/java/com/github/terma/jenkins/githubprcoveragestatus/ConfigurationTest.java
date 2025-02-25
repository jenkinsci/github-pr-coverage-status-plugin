package com.github.terma.jenkins.githubprcoveragestatus;

import hudson.util.Secret;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigurationTest {

    @Test
    public void testEncryptionAndDecryption() {
        String sensitiveData = "mySecretPassword";
        Secret secret = Secret.fromString(sensitiveData);
        String decrypted = secret.getPlainText();
        assertEquals("The decrypted data should match the original sensitive data", sensitiveData, decrypted);
    }

    @Test
    public void testConfigurationSensitiveField() {
        String sensitive = "anotherSecret";
        // Assuming a Configuration class with a default constructor and methods to set and get a sensitive field
        Configuration config = new Configuration();
        config.setSensitiveField(Secret.fromString(sensitive));
        Secret retrievedSecret = config.getSensitiveField();
        assertNotNull("Sensitive field should not be null", retrievedSecret);
        assertEquals("The sensitive field should be stored and retrieved correctly in plaintext", sensitive, retrievedSecret.getPlainText());
    }

    @Test
    public void testMultipleEncryptionDecryption() {
        String[] sensitiveValues = {"password123", "tokenValue", "secretKey!"};
        for (String value : sensitiveValues) {
            Secret secret = Secret.fromString(value);
            String decrypted = secret.getPlainText();
            assertEquals("Decrypted text should match the original for value: " + value, value, decrypted);
        }
    }
}
