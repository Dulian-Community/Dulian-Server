package dulian.dulian.global.utils

import kotlin.random.Random


object RandomUtils {

    /**
     * 랜덤 숫자를 생성
     *
     * @param digits 생성할 숫자의 자릿수
     *
     * @return 생성된 랜덤 숫자
     */
    fun generateRandomNumbers(digits: Int): Int {
        require(digits > 0) { "digits must be greater than 0" }

        return (1..digits)
            .map { Random.nextInt(0, 10) }
            .joinToString("")
            .toInt()
    }
}
