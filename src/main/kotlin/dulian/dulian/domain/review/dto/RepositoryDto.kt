package dulian.dulian.domain.review.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class RepositoryDto(
    val id: Long,
    val name: String,
    @field:JsonProperty("full_name") val fullName: String,
    val private: Boolean,
    val language: String,
) {

}
