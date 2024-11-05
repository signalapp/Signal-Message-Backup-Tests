import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.thoughtcrime.securesms.backup.v2.proto.Frame
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.Base64
import java.util.UUID
import kotlin.reflect.full.declaredMemberProperties

/**
 * Writes a 32-bit variable-length integer to the stream.
 *
 * The format uses one byte for each 7 bits of the integer, with the most significant bit (MSB) of each byte indicating whether more bytes need to be read.
 */
fun OutputStream.writeVarInt32(value: Int) {
  var remaining = value

  while (true) {
    // We write 7 bits of the integer at a time
    val lowestSevenBits = remaining and 0x7F
    remaining = remaining ushr 7

    if (remaining == 0) {
      // If there are no more bits to write, we're done
      write(lowestSevenBits)
      return
    } else {
      // Otherwise, we need to write the next 7 bits, and set the MSB to 1 to indicate that there are more bits to come
      write(lowestSevenBits or 0x80)
    }
  }
}

/**
 * Reads a 32-bit variable-length integer from the stream.
 *
 * The format uses one byte for each 7 bits of the integer, with the most significant bit (MSB) of each byte indicating whether more bytes need to be read.
 * If the MSB is 0, it indicates the final byte. The actual integer value is constructed from the remaining 7 bits of each byte.
 */
fun InputStream.readVarInt32(): Int {
  var result = 0

  // We read 7 bits of the integer at a time, up to the full size of an integer (32 bits).
  for (shift in 0 until 32 step 7) {
    // Despite returning an int, the range of the returned value is 0..255, so it's just a byte.
    // I believe it's an int just so it can return -1 when the stream ends.
    val byte: Int = read()
    if (byte < 0) {
      return -1
    }

    val lowestSevenBits = byte and 0x7F
    val shiftedBits = lowestSevenBits shl shift

    result = result or shiftedBits

    // If the MSB is 0, that means the varint is finished, and we have our full result
    if (byte and 0x80 == 0) {
      return result
    }
  }

  throw IOException("Malformed varint!")
}

/**
 * Reads the specified number of bytes from the stream and returns it as a [ByteArray].
 * Throws an [IOException] if the stream doesn't have that many bytes.
 */
@Throws(IOException::class)
fun InputStream.readNBytesOrThrow(length: Int): ByteArray {
  return this.readNBytes(length).also {
    if (it.size != length) {
      throw IllegalStateException("Not enough bytes!")
    }
  }
}

fun UUID.toByteArray(): ByteArray {
  val buffer: ByteBuffer = ByteBuffer.wrap(ByteArray(16))
  buffer.putLong(this.mostSignificantBits)
  buffer.putLong(this.leastSignificantBits)

  return buffer.array()
}

fun base64Decode(value: String): ByteArray {
  return Base64.getDecoder().decode(value)
}

/** Synthetic value to make it easier to get ACI's off of frames that you know are Contacts with ACI's */
val Frame.aci: ByteString
  get() = this.recipient!!.contact!!.aci!!

fun UUID.toByteString(): ByteString = this.toByteArray().toByteString()

/**
 * Takes a class and converts it to a nice, multi-line string with good indentation. Optimized for readability.
 */
@OptIn(ExperimentalStdlibApi::class)
fun Any?.prettyPrint(indent: String = ""): String {
  when (this) {
    null -> return "${indent}null"
    is Int,
    is Long,
    is Float,
    is Double,
    is Boolean -> return "${indent}$this"
    is String -> return "${indent}\"$this\""
    is ByteArray -> return "<${this.toHexString(HexFormat.Default)}>"
    is ByteString -> return "<${this.toByteArray().toHexString(HexFormat.Default)}>"
    is Enum<*> -> return "${indent}${this::class.simpleName}.${this.name}"
  }

  val clazz = this!!::class
  val fields = clazz.declaredMemberProperties

  if (fields.isEmpty()) {
    return "${indent}${clazz.simpleName} {}"
  }

  val fieldsString = clazz.declaredMemberProperties
    .filter {
      when (val value = it.getter.call(this)) {
        is List<*> -> value.isNotEmpty()
        is Boolean -> value != false
        is Long -> value != 0L
        is Int -> value != 0
        else -> value != null
      }
    }
    .joinToString(separator = "\n") { field ->
      when (val value = field.getter.call(this)) {
        is List<*> -> {
          if (value.isEmpty()) {
            "$indent  ${field.name} = []"
          } else {
            "$indent  ${field.name} = [\n${value.joinToString(separator = ",\n") { it.prettyPrint(indent = "$indent    ") }}\n$indent  ]"
          }
        }
        else -> {
          "$indent  ${field.name} = ${value.prettyPrint(indent = "$indent  ")}"
        }
      }
    }

  return "$indent${clazz.simpleName} {\n$fieldsString\n$indent}"
    .replace(Regex("=\\s+"), "= ")
    .replace(Regex("\\{\\s+}", RegexOption.MULTILINE), "{}")
}
