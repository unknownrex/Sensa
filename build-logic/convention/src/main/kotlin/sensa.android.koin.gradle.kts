import com.unknownrex.sensa.buildlogic.lib

dependencies {
    add("implementation", platform(lib("koin-bom")))
    add("implementation", lib("koin-android"))
    add("implementation", lib("koin-androidx-compose"))
}
