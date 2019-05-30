/*
 * Copyright 2017 the original author or authors.
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

package org.springframework.kotlin.coroutine.reactive

import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Role
import org.springframework.core.ReactiveAdapterRegistry
import org.springframework.kotlin.coroutine.registerReceiveChannel

@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
internal open class CoroutineReactiveAdapterRegistryConfiguration {
    @Bean
    open fun reactiveAdapterRegistryInit(): InitializingBean = InitializingBean {
        ReactiveAdapterRegistry.getSharedInstance().registerReceiveChannel()
    }
}