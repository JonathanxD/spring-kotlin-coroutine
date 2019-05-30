/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.kotlin.coroutine.web

import kotlinx.coroutines.delay
import org.springframework.kotlin.coroutine.annotation.Coroutine
import org.springframework.kotlin.coroutine.context.DEFAULT_DISPATCHER
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
open class CoroutineController {
    @GetMapping("/suspendedMultiply/{a}/{b}")
    @Coroutine(DEFAULT_DISPATCHER)
    suspend open fun suspendedMultiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int): Int =
        a*b

    @GetMapping("/multiply/{a}/{b}")
    suspend open fun multiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int): Int =
        a*b

    @GetMapping("/delayedMultiply/{a}/{b}")
    suspend open fun delayedMultiply(@PathVariable("a") a: Int, @PathVariable("b") b: Int): Int {
        delay(1L)
        return a * b
    }
}