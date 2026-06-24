package net.kyrptonaught.inventorysorter.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClientServerSupportTest {
    @Test
    void unknownSupportUsesFallbackUntilPresenceIsKnown() {
        ClientServerSupport support = new ClientServerSupport();

        Assertions.assertFalse(support.isPresent());
        Assertions.assertFalse(support.shouldUseServerSorting());
    }

    @Test
    void presentSupportUsesServerSorting() {
        ClientServerSupport support = new ClientServerSupport();

        support.markPresent();

        Assertions.assertTrue(support.isPresent());
        Assertions.assertTrue(support.shouldUseServerSorting());
    }

    @Test
    void absentSupportUsesFallbackSorting() {
        ClientServerSupport support = new ClientServerSupport();

        support.markAbsent();

        Assertions.assertFalse(support.isPresent());
        Assertions.assertFalse(support.shouldUseServerSorting());
    }

    @Test
    void resetReturnsToUnknownFallbackPriority() {
        ClientServerSupport support = new ClientServerSupport();
        support.markPresent();

        support.reset();

        Assertions.assertFalse(support.isPresent());
        Assertions.assertFalse(support.shouldUseServerSorting());
    }
}
