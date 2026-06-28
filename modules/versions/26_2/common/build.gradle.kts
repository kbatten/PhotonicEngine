plugins {
    id("net.fabricmc.fabric-loom")
    `photonics-common`
}

loom {
    accessWidenerPath.set(file("src/main/resources/.accesswidener"))
}