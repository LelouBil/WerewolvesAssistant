package net.leloubil.werewolvesassistant.modules

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import org.koin.core.annotation.Singleton
import org.koin.core.scope.Scope


@Module
@ComponentScan("net.leloubil.werewolvesassistant")
class AppModule


expect class ContextWrapper {
    val context: Any
}

@Module
@Configuration
expect class PlatformSpecificModule()  {
    @Single
    fun providesContextWrapper(scope: Scope): ContextWrapper
}

@KoinApplication(
    modules = [AppModule::class, PlatformSpecificModule::class]
)
object KoinApp
