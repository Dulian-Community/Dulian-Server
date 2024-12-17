package dulian.dulian.domain.board.controller

import dulian.dulian.domain.board.dto.GeneralBoardAddDto
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/board")
class BoardController {

    @PostMapping
    fun addBoard(
        @RequestBody @Valid request: GeneralBoardAddDto.Request
    ) {
        println(request.images)
    }
}
