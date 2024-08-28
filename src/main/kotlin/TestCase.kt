/**
 * Base class for a test case. Each test should strive to test a specific "category" of backup items.
 *
 * Note that tests should be deterministic. All the prebuilt generators are already deterministic, using a seeded
 * random generator. If you need your own random values outside the context of a generator, be sure to use [SeededRandom].
 *
 * @param baseFileName The root of the file name that will be used for test output. If the value is "foo", the output
 *                     will be "foo_00.binproto", "foo_01.binproto", etc.
 */
abstract class TestCase(val baseFileName: String) {

  fun initialize() {
    SeededRandom.reset()
  }

  abstract fun PermutationScope.execute()
}
