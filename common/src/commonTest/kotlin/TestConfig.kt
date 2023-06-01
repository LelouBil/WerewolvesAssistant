import io.github.aakira.napier.Napier
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.core.extensions.RuntimeTagExpressionExtension
import io.kotest.core.test.config.TestCaseConfig
import net.leloubil.common.CustomAntiLog

class TestConfig : AbstractProjectConfig() {
    override val coroutineTestScope: Boolean = true
    override suspend fun beforeProject() {
        Napier.base(CustomAntiLog())
    }
}
