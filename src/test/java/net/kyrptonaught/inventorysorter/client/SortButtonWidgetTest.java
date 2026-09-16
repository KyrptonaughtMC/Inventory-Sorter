package net.kyrptonaught.inventorysorter.client;

import net.kyrptonaught.inventorysorter.ButtonType;
import net.kyrptonaught.inventorysorter.SortTarget;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SortButtonWidgetTest {
    @Test
    void scrollIfHoveredIgnoresMissingButton() {
        Assertions.assertFalse(SortButtonWidget.scrollIfHovered(null, 1, 2, 3, 4));
    }

    @Test
    void scrollIfHoveredIgnoresHiddenButton() {
        RecordingSortButtonWidget button = new RecordingSortButtonWidget(true, true);
        button.visible = false;

        Assertions.assertFalse(SortButtonWidget.scrollIfHovered(button, 1, 2, 3, 4));
        Assertions.assertFalse(button.scrolled);
    }

    @Test
    void scrollIfHoveredIgnoresButtonThatIsNotHovered() {
        RecordingSortButtonWidget button = new RecordingSortButtonWidget(false, true);

        Assertions.assertFalse(SortButtonWidget.scrollIfHovered(button, 1, 2, 3, 4));
        Assertions.assertFalse(button.scrolled);
    }

    @Test
    void scrollIfHoveredDelegatesToHoveredVisibleButton() {
        RecordingSortButtonWidget button = new RecordingSortButtonWidget(true, true);

        Assertions.assertTrue(SortButtonWidget.scrollIfHovered(button, 1, 2, 3, 4));
        Assertions.assertTrue(button.scrolled);
        Assertions.assertEquals(1, button.mouseX);
        Assertions.assertEquals(2, button.mouseY);
        Assertions.assertEquals(3, button.verticalAmount);
        Assertions.assertEquals(4, button.horizontalAmount);
    }

    @Test
    void scrollFollowsCurrentButtonBoundsAfterContainerMoves() {
        RecordingSortButtonWidget button = new RecordingSortButtonWidget(false, true);
        button.setPosition(100, 200);

        Assertions.assertTrue(SortButtonWidget.scrollIfHovered(button, 101, 202, 3, 4));
        button.scrolled = false;
        button.setPosition(177, 220);

        Assertions.assertFalse(SortButtonWidget.scrollIfHovered(button, 101, 202, 3, 4));
        Assertions.assertFalse(button.scrolled);
        Assertions.assertTrue(SortButtonWidget.scrollIfHovered(button, 178, 222, 3, 4));
    }

    private static class RecordingSortButtonWidget extends SortButtonWidget {
        private final boolean hovered;
        private final boolean scrollResult;
        private boolean scrolled;
        private double mouseX;
        private double mouseY;
        private double verticalAmount;
        private double horizontalAmount;

        private RecordingSortButtonWidget(boolean hovered, boolean scrollResult) {
            super(ButtonType.INVENTORY, hovered ? 0 : 100, hovered ? 0 : 200, SortTarget.CONTAINER);
            this.hovered = hovered;
            this.scrollResult = scrollResult;
        }

        @Override
        public boolean isHovered() {
            return hovered;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount, double horizontalAmount) {
            this.scrolled = true;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.verticalAmount = verticalAmount;
            this.horizontalAmount = horizontalAmount;
            return scrollResult;
        }
    }
}
