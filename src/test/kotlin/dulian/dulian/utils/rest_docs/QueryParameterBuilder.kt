package dulian.dulian.utils.rest_docs

import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName

class QueryParameterBuilder {
    val queryParameterDescriptors = mutableListOf<ParameterDescriptor>()

    /**
     * Add query parameter to the request
     *
     * @param name name of the parameter
     * @param description description of the parameter
     */
    fun param(
        name: String,
        description: String
    ) {
        queryParameterDescriptors.add(
            parameterWithName(name).description(description)
        )
    }
}