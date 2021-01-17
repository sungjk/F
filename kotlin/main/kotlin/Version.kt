/**
 * Created by jeremy on 2021/01/18.
 */
data class Version(val major: Int, val minor: Int, val patch: Int) : Any() {
    companion object {
        operator fun invoke(version: String): Version {
            val split = version.split(".")
            return when {
                split.size >= 3 -> Version(split[0].toInt(), split[1].toInt(), split[2].toInt())
                split.size == 2 -> Version(split[0].toInt(), split[1].toInt(), 0)
                split.size == 1 -> Version(split[0].toInt(), 0, 0)
                else -> Version(0, 0, 0)
            }
        }
    }

    operator fun compareTo(that: Version): Int =
        if (major > that.major) 1
        else if (major == that.major && minor > that.minor) 1
        else if (major == that.major && minor == that.minor && patch > that.patch) 1
        else if (major == that.major && minor == that.minor && patch == that.patch) 0
        else -1

}
