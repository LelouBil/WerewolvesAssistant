package net.leloubil.werewolvesassistant.modules

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module


@Module
@ComponentScan("net.leloubil.werewolvesassistant")
@Configuration
class AppModule

@KoinApplication
object KoinApp
