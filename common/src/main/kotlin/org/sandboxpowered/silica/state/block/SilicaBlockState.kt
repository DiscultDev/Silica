package org.sandboxpowered.silica.state.block

import com.google.common.collect.ImmutableMap
import org.sandboxpowered.api.block.Block
import org.sandboxpowered.api.state.BlockState
import org.sandboxpowered.api.state.property.Property
import org.sandboxpowered.api.util.math.Position
import org.sandboxpowered.api.world.World
import org.sandboxpowered.silica.state.BaseState

class SilicaBlockState(base: Block, properties: ImmutableMap<Property<*>, Comparable<*>>) :
    BaseState<Block, BlockState>(base, properties), BlockState {
    override fun getBlock(): Block {
        return base
    }

    override fun getDestroySpeed(world: World, pos: Position): Float {
        return 1f
    }
}