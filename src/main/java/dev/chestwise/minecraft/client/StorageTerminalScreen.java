package dev.chestwise.minecraft.client;

import dev.chestwise.platform.ChestwiseNetworking;
import dev.chestwise.core.AggregateCountFormat;
import dev.chestwise.core.SortMode;
import dev.chestwise.minecraft.StorageTerminalMenu;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
//? if < 26.2 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
*///?}
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.ChatFormatting;
//? if < 26.2 {
import net.minecraft.world.inventory.ClickType;
//?} else {
/*import net.minecraft.world.inventory.ContainerInput;
*///?}

public final class StorageTerminalScreen extends AbstractContainerScreen<StorageTerminalMenu> {
    private static final int BACKGROUND = 0xffc6c6c6;
    private static final int PANEL = 0xff373737;
    private static final int SLOT = 0xff8b8b8b;
    private EditBox search;
    private int debounceTicks;
    private boolean queryDirty;
    private ChestwiseClientPreferences preferences;
    private SortMode preferredSort;

    public StorageTerminalScreen(StorageTerminalMenu menu, Inventory inventory, Component title) {
        //? if < 26.2 {
        super(menu, inventory, title);
        imageWidth = 248;
        imageHeight = 222;
        //?} else {
        /*super(menu, inventory, title, 248, 222);
        *///?}
        inventoryLabelY = 128;
        titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();
        search = new EditBox(font, leftPos + 8, topPos + 17, 106, 12, Component.translatable("gui.chestwise.search"));
        search.setMaxLength(80);
        search.setHint(Component.translatable("gui.chestwise.search"));
        search.setResponder(ignored -> {
            queryDirty = true;
            debounceTicks = 5;
        });
        addRenderableWidget(search);
        addRenderableWidget(button(118, 16, 16, "<", StorageTerminalMenu.BUTTON_PREVIOUS));
        addRenderableWidget(button(136, 16, 16, ">", StorageTerminalMenu.BUTTON_NEXT));
        addRenderableWidget(button(154, 16, 14, "S", StorageTerminalMenu.BUTTON_SORT));
        addRenderableWidget(button(8, 124, 76, "gui.chestwise.deposit_matching", StorageTerminalMenu.BUTTON_DEPOSIT_MATCHING));
        addRenderableWidget(button(86, 124, 82, "gui.chestwise.deposit_all", StorageTerminalMenu.BUTTON_DEPOSIT_ALL));
        preferences = ChestwiseClientPreferences.load(
            minecraft.gameDirectory.toPath().resolve("config/chestwise-client.properties")
        );
        preferredSort = preferences.sortMode();
        int sortSteps = Math.floorMod(preferredSort.ordinal() - menu.sortMode().ordinal(), SortMode.values().length);
        if (minecraft.gameMode != null) {
            for (int step = 0; step < sortSteps; step++) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, StorageTerminalMenu.BUTTON_SORT);
            }
            for (int slot : preferences.protectedSlots()) {
                if (!menu.isProtectedInventorySlot(slot)) {
                    minecraft.gameMode.handleInventoryButtonClick(
                        menu.containerId,
                        StorageTerminalMenu.BUTTON_PROTECT_BASE + slot
                    );
                }
            }
        }
        setInitialFocus(search);
    }

    @Override
    protected void slotClicked(
        Slot slot,
        int slotId,
        int button,
        //? if < 26.2 {
        ClickType clickType
        //?} else {
        /*ContainerInput clickType
        *///?}
    ) {
        if (preferences != null && slot != null && slotId >= StorageTerminalMenu.PLAYER_START
            && button == 2
            //? if < 26.2 {
            && clickType == ClickType.PICKUP
            //?} else {
            /*&& clickType == ContainerInput.PICKUP
            *///?}
        ) {
            preferences.toggleProtectedSlot(slot.getContainerSlot());
        }
        super.slotClicked(slot, slotId, button, clickType);
    }

    private Button button(int x, int y, int width, String label, int id) {
        Component text = label.length() == 1 ? Component.literal(label) : Component.translatable(label);
        return Button.builder(text, ignored -> sendButton(id))
            .bounds(leftPos + x, topPos + y, width, 14)
            .build();
    }

    private void sendButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            if (id == StorageTerminalMenu.BUTTON_SORT && preferences != null) {
                preferredSort = SortMode.values()[(preferredSort.ordinal() + 1) % SortMode.values().length];
                preferences.setSortMode(preferredSort);
            }
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    //? if <= 1.20.1 {
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        double vertical = delta;
    //?} else {
    /*public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
    *///?}
        if (preferences != null && preferences.mouseWheelTransfer()
            && hoveredSlot != null && hoveredSlot.index < StorageTerminalMenu.DISPLAY_SLOTS && vertical != 0.0) {
            int base = vertical > 0.0
                ? StorageTerminalMenu.BUTTON_SCROLL_WITHDRAW_BASE
                : StorageTerminalMenu.BUTTON_SCROLL_DEPOSIT_BASE;
            sendButton(base + hoveredSlot.index);
            return true;
        }
        //? if <= 1.20.1 {
        return super.mouseScrolled(mouseX, mouseY, delta);
        //?} else {
        /*return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
        *///?}
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        //? if <= 1.20.1 {
        search.tick();
        //?}
        if (queryDirty && --debounceTicks <= 0) {
            queryDirty = false;
            ChestwiseNetworking.sendSearch(search.getValue());
        }
    }

    //? if < 26.2 {
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //? if <= 1.20.1 {
        renderBackground(graphics);
        //?}
        /*? if > 1.20.1 {*/
        /*renderBackground(graphics, mouseX, mouseY, partialTick);
        *//*?}*/
        super.render(graphics, mouseX, mouseY, partialTick);
        renderAggregatedCounts(graphics);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderAggregatedCounts(GuiGraphics graphics) {
        for (int slot = 0; slot < StorageTerminalMenu.DISPLAY_SLOTS; slot++) {
            long count = menu.displayCount(slot);
            if (count <= 1) {
                continue;
            }
            String label = AggregateCountFormat.compact(count);
            int x = leftPos + 8 + slot % StorageTerminalMenu.COLUMNS * 18 + 16 - font.width(label);
            int y = topPos + 32 + slot / StorageTerminalMenu.COLUMNS * 18 + 8;
            graphics.drawString(font, label, x, y, 0xffffff, true);
        }
    }
    //?}

    /*? if >= 26.2 {*/
    /*@Override
    protected void extractSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY) {
        super.extractSlot(graphics, slot, mouseX, mouseY);
        if (slot.index >= StorageTerminalMenu.DISPLAY_SLOTS) {
            return;
        }
        long count = menu.displayCount(slot.index);
        if (count <= 1) {
            return;
        }
        String label = AggregateCountFormat.compact(count);
        graphics.text(font, Component.literal(label), slot.x + 16 - font.width(label), slot.y + 8, 0xffffff, true);
    }
    *//*?}*/

    @Override
    protected List<Component> getTooltipFromContainerItem(net.minecraft.world.item.ItemStack stack) {
        List<Component> tooltip = new ArrayList<>(super.getTooltipFromContainerItem(stack));
        if (hoveredSlot != null && hoveredSlot.index < StorageTerminalMenu.DISPLAY_SLOTS) {
            long count = menu.displayCount(hoveredSlot.index);
            if (count > 0) {
                tooltip.add(Component.translatable("gui.chestwise.total", Long.toString(count)).withStyle(ChatFormatting.GRAY));
            }
        }
        return tooltip;
    }

    @Override
    //? if < 26.2 {
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
    //?} else {
    /*public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
    *///?}
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, BACKGROUND);
        graphics.fill(leftPos + 4, topPos + 13, leftPos + imageWidth - 4, topPos + 122, PANEL);
        for (int slot = 0; slot < StorageTerminalMenu.DISPLAY_SLOTS; slot++) {
            int x = leftPos + 7 + slot % StorageTerminalMenu.COLUMNS * 18;
            int y = topPos + 31 + slot / StorageTerminalMenu.COLUMNS * 18;
            graphics.fill(x, y, x + 18, y + 18, 0xff202020);
            graphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT);
        }
        graphics.fill(leftPos + 4, topPos + 137, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xffa0a0a0);
        for (int slot = 0; slot < 9; slot++) {
            int x = leftPos + 183 + slot % 3 * 18;
            int y = topPos + 141 + slot / 3 * 18;
            graphics.fill(x, y, x + 18, y + 18, 0xff555555);
            graphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT);
        }
        graphics.fill(leftPos + 219, topPos + 159, leftPos + 237, topPos + 177, 0xff555555);
        graphics.fill(leftPos + 220, topPos + 160, leftPos + 236, topPos + 176, SLOT);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int inventorySlot = column + row * 9 + 9;
                drawPlayerSlot(graphics, 7 + column * 18, 139 + row * 18, inventorySlot);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawPlayerSlot(graphics, 7 + column * 18, 197, column);
        }
    }

    private void drawPlayerSlot(
        //? if < 26.2 {
        GuiGraphics graphics,
        //?} else {
        /*GuiGraphicsExtractor graphics,
        *///?}
        int x,
        int y,
        int inventorySlot
    ) {
        int inner = menu.isProtectedInventorySlot(inventorySlot) ? 0xffb07a2a : SLOT;
        graphics.fill(leftPos + x, topPos + y, leftPos + x + 18, topPos + y + 18, 0xff555555);
        graphics.fill(leftPos + x + 1, topPos + y + 1, leftPos + x + 17, topPos + y + 17, inner);
    }

    @Override
    //? if < 26.2 {
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    //?} else {
    /*protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    *///?}
        //? if < 26.2 {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x303030, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x303030, false);
        graphics.drawString(font, Component.translatable("gui.chestwise.crafting"), 184, inventoryLabelY, 0x303030, false);
        Component pageText = Component.translatable("gui.chestwise.page", menu.page() + 1, menu.totalPages());
        graphics.drawString(font, pageText, 116, 6, 0x303030, false);
        graphics.drawString(font, sortLabel(), 171, 19, 0xe0e0e0, false);
        //?} else {
        /*graphics.text(font, title, titleLabelX, titleLabelY, 0x303030, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x303030, false);
        graphics.text(font, Component.translatable("gui.chestwise.crafting"), 184, inventoryLabelY, 0x303030, false);
        Component pageText = Component.translatable("gui.chestwise.page", menu.page() + 1, menu.totalPages());
        graphics.text(font, pageText, 116, 6, 0x303030, false);
        graphics.text(font, sortLabel(), 171, 19, 0xe0e0e0, false);
        *///?}
    }

    private Component sortLabel() {
        String name = menu.sortMode().name().toLowerCase(Locale.ROOT);
        return Component.translatable("gui.chestwise.sort." + name);
    }

    @Override
    //? if < 26.2 {
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (search.keyPressed(keyCode, scanCode, modifiers) || search.canConsumeInput()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    //?} else {
    /*public boolean keyPressed(KeyEvent event) {
        if (search.keyPressed(event) || search.canConsumeInput()) {
            return true;
        }
        return super.keyPressed(event);
    }
    *///?}
}
