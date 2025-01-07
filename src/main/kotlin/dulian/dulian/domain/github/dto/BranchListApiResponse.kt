package dulian.dulian.domain.github.dto

data class BranchListApiResponse(
    val name: String
) {
    companion object {
        fun of(
            name: String
        ) = BranchListApiResponse(
            name = name
        )
    }
}