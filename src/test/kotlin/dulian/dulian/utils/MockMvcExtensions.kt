package dulian.dulian.utils

import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

fun MockMvcRequestBuilders.post(
    url: String,
    content: String
) {
    MockMvcRequestBuilders.post(url)
        .contentType(MediaType.APPLICATION_JSON)
        .content(content)
}