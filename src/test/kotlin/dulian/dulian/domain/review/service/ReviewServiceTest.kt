package dulian.dulian.domain.review.service

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser

@SpringBootTest
class ReviewServiceTest(
    @Autowired
    private val reviewService: ReviewService
) {

    @Test
    @WithMockUser(username = "13", roles = ["USER"])
    fun test() {
        reviewService.getRepositoryList()
    }
}