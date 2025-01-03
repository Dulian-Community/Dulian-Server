package dulian.dulian.utils.rest_docs

import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath

class RequestBodyBuilder {
    val requestBodyDescriptors = mutableListOf<FieldDescriptor>()

    /**
     * Add field to the request body
     *
     * @param name field name
     * @param description field description
     */
    fun field(
        name: String,
        description: String
    ) {
        requestBodyDescriptors.add(
            fieldWithPath(name).description(description)
        )
    }
}
