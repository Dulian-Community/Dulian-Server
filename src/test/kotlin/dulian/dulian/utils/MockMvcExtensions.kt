package dulian.dulian.utils

import dulian.dulian.utils.rest_docs.CustomRestDocsBuilder
import org.springframework.test.web.servlet.MockMvc

fun MockMvc.makeDocument(
    identifier: String,
    tag: String,
    summary: String,
    customRestDocsBuilder: CustomRestDocsBuilder.() -> Unit
) {
    CustomRestDocsBuilder(this, identifier, tag, summary).apply(customRestDocsBuilder).build()
}
