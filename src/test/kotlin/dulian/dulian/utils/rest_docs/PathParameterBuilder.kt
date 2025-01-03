package dulian.dulian.utils.rest_docs

import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName

class PathParameterBuilder {
    val pathParameterDescriptors = mutableListOf<ParameterDescriptor>()

    /**
     * Add field to the path
     *
     * @param name field name
     * @param description field description
     */
    fun field(
        name: String,
        description: String
    ) {
        pathParameterDescriptors.add(
            parameterWithName(name).description(description)
        )
    }
}