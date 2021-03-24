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

package org.springframework.data.mongodb.repository.query

import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.data.mapping.model.SpELExpressionEvaluator
import org.springframework.data.mongodb.core.CoroutineMongoOperations
import org.springframework.data.mongodb.core.ReactiveDatabaseCallback
import org.springframework.data.mongodb.core.query.BasicQuery
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.util.json.ParameterBindingContext
import org.springframework.data.mongodb.util.json.ParameterBindingDocumentCodec
import org.springframework.data.mongodb.util.json.ValueProvider
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider
import org.springframework.data.spel.ExpressionDependencies
import org.springframework.expression.ExpressionParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.util.Assert
import reactor.core.publisher.Mono
import java.util.ArrayList

open class CoroutineStringBasedMongoQuery(
        query: String,
        method: CoroutineMongoQueryMethod,
        operations: CoroutineMongoOperations,
        expressionParser: ExpressionParser,
        evaluationContextProvider: QueryMethodEvaluationContextProvider
): AbstractCoroutineMongoQuery(method, operations, expressionParser, evaluationContextProvider) {

    /*
    String query, MongoQueryMethod method, MongoOperations mongoOperations,
			ExpressionParser expressionParser, QueryMethodEvaluationContextProvider evaluationContextProvider
     */

    private val COUNT_EXISTS_AND_DELETE = "Manually defined query for %s cannot be a count and exists or delete query at the same time!"
    private val LOG = LoggerFactory.getLogger(CoroutineStringBasedMongoQuery::class.java)

    private val query: String
    private val fieldSpec: String?
    private val isCountQuery: Boolean
    private val isDeleteQuery: Boolean
    private val isExistsQuery: Boolean
    private val expressionParser: ExpressionParser
    private val evaluationContextProvider: QueryMethodEvaluationContextProvider

    constructor(method: CoroutineMongoQueryMethod, mongoOperations: CoroutineMongoOperations,
                expressionParser: ExpressionParser, evaluationContextProvider: QueryMethodEvaluationContextProvider):
            this(method.annotatedQuery!!, method, mongoOperations, expressionParser, evaluationContextProvider)

    init {
        Assert.notNull(query, "Query must not be null!")
        Assert.notNull(expressionParser, "SpelExpressionParser must not be null!")

        this.query = query
        this.expressionParser = expressionParser
        this.evaluationContextProvider = evaluationContextProvider
        fieldSpec = method.fieldSpecification

        if (method.hasAnnotatedQuery()) {
            val queryAnnotation = method.queryAnnotation!!
            this.isCountQuery = queryAnnotation.count
            this.isExistsQuery = queryAnnotation.exists
            this.isDeleteQuery = queryAnnotation.delete

            require(
                !(BooleanUtil.countBooleanTrueValues(isCountQuery, isExistsQuery, isDeleteQuery) > 1)
            ) { String.format(COUNT_EXISTS_AND_DELETE, method) }
        } else {
            isCountQuery = false
            isExistsQuery = false
            isDeleteQuery = false
        }
    }

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.mongodb.repository.query.AbstractMongoQuery#createQuery(org.springframework.data.mongodb.repository.query.ConvertingParameterAccessor)
	 */
    override fun createQuery(accessor: ConvertingParameterAccessor): Query {

        val codec: ParameterBindingDocumentCodec = getParameterBindingCodec()

        val queryObject = codec.decode(query, getBindingContext(query, accessor, codec))
        val fieldsObject = codec.decode(fieldSpec, getBindingContext(fieldSpec!!, accessor, codec))

        val query = BasicQuery(queryObject, fieldsObject).with(accessor.sort)

        if (LOG.isDebugEnabled) {
            LOG.debug(
                String.format(
                    "Created query %s for %s fields.",
                    query.queryObject,
                    query.fieldsObject
                )
            )
        }

        return query
    }

    private fun getBindingContext(
        json: String, accessor: ConvertingParameterAccessor,
        codec: ParameterBindingDocumentCodec
    ): ParameterBindingContext {
        val dependencies = codec.captureExpressionDependencies(
            json, { index: Int ->
                accessor.getBindableValue(
                    index
                )
            },
            expressionParser
        )
        val evaluator: SpELExpressionEvaluator = getSpELExpressionEvaluatorFor(dependencies, accessor)
        return ParameterBindingContext({ index: Int -> accessor.getBindableValue(index) }, evaluator)
    }

    private fun getParameterBindingCodec(): ParameterBindingDocumentCodec {
        return ParameterBindingDocumentCodec(getCodecRegistry())
    }

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.mongodb.repository.query.AbstractMongoQuery#isCountQuery()
	 */
    override fun isCountQuery() = isCountQuery

    /*
	 * (non-Javadoc)
	 * @see org.springframework.data.mongodb.repository.query.AbstractMongoQuery#isDeleteQuery()
	 */
    override fun isDeleteQuery() = isDeleteQuery
}