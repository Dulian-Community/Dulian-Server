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
}
