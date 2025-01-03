package dulian.dulian.utils.rest_docs

import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath

class ResponseBodyBuilder {
    val responseBodyDescriptors = mutableListOf<FieldDescriptor>()

    /**
     * Add field to the response body
     *
     * @param name field name
     * @param description field description
     */
    fun field(
        name: String,
        description: String
    ) {
        responseBodyDescriptors.add(
            fieldWithPath(name).description(description)
        )
    }

    /**
     * Add field to the response body with prefix
     *
     * @param prefix prefix of the field
     * @param responseBodyPrefixBuilder lambda function to add fields with prefix
     */
    fun prefix(
        prefix: String,
        responseBodyPrefixBuilder: ResponseBodyPrefixBuilder.() -> Unit
    ) {
        ResponseBodyPrefixBuilder().apply(responseBodyPrefixBuilder).responseBodyDescriptorsWithPrefix.forEach {
            responseBodyDescriptors.add(
                fieldWithPath("$prefix.${it.path}").description(it.description)
            )
        }
    }

    fun commonField() {
        field("status", "상태")
        field("statusCode", "상태 코드")
        field("timestamp", "응답 시간")
    }

    class ResponseBodyPrefixBuilder {
        val responseBodyDescriptorsWithPrefix = mutableListOf<FieldDescriptor>()

        /**
         * Add field to the response body with prefix
         *
         * @param name field name
         * @param description field description
         */
        fun field(
            name: String,
            description: String
        ) {
            responseBodyDescriptorsWithPrefix.add(
                fieldWithPath(name).description(description)
            )
        }

        /**
         * Add field to the response body with prefix
         *
         * @param prefix prefix of the field
         * @param responseBodyPrefixBuilder lambda function to add fields with prefix
         */
        fun prefix(
            prefix: String,
            responseBodyPrefixBuilder: ResponseBodyPrefixBuilder.() -> Unit
        ) {
            ResponseBodyPrefixBuilder().apply(responseBodyPrefixBuilder).responseBodyDescriptorsWithPrefix.forEach {
                responseBodyDescriptorsWithPrefix.add(
                    fieldWithPath("$prefix.${it.path}").description(it.description)
                )
            }
        }
    }
}
