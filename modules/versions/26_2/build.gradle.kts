val mainLibs = libs262

photonics {
    minecraft = mainLibs.versions.minecraft.get()
    javaVersion = JavaVersion.VERSION_25

    commonDependencies {
         //Use by fabric (for obvious reasons) & common for mixin dependencies
        fabricLoader(mainLibs.fabric.loader)

        shadow(sharedLibs.semver)
        shadow(sharedLibs.fastutil.concurrent.wrapper) {
            isTransitive = false
        }

        runtimeOnly(mainLibs.antlr4.runtime)
        implementation(mainLibs.glsl.transformer)
        implementation(mainLibs.jcpp)

        implementation(mainLibs.sodium)
        implementation(mainLibs.iris)
    }
}