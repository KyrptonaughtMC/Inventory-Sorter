package net.kyrptonaught.inventorysorter.client.clothconfig;

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.EnumListEntry;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.kyrptonaught.inventorysorter.sort.SortPriorityPosition;
import net.kyrptonaught.inventorysorter.SortPriorityRule;
import net.kyrptonaught.inventorysorter.sort.SortPriorityRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Environment(EnvType.CLIENT)
public class SortPriorityRulesEntry extends TooltipListEntry<List<SortPriorityRule>> {
    private static final int TEXT_COLOR = ARGB.opaque(0xE0E0E0);
    private static final int ERROR_TEXT_COLOR = ARGB.opaque(0xFF5555);
    private static final int GAP = 4;
    private static final int POSITION_WIDTH = 150;
    private static final int SMALL_BUTTON_WIDTH = 22;
    private static final int ADD_BUTTON_WIDTH = 52;
    private static final int ROW_HEIGHT = 24;

    private final List<RuleRow> rows;
    private RuleRow draftRow;
    private final List<SortPriorityRule> original;
    private final ConfigEntryBuilder entryBuilder;
    private boolean isSelected;

    public SortPriorityRulesEntry(ConfigEntryBuilder entryBuilder, Component fieldName, List<SortPriorityRule> rules, Consumer<List<SortPriorityRule>> saveConsumer) {
        super(fieldName, null, false);
        this.entryBuilder = entryBuilder;
        this.original = List.copyOf(rules);
        this.rows = new ArrayList<>();
        rules.stream()
                .map(this::existingRow)
                .forEach(this.rows::add);
        this.draftRow = draftRow();
        this.saveCallback = saveConsumer;
        this.setErrorSupplier(this::firstConfigError);
    }

    @Override
    public boolean isEdited() {
        return !this.original.equals(this.getValue());
    }

    @Override
    public List<SortPriorityRule> getValue() {
        return this.rows.stream()
                .map(RuleRow::toRule)
                .filter(SortPriorityRulesEntry::isSaveable)
                .map(rule -> new SortPriorityRule(rule.match().trim(), rule.position()))
                .toList();
    }

    @Override
    public Optional<List<SortPriorityRule>> getDefaultValue() {
        return Optional.empty();
    }

    @Override
    public int getItemHeight() {
        return ROW_HEIGHT * (this.rows.size() + 1);
    }

