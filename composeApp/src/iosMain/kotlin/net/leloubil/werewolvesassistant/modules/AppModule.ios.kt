package net.leloubil.werewolvesassistant.modules

import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import org.koin.core.annotation.Singleton
import org.koin.core.scope.Scope

@Module
@Configuration
actual class PlatformSpecificModule actual constructor() {
    @Single
    actual fun providesContextWrapper(scope: Scope): ContextWrapper = ContextWrapper()
}

actual class ContextWrapper {
    actual val context: Any = Unit
}
