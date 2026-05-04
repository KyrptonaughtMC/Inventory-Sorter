package net.kyrptonaught.inventorysorter.client;

import net.kyrptonaught.inventorysorter.ButtonType;
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
    void scrollScreenButtonsIfHoveredIgnoresMissingScreen() {
        Assertions.assertFalse(SortButtonWidget.scrollScreenButtonsIfHovered(null, 1, 2, 3, 4));
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
            super(ButtonType.INVENTORY, 0, 0, false, null);
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
