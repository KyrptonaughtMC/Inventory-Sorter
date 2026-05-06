package net.kyrptonaught.inventorysorter.client;

import net.kyrptonaught.inventorysorter.network.LastSeenVersionPacket;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TranslationReminderTest {
    @Test
    void currentVersionAndLanguageDoNotNeedReminder() {
        LastSeenVersionPacket lastSeenVersion = new LastSeenVersionPacket("26.1.2", "en_us");

        boolean seen = TranslationReminder.hasSeenCurrentVersion(lastSeenVersion, "26.1.2", "en_us");

        Assertions.assertTrue(seen);
    }

    @Test
    void differentVersionNeedsReminder() {
        LastSeenVersionPacket lastSeenVersion = new LastSeenVersionPacket("26.1.1", "en_us");

        boolean seen = TranslationReminder.hasSeenCurrentVersion(lastSeenVersion, "26.1.2", "en_us");

        Assertions.assertFalse(seen);
    }

    @Test
    void differentLanguageNeedsReminder() {
        LastSeenVersionPacket lastSeenVersion = new LastSeenVersionPacket("26.1.2", "zh_cn");

        boolean seen = TranslationReminder.hasSeenCurrentVersion(lastSeenVersion, "26.1.2", "en_us");

        Assertions.assertFalse(seen);
    }

    @Test
    void selectedLanguageComparisonIsCaseInsensitive() {
        LastSeenVersionPacket lastSeenVersion = new LastSeenVersionPacket("26.1.2", "en_us");

        boolean seen = TranslationReminder.hasSeenCurrentVersion(lastSeenVersion, "26.1.2", "EN_US");

        Assertions.assertTrue(seen);
    }
}
