package com.netguard

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SocApplication

fun main(args: Array<String>) {
    runApplication<SocApplication>(*args)
}