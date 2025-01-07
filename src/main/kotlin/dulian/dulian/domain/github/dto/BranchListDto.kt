package dulian.dulian.domain.github.dto

class BranchListDto {
    data class Response(
        val result: List<Branch>,
        val totalElements: Int
    ) {
        companion object {
            fun of(
                responses: List<BranchListApiResponse>
            ) = Response(
                result = responses.map {
                    Branch.of(it)
                },
                totalElements = responses.size
            )
        }

        data class Branch(
            val name: String
        ) {
            companion object {
                fun of(
                    response: BranchListApiResponse
                ) = Branch(
                    name = response.name
                )
            }
        }
    }
}