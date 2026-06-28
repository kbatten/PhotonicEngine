import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar

val phConfig = parent!!.extensions.getByName<PhotonicsExtension>("photonics")

loom {
    runConfigs {
        configureEach {
            generateRunConfig = true
        }
    }
}

dependencies {
    add("minecraft", "com.mojang:minecraft:${phConfig.minecraft.get()}")
    phConfig._dependencyBlock.orNull?.execute(PhotonicsCommonDependenciesScope(this))

    val fabricLoader = _fabricLoader

    // Fabric loader is needed on common for mixin dependency.
    // Why not just include the mixin dependency raw? I have no clue, ask architectury.
    if (fabricLoader != null) {
        add("implementation", fabricLoader)
    }
}

// Minecraft 26.2 ships de-obfuscated, so Loom performs no remap step.
// The shaded jar is therefore the final mod jar; make `build`/`assemble` produce it.
tasks {
    named("assemble") {
        dependsOn(shadowJar)
    }
}
