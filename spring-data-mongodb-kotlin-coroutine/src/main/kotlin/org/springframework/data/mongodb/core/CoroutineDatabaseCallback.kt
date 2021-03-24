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

package org.springframework.data.mongodb.core

import com.mongodb.reactivestreams.client.MongoDatabase
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.openSubscription

interface CoroutineDatabaseCallback<T: Any> {
    val reactiveDatabaseCallback: ReactiveDatabaseCallback<T>

    fun doInDB(db: MongoDatabase): Flow<T>

    companion object {
        operator fun <T: Any> invoke(callback:  ReactiveDatabaseCallback<T>): CoroutineDatabaseCallback<T> = object: CoroutineDatabaseCallback<T> {
            override val reactiveDatabaseCallback: ReactiveDatabaseCallback<T>
                get() = callback

            override fun doInDB(db: MongoDatabase): Flow<T> =
                callback.doInDB(db).asFlow()
        }
    }
}