package dulian.dulian.utils

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin

fun fixtureMonkey(): FixtureMonkey = FixtureMonkey.builder()
    .defaultNotNull(true)
    .plugin(KotlinPlugin())
    .plugin(JakartaValidationPlugin())
    .build()