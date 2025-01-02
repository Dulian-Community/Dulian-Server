//package dulian.dulian.domain.board.service
//
//import dulian.dulian.domain.board.entity.Board
//import dulian.dulian.utils.fixtureMonkey
//import io.kotest.matchers.shouldNotBe
//import org.junit.jupiter.api.RepeatedTest
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//
//@SpringBootTest
//class BoardServiceTest1(
//    @Autowired
//    private val boardLikeService: BoardLikeService
//) {
////    @Test
////    @WithMockUser(username = "test1234", roles = ["USER"])
////    fun test() {
////        val numberOfThreads = 10
////        val executorService = Executors.newFixedThreadPool(numberOfThreads)
////
////        repeat(numberOfThreads) {
////            executorService.submit {
////                boardLikeService.like(5L)
////            }
////        }
////
////        executorService.shutdown()
////        executorService.awaitTermination(1, TimeUnit.MINUTES)
////    }
//
//    @RepeatedTest(100)
//    fun test() {
//        val monkey = fixtureMonkey()
//        val test = monkey.giveMeBuilder(Board::class.java)
//            .setNotNull("boardId")
//            .sample()
//
//        println(test.boardId)
//        test.boardId shouldNotBe null
//    }
//}