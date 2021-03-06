package org.sandboxpowered.silica.service;

import org.sandboxpowered.api.block.Block;
import org.sandboxpowered.api.capability.Capability;
import org.sandboxpowered.api.client.Client;
import org.sandboxpowered.api.content.Content;
import org.sandboxpowered.api.fluid.Fluid;
import org.sandboxpowered.api.fluid.FluidStack;
import org.sandboxpowered.api.item.Item;
import org.sandboxpowered.api.registry.Registry;
import org.sandboxpowered.api.server.Server;
import org.sandboxpowered.api.state.property.Property;
import org.sandboxpowered.api.tags.Tag;
import org.sandboxpowered.api.util.Identity;
import org.sandboxpowered.api.util.nbt.ReadableCompoundTag;
import org.sandboxpowered.api.util.text.Text;
import org.sandboxpowered.internal.InternalService;
import org.sandboxpowered.silica.block.SilicaBlockProperties;
import org.sandboxpowered.silica.registry.SilicaRegistries;

public class SilicaInternalService implements InternalService {
    @Override
    public <T> Capability<T> componentFunction(Class<T> c) {
        return null;
    }

    @Override
    public Text createLiteralText(String text) {
        return null;
    }

    @Override
    public Text createTranslatedText(String translation) {
        return null;
    }

    @Override
    public <T extends Content<T>> Registry<T> registryFunction(Class<T> c) {
        if (c == Block.class) {
            return SilicaRegistries.BLOCK_REGISTRY.cast();
        } else if (c == Item.class) {
            return SilicaRegistries.ITEM_REGISTRY.cast();
        } else if (c == Fluid.class) {
            return SilicaRegistries.FLUID_REGISTRY.cast();
        }
        return null;
    }

    @Override
    public <T extends Comparable<T>> Property<T> getProperty(String property) {
        Property<?> prop = switch (property) {
            case "attached" -> SilicaBlockProperties.ATTACHED;
            case "bottom" -> SilicaBlockProperties.BOTTOM;
            case "conditional" -> SilicaBlockProperties.CONDITIONAL;
            case "disarmed" -> SilicaBlockProperties.DISARMED;
            case "drag" -> SilicaBlockProperties.DRAG;
            case "enabled" -> SilicaBlockProperties.ENABLED;
            case "extended" -> SilicaBlockProperties.EXTENDED;
            case "eye" -> SilicaBlockProperties.EYE;
            case "falling" -> SilicaBlockProperties.FALLING;
            case "hanging" -> SilicaBlockProperties.HANGING;
            case "has_bottle_0" -> SilicaBlockProperties.HAS_BOTTLE_0;
            case "has_bottle_1" -> SilicaBlockProperties.HAS_BOTTLE_1;
            case "has_bottle_2" -> SilicaBlockProperties.HAS_BOTTLE_2;
            case "has_record" -> SilicaBlockProperties.HAS_RECORD;
            case "has_book" -> SilicaBlockProperties.HAS_BOOK;
            case "inverted" -> SilicaBlockProperties.INVERTED;
            case "in_wall" -> SilicaBlockProperties.IN_WALL;
            case "lit" -> SilicaBlockProperties.LIT;
            case "locked" -> SilicaBlockProperties.LOCKED;
            case "occupied" -> SilicaBlockProperties.OCCUPIED;
            case "open" -> SilicaBlockProperties.OPEN;
            case "persistent" -> SilicaBlockProperties.PERSISTENT;
            case "powered" -> SilicaBlockProperties.POWERED;
            case "short" -> SilicaBlockProperties.SHORT;
            case "signal_fire" -> SilicaBlockProperties.SIGNAL_FIRE;
            case "snowy" -> SilicaBlockProperties.SNOWY;
            case "triggered" -> SilicaBlockProperties.TRIGGERED;
            case "unstable" -> SilicaBlockProperties.UNSTABLE;
            case "waterlogged" -> SilicaBlockProperties.WATERLOGGED;
            case "up" -> SilicaBlockProperties.UP;
            case "down" -> SilicaBlockProperties.DOWN;
            case "north" -> SilicaBlockProperties.NORTH;
            case "east" -> SilicaBlockProperties.EAST;
            case "south" -> SilicaBlockProperties.SOUTH;
            case "west" -> SilicaBlockProperties.WEST;
            case "fluidlevel" -> SilicaBlockProperties.FLUID_LEVEL;
            case "age_1" -> SilicaBlockProperties.AGE_1;
            case "age_2" -> SilicaBlockProperties.AGE_2;
            case "age_3" -> SilicaBlockProperties.AGE_3;
            case "age_5" -> SilicaBlockProperties.AGE_5;
            case "age_7" -> SilicaBlockProperties.AGE_7;
            case "age_15" -> SilicaBlockProperties.AGE_15;
            case "age_25" -> SilicaBlockProperties.AGE_25;
            case "bites" -> SilicaBlockProperties.BITES;
            case "delay" -> SilicaBlockProperties.DELAY;
            case "distance_1_7" -> SilicaBlockProperties.DISTANCE_1_7;
            case "eggs" -> SilicaBlockProperties.EGGS;
            case "hatch" -> SilicaBlockProperties.HATCH;
            case "layers" -> SilicaBlockProperties.LAYERS;
            case "level_3" -> SilicaBlockProperties.LEVEL_3;
            case "level_8" -> SilicaBlockProperties.LEVEL_8;
            case "level_1_8" -> SilicaBlockProperties.LEVEL_1_8;
            case "level_15" -> SilicaBlockProperties.LEVEL_15;
            case "honey_level" -> SilicaBlockProperties.HONEY_LEVEL;
            case "moisture" -> SilicaBlockProperties.MOISTURE;
            case "note" -> SilicaBlockProperties.NOTE;
            case "pickles" -> SilicaBlockProperties.PICKLES;
            case "power" -> SilicaBlockProperties.POWER;
            case "stage" -> SilicaBlockProperties.STAGE;
            case "distance_0_7" -> SilicaBlockProperties.DISTANCE_0_7;
            case "rotation" -> SilicaBlockProperties.ROTATION;
            case "facing" -> SilicaBlockProperties.FACING;
            case "horizontal_facing" -> SilicaBlockProperties.HORIZONTAL_FACING;
            case "hopper_facing" -> SilicaBlockProperties.HOPPER_FACING;
            case "horizontal_axis" -> SilicaBlockProperties.HORIZONTAL_AXIS;
            case "axis" -> SilicaBlockProperties.AXIS;
            case "slab_type" -> SilicaBlockProperties.SLAB_TYPE;
            case "half" -> SilicaBlockProperties.HALF;
            case "hinge" -> SilicaBlockProperties.HINGE;
            case "bed_half" -> SilicaBlockProperties.BED_HALF;
            case "stair_shape" -> SilicaBlockProperties.STAIR_SHAPE;
            default -> throw new IllegalStateException("Unexpected value: " + property);
        };
        return (Property<T>) prop;
    }

    @Override
    public Server serverInstance() {
        return null;
    }

    @Override
    public FluidStack fluidStackFunction(Fluid fluid, int amount) {
        return null;
    }

    @Override
    public FluidStack fluidStackFromTagFunction(ReadableCompoundTag tag) {
        return null;
    }

    @Override
    public Identity.Variant createVariantIdentity(Identity identity, String variant) {
        return null;
    }

    @Override
    public Client clientInstance() {
        return null;
    }

    @Override
    public Tag<Block> getBlockTag(String string) {
        return null;
    }
}
