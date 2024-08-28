# Signal Message Backup Tests
The goal of this project is to provide a set of shared backup test files that clients can use to validate that they are 
importing and exporting data correctly. They can do this by importing the test file, exporting a new one, and verifying
that the new file is functionally equivalent to the original (using the [libsignal](https://github.com/signalapp/libsignal) comparator).

The tests themselves are generated through a Kotlin DSL that allows for the easy creation of permutations of a given
proto. This helps ensure that we have strong coverage over a wide range of possible backup files.

# Using shared test cases
The test cases are located in [test-cases](test-cases). The actual backup file has a `.binproto` extension. Each of 
these is paired with a `.txtproto` file that contains a human-readable version of the backup file to aid in debugging.

The `.binproto`'s themselves are unencrypted and un-gzipped. This is to avoid any need for a shared key.

# Creating test cases
If you import this project into IntelliJ, it comes with a template for creating a new test case. If you right-click
the `tests` package and click `New > Signal Backup Test Case`, you should have a basic template. Everything is generated
with a largely straightforward kotlin DSL. There's lots of existing test cases you can use as an example to see how
things are put together and how you can easily generate permutations of a given proto.

After creating the test file, add it to the `ALL_TEST_CASES` list in `Main.kt`, and run the project. If you're not using
Intellij, you can simply run `./gradlew run` from the terminal.