package dev.chestwise.minecraft;

import dev.chestwise.core.ItemDescriptor;
import dev.chestwise.core.ItemIdentity;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings({"deprecation", "removal"})
public final class ItemStackIdentity {
    private ItemStackIdentity() {
    }

    public static ItemIdentity identity(ItemStack stack) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        //? if <= 1.20.1 {
        String data = stack.hasTag() ? stack.getTag().toString() : "";
        //?}
        /*? if > 1.20.1 {*/
        /*String data = stack.getComponentsPatch().toString();
        *//*?}*/
        return new ItemIdentity(id, data);
    }

    public static ItemDescriptor describe(ItemStack stack) {
        //? if < 26.2 {
        Set<String> tags = stack.getTags()
        //?} else {
        /*Set<String> tags = stack.getItem().builtInRegistryHolder().tags()
        *///?}
            .map(tag -> tag.location().toString())
            .collect(Collectors.toUnmodifiableSet());
        return new ItemDescriptor(
            identity(stack),
            stack.getHoverName().getString(),
            tags,
            BuiltInRegistries.ITEM.getId(stack.getItem())
        );
    }

    public static boolean matches(ItemStack stack, ItemIdentity identity) {
        return !stack.isEmpty() && identity(stack).equals(identity);
    }

    public static boolean sameVariant(ItemStack first, ItemStack second) {
        return !first.isEmpty() && !second.isEmpty() && identity(first).equals(identity(second));
    }
}
