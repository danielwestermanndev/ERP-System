// src/test/java/com/dwestermann/erp/ErpSystemApplicationTests.java
package com.dwestermann.erp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = {
		"jwt.secret-key=test-secret-key-minimum-256-bits-for-HS256-algorithm-testing-purposes-only"
})
class ErpSystemApplicationTests {

	@Test
	void contextLoads() {
		// Dieser Test prüft nur, ob der Spring Context korrekt geladen wird
		// Wenn er fehlschlägt, gibt es ein Problem mit der Bean-Konfiguration
	}
}