    @Override
    public void updateSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float delta) {
        super.extractRenderState(graphics, index, y, x, entryWidth, entryHeight, mouseX, mouseY, isHovered, delta);

        int rowY = y;
        for (int i = 0; i < this.rows.size(); i++) {
            this.rows.get(i).extractRenderState(graphics, x, rowY, entryWidth, mouseX, mouseY, delta, this.isEditable(), this.isSelected, i);
            rowY += ROW_HEIGHT;
        }
        this.draftRow.extractRenderState(graphics, x, rowY, entryWidth, mouseX, mouseY, delta, this.isEditable(), this.isSelected, this.rows.size());
    }

    @Override
    public List<? extends GuiEventListener> children() {
        List<GuiEventListener> children = new ArrayList<>();
        this.rows.forEach(row -> children.addAll(row.widgets()));
        children.addAll(this.draftRow.widgets());
        return children;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        List<NarratableEntry> narratables = new ArrayList<>();
        this.rows.forEach(row -> narratables.addAll(row.widgets()));
        narratables.addAll(this.draftRow.widgets());
        return narratables;
    }

    private Optional<Component> firstConfigError() {
        return this.rows.stream()
                .map(row -> validationError(row.matchField.getValue()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .or(() -> validationError(this.draftRow.matchField.getValue()));
    }

    private void moveUp(RuleRow row) {
        int index = this.rows.indexOf(row);
        if (index > 0) {
            Collections.swap(this.rows, index, index - 1);
        }
    }

    private void moveDown(RuleRow row) {
        int index = this.rows.indexOf(row);
        if (index >= 0 && index < this.rows.size() - 1) {
            Collections.swap(this.rows, index, index + 1);
        }
    }

    private void delete(RuleRow row) {
        this.rows.remove(row);
    }

    private void addDraft() {
        if (validationError(this.draftRow.matchField.getValue()).isPresent() || this.draftRow.matchField.getValue().isBlank()) {
            return;
        }
        this.rows.add(existingRow(this.draftRow.toRule()));
        this.draftRow = draftRow();
    }

    private RuleRow existingRow(SortPriorityRule rule) {
        return new RuleRow(rule, false);
    }

    private RuleRow draftRow() {
        return new RuleRow(new SortPriorityRule("", SortPriorityPosition.DEFAULT), true);
    }

    private static boolean isSaveable(SortPriorityRule rule) {
        return rule.match() != null && !rule.match().isBlank();
    }

    private static Optional<Component> validationError(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return SortPriorityRules.validationError(value)
                .map(message -> Component.translatable("inventorysorter.config.sortPriorityRules.error", message));
    }

    private final class RuleRow {
        private final EditBox matchField;
        private final EnumListEntry<SortPriorityPosition> positionEntry;
        private final Button positionButton;
        private final Button upButton;
        private final Button downButton;
        private final Button deleteButton;
        private final Button addButton;
        private final boolean draft;

        private RuleRow(SortPriorityRule rule, boolean draft) {
            this.draft = draft;
            this.matchField = new EditBox(Minecraft.getInstance().font, 0, 0, 120, 18, SortPriorityRulesEntry.this.getFieldName()) {
                public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
                    this.setFocused(SortPriorityRulesEntry.this.isSelected && SortPriorityRulesEntry.this.getFocused() == this);
                    this.setTextColor(validationError(this.getValue()).isPresent() ? ERROR_TEXT_COLOR : TEXT_COLOR);
                    super.extractWidgetRenderState(graphics, mouseX, mouseY, delta);
                }
            };
            this.matchField.setMaxLength(999999);
            this.matchField.setValue(rule.match());
            this.matchField.moveCursorToStart(false);

            this.positionEntry = SortPriorityRulesEntry.this.entryBuilder
                    .startEnumSelector(Component.empty(), SortPriorityPosition.class, rule.position())
                    .setEnumNameProvider(position -> Component.translatable(((SortPriorityPosition) position).getTranslationKey()))
                    .build();
            this.positionButton = (Button) this.positionEntry.children().getFirst();
            this.upButton = Button.builder(Component.literal("^"), button -> SortPriorityRulesEntry.this.moveUp(this)).bounds(0, 0, SMALL_BUTTON_WIDTH, 20).build();
            this.downButton = Button.builder(Component.literal("v"), button -> SortPriorityRulesEntry.this.moveDown(this)).bounds(0, 0, SMALL_BUTTON_WIDTH, 20).build();
            this.deleteButton = Button.builder(Component.literal("X"), button -> SortPriorityRulesEntry.this.delete(this)).bounds(0, 0, SMALL_BUTTON_WIDTH, 20).build();
            this.addButton = Button.builder(Component.translatable("inventorysorter.config.sortPriorityRules.add"), button -> SortPriorityRulesEntry.this.addDraft()).bounds(0, 0, ADD_BUTTON_WIDTH, 20).build();
        }

        private void extractRenderState(GuiGraphicsExtractor graphics, int x, int y, int entryWidth, int mouseX, int mouseY, float delta, boolean editable, boolean selected, int rowIndex) {
            int controlsWidth = this.draft
                    ? POSITION_WIDTH + ADD_BUTTON_WIDTH + 2 * GAP
                    : POSITION_WIDTH + 3 * SMALL_BUTTON_WIDTH + 4 * GAP;
            int matchWidth = Math.max(80, entryWidth - controlsWidth);
            int controlX = x + matchWidth + GAP;

            this.matchField.setX(x);
            this.matchField.setY(y + 1);
            this.matchField.setWidth(matchWidth);
            this.matchField.setEditable(editable);

            this.positionEntry.setEditable(editable);
            this.positionButton.setX(controlX);
            this.positionButton.setY(y);
            this.positionButton.setWidth(POSITION_WIDTH);
            this.positionButton.setMessage(Component.translatable(this.positionEntry.getValue().getTranslationKey()));
            this.positionButton.active = editable;
            controlX += POSITION_WIDTH + GAP;

            if (this.draft) {
                this.addButton.setX(controlX);
                this.addButton.setY(y);
                this.addButton.active = editable && validationError(this.matchField.getValue()).isEmpty() && !this.matchField.getValue().isBlank();
                this.addButton.extractRenderState(graphics, mouseX, mouseY, delta);
            } else {
                this.upButton.setX(controlX);
                this.upButton.setY(y);
                this.upButton.active = editable && rowIndex > 0;
                controlX += SMALL_BUTTON_WIDTH + GAP;

                this.downButton.setX(controlX);
                this.downButton.setY(y);
                this.downButton.active = editable && rowIndex < SortPriorityRulesEntry.this.rows.size() - 1;
                controlX += SMALL_BUTTON_WIDTH + GAP;

                this.deleteButton.setX(controlX);
                this.deleteButton.setY(y);
                this.deleteButton.active = editable;

                this.upButton.extractRenderState(graphics, mouseX, mouseY, delta);
                this.downButton.extractRenderState(graphics, mouseX, mouseY, delta);
                this.deleteButton.extractRenderState(graphics, mouseX, mouseY, delta);
            }

            this.positionButton.extractRenderState(graphics, mouseX, mouseY, delta);
            this.matchField.extractRenderState(graphics, mouseX, mouseY, delta);
        }

        private List<AbstractWidget> widgets() {
            return this.draft
                    ? List.of(this.matchField, this.positionButton, this.addButton)
                    : List.of(this.matchField, this.positionButton, this.upButton, this.downButton, this.deleteButton);
        }

        private SortPriorityRule toRule() {
            return new SortPriorityRule(this.matchField.getValue(), this.positionEntry.getValue());
        }
    }
}
