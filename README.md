# Json Configuration

A simple implementation to allow .json configuration handling with Bukkit.

## Compiling

This project utilizes Maven as build and dependency management tool.

1. Make sure to have Maven and Git installed.
2. Clone this repository with `git clone https://github.com/WinX64/json-configuration.git`
3. Build the project and install it on your machine with `mvn clean install`
4. Use it on whatever project you desire. Make sure that the dependency is available at runtime.

## How to use

As this library was made to be compatible with the existing configuration API, it can be easily swapped in already written code.

All that is necessary is a change in the line that loads the configuration, from `YamlConfiguration.loadConfiguration(...);` to `JsonConfiguration.loadConfiguration(...);`.

Writing, reading and saving the configuration stays the same.

## Contributing

Feel free to open a pull request if you have any improvement or fix to propose, and it will be reviewed as soon as possible.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* The Bukkit Team for providing the base of the configuration API.
* Google for providing the [gson](https://github.com/google/gson) library.
