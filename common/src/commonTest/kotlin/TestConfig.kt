import io.github.aakira.napier.Napier
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.core.extensions.RuntimeTagExpressionExtension
import net.leloubil.common.CustomAntiLog

class TestConfig : AbstractProjectConfig() {
    override val coroutineTestScope: Boolean = true
    override fun extensions(): List<Extension> = listOf(RuntimeTagExpressionExtension("focus"))

    override suspend fun beforeProject() {
        Napier.base(CustomAntiLog())
    }
}
