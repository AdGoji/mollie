# ChangeLog #

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] ##

## [0.3.1] - 2023-07-13 ##

### Fixed ###

- Unable to produce JSON with `java.time.LocalDate` ([#11](https://github.com/AdGoji/mollie/issues/11)).

## [0.3.0] - 2023-07-12 ##

### Added ###

- New client option: `throw-exceptions?` ([#9](https://github.com/AdGoji/mollie/issues/9)).

### Changed ###

- Client option `:check-response` is renamed to `:check-response?` ([#9](https://github.com/AdGoji/mollie/issues/9)).

## [0.2.0] - 2022-06-07 ##

### Fixed ###

- Spec error during fetching all entities with pagination ([#7](https://github.com/AdGoji/mollie/issues/7)).

### Changed ###

- Response format for mandates and payments. Nested `details` map was
  brought to the top level with different namespace.

## [0.1.4] - 2023-06-05 ##

### Added ###

- README and documentation strings ([#3](https://github.com/AdGoji/mollie/issues/3)).

### Fixed ###

- Include spec to the classpath ([#5](https://github.com/AdGoji/mollie/issues/5)).

## [0.1.3] - 2023-06-29 ##

### Added ###

- Customers management ([#1](https://github.com/AdGoji/mollie/issues/1)).
- Payments management ([#1](https://github.com/AdGoji/mollie/issues/1)).
- Mandates management ([#1](https://github.com/AdGoji/mollie/issues/1)).
- Subscriptions management ([#1](https://github.com/AdGoji/mollie/issues/1)).

[unreleased]: https://github.com/AdGoji/mollie/compare/0.3.1..HEAD
[0.3.1]: https://github.com/AdGoji/mollie/compare/0.3.0..0.3.1
[0.3.0]: https://github.com/AdGoji/mollie/compare/0.2.0..0.3.0
[0.2.0]: https://github.com/AdGoji/mollie/compare/0.1.4..0.2.0
[0.1.4]: https://github.com/AdGoji/mollie/compare/0.1.3..0.1.4
[0.1.3]: https://github.com/AdGoji/mollie/releases/tag/0.1.3
