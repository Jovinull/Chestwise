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
    /** GLFW escape key. A focused search box reports canConsumeInput() for every
     *  key, which swallowed the close and left the mouse as the only way out. */
    private static final int KEY_ESCAPE = 256;
    /** Five 16x16 glyphs in a row: search, deposit, quick stack, restock, locate. */
    //? if < 26.2 {
    private static final net.minecraft.resources.ResourceLocation ICONS =
    //?} else {
    /*private static final net.minecraft.resources.Identifier ICONS =
    *///?}
        dev.chestwise.minecraft.ChestwiseContent.id("textures/gui/terminal_icons.png");
    private static final int ICON_SHEET_WIDTH = 80;
    private static final int ICON_SIZE = 16;
    private static final int ICON_SEARCH = 0;

    private static final int BACKGROUND = 0xffc6c6c6;
    private static final int PANEL = 0xff373737;
    private static final int SLOT = 0xff8b8b8b;
    private EditBox search;
    private int debounceTicks;
    private boolean queryDirty;
    private ChestwiseClientPreferences preferences;
    private SortMode preferredSort;
    private Button sortButton;

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
        search = new EditBox(font, leftPos + 24, topPos + 17, 80, 12, Component.translatable("gui.chestwise.search"));
        search.setMaxLength(80);
        search.setHint(Component.translatable("gui.chestwise.search"));
        search.setResponder(ignored -> {
            queryDirty = true;
            debounceTicks = 5;
        });
        addRenderableWidget(search);
        addRenderableWidget(button(108, 16, 14, "<", StorageTerminalMenu.BUTTON_PREVIOUS));
        addRenderableWidget(button(124, 16, 14, ">", StorageTerminalMenu.BUTTON_NEXT));
        // The sort control now names the mode it will apply, so no loose label
        // is needed beside it.
        sortButton = Button.builder(sortLabel(), ignored -> sendButton(StorageTerminalMenu.BUTTON_SORT))
            .bounds(leftPos + 142, topPos + 16, 28, 14)
            .build();
        addRenderableWidget(sortButton);
        // Kept clear of the inventory label, which used to sit underneath them.
        addRenderableWidget(button(174, 141, 68, "gui.chestwise.deposit_matching", StorageTerminalMenu.BUTTON_DEPOSIT_MATCHING));
        addRenderableWidget(button(174, 161, 68, "gui.chestwise.deposit_all", StorageTerminalMenu.BUTTON_DEPOSIT_ALL));
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
        if (sortButton != null) {
            sortButton.setMessage(sortLabel());
        }
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
        // Items render at z=150 and vanilla stack counts at z=200, so drawing at
        // the default depth would bury these behind the item sprite.
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 300.0F);
        for (int slot = 0; slot < StorageTerminalMenu.DISPLAY_SLOTS; slot++) {
            long count = menu.displayCount(slot);
            if (count <= 1) {
                continue;
            }
            String label = AggregateCountFormat.compact(count);
            // Same offsets vanilla uses for a stack count, so both read alike.
            int x = leftPos + 8 + slot % StorageTerminalMenu.COLUMNS * 18 + 17 - font.width(label);
            int y = topPos + 32 + slot / StorageTerminalMenu.COLUMNS * 18 + 9;
            graphics.drawString(font, label, x, y, 0xffffff, true);
        }
        graphics.pose().popPose();
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
        graphics.text(font, Component.literal(label), slot.x + 17 - font.width(label), slot.y + 9, 0xffffff, true);
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
        // Crafting now occupies the empty right-hand third of the upper panel
        // instead of being wedged beside the player inventory.
        for (int slot = 0; slot < 9; slot++) {
            int x = leftPos + 180 + slot % 3 * 18;
            int y = topPos + 31 + slot / 3 * 18;
            graphics.fill(x, y, x + 18, y + 18, 0xff202020);
            graphics.fill(x + 1, y + 1, x + 17, y + 17, SLOT);
        }
        drawResultArrow(graphics);
        // Labels the search field without spending any of its width on text.
        drawIcon(graphics, ICON_SEARCH, 6, 15);
        graphics.fill(leftPos + 197, topPos + 98, leftPos + 217, topPos + 118, 0xff8b8b8b);
        graphics.fill(leftPos + 198, topPos + 99, leftPos + 216, topPos + 117, 0xff202020);
        graphics.fill(leftPos + 199, topPos + 100, leftPos + 215, topPos + 116, SLOT);
        graphics.fill(leftPos + 4, topPos + 137, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xffa0a0a0);
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

    private void drawIcon(
        //? if < 26.2 {
        GuiGraphics graphics,
        //?} else {
        /*GuiGraphicsExtractor graphics,
        *///?}
        int index,
        int x,
        int y
    ) {
        //? if < 26.2 {
        graphics.blit(ICONS, leftPos + x, topPos + y, index * ICON_SIZE, 0,
            ICON_SIZE, ICON_SIZE, ICON_SHEET_WIDTH, ICON_SIZE);
        //?} else {
        /*graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, ICONS,
            leftPos + x, topPos + y, index * ICON_SIZE, 0,
            ICON_SIZE, ICON_SIZE, ICON_SHEET_WIDTH, ICON_SIZE);
        *///?}
    }

    private void drawResultArrow(
        //? if < 26.2 {
        GuiGraphics graphics
        //?} else {
        /*GuiGraphicsExtractor graphics
        *///?}
    ) {
        int shaft = 0xff8b8b8b;
        graphics.fill(leftPos + 205, topPos + 87, leftPos + 210, topPos + 92, shaft);
        for (int step = 0; step < 5; step++) {
            graphics.fill(leftPos + 202 + step, topPos + 92 + step, leftPos + 213 - step, topPos + 93 + step, shaft);
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
        // Sits on the dark panel now, so it needs the light ink.
        graphics.drawString(font, Component.translatable("gui.chestwise.crafting"), 180, 19, 0xe0e0e0, false);
        Component pageText = Component.translatable("gui.chestwise.page", menu.page() + 1, menu.totalPages());
        graphics.drawString(font, pageText, 116, 6, 0x303030, false);
        //?} else {
        /*graphics.text(font, title, titleLabelX, titleLabelY, 0x303030, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x303030, false);
        // Sits on the dark panel now, so it needs the light ink.
        graphics.text(font, Component.translatable("gui.chestwise.crafting"), 180, 19, 0xe0e0e0, false);
        Component pageText = Component.translatable("gui.chestwise.page", menu.page() + 1, menu.totalPages());
        graphics.text(font, pageText, 116, 6, 0x303030, false);
        *///?}
    }

    private Component sortLabel() {
        String name = menu.sortMode().name().toLowerCase(Locale.ROOT);
        return Component.translatable("gui.chestwise.sort." + name);
    }

    @Override
    //? if < 26.2 {
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode != KEY_ESCAPE
            && (search.keyPressed(keyCode, scanCode, modifiers) || search.canConsumeInput())) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    //?} else {
    /*public boolean keyPressed(KeyEvent event) {
        if (event.key() != KEY_ESCAPE && (search.keyPressed(event) || search.canConsumeInput())) {
            return true;
        }
        return super.keyPressed(event);
    }
    *///?}
}
