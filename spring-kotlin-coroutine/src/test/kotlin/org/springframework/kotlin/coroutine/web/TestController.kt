package org.springframework.kotlin.coroutine.web

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
open class TestController {
    @GetMapping("/multiply/{a}/{b}")
    open fun multiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int): Int = a*b

    @GetMapping("/notfound")
    open fun notfound(): ResponseEntity<Int> = ResponseEntity.notFound().build()
}