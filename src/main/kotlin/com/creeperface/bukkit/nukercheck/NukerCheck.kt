package com.creeperface.bukkit.nukercheck

import com.creeperface.bukkit.nukercheck.check.Nuker
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.plugin.java.JavaPlugin

class NukerCheck : JavaPlugin(), Listener {

    override fun onEnable() {
        this.server.pluginManager.registerEvents(this, this)
    }

    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        val p = e.player
        val b = e.block

        if (!Nuker.run(p, b)) {
            e.isCancelled = true
            p.sendMessage("${ChatColor.RED}Nuker detected")
        }
    }
}