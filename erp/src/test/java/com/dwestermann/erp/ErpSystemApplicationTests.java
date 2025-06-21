package com.dwestermann.erp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // Wichtig: Test-Profil verwenden
class ErpSystemApplicationTests {

	@Test
	void contextLoads() {
		// Dieser Test prüft nur, ob der Spring Context korrekt geladen wird
		// Wenn er fehlschlägt, gibt es ein Problem mit der Bean-Konfiguration
	}
}