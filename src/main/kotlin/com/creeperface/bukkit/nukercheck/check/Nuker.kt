package com.creeperface.bukkit.nukercheck.check

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.util.BlockIterator
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.*

object Nuker {

    fun run(p: Player, b: Block): Boolean {
        val pos = p.location.add(0.0, p.eyeHeight, 0.0)
        val bb = b.boundingBox
        val blockLoc = b.location

        bb.expand(-0.01)

        val positions = HashSet<Vector>()

        if (pos.x > bb.maxX || pos.x < bb.minX) {
            if (pos.x > b.x) {
                //SIDE EAST
                positions.add(Vector(bb.maxX, bb.maxY, bb.maxZ))
                positions.add(Vector(bb.maxX, bb.maxY, bb.minZ))
                positions.add(Vector(bb.maxX, bb.minY, bb.minZ))
                positions.add(Vector(bb.maxX, bb.minY, bb.maxZ))
            } else {
                //SIDE WEST
                positions.add(Vector(bb.minX, bb.maxY, bb.maxZ))
                positions.add(Vector(bb.minX, bb.maxY, bb.minZ))
                positions.add(Vector(bb.minX, bb.minY, bb.minZ))
                positions.add(Vector(bb.minX, bb.minY, bb.maxZ))
            }
        }

        if (pos.z > bb.maxZ || pos.z < bb.minZ) {
            if (pos.z > b.z) {
                //SIDE SOUTH
                positions.add(Vector(bb.minX, bb.maxY, bb.maxZ))
                positions.add(Vector(bb.maxX, bb.minY, bb.maxZ))
                positions.add(Vector(bb.minX, bb.minY, bb.maxZ))
                positions.add(Vector(bb.maxX, bb.maxY, bb.maxZ))
            } else {
                //SIDE NORTH
                positions.add(Vector(bb.minX, bb.maxY, bb.minZ))
                positions.add(Vector(bb.maxX, bb.minY, bb.minZ))
                positions.add(Vector(bb.minX, bb.minY, bb.minZ))
                positions.add(Vector(bb.maxX, bb.maxY, bb.minZ))
            }
        }

        if (pos.y > bb.maxY || pos.y < bb.minY) {
            if (pos.y > b.y) {
                //SIDE UP
                positions.add(Vector(bb.minX, bb.maxY, bb.minZ))
                positions.add(Vector(bb.maxX, bb.maxY, bb.minZ))
                positions.add(Vector(bb.minX, bb.maxY, bb.maxZ))
                positions.add(Vector(bb.maxX, bb.maxY, bb.maxZ))
            } else {
                //SIDE DOWN
                positions.add(Vector(bb.minX, bb.minY, bb.minZ))
                positions.add(Vector(bb.maxX, bb.minY, bb.minZ))
                positions.add(Vector(bb.minX, bb.minY, bb.maxZ))
                positions.add(Vector(bb.maxX, bb.minY, bb.maxZ))
            }
        }

        if (positions.isEmpty()) { //inside the block probably
            return true
        }

        val posVector = pos.toVector()
        for (corner in positions) {
            val x = corner.x - pos.x
            val y = corner.y - pos.y
            val z = corner.z - pos.z

            val diff = abs(x) + abs(z)

            val yaw = Math.toDegrees(-atan2(x / diff, z / diff))
            val pitch = if (y == 0.toDouble()) 0.toDouble() else Math.toDegrees(-atan2(y, sqrt(x * x + z * z)))
            val found = getTargetBlock(
                Location(pos.world, pos.x, pos.y, pos.z, yaw.toFloat(), pitch.toFloat()),
                blockLoc,
                ceil(corner.distance(posVector) + 2).toInt(),
                Material.AIR.ordinal
            )

            if (corner.distanceSquared(posVector) <= 0.25 || found == b) {
                return true
            } else if (found == null) {
                return false
            }
        }

        return false
    }

    private fun getTargetBlock(pos: Location, target: Location, maxDistance: Int, vararg transparent: Int): Block? {
        try {
            val blocks = rayTrace(pos, target, maxDistance, 1, *transparent)

            val block = blocks[0]

            if (transparent.isNotEmpty()) {
                if (Arrays.binarySearch(transparent, block.type.ordinal) < 0) {
                    return block
                }
            } else {
                return block
            }
        } catch (ignored: Exception) {

        }

        return null
    }

    private fun rayTrace(
        pos: Location,
        target: Location,
        maxDistance: Int,
        maxLength: Int,
        vararg transparent: Int
    ): Array<Block> {
        val blocks = ArrayList<Block>()

        val direction = pos.direction
        val itr = BlockIterator(pos.world!!, pos.toVector(), direction, 0.0, max(120, maxDistance))

        while (itr.hasNext()) {
            val block = itr.next()
            blocks.add(block)

            if (maxLength != 0 && blocks.size > maxLength) {
                blocks.removeAt(0)
            }

            if (block.type.isOccluding || block.location == target) {
                break
            }
        }

        return blocks.toTypedArray()
    }
}