package com.netguard.controller

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.*
import java.util.concurrent.ConcurrentHashMap

@RestController
@CrossOrigin(origins = ["*"])
class PacketController(val messagingTemplate: SimpMessagingTemplate) {

    // Configuración de Seguridad
    private val whitelist = setOf("127.0.0.1", "10.221.50.109")
    private val blacklistedIps = ConcurrentHashMap.newKeySet<String>()
    private val hostHistory = ConcurrentHashMap<String, MutableSet<Int>>()

    @PostMapping("/api/packet")
    fun receivePacket(@RequestBody packet: Map<String, Any>) {
        val srcIp = packet["src"] as String
        
        // 1. Filtro de Firewall: Si está bloqueada, se descarta
        if (blacklistedIps.contains(srcIp)) return

        val enrichedPacket = packet.toMutableMap()
        
        // 2. Lógica de Detección (Solo para IPs fuera de la Whitelist)
        if (!whitelist.contains(srcIp)) {
            val port = packet["port"] as Int
            val ports = hostHistory.getOrPut(srcIp) { ConcurrentHashMap.newKeySet() }
            ports.add(port)
            
            if (ports.size > 25) {
                enrichedPacket["status"] = "CRITICAL"
            } else {
                enrichedPacket["status"] = "LIVE"
            }
        } else {
            // Tu tráfico se marca como seguro
            enrichedPacket["status"] = "SAFE"
        }

        messagingTemplate.convertAndSend("/topic/metrics", enrichedPacket)
    }

    @PostMapping("/api/block")
    fun blockIp(@RequestBody payload: Map<String, String>) {
        payload["ip"]?.let { 
            blacklistedIps.add(it)
            println("🚫 IP BLOQUEADA: $it")
        }
    }
